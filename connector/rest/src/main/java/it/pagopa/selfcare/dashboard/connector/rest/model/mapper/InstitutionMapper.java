package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.core.generated.openapi.v1.dto.InstitutionProducts;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.Product;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.UserProductsResponse;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "id", source = "institutionId")
    @Mapping(target = "description", source = "institutionName")
    @Mapping(target = "parentDescription", source = "institutionRootName")
    @Mapping(target = "status", source = ".", qualifiedByName = "toStatus")
    InstitutionInfo toInstitutionInfo(InstitutionProducts institutionProducts);

    @Named("toStatus")
    default RelationshipState toStatus(InstitutionProducts institutionProducts) {
        return institutionProducts.getProducts().stream()
                .map(Product::getStatus)
                .sorted()
                .findFirst()
                .map(statusEnum -> RelationshipState.valueOf(statusEnum.name()))
                .orElse(null);
    }
}
