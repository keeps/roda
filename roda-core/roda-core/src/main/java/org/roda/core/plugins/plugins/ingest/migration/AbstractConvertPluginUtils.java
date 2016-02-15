package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.xmlbeans.XmlException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class AbstractConvertPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  public static void reIndexPlugins(ModelService model, Set<String> aipSet) throws InvalidParameterException,
    RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    Plugin<AIP> psp = new PremisSkeletonPlugin();
    Plugin<AIP> sfp = new SiegfriedPlugin();
    Plugin<AIP> ttp = new TikaFullTextPlugin();

    Map<String, String> params = new HashMap<String, String>();
    params.put("createsPluginEvent", "false");
    psp.setParameterValues(params);
    sfp.setParameterValues(params);
    ttp.setParameterValues(params);

    PluginOrchestrator pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
    pluginOrchestrator.runPluginOnAIPs(psp, new ArrayList<String>(aipSet));
    pluginOrchestrator.runPluginOnAIPs(sfp, new ArrayList<String>(aipSet));
    pluginOrchestrator.runPluginOnAIPs(ttp, new ArrayList<String>(aipSet));

    for (String aipId : aipSet) {
      model.notifyAIPUpdated(aipId);
    }
  }

  public static void reIndexingRepresentation(IndexService index, ModelService model, StorageService storage,
    String aipId, String representationId, boolean notify) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
    TikaException, ValidationException, InvalidParameterException, XmlException, IOException {

    AIP aip = model.retrieveAIP(aipId);
    Representation representation = model.retrieveRepresentation(aipId, representationId);
    boolean createPluginEvent = false;
    boolean inotify = false;
    // TODO set agent
    IndexedPreservationAgent agent = null;
    PremisSkeletonPluginUtils.runPremisSkeletonOnRepresentation(model, storage, aip, representationId, inotify);
    SiegfriedPluginUtils.runSiegfriedOnRepresentation(index, model, storage, aip, representation, agent,
      createPluginEvent, inotify);
    TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation, inotify);

    if (notify) {
      model.notifyAIPUpdated(aipId);
    }
  }

}
