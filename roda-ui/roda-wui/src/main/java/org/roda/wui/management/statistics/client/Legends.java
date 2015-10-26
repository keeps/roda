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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Luis Faria
 * 
 */
public class Legends extends Composite {

  private static final boolean DEFAULT_VERTICAL = true;

  private final boolean vertical;

  private Panel layout;

  /**
   * Create a new legends
   */
  public Legends() {
    this(DEFAULT_VERTICAL);
  }

  /**
   * Create a new legends
   * 
   * @param vertical
   */
  public Legends(boolean vertical) {
    this.vertical = vertical;

    if (vertical) {
      layout = new VerticalPanel();
    } else {
      layout = new FlowPanel();
    }

    initWidget(layout);

    layout.setStylePrimaryName("wui-statistic-legends");

    if (vertical) {
      layout.addStyleDependentName("vertical");
    } else {
      layout.addStyleDependentName("horizontal");
    }

  }

  /**
   * Is orientation vertical
   * 
   * @return true if vertical, false if horizontal
   */
  public boolean isVertical() {
    return vertical;
  }

  /**
   * Add a new legend
   * 
   * @param color
   *          the color style, e.g. #000000
   * @param text
   *          the legend text
   */
  public void addLegend(String color, String text) {
    HTML legend = new HTML("<li class='legend-bullet' style='color: " + color + "'><span class='legend-text'>" + text
      + "</span></li>");
    legend.addStyleName("legend");
    layout.add(legend);
  }

  /**
   * Clear legends list
   */
  public void clear() {
    layout.clear();

  }

}
