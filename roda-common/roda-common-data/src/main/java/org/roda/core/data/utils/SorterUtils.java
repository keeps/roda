package org.roda.core.data.utils;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class SorterUtils {
  public static Sorter dipsDefault() {
    return new Sorter(new SortParameter(RodaConstants.DIP_ID, false));
  }

  public static Sorter representationDefault() {
    return new Sorter(new SortParameter(RodaConstants.REPRESENTATION_ID, false));
  }

  public static Sorter filesDefault() {
    return new Sorter(new SortParameter(RodaConstants.FILE_ORIGINALNAME, false));
  }
}
