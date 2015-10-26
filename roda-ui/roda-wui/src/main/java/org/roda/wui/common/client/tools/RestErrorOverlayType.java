/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import com.google.gwt.core.client.JavaScriptObject;

public class RestErrorOverlayType extends JavaScriptObject {
  // Overlay types always have protected, zero-arg ctors
 protected RestErrorOverlayType() { }

  // Typically, methods on overlay types are JSNI
  public final native String getType() /*-{
		return this.type;
  }-*/;

  public final native String getMessage() /*-{
		return this.message;
  }-*/;
}
