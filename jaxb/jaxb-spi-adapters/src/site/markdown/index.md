# About jGuru-shared-jaxb-spi-adapters

Module containing commonly (re-)used JAXB mechanics, including several XmlAdapter implementations
mainly for JavaSE types dealing with Times, Dates, TimeZones, Locales, Currencies etc. 

## Using the `shared-spi-jaxb` component within a project

Simply include the adapters within the `package-info.java` file to enable them for all entities 
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

### Dependency Graph

The dependency graph for this project is shown below:

![Dependency Graph](./images/dependency_graph.png)