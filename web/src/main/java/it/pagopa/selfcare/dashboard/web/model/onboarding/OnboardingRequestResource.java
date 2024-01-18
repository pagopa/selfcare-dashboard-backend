package it.pagopa.selfcare.dashboard.web.model.onboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@Data
public class OnboardingRequestResource {


    @ApiModelProperty(value = "${swagger.dashboard.onboarding-request.model.status}", required = true)
    @JsonProperty(required = true)
    private OnboardingStatus status;

    @ApiModelProperty(value = "${swagger.dashboard.onboarding-request.model.institutionInfo}", required = true)
    @JsonProperty(required = true)
    private InstitutionInfo institutionInfo;

    @ApiModelProperty(value = "${swagger.dashboard.onboarding-request.model.manager}", required = true)
    @JsonProperty(required = true)
    private UserInfo manager;

    @ApiModelProperty(value = "${swagger.dashboard.onboarding-request.model.admins}")
    private List<UserInfo> admins;


    @Data
    @EqualsAndHashCode(of = "id")
    public static class InstitutionInfo {

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.id}", required = true)
        @JsonProperty(required = true)
        private String id;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.name}", required = true)
        @JsonProperty(required = true)
        private String name;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.institutionType}", required = true)
        @JsonProperty(required = true)
        private InstitutionType institutionType;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.address}", required = true)
        @JsonProperty(required = true)
        private String address;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.zipCode}", required = true)
        @JsonProperty(required = true)
        private String zipCode;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.city}")
        private String city;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.country}")
        private String country;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.county}")
        private String county;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.mailAddress}", required = true)
        @JsonProperty(required = true)
        private String mailAddress;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.fiscalCode}", required = true)
        @JsonProperty(required = true)
        private String fiscalCode;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.vatNumber}", required = true)
        @JsonProperty(required = true)
        private String vatNumber;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.recipientCode}", required = true)
        @JsonProperty(required = true)
        private String recipientCode;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData}")
        private PspData pspData;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.dpoData}")
        private DpoData dpoData;

        @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations}")
        private AdditionalInformations additionalInformations;

        @Data
        public static class AdditionalInformations{
            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.belongRegulatedMarket}")
            private boolean belongRegulatedMarket;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.regulatedMarketNote}")
            private String regulatedMarketNote;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.isIpa}")
            private boolean isIpa;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.ipaCode}")
            private String ipaCode;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.establishedByRegulatoryProvision}")
            private boolean establishedByRegulatoryProvision;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.establishedByRegulatoryProvisionNote}")
            private String establishedByRegulatoryProvisionNote;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.isAgentOfPublicService}")
            private boolean isAgentOfPublicService;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.agentOfPublicServiceNote}")
            private boolean agentOfPublicServiceNote;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.additionalInformations.otherNote}")
            private String otherNote;
        }

        @Data
        public static class PspData {

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.businessRegisterNumber}", required = true)
            @JsonProperty(required = true)
            private String businessRegisterNumber;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.legalRegisterName}", required = true)
            @JsonProperty(required = true)
            private String legalRegisterName;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.legalRegisterNumber}", required = true)
            @JsonProperty(required = true)
            private String legalRegisterNumber;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.abiCode}", required = true)
            @JsonProperty(required = true)
            private String abiCode;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.pspData.vatNumberGroup}", required = true)
            @JsonProperty(required = true)
            private Boolean vatNumberGroup;
        }


        @Data
        public static class DpoData {

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.dpoData.address}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            private String address;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.dpoData.pec}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            @Email
            private String pec;

            @ApiModelProperty(value = "${swagger.dashboard.institutions.model.dpoData.email}", required = true)
            @JsonProperty(required = true)
            @NotBlank
            @Email
            private String email;
        }
    }

    @Data
    public static class UserInfo {

        @ApiModelProperty(value = "${swagger.dashboard.user.model.id}", required = true)
        @JsonProperty(required = true)
        private UUID id;

        @ApiModelProperty(value = "${swagger.dashboard.user.model.name}", required = true)
        @JsonProperty(required = true)
        private String name;

        @ApiModelProperty(value = "${swagger.dashboard.user.model.surname}", required = true)
        @JsonProperty(required = true)
        private String surname;

        @ApiModelProperty(value = "${swagger.dashboard.user.model.institutionalEmail}", required = true)
        @JsonProperty(required = true)
        private String email;

        @ApiModelProperty(value = "${swagger.dashboard.user.model.fiscalCode}", required = true)
        @JsonProperty(required = true)
        private String fiscalCode;

    }
}
