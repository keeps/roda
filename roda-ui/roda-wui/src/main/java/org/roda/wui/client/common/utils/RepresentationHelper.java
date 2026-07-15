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
