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
package org.roda.disseminators.common.loading.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Luis Faria
 * 
 */
public class Loading implements EntryPoint {

  private static final int REFRESH_TIMEOUT_MS = 10000;
  private static LoadingConstants loadingConstants = (LoadingConstants) GWT.create(LoadingConstants.class);

  private final HorizontalPanel layout;
  private final Image loadingImage;
  private final VerticalPanel labelLayout;
  private final Label loadingTitle;
  private final Label loadingDescription;
  private final Timer refreshTimer;

  /**
   * Create a new loading panel
   */
  public Loading() {
    layout = new HorizontalPanel();
    loadingImage = new Image("loading.gif");

    labelLayout = new VerticalPanel();
    loadingTitle = new Label(loadingConstants.loadingTitle());
    loadingDescription = new Label(loadingConstants.loadingDescription());

    labelLayout.add(loadingTitle);
    labelLayout.add(loadingDescription);

    layout.add(loadingImage);
    layout.add(labelLayout);

    refreshTimer = new Timer() {

      @Override
      public void run() {
        refresh();
      }

    };
    refreshTimer.scheduleRepeating(REFRESH_TIMEOUT_MS);

    layout.setCellVerticalAlignment(loadingImage, HasAlignment.ALIGN_MIDDLE);

    layout.addStyleName("loading-layout");
    loadingImage.addStyleName("loading-image");
    labelLayout.addStyleName("loading-labels");
    loadingTitle.addStyleName("loading-title");
    loadingDescription.addStyleName("loading-description");
  }

  public void onModuleLoad() {
    RootPanel.get().add(layout);
  }

  protected void refresh() {
    Window.open(History.getToken(), "_self", "");
  }

}
