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
package org.roda.wui.common.client.widgets;

import java.util.List;
import java.util.Vector;

import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class WUIButton extends AccessibleFocusPanel implements HasText {

  private List<ClickListener> clickListeners;

  private final Grid layout;

  // private final Widget leftDecoration;

  private final Label label;

  // private final Widget rightDecoration;

  private boolean enabled;

  /**
   * The type of the left part of the button
   * 
   */
  public enum Left {
    /**
     * Square button left part
     */
    SQUARE,
    /**
     * Round button left part
     */
    ROUND
  }

  /**
   * The type of the right part of the button
   * 
   */
  public enum Right {
    /**
     * button right part with a cross
     */
    CROSS,
    /**
     * button right part with an arrow pointing forward
     */
    ARROW_FORWARD,
    /**
     * button right part with an arrow pointing backward
     */
    ARROW_BACKWARD,
    /**
     * button right part with an arrow pointing downward
     */
    ARROW_DOWN,
    /**
     * button right part with an arrow pointing upward
     */
    ARROW_UP,
    /**
     * button right part with a report symbol
     */
    REPORT,
    /**
     * button right part with a RI symbol
     */
    RI,
    /**
     * button right part with an information symbol
     */
    INFO,
    /**
     * button right part with a minus symbol
     */
    MINUS,
    /**
     * button right part with a plus symbol
     */
    PLUS,
    /**
     * button right part with a check symbol
     */
    CHECK,
    /**
     * button right part with a record symbol
     */
    REC
  }

  private Left leftType;

  private Right rightType;

  /**
   * Create a new WUI button
   * 
   * @param leftType
   * @param rightType
   */
  public WUIButton(Left leftType, Right rightType) {
    this.layout = new Grid(1, 3);
    this.label = new Label();
    this.leftType = null;
    this.rightType = null;

    setLeftSide(leftType);
    setRightSide(rightType);

    this.setWidget(layout);
    layout.setWidget(0, 0, new HTML("&nbsp;"));
    layout.setWidget(0, 1, label);
    layout.setWidget(0, 2, new HTML("&nbsp;"));

    layout.setBorderWidth(0);
    layout.setCellPadding(0);
    layout.setCellSpacing(0);

    this.setStylePrimaryName("wui-button");
    this.addStyleDependentName("enabled");

    this.addMouseListener(new MouseListener() {

      @Override
      public void onMouseDown(Widget sender, int x, int y) {
        addStyleDependentName("pressed");

      }

      @Override
      public void onMouseEnter(Widget sender) {
        addStyleDependentName("hover");
      }

      @Override
      public void onMouseLeave(Widget sender) {
        removeStyleDependentName("hover");
      }

      @Override
      public void onMouseMove(Widget sender, int x, int y) {
        // do nothing
      }

      @Override
      public void onMouseUp(Widget sender, int x, int y) {
        removeStyleDependentName("pressed");
      }

    });

    this.label.addStyleName("wui-button-text");
    this.layout.addStyleName("wui-button-layout");
    layout.getCellFormatter().addStyleName(0, 0, "button-left");
    layout.getCellFormatter().addStyleName(0, 2, "button-right");
    this.layout.getCellFormatter().addStyleName(0, 1, "wui-button-text-container");
    this.label.setWordWrap(false);
    this.enabled = true;
    this.clickListeners = new Vector<>();
  }

  /**
   * Create a new WUI button
   * 
   * @param text
   * @param leftType
   * @param rightType
   */
  public WUIButton(String text, Left leftType, Right rightType) {
    this(leftType, rightType);
    setText(text);
  }

  @Override
  public void setText(String text) {
    this.label.setText(text.toUpperCase());
  }

  @Override
  public String getText() {
    return this.getText();
  }

  /**
   * Set button left type
   * 
   * @param type
   */
  public void setLeftSide(Left type) {
    if (leftType == null) {
      addLeftSide(type);
      leftType = type;
    } else if (leftType != type) {
      removeLeftSide(leftType);
      addLeftSide(type);
      leftType = type;
    }
  }

  private void addLeftSide(Left type) {
    if (type == Left.SQUARE) {
      layout.getCellFormatter().addStyleName(0, 0, "button-left-square");
    } else if (type == Left.ROUND) {
      layout.getCellFormatter().addStyleName(0, 0, "button-left-round");
    }
  }

  private void removeLeftSide(Left type) {
    if (type == Left.SQUARE) {
      layout.getCellFormatter().removeStyleName(0, 0, "button-left-square");
    } else if (type == Left.ROUND) {
      layout.getCellFormatter().removeStyleName(0, 0, "button-left-round");
    }
  }

  /**
   * Set button right type
   * 
   * @param type
   */
  public void setRightSide(Right type) {
    if (rightType == null) {
      addRightSide(type);
      rightType = type;
    } else if (rightType != type) {
      removeRightSide(rightType);
      addRightSide(type);
      rightType = type;
    }
  }

  private void addRightSide(Right type) {
    if (type == Right.CROSS) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-cross");
    } else if (type == Right.ARROW_UP) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-arrow-up");
    } else if (type == Right.ARROW_DOWN) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-arrow-down");
    } else if (type == Right.ARROW_FORWARD) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-arrow-forward");
    } else if (type == Right.ARROW_BACKWARD) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-arrow-backward");
    } else if (type == Right.REPORT) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-action_report");
    } else if (type == Right.RI) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-ri");
    } else if (type == Right.INFO) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-info");
    } else if (type == Right.MINUS) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-minus");
    } else if (type == Right.PLUS) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-plus");
    } else if (type == Right.CHECK) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-check");
    } else if (type == Right.REC) {
      layout.getCellFormatter().addStyleName(0, 2, "button-right-rec");
    }

  }

  private void removeRightSide(Right type) {
    if (type == Right.CROSS) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-cross");
    } else if (type == Right.ARROW_UP) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-arrow-up");
    } else if (type == Right.ARROW_DOWN) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-arrow-down");
    } else if (type == Right.ARROW_FORWARD) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-arrow-forward");
    } else if (type == Right.ARROW_BACKWARD) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-arrow-backward");
    } else if (type == Right.REPORT) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-action_report");
    } else if (type == Right.RI) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-ri");
    } else if (type == Right.INFO) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-info");
    } else if (type == Right.MINUS) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-minus");
    } else if (type == Right.PLUS) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-plus");
    } else if (type == Right.CHECK) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-check");
    } else if (type == Right.REC) {
      layout.getCellFormatter().removeStyleName(0, 2, "button-right-rec");
    }
  }

  /**
   * Get button left type
   * 
   * @return
   */
  public Left getLeftType() {
    return leftType;
  }

  /**
   * Get button right type
   * 
   * @return
   */
  public Right getRightType() {
    return rightType;
  }

  /**
   * Is button enabled
   * 
   * @return
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Enable/Disable all ClickListeners and add enable/disable dependent style
   * name
   * 
   * @param enabled
   *          Enable/Disable option
   */
  public void setEnabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      if (this.enabled) {
        // add all click listener to super focus panel
        for (ClickListener listener : clickListeners) {
          super.addClickListener(listener);
        }
        // change style dependent name
        this.removeStyleDependentName("disabled");
        this.addStyleDependentName("enabled");
      } else {
        // remove all click listener to super focus panel
        for (ClickListener listener : clickListeners) {
          super.removeClickListener(listener);
        }
        // change style dependent name
        this.removeStyleDependentName("enabled");
        this.addStyleDependentName("disabled");
      }
    }
  }

  @Override
  public void addClickListener(ClickListener listener) {
    clickListeners.add(listener);
    if (this.enabled) {
      super.addClickListener(listener);
    }
  }

  @Override
  public void removeClickListener(ClickListener listener) {
    clickListeners.remove(listener);
    if (this.enabled) {
      super.removeClickListener(listener);
    }
  }

}
