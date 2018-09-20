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
