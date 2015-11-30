/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.File;
import org.roda.core.data.v2.IndexResult;
import org.roda.wui.client.common.FileList;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class ViewRepresentation extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.get(1);

        ViewRepresentation view = new ViewRepresentation(aipId, representationId);
        callback.onSuccess(view);
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "view";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ViewRepresentation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private String aipId;
  private String representationId;

  @UiField(provided = true)
  FileList filesPanel;

  @UiField
  Button back;

  @UiField
  Button nextFile;

  @UiField
  Button previousFile;

  @UiField
  Button downloadFile;

  /**
   * Create a new panel to edit a user
   * 
   * @param descriptiveMetadataId
   * @param aipId
   * 
   * @param user
   *          the user to edit
   */
  public ViewRepresentation(String aipId, String representationId) {
    this.aipId = aipId;
    this.representationId = representationId;

    filesPanel = new FileList();
    filesPanel.setFilter(new Filter(new SimpleFilterParameter()));

    initWidget(uiBinder.createAndBindUi(this));

    back.setText(messages.backButton());
    nextFile.setText(messages.viewRepresentationNextFileButton());
    previousFile.setText(messages.viewRepresentationPreviousFileButton());
    downloadFile.setText(messages.viewRepresentationDownloadFileButton());

    // nextFile.setVisible(false);
    // previousFile.setVisible(false);
    // downloadFile.setVisible(false);

    filesPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        File file = filesPanel.getSelectionModel().getSelectedObject();

      }
    });

    filesPanel.addValueChangeHandler(new ValueChangeHandler<IndexResult<File>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<File>> event) {

      }
    });
  }

  @UiHandler("back")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }
}
