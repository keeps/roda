/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.agents.Agent;
import org.roda.wui.client.common.lists.AgentList;

public class SelectAgentDialog extends DefaultSelectDialog<Agent, Void> {

  private static final Filter DEFAULT_FILTER_AGENT = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AGENT_SEARCH, "*"));

  public SelectAgentDialog(String title) {
    this(title, DEFAULT_FILTER_AGENT);
  }

  public SelectAgentDialog(String title, Filter filter) {
    this(title, filter, false);
  }

  public SelectAgentDialog(String title, Filter filter, boolean selectable) {
    super(title, filter, RodaConstants.AGENT_SEARCH, new AgentList(filter, null, title, selectable), false);
  }

}
