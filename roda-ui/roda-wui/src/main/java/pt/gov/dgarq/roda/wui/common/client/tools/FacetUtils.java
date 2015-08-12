package pt.gov.dgarq.roda.wui.common.client.tools;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;

import pt.gov.dgarq.roda.core.data.adapter.facet.FacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.AsyncTableCell;

public class FacetUtils {

	private static ClientLogger LOGGER = new ClientLogger(FacetUtils.class.getName());

	public static <T extends Serializable> void bindFacets(final AsyncTableCell<T> list,
			final Map<String, FlowPanel> facetPanels) {
		list.addValueChangeHandler(new ValueChangeHandler<IndexResult<T>>() {

			@Override
			public void onValueChange(ValueChangeEvent<IndexResult<T>> event) {
				FacetUtils.updateFacetPanels(list, facetPanels, event.getValue().getFacetResults());
			}
		});
	}

	private static <T extends Serializable> void updateFacetPanels(final AsyncTableCell<T> list,
			final Map<String, FlowPanel> facetPanels, final List<FacetFieldResult> facetResults) {

		for (FacetFieldResult facetResult : facetResults) {
			final String facetField = facetResult.getField();
			FlowPanel facetPanel = facetPanels.get(facetResult.getField());
			if (facetPanel != null) {
				facetPanel.clear();

				for (FacetValue facetValue : facetResult.getValues()) {
					final String value = facetValue.getValue();
					long count = facetValue.getCount();
					boolean selected = facetResult.getSelectedValues().contains(value);
					StringBuilder checkboxLabel = new StringBuilder();
					checkboxLabel.append(value);
					if (count > 0 || facetResult.getSelectedValues().size() == 0 || selected) {
						checkboxLabel.append(" (").append(count).append(")");
					}

					CheckBox facetValuePanel = new CheckBox(checkboxLabel.toString());
					facetValuePanel.addStyleName("sidebar-facet-label");
					facetValuePanel.setEnabled(count > 0 || facetResult.getSelectedValues().size() > 0);
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
				LOGGER.warn("Got a facet but haven't got a panel for it");
			}
		}
	}
}
