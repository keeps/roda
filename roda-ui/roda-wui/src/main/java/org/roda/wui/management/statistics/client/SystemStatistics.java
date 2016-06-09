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
package org.roda.wui.management.statistics.client;

import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;

/**
 * @author Luis Faria
 * 
 */
public class SystemStatistics extends StatisticTab {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private DockPanel layout;
  private HTML link;
  private Frame iframe;

  /**
   * Create a new System Statistics
   */
  public SystemStatistics() {
    super();
    layout = new DockPanel();
    initWidget(layout);
    this.addStyleName("statistics-system");
  }

  protected boolean init() {
    boolean ret = false;
    if (super.init()) {
      link = new HTML("<a href='" + GWT.getModuleBaseURL() + "Munin/index.html' target='_blank'>"
        + messages.systemStatisticsLink() + "</a>");

      iframe = new Frame(GWT.getModuleBaseURL() + "Munin/index.html");

      layout.add(link, DockPanel.NORTH);
      layout.add(iframe, DockPanel.CENTER);

      iframe.setWidth("100%");
      iframe.setHeight("450px");

      layout.setCellHorizontalAlignment(link, HasAlignment.ALIGN_RIGHT);

      link.addStyleName("statistics-system-link");
      iframe.addStyleName("statistics-system-frame");
    }
    return ret;
  }

  @Override
  public String getTabText() {
    return messages.systemStatistics();
  }
}
