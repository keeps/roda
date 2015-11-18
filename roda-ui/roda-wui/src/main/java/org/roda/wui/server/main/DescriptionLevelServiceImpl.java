/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.eadc.DescriptionLevelManager;
import org.roda.core.util.FileUtility;
import org.roda.wui.client.main.DescriptionLevelInfoPack;
import org.roda.wui.client.main.DescriptionLevelService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DescriptionLevelServiceImpl extends RemoteServiceServlet implements DescriptionLevelService {

  private static final long serialVersionUID = 1133363430147430537L;

  private static final Logger logger = LoggerFactory.getLogger(DescriptionLevelServiceImpl.class);

  static {
    try {
      Properties descriptionLevels = new Properties();
      descriptionLevels.load(FileUtility.getConfigurationFile(RodaCoreFactory.getConfigPath(),
        "roda-description-levels-hierarchy.properties"));

      new DescriptionLevelManager(descriptionLevels);
    } catch (IOException ex) {
      logger.error("Error loading description levels", ex);
    } catch (IllegalArgumentException e) {
      logger.error("Error loading description levels", e);
    }
  }

  @Override
  public List<String> getDescriptionLevels() {
    return DescriptionLevelManager.getLevels();
  }

  @Override
  public DescriptionLevelInfoPack getAllDescriptionLevels() {
    DescriptionLevelInfoPack pack = new DescriptionLevelInfoPack();

    pack.setDescriptionLevelsInfo(DescriptionLevelManager.getDescriptionLevelsInfo());
    pack.setDescriptionLevels(DescriptionLevelManager.getDescriptionLevels());
    pack.setRootDescriptionLevels(DescriptionLevelManager.getRootDescriptionLevels());
    pack.setLeafDescriptionLevels(DescriptionLevelManager.getLeafDescriptionLevels());
    pack.setRepresentationDescriptionLevels(DescriptionLevelManager.getRepresentationsDescriptionLevels());
    pack.setAllButRepresentationDescriptionLevels(DescriptionLevelManager.getAllButRepresentationsDescriptionLevels());

    return pack;
  }

}
