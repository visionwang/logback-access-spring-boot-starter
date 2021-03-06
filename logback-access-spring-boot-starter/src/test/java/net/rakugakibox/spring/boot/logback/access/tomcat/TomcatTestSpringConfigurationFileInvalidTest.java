package net.rakugakibox.spring.boot.logback.access.tomcat;

import net.rakugakibox.spring.boot.logback.access.AbstractTestSpringConfigurationFileInvalidTest;
import net.rakugakibox.spring.boot.logback.access.test.TomcatServletWebServerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The test in the case where test spring configuration files is invalid for Tomcat.
 */
public class TomcatTestSpringConfigurationFileInvalidTest extends AbstractTestSpringConfigurationFileInvalidTest {

    /**
     * Constructs an instance.
     */
    public TomcatTestSpringConfigurationFileInvalidTest() {
        super(ContextConfiguration.class);
    }

    /**
     * The context configuration.
     */
    @Configuration
    @Import(TomcatServletWebServerConfiguration.class)
    public static class ContextConfiguration extends AbstractContextConfiguration {
    }

}
