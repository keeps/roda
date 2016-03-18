/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;

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
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPluginUtils;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPluginUtils;
import org.roda.core.storage.StorageService;
import org.xml.sax.SAXException;

public class AbstractConvertPluginUtils {

  public static void reIndexingRepresentationAfterConversion(IndexService index, ModelService model,
    StorageService storage, String aipId, String representationId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, PluginException, AlreadyExistsException, SAXException,
    TikaException, ValidationException, InvalidParameterException, XmlException, IOException {

    AIP aip = model.retrieveAIP(aipId);
    Representation representation = model.retrieveRepresentation(aipId, representationId);
    // TODO set agent
    PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, storage, aip, representationId);
    SiegfriedPluginUtils.runSiegfriedOnRepresentation(index, model, storage, aip, representation);
    TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation);
  }

}
