/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditFormat extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String formatId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(Format.class.getName(), formatId, fieldsToReturn,
          new AsyncCallback<Format>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(Format format) {
              EditFormat editFormat = new EditFormat(format);
              callback.onSuccess(editFormat);
            }
          });
      } else {
        HistoryUtils.newHistory(FormatRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(FormatRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_format";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditFormat> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Format format;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FORMAT_ID,
    RodaConstants.FORMAT_NAME, RodaConstants.FORMAT_DEFINITION, RodaConstants.FORMAT_CATEGORY,
    RodaConstants.FORMAT_LATEST_VERSION, RodaConstants.FORMAT_DEVELOPER, RodaConstants.FORMAT_POPULARITY,
    RodaConstants.FORMAT_INITIAL_RELEASE, RodaConstants.FORMAT_IS_OPEN_FORMAT, RodaConstants.FORMAT_STANDARD,
    RodaConstants.FORMAT_WEBSITE, RodaConstants.FORMAT_PROVENANCE_INFORMATION, RodaConstants.FORMAT_EXTENSIONS,
    RodaConstants.FORMAT_MIMETYPES, RodaConstants.FORMAT_PRONOMS, RodaConstants.FORMAT_UTIS,
    RodaConstants.FORMAT_ALTERNATIVE_DESIGNATIONS, RodaConstants.FORMAT_VERSIONS);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  FormatDataPanel formatDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public EditFormat(Format format) {
    this.format = format;
    this.formatDataPanel = new FormatDataPanel(true, true, format);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (formatDataPanel.isChanged() && formatDataPanel.isValid()) {
      String formatId = format.getId();
      format = formatDataPanel.getFormat();
      format.setId(formatId);
      BrowserService.Util.getInstance().updateFormat(format, new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }

        @Override
        public void onSuccess(Void result) {
          HistoryUtils.newHistory(ShowFormat.RESOLVER, format.getId());
        }

      });
    } else {
      HistoryUtils.newHistory(ShowFormat.RESOLVER, format.getId());
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().deleteFormat(
      new SelectedItemsList<Format>(Arrays.asList(format.getUUID()), Format.class.getName()), new AsyncCallback<Job>() {
        @Override
        public void onFailure(Throwable caught) {
          HistoryUtils.newHistory(InternalProcess.RESOLVER);
        }

        @Override
        public void onSuccess(Job result) {
          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              Timer timer = new Timer() {
                @Override
                public void run() {
                  HistoryUtils.newHistory(FormatRegister.RESOLVER);
                }
              };

              timer.schedule(RodaConstants.ACTION_TIMEOUT);
            }

            @Override
            public void onSuccess(final Void nothing) {
              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
            }
          });
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowFormat.RESOLVER, format.getId());
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editFormatNotFound(format.getName()));
      cancel();
    } else {
      AsyncCallbackUtils.defaultFailureTreatment(caught);
    }
  }

  protected void enableApplyButton(boolean enabled) {
    buttonApply.setVisible(enabled);
  }
}
