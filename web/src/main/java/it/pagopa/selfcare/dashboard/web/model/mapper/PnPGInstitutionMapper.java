package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.UpdatePnPGInstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.PnPGInstitutionResource;
import it.pagopa.selfcare.dashboard.web.model.UpdatePnPGInstitutionDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.stream.Collectors;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.PENDING;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.TOBEVALIDATED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PnPGInstitutionMapper {

    public static PnPGInstitutionResource toResource(InstitutionInfo model) {
        PnPGInstitutionResource resource;

        if (model == null) {
            resource = null;
        } else {
            resource = new PnPGInstitutionResource();
            resource.setId(model.getId());
            resource.setExternalId(model.getExternalId());
            resource.setOrigin(model.getOrigin());
            resource.setOriginId(model.getOriginId());
            resource.setName(model.getDescription());
            resource.setFiscalCode(model.getTaxCode());
            resource.setMailAddress(model.getDigitalAddress());
            resource.setStatus(model.getStatus().toString());
            resource.setAddress(model.getAddress());
            resource.setZipCode(model.getZipCode());
            if (model.getBilling() != null) {
                resource.setRecipientCode(model.getBilling().getRecipientCode());
            }
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

    public static UpdatePnPGInstitutionResource toUpdateResource(UpdatePnPGInstitutionDto dto) {
        UpdatePnPGInstitutionResource resource;

        if (dto == null) {
            resource = null;
        } else {
            resource = new UpdatePnPGInstitutionResource();
            resource.setDescription(dto.getDescription());
            resource.setDigitalAddress(dto.getDigitalAddress());
        }

        return resource;
    }
}
