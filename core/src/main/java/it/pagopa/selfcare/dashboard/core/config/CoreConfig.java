package it.pagopa.selfcare.dashboard.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.net.URL;

@Configuration
@PropertySource("classpath:config/core-config.properties")
@EnableAsync
class CoreConfig {

    private final URL rootTemplateUrl;

    @Autowired
    public CoreConfig(@Value("${dashboard.notification.template.root.url}") URL rootTemplateUrl) {
        this.rootTemplateUrl = rootTemplateUrl;
    }

    //    @Bean
    FreeMarkerConfigurationFactory freeMarkerConfigurationFactory() {
        FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactory = new FreeMarkerConfigurationFactoryBean();
        freeMarkerConfigurationFactory.setPreTemplateLoaders(new CloudTemplateLoader(rootTemplateUrl));
        return freeMarkerConfigurationFactory;
    }


    @Bean
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(SimpleAsyncTaskExecutor delegate) {
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }

    @Bean
    public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();

        executor.setThreadNamePrefix("NotificationManager-");
        return executor;
    }

}
