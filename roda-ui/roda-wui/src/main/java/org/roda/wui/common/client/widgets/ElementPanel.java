/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets;

import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

/**
 * Element Panel
 * 
 * @author Luis Faria
 * 
 * @param <T>
 *          the element type
 */
public abstract class ElementPanel<T> extends AccessibleFocusPanel {

  private T element;

  private boolean selected;

  /**
   * Create a new element panel
   * 
   * @param element
   */
  public ElementPanel(T element) {
    this.element = element;
  }

  /**
   * Is current panel selected
   * 
   * @return
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Set current panel selected;
   * 
   * @param selected
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
    if (selected) {
      addStyleDependentName("selected");
    } else {
      removeStyleDependentName("selected");
    }
  }

  /**
   * Get element defined by this panel
   * 
   * @return
   */
  public T get() {
    return element;
  }

  /**
   * Set element defined by this panel
   * 
   * @param element
   */
  public void set(T element) {
    if (this.element != element) {
      this.element = element;
      update(element);
    }
  }

  /**
   * Update layout with element
   * 
   * @param element
   */
  protected abstract void update(T element);
}
