package it.pagopa.selfcare.dashboard.model.mapper;

import it.pagopa.selfcare.dashboard.model.ContractInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductRestClientMapper {

    ContractInfo toContractInfo(it.pagopa.selfcare.product.generated.openapi.v1.dto.ContractTemplateResponse source);

}
