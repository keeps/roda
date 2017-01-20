package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;

public class OptionsSliderHelper {

  private OptionsSliderHelper() {

  }

  static <T extends IsIndexed> void updateOptionsObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedFile) {
      updateOptionsSliderPanel((IndexedFile) object, slider);
    } else if (object instanceof IndexedRepresentation) {
      updateOptionsSliderPanel((IndexedRepresentation) object, slider);
    } else if (object instanceof IndexedAIP) {
      updateOptionsSliderPanel((IndexedAIP) object, slider);
    } else if (object instanceof IndexedDIP) {
      updateOptionsSliderPanel((IndexedDIP) object, slider);
    } else if (object instanceof DIPFile) {
      updateOptionsSliderPanel((DIPFile) object, slider);
    } else {
      // do nothing
    }
  }

  private static void updateOptionsSliderPanel(IndexedAIP aip, SliderPanel slider) {
    slider.clear();
    // TODO slider.addContent(FileActions.get().createActionsLayout(file));
  }

  private static void updateOptionsSliderPanel(IndexedRepresentation representation, SliderPanel slider) {
    slider.clear();
    slider.addContent(RepresentationActions.get().createActionsLayout(representation));
  }

  private static void updateOptionsSliderPanel(final IndexedFile file, final SliderPanel slider) {
    slider.clear();
    slider.addContent(FileActions.get().createActionsLayout(file));
  }

  private static void updateOptionsSliderPanel(final IndexedDIP dip, final SliderPanel slider) {
    slider.clear();
    slider.addContent(DisseminationActions.get().createActionsLayout(dip));
  }

  private static void updateOptionsSliderPanel(final DIPFile file, final SliderPanel slider) {
    slider.clear();
    slider.addContent(DisseminationFileActions.get().createActionsLayout(file));
  }

}
