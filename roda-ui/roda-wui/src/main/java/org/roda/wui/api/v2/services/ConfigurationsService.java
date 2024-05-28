package org.roda.wui.api.v2.services;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.wui.client.browse.Viewers;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class ConfigurationsService {

  private static final String UI_VIEWERS_PREFIX = "ui.viewers.";

  public Viewers getViewerConfigurations() {
    Viewers viewers = new Viewers();
    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    List<String> viewersSupported = RodaUtils.copyList(rodaConfig.getList("ui.viewers"));

    for (String type : viewersSupported) {
      List<String> fieldPronoms = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".pronoms"));
      List<String> fieldMimetypes = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".mimetypes"));
      List<String> fieldExtensions = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".extensions"));

      for (String pronom : fieldPronoms) {
        viewers.addPronom(pronom, type);
      }

      for (String mimetype : fieldMimetypes) {
        viewers.addMimetype(mimetype, type);
      }

      for (String extension : fieldExtensions) {
        viewers.addExtension(extension, type);
      }

      viewers.setTextLimit(rodaConfig.getString(UI_VIEWERS_PREFIX + "text.limit", ""));
      viewers.setOptions(rodaConfig.getString(UI_VIEWERS_PREFIX + type + ".options", ""));
    }

    return viewers;
  }
}
