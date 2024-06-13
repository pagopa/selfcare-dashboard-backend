package it.pagopa.selfcare.dashboard.connector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class BaseConnectorTest {

    protected ObjectMapper objectMapper;

    public BaseConnectorTest() {
    }

    public void setUp() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper((new ObjectMapper()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).registerModule(new JavaTimeModule()));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
}
