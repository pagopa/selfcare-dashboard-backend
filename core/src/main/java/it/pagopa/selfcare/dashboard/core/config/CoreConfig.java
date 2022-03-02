package it.pagopa.selfcare.dashboard.core.config;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.net.URL;
import java.util.Properties;

@Configuration
@PropertySource("classpath:config/core-config.properties")
@EnableAsync
@EnableAutoConfiguration(exclude = FreeMarkerAutoConfiguration.class)
class CoreConfig {

    private final URL rootTemplateUrl;

    @Autowired
    public CoreConfig(@Value("${dashboard.notification.template.default-url}") URL rootTemplateUrl) {
        this.rootTemplateUrl = rootTemplateUrl;
    }

    @Bean
    freemarker.template.Configuration customFreeMarkerConfiguration() throws TemplateException {
        Properties settings = new Properties();
        settings.put("recognize_standard_file_extensions", "true");
        settings.setProperty("localized_lookup", "false");
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setTemplateLoader(new CloudTemplateLoader(rootTemplateUrl));
        configuration.setSettings(settings);
        return configuration;
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
