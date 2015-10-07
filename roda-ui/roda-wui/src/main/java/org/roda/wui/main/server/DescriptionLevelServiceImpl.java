package org.roda.wui.main.server;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.roda.common.RodaCoreFactory;
import org.roda.core.data.eadc.DescriptionLevelManager;
import org.roda.wui.main.client.DescriptionLevelInfoPack;
import org.roda.wui.main.client.DescriptionLevelService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DescriptionLevelServiceImpl extends RemoteServiceServlet implements DescriptionLevelService {

  private static final long serialVersionUID = 1133363430147430537L;

  private static final Logger logger = Logger.getLogger(DescriptionLevelServiceImpl.class);

  static {
    try {
      Properties descriptionLevels = new Properties();
      descriptionLevels.load(RodaCoreFactory.getConfigurationFile("roda-description-levels-hierarchy.properties"));

      new DescriptionLevelManager(descriptionLevels);
    } catch (IOException ex) {
      logger.error(ex);
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
