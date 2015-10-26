/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.main.client;

import java.util.List;

import org.roda.core.data.eadc.DescriptionLevel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("descriptionlevel")
public interface DescriptionLevelService extends RemoteService {

  List<String> getDescriptionLevels();

  DescriptionLevelInfoPack getAllDescriptionLevels();
}
