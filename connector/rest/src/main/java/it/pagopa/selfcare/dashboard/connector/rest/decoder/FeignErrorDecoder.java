package it.pagopa.selfcare.dashboard.connector.rest.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import it.pagopa.selfcare.dashboard.connector.exception.ResourceNotFoundException;

public class FeignErrorDecoder extends ErrorDecoder.Default {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            throw new ResourceNotFoundException(response.reason());
        } else {
            return super.decode(methodKey, response);
        }
    }
}
