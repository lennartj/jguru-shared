package se.jguru.shared.persistence.spi.jdbc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import se.jguru.shared.algorithms.api.resources.PropertyResources
import se.jguru.shared.json.spi.jackson.JacksonAlgorithms

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class SqlStatementTest {

    lateinit var unitUnderTest: SqlStatements

    @BeforeEach
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
            assertThat(resurrectedStatementList.size).isEqualTo(statementList.size)

            statementList.forEachIndexed { index, sqlStatement ->
                assertThat(sqlStatement.numParameters).isEqualTo(1)
                assertThat(sqlStatement.compareTo(resurrectedStatementList[index])).isEqualTo(0)
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
        assertThat(statement).isNotNull
        assertThat(sql).isNotNull

        assertThat(template).contains(SqlTemplateSubstitution.DISTINCT.token())
        assertThat(template).contains(SqlTemplateSubstitution.WHERE.token())

        assertThat(sql).doesNotContain(SqlTemplateSubstitution.DISTINCT.token())
        assertThat(sql).doesNotContain(SqlTemplateSubstitution.WHERE.token())

        assertThat(sql).contains("distinct")
        assertThat(sql).contains(whereClause)
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
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun validateSplittingStringIntoListInKotlinManner() {

        // Assemble
        val expected = listOf("algorithm", "hashiterations", "lastupdatedat")
        val params = "algorithm, hashiterations, lastupdatedat"

        // Act
        val result = SqlStatement.split(params)

        // Assert
        assertThat(result.size).isEqualTo(3)
        for(index in expected.indices) {
            assertThat(result[index]).isEqualTo(expected[index])
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
        assertThat(result).isEqualTo(expected)
    }
}