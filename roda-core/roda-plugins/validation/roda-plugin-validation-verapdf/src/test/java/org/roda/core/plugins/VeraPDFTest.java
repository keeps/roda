package org.roda.core.plugins;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.plugins.validation.VeraPDFPlugin;

public class VeraPDFTest extends AbstractConvertTest {

  @Test
  public void testVeraPDFPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    SolrServerException, IsStillUpdatingException {
    ingestCorpora(2);

    Plugin<AIP> plugin = new VeraPDFPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, getFakeJobId());
    parameters.put("profile", "1b");
    parameters.put("hasFeatures", "False");
    plugin.setParameterValues(parameters);

    // FIXME 20160623 hsilva: passing by null just to make code compiling
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(null, plugin);
    // Report report0 = reports.get(0).getReports().get(0);
    // if (!PluginState.PARTIAL_SUCCESS.equals(report0.getPluginState())) {
    // Assert.fail("Report not a partial success: " + report0);
    // }

    // Plugin<Representation> plugin2 = new PdfToPdfaPlugin<Representation>();
    // FIXME 20160623 hsilva: passing by null just to make code compiling
    // RodaCoreFactory.getPluginOrchestrator().runPluginOnAllRepresentations(null,
    // plugin2);

    // Plugin<AIP> plugin3 = new VeraPDFPlugin();
    // plugin3.setParameterValues(parameters);
    // FIXME 20160623 hsilva: passing by null just to make code compiling
    // RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs(null,
    // plugin3);
    // Report report1 = reports.get(0).getReports().get(1);
    // if (!PluginState.SUCCESS.equals(report1.getPluginState())) {
    // Assert.fail("Report failed: " + report1);
    // }

  }

}
