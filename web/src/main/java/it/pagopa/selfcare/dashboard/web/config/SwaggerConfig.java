package it.pagopa.selfcare.dashboard.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * The Class SwaggerConfig.
 */
@Configuration
class SwaggerConfig {

    public static final String AUTH_SCHEMA_NAME = "bearerAuth";

    @Configuration
    @Profile("swaggerIT")
    @PropertySource("classpath:/swagger/swagger_it.properties")
    public static class itConfig {
    }

    @Configuration
    @Profile("swaggerEN")
    @PropertySource("classpath:/swagger/swagger_en.properties")
    public static class enConfig {
    }


    @Bean
    public Docket swaggerSpringPlugin(@Value("${swagger.title:${spring.application.name}}") String title,
                                      @Value("${swagger.description:Api and Models}") String description,
                                      @Value("${swagger.version:${spring.application.version}}") String version,
                                      @Value("${swagger.dashboard.api.description}") String productApiDesc,
                                      @Value("${swagger.security.schema.bearer.description}") String authSchemaDesc
    ) {
        return (new Docket(DocumentationType.OAS_30))
                .apiInfo(apiInfo(title, description, version))
                .select().apis(RequestHandlerSelectors.basePackage("it.pagopa.selfcare.dashboard.web")).build()
                .tags(new Tag("dashboard", productApiDesc))
                .directModelSubstitute(LocalTime.class, String.class)
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.JWT_BEARER_BUILDER
                        .name(AUTH_SCHEMA_NAME)
                        .description(authSchemaDesc)
                        .build()));
    }


    private ApiInfo apiInfo(String title, String description, String version) {
        return new ApiInfoBuilder().title(title).description(description).version(version).build();
    }


    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).build();
    }


    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference(AUTH_SCHEMA_NAME, authorizationScopes));
    }

}
