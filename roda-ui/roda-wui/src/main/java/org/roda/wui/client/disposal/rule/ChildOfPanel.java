package org.roda.wui.client.disposal.rule;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.wui.client.ingest.process.PluginParameterPanel;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ChildOfPanel extends Composite implements HasValueChangeHandlers<Pair<String, String>> {

  interface MyUiBinder extends UiBinder<Widget, ChildOfPanel> {
  }

  private static ChildOfPanel.MyUiBinder uiBinder = GWT.create(ChildOfPanel.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label pluginParameterPanelError;

  @UiField(provided = true)
  PluginParameterPanel pluginParameterPanel;

  @UiField
  Label editPanelLabel;

  @UiField
  HorizontalPanel editPanel;

  private final boolean editMode;

  private String aipId;
  private String aipName;

  private boolean changed = false;
  private boolean checked = false;

  public ChildOfPanel(String aipId, String aipName, boolean editMode, DisposalRule disposalRule) {
    initPluginParameterPanel();
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;
    this.aipId = aipId;
    this.aipName = aipName;

    if (editMode && disposalRule.getType().equals(ConditionType.IS_CHILD_OF)) {
      editPanelLabel.setText(messages.conditionActualParent());
      editPanel.setVisible(true);
      pluginParameterPanel.setVisible(false);
      setEditPanel();

    } else {
      editPanelLabel.setVisible(false);
      editPanel.setVisible(false);
      pluginParameterPanel.setVisible(true);
    }
  }

  private void setEditPanel() {

    Label aipLabel = new Label();

    Anchor anchor = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));
    anchor.addStyleName("toolbarLink toolbarLinkSmall");
    anchor.addClickHandler(clickEvent -> {
      ChildOfPanel.this.onChange();
      editPanelLabel.setVisible(false);
      editPanel.setVisible(false);
      editPanel.clear();
      pluginParameterPanel.setVisible(true);
    });

    String parent = aipName + " (" + aipId + ")";
    aipLabel.setText(parent);
    aipLabel.addStyleName("itemText value");

    editPanel.add(aipLabel);
    editPanel.setCellWidth(aipLabel, "100%");

    editPanel.add(anchor);
  }

  private void initPluginParameterPanel() {
    pluginParameterPanel = new PluginParameterPanel(new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      messages.selectParentTitle(), PluginParameter.PluginParameterType.AIP_ID, "", false, false,
      "Use the provided parent node if the SIPs does not provide one."));
    pluginParameterPanel.getLayout().removeStyleName("plugin-options-parameter");
  }

  public boolean isValid() {
    List<String> errorList = new ArrayList<>();

    if (!editMode) {
      String aipId = pluginParameterPanel.getValue();
      String aipName = pluginParameterPanel.getAipTitle();
      if (StringUtils.isBlank(aipId) || StringUtils.isBlank(aipName)) {
        pluginParameterPanel.addStyleName("isWrong");
        pluginParameterPanelError.setText(messages.mandatoryField());
        pluginParameterPanelError.setVisible(true);
        Window.scrollTo(pluginParameterPanel.getAbsoluteLeft(), pluginParameterPanel.getAbsoluteTop());
        errorList.add(messages.isAMandatoryField("TO DO"));

      } else {
        pluginParameterPanel.removeStyleName("isWrong");
        pluginParameterPanelError.setVisible(false);
      }
    }
    checked = true;
    return errorList.isEmpty();
  }

  public boolean isEditMode() {
    return editMode;
  }

  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Pair<String, String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Pair<String, String> getValue() {
    return getChildOfFields();
  }

  private Pair<String, String> getChildOfFields() {
    Pair<String, String> childOfFields = new Pair<>();
    childOfFields.setFirst(pluginParameterPanel.getValue());
    childOfFields.setSecond(pluginParameterPanel.getAipTitle());
    return childOfFields;
  }

}
