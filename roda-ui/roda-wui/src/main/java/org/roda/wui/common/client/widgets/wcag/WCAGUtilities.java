/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.widgets.wcag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Random;

public class WCAGUtilities {

  private static final Set<String> INPUT_TAGNAMES = new HashSet<>(Arrays.asList("INPUT", "SELECT"));

  private static WCAGUtilities instance = null;

  public void makeAccessible(Element element) {

    if (element.getAttribute("align") != null) {
      String className = "";
      if (element.getAttribute("align").equals("right")) {
        className = "alignRight";
      } else if (element.getAttribute("align").equals("left")) {
        className = "alignLeft";
      } else if (element.getAttribute("align").equals("center")) {
        className = "alignCenter";
      } else {
        className = "alignJustify";
      }
      element.removeAttribute("align");
      element.addClassName(className);
    }

    if (INPUT_TAGNAMES.contains(element.getTagName())) {
      addAttributeIfNonExistent(element, "title", "t_" + Random.nextInt(1000), true);
    }

    if (element.getChildCount() > 0) {
      for (int i = 0; i < element.getChildCount(); i++) {
        if (element.getChild(i).getNodeType() == Node.ELEMENT_NODE) {
          makeAccessible((Element) element.getChild(i));
        }
      }
    }

  }

  public static WCAGUtilities getInstance() {
    if (instance == null) {
      instance = new WCAGUtilities();
    }
    return instance;
  }

  public static void addAttributeIfNonExistent(Element element, String attributeName, String attributeValue) {
    addAttributeIfNonExistent(element, attributeName, attributeValue, false);
  }

  public static void addAttributeIfNonExistent(Element element, String attributeName, String attributeValue,
    boolean warn) {
    if (element.getAttribute(attributeName) == null || element.getAttribute(attributeName).equalsIgnoreCase("")) {
      if (warn) {
        // GWT.log("Setting of " + element.getTagName() + " attribute " +
        // attributeName + " to " + attributeValue);
      }
      element.setAttribute(attributeName, attributeValue);
    }
  }
}
