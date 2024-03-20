package it.pagopa.selfcare.dashboard.core;

public interface UserV2GroupService {
    void deleteMembersByUserId(String userId, String institutionId, String productId);
}
