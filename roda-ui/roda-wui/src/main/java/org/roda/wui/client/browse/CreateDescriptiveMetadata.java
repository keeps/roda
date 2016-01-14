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

import java.util.ArrayList;
import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateDescriptiveMetadata extends Composite {

  public static final String NEW = "new";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1 || historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        boolean isNew = historyTokens.size() == 2 && historyTokens.get(1).equals(NEW);

        CreateDescriptiveMetadata create = new CreateDescriptiveMetadata(aipId, isNew);
        callback.onSuccess(create);

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
      return "create_metadata";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateDescriptiveMetadata> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private final String aipId;

  private final boolean isNew;

  private List<SupportedMetadataTypeBundle> metadataTypes = new ArrayList<SupportedMetadataTypeBundle>();

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  TextBox id;

  @UiField
  ListBox type;

  @UiField
  TextArea xml;

  @UiField
  Button buttonApply;

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
  public CreateDescriptiveMetadata(String aipId, boolean isNew) {
    this.aipId = aipId;
    this.isNew = isNew;

    initWidget(uiBinder.createAndBindUi(this));

    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String value = type.getSelectedValue();

        if (value != null && value.length() > 0) {
          SupportedMetadataTypeBundle selectedBundle = null;
          for (SupportedMetadataTypeBundle bundle : metadataTypes) {
            if (bundle.getType().equals(value)) {
              selectedBundle = bundle;
              break;
            }
          }

          if (selectedBundle != null) {
            // TODO only set text if it was not yet edited
            xml.setText(selectedBundle.getTemplate() != null ? selectedBundle.getTemplate() : "");
          }

          id.setText(value + ".xml");

        } else if (value != null) {
          id.setText("");
        }
      }
    });

    BrowserService.Util.getInstance().getSupportedMetadata(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SupportedMetadataTypeBundle>>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(caught.getClass().getName(), caught.getMessage());
        }

        @Override
        public void onSuccess(List<SupportedMetadataTypeBundle> metadataTypes) {

          CreateDescriptiveMetadata.this.metadataTypes = metadataTypes;

          for (SupportedMetadataTypeBundle b : metadataTypes) {
            type.addItem(b.getLabel(), b.getType());
          }

          type.addItem("Other", "");

          type.setSelectedIndex(type.getItemCount() - 1);

        }
      });

  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    buttonApply.setEnabled(false);
    String idText = id.getText();
    String typeText = type.getSelectedValue();
    String xmlText = xml.getText();

    if (idText.length() > 0) {

      DescriptiveMetadataEditBundle newBundle = new DescriptiveMetadataEditBundle(idText, typeText, xmlText);

      BrowserService.Util.getInstance().createDescriptiveMetadataFile(aipId, newBundle, new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof MetadataParseException) {
            MetadataParseException e = (MetadataParseException) caught;
            updateErrors(e);
          } else {
            // TODO show error
            Toast.showError(caught.getMessage());
          }
          buttonApply.setEnabled(true);
        }

        @Override
        public void onSuccess(Void result) {
          errors.setText("");
          errors.setVisible(false);
          Toast.showInfo("Success", "Created descriptive metadata file");
          Tools.newHistory(Browse.RESOLVER, aipId);
        }
      });
    } else {
      Toast.showError("Please fill the mandatory fields");
      buttonApply.setEnabled(true);
    }

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

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    if (isNew) {

      BrowserService.Util.getInstance().removeAIP(aipId, new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(caught.getClass().getName(), caught.getMessage());
        }

        @Override
        public void onSuccess(String parentId) {
          if (parentId != null) {
            Tools.newHistory(Browse.RESOLVER, parentId);
          } else {
            Tools.newHistory(Browse.RESOLVER);
          }
        }
      });

    } else {
      Tools.newHistory(Browse.RESOLVER, aipId);
    }
  }

}
