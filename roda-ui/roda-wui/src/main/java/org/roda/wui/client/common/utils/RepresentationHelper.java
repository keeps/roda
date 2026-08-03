/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import org.roda.wui.client.common.labels.Tag;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RepresentationHelper {

  private RepresentationHelper() {
    // Utility class
  }

  public static Tag.TagStyle getTagStyle(String representationStatus) {
    switch (representationStatus) {
      case "ORIGINAL":
        return Tag.TagStyle.ORIGINAL;
      case "INGESTED":
        return Tag.TagStyle.INGESTED;
      case "ACCESS":
        return Tag.TagStyle.DISSEMINATION;
      case "PRESERVATION":
        return Tag.TagStyle.PRESERVATION;
      default:
        return Tag.TagStyle.CUSTOM;
    }
  }
}
