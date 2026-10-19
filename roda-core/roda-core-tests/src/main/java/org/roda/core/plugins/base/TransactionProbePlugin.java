/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.roda.core.catalog.CatalogReader;
import org.roda.core.catalog.Catalogs;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;

/**
 * Creates an AIP and records what the index and the catalog show about it while
 * the plugin is still running (i.e. inside its transaction). Optionally fails
 * afterwards, so that the transaction is rolled back.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransactionProbePlugin extends AbstractPlugin<Void> {
  public static final String PARAM_AIP_ID = "probe.aip_id";
  public static final String PARAM_SIP_ID = "probe.sip_id";
  public static final String PARAM_FAIL = "probe.fail";

  /** What the plugin saw inside its transaction, by AIP id. */
  public record Observation(boolean transactional, boolean visibleInIndex, boolean visibleInCatalog) {
  }

  public static final Map<String, Observation> OBSERVATIONS = new ConcurrentHashMap<>();

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> list)
    throws PluginException {
    String aipId = getParameterValues().get(PARAM_AIP_ID);
    String sipId = getParameterValues().get(PARAM_SIP_ID);
    try {
      model.createAIP(AIPState.ACTIVE, null, "probe", new Permissions(), null, List.of(sipId),
        PluginHelper.getJobId(this), true, "admin", aipId);
    } catch (RODAException e) {
      throw new PluginException("Could not create the probe AIP", e);
    }

    boolean visibleInIndex;
    try {
      index.retrieve(IndexedAIP.class, aipId, Collections.emptyList());
      visibleInIndex = true;
    } catch (NotFoundException e) {
      visibleInIndex = false;
    } catch (RODAException e) {
      throw new PluginException(e);
    }

    boolean visibleInCatalog = Catalogs.reader(model).map(reader -> containsAIP(reader, sipId, aipId)).orElse(false);
    OBSERVATIONS.put(aipId,
      new Observation(model instanceof TransactionalModelService, visibleInIndex, visibleInCatalog));

    if (Boolean.parseBoolean(getParameterValues().get(PARAM_FAIL))) {
      throw new PluginException("Probe failing on purpose to roll back its transaction");
    }
    return null;
  }

  private static boolean containsAIP(CatalogReader reader, String sipId, String aipId) {
    return reader.findByIngestSIPId(sipId).stream().anyMatch(aip -> aip.id().equals(aipId))
      && reader.get(aipId).isPresent();
  }

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public String getDescription() {
    return getClass().getName();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return null;
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<String> getCategories() {
    return Collections.emptyList();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new TransactionProbePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() {
    // nothing to do
  }

  @Override
  public void shutdown() {
    // nothing to do
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) {
    return null;
  }
}
