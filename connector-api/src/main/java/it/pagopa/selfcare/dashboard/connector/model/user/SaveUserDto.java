package it.pagopa.selfcare.dashboard.connector.model.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SaveUserDto extends MutableUserFieldsDto {

    @NotEmpty
    private String fiscalCode;

}
