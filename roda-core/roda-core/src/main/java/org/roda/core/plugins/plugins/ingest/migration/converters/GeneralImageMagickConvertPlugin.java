package org.roda.core.plugins.plugins.ingest.migration.converters;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.migration.ImageMagickConvertPlugin;

public class GeneralImageMagickConvertPlugin extends ImageMagickConvertPlugin {

  @Override
  public Plugin<AIP> cloneMe() {
    return new GeneralImageMagickConvertPlugin();
  }

}
