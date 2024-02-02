/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClassificationPlanUtils {

  private ClassificationPlanUtils() {
    // do nothing
  }

  public static ConsumesOutputStream retrieveClassificationPlan(User user, String filename)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    try {
      JsonFactory factory = new JsonFactory();
      ObjectMapper mapper = new ObjectMapper(factory);
      ObjectNode root = mapper.createObjectNode();

      ArrayNode array = mapper.createArrayNode();
      List<String> descriptionsLevels = RodaUtils
        .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.LEVELS_CLASSIFICATION_PLAN));

      Filter allButRepresentationsFilter = new Filter(
        new OneOfManyFilterParameter(RodaConstants.AIP_LEVEL, descriptionsLevels));

      IndexService index = RodaCoreFactory.getIndexService();
      boolean justActive = true;
      try (IterableIndexResult<IndexedAIP> res = index.findAll(IndexedAIP.class, allButRepresentationsFilter, user,
        justActive, new ArrayList<>())) {
        Iterator<IndexedAIP> it = res.iterator();
        while (it.hasNext()) {
          array.add(aipToJSON(it.next()));
        }
      }

      root.set("dos", array);

      return new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_APPLICATION_JSON, out -> {
        try {
          mapper.writeValue(out, root);
        } catch (IOException e) {
          // do nothing
        }
      });
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public static ObjectNode aipToJSON(IndexedAIP indexedAIP)
    throws IOException, RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    ModelService model = RodaCoreFactory.getModelService();

    ObjectNode node = mapper.createObjectNode();
    if (indexedAIP.getTitle() != null) {
      node = node.put("title", indexedAIP.getTitle());
    }
    if (indexedAIP.getId() != null) {
      node = node.put("id", indexedAIP.getId());
    }
    if (indexedAIP.getParentID() != null) {
      node = node.put("parentId", indexedAIP.getParentID());
    }
    if (indexedAIP.getLevel() != null) {
      node = node.put("descriptionlevel", indexedAIP.getLevel());
    }
    AIP modelAIP = model.retrieveAIP(indexedAIP.getId());

    if (modelAIP.getType() != null) {
      ObjectNode contentTypeNode = mapper.createObjectNode();
      contentTypeNode = contentTypeNode.put("packageType", RodaConstants.AIP_DEFAULT_PACKAGE_TYPE);
      contentTypeNode = contentTypeNode.put("value", modelAIP.getType());
      node.set("contentType", contentTypeNode);
    }
    if (modelAIP != null) {
      List<DescriptiveMetadata> descriptiveMetadata = modelAIP.getDescriptiveMetadata();
      if (descriptiveMetadata != null && !descriptiveMetadata.isEmpty()) {
        ArrayNode metadata = mapper.createArrayNode();
        for (DescriptiveMetadata dm : descriptiveMetadata) {
          ObjectNode dmNode = mapper.createObjectNode();
          if (dm.getId() != null) {
            dmNode = dmNode.put("id", dm.getId());
          }
          if (dm.getType() != null) {
            dmNode = dmNode.put("metadataType", dm.getType());
          }
          if (dm.getVersion() != null) {
            dmNode = dmNode.put("metadataVersion", dm.getVersion());
          }
          Binary b = model.retrieveDescriptiveMetadataBinary(modelAIP.getId(), dm.getId());
          InputStream is = b.getContent().createInputStream();
          dmNode = dmNode.put("content", new String(Base64.encodeBase64(IOUtils.toByteArray(is))));
          IOUtils.closeQuietly(is);
          dmNode = dmNode.put("contentEncoding", "Base64");

          metadata = metadata.add(dmNode);
        }
        node.set("metadata", metadata);
      }
    }
    return node;
  }
}
