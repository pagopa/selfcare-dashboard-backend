package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.InstitutionProducts;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.OnboardedProductResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InstitutionV2Mapper {

    @Mapping(target = "id", source = "institutionId")
    @Mapping(target = "description", source = "institutionName")
    @Mapping(target = "parentDescription", source = "institutionRootName")
    @Mapping(target = "status", source = ".", qualifiedByName = "toStatus")
    InstitutionInfo toInstitutionInfo(InstitutionProducts institutionProducts);

    @Named("toStatus")
    default RelationshipState toStatus(InstitutionProducts institutionProducts) {
        return institutionProducts.getProducts().stream()
                .map(OnboardedProductResponse::getStatus)
                .sorted()
                .findFirst()
                .map(statusEnum -> RelationshipState.valueOf(statusEnum.name()))
                .orElse(null);
    }
}
