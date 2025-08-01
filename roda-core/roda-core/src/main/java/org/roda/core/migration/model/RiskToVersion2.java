/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.migration.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RiskToVersion2 implements MigrationAction<Risk> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiskToVersion2.class);

  private static final Map<String, String> MAPPING = new HashMap<>();

  static {
    MAPPING.put("posMitigationProbability", "postMitigationProbability");
    MAPPING.put("posMitigationImpact", "postMitigationImpact");
    MAPPING.put("posMitigationSeverity", "postMitigationSeverity");
    MAPPING.put("posMitigationSeverityLevel", "postMitigationSeverityLevel");
    MAPPING.put("posMitigationNotes", "postMitigationNotes");
  }

  @Override
  public void migrate(ModelService model) throws RODAException {
    try (CloseableIterable<OptionalWithCause<Risk>> risks = model.list(Risk.class)) {
      for (OptionalWithCause<Risk> risk : risks) {
        if (!model.hasDirectory(risk.get())) {
          Binary binary = model.getBinary(risk.get());
          migrate(model, binary, risk.get());
        }
      }
    } catch (IOException e) {
      throw new RODAException(e);
    }
  }

  private void migrate(ModelService model, Binary binary, Risk risk) {
    try (InputStream inputStream = binary.getContent().createInputStream()) {
      JsonNode json = JsonUtils.parseJson(inputStream);
      if (json instanceof ObjectNode) {
        ObjectNode obj = (ObjectNode) json;
        obj = JsonUtils.refactor(obj, MAPPING);

        StringContentPayload payload = new StringContentPayload(JsonUtils.getJsonFromNode(obj));
        boolean asReference = false;
        boolean createIfNotExists = false;
        boolean snapshotCurrentVersion = false;
        model.updateBinaryContent(risk, payload, asReference, createIfNotExists, snapshotCurrentVersion, null);
      } else {
        LOGGER.error("Could not migrate risk {} because the JSON is not an object node", binary.getStoragePath());
      }

    } catch (IOException | RODAException e) {
      LOGGER.error("Could not migrate risk {}", binary.getStoragePath(), e);
    }
  }

  @Override
  public boolean isToVersionValid(int toVersion) {
    return toVersion == 2;
  }

}
