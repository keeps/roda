/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConvertPluginUtils.class);

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

    // FIXME 20160315 hsilva: do not invoke directly or even instantiate a
    // pluginorchestrator
    PluginOrchestrator pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
    pluginOrchestrator.runPluginOnAIPs(psp, new ArrayList<String>(aipSet));
    pluginOrchestrator.runPluginOnAIPs(sfp, new ArrayList<String>(aipSet));
    pluginOrchestrator.runPluginOnAIPs(ttp, new ArrayList<String>(aipSet));

    for (String aipId : aipSet) {
      // FIXME 20160315 hsilva: is it really necessary to generate an AIP event?
      model.notifyAIPUpdated(aipId);
    }
  }

  public static void reIndexingRepresentationAfterConversion(IndexService index, ModelService model,
    StorageService storage, String aipId, String representationId, boolean notify) throws RequestNotValidException,
      GenericException, NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException,
      SAXException, TikaException, ValidationException, InvalidParameterException, XmlException, IOException {

    AIP aip = model.retrieveAIP(aipId);
    Representation representation = model.retrieveRepresentation(aipId, representationId);
    // TODO set agent
    PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, storage, aip, representationId);
    SiegfriedPluginUtils.runSiegfriedOnRepresentation(index, model, storage, aip, representation);
    TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation);

    if (notify) {
      // FIXME 20160315 hsilva: is it really necessary to generate an AIP event?
      model.notifyAIPUpdated(aipId);
    }
  }

}
