package it.pagopa.selfcare.dashboard.connector.exception;

public class UserGroupAlreadyExistException extends RuntimeException{
    public UserGroupAlreadyExistException(String message){
        super(message);
    }
}
