package org.roda.wui.client.planning.agents.tabs;

import com.google.gwt.core.client.GWT;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class PreservationAgentDetailsPanel extends GenericMetadataCardPanel<IndexedPreservationAgent> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public PreservationAgentDetailsPanel(IndexedPreservationAgent agent) {
    super();
    setData(agent);
  }

  @Override
  public void setData(IndexedPreservationAgent agent) {
    // 1. Clear any existing fields in case setData is called multiple times
    metadataContainer.clear();

    if (agent == null) {
      return;
    }

    addFieldIfNotNull(messages.preservationAgentName(), IndexedPreservationAgent::getName, agent);
    addFieldIfNotNull(messages.preservationAgentId(), IndexedPreservationAgent::getId, agent);
    addFieldIfNotNull(messages.preservationAgentType(), IndexedPreservationAgent::getType, agent);
    addFieldIfNotNull(messages.preservationAgentVersion(), IndexedPreservationAgent::getVersion, agent);
    addFieldIfNotNull(messages.preservationAgentNote(), IndexedPreservationAgent::getNote, agent);
    addFieldIfNotNull(messages.preservationAgentExtension(), IndexedPreservationAgent::getExtension, agent);
  }
}
