package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import lombok.Data;

import java.util.Map;

@Data
public class UserRequestDto {
    Map<String, Object> cFields;
    Map<String, Object> hcFields;
}
