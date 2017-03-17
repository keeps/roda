/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;

public class IndexUtils {

  private IndexUtils() {
    // do nothing
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> Class<T> giveRespectiveIndexClass(Class<? extends IsRODAObject> inputClass) {
    if (AIP.class.equals(inputClass)) {
      return (Class<T>) IndexedAIP.class;
    } else if (Representation.class.equals(inputClass)) {
      return (Class<T>) IndexedRepresentation.class;
    } else if (File.class.equals(inputClass)) {
      return (Class<T>) IndexedFile.class;
    } else if (Risk.class.equals(inputClass)) {
      return (Class<T>) IndexedRisk.class;
    } else if (DIP.class.equals(inputClass)) {
      return (Class<T>) IndexedDIP.class;
    } else if (Report.class.equals(inputClass)) {
      return (Class<T>) IndexedReport.class;
    } else {
      return (Class<T>) inputClass;
    }
  }
}
