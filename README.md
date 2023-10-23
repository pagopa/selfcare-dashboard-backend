# Microservice dashboard-backend

Microservice responsible for orchestrating the functionalities required for user dashboard activities. It retrieves institutions for a specific user by identifying their roles and permissions. It manages delegations related to an institution and technology partner and allows access to individual product back-office.

## Required Configuration Properties

Before running you must set these properties as environment variables.


| **Property**                                             | **Environment Variable**                 | **Default** | **Required** |
|----------------------------------------------------------|------------------------------------------|-------------|:------------:|
| rest-client.user-registry.base-url<br/>                  | USERVICE_USER_REGISTRY_URL               |             |     yes      |
| feign.client.config.user-registry.*.x-api-key<br/>       | USERVICE_USER_REGISTRY_API_KEY           |             |     yes      |
| rest-client.pago-pa-backoffice.base-url<br/>             | PAGO_PA_BACKOFFICE_URL                   |             |     yes      |
| backoffice.pago-pa.subscriptionKey<br/>                  | BACKOFFICE_PAGO_PA_API_KEY               |             |     yes      |
| jwt.signingKey<br/> <br/>                                | JWT_TOKEN_PUBLIC_KEY                     |             |     yes      |

## Running the application

You can run your application using:
```shell script
./mvnw install
./mvnw spring-boot:run -pl app
```

Application will respond to the url http://localhost:8080