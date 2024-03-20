# Container App

Deploy container on a Container App instance.

<!-- markdownlint-disable -->
<!-- BEGINNING OF PRE-COMMIT-TERRAFORM DOCS HOOK -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.6.0 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_container_app_dashboard_backend"></a> [container\_app\_dashboard\_backend](#module\_container\_app\_dashboard\_backend) | github.com/pagopa/selfcare-commons//infra/terraform-modules/container_app_microservice | main |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_settings"></a> [app\_settings](#input\_app\_settings) | n/a | <pre>list(object({<br>    name  = string<br>    value = string<br>  }))</pre> | n/a | yes |
| <a name="input_container_app"></a> [container\_app](#input\_container\_app) | Container App configuration | <pre>object({<br>    min_replicas = number<br>    max_replicas = number<br><br>    scale_rules = list(object({<br>      name = string<br>      custom = object({<br>        metadata = map(string)<br>        type     = string<br>      })<br>    }))<br><br>    cpu    = number<br>    memory = string<br>  })</pre> | n/a | yes |
| <a name="input_env_short"></a> [env\_short](#input\_env\_short) | Environment short name | `string` | n/a | yes |
| <a name="input_image_tag"></a> [image\_tag](#input\_image\_tag) | Image tag to use for the container | `string` | `"latest"` | no |
| <a name="input_is_pnpg"></a> [is\_pnpg](#input\_is\_pnpg) | (Optional) True if you want to apply changes to PNPG environment | `bool` | `false` | no |
| <a name="input_secrets_names"></a> [secrets\_names](#input\_secrets\_names) | KeyVault secrets to get values from | `list(string)` | n/a | yes |
| <a name="input_tags"></a> [tags](#input\_tags) | n/a | `map(any)` | n/a | yes |

## Outputs

No outputs.
<!-- END OF PRE-COMMIT-TERRAFORM DOCS HOOK -->