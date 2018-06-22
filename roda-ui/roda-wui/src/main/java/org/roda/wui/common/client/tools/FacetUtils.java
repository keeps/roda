/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class FacetUtils {
  private static final ClientLogger LOGGER = new ClientLogger(FacetUtils.class.getName());

  private FacetUtils() {
    // do nothing
  }

  public static <T extends IsIndexed> void bindFacets(final AsyncTableCell<T, ?> list,
    final FlowPanel facetsPanel) {
    bindFacets(list, facetsPanel, false);
  }

  public static <T extends IsIndexed> void bindFacets(final AsyncTableCell<T, ?> list,
    final FlowPanel facetsPanel, final boolean hideDisabled) {
    Map<String, FlowPanel> facetPanels = createFacetPanels(list, facetsPanel);
    list.addValueChangeHandler(new ValueChangeHandler<IndexResult<T>>() {
      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<T>> event) {
        FacetUtils.updateFacetPanels(list, facetPanels, event.getValue().getFacetResults(), hideDisabled);
      }
    });
  }

  private static <T extends IsIndexed> Map<String, FlowPanel> createFacetPanels(final AsyncTableCell<T, ?> list,
    final FlowPanel parentPanel) {
    Facets facets = list.getFacets();

    Map<String, FlowPanel> result = new HashMap<>();
    for (FacetParameter facetParameter : facets.getParameters().values()) {

      FlowPanel facetAndTitle = new FlowPanel();
      facetAndTitle.addStyleName("sidebar-facet-panel");

      String title = ConfigurationManager.getTranslation(RodaConstants.I18N_UI_FACETS_PREFIX,
        list.getClassToReturn().getSimpleName(), facetParameter.getName());

      Label titleLabel = new Label(title);
      titleLabel.addStyleName("h4");

      FlowPanel facetPanel = new FlowPanel();

      facetAndTitle.add(titleLabel);
      facetAndTitle.add(facetPanel);
      parentPanel.add(facetAndTitle);

      result.put(facetParameter.getName(), facetPanel);
    }

    return result;
  }

  private static <T extends IsIndexed> void updateFacetPanels(final AsyncTableCell<T, ?> list,
    final Map<String, FlowPanel> facetPanels, final List<FacetFieldResult> facetResults, final boolean hideDisabled) {

    for (FacetFieldResult facetResult : facetResults) {
      final String facetField = facetResult.getField();
      FlowPanel facetPanel = facetPanels.get(facetField);
      if (facetPanel != null) {
        facetPanel.clear();
        if (facetResult.getTotalCount() == 0) {
          facetPanel.getParent().addStyleName("facet-empty");
        } else {
          facetPanel.getParent().removeStyleName("facet-empty");
        }

        for (FacetValue facetValue : facetResult.getValues()) {
          final String value = facetValue.getValue();
          final String label = facetValue.getLabel();
          long count = facetValue.getCount();
          boolean selected = facetResult.getSelectedValues().contains(value);
          StringBuilder checkboxLabel = new StringBuilder();
          checkboxLabel.append(label);
          if (count > 0 || facetResult.getSelectedValues().isEmpty() || selected) {
            checkboxLabel.append(" (").append(count).append(")");
          }

          CheckBox facetValuePanel = new CheckBox(checkboxLabel.toString());
          facetValuePanel.setTitle(checkboxLabel.toString());
          facetValuePanel.addStyleName("sidebar-facet-label");
          facetValuePanel.addStyleName("fade-out");

          boolean enabled = count > 0 || !facetResult.getSelectedValues().isEmpty();
          facetValuePanel.setEnabled(enabled);
          if (hideDisabled) {
            facetValuePanel.setVisible(enabled);
          }

          facetPanel.add(facetValuePanel);
          facetValuePanel.setValue(selected);

          facetValuePanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
              Facets facets = list.getFacets();
              FacetParameter selectedFacetParameter = facets.getParameters().get(facetField);

              if (selectedFacetParameter != null) {
                if (event.getValue()) {
                  selectedFacetParameter.getValues().add(value);
                } else {
                  selectedFacetParameter.getValues().remove(value);
                }
              } else {
                LOGGER.warn("Haven't found the facet parameter: " + facetField);
              }

              list.setFacets(facets);
            }
          });
        }

      } else {
        LOGGER.warn("Got a facet but haven't got a panel for it: " + facetField);
      }
    }
  }

  public static boolean hasSelected(Facets facets) {
    boolean hasSelected = false;
    if (facets != null) {
      for (FacetParameter fp : facets.getParameters().values()) {
        if (!fp.getValues().isEmpty()) {
          hasSelected = true;
          break;
        }
      }
    }
    return hasSelected;
  }

  public static void clearFacets(Facets facets) {
    for (Map.Entry<String, FacetParameter> entry : facets.getParameters().entrySet()) {
      entry.getValue().getValues().clear();
    }
  }
}
