package it.pagopa.selfcare.dashboard.model.mapper;

import it.pagopa.selfcare.commons.base.security.PartyRole;
import it.pagopa.selfcare.commons.base.security.ProductGrantedAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareAuthority;
import it.pagopa.selfcare.commons.base.security.SelfCareGrantedAuthority;
import it.pagopa.selfcare.dashboard.model.InstitutionBaseResource;
import it.pagopa.selfcare.dashboard.model.InstitutionResource;
import it.pagopa.selfcare.dashboard.model.UpdateInstitutionDto;
import it.pagopa.selfcare.dashboard.model.institution.*;
import it.pagopa.selfcare.dashboard.security.ExchangeTokenServiceV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.pagopa.selfcare.dashboard.model.institution.RelationshipState.PENDING;
import static it.pagopa.selfcare.dashboard.model.institution.RelationshipState.TOBEVALIDATED;

@Mapper(componentModel = "spring")
public interface InstitutionResourceMapper {

    @Mapping(target = "name", source = "description")
    @Mapping(target = "userRole", expression = "java(toUserRole(model.getId(), model.getStatus()))")
    InstitutionBaseResource toResource(InstitutionInfo model);

    @Mapping(target = "userRole", expression = "java(toUserRole(model.getUserRole()))")
    InstitutionBaseResource toResource(InstitutionBase model);

    @Mapping(target = "name", source = "description")
    @Mapping(target = "fiscalCode", source = "taxCode")
    @Mapping(target = "mailAddress", source = "digitalAddress")
    @Mapping(target = "recipientCode", source = "billing.recipientCode")
    @Mapping(target = "vatNumber", source = "billing.vatNumber")
    @Mapping(target = "vatNumberGroup", source = "paymentServiceProvider.vatNumberGroup")
    @Mapping(target = "products", source = "onboarding")
    @Mapping(target = "parentDescription", source = "rootParent.description")
    @Mapping(target = "category", expression = "java(retrieveCategory(model.getAttributes()))")
    @Mapping(target = "categoryCode", expression = "java(retrieveCategoryCode(model.getAttributes()))")
    InstitutionResource toResource(Institution model);

    UpdateInstitutionResource toUpdateResource(UpdateInstitutionDto dto);

    @Named("retrieveCategory")
    default String retrieveCategory(List<Attribute> attributes){
        if(!CollectionUtils.isEmpty(attributes)){
            return attributes.get(0).getDescription();
        }
        return null;
    }

    @Named("retrieveCategoryCode")
    default String retrieveCategoryCode(List<Attribute> attributes){
        if(!CollectionUtils.isEmpty(attributes)){
            return attributes.get(0).getCode();
        }
        return null;
    }

    @Named("toUserRole")
    default String toUserRole(String institutionId,  RelationshipState status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = "";
        if (authentication != null) {
            Optional<SelfCareGrantedAuthority> selcAuthority = authentication.getAuthorities()
                    .stream()
                    .filter(grantedAuthority -> SelfCareGrantedAuthority.class.isAssignableFrom(grantedAuthority.getClass()))
                    .map(SelfCareGrantedAuthority.class::cast)
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

    @Named("toUserRole")
    default String toUserRole(String userRole) {
        if(StringUtils.hasText(userRole)){
            return PartyRole.valueOf(userRole).getSelfCareAuthority().toString();
        }
        return null;
    }

    @Mapping(target = "name", source = "institution.description")
    @Mapping(target = "aooParent", source = "institution.aooParentCode")
    @Mapping(target = "subUnitType", source = "institution.subunitType")
    @Mapping(target = "subUnitCode", source = "institution.subunitCode")
    @Mapping(target = "rootParent", expression = "java(toRootParent(institution))")
    @Mapping(target = "roles", expression = "java(toRoles(productGrantedAuthorities, isBillingToken))")
    ExchangeTokenServiceV2.Institution toInstitution(Institution institution, List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken);

    @Mapping(target = "name", source = "institution.description")
    @Mapping(target = "aooParent", source = "institution.aooParentCode")
    @Mapping(target = "subUnitType", source = "institution.subunitType")
    @Mapping(target = "subUnitCode", source = "institution.subunitCode")
    @Mapping(target = "rootParent", expression = "java(toRootParent(institution))")
    @Mapping(target = "roles", expression = "java(toRolesBackofficeAdmin(productGrantedAuthorities, isBillingToken))")
    InstitutionBackofficeAdmin toInstitutionBackofficeAdmin(Institution institution, List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken);

    @Named("toRootParent")
    default RootParent toRootParent(Institution institutionInfo) {
        RootParent rootParent = new RootParent();
        if(institutionInfo != null && institutionInfo.getRootParent() != null) {
            rootParent.setId(institutionInfo.getRootParent().getId());
            rootParent.setDescription(institutionInfo.getRootParent().getDescription());
        }
        return rootParent;
    }

    default List<ExchangeTokenServiceV2.Role> toRoles(List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken) {
        List<ExchangeTokenServiceV2.Role> roles = new ArrayList<>();

        for (ProductGrantedAuthority authority : productGrantedAuthorities) {
            roles.addAll(constructRole(authority, isBillingToken));
        }
        return roles;
    }

    default List<RoleBackofficeAdmin> toRolesBackofficeAdmin(List<ProductGrantedAuthority> productGrantedAuthorities, boolean isBillingToken) {
        List<RoleBackofficeAdmin> roles = new ArrayList<>();

        for (ProductGrantedAuthority authority : productGrantedAuthorities) {
            roles.addAll(constructRoleBackofficeAdmin(authority, isBillingToken));
        }
        return roles;
    }

    default List<ExchangeTokenServiceV2.Role> constructRole(ProductGrantedAuthority productGrantedAuthority, boolean isBillingToken) {
        return productGrantedAuthority.getProductRoles().stream()
                .map(productRoleCode -> {
                    ExchangeTokenServiceV2.Role role = new ExchangeTokenServiceV2.Role();
                    role.setPartyRole(productGrantedAuthority.getPartyRole());
                    role.setProductRole(productRoleCode);
                    if (isBillingToken) {
                        role.setProductId(productGrantedAuthority.getProductId());
                    }
                    return role;
                }).toList();
    }

    default List<RoleBackofficeAdmin> constructRoleBackofficeAdmin(ProductGrantedAuthority productGrantedAuthority, boolean isBillingToken) {
        return productGrantedAuthority.getProductRoles().stream()
                .map(productRoleCode -> {
                    RoleBackofficeAdmin role = new RoleBackofficeAdmin();
                    role.setPartyRole(productGrantedAuthority.getPartyRole().toString());
                    role.setProductRole(productRoleCode);
                    if (isBillingToken) {
                        role.setProductId(productGrantedAuthority.getProductId());
                    }
                    return role;
                }).toList();
    }
}
