package org.roda.core.plugins.plugins.ingest.migration.converters;

import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.migration.ImageMagickConvertPlugin;

public class ImagetoTiffConvertPlugin extends ImageMagickConvertPlugin {

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    outputFormat = "tiff";
    conversionProfile = "toTiff";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ImagetoTiffConvertPlugin();
  }

}
