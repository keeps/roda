/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DescriptionLevelServiceAsync {

  DescriptionLevelServiceAsync INSTANCE = GWT.create(DescriptionLevelService.class);

  void getDescriptionLevelConfiguration(String localeString, AsyncCallback<DescriptionLevelConfiguration> callback);

}
