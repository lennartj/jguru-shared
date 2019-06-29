package se.jguru.shared.persistence.spi.jdbc

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.persistence.spi.jdbc.helpers.Person
import java.sql.ResultSet
import java.util.Locale
import javax.sql.DataSource

class DbOperationsTest : AbstractJdbcTest() {

    override fun createDataSource(): DataSource = createDatabasePool(
        "jdbc:hsqldb:mem:unitTestDB",
        "sa",
        "")

    override fun getInitializeDbStateSqlStatements(): List<String> {

        fun createTable(name: String) = "create table if not exists $name (id int primary key,"
        fun deleteFromTable(name: String) = "delete from $name"
        fun insertIntoTable(name: String) = "insert into $name values ("

        return listOf(

            // Create the tables
            "${createTable("person")} firstName varchar(64) not null, lastName varchar(64) not null)",
            "${createTable("pet")} petName varchar(64) not null, petType varchar(64) not null)",
            "create table if not exists pet_to_person (personId int, petId int, " +
                "foreign key (personId) references person(id), foreign key (petId) references pet(id), " +
                "constraint jump_id primary key (personId, petId))",

            // Truncate each table
            deleteFromTable("pet_to_person"),
            deleteFromTable("pet"),
            deleteFromTable("person"),

            // Create data within each table
            "${insertIntoTable("pet")} 1, 'fido', 'dog')",
            "${insertIntoTable("pet")} 2, 'pluto', 'cat')",
            "${insertIntoTable("pet")} 3, 'bluppo', 'cat')",
            "${insertIntoTable("person")} 1, 'Lennart', 'Jörelid')",
            "${insertIntoTable("person")} 2, 'Johan', 'Wendels')",
            "${insertIntoTable("pet_to_person")} 1, 2)",
            "${insertIntoTable("pet_to_person")} 2, 1)",
            "${insertIntoTable("pet_to_person")} 2, 3)"
        )
    }

    @Test
    fun validateGettingDatabaseMetadata() {

        // Assemble
        val expected = listOf("PERSON", "PET", "PET_TO_PERSON")

        // Act
        val databaseMetadata = doInConnection { it.metaData }
        val tablesRS = databaseMetadata.getTables(null, null, "%", arrayOf("TABLE"))

        val tableNames = mutableListOf<String>()
        while (tablesRS.next()) {
            tableNames.add(tablesRS.getString(3))
        }

        // Assert
        Assert.assertNotNull(databaseMetadata)
        expected.forEach { Assert.assertTrue(expected.contains(it.toUpperCase(Locale.ENGLISH))) }
    }

    @Test
    fun validateReadingData() {

        // Assemble
        val sql = "select id, firstName, lastName from person order by id"
        val personConverter = { rs: ResultSet, _: Int ->

            val id = DbOperations.getIntOrNull(1, rs)
            val firstName = rs.getString(2)
            val lastName = rs.getString(3)

            Person(id!!, firstName, lastName)
        }

        // Act
        val people = DbOperations.readAndConvert(dataSource, sql, personConverter)
            .map { Pair(it.id, it) }
            .toMap()

        // Assert
        Assert.assertEquals(2, people.size)

        Assert.assertEquals("Jörelid", people[1]?.lastName)
        Assert.assertEquals("Wendels", people[2]?.lastName)
    }
}
