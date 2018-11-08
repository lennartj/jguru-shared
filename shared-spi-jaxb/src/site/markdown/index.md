# About jGuru-shared-spi-jaxb

Module containing commonly (re-)used JAXB mechanics, including several XmlAdapter implementations
mainly for JavaSE types dealing with Times, Dates, TimeZones, Locales, Currencies etc.
Such adapters reside within the package `se.jguru.shared.spi.jaxb.adapter`. 

Moreover, the SPI provides concrete implementations to `AbstractMarshallerAndUnmarshaller` delivered 
within the `shared-algorithms-api` component. There are two Java 11-compliant implementations, namely
the [Eclipselink](http://www.eclipse.org/eclipselink/#moxy) implementation and the 
[Metro / Glassfish Reference Implementation](https://javaee.github.io/metro/)

## Using the `shared-spi-jaxb` component within a project

All JAXB implementations within this project are imported with `provided` scope, to refrain from 
polluting the classpath within consuming projects. This implies that implementing projects must include 
the desired implementation on the classpath. Do this roughly as follows:

        <dependencies>
        
            ...
    
            <!--
                External dependencies.
            -->
            <dependency>
                <groupId>javax</groupId>
                <artifactId>javaee-api</artifactId>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>se.jguru.shared.spi.jaxb</groupId>
                <artifactId>jguru-shared-spi-jaxb</artifactId>
                <version>... latest_release_version ...</version>
            </dependency>
            
            <!-- 
                The JAXB implementation of choice
                If you run within an AppServer which provides an implementation, 
                simply switch to PROVIDED scope below. 
            -->
            <dependency>
                <groupId>org.eclipse.persistence</groupId>
                <artifactId>org.eclipse.persistence.moxy</artifactId>
                <scope>runtime</scope>
            </dependency>
        ...
        
        </dependencies>    

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png)