package pt.gov.dgarq.roda.common;

import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class RodaCorePropertiesReloadStrategy extends FileChangedReloadingStrategy {

  public void reloadingPerformed() {
    RodaCoreFactory.reloadRodaConfigurationsAfterFileChange();
    super.updateLastModified();
  }

}
