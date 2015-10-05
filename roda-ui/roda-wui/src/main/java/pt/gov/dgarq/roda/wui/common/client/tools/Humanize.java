package pt.gov.dgarq.roda.wui.common.client.tools;

import com.google.gwt.i18n.client.NumberFormat;

public class Humanize {

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0 B";
    }
    final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
  
}
