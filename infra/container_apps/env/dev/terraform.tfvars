env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-dashboard-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 1
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
}

app_settings = [
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "b4f-dashboard"
  },
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar"
  },
  {
    name  = "APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL"
    value = "OFF"
  },
  {
    name  = "B4F_ONBOARDING_LOG_LEVEL"
    value = "DEBUG"
  },
  {
    name  = "REST_CLIENT_LOGGER_LEVEL"
    value = "FULL"
  },
  {
    name  = "JWT_TOKEN_EXCHANGE_ISSUER"
    value = "https://dev.selfcare.pagopa.it"
  },
  {
    name  = "PUBLIC_FILE_STORAGE_BASE_URL"
    value = "https://selcdcheckoutsa.z6.web.core.windows.net"
  },
  {
    name  = "PAGO_PA_BACKOFFICE_URL"
    value = "https://api.dev.platform.pagopa.it/apiconfig/auth/api/v1"
  },
  {
    name  = "REST_CLIENT_READ_TIMEOUT"
    value = "30000"
  },
  {
    name  = "REST_CLIENT_CONNECT_TIMEOUT"
    value = "30000"
  },
  {
    name  = "USER_STATES_FILTER"
    value = "ACTIVE,SUSPENDED"
  },
  {
    name  = "SUPPORT_API_ZENDESK_REDIRECT_URI"
    value = "https://selfcare.assistenza.pagopa.it/hc/it/requests/new"
  },
  {
    name  = "SUPPORT_API_ZENDESK_ORGANIZATION"
    value = "_users_hc_selfcare"
  },
  {
    name  = "MS_CORE_URL"
    value = "https://selc-d-ms-core-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_PROCESS_URL"
    value = "https://selc-d-ms-core-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_REGISTRY_PROXY_URL"
    value = "https://selc-d-party-reg-proxy-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_PRODUCT_URL"
    value = "https://selc.internal.dev.selfcare.pagopa.it/ms-product/v1"
  },
  {
    name  = "MS_USER_GROUP_URL"
    value = "https://selc-d-ms-user-group-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_USER_REGISTRY_URL"
    value = "https://api.uat.pdv.pagopa.it/user-registry/v1"
  },
  {
    name  = "JWT_TOKEN_EXCHANGE_DURATION"
    value = "PT15M"
  },
  {
    name  = "TOKEN_EXCHANGE_BILLING_URL"
    value = "https://dev.portalefatturazione.pagopa.it/auth?selfcareToken=<IdentityToken>"
  },
  {
    name  = "TOKEN_EXCHANGE_BILLING_AUDIENCE" 
    value = "dev.portalefatturazione.pagopa.it"
  }
]

secrets_names = {
    "APPLICATIONINSIGHTS_CONNECTION_STRING"           = "appinsights-connection-string"
    "BLOB_STORAGE_CONN_STRING"                        = "web-storage-connection-string"
    "USER_REGISTRY_API_KEY"                           = "user-registry-api-key"
    "BACKOFFICE_PAGO_PA_API_KEY"                      = "pagopa-backoffice-api-key"
    "SUPPORT_API_KEY"                                 = "zendesk-support-api-key"
    "JWT_TOKEN_EXCHANGE_PRIVATE_KEY"                  = "jwt-exchange-private-key"
    "JWT_TOKEN_EXCHANGE_KID"                          = "jwt-exchange-kid"
    "JWT_TOKEN_PUBLIC_KEY"                            = "jwt-public-key"
}
