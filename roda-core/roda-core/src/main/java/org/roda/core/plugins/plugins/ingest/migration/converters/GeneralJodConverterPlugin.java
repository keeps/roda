package org.roda.core.plugins.plugins.ingest.migration.converters;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.migration.JodConverterPlugin;

public class GeneralJodConverterPlugin extends JodConverterPlugin {

  @Override
  public Plugin<AIP> cloneMe() {
    return new GeneralJodConverterPlugin();
  }

}

