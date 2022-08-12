/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
