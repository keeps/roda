/**
 * 
 */
package org.roda.wui.ingest.list.client;

import java.util.MissingResourceException;

import org.roda.core.common.IllegalOperationException;
import org.roda.core.data.SIPState;
import org.roda.core.data.SIPStateTransition;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;

/**
 * @author Luis Faria
 * 
 */
public class IngestReportPanel {

  private static IngestListConstants constants = (IngestListConstants) GWT.create(IngestListConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private SIPState sip;

  private final VerticalPanel layout;

  private final HorizontalPanel filenameLayout;

  private final Label filenameLabel;

  private final Label filenameValue;

  private final VerticalPanel transitionsLayout;

  /**
   * Create a new ingest report panel
   * 
   * @param sip
   */
  public IngestReportPanel(SIPState sip) {
    this.sip = sip;
    layout = new VerticalPanel();

    filenameLayout = new HorizontalPanel();
    filenameLabel = new Label(constants.reportFilenameLabel());
    filenameValue = new Label(sip.getOriginalFilename());
    filenameLayout.add(filenameLabel);
    filenameLayout.add(filenameValue);
    layout.add(filenameLayout);

    transitionsLayout = new VerticalPanel();
    layout.add(transitionsLayout);

    SIPStateTransition[] transitions = sip.getStateTransitions();
    for (int i = 0; i < transitions.length; i++) {
      transitionsLayout.add(createTransitionPanel(transitions[i]));
    }

    layout.addStyleName("wui-ingest-report");
    filenameLayout.addStyleName("report-file");
    filenameLabel.addStyleName("report-file-label");
    filenameValue.addStyleName("report-file-value");
    transitionsLayout.addStyleName("report-transitions");
  }

  private Widget createTransitionPanel(SIPStateTransition transition) {

    DisclosurePanel layout;
    try {
      layout = new DisclosurePanel(constants.getString("state_" + transition.getToState()));
      new Label(constants.getString("state_" + transition.getToState()));
    } catch (MissingResourceException e) {
      layout = new DisclosurePanel(transition.getToState());
    }

    Grid content = new Grid(4, 2);
    Label dateLabel = new Label(constants.reportTransitionDate());
    Label taskLabel = new Label(constants.reportTransitionTask());
    Label outcomeLabel = new Label(constants.reportTransitionOutcome());
    Label outcomeDetailsLabel = new Label(constants.reportTransitionOutcomeDetails());

    Label dateValue = new Label(Tools.formatDateTimeMs(transition.getDatetime()));
    Label taskValue;
    try {
      taskValue = new Label(constants.getString("task_" + transition.getTaskID()));
    } catch (MissingResourceException e) {
      taskValue = new Label(transition.getTaskID());
    }
    Label outcomeValue = new Label(transition.isSuccess() ? constants.reportTransitionSuccess()
      : constants.reportTransitionFailure());
    Label outcomeDetailsValue = new Label(transition.getDescription());

    logger.info("outcome details: " + transition.getDescription());

    content.setWidget(0, 0, dateLabel);
    content.setWidget(0, 1, dateValue);
    content.setWidget(1, 0, taskLabel);
    content.setWidget(1, 1, taskValue);
    content.setWidget(2, 0, outcomeLabel);
    content.setWidget(2, 1, outcomeValue);
    content.setWidget(3, 0, outcomeDetailsLabel);
    content.setWidget(3, 1, outcomeDetailsValue);

    layout.setContent(content);

    content.getColumnFormatter().setWidth(1, "100%");
    content.getColumnFormatter().setStyleName(0, "sip-transition-content-labels");
    content.getCellFormatter().setVerticalAlignment(3, 0, HasAlignment.ALIGN_TOP);

    layout.addStyleName("sip-transition");
    content.addStyleName("sip-transition-content");
    dateLabel.addStyleName("sip-transition-content-label");
    taskLabel.addStyleName("sip-transition-content-label");
    outcomeLabel.addStyleName("sip-transition-content-label");
    outcomeDetailsLabel.addStyleName("sip-transition-content-label");
    dateValue.addStyleName("sip-transition-content-value");
    taskValue.addStyleName("sip-transition-content-value");
    outcomeValue.addStyleName("sip-transition-content-value");
    outcomeDetailsValue.addStyleName("sip-transition-content-value");

    return layout;
  }

  public SIPState getSip() {
    return sip;
  }

  public Widget getWidget() {
    return layout;
  }

  public void update(SIPState updatedSIP) {
    try {
      if (sip.hasChanges(updatedSIP)) {
        sip = updatedSIP;
        transitionsLayout.clear();
        SIPStateTransition[] transitions = sip.getStateTransitions();
        for (int i = 0; i < transitions.length; i++) {
          transitionsLayout.add(createTransitionPanel(transitions[i]));
        }
      }
    } catch (IllegalOperationException e) {
      logger.error("Tryed to update ingest report with different SIP id", e);
    }

  }
}
