package org.roda.core.data.utils;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;

public class RepresentationInformationUtils {
  public static final String REPRESENTATION_INFORMATION_FILTER_SEPARATOR = ":";

  private RepresentationInformationUtils() {
    // do nothing
  }

  public static String createRepresentationInformationFilter(String classname, String field, String value) {
    return classname + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field + REPRESENTATION_INFORMATION_FILTER_SEPARATOR
      + value;
  }

  public static String createRepresentationInformationAipFilter(String value) {
    return RodaConstants.INDEX_AIP + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + RodaConstants.AIP_TYPE
      + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + value;
  }

  public static String createRepresentationInformationRepresentationFilter(String value) {
    return RodaConstants.INDEX_REPRESENTATION + REPRESENTATION_INFORMATION_FILTER_SEPARATOR
      + RodaConstants.REPRESENTATION_TYPE + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + value;
  }

  public static List<String> createRepresentationInformationFileFilter(String pronom, String mimetype,
    String alternateDesignation) {
    List<String> filters = new ArrayList<>();

    if (pronom != null && !pronom.isEmpty()) {
      filters.add(RodaConstants.INDEX_FILE + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + RodaConstants.FILE_PRONOM
        + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + pronom);
    }

    if (mimetype != null && !mimetype.isEmpty()) {
      filters.add(RodaConstants.INDEX_FILE + REPRESENTATION_INFORMATION_FILTER_SEPARATOR
        + RodaConstants.FILE_FORMAT_MIMETYPE + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + mimetype);
    }

    if (alternateDesignation != null && !alternateDesignation.isEmpty()) {
      filters.add(RodaConstants.INDEX_FILE + REPRESENTATION_INFORMATION_FILTER_SEPARATOR
        + RodaConstants.FILE_FORMAT_DESIGNATION + REPRESENTATION_INFORMATION_FILTER_SEPARATOR + alternateDesignation);
    }

    return filters;
  }

  public static String getValueFromFilter(String filter) {
    String[] splittedFilter = filter.split(REPRESENTATION_INFORMATION_FILTER_SEPARATOR);
    return splittedFilter[2];
  }
}
