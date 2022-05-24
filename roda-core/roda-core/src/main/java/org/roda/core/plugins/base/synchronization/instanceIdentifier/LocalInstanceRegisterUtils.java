package org.roda.core.plugins.base.synchronization.instanceIdentifier;

import java.util.List;

import org.roda.core.common.SyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class LocalInstanceRegisterUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncUtils.class);

  public static String getDetailsFromList(List<String> detailsList) {
    StringBuilder details = new StringBuilder();
    for (String detail : detailsList) {
      details.append(detail).append("\n");
    }
    return details.toString();
  }
}
