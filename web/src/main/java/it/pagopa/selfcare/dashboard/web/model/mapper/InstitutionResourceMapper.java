package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InstitutionResourceMapper {

    @Mapping(target = "name", source = "description")
    @Mapping(target = "fiscalCode", source = "taxCode")
    @Mapping(target = "mailAddress", source = "digitalAddress")
    @Mapping(target = "recipientCode", source = "billing.recipientCode")
    @Mapping(target = "vatNumber", source = "billing.vatNumber")
    @Mapping(target = "vatNumberGroup", source = "paymentServiceProvider.vatNumberGroup")
    InstitutionResource toResource(InstitutionInfo model);
}
