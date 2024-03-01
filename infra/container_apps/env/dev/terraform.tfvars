env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-dashboard-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 0
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
    value = "-javaagent:/applicationinsights-agent.jar"
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
  }

  BLOB_STORAGE_CONN_STRING
  BLOB_CONTAINER_REF
  MS_CORE_URL
  BACKOFFICE_PAGO_PA_API_KEY
  USERVICE_PARTY_MANAGEMENT_URL
  USERVICE_PARTY_PROCESS_URL
  USERVICE_PARTY_REGISTRY_PROXY_URL
  MS_PRODUCT_URL
  MS_USER_GROUP_URL
  USERVICE_USER_REGISTRY_URL
  USERVICE_USER_REGISTRY_API_KEY

  INSTITUTION_LOGO_ALLOWED_MIME_TYPES
  INSTITUTION_LOGO_ALLOWED_EXTENSIONS
  SUPPORT_API_KEY
  SUPPORT_API_ZENDESK_ACTION_URI

  MULTIPART_MAX_FILE_SIZE
  MULTIPART_MAX_FILE_SIZE

  JWT_TOKEN_EXCHANGE_PRIVATE_KEY
  JWT_TOKEN_EXCHANGE_DURATION
  JWT_TOKEN_EXCHANGE_KID
  JWT_TOKEN_EXCHANGE_ISSUER
  TOKEN_EXCHANGE_BILLING_URL
  TOKEN_EXCHANGE_BILLING_AUDIENCE
]

secrets_names = [
]
