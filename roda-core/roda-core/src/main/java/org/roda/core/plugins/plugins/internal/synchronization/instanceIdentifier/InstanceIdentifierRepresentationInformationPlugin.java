package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.plugins.Plugin;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierRepresentationInformationPlugin
  extends InstanceIdentifierRodaEntityPlugin<RepresentationInformation> {
  @Override
  public String getName() {
    return "Representation Information instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<RepresentationInformation> cloneMe() {
    return new InstanceIdentifierRepresentationInformationPlugin();
  }

  @Override
  public List<Class<RepresentationInformation>> getObjectClasses() {
    return Arrays.asList(RepresentationInformation.class);
  }

}
