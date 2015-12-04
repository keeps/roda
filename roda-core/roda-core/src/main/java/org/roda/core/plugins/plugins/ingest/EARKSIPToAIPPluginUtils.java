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

import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FileStorageService;
import org.roda_project.commons_ip.migration.impl.eark.EARKSIPToRODAAIP;
import org.roda_project.commons_ip.model.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPluginUtils.class);

  public static AIP earkSIPToAip(Path sipPath, ModelService model, StorageService storage)
    throws IOException, StorageServiceException, ModelServiceException, MigrationException {
    EARKSIPToRODAAIP migrator = new EARKSIPToRODAAIP();
    Path aip = migrator.convert(sipPath);
    
    StorageService temp = new FileStorageService(aip.getParent());
    model.createAIP(aip.getFileName().toString(), temp, DefaultStoragePath.parse(aip.getFileName().toString()));
    
    return model.retrieveAIP(aip.getFileName().toString());

  }
}
