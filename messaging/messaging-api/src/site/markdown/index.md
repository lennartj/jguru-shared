# About `jguru-shared-messaging-api`

The Shared Messaging API simplifies synthesizing and sending JMS messages to/through a message broker.
The bearing idea is to hide the JMS-specific parts, and instead synthesize messages using a 
Map (which contains the message headers) and a single object for message payload. This method of working
drastically increases testability and reduces the need for an operative broker within unit test code. 

The API contains mainly 2 structures:

1. **MessagingHelper**: Main specification for how to send messages and creating JMS destinations.
   Has 2 subclasses for working with messing within a JavaSE or a JavaEE context. 
   These structures hides the JMS API complexity behind 3 methods.   
2. **JmsCompliantMap**: SortedMap implementation which permits adding values only if they are 
   compliant with the JMS specification for header properties. This structure permits working with a Map 
   when synthesizing or collecting JMS message headers and hence transforms the JMS API experience to 
   working with a Map.

While other structures exist, they are mainly enumerations granting type safety for JMS constants or pure 
algorithms which are normally invoked from within the concrete MessagingHelper subclasses. 
The types within the messaging API have simple relations:

![MessagingHelper Structure](images/plantuml/messagingHelper.png "Messaging Helper Structure")

## Code examples

The samples below should illustrate how to use the Messaging API from Java and Kotlin.

#### Java: Sending a JMS Message

It is/should be trivial to fully tailor a JMS Message to send using the Messaging API, as shown by the 
code sample below:

      // Compile JMS headers by synthesizing a JmsCompliantMap         
      final JmsCompliantMap sentProperties = new JmsCompliantMap();
      sentProperties.put("someStringProperty", "someStringValue");
      sentProperties.put("someLongProperty", 42L);
      sentProperties.put("someByteArrayProperty", new byte[]{1, 4, 56, 127});
  
      // Send a message with the JMS headers extracted from the JmsCompliantMap and given body.
      // Since the body is a String, a JMS TextMessage will be created and sent.
      final String jmsMessageID = messagingHelper.send(sentProperties, "This is a message body", destination);
      
#### Kotlin: Sending a JMS Message

It is/should be trivial to fully tailor a JMS Message to send using the Messaging API, as shown by the 
code sample below:

      // Compile JMS headers by synthesizing a JmsCompliantMap         
      val sentProperties = JmsCompliantMap()
      sentProperties["someStringProperty"] = "someStringValue"
      sentProperties["someLongProperty"] = 42L
      sentProperties["someByteArrayProperty"] = byteArrayOf(1, 4, 56, 127)
      
      // Send a message with the JMS headers extracted from the JmsCompliantMap and given body.
      // Since the body is a String, a JMS TextMessage will be created and sent.
      val jmsMessageID = messagingHelper.send(sentProperties, "This is a message body", destination)      
      
#### Java: Creating a Queue

It is/should be trivial to create a JMS Destination, as shown by the code sample below:

      // Get a JMS Session from the broker, and wrap it in a MessagingHelper
      final Session session = ... 
      final MessagingHelper messagingHelper = new JmsSessionMessagingHelper(session);
      
      // Create the queue to which messages should be sent
      final Queue destination = messagingHelper.createQueue(DESTINATION_NAME);  
      
#### Kotlin: Creating a Queue

It is/should be trivial to create a JMS Destination, as shown by the code sample below:

      // Get a JMS Session from the broker, and wrap it in a MessagingHelper
      val session : Session = ... 
      val messagingHelper = new JmsSessionMessagingHelper(session);
      
      // Create the queue to which messages should be sent
      val destination = messagingHelper.createQueue(DESTINATION_NAME);

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png)