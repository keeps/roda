/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {
  interface Binder extends UiBinder<Widget, Footer> {
  }

  public Footer() {
    super();
    Binder uiBinder = GWT.create(Binder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

}
