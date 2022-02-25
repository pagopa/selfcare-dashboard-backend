package it.pagopa.selfcare.dashboard.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.net.URL;

@Configuration
@PropertySource("classpath:config/core-config.properties")
class CoreConfig {

    private URL property;

    @Autowired
    public CoreConfig(@Value("${dashboard.notification.template.root.url}") URL property) {
        this.property = property;
    }

    //    @Bean
    FreeMarkerConfigurationFactory freeMarkerConfigurationFactory() {
        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactory = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactory.setPreTemplateLoaders(new CloudTemplateLoader(property));
        return freeMarkerConfigurationFactory;
    }

}
