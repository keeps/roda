/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.Path;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.MigrationException;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPDescriptiveMetadata;
import org.roda_project.commons_ip.model.SIPRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPluginUtils.class);

  public static AIP earkSIPToAIP(SIP sip, Path sipPath, ModelService model, StorageService storage, String parentId)
    throws IOException, MigrationException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    boolean active = false;
    AIPPermissions permissions = new AIPPermissions();
    boolean notify = true;

    // TODO check if parent exists

    AIP aip = model.createAIP(active, parentId, permissions, notify);

    if (sip.getRepresentations() != null && sip.getRepresentations().size() > 0) {
      for (SIPRepresentation sr : sip.getRepresentations()) {
        PluginHelper.createDirectories(model, aip.getId(), sr.getObjectID());
        if (sr.getData() != null && sr.getData().size() > 0) {
          for (Path p : sr.getData()) {
            Binary fileBinary = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
            model.createFile(aip.getId(), sr.getObjectID(), p.getFileName().toString(), fileBinary);
          }
        }
        /*
         * if(sr.getAdministrativeMetadata()!=null &&
         * sr.getAdministrativeMetadata().size()>0){ for (SIPMetadata dm :
         * sr.getAdministrativeMetadata()) { Binary fileBinary = (Binary)
         * FSUtils.convertPathToResource(dm.getMetadata().getParent(),
         * dm.getMetadata()); model.createDescriptiveMetadata(aip.getId(),
         * "rep_"+sr.getObjectID()+"_admin_"+dm.getMetadata().getFileName().
         * toString(), fileBinary, "XXX"); } }
         * if(sr.getDescriptiveMetadata()!=null &&
         * sr.getDescriptiveMetadata().size()>0){ for (SIPMetadata dm :
         * sr.getDescriptiveMetadata()) { Binary fileBinary = (Binary)
         * FSUtils.convertPathToResource(dm.getMetadata().getParent(),
         * dm.getMetadata()); model.createDescriptiveMetadata(aip.getId(),
         * "rep_"+sr.getObjectID()+"_descriptive_"+dm.getMetadata().getFileName(
         * ).toString(), fileBinary, "XXX"); } } if(sr.getOtherMetadata()!=null
         * && sr.getOtherMetadata().size()>0){ for (SIPMetadata dm :
         * sr.getOtherMetadata()) { Binary fileBinary = (Binary)
         * FSUtils.convertPathToResource(dm.getMetadata().getParent(),
         * dm.getMetadata()); model.createDescriptiveMetadata(aip.getId(),
         * "rep_"+sr.getObjectID()+"_other_"+dm.getMetadata().getFileName().
         * toString(), fileBinary, "XXX"); } }
         */
      }
    }

    if (sip.getDescriptiveMetadata() != null && sip.getDescriptiveMetadata().size() > 0) {
      for (SIPDescriptiveMetadata dm : sip.getDescriptiveMetadata()) {
        Binary fileBinary = (Binary) FSUtils.convertPathToResource(dm.getMetadata().getParent(), dm.getMetadata());
        String type = (dm.getMetadataType() != null) ? dm.getMetadataType().toString() : "";
        model.createDescriptiveMetadata(aip.getId(), dm.getMetadata().getFileName().toString(), fileBinary, type);
      }
    }
    /*
     * if (sip.getAdministrativeMetadata() != null &&
     * sip.getAdministrativeMetadata().size() > 0) { for (SIPMetadata dm :
     * sip.getAdministrativeMetadata()) { Binary fileBinary = (Binary)
     * FSUtils.convertPathToResource(dm.getMetadata().getParent(),
     * dm.getMetadata());
     * model.createDescriptiveMetadata(aip.getId(),dm.getMetadata().getFileName(
     * ).toString(), fileBinary, "XXX"); } } if (sip.getOtherMetadata() != null
     * && sip.getOtherMetadata().size() > 0) { for (SIPMetadata dm :
     * sip.getOtherMetadata()) { Binary fileBinary = (Binary)
     * FSUtils.convertPathToResource(dm.getMetadata().getParent(),
     * dm.getMetadata()); model.createDescriptiveMetadata(aip.getId(),
     * dm.getMetadata().getFileName().toString(), fileBinary, "XXX"); } }
     */
    return model.retrieveAIP(aip.getId());

  }
}
