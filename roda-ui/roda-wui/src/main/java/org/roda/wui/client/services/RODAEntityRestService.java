package org.roda.wui.client.services;

import org.fusesource.restygwt.client.DirectRestService;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public interface RODAEntityRestService<T extends IsIndexed> extends DirectRestService {

  @RequestMapping(method = RequestMethod.GET, path = "/find/{" + RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID
    + "}", produces = MediaType.APPLICATION_JSON_VALUE)
  T findByUuid(
    @Parameter(description = "The id", required = true) @PathVariable(RodaConstants.API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID) String uuid);
}
