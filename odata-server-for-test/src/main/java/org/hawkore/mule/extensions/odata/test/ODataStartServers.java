package org.hawkore.mule.extensions.odata.test;

import org.hawkore.mule.extensions.odata.test.utils.v2.OlingoV2SampleServer;
import org.hawkore.mule.extensions.odata.test.utils.v4.OlingoV4SampleServer;

/**
 * This class provides a simple way to starts OData servers for <b>TESTING</b> purposes.
 *
 * <p>
 * Run from a terminal in 'odata-server-for-test' directory:
 *
 * <pre>
 * mvn exec:java -Dexec.mainClass="org.hawkore.mule.extensions.odata.test.ODataStartServers" -Dexec
 * .classpathScope=compile
 * </pre>
 * <p>
 * If you want to start a servers with JVM parameters, set
 * <b><code>MAVEN_OPTS</code></b> system property before start node.
 *
 * <p>
 * Sample for linux:
 *
 * <pre>
 * export MAVEN_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:+DisableExplicitGC"
 * </pre>
 *
 * @author Manuel Núñez (manuel.nunez@hawkore.com)
 */
public class ODataStartServers {

    public static void main(String[] args) {
        OlingoV2SampleServer.start();
        OlingoV4SampleServer.start();
    }

}
