# About jGuru-shared-jaxb-spi-eclipselink

Java 11-compliant SPI providing a concrete `AbstractMarshallerAndUnmarshaller` implementation 
delegating processing to the [EclipseLink MOXy](http://www.eclipse.org/eclipselink/#moxy) library.  
within the `shared-algorithms-api` component.

## Using the `jGuru-shared-jaxb-spi-eclipselink` component within a project

Include the adapters within the `package-info.java` file to enable them for all entities 
within the package. This is done similar to the following:

        @XmlSchema(
                xmlns = {
                        @XmlNs(prefix = "shared", namespaceURI = SharedJaxbPatterns.NAMESPACE),
                        @XmlNs(prefix = "organisation", namespaceURI = OrganisationPatterns.NAMESPACE),
                        @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                        @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                        @XmlNs(prefix = "vc", namespaceURI = "http://www.w3.org/2007/XMLSchema-versioning")
                }
        )
        @XmlJavaTypeAdapters({
                @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class),
                @XmlJavaTypeAdapter(type = LocalTime.class, value = LocalTimeAdapter.class),
                @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
                @XmlJavaTypeAdapter(type = ZonedDateTime.class, value = ZonedDateTimeAdapter.class),
                @XmlJavaTypeAdapter(type = TimeZone.class, value = TimeZoneAdapter.class),
                @XmlJavaTypeAdapter(type = Locale.class, value = LocaleAdapter.class)
        })
        @XmlAccessorType(XmlAccessType.FIELD)
        package some.package.within.a.project;

All JAXB implementations within this project are imported with `provided` scope, to refrain from 
polluting the classpath within consuming projects. This implies that implementing projects must include 
the desired implementation on the classpath. Do this roughly as follows:

            <properties>
                ...

                <!-- Version constants -->
                <eclipselink.version>2.7.3</eclipselink.version>
                <validation-api.version>2.0.1.Final</validation-api.version>
            </properties>
        
            <!-- +=============================================== -->
            <!-- | Section 2:  Dependency (management) settings   -->
            <!-- +=============================================== -->
        <dependencyManagement>
            <dependencies>
        
                <dependency>
                    <groupId>org.eclipse.persistence</groupId>
                    <artifactId>org.eclipse.persistence.moxy</artifactId>
                    <version>${eclipselink.version}</version>
                </dependency>
            
                <dependency>
                    <groupId>javax.validation</groupId>
                    <artifactId>validation-api</artifactId>
                    <version>${validation-api.version}</version>
                    <scope>provided</scope>
                </dependency>
                
                ....
            </dependencies>
        </dependencyManagement>
            
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
                <groupId>se.jguru.shared.jaxb.spi.eclipselink</groupId>
                <artifactId>jguru-shared-jaxb-spi-eclipselink</artifactId>
                <version>... latest stable version ...</version>
            </dependency>
            
            <!-- 
                The JAXB implementation of choice
                If you run within an AppServer which provides an implementation, 
                simply switch to PROVIDED scope below. 
            -->
            <dependency>
                <groupId>org.eclipse.persistence</groupId>
                <artifactId>org.eclipse.persistence.moxy</artifactId>
                <scope>provided</scope>
            </dependency>
            <dependency>
                <groupId>javax.validation</groupId>
                <artifactId>validation-api</artifactId>
                <scope>provided</scope>
            </dependency>            
        ...
        
        </dependencies>    

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png)