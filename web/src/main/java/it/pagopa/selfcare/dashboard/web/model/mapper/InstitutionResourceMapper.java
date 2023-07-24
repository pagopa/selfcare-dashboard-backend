package it.pagopa.selfcare.dashboard.web.model.mapper;

import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.connector.model.institution.InstitutionInfo;
import it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState;
import it.pagopa.selfcare.dashboard.web.model.InstitutionResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.PENDING;
import static it.pagopa.selfcare.dashboard.connector.model.institution.RelationshipState.TOBEVALIDATED;

@Mapper(componentModel = "spring")
public interface InstitutionResourceMapper {

    @Mapping(target = "name", source = "description")
    @Mapping(target = "fiscalCode", source = "taxCode")
    @Mapping(target = "mailAddress", source = "digitalAddress")
    @Mapping(target = "recipientCode", source = "billing.recipientCode")
    @Mapping(target = "vatNumber", source = "billing.vatNumber")
    @Mapping(target = "vatNumberGroup", source = "paymentServiceProvider.vatNumberGroup")
    @Mapping(target = "userRole", expression = "java(toUserRole(model.getId(), model.getStatus()))")
    InstitutionResource toResource(InstitutionInfo model);

    @Named("toUserRole")
    default String toUserRole(String institutionId,  RelationshipState status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = "";
        if (authentication != null) {
            Optional<SelfCareGrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(grantedAuthority -> (SelfCareGrantedAuthority) grantedAuthority)
                    .filter(selfCareAuthority -> selfCareAuthority.getInstitutionId().equals(institutionId))
                    .findAny();
            if (selcAuthority.isPresent()) {
                userRole = selcAuthority.get().getAuthority();
            } else {
                if (PENDING.equals(status) || TOBEVALIDATED.equals(status))
                    userRole = SelfCareAuthority.ADMIN.toString();
            }

        }
        return userRole;
    }

}