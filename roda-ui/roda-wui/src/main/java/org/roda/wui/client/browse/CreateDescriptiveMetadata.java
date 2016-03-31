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
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
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
            if (value.contains(RodaConstants.METADATA_VERSION_SEPARATOR) && bundle.getVersion() != null) {
              String type = value.substring(0, value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
              String version = value.substring(value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1,
                value.length());
              if (bundle.getType().equals(type) && bundle.getVersion().equals(version)) {
                selectedBundle = bundle;
                break;
              }
            } else if (bundle.getType().equals(value)) {
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
            if (b.getVersion() != null) {
              type.addItem(b.getLabel(), b.getType() + RodaConstants.METADATA_VERSION_SEPARATOR + b.getVersion());
            } else {
              type.addItem(b.getLabel(), b.getType());
            }
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
    String typeVersion = null;
    String xmlText = xml.getText();

    if (typeText.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
      typeVersion = typeText.substring(typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1,
        typeText.length());
      typeText = typeText.substring(0, typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
    }

    if (idText.length() > 0) {

      DescriptiveMetadataEditBundle newBundle = new DescriptiveMetadataEditBundle(idText, typeText, typeVersion,
        xmlText);

      BrowserService.Util.getInstance().createDescriptiveMetadataFile(aipId, newBundle, new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof ValidationException) {
            ValidationException e = (ValidationException) caught;
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

  protected void updateErrors(ValidationException e) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    for (ValidationIssue issue : e.getReport().getIssues()) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='error'>"));
      b.append(messages.metadataParseError(issue.getLineNumber(), issue.getColumnNumber(), issue.getMessage()));
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

      SelectedItemsList selected = new SelectedItemsList(Arrays.asList(aipId));
      BrowserService.Util.getInstance().removeAIP(selected, new AsyncCallback<String>() {

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
