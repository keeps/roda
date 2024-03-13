package org.roda.wui.api.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.v1.utils.ApiResponseMessage;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v1.utils.ExtraMediaType;
import org.roda.wui.client.services.IndexService;
import org.roda.wui.common.I18nUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path(IndexResource.ENDPOINT)
@Tag(name = IndexResource.SWAGGER_ENDPOINT)
public class IndexResource implements IndexService {
  public static final String ENDPOINT = "/v2/index";
  public static final String SWAGGER_ENDPOINT = "v2 index";
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(org.roda.wui.api.v1.IndexResource.class);

  /**
   * HTTP request.
   */
  @Context
  private HttpServletRequest request;

  @Override
  public <T extends IsIndexed> IndexResult<T> find(FindRequest findRequest, String localeString) throws RODAException {
    if(findRequest.filter == null || findRequest.filter.getParameters().isEmpty()){
      return new IndexResult<>();
    }

    final User user = UserUtility.getApiUser(request);


    final IndexResult<T> result = Browser.find(getClass(findRequest.classToReturn), findRequest.filter,
      findRequest.sorter, findRequest.sublist, findRequest.facets, user, findRequest.onlyActive,
      findRequest.fieldsToReturn);

    return I18nUtility.translate(result, getClass(findRequest.classToReturn), localeString);

  }

  /**
   * Return the {@link Class} with the specified class name.
   *
   * @param className
   *          the fully qualified name of the desired class.
   * @param <T>
   *          the type of {@link Class}.
   * @return the {@link Class} with the specified class name.
   * @throws RequestNotValidException
   *           if the class name is not valid.
   */
  @SuppressWarnings("unchecked")
  private <T> Class<T> getClass(final String className) throws RequestNotValidException {
    if (RodaConstants.WHITELIST_CLASS_NAMES.contains(className)) {
      try {
        return (Class<T>) Class.forName(className);
      } catch (final ClassNotFoundException e) {
        throw new RequestNotValidException(String.format("Invalid value for classToReturn '%s'", className), e);
      }
    } else {
      throw new RequestNotValidException(String.format("Invalid value for classToReturn '%s'", className));
    }
  }

}
