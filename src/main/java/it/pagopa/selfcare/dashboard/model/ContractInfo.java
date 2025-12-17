package it.pagopa.selfcare.dashboard.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContractInfo {

    private String contractTemplateId;
    private String contractTemplatePath;
    private String contractTemplateVersion;
    private LocalDateTime createdAt;
    private String createdBy;
    private String description;
}
