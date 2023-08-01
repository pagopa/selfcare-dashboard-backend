package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.Institution;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdateInstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.UpdateInstitutionDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.PENDING;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.TOBEVALIDATED;

public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource;

        if (model == null) {
            resource = null;
        } else {
            resource = new InstitutionResource();
            resource.setId(model.getId());
            resource.setExternalId(model.getExternalId());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            resource.setName(model.getDescription());
            resource.setCategory(model.getCategory());
            resource.setFiscalCode(model.getTaxCode());
            resource.setMailAddress(model.getDigitalAddress());
            resource.setStatus(model.getStatus().toString());
            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
            resource.setParentDescription(model.getParentDescription());
            resource.setSubunitCode(model.getSubunitCode());
            resource.setSubunitType(model.getSubunitType());
            resource.setAooParentCode(model.getAooParentCode());

            if (model.getBilling() != null) {
                resource.setRecipientCode(model.getBilling().getRecipientCode());
                resource.setVatNumber(model.getBilling().getVatNumber());
            }
            if (model.getPaymentServiceProvider() != null) {
                resource.setVatNumberGroup(model.getPaymentServiceProvider().getVatNumberGroup());
            }
            resource.setSupportContact(SupportContactMapper.toResource(model.getSupportContact()));
            resource.setGeographicTaxonomies(model.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::toResource)
                    .collect(Collectors.toList()));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Optional<SelfCareGrantedAuthority> selcAuthority = authentication.getAuthorities()
                        .stream()
                        .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                        .map(grantedAuthority -> (SelfCareGrantedAuthority) grantedAuthority)
                        .filter(selfCareAuthority -> selfCareAuthority.getInstitutionId().equals(resource.getId()))
                        .findAny();
                selcAuthority.ifPresentOrElse(selfCareAuthority -> resource.setUserRole(selfCareAuthority.getAuthority()),
                        () -> {
                            if (PENDING.equals(model.getStatus()) || TOBEVALIDATED.equals(model.getStatus())) {
                                resource.setUserRole(SelfCareAuthority.ADMIN.toString());
                            }
                        });
            }

        }

        return resource;
    }

    public static InstitutionResource toResource(Institution model) {
        InstitutionResource resource;

        if (model == null) {
            resource = null;
        } else {
            resource = new InstitutionResource();
            resource.setId(model.getId());
            resource.setExternalId(model.getExternalId());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            resource.setName(model.getDescription());
            resource.setCategory(model.getCategory());
            resource.setFiscalCode(model.getTaxCode());
            resource.setMailAddress(model.getDigitalAddress());
            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
            if (model.getBilling() != null) {
                resource.setRecipientCode(model.getBilling().getRecipientCode());
                resource.setVatNumber(model.getBilling().getVatNumber());
            }
            if (model.getPaymentServiceProvider() != null) {
                resource.setVatNumberGroup(model.getPaymentServiceProvider().getVatNumberGroup());
            }
            if (model.getAttributes() != null && !model.getAttributes().isEmpty()) {
                resource.setCategory(model.getAttributes().get(0).getDescription());
            }
            if(model.getOnboarding() != null && !model.getOnboarding().isEmpty()){
                resource.setProducts(model.getOnboarding());
            }
            resource.setSupportContact(SupportContactMapper.toResource(model.getSupportContact()));
            resource.setGeographicTaxonomies(model.getGeographicTaxonomies().stream()
                    .map(GeographicTaxonomyMapper::toResource)
                    .collect(Collectors.toList()));

        }

        return resource;
    }

    public static UpdateInstitutionResource toUpdateResource(UpdateInstitutionDto dto) {
        UpdateInstitutionResource resource;

        if (dto == null) {
            resource = null;
        } else {
            resource = new UpdateInstitutionResource();
            resource.setDescription(dto.getDescription());
            resource.setDigitalAddress(dto.getDigitalAddress());
        }

        return resource;
    }
}
