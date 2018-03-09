package se.jguru.shared.entity.test

import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.TimeZone

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class TimeZoneRuleTest {

    val fijiTimeZoneID = "Pacific/Fiji"
    val perthTimeZoneID = "Australia/Perth"
    lateinit var desiredTimeZone: TimeZone

    @Rule
    @JvmField
    var tzRule: TimeZoneRule = when (TimeZone.getDefault().id) {
        fijiTimeZoneID -> TimeZoneRule(TimeZone.getTimeZone(perthTimeZoneID))
        else -> TimeZoneRule(TimeZone.getTimeZone(fijiTimeZoneID))
    }

    @Before
    fun setupSharedState() {

        // Arrays.stream(TimeZone.getAvailableIDs()).sorted().forEach { println("[$it]") }
        desiredTimeZone = tzRule.desiredTimeZone!!
    }

    @Test
    fun validateDesiredTimeZone() {

        // Act & Assert
        Assert.assertEquals(desiredTimeZone, TimeZone.getDefault())
    }
}