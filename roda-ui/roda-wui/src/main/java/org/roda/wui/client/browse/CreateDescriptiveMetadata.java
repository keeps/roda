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
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
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
import com.google.gwt.user.datepicker.client.DateBox;

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

  private boolean inXML = false;

  private List<SupportedMetadataTypeBundle> metadataTypes = new ArrayList<SupportedMetadataTypeBundle>();
  private SupportedMetadataTypeBundle selectedBundle = null;
  private TextArea metadataXML;
  private String metadataTextFromForm = null;

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  TextBox id;

  @UiField
  ListBox type;

  @UiField
  Label formOrXMLLabel;

  @UiField
  FocusPanel showXml;

  @UiField
  FlowPanel formOrXML;

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
    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox metadata-edit-area metadata-form-textbox");

    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String value = type.getSelectedValue();

        selectedBundle = null;
        if (value != null && value.length() > 0) {
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

          id.setText(value + ".xml");
        } else if (value != null) {
          id.setText("");
        }
        updateFormOrXML();
      }
    });

    BrowserService.Util.getInstance().getSupportedMetadata(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
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

          updateFormOrXML();
        }
      });

  }

  private void createForm(SupportedMetadataTypeBundle bundle) {
    formOrXML.clear();
    for (MetadataValue mv : bundle.getValues()) {
      if (mv.get("hidden") != null && mv.get("hidden").equals("true"))
        continue;

      FlowPanel layout = new FlowPanel();
      layout.addStyleName("plugin-options-parameter");
      String controlType = mv.get("type");
      if (controlType == null) {
        addTextField(layout, mv);
      } else {
        switch (controlType) {
          case "text":
            addTextField(layout, mv);
            break;
          case "textarea":
          case "big-text":
          case "text-area":
            addTextArea(layout, mv);
            break;
          case "list":
            addList(layout, mv);
            break;
          case "date":
            addDatePicker(layout, mv);
            break;
          default:
            addTextField(layout, mv);
            break;
        }
      }
    }
  }

  private String getFieldLabel(MetadataValue mv) {
    String result = mv.getId();
    String rawLabel = mv.get("label");
    if (rawLabel != null && rawLabel.length() > 0) {
      String loc = LocaleInfo.getCurrentLocale().getLocaleName();
      try {
        JSONObject jsonObject = JSONParser.parseLenient(rawLabel).isObject();
        JSONString jsonString = jsonObject.get(loc).isString();

        if (jsonString != null) {
          result = jsonString.stringValue();
        }
      } catch (JSONException e) {
        // do nothing, the JSON was malformed or the label for the desired
        // language doesn't exist
      }
    }
    return result;
  }

  private void addTextField(final FlowPanel layout, final MetadataValue mv) {
    // Top label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final TextBox mvText = new TextBox();
    mvText.addStyleName("form-textbox");
    if (mv.get("value") != null) {
      mvText.setText(mv.get("value"));
    }
    mvText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvText.getValue());
      }
    });

    layout.add(mvLabel);
    layout.add(mvText);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    formOrXML.add(layout);
  }

  private void addTextArea(final FlowPanel layout, final MetadataValue mv) {
    // Top label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final TextArea mvText = new TextArea();
    mvText.addStyleName("form-textbox metadata-form-text-area");
    if (mv.get("value") != null) {
      mvText.setText(mv.get("value"));
    }
    mvText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvText.getValue());
      }
    });

    layout.add(mvLabel);
    layout.add(mvText);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    formOrXML.add(layout);
  }

  private void addList(final FlowPanel layout, final MetadataValue mv) {
    // Top Label
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final ListBox mvList = new ListBox();
    mvList.addStyleName("form-textbox");

    String list = mv.get("list");
    if (list != null) {
      JSONArray jsonArray = JSONParser.parseLenient(list).isArray();
      for (int i = 0; i < jsonArray.size(); i++) {
        String value = jsonArray.get(i).isString().stringValue();
        mvList.addItem(value);

        if (value.equals(mv.get("value"))) {
          mvList.setSelectedIndex(i);
        }
      }
    }

    mvList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        mv.set("value", mvList.getSelectedValue());
      }
    });

    if (mv.get("value") == null || mv.get("value").isEmpty()) {
      mvList.setSelectedIndex(0);
      mv.set("value", mvList.getSelectedValue());
    }

    layout.add(mvLabel);
    layout.add(mvList);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    formOrXML.add(layout);
  }

  private void addDatePicker(final FlowPanel layout, final MetadataValue mv) {
    // Top label
    final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
    Label mvLabel = new Label(getFieldLabel(mv));
    mvLabel.addStyleName("form-label");

    // Field
    final DateBox mvDate = new DateBox();
    mvDate.addStyleName("form-textbox");
    mvDate.setFormat(new DateBox.DefaultFormat() {
      @Override
      public String format(DateBox dateBox, Date date) {
        if (date == null)
          return null;
        return dateTimeFormat.format(date);
      }
    });
    if (mv.get("value") != null) {
      Date date = dateTimeFormat.parse(mv.get("value"));
      mvDate.setValue(date);
    }
    mvDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
      @Override
      public void onValueChange(ValueChangeEvent<Date> valueChangeEvent) {
        String newValue = dateTimeFormat.format(mvDate.getValue());
        mv.set("value", newValue);
      }
    });

    layout.add(mvLabel);
    layout.add(mvDate);

    // Description
    String description = mv.get("description");
    if (description != null && description.length() > 0) {
      Label mvDescription = new Label(description);
      mvDescription.addStyleName("form-help");
      layout.add(mvDescription);
    }

    formOrXML.add(layout);
  }

  public void setInXML(boolean inHTML) {
    this.inXML = inHTML;
    if (inHTML) {
      showXml.removeStyleName("toolbarLink-selected");
    } else {
      showXml.addStyleName("toolbarLink-selected");
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
              public void onFailure(Throwable throwable) {
                Toast.showError(throwable.getClass().getName(), throwable.getMessage());
              }

              @Override
              public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                  formOrXML.clear();
                  createForm(selectedBundle);
                  formOrXMLLabel.setText("Form");
                } else {
                  setInXML(!inXML);
                }
              }
            });
        } else {
          formOrXML.clear();
          createForm(selectedBundle);
          formOrXMLLabel.setText("Form");
        }
      }
    } else {
      formOrXML.clear();
      if (selectedBundle != null)
        metadataXML.setText(selectedBundle.getTemplate());
      else
        metadataXML.setText("");
      formOrXML.add(metadataXML);
      formOrXMLLabel.setText("Template preview");
      showXml.setVisible(false);
    }
  }

  private void updateMetadataXML() {
    BrowserService.Util.getInstance().getDescriptiveMetadataPreview(aipId, selectedBundle, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        Toast.showError(caught.getClass().getName(), caught.getMessage());
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
    buttonApply.setEnabled(false);
    String idText = id.getText();
    String typeText = selectedBundle.getType();
    String typeVersion = selectedBundle.getVersion();
    String xmlText = metadataXML.getText();
    boolean hasOverridenTheForm = inXML && !xmlText.equals(metadataTextFromForm);

    if (idText.length() > 0) {
      TreeSet<MetadataValue> values = null;
      // we only send the values map if the user hasn't overriden the form by
      // modifying the XML directly
      if (!hasOverridenTheForm) {
        values = selectedBundle.getValues();
      }
      DescriptiveMetadataEditBundle newBundle = new DescriptiveMetadataEditBundle(idText, typeText, typeVersion,
        xmlText, selectedBundle.getTemplate(), values);

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

      SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<IndexedAIP>(Arrays.asList(aipId),
        IndexedAIP.class.getName());
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
