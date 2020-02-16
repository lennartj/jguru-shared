package se.jguru.shared.persistence.spi.jdbc

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.json.spi.jackson.JacksonAlgorithms

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SqlStatementTest {

    lateinit var unitUnderTest: SqlStatements

    @Before
    fun setupSharedState() {

        unitUnderTest = SqlStatements("SomeSystem")

        (1..10).forEach {
            unitUnderTest.addStatement(SqlStatement(
                "statement_$it",
                when {
                    it % 2 == 0 -> SqlStatementType.CREATE
                    else -> SqlStatementType.READ
                },
                "select ##PARAMS## from bar where barling = ?",
                "foo"
            ))
        }
    }

    @Test
    fun validateSerializingToJson() {

        // Assemble
        val expected = PropertyResources.readFully("testdata/sqlStatements.json")

        // Act
        val result = JacksonAlgorithms.serialize(unitUnderTest)
        // println("Got: $result")

        // Assert
        JSONAssert.assertEquals(expected, result, true)
    }

    @Test
    fun validateDeserializingFromJson() {

        // Assemble
        val data = PropertyResources.readFully("testdata/sqlStatements.json")

        // Act
        val resurrected = JacksonAlgorithms.deserialize(data, SqlStatements::class.java)

        // Assert
        unitUnderTest.statements.forEach { entry ->

            val statementList = entry.value
            val resurrectedStatementList = resurrected.statements[entry.key]!!
            Assert.assertEquals(statementList.size, resurrectedStatementList.size)

            statementList.forEachIndexed { index, sqlStatement ->
                Assert.assertEquals(1, sqlStatement.numParameters)
                Assert.assertEquals(0, sqlStatement.compareTo(resurrectedStatementList.get(index)))
            }
        }
    }

    @Test
    fun validateTokenizingSqlTemplate() {

        // Assemble
        val data = PropertyResources.readFully("testdata/sqlStatementsWithTokens.json")
        val whereClause = "apa = any (?)"
        val tokenMap = SqlTemplateSubstitution.builder()
            .distinct()
            .where(whereClause)
            .build()

        // Act
        val statement = JacksonAlgorithms.deserialize(data, SqlStatements::class.java)
            .getStatement("readOrganisationalGroupRowData", SqlStatementType.READ)
        val template = statement.template
        val sql = statement.tokenize(tokenMap)

        // Assert
        Assert.assertNotNull(statement)
        Assert.assertNotNull(sql)

        Assert.assertTrue(template.contains(SqlTemplateSubstitution.DISTINCT.token()))
        Assert.assertTrue(template.contains(SqlTemplateSubstitution.WHERE.token()))

        Assert.assertFalse(sql.contains(SqlTemplateSubstitution.DISTINCT.token()))
        Assert.assertFalse(sql.contains(SqlTemplateSubstitution.WHERE.token()))

        Assert.assertTrue(sql.contains("distinct"))
        Assert.assertTrue(sql.contains(whereClause))
    }

    @Test
    fun validateTokenizingArguments() {

        // Assemble
        val params = "algorithm, hashiterations, lastupdatedat, noteonupdate, salt, type, value, user_id"
        val statement = SqlStatement("someID",
            SqlStatementType.CREATE,
            "insert into organisations.credentials (##PARAMS##) values (##ARGUMENTS##)",
            params)

        val expected = "insert into organisations.credentials ($params) values (?, ?, ?, ?, ?, ?, ?, ?)"

        // Act
        val result = statement.tokenize()

        // Assert
        Assert.assertEquals(expected, result)
    }

    @Test
    fun validateSplittingStringIntoListInKotlinManner() {

        // Assemble
        val expected = listOf("algorithm", "hashiterations", "lastupdatedat")
        val params = "algorithm, hashiterations, lastupdatedat"

        // Act
        val result = SqlStatement.split(params)

        // Assert
        Assert.assertEquals(3, result.size)
        for(index in expected.indices) {
            Assert.assertEquals(expected[index], result[index])
        }
    }

    @Test
    fun validateTokenizingUpdateArguments() {

        // Assemble
        val params = "algorithm, hashiterations, lastupdatedat"
        val statement = SqlStatement("someID",
            SqlStatementType.UPDATE,
            "update organisations.credentials set ##SET_PARAMS## where id = ?",
            params)

        val expected = "update organisations.credentials set algorithm = ?, hashiterations = ?, lastupdatedat = ? where id = ?"

        // Act
        val result = statement.tokenize()

        // Assert
        Assert.assertEquals(expected, result)
    }
}