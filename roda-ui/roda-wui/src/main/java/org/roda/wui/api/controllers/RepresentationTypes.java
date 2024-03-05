package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RepresentationTypes {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  RepresentationTypes(){
    // do nothing
  }

  public static Pair<Boolean, List<String>> retrieveRepresentationTypeOptions(String locale, User user) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.representation_type.controlled_vocabulary", false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.representation_type.value");
    } else {
      try {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_TYPE));
        IndexResult<IndexedRepresentation> result = Browser.find(IndexedRepresentation.class.getName(), Filter.ALL, Sorter.NONE,
          user, Sublist.NONE, facets, locale, false, new ArrayList<>());

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.get(0).getValues()) {
          types.add(facetValue.getValue());
        }

        Boolean flag = false;

        for (String word : types) {
          if (word.equals("MIXED")) {
            flag = true;
            break;
          }
        }

        if (!flag)
          types.add("MIXED");
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not execute find request on representations", e);
      }
    }

    return Pair.of(isControlled, types);
  }
}
