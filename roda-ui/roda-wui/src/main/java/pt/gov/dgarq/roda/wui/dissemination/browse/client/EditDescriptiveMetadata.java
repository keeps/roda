/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.MessagePopup;

/**
 * @author Luis Faria
 * 
 */
public class EditDescriptiveMetadata extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String descriptiveMetadataId = historyTokens.get(1);

        BrowserService.Util.getInstance().getDescriptiveMetadataEditBundle(aipId, descriptiveMetadataId,
          new AsyncCallback<DescriptiveMetadataEditBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(DescriptiveMetadataEditBundle bundle) {
            EditDescriptiveMetadata edit = new EditDescriptiveMetadata(aipId, bundle);
            callback.onSuccess(edit);
          }
        });
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for edit metadata permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_metadata";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditDescriptiveMetadata> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final String aipId;
  private final DescriptiveMetadataEditBundle bundle;

  // private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  TextBox id;

  @UiField
  TextBox type;

  @UiField
  TextArea xml;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  @UiField
  HTML errors;

  /**
   * Create a new panel to edit a user
   * 
   * @param user
   *          the user to edit
   */
  public EditDescriptiveMetadata(String aipId, DescriptiveMetadataEditBundle bundle) {
    this.aipId = aipId;
    this.bundle = bundle;

    initWidget(uiBinder.createAndBindUi(this));

    id.setText(bundle.getId());
    type.setText(bundle.getType());
    xml.setText(bundle.getXml());

    id.setEnabled(false);

  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    String typeText = type.getText();
    String xmlText = xml.getText();

    DescriptiveMetadataEditBundle updatedBundle = new DescriptiveMetadataEditBundle(bundle.getId(), typeText, xmlText);

    BrowserService.Util.getInstance().updateDescriptiveMetadataFile(aipId, updatedBundle, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof MetadataParseException) {
          MetadataParseException e = (MetadataParseException) caught;
          updateErrors(e);
        } else {
          // TODO show error
          MessagePopup.showError(caught.getMessage());
        }
      }

      @Override
      public void onSuccess(Void result) {
        errors.setText("");
        errors.setVisible(false);
        MessagePopup.showInfo("Success", "Saved descriptive metadata file");
        Tools.newHistory(Browse.RESOLVER, aipId);
      }
    });

  }

  protected void updateErrors(MetadataParseException e) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    for (ParseError error : e.getErrors()) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='error'>"));
      b.append(messages.metadataParseError(error.getLineNumber(), error.getColumnNumber(), error.getMessage()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }

    errors.setHTML(b.toSafeHtml());
    errors.setVisible(true);
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().removeDescriptiveMetadataFile(aipId, bundle.getId(), new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        // TODO show error
        MessagePopup.showError(caught.getMessage());
      }

      @Override
      public void onSuccess(Void result) {
        MessagePopup.showInfo("Success", "Removed descriptive metadata file");
        Tools.newHistory(Browse.RESOLVER, aipId);
      }
    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }

}
