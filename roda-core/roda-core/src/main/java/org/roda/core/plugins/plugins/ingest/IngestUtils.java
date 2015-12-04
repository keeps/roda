/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.StorageServiceException;

import com.google.common.collect.Sets;

public class IngestUtils {
  public static void createDirectories(ModelService model, String aipId, String representationID)
    throws StorageServiceException {
    model.getStorage().createDirectory(ModelUtils.getRepresentationsPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getRepresentationPath(aipId, representationID),
      getRepresentationMetadata(representationID));
    model.getStorage().createDirectory(ModelUtils.getMetadataPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getDescriptiveMetadataPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getPreservationPath(aipId), new HashMap<String, Set<String>>());
    model.getStorage().createDirectory(ModelUtils.getPreservationPath(aipId, representationID),
      new HashMap<String, Set<String>>());
  }

  private static Map<String, Set<String>> getRepresentationMetadata(String representationId) {
    SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
    String dateString = iso8601DateFormat.format(new Date());
    Map<String, Set<String>> data = new HashMap<String, Set<String>>();
    data.put("active", Sets.newHashSet("true"));
    data.put("date.created", Sets.newHashSet(dateString));
    data.put("date.modified", Sets.newHashSet(dateString));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.content.model", Sets.newHashSet(""));
    data.put("representation.dObject.pid", Sets.newHashSet(""));
    data.put("representation.id", Sets.newHashSet(representationId));
    data.put("representation.label", Sets.newHashSet(""));
    data.put("representation.pid", Sets.newHashSet(""));
    data.put("representation.state", Sets.newHashSet(""));
    data.put("representation.subtype", Sets.newHashSet(""));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.statuses", Sets.newHashSet("original"));
    return data;
  }
}
