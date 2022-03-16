package it.pagopa.selfcare.dashboard.web.config;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalTime;
import java.util.ArrayList;
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

    private final Environment environment;


    @Autowired
    SwaggerConfig(Environment environment) {
        this.environment = environment;
    }


    @Bean
    public Docket swaggerSpringPlugin() {
        return (new Docket(DocumentationType.OAS_30))
                .apiInfo(new ApiInfoBuilder()
                        .title(environment.getProperty("swagger.title", environment.getProperty("spring.application.name")))
                        .description(environment.getProperty("swagger.description", "Api and Models"))
                        .version(environment.getProperty("swagger.version", environment.getProperty("spring.application.version")))
                        .build())
                .select().apis(RequestHandlerSelectors.basePackage("it.pagopa.selfcare.dashboard.web.controller")).build()
                .tags(new Tag("institutions", environment.getProperty("swagger.dashboard.institutions.api.description")),
                        new Tag("token", environment.getProperty("swagger.dashboard.token.api.description")),
                        new Tag("products", environment.getProperty("swagger.dashboard.product.api.description")),
                        new Tag("relationships", environment.getProperty("swagger.dashboard.relationships.api.description")),
                        new Tag("user-groups", environment.getProperty("swagger.dashboard.user-groups.api.description")))
                .directModelSubstitute(LocalTime.class, String.class)
                .ignoredParameterTypes(Pageable.class)
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

    @Component
    @Order
    static class PageableParameterReader implements OperationBuilderPlugin {
        private final Environment environment;
        private final TypeResolver resolver;

        @Autowired
        public PageableParameterReader(Environment environment,
                                       TypeResolver resolver) {
            this.environment = environment;
            this.resolver = resolver;
        }

        @Override
        public void apply(OperationContext context) {
            List<ResolvedMethodParameter> methodParameters = context.getParameters();
            ResolvedType pageableType = resolver.resolve(Pageable.class);
            List<RequestParameter> parameters = new ArrayList<>();
            for (ResolvedMethodParameter methodParameter : methodParameters) {
                ResolvedType resolvedType = methodParameter.getParameterType();
                if (pageableType.equals(resolvedType)) {
                    parameters.add(new RequestParameterBuilder()
                            .in(ParameterType.QUERY)
                            .name("page")
                            .query(q -> q.model(m -> m.scalarModel(ScalarType.INTEGER)))
                            .description("Results page you want to retrieve (0..N)").build());
                    parameters.add(new RequestParameterBuilder()
                            .in(ParameterType.QUERY)
                            .name("size")
                            .query(q -> q.model(m -> m.scalarModel(ScalarType.INTEGER)))
                            .description("Number of records per page, default size is 20").build());
                    parameters.add(new RequestParameterBuilder()
                            .in(ParameterType.QUERY)
                            .name("sort")
                            .query(q -> q.model(m -> m.collectionModel(c -> c.model(cm -> cm.scalarModel(ScalarType.STRING)))))
                            .description("Sorting criteria in the format: property(,asc|desc). "
                                    + "Default sort order is ascending. "
                                    + "Multiple sort criteria are supported.")
                            .build());
                    context.operationBuilder().requestParameters(parameters);
                }
            }
        }

        @Override
        public boolean supports(DocumentationType delimiter) {
            return true;
        }
    }

}
