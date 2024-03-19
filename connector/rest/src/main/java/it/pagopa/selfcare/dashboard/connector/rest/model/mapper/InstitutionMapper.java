package it.pagopa.selfcare.dashboard.connector.rest.model.mapper;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.core.generated.openapi.v1.dto.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.*;
import it.pagopa.selfcare.dashboard.connector.model.institution.Billing;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.product.PartyProduct;
import it.pagopa.selfcare.dashboard.connector.model.product.ProductOnBoardingStatus;
import it.pagopa.selfcare.user.generated.openapi.v1.dto.UserInstitutionRoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "id", source = "institutionId")
    @Mapping(target = "name", source = "institutionName")
    @Mapping(target = "parentDescription", source = "institutionRootName")
    @Mapping(target = "userRole", expression = "java(institutionProducts.getRole().name())")
    @Mapping(target = "status", expression = "java(institutionProducts.getStatus().name())")
    InstitutionBase toInstitutionBase(UserInstitutionRoleResponse institutionProducts);

    @Mapping(target = "id", source = "institutionId")
    @Mapping(target = "description", source = "institutionName")
    @Mapping(target = "parentDescription", source = "institutionRootName")
    @Mapping(target = "status", source = ".", qualifiedByName = "toStatus")
    InstitutionInfo toInstitutionInfo(InstitutionProducts institutionProducts);

    List<InstitutionInfo> toInstitutionInfo(List<OnboardedInstitutionResponse> institutionResponse);

    @Mapping(target = "status", expression = "java(getState(institutionResponse.getState()))")
    @Mapping(target = "category", expression = "java(getCategory(institutionResponse.getAttributes()))")
    @Mapping(target = "geographicTaxonomies", expression = "java(getGeographicTaxonomies(institutionResponse.getGeographicTaxonomies()))")
    InstitutionInfo toInstitutionInfo(OnboardedInstitutionResponse institutionResponse);

    @Mapping(target = "institutionType", expression = "java(toInstitutionType(institutionResponse.getInstitutionType()))")
    @Mapping(target = "description" , source = "institutionUpdate.description")
    @Mapping(target = "taxCode" , source = "institutionUpdate.taxCode")
    @Mapping(target = "digitalAddress" , source = "institutionUpdate.digitalAddress")
    @Mapping(target = "address" , source = "institutionUpdate.address")
    @Mapping(target = "zipCode" , source = "institutionUpdate.zipCode")
    @Mapping(target = "city" , source = "institutionResponse.city")
    @Mapping(target = "country" , source = "institutionResponse.country")
    @Mapping(target = "county" , source = "institutionResponse.county")
    @Mapping(target = "paymentServiceProvider" , source = "institutionResponse.paymentServiceProvider")
    @Mapping(target = "dataProtectionOfficer" , source = "institutionResponse.dataProtectionOfficer")
    //@Mapping(target = "billing" , source = "institutionResponse.billing")
    @Mapping(target = "additionalInformations" , source = "institutionUpdate.additionalInformations")
    @Mapping(target = "geographicTaxonomies", ignore = true)
    @Mapping(target = "supportContact", ignore = true)
    InstitutionInfo toInstitutionInfo(InstitutionResponse institutionResponse, InstitutionUpdate institutionUpdate);

    @Mapping(target = "category", expression = "java(getCategory(institution.getAttributes()))")
    Institution toInstitution(InstitutionResponse institution);

    InstitutionPut toInstitutionPut(UpdateInstitutionResource updateInstitutionResource);

    List<GeographicTaxonomy> toGeographicTaxonomy(List<GeoTaxonomies> geographicTaxonomies);

    Billing toBilling(BillingResponse billing);

    @Mapping(target = "onBoardingStatus", expression = "java(toOnboardedProductState(institutionProduct.getState()))")
    PartyProduct toPartyProduct(InstitutionProduct institutionProduct);

    @Named("toInstitutionType")
    default InstitutionType toInstitutionType(InstitutionResponse.InstitutionTypeEnum institutionTypeEnum) {
        return InstitutionType.valueOf(institutionTypeEnum.name());
    }

    @Named("toOnboardedProductState")
    default ProductOnBoardingStatus toOnboardedProductState(InstitutionProduct.StateEnum stateEnum) {
        if(stateEnum != null){
            return ProductOnBoardingStatus.valueOf(stateEnum.getValue());
        }
        return null;
    }

    @Named("getCategory")
    default String getCategory(List<AttributesResponse> attributesResponse) {
        if(!CollectionUtils.isEmpty(attributesResponse)) {
            return attributesResponse.get(0).getCode();
        }
        return null;
    }

    @Named("getGeographicTaxonomies")
    default List<GeographicTaxonomy> getGeographicTaxonomies(List<GeoTaxonomies> geographicTaxonomies) {
        if ( geographicTaxonomies == null ) {
            return Collections.emptyList();
        }
        List<GeographicTaxonomy> list = new ArrayList<>();
        for ( GeoTaxonomies geoTaxonomies : geographicTaxonomies ) {
            GeographicTaxonomy geographicTaxonomy = new GeographicTaxonomy();
            geographicTaxonomy.setCode( geoTaxonomies.getCode() );
            geographicTaxonomy.setDesc( geoTaxonomies.getDesc() );
            list.add(geographicTaxonomy);
        }
        return list;
    }

    @Named("getState")
    default RelationshipState getState(String state) {
        if(StringUtils.hasText(state)){
            return RelationshipState.valueOf(state);
        }
        return null;
    }

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
