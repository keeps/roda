/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */

package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.IncrementalAssociativeList;
import org.roda.wui.client.common.IncrementalList;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.BrowseMessages;
import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 *
 */
public class AgentDataPanel extends Composite implements HasValueChangeHandlers<Agent> {

  interface MyUiBinder extends UiBinder<Widget, AgentDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  TextBox name;

  @UiField
  TextBox type;

  @UiField
  TextArea description;

  @UiField
  TextBox category;

  @UiField
  TextBox version;

  @UiField
  TextBox license;

  @UiField
  IntegerBox popularity;

  @UiField
  DateBox initialRelease;

  @UiField
  TextBox website;

  @UiField
  TextBox download;

  @UiField
  TextArea provenanceInformation;

  @UiField
  IncrementalList platforms;

  @UiField
  IncrementalList extensions;

  @UiField
  IncrementalList mimetypes;

  @UiField
  IncrementalList pronoms;

  @UiField
  IncrementalList utis;

  @UiField(provided = true)
  IncrementalAssociativeList formatIds;

  @UiField(provided = true)
  IncrementalAssociativeList requiredAgents;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean editmode;

  private boolean changed = false;
  private boolean checked = false;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   */
  public AgentDataPanel(boolean editmode, Agent agent) {
    this(true, editmode, agent);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editmode
   */
  public AgentDataPanel(boolean visible, boolean editmode, Agent agent) {

    formatIds = new IncrementalAssociativeList(Format.class, RodaConstants.FORMAT_ID, RodaConstants.FORMAT_SEARCH,
      messages.getFormatsDialogName());

    requiredAgents = new IncrementalAssociativeList(Agent.class, RodaConstants.AGENT_ID, RodaConstants.AGENT_SEARCH,
      messages.getAgentsDialogName());

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    initialRelease.setFormat(dateFormat);
    initialRelease.getDatePicker().setYearArrowsVisible(true);
    initialRelease.setFireNullValues(true);
    initialRelease.setValue(new Date());

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        AgentDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    name.addChangeHandler(changeHandler);
    name.addKeyUpHandler(keyUpHandler);
    type.addChangeHandler(changeHandler);
    type.addKeyUpHandler(keyUpHandler);
    description.addChangeHandler(changeHandler);
    description.addKeyUpHandler(keyUpHandler);
    category.addChangeHandler(changeHandler);
    category.addKeyUpHandler(keyUpHandler);

    version.addChangeHandler(changeHandler);
    license.addChangeHandler(changeHandler);
    popularity.addChangeHandler(changeHandler);

    initialRelease.addValueChangeHandler(new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        onChange();
      }
    });

    website.addChangeHandler(changeHandler);
    download.addChangeHandler(changeHandler);
    provenanceInformation.addChangeHandler(changeHandler);

    platforms.addChangeHandler(changeHandler);
    extensions.addChangeHandler(changeHandler);
    mimetypes.addChangeHandler(changeHandler);
    pronoms.addChangeHandler(changeHandler);
    utis.addChangeHandler(changeHandler);

    formatIds.addChangeHandler(changeHandler);
    requiredAgents.addChangeHandler(changeHandler);

    if (editmode) {
      setAgent(agent);
    }
  }

  public boolean isValid() {
    boolean valid = true;

    if (name.getText().length() == 0) {
      valid = false;
      name.addStyleName("isWrong");
    } else {
      name.removeStyleName("isWrong");
    }

    if (initialRelease.getValue() == null || initialRelease.getValue().after(new Date())) {
      valid = false;
      initialRelease.addStyleName("isWrong");
    } else {
      initialRelease.removeStyleName("isWrong");
    }

    if (category.getText().length() == 0) {
      valid = false;
      category.addStyleName("isWrong");
    } else {
      category.removeStyleName("isWrong");
    }

    try {
      Integer.parseInt(popularity.getText());
      popularity.removeStyleName("isWrong");
    } catch (NumberFormatException e) {
      valid = false;
      popularity.addStyleName("isWrong");
    }

    checked = true;
    return valid;
  }

  public void setAgent(Agent agent) {
    this.name.setText(agent.getName());
    this.type.setText(agent.getType());
    this.description.setText(agent.getDescription());
    this.category.setText(agent.getCategory());
    this.version.setText(agent.getVersion());
    this.license.setText(agent.getLicense());
    this.initialRelease.setValue(agent.getInitialRelease());
    this.popularity.setValue(agent.getPopularity());
    this.website.setText(agent.getWebsite());
    this.download.setText(agent.getDownload());
    this.provenanceInformation.setText(agent.getProvenanceInformation());

    this.platforms.setTextBoxList(agent.getPlatforms());
    this.extensions.setTextBoxList(agent.getExtensions());
    this.mimetypes.setTextBoxList(agent.getMimetypes());
    this.pronoms.setTextBoxList(agent.getPronoms());
    this.utis.setTextBoxList(agent.getUtis());

    final Map<String, String> formatInfo = new HashMap<String, String>();
    final String agentId = agent.getId();

    BrowserService.Util.getInstance().retrieveFormats(agentId, new AsyncCallback<List<Format>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<Format> result) {
        for (Format f : result) {
          formatInfo.put(f.getId(), f.getName());
        }
        AgentDataPanel.this.formatIds.setTextBoxList(formatInfo);
      }
    });

    if (agent.getId() != null) {
      requiredAgents.setExcludedIds(Arrays.asList(agent.getId()));
    }

    final Map<String, String> agentInfo = new HashMap<String, String>();

    BrowserService.Util.getInstance().retrieveRequiredAgents(agentId, new AsyncCallback<List<Agent>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<Agent> result) {
        for (Agent a : result) {
          agentInfo.put(a.getId(), a.getName());
        }
        AgentDataPanel.this.requiredAgents.setTextBoxList(agentInfo);
      }
    });

  }

  public Agent getAgent() {

    Agent agent = new Agent();
    agent.setName(name.getText());
    agent.setType(type.getText());
    agent.setDescription(description.getText());
    agent.setCategory(category.getText());
    agent.setVersion(version.getText());
    agent.setLicense(license.getText());
    agent.setInitialRelease(initialRelease.getValue());
    agent.setPopularity(popularity.getValue());
    agent.setWebsite(website.getText());
    agent.setDownload(download.getText());
    agent.setProvenanceInformation(provenanceInformation.getText());

    agent.setPlatforms(platforms.getTextBoxesValue());
    agent.setExtensions(extensions.getTextBoxesValue());
    agent.setMimetypes(mimetypes.getTextBoxesValue());
    agent.setPronoms(pronoms.getTextBoxesValue());
    agent.setUtis(utis.getTextBoxesValue());
    agent.setFormatIds(formatIds.getTextBoxesValue());
    agent.setAgentsRequired(requiredAgents.getTextBoxesValue());

    return agent;
  }

  public void clear() {
    name.setText("");
    type.setText("");
    description.setText("");
    category.setText("");
    version.setText("");
    license.setText("");
    popularity.setValue(null);
    website.setText("");
    download.setText("");
    provenanceInformation.setText("");

    platforms.clearTextBoxes();
    extensions.clearTextBoxes();
    mimetypes.clearTextBoxes();
    pronoms.clearTextBoxes();
    utis.clearTextBoxes();
    formatIds.clearTextBoxes();
    requiredAgents.clearTextBoxes();
  }

  /**
   * Is user data panel editable, i.e. on create user mode
   *
   * @return true if editable
   */
  public boolean isEditmode() {
    return editmode;
  }

  /**
   * Is user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Agent> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Agent getValue() {
    return getAgent();
  }
}
