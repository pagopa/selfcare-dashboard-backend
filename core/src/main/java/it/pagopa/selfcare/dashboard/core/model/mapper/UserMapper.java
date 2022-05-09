package it.pagopa.selfcare.dashboard.core.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.Certification;
import it.pagopa.selfcare.dashboard.connector.model.user.CertifiedField;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContact;
import it.pagopa.selfcare.dashboard.connector.model.user.WorkContactResource;

import java.time.LocalDate;

public class UserMapper {

    public static CertifiedField<String> map(String certifiableField, Certification certification) {
        CertifiedField<String> resource = null;
        if (certifiableField != null) {
            resource = new CertifiedField<>();
            resource.setValue(certifiableField);
            resource.setCertification(Certification.valueOf(certification.toString()));
        }
        return resource;
    }

    public static WorkContactResource map(WorkContact workContact) {
        WorkContactResource workContactResource = null;
        if (workContact != null) {
            workContactResource = new WorkContactResource();
            workContactResource.setEmail(map(workContact.getEmail(), Certification.NONE));
        }
        return workContactResource;
    }

    public static CertifiedField<LocalDate> map(LocalDate date, Certification certification) {
        CertifiedField<LocalDate> resource = null;
        if (date != null) {
            resource = new CertifiedField<>();
            resource.setValue(date);
            resource.setCertification(Certification.valueOf(certification.toString()));
        }
        return resource;
    }

}
