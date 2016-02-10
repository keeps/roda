package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import org.xml.sax.SAXException;

public class AbstractConvertPluginUtils {

  public static void runReindexingPlugins(String aipId) throws InvalidParameterException {
    // TODO change to execute on the AIP with the new representation
    Plugin<AIP> psp = new PremisSkeletonPlugin();
    Plugin<AIP> sfp = new SiegfriedPlugin();
    Plugin<AIP> ttp = new TikaFullTextPlugin();

    Map<String, String> params = new HashMap<String, String>();
    params.put("createsPluginEvent", "false");
    psp.setParameterValues(params);
    sfp.setParameterValues(params);
    ttp.setParameterValues(params);

    PluginOrchestrator pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
    pluginOrchestrator.runPluginOnAIPs(psp, Arrays.asList(aipId));
    pluginOrchestrator.runPluginOnAIPs(sfp, Arrays.asList(aipId));
    pluginOrchestrator.runPluginOnAIPs(ttp, Arrays.asList(aipId));
  }

  public static void reIndexingRepresentation(IndexService index, ModelService model, StorageService storage,
    String aipId, String representationId) throws IOException, RequestNotValidException, GenericException,
      NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
      TikaException, ValidationException, XmlException, InvalidParameterException {

    AIP aip = model.retrieveAIP(aipId);
    Representation representation = model.retrieveRepresentation(aipId, representationId);

    PremisSkeletonPluginUtils.createPremisForRepresentation(model, storage, aip, representationId);

    SiegfriedPluginUtils.runSiegfriedOnRepresentation(index, model, storage, aip, representation, null, false);
    TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation);
  }

}
