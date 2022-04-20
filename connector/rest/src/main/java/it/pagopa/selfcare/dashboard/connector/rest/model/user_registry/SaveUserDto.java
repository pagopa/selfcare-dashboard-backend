package it.pagopa.selfcare.dashboard.connector.rest.model.user_registry;

import it.pagopa.selfcare.dashboard.connector.model.user.MutableUserFieldsDto;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SaveUserDto extends MutableUserFieldsDto {

    @NotEmpty
    private String fiscalCode;

}
