package it.pagopa.selfcare.dashboard.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;


@Data
@ApiModel
@Validated
@Builder
public class ContractTemplateUploadRequest {

    private MultipartFile file;

    private String name;

    private String productId;

    private String version;

    private String description;

    private String createdBy;

}
