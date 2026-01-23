/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
