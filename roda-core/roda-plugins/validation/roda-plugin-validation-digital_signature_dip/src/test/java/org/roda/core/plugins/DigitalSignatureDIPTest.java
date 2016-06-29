package org.roda.core.plugins;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Test;
import org.roda.common.certification.PDFSignatureUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.validation.DigitalSignatureDIPPlugin;
import org.roda.core.plugins.plugins.validation.DigitalSignatureDIPPluginUtils;
import org.roda.core.storage.DirectResourceAccess;

public class DigitalSignatureDIPTest extends AbstractConvertTest {

  @Test
  public void testDigitalSignatureDIPPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException, SolrServerException, IsStillUpdatingException {
    AIP aip = ingestCorpora(2);
    CloseableIterable<OptionalWithCause<File>> allFiles = getModel().listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    OptionalWithCause<File> file = allFiles.iterator().next();

    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file.get());
    DirectResourceAccess directAccess = getModel().getStorage().getDirectAccess(fileStoragePath);
    Assert.assertEquals(0, PDFSignatureUtils.countSignaturesPDF(directAccess.getPath()));
    IOUtils.closeQuietly(directAccess);

    Plugin<Representation> plugin = new DigitalSignatureDIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, getFakeJobId());
    plugin.setParameterValues(parameters);

    DigitalSignatureDIPPluginUtils.setKeystorePath(getCorporaPath().toString() + "/Certification/keystore.jks");
    // FIXME 20160623 hsilva: passing by null just to make code compiling
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(null, plugin);

    aip = getModel().retrieveAIP(aip.getId());
    CloseableIterable<OptionalWithCause<File>> allNewFiles = getModel().listFilesUnder(aip.getId(),
      aip.getRepresentations().get(1).getId(), true);
    OptionalWithCause<File> newFile = allNewFiles.iterator().next();

    StoragePath newFileStoragePath = ModelUtils.getFileStoragePath(newFile.get());
    DirectResourceAccess newDirectAccess = getModel().getStorage().getDirectAccess(newFileStoragePath);
    Assert.assertEquals(1, PDFSignatureUtils.countSignaturesPDF(newDirectAccess.getPath()));
    IOUtils.closeQuietly(newDirectAccess);
  }

}
