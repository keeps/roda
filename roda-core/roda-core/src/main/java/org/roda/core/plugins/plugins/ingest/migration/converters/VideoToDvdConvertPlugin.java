package org.roda.core.plugins.plugins.ingest.migration.converters;

import java.util.Map;

import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.migration.MencoderConvertPlugin;

public class VideoToDvdConvertPlugin extends MencoderConvertPlugin {

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    outputFormat = "mpeg";
    conversionProfile = "toDvd";
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new VideoToDvdConvertPlugin();
  }

}
