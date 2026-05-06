package org.roda.wui.client.planning.agents.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PreservationAgentDetailsPanel extends GenericMetadataCardPanel<IndexedPreservationAgent> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public PreservationAgentDetailsPanel(IndexedPreservationAgent agent) {
    setData(agent);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedPreservationAgent agent) {
    return null;
  }

  @Override
  protected void buildFields(IndexedPreservationAgent agent) {
    buildField(messages.preservationAgentName()).withValue(agent.getName()).build();

    buildField(messages.preservationAgentId()).withValue(agent.getId()).build();

    buildField(messages.preservationAgentType()).withValue(agent.getType()).build();

    buildField(messages.preservationAgentVersion()).withValue(agent.getVersion()).build();

    buildField(messages.preservationAgentNote()).withValue(agent.getNote()).build();

    buildField(messages.preservationAgentExtension()).withValue(agent.getExtension()).build();
  }
}