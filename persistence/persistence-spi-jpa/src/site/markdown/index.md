# About `jguru-shared-persistence-spi-jpa`

The Shared Persistence JPA SPI provides shallow/non-invasive utilities for interacting with 
(SQL) databases using the Java Persistence API. 

The SPI contains algorithms and structures to simplify the following operations:

1. **Standard JPA Converters**: AttributeConverter implementations for several commonly used JDK-based classes,
   effectively turning them into first-class JPA citizens without needing to implement special
   conversion logic.
2. **Simplified PersistenceProvider access**: The default JPA mechanics relies upon system properties to select
   the JPA provider for use. The Persistence JPA SPI supplies enum-based mechanics to inject
   the provider in a simpler way. 
3. **Test-utility ClassLoader**: The default JPA specification does not allow using any file other than the
   *META-INF/persistence.xml* to specify persistence context/unit properties. This classloader permits using 
   placing the persistence specification in other areas, which is convenient when running unit tests using 
   JPA mechanics.
4. **JpaHelper**: Commonly used JPA algorithms to find any active PersistenceProviders and extract SQL Schema 
   definitions from a supplied `persistence.xml` file.