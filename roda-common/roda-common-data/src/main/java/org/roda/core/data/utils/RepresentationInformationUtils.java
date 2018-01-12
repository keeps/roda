package org.roda.core.data.utils;

public class RepresentationInformationUtils {
  public static final String REPRESENTATION_INFORMATION_FILTER_SEPARATOR = ":";

  private RepresentationInformationUtils() {
    // do nothing
  }

  public static String createRepresentationInformationFilter(String classname, String field, String value) {
    return classname + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field + REPRESENTATION_INFORMATION_FILTER_SEPARATOR
      + value;
  }

  public static String getValueFromFilter(String filter) {
    String[] splittedFilter = filter.split(REPRESENTATION_INFORMATION_FILTER_SEPARATOR);
    return splittedFilter[2];
  }

  public static String[] breakFilterIntoParts(String filter) {
    if (filter != null && !filter.isEmpty()) {
      String[] splitfilter = filter.split(REPRESENTATION_INFORMATION_FILTER_SEPARATOR);
      if (splitfilter.length == 3) {
        return new String[] {splitfilter[1], splitfilter[2], splitfilter[0]};
      }
    }
    return new String[] {"", "", ""};
  }
}
