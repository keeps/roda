/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.model.BrowseFileResponse;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.services.Services;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;

public class Sliders {

  private Sliders() {

  }

  public static SliderPanel createSlider(FlowPanel container, FocusPanel toggleButton) {
    SliderPanel slider = new SliderPanel();
    container.add(slider);
    container.addStyleName("slider-container");
    slider.setToggleButton(toggleButton);
    return slider;
  }

  public static <T extends IsIndexed> SliderPanel createDisseminationSlider(FlowPanel container,
    FocusPanel toggleButton, T object, Services services) {
    SliderPanel slider = createSlider(container, toggleButton);
    DisseminationsSliderHelper.updateDisseminationsObjectSliderPanel(object, slider, services);
    return slider;
  }

  public static SliderPanel createAipInfoSlider(FlowPanel container, FocusPanel toggleButton,
    BrowseAIPResponse response) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.updateInfoSliderPanel(response, slider);
    return slider;
  }

  public static SliderPanel createFileInfoSlider(FlowPanel container, FocusPanel toggleButton, IndexedFile file,
    BrowseFileResponse response) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.createFileInfoSliderPanel(file, response, slider);
    return slider;
  }

  public static SliderPanel createRepresentationInfoSlider(FlowPanel container, FocusPanel toggleButton,
    BrowseRepresentationResponse response) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.updateInfoSliderPanel(response, slider);
    return slider;
  }

}
