## Start OData servers for testing

With this module you can start two OData servers: one using OData V2 protocol and another one using OData V4 protocol for testing purposes.

- **To start OData servers**, open a terminal at this directory and run:

    ```
    mvn clean compile exec:java -Dexec.mainClass="org.hawkore.mule.extensions.odata.test.ODataStartServers" -Dexec.classpathScope=compile
    ```

Visit [Hawkore's home page](https://www.hawkore.com).
