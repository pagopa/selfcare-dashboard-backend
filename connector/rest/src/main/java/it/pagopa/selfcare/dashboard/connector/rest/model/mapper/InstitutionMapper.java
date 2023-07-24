package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionProducts;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.UserProductsResponse;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "id", source = "institutionId")
    @Mapping(target = "description", source = "institutionName")
    @Mapping(target = "parentDescription", source = "institutionRootName")
    InstitutionInfo toInstitutionInfo(InstitutionProducts institutionProducts);
}
