package org.roda.api.v1.factories;

import org.roda.api.v1.AipsService;
import org.roda.api.v1.impl.AipsServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2015-09-03T11:38:49.275+01:00")
public class AipsServiceFactory {

  private final static AipsService service = new AipsServiceImpl();

  public static AipsService getAipsApi() {
    return service;
  }
}
