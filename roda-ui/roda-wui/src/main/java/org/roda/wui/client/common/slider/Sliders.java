/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;

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

  public static <T extends IsIndexed> SliderPanel createDisseminationsSlider(FlowPanel container,
    FocusPanel toggleButton, T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    DisseminationsSliderHelper.updateDisseminationsObjectSliderPanel(object, slider);
    return slider;
  }

  public static <T extends IsIndexed> SliderPanel createInfoSlider(FlowPanel container, FocusPanel toggleButton,
    T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.updateInfoObjectSliderPanel(object, slider);
    return slider;
  }

  public static SliderPanel createFileInfoSlider(FlowPanel container, FocusPanel toggleButton,
    BrowseFileBundle bundle) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.updateInfoSliderPanel(bundle, slider);
    return slider;
  }

  public static SliderPanel createAipInfoSlider(FlowPanel container, FocusPanel toggleButton, BrowseAIPBundle bundle) {
    SliderPanel slider = createSlider(container, toggleButton);
    InfoSliderHelper.updateInfoSliderPanel(bundle, slider);
    return slider;
  }

  public static <T extends IsIndexed> SliderPanel createOptionsSlider(FlowPanel container, FocusPanel toggleButton,
    T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    OptionsSliderHelper.updateOptionsObjectSliderPanel(object, slider);
    return slider;
  }

}
