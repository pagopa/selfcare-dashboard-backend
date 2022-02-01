package it.pagopa.selfcare.dashboard.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.dashboard.core.RelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/relationships", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "relationships")
public class RelationshipController {

    private final RelationshipService relationshipService;


    @Autowired
    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }


    @PostMapping(value = "/{relationshipId}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.suspendUser}")
    public void suspendRelationship(@ApiParam("${swagger.dashboard.user.model.relationshipId}")
                                    @PathVariable("relationshipId")
                                            String relationshipId) {
        if (log.isDebugEnabled()) {
            log.trace("suspendUser");
            log.debug("relationshipId = {}", relationshipId);
        }

        relationshipService.suspend(relationshipId);
    }


    @PostMapping(value = "/{relationshipId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.dashboard.institutions.api.activateUser}")
    public void activateRelationship(@ApiParam("${swagger.dashboard.user.model.relationshipId}")
                                     @PathVariable("relationshipId")
                                             String relationshipId) {
        if (log.isDebugEnabled()) {
            log.trace("activateUser");
            log.debug("relationshipId = {}", relationshipId);
        }

        relationshipService.activate(relationshipId);
    }

}
