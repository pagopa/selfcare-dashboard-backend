package it.pagopa.selfcare.dashboard.core;

import java.io.InputStream;

public interface FileStorageService {

    void storeInstitutionLogo(String institutionId, InputStream logo, String contentType, String fileName);

}
