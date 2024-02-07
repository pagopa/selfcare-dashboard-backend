locals {
  github = {
    org        = "pagopa"
    repository = "selfcare-dashboard-backend"
  }

  prefix         = "selc"
  domain         = "b4f-dashboard"
  location_short = "weu"
  location       = "westeurope"
  project        = "${var.prefix}-${var.env_short}"
}

variable "env" {
  type = string
}

variable "env_short" {
  type = string
}

variable "domain" {
  type = string
}

variable "prefix" {
  type    = string
  default = "selc"
  validation {
    condition = (
    length(var.prefix) <= 6
    )
    error_message = "Max length is 6 chars."
  }
}

variable "cd_github_federations" {
  type = list(object({
    repository        = string
    credentials_scope = optional(string, "environment")
    subject           = string
  }))
  description = "GitHub Organization, repository name and scope permissions"
}

variable "environment_cd_roles" {
  type = object({
    subscription    = list(string)
    resource_groups = map(list(string))
  })
  description = "Continous Delivery roles for managed identity"
}

variable "github_repository_environment" {
  type = object({
    protected_branches     = bool
    custom_branch_policies = bool
    reviewers_teams        = list(string)
  })
  description = "GitHub Continuous Integration roles"
  default     = {
    protected_branches     = false
    custom_branch_policies = true
    reviewers_teams        = ["selfcare-contributors"]
  }
}
