package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource;

        if (model == null) {
            resource = null;

        } else {
            resource = new InstitutionResource();
            resource.setId(model.getInstitutionId());
            resource.setName(model.getDescription());
            resource.setCategory(model.getCategory());
            resource.setFiscalCode(model.getTaxCode());
            resource.setMailAddress(model.getDigitalAddress());
            resource.setStatus(model.getStatus());
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
                            if ("PENDING".equals(resource.getStatus())) {
                                resource.setUserRole(SelfCareAuthority.ADMIN.toString());
                            }
                        });
            }
        }

        return resource;
    }
}
