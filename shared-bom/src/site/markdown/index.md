# About `jguru-shared-bom`

The Shared Bill-Of-Materials ("BOM") supplies a version specification 
POM for all dependencies used within the jGuru Shared components reactor.
It is - as all BOMs - intended to be used in target projects as follows:

## 1. Include an *import*-scoped dependency management entry

Note that the type should be *pom* and that the scope should be *import*: 

    <!-- +=============================================== -->
    <!-- | Section 2:  Dependency (management) settings   -->
    <!-- +=============================================== -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>se.jguru.shared.bom</groupId>
                <artifactId>jguru-shared-bom</artifactId>
                <version>TheLatestStableVersion</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
 
## 2. Use any of the defined dependencies

Within your project, you should now be able to use the defined dependencies
without supplying version information:

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <scope>test</scope>
        </dependency>        