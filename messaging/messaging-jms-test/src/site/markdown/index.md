# About `jguru-shared-messaging-jms-test`

The Shared Messaging JMS Test simplifies creating automated tests for sending JMS Messages, including simple handling
of an in-memory (but fully operative) MessageBroker. The MessageBroker of choice is 
[Apache Artemis](https://activemq.apache.org/artemis/), but the JMS Test structure permits integrating any type of JMS 
broker without changing the structure.

The API contains mainly 4 structures:

1. **AbstractJmsTest**: Main specification which should be extended when creating simple JMS tests.
   Abstract subclasses adapt this specification to a particular broker, such as Artemis. 
2. **AbstractRequestResponseJmsTest**: Abstract superclass for more realistic Request/Response tests containing a 
   JMS MessageBroker. Abstract subclasses adapt this specification to a particular broker, such as Artemis.
3. **MessageBroker**: Specification for how to integrate a Messsage Broker into the JMS test structure.
   Concrete implementations exist for Artemis, but is pluggable for arbitrary products.  
4. **ArtemisTestClient**: Concrete utility class which enable simple (and programmatic) access to the Artemis 
   JMS broker. For more generic approaches, simply use the structures found within the `jguru-shared-messaging-api`.
   (However, this is illustrated within the Example projects as well).    

The MessageBroker type structure is illustrated below, including the concrete Artemis implementations:

![MessageBroker Structure](images/plantuml/messageBrokerStructures.png "Messaging Broker Structures")

## Code examples

The samples below should illustrate how to use the Messaging JMS Test component. Remember to import it in `test` 
scope in projects intending to use its functionality:

        <dependency>
            <groupId>se.jguru.shared.messaging.test.jms</groupId>
            <artifactId>jguru-shared-messaging-jms-test</artifactId>
            <version>whicheverVersion</version>
            <scope>test</scope>
        </dependency>

#### Kotlin: Request/Response JMS Test

The JMS test structure is illustrated below:

![JMS Test Structure](images/plantuml/jmsTestStructures.png "JMS Test Structure")

This code example extends the `AbstractArtemisRequestResponseJmsTest` and the `AbstractTransactionalMessageListener` to
illustrate the 

First, create an AbstractTransactionalMessageListener subclass to implement the service-side message handling.
Refer to the `AbstractTransactionalMessageListener` to find the internals - such as the `serverSideResponseSession` 
which holds the JMS Session used by the service implementation to connect to the Broker. This Session is 
**separate from the client-side JMS Session**. Hence, the service side implementation (which should be a 
`MessageListener` subclass) executes as one JMS client, and the client-side executes as another JMS client.

Despite this, the Service-side MessageListener is simple to implement (override a single method):    

      class CorrelationIdMessageListener(
        session: Session,
        messageProducer: MessageProducer,
        val serverSideReceivedMessages: MutableList<Message>
      ) : AbstractTransactionalMessageListener(session, messageProducer) {
      
      
        override fun <T : Message> generateResponse(inboundRequestMessage: Message): T {
      
          // Add the inbound message, for tracking.
          serverSideReceivedMessages.add(inboundRequestMessage)
      
          // Create the outbound Message
          val inboundTextMessage = inboundRequestMessage as TextMessage
          val toReturn = serverSideResponseSession.createTextMessage("Received inbound: ${inboundTextMessage.text}") as T
          toReturn.jmsCorrelationID = inboundTextMessage.jmsMessageID
      
          return toReturn
        }
      }

Then create the AbstractArtemisRequestResponseJmsTest subclass, using the Service-side MessageListener within the
`getServiceSideListener` method, and the client-side (i.e. unit test) code within the tests. Note the following:

1. **Messaging Tests are Asynchronous and Threaded**. We need to set up `CountdownLatch`es to ensure that execution in
   the unit test thread does not continue until a message has been properly consumed on the service side and re-sent
   to the client side. 
2. **CountdownLatch**. Thread synchronization can be done by the `CountDownLatch` structure from the standard JDK 
   library. Such objects are normally created within the UnitTest thread and given a countdown value 
   (i.e. `val receivedMessagesLatch = CountDownLatch(1)`). The `countDown` method is then called either within the 
   Service-side thread or the UnitTest MessageListener thread as illustrated in the example below. 
   Finally, the `await` method halts execution (of the UnitTest thread) until the condition is met (i.e. the `countDown`
   method is invoked as many times as the initially given value or a timeout has been reached).
   It is recommended to use the await form with a timeout, as shown in the code sample below.
3. **Transactions**. Mind that the test uses Message Broker Transactions, implying that Messages will be sent only 
   when the user-level transaction is `commit`-ed. 

         
        open class AbstractArtemisRequestResponseTestTest : AbstractArtemisRequestResponseJmsTest(true) {
      
        // Override the getServiceSideListener method to create the ATM
        
        override fun getServiceSideListener(
          serverSideReceivedMessages: MutableList<Message>,
          serverSideResponseSession: Session,
          responseMessageProducer: MessageProducer): AbstractTransactionalMessageListener {
      
          return CorrelationIdMessageListener(serverSideResponseSession, responseMessageProducer, serverSideReceivedMessages)
        }
      
        @Test
        @Throws(Exception::class)
        fun validateTextMessageYieldsCorrectResponse() {
      
          // Assemble
          val clientMessage = "This is a client-side originated message."
          val receivedClientResponses = CopyOnWriteArrayList<Message>()
      
          val clientConnection = createConnection()
          val clientRequestSession = createSession(clientConnection)
          val clientRequestQueue = clientRequestSession.createQueue(CLIENT_SIDE_OUTBOUND_REQUEST)
      
          val clientResponseSession = createSession(clientConnection)
          val clientResponseQueue = clientResponseSession.createQueue(CLIENT_SIDE_INBOUND_RESPONSE)
      
          val clientRequestProducer = clientRequestSession.createProducer(clientRequestQueue)
          clientRequestProducer.deliveryMode = DeliveryMode.NON_PERSISTENT
      
          val receivedMessagesLatch = CountDownLatch(1)
      
          val clientResponseConsumer = clientResponseSession.createConsumer(clientResponseQueue)
          val clientResponseListener = MessageListener { message ->
            receivedClientResponses.add(message)
            receivedMessagesLatch.countDown()
          }
          clientResponseConsumer.messageListener = clientResponseListener
      
          // Act
          val toSend = clientRequestSession.createTextMessage()
          toSend.setStringProperty("foo", "bar")
          toSend.text = clientMessage
          clientRequestProducer.send(toSend)
          clientRequestSession.commit()
      
          val correctlyReceivedMessages = receivedMessagesLatch.await(2, TimeUnit.SECONDS)
      
          // Assert
          Assert.assertTrue(correctlyReceivedMessages)
      
          Assert.assertEquals("Expected 1 received client response message, but got [" + receivedClientResponses.size
                                + "]: " + receivedClientResponses, 1, receivedClientResponses.size)
          Assert.assertEquals("Expected 1 received server message, but got: $serverSideReceivedMessages",
                              1, serverSideReceivedMessages.size)
      
          val response = receivedClientResponses[0] as TextMessage
          Assert.assertEquals(response.text, "Received inbound: $clientMessage")
        }
        }

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png) 