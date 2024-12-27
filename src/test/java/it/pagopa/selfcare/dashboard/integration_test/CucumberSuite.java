package it.pagopa.selfcare.dashboard.integration_test;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import it.pagopa.selfcare.dashboard.SelfCareDashboardApplication;
import it.pagopa.selfcare.dashboard.integration_test.steps.DashboardStepsUtil;
import org.junit.platform.suite.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest(classes = {SelfCareDashboardApplication.class})
@ExcludeTags({"exclude"})
public class CucumberSuite {

    private DashboardStepsUtil dashboardStepsUtil;

    @Before
    public void setUp() {
        dashboardStepsUtil = new DashboardStepsUtil();
    }
}

