package org.roda.core.migration.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FormatToVersion2 implements MigrationAction<Format> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormatToVersion2.class);

  @Override
  public void migrate(StorageService storage) throws RODAException {
    CloseableIterable<Resource> formats = null;

    try {
      formats = storage.listResourcesUnderDirectory(ModelUtils.getFormatContainerPath(), false);

      for (Resource resource : formats) {
        if (!resource.isDirectory() && resource instanceof Binary) {
          Binary binary = (Binary) resource;
          migrate(storage, binary);
        }
      }
    } catch (NotFoundException e) {
      LOGGER.warn("Could not find resource", e);
    } finally {
      IOUtils.closeQuietly(formats);
    }
  }

  private void migrate(StorageService storage, Binary binary) {
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      JsonNode json = JsonUtils.parseJson(inputStream);
      if (json instanceof ObjectNode) {
        ObjectNode obj = (ObjectNode) json;
        // obj = JsonUtils.refactor(obj, MAPPING);

        StringContentPayload payload = new StringContentPayload(JsonUtils.getJsonFromNode(obj));
        boolean asReference = false;
        boolean createIfNotExists = false;
        storage.updateBinaryContent(binary.getStoragePath(), payload, asReference, createIfNotExists);
      } else {
        LOGGER.error("Could not migrate format {} because the JSON is not an object node", binary.getStoragePath());
      }

    } catch (IOException | RODAException e) {
      LOGGER.error("Could not migrate format {}", binary.getStoragePath(), e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

  }

  @Override
  public boolean isToVersionValid(int toVersion) {
    return toVersion == 2;
  }

}
