/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateLocalInstanceConfiguration extends Composite {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FlowPanel description;  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateLocalInstanceConfiguration createLocalInstanceConfiguration = new CreateLocalInstanceConfiguration();
      callback.onSuccess(createLocalInstanceConfiguration);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {CreateLocalInstanceConfiguration.RESOLVER}, false,
        callback);
    }

    @Override
    public String getHistoryToken() {
      return "create_instance_configuration";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(LocalInstanceManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };
  @UiField(provided = true)
  LocalInstanceConfigurationDataPanel localInstanceConfigurationDataPanel;
  private LocalInstance localInstance;
  public CreateLocalInstanceConfiguration() {
    this.localInstance = new LocalInstance();
    this.localInstanceConfigurationDataPanel = new LocalInstanceConfigurationDataPanel(localInstance, false);
    this.localInstanceConfigurationDataPanel.setLocalInstanceConfiguration(localInstance);

    initWidget(uiBinder.createAndBindUi(this));

    this.description.add(new HTMLWidgetWrapper(("LocalInstanceDescription.html")));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (localInstanceConfigurationDataPanel.isValid()) {
      LocalInstance localInstanceReturned = localInstanceConfigurationDataPanel.getLocalInstance();
      BrowserService.Util.getInstance().createLocalInstance(localInstanceReturned,
        new NoAsyncCallback<DistributedInstance>() {
          @Override
          public void onSuccess(DistributedInstance distributedInstance) {
            HistoryUtils.newHistory(LocalInstanceManagement.RESOLVER);
          }
        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(LocalInstanceManagement.RESOLVER);
  }

  interface MyUiBinder extends UiBinder<Widget, CreateLocalInstanceConfiguration> {
  }



}
