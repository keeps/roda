package org.roda.core.data.v2.institution;

import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface RODAInstitution  extends IsIndexed, IsModelObject, HasId {
  boolean isActive();

  @Override
  String getId();

  String getName();
}
