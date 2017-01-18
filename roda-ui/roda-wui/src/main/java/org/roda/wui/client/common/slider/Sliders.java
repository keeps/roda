package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;

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

  public static <T extends IsIndexed> SliderPanel createOptionsSlider(FlowPanel container, FocusPanel toggleButton,
    T object) {
    SliderPanel slider = createSlider(container, toggleButton);
    OptionsSliderHelper.updateOptionsObjectSliderPanel(object, slider);
    return slider;
  }

}
