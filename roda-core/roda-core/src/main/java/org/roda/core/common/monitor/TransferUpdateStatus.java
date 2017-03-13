/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class TransferUpdateStatus {
  private static TransferUpdateStatus status;
  private static boolean isUpdatingBaseStatus;
  private static Set<String> isUpdatingFolderStatus;

  private TransferUpdateStatus() {
  }

  public static TransferUpdateStatus getInstance() {
    if (status == null) {
      status = new TransferUpdateStatus();
      isUpdatingBaseStatus = false;
      isUpdatingFolderStatus = new HashSet<>();
    }

    return status;
  }

  public boolean isUpdatingStatus(Optional<String> folderRelativePath) {
    boolean ret = false;
    if (isUpdatingBaseStatus || !folderRelativePath.isPresent()) {
      ret = isUpdatingBaseStatus;
    } else {
      String relativePath = folderRelativePath.get();
      if (isUpdatingFolderStatus.contains(relativePath)) {
        ret = true;
      } else {
        // check ancestors
        List<String> split = new ArrayList<>(Arrays.asList(relativePath.split("/")));
        while (split.size() > 1 && !ret) {
          split.remove(split.size() - 1);
          String ancestorRelativePath = StringUtils.join(split, "/");
          ret = isUpdatingFolderStatus.contains(ancestorRelativePath);
        }

      }
    }

    return ret;
  }

  public void setUpdatingStatus(Optional<String> folderRelativePath, boolean isUpdatingStatus) {
    if (folderRelativePath.isPresent()) {
      if (isUpdatingStatus) {
        isUpdatingFolderStatus.add(folderRelativePath.get());
      } else {
        isUpdatingFolderStatus.remove(folderRelativePath.get());
      }
    } else {
      TransferUpdateStatus.isUpdatingBaseStatus = isUpdatingStatus;
    }
  }

}
