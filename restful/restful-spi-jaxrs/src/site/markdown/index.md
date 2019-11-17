# About `jguru-shared-restful-spi-jaxrs`

The Shared Restful JaxRS SPI contains utilities such as REST-ful Producers 
that simplifies converting objects to and from JSON using the 
[Jackson](https://github.com/FasterXML/jackson) framework. 

### Example / typical use

Simply add the JacksonJsonMapper as a resource within the application, 
and add the `Consumes` and `Produces` annotations on a form similar 
to the following:

    @Consumes("application/json", "application/json;charset=utf-8")
    @Produces("application/json", "application/json;charset=utf-8")
    open class SomeClassJsonMapper : JacksonJsonMapper<SomeClass>()
    
As an alternative, you can use the `JacksonJsonAnyMapper` implementation which
handles *Any* objects. That - in turn - implies that the default Jackson-flavoured 
        