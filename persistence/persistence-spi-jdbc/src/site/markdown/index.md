# About `jguru-shared-persistence-spi-jdbc`

The Shared Persistence JDBC SPI provides shallow/non-invasive utilities for interacting with SQL databases
without using JPA. 

The SPI contains algorithms to simplify the following operations:

1. **Synthesis**: Generating SQL statements by tokenizing SQL template strings.
2. **Common Template Storage Format**: Utilities to store SQL templates, parameters and classification in 
   a common  format.
3. **Common Substitution Tokens**: Define standard substitution tokens.
4. **Algorithms**: Defines some commonly used algorithms.

The typical use of the Persistence JDBC SPI is illustrated in the examples below and in the unit tests of this project.

## SQL statement storage

It is normally good to have a structured, and common storage for SQL statements used within the DAO layer of a system. 
While not requiring this storage to operate properly, the `jguru-shared-persistence-spi-jdbc` component contains a 
structure for storing SQL statements within a simple JSON format. The format is defined within the class
`SqlStatements` which contain a human-readable systemName and all relevant SQL grouped by function (i.e. 
CREATE, READ, UPDATE, DELETE) as illustrated below.

Typically, reading the statements into memory is done using Jackson deserialization.
Assuming that the file is located within the resource path `sql/MySqlStatements.json`, and that the class
`SomeDaoFactory` is packaged within the same JAR as the statements: 

    // Deserialize the SqlStatements
    val sqlText = PropertyResources.readFully("sql/MySqlStatements.json", SomeDaoFactory::class.java.classLoader)
    val sqlStatements = JacksonAlgorithms.deserialize(sqlText, SqlStatements::class.java)
    
    // Retrieve a SQL statement
    val sql = sqlStatements.getStatement("readUserByJpaID", SqlStatementType.READ).tokenize()
    
### SQL Tokens

SQL tokenization must be quick, safe and non-limiting, implying that not all types of string tokenizations are
permitted or supported. Instead, the class `SqlTemplateSubstitution` contains a limited - but normally sufficient - 
amount of tokens which are substituted within the SQL template when the tokenize() method is invoked.

Of these tokens, the PARAMS, SET_PARAMS and ARGUMENTS are normally synthetic (i.e. calculated during 
tokenization), whereas the other as simply substituted by strings. Illustrated below, a typical CREATE 
statement should supply column names which match the arguments for an insert statement. This is conveniently 
done using a template on the form `insert into organisations.credentials (##PARAMS##) values (##ARGUMENTS##)`, where 
the `##PARAMS##` token is substituted for the string given within the `params` value. 
This - in itself - is not too productive, but the `##ARGUMENTS##` bit is calculated to match the number 
of given parameters automatically.

In the same manner, updating a large number of columns quickly gets somewhat repetitive in plan SQL.
Hence the token SET_PARAMS can be used along with a template on the form
`update organisations.foobar ##SET_PARAMS## where id = ?`. The value of the `SET_PARAMS` should be
only the column names (i.e. `foo, bar, baz`) and the SqlStatement tokenization generates the required
SQL: `update organisations.foobar foo = ?, bar = ?, baz = ? where id = ?` 

## SQL Statement Storage form

The storage of SQL matches the JSON structure of the SqlStatements class as indicated above. 
A sample is shown below. 

    {
        "systemName": "TheDatabaseOrSystemName",
        "statements": {
            "READ": [
                {
                    "identifier": "readOrganisationalGroupRowData",
                    "sqlType": "READ",
                    "template": "select ##DISTINCT## og.id, og.description, og.emaillistname, og.latestimportupdate, og.legacyid, og.maxmembers, og.name, og.organisation_id, og.parent_group_id, og.abbreviation, cat.id, cat.parent_category_id from organisations.organisationalgroup og left join organisations.categories_for_group on og.id = categories_for_group.group_id left join organisations.category cat on categories_for_group.category_id = cat.id ##WHERE## order by og.parent_group_id nulls first, og.id, og.organisation_id, cat.parent_category_id nulls first, cat.id"
                },
                {
                    "identifier": "readUserByJpaID",
                    "sqlType": "READ",
                    "template": "select ##PARAMS## from organisations.membership mship join organisations.organisation org on mship.organisation_id = org.id join organisations.organisationuser ou on mship.user_id = ou.id where ou.id = ?",
                    "params": "ou.id, ou.firstname, ou.lastname, ou.birthday, ou.email, ou.phone, ou.username, ou.istestuser, org.id, org.abbreviation, mship.loginPermitted, mship.startdate, mship.enddate, mship.systemnote"
                }
            ],    
            "CREATE": [
                {
                    "identifier": "createCredentialsForUserJpaID",
                    "sqlType": "CREATE",
                    "template": "insert into organisations.credentials (##PARAMS##) values (##ARGUMENTS##)",
                    "params": "algorithm, hashiterations, lastupdatedat, noteonupdate, salt, type, value, user_id"
                }
            ],
            "UPDATE": [
                {
                  "identifier": "updateEmailForUserJpaID",
                  "sqlType": "UPDATE",
                  "template": "update organisations.organisationuser set #SET_PARAMS# where id = ?",
                  "params": "email, emailverifiedat"
                }                            
            ],
            "DELETE": [            
            ]
        }
    }
    
# DbOperations convenience methods

Assuming that we have access to a DataSource (it is recommended to use a Hikari DataSource unless a DataSource of 
another type is supplied to you), the stereotypic statement to read and convert DB data is shown below. The 
convenience DB operation algorithms are defined within the singleton object `DbOperations` which you may use
to simplify the process to read DB data and convert it to objects as well as bridge the JDBC accessor methods.

Note that all of the JDBC methods which retrieve numeric objects may represent SQL null as 0 (c.f. the JDBC 
documentation for more info on this). The convenience methods `DbOperations.getXXorNull` ensure that all SQL null 
values are returned as `null` values.

Hence, a snippet which reads DB data and converts it to objects is shown below.

        // Assemble
        val sql = "select id, firstName, lastName from person order by id"
        val personConverter = { rs: ResultSet, rowIndex: Int ->

            val id = DbOperations.getIntOrNull(1, rs)
            val firstName = rs.getString(2)
            val lastName = rs.getString(3)

            Person(id!!, firstName, lastName)
        }

        // Act
        val people = DbOperations.readAndConvert(dataSource, sql, personConverter)
            .map { Pair(it.id, it) }
            .toMap()
    
Of course, we may combine the statements above and refactor the code to something like the below, where
all converting and fetching is compressed into a single statement:  

        val search = ... get search or filter criterion from user input ...
        val section = ... some state within the class, injected at construction ...
        
        // Find all users   
        val readData = DbOperations.readAndConvert(
                connectionPool,
                sqlStatements.getStatement("readUsersBySectionAndSearchCriterion", SqlStatementType.READ).tokenize(),
                { rs, rowIndex -> getRowConverterFor(section, rs, rowIndex) },
                mutableListOf(section.jpaId, lcAndEscape(search), lcAndEscape(search)))
                
## Returning Generated Primary Keys

Most modern databases support DB-generated primary keys. However, not all JDBC drivers support intuitively knowing
which columns in a resultset were auto-generated. Therefore, we must supply the names of all generated primary keys
we want to return from an insert SQL statement. This takes the form of an array:

        val theInsertSQL = "insert into nickname (nick) values (?)"
        val nicks = ...
        
        val insertMetadata = DbOperations.insertOrUpdate(dataSource, nickInsertSQL, nicks, arrayOf("id")) { arrayOf(it) }                 
        val genPKs = insertNicknamesMetadata.generatedPrimaryKeys 
        
The List of retrieved generated keys has the signature `List<Any?>` and contains the generated primary keys
in the order returned by the database.   

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png) 