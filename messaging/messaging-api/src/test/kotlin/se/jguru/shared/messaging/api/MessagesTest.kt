package se.jguru.shared.messaging.api


/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
class MessagesTest {

    /*
    private val testIndex = AtomicInteger(1000)

    lateinit var artemis : EmbeddedActiveMQBroker
    lateinit var jmsContext : JMSContext

    @BeforeEach
    fun setupTestArtemisService() {

        artemis = EmbeddedActiveMQBroker("inVm")
        jmsContext = artemis.jmsContext()
    }

    @AfterEach
    fun teardownTestArtemisService() {
        artemis.stop()
    }

    @Test
    fun validateCopyingDataToAndFromMessage() {

        // Assemble
        val props = JmsCompliantMap()
        props["foo"] = "bar"
        props["meaningOfLife"] = 42L

        // Act
        val textMessage = jmsContext.createTextMessage()
        props.copyPropertiesTo(textMessage)

        // Assert
        val result = Messages.getPropertyMap(textMessage)
        assertThat(result["foo"]).isEqualTo("bar")
        assertThat(result["meaningOfLife"]).isEqualTo(42L)

        // println("Got: $result")

        /*
        Got: {
        JMSDeliveryMode=2,
        JMSDeliveryTime=0,
        JMSExpiration=0,
        JMSPriority=4,
        JMSRedelivered=false,
        JMSTimestamp=1549128518621,
        JMSXDeliveryCount=0,
        foo=bar,
        meaningOfLife=42
        }
         */
    }

    @Test
    fun validateExtractingPropertiesFromMapMessage() {

        // Assemble
        val props = TreeMap<String, Any>()
        props["string"] = "bar"
        props["long"] = 42L
        props["char"] = 'r'
        props["bytes"] = "foonbar".toByteArray()

        // Act
        val mapMessage = jmsContext.createMapMessage()
        Messages.writeToBody(props, mapMessage)

        val result = Messages.readFromBody(mapMessage)

        // Assert
        result.forEach { (k, v) -> assertThat(props[k]).isEqualTo(v) }
    }
     */
}