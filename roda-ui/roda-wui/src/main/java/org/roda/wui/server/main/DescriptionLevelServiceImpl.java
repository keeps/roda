/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.main;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DescriptionLevelManager;
import org.roda.wui.client.main.DescriptionLevelInfoPack;
import org.roda.wui.client.main.DescriptionLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DescriptionLevelServiceImpl extends RemoteServiceServlet implements DescriptionLevelService {

  private static final long serialVersionUID = 1133363430147430537L;

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(DescriptionLevelServiceImpl.class);

  @Override
  public List<String> getDescriptionLevels() {
    return RodaCoreFactory.getDescriptionLevelManager().getLevels();
  }

  @Override
  public DescriptionLevelInfoPack getAllDescriptionLevels() {
    DescriptionLevelManager descriptionLevelManager = RodaCoreFactory.getDescriptionLevelManager();

    DescriptionLevelInfoPack pack = new DescriptionLevelInfoPack();

    pack.setDescriptionLevels(descriptionLevelManager.getDescriptionLevels());
    
    pack.setDefaultClass(descriptionLevelManager.getDefaultClass());
    
    pack.setGhostClass(descriptionLevelManager.getGhostClass());
    return pack;
  }

}
