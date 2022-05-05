package it.pagopa.selfcare.dashboard.core.model.mapper;

import it.pagopa.selfcare.dashboard.connector.model.user.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public class UserMapper {
    public static MutableUserFieldsDto map(UserDto model) {
        MutableUserFieldsDto resource = null;
        if (model != null) {
            resource = new MutableUserFieldsDto();
            resource.setEmail(map(model.getEmail(), Certification.NONE));
            resource.setFamilyName(map(model.getFamilyName(), Certification.NONE));
            resource.setName(map(model.getName(), Certification.NONE));
            resource.setWorkContacts(model.getWorkContacts().entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), map(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return resource;
    }

    public static SaveUserDto map(SaveUser model) {
        SaveUserDto resource = null;
        if (model != null) {
            resource = new SaveUserDto();
            resource.setEmail(map(model.getEmail(), Certification.NONE));
            resource.setFamilyName(map(model.getFamilyName(), Certification.NONE));
            resource.setName(map(model.getName(), Certification.NONE));
            resource.setFiscalCode(model.getFiscalCode());
            resource.setWorkContacts(model.getWorkContacts().entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), map(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return resource;
    }

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
