package it.pagopa.selfcare.dashboard.connector.rest.model.process;

import lombok.Data;

import java.util.List;

/**
 * OnBoardingInfo
 */

@Data
public class OnBoardingInfo {
    private PersonInfo person;
    private List<InstitutionInfo> institutions;
}
