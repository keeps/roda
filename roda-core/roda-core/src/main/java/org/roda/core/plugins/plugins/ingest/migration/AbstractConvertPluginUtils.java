/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.xmlbeans.XmlException;
import org.roda.core.RodaCoreFactory;
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
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.xml.sax.SAXException;

public class AbstractConvertPluginUtils {

  public static <T extends Serializable> void reIndexingRepresentationAfterConversion(Plugin<T> plugin,
    IndexService index, ModelService model, String aipId, String representationId) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException,
    SAXException, TikaException, ValidationException, InvalidParameterException, XmlException, IOException {

    AIP aip = model.retrieveAIP(aipId);
    Representation representation = model.retrieveRepresentation(aipId, representationId);
    // TODO set agent
    List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
    PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aip, representationId, algorithms);
    SiegfriedPluginUtils.runSiegfriedOnRepresentation(plugin, index, model, aip, representation);
    TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(null, index, model, aip, representation, true, true);
  }

}
