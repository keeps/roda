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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

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
  private DescriptiveMetadataEditBundle bundle;
  private SupportedMetadataTypeBundle supportedBundle;
  private boolean inXML = false;
  private TextArea metadataXML;
  private String metadataTextFromForm = null;

  // private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox id;

  @UiField
  ListBox type;

  @UiField
  Label formOrXMLLabel;

  @UiField
  Label formCompleteDanger;

  @UiField
  FocusPanel showXml;

  @UiField
  FlowPanel formOrXML;

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
  public EditDescriptiveMetadata(final String aipId, final DescriptiveMetadataEditBundle bundleParam) {
    this.aipId = aipId;
    this.bundle = bundleParam;

    supportedBundle = new SupportedMetadataTypeBundle(bundle.getType(), bundle.getVersion(), bundle.getId(),
      bundle.getRawTemplate(), bundle.getValues());

    initWidget(uiBinder.createAndBindUi(this));
    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox metadata-edit-area metadata-form-textbox");

    id.setText(bundle.getId());
    id.setEnabled(false);

    type.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        String typeString = null, version = "";
        String value = type.getSelectedValue();
        if (value.contains(RodaConstants.METADATA_VERSION_SEPARATOR) && bundle.getVersion() != null) {
          typeString = value.substring(0, value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
          version = value.substring(value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1, value.length());
        }
        if (typeString == null) {
          typeString = value;
        }

        BrowserService.Util.getInstance().getDescriptiveMetadataEditBundle(aipId, bundle.getId(), typeString, version,
          new AsyncCallback<DescriptiveMetadataEditBundle>() {
            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(DescriptiveMetadataEditBundle editBundle) {
              bundle = editBundle;
              supportedBundle = new SupportedMetadataTypeBundle(bundle.getType(), bundle.getVersion(), bundle.getId(),
                bundle.getRawTemplate(), bundle.getValues());
              updateFormOrXML();
            }
          });
      }
    });

    BrowserService.Util.getInstance().getSupportedMetadata(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<List<SupportedMetadataTypeBundle>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(List<SupportedMetadataTypeBundle> metadataTypes) {
          // TODO sort by alphabetic order of value
          int selected = -1;
          int index = 0;
          for (SupportedMetadataTypeBundle b : metadataTypes) {
            if (b.getVersion() != null) {
              type.addItem(b.getLabel(), b.getType() + RodaConstants.METADATA_VERSION_SEPARATOR + b.getVersion());
            } else {
              type.addItem(b.getLabel(), b.getType());
            }

            String lowerCaseType = bundle.getType() != null ? bundle.getType().toLowerCase() : null;
            if (b.getType().toLowerCase().equals(lowerCaseType)) {
              String lowerCaseVersion = bundle.getVersion() != null ? bundle.getVersion().toLowerCase() : null;
              if (b.getVersion() != null && lowerCaseVersion != null) {
                if (lowerCaseVersion != null && b.getVersion().equals(lowerCaseVersion)) {
                  selected = index;
                }
              } else if (b.getVersion() == null && lowerCaseVersion == null) {
                selected = index;
              }
            }

            index++;
          }

          updateFormOrXML();

          if (selected >= 0) {
            type.addItem(messages.otherItem(), "");
            type.setSelectedIndex(selected);
          } else if ("".equals(bundle.getType())) {
            type.addItem(messages.otherItem(), "");
            type.setSelectedIndex(type.getItemCount() - 1);
          } else {
            type.addItem(messages.otherItem() + "(" + bundle.getType() + ")", bundle.getType());
            type.addItem(messages.otherItem(), "");
            type.setSelectedIndex(type.getItemCount() - 2);
          }
        }
      });
  }

  private void createForm(SupportedMetadataTypeBundle bundle) {
    formOrXML.clear();
    CreateForm.create(formOrXML, bundle);
  }

  public void setInXML(boolean inXML) {
    this.inXML = inXML;
    if (inXML) {
      showXml.removeStyleName("toolbarLink-selected");
      formOrXML.addStyleName("metadata-edit-area-wrapper");
    } else {
      showXml.addStyleName("toolbarLink-selected");
      formOrXML.removeStyleName("metadata-edit-area-wrapper");
    }
  }

  @UiHandler("showXml")
  void buttonShowXmlHandler(ClickEvent e) {
    setInXML(!inXML);
    updateFormOrXML();
  }

  private void updateFormOrXML() {
//    formCompleteDanger.setVisible(!bundle.isComplete());
    formCompleteDanger.setVisible(false);
    if (bundle != null && bundle.getValues() != null) {
      showXml.setVisible(true);
      if (inXML) {
        updateMetadataXML();
      } else {
        // if the user changed the metadata text
        if (metadataTextFromForm != null && !metadataXML.getText().equals(metadataTextFromForm)) {
          Dialogs.showConfirmDialog(messages.confirmChangeToFormTitle(), messages.confirmChangeToFormMessage(),
            messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {
              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                  formOrXML.clear();
                  createForm(supportedBundle);
                  formOrXMLLabel.setText("Form");
                } else {
                  setInXML(!inXML);
                }
              }
            });
        } else {
          formOrXML.clear();
          createForm(supportedBundle);
          formOrXMLLabel.setText("Form");
        }
      }
    } else {
      formOrXML.clear();
      if (bundle != null) {
        metadataXML.setText(bundle.getRawTemplate());
      } else {
        metadataXML.setText("");
      }
      formOrXML.add(metadataXML);
      formOrXMLLabel.setText("Template preview");
      showXml.setVisible(false);
    }
  }

  private void updateMetadataXML() {
    BrowserService.Util.getInstance().getDescriptiveMetadataPreview(aipId, supportedBundle,
      new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(String preview) {
          formOrXML.clear();
          metadataXML.setText(preview);
          formOrXML.add(metadataXML);
          formOrXMLLabel.setText("Template preview");
          metadataTextFromForm = preview;
        }
      });
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    String xmlText = metadataXML.getText();
    boolean hasOverridenTheForm = inXML && !xmlText.equals(metadataTextFromForm);
    if (hasOverridenTheForm) {
      updateMetadataOnServer(xmlText);
    } else {
      // Get the resulting XML using the data from the form
      BrowserService.Util.getInstance().getDescriptiveMetadataPreview(aipId, supportedBundle,
        new AsyncCallback<String>() {
          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(String preview) {
            updateMetadataOnServer(preview);
          }
        });
    }
  }

  private void updateMetadataOnServer(String content) {
    String typeText = type.getSelectedValue();
    String version = null;

    if (typeText.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
      version = typeText.substring(typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1,
        typeText.length());
      typeText = typeText.substring(0, typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
    }

    DescriptiveMetadataEditBundle updatedBundle = new DescriptiveMetadataEditBundle(bundle.getId(), typeText, version,
      content);

    BrowserService.Util.getInstance().updateDescriptiveMetadataFile(aipId, updatedBundle, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof ValidationException) {
          ValidationException e = (ValidationException) caught;
          updateErrors(e);
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }
      }

      @Override
      public void onSuccess(Void result) {
        errors.setText("");
        errors.setVisible(false);
        Toast.showInfo(messages.dialogSuccess(), messages.metadataFileSaved());
        Tools.newHistory(Browse.RESOLVER, aipId);
      }
    });

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

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().removeDescriptiveMetadataFile(aipId, bundle.getId(), new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogSuccess(), messages.metadataFileRemoved());
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
