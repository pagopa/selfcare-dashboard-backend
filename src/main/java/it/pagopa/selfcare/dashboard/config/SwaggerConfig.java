package it.pagopa.selfcare.dashboard.config;

import com.fasterxml.classmate.TypeResolver;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.security.JwtAuthenticationToken;
import it.pagopa.selfcare.commons.web.swagger.BaseSwaggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.HttpAuthenticationScheme;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static it.pagopa.selfcare.commons.web.swagger.BaseSwaggerConfig.*;

/**
 * The Class SwaggerConfig.
 */
@Configuration
@Import(BaseSwaggerConfig.class)
public class SwaggerConfig {

    private static final String AUTH_SCHEMA_NAME = "bearerAuth";

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

    private final Environment environment;


    @Autowired
    SwaggerConfig(Environment environment) {
        this.environment = environment;
    }


    @Bean
    public Docket swaggerSpringPlugin(@Autowired TypeResolver typeResolver) {
        return (new Docket(DocumentationType.OAS_30))
                .apiInfo(new ApiInfoBuilder()
                        .title(environment.getProperty("swagger.title", environment.getProperty("spring.application.name")))
                        .description(environment.getProperty("swagger.description", "Api and Models"))
                        .version(environment.getProperty("swagger.version", environment.getProperty("spring.application.version")))
                        .build())
                .select().apis(RequestHandlerSelectors.basePackage("it.pagopa.selfcare.dashboard.controller")).build()
                .tags(new Tag("institutions", environment.getProperty("swagger.dashboard.institutions.api.description")),
                        new Tag("pnPGInstitutions", environment.getProperty("swagger.dashboard.pnPGInstitutions.api.description")),
                        new Tag("token", environment.getProperty("swagger.dashboard.token.api.description")),
                        new Tag("products", environment.getProperty("swagger.dashboard.product.api.description")),
                        new Tag("relationships", environment.getProperty("swagger.dashboard.relationships.api.description")),
                        new Tag("user-groups", environment.getProperty("swagger.dashboard.user-groups.api.description")),
                        new Tag("onboarding", environment.getProperty("swagger.dashboard.onboarding.api.description")))
                .directModelSubstitute(LocalTime.class, String.class)
                .ignoredParameterTypes(Pageable.class)
                .ignoredParameterTypes(Authentication.class)
                .ignoredParameterTypes(JwtAuthenticationToken.class)
                .forCodeGeneration(true)
                .useDefaultResponseMessages(false)
                .globalResponses(HttpMethod.GET, List.of(INTERNAL_SERVER_ERROR_RESPONSE, UNAUTHORIZED_RESPONSE, BAD_REQUEST_RESPONSE, NOT_FOUND_RESPONSE))
                .globalResponses(HttpMethod.DELETE, List.of(INTERNAL_SERVER_ERROR_RESPONSE, UNAUTHORIZED_RESPONSE, BAD_REQUEST_RESPONSE))
                .globalResponses(HttpMethod.POST, List.of(INTERNAL_SERVER_ERROR_RESPONSE, UNAUTHORIZED_RESPONSE, BAD_REQUEST_RESPONSE))
                .globalResponses(HttpMethod.PUT, List.of(INTERNAL_SERVER_ERROR_RESPONSE, UNAUTHORIZED_RESPONSE, BAD_REQUEST_RESPONSE))
                .globalResponses(HttpMethod.PATCH, List.of(INTERNAL_SERVER_ERROR_RESPONSE, UNAUTHORIZED_RESPONSE, BAD_REQUEST_RESPONSE))
                .additionalModels(typeResolver.resolve(Problem.class))
                .securityContexts(Collections.singletonList(SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .build()))
                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.JWT_BEARER_BUILDER
                        .name(AUTH_SCHEMA_NAME)
                        .description(environment.getProperty("swagger.security.schema.bearer.description"))
                        .build()));
    }


    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference(AUTH_SCHEMA_NAME, authorizationScopes));
    }

}
