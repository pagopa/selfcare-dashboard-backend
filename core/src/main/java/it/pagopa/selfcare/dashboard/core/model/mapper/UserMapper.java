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
            resource.setBirthDate(map(model.getBirthDate(), Certification.NONE));
            resource.setWorkContacts(model.getWorkContacts().entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), map(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return resource;
    }

    public static CertifiableFieldResource<String> map(String certifiableField, Certification certification) {
        CertifiableFieldResource<String> resource = null;
        if (certifiableField != null) {
            resource = new CertifiableFieldResource<>();
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

    public static CertifiableFieldResource<LocalDate> map(LocalDate date, Certification certification) {
        CertifiableFieldResource<LocalDate> resource = null;
        if (date != null) {
            resource = new CertifiableFieldResource<>();
            resource.setValue(date);
            resource.setCertification(Certification.valueOf(certification.toString()));
        }
        return resource;
    }

    public static User toUser(UserResource model) {
        User resource = null;
        if (model != null) {
            resource = new User();
            resource.setId(model.getId().toString());
            resource.setFiscalCode(model.getFiscalCode());
            resource.setName(model.getName().getValue());
            resource.setSurname(model.getFamilyName().getValue());
            resource.setEmail(model.getEmail().getValue());
        }
        return resource;
    }

}
