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
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateDescriptiveMetadata extends Composite {

  public static final String AIP = "aip";
  public static final String REPRESENTATION = "representation";

  public static final String NEW = "new";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      GWT.log(historyTokens.toString());
      boolean isAIP = historyTokens.get(0).equals(AIP);

      if ((isAIP && (historyTokens.size() == 2 || historyTokens.size() == 3))
        || (historyTokens.get(0).equals(REPRESENTATION) && (historyTokens.size() == 3 || historyTokens.size() == 4))) {
        final String aipId = historyTokens.get(1);
        boolean isNew;
        CreateDescriptiveMetadata create;

        if (isAIP) {
          isNew = historyTokens.size() == 3 && historyTokens.get(2).equals(NEW);
          create = new CreateDescriptiveMetadata(aipId, null, isNew);
        } else {
          final String representationId = historyTokens.get(2);
          isNew = historyTokens.size() == 4 && historyTokens.get(3).equals(NEW);
          create = new CreateDescriptiveMetadata(aipId, representationId, isNew);
        }

        callback.onSuccess(create);
      } else {
        HistoryUtils.newHistory(BrowseAIP.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for edit metadata permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseAIP.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_metadata";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateDescriptiveMetadata> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String aipId;
  private final String representationId;
  private final boolean isNew;

  private boolean inXML = false;

  private List<SupportedMetadataTypeBundle> metadataTypes = new ArrayList<SupportedMetadataTypeBundle>();
  private SupportedMetadataTypeBundle selectedBundle = null;
  private TextArea metadataXML;
  private String metadataTextFromForm = null;

  @UiField
  TextBox id;

  @UiField
  ListBox type;

  @UiField
  FocusPanel showXml;

  @UiField
  HTML showXmlIconXML;

  @UiField
  HTML showXmlIconForm;

  @UiField
  FlowPanel formOrXML;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField
  HTML errors;

  @UiField
  HTML idError;

  /**
   * Create a new panel to edit a user
   * 
   * @param user
   *          the user to edit
   */
  public CreateDescriptiveMetadata(String aipId, String representationId, boolean isNew) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.isNew = isNew;

    initWidget(uiBinder.createAndBindUi(this));
    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox metadata-edit-area metadata-form-textbox");

    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String value = type.getSelectedValue();

        selectedBundle = null;
        if (StringUtils.isNotBlank(value)) {
          for (SupportedMetadataTypeBundle bundle : metadataTypes) {
            if (bundle.getId().equals(value)) {
              selectedBundle = bundle;
              break;
            }
          }

          id.setText(value + ".xml");
        } else {
          id.setText("");
        }
        updateFormOrXML();
      }
    });

    BrowserService.Util.getInstance().retrieveSupportedMetadata(aipId, representationId,
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<List<SupportedMetadataTypeBundle>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
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

          type.addItem(messages.otherItem(), "");
          type.setSelectedIndex(0);
          selectedBundle = metadataTypes.get(0);
          if (selectedBundle.getVersion() != null) {
            id.setText(selectedBundle.getType() + RodaConstants.METADATA_VERSION_SEPARATOR + selectedBundle.getVersion()
              + ".xml");
          } else {
            id.setText(selectedBundle.getType() + ".xml");
          }
          updateFormOrXML();
        }
      });

    Element firstElement = showXml.getElement().getFirstChildElement();
    if (firstElement.getTagName().equalsIgnoreCase("input")) {
      firstElement.setAttribute("title", "browse input");
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void createForm(SupportedMetadataTypeBundle bundle) {
    formOrXML.clear();
    FormUtilities.create(formOrXML, bundle.getValues(), true);
  }

  public void setInXML(boolean inXML) {
    this.inXML = inXML;
    if (inXML) {
      showXmlIconXML.setVisible(false);
      showXmlIconForm.setVisible(true);
    } else {
      showXmlIconXML.setVisible(true);
      showXmlIconForm.setVisible(false);
    }
  }

  @UiHandler("showXml")
  void buttonShowXmlHandler(ClickEvent e) {
    setInXML(!inXML);
    updateFormOrXML();
  }

  private void updateFormOrXML() {
    if (selectedBundle != null && selectedBundle.getValues() != null) {
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
                  createForm(selectedBundle);
                } else {
                  setInXML(!inXML);
                }
              }
            });
        } else {
          formOrXML.clear();
          createForm(selectedBundle);
        }
      }
    } else {
      formOrXML.clear();
      if (selectedBundle != null)
        metadataXML.setText(selectedBundle.getTemplate());
      else
        metadataXML.setText("");
      formOrXML.add(metadataXML);
      showXml.setVisible(false);
    }
  }

  private void updateMetadataXML() {
    BrowserService.Util.getInstance().retrieveDescriptiveMetadataPreview(selectedBundle, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(String preview) {
        formOrXML.clear();
        metadataXML.setText(preview);
        formOrXML.add(metadataXML);
        metadataTextFromForm = preview;
      }
    });
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    buttonApply.setEnabled(false);
    String idText = id.getText();
    String typeText = selectedBundle != null ? selectedBundle.getType() : messages.otherItem(); // Other
    String typeVersion = selectedBundle != null ? selectedBundle.getVersion() : null;
    String template = selectedBundle != null ? selectedBundle.getTemplate() : null;
    String xmlText = metadataXML.getText();
    boolean hasOverridenTheForm = inXML && !xmlText.equals(metadataTextFromForm);

    if (idText.length() > 0) {
      Set<MetadataValue> values = null;
      // we only send the values map if the user hasn't overriden the form by
      // modifying the XML directly
      if (!hasOverridenTheForm && selectedBundle != null) {
        values = selectedBundle.getValues();
      }
      DescriptiveMetadataEditBundle newBundle = new DescriptiveMetadataEditBundle(idText, typeText, typeVersion,
        xmlText, template, values, true);

      BrowserService.Util.getInstance().createDescriptiveMetadataFile(aipId, representationId, newBundle,
        new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof ValidationException) {
              ValidationException e = (ValidationException) caught;
              updateErrors(e);
              idError.setVisible(false);
            } else if (caught instanceof AlreadyExistsException) {
              idError.setVisible(true);
              idError.setHTML(SafeHtmlUtils.fromSafeConstant(messages.fileAlreadyExists()));
              errors.setVisible(false);
            } else {
              idError.setVisible(false);
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }
            buttonApply.setEnabled(true);
          }

          @Override
          public void onSuccess(Void result) {
            errors.setText("");
            errors.setVisible(false);
            Toast.showInfo(messages.dialogSuccess(), messages.metadataFileCreated());
            if (representationId == null) {
              HistoryUtils.newHistory(BrowseAIP.RESOLVER, aipId);
            } else {
              HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
            }
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
      if (representationId == null) {
        SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<IndexedAIP>(Arrays.asList(aipId),
          IndexedAIP.class.getName());
        BrowserService.Util.getInstance().deleteAIP(selected, null, new AsyncCallback<String>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(String parentId) {
            if (parentId != null) {
              HistoryUtils.newHistory(BrowseAIP.RESOLVER, parentId);
            } else {
              HistoryUtils.newHistory(BrowseAIP.RESOLVER);
            }
          }
        });
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
      }
    } else {
      if (representationId == null) {
        HistoryUtils.newHistory(BrowseAIP.RESOLVER, aipId);
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
      }
    }
  }

}
