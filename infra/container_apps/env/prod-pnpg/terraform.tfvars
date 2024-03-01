is_pnpg   = true
env_short = "p"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-dashboard-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 5
  scale_rules = [
    {
      custom = {
        metadata = {
          "desiredReplicas" = "3"
          "start"           = "0 8 * * MON-FRI"
          "end"             = "0 19 * * MON-FRI"
          "timezone"        = "Europe/Rome"
        }
        type = "cron"
      }
      name = "cron-scale-rule"
    }
  ]
  cpu    = 1.25
  memory = "2.5Gi"
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
    value = "INFO"
  },
  {
    name  = "REST_CLIENT_LOGGER_LEVEL"
    value = "BASIC"
  },
  {
    name  = "JWT_TOKEN_EXCHANGE_ISSUER"
    value = "https://pnpg.selfcare.pagopa.it"
  },
  {
    name  = "PUBLIC_FILE_STORAGE_BASE_URL"
    value = "https://selcpweupnpgcheckoutsa.z6.web.core.windows.net"
  },
  {
    name  = "JWT_ISSUER"
    value = "https://hub-login.spid.dev.pn.pagopa.it"
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
    value = "https://send.assistenza.pagopa.it/hc/it/requests/new"
  },
  {
    name  = "SUPPORT_API_ZENDESK_ORGANIZATION"
    value = "_users_hc_send"
  }
]

secrets_names = [
]