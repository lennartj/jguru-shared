package se.jguru.shared.persistence.spi.jdbc

import org.junit.Assert
import org.junit.Test
import se.jguru.shared.persistence.spi.jdbc.helpers.Person
import se.jguru.shared.persistence.spi.jdbc.helpers.PersonAndPetView
import se.jguru.shared.persistence.spi.jdbc.helpers.Pet
import java.sql.ResultSet
import java.util.Locale
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet
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

    @Test
    fun validateInsertingData() {

        // Assemble
        val petInsertSQL = "insert into pet (id, petName, petType) values (?, ?, ?)"
        val ownerInsertSQL = "insert into pet_to_person (personId, petId) values (?, ?)"

        val dogs = (10..19).map { Pet(it, "Fluffy_$it", "dog") }
        val owners = (10..19).map { Pair(1, it) }

        // Act
        val petsInserted = DbOperations.insert(dataSource, petInsertSQL, dogs) { arrayOf(it.id, it.name, it.type) }
        val numOwnerRows = DbOperations.insert(dataSource, ownerInsertSQL, owners)
        { arrayOf(it.first, it.second) }

        // Assert
        Assert.assertEquals(10, petsInserted)
        Assert.assertEquals(10, numOwnerRows)

        val db = getDbStructure()
        Assert.assertEquals(2, db.size)

        val personWithManyPets = db.first { it.id == 1 }
        Assert.assertEquals(11, personWithManyPets.pets.size)
    }

    @Test
    fun validateUpdatingData() {

        // Assemble
        val ownerUpdateSQL = "update person set firstName = ?, lastName = ? where id = ?"
        val updatedOwnerValues = listOf(Person(1, "Fleptut", "Jörelid"))

        // Act
        val numRowsModified = DbOperations.update(dataSource, ownerUpdateSQL, updatedOwnerValues)
        { arrayOf(it.firstName, it.lastName, it.id) }

        // Assert
        Assert.assertEquals(1, numRowsModified)

        val db = getDbStructure()
        Assert.assertEquals(2, db.size)

        val theLennart = db.first { it.id == 1 }
        Assert.assertEquals("Fleptut", theLennart.firstName)
        Assert.assertEquals("Jörelid", theLennart.lastName)
    }

    //
    // Private helpers
    //

    private fun getDbStructure() : SortedSet<Person> {

        val sql = "select p.id, p.firstName, p.lastName, pet.id, pet.petName, pet.petType from person p " +
            "join pet_to_person ptp on p.id = ptp.personId " +
            "join pet on pet.id = ptp.petId " +
            "order by p.id, pet.id";

        val personAndPetConverter = { rs: ResultSet, _: Int ->

            val personId = DbOperations.getIntOrNull(1, rs)
            val firstName = rs.getString(2)
            val lastName = rs.getString(3)
            val petId = DbOperations.getIntOrNull(4, rs)
            val petName = rs.getString(5)
            val petType = rs.getString(6)

            PersonAndPetView(personId!!, firstName, lastName, petId!!, petName, petType)
        }

        val ppView = DbOperations.readAndConvert(dataSource, sql, personAndPetConverter)
        val personMap = TreeMap<Int, Person>()

        ppView.forEach {

            val person = when(personMap[it.personId] == null) {
                false -> personMap[it.personId]!!
                else -> {

                    val person = Person(it.personId, it.firstName, it.lastName)
                    personMap[person.id] = person
                    person
                }
            }

            person.pets.add(Pet(it.petId, it.petName, it.petType))
        }

        val toReturn = TreeSet<Person>()
        toReturn.addAll(personMap.values)

        return toReturn
    }
}
