package it.pagopa.selfcare.dashboard.core.config;

import freemarker.template.TemplateException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
@PropertySource("classpath:config/core-config.properties")
@EnableAsync
@EnableAutoConfiguration(exclude = FreeMarkerAutoConfiguration.class)
class CoreConfig implements AsyncConfigurer {

    private final URL rootTemplateUrl;
    private final TaskExecutorBuilder taskExecutorBuilder;


    @Autowired
    public CoreConfig(@Value("${dashboard.notification.template.default-url}") URL rootTemplateUrl,
                      TaskExecutorBuilder taskExecutorBuilder) {
        this.rootTemplateUrl = rootTemplateUrl;
        this.taskExecutorBuilder = taskExecutorBuilder;
    }


    @Bean
    public freemarker.template.Configuration customFreeMarkerConfiguration() throws TemplateException {
        Properties settings = new Properties();
        settings.put("recognize_standard_file_extensions", "true");
        settings.setProperty("localized_lookup", "false");
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setTemplateLoader(new CloudTemplateLoader(rootTemplateUrl));
        configuration.setSettings(settings);
        return configuration;
    }


    @Bean
    public SimpleAsyncUncaughtExceptionHandler simpleAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return simpleAsyncUncaughtExceptionHandler();
    }


    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor delegate = taskExecutorBuilder.build();
        delegate.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }

}
