/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MyResources extends ClientBundle {
  public static final MyResources INSTANCE = GWT.create(MyResources.class);

  interface Style extends CssResource {
    String html();

    String body();

    String h1();
  }

  @Source("main.gss")
  Style css();
}
