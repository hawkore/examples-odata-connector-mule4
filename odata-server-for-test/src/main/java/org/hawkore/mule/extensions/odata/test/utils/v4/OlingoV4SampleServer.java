package org.hawkore.mule.extensions.odata.test.utils.v4;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Semaphore;

import org.apache.cxf.Bus;
import org.apache.olingo.sample.server.v4.CarsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * The OData v2 sample server.
 *
 * @author Manuel Núñez Sánchez (manuel.nunez@hawkore.com)
 */
@SpringBootApplication
@PropertySource({"classpath:applicationV4.properties"})
@ComponentScan(basePackageClasses = {OlingoV4SampleServer.class})
public class OlingoV4SampleServer extends SpringBootServletInitializer {

    @Autowired
    private Bus bus;
    @Value("${odata.service.endpoint}")
    private String serviceEndpoint;
    @Value("${odata.server.url.mappings}")
    private String urlMappings;
    // simple control server initializacion
    private static Semaphore SEM;
    private static Thread odataServerThread;
    private static final Logger LOGGER = LoggerFactory.getLogger(OlingoV4SampleServer.class);

    /**
     * The entry point of application.
     *
     * @param args
     *     the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OlingoV4SampleServer.class, args);
    }

    /**
     * Start.
     */
    public static synchronized void start() {
        if (odataServerThread == null) {
            LOGGER.info("Starting OData V4 test server...");
            SEM = new Semaphore(0);
            odataServerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OlingoV4SampleServer.main(new String[0]);
                    } catch (Exception e) {
                        SEM.release();
                    }
                }
            });
            odataServerThread.start();
            try {
                // waits until app starts
                SEM.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Stop.
     */
    public static synchronized void stop() {
        if (odataServerThread != null) {
            odataServerThread.interrupt();
            odataServerThread = null;
            SEM = null;
        }
    }

    /**
     * OData servlet bean servlet registration bean.
     *
     * @return the servlet registration bean
     */
    @Bean("ODataServerV4")
    public ServletRegistrationBean oDataServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new CarsServlet(), urlMappings);
        bean.setLoadOnStartup(1);
        return bean;
    }

    /**
     * On App starts
     */
    @Component()
    public class ODataServerV4StartListener implements ApplicationListener<ContextRefreshedEvent> {

        /**
         * On application event.
         *
         * @param contextRefreshedEvent
         *     the context refreshed event
         */
        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
            Thread r = new Thread(new Runnable() {
                @Override
                public void run() {
                    // waits some time until service available
                    boolean ok = false;
                    while (!ok) {
                        try {
                            HttpURLConnection con = (HttpURLConnection)new URL(serviceEndpoint).openConnection();
                            con.setInstanceFollowRedirects(false);
                            con.setDoOutput(true);
                            con.setDoInput(true);
                            con.setRequestProperty("Accept", "*/*");
                            con.setRequestMethod("GET");
                            con.setRequestProperty("Connection", "keep-alive");
                            con.setConnectTimeout(100);
                            con.setReadTimeout(100);
                            con.connect();
                            con.getInputStream().close();
                            con.disconnect();
                            ok = true;
                        } catch (Exception e) {
                            ok = false;
                            try {
                                Thread.sleep(100l);
                            } catch (InterruptedException e2) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (SEM != null) {
                        SEM.release();
                    }
                    LOGGER.info("OData V4 test server STARTED at " + serviceEndpoint);
                }
            });
            r.start();
        }

    }

}
