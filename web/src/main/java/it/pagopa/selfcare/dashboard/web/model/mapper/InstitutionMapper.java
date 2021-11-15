package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class InstitutionMapper {

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;

        if (model != null) {
            resource = new InstitutionResource();
            resource.setId(model.getInstitutionId());
            resource.setName(model.getDescription());
            resource.setCategory(model.getCategory());
            resource.setIPACode(null);//TODO
            resource.setFiscalCode(null);//TODO
            resource.setMailAddress(model.getDigitalAddress());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Optional<? extends GrantedAuthority> selcAuthority = authentication.getAuthorities()
                        .stream()
                        .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                        .findAny();
                if (selcAuthority.isPresent()) {
                    resource.setUserRole(selcAuthority.get().getAuthority());
                }
            }
        }

        return resource;
    }
}
