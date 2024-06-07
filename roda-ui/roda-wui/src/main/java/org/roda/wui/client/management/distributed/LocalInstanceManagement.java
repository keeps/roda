/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import com.google.gwt.core.client.GWT;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.Management;
import org.roda.wui.client.services.DistributedInstancesRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalInstanceManagement extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        Services services = new Services("Get local instance", "get");
        services.distributedInstanceResource(DistributedInstancesRestService::getLocalInstance)
          .whenComplete((localInstance, error) -> {
            if (!localInstance.equals(new LocalInstance())) {
              HistoryUtils.newHistory(ShowLocalInstanceConfiguration.RESOLVER);
            } else {
              HistoryUtils.newHistory(CreateLocalInstanceConfiguration.RESOLVER);
            }
          });
      } else if (historyTokens.get(0).equals(EditLocalInstanceConfiguration.RESOLVER.getHistoryToken())) {
        EditLocalInstanceConfiguration.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(CreateLocalInstanceConfiguration.RESOLVER.getHistoryToken())) {
        CreateLocalInstanceConfiguration.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(ShowLocalInstanceConfiguration.RESOLVER.getHistoryToken())) {
        ShowLocalInstanceConfiguration.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {LocalInstanceManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "instance_management";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };
}
