package org.roda.wui.api.v2.advice;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.wui.api.v2.utils.SecurityFilteringUtils;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.filters.AipPermissionPropertyFilter;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@ControllerAdvice
public class SecurityFilteringResponseAdvice implements ResponseBodyAdvice<Object> {

  @Autowired
  private HttpServletRequest request;

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // Only intercept when Spring selects the Jackson 3 HTTP message converter
    return JacksonJsonHttpMessageConverter.class.isAssignableFrom(converterType);
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType,
    org.springframework.http.server.ServerHttpRequest serverRequest,
    org.springframework.http.server.ServerHttpResponse serverResponse) {
    if (body instanceof IndexResult<?> indexResult) {
      if (indexResult.getFacetResults() != null && !indexResult.getFacetResults().isEmpty()) {
        RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
        User currentUser = requestContext.getUser();

        // Strip any returned facet that the user is not authorized to see
        indexResult.getFacetResults()
          .removeIf(facet -> !SecurityFilteringUtils.isFieldAuthorizedForUser(facet.getField(), currentUser));
      }
    }
    return body;
  }

  @Override
  public Map<String, Object> determineWriteHints(Object body, MethodParameter returnType, MediaType selectedContentType,
    Class<? extends HttpMessageConverter<?>> selectedConverterType) {
    if (body == null) {
      return null;
    }

    // Extract current user from RODA request context
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    User currentUser = requestContext.getUser();

    // Instantiate the Jackson 3 filter provider for this specific user
    FilterProvider filters = new SimpleFilterProvider()
      .addFilter("aipPermissionFilter", new AipPermissionPropertyFilter(currentUser)).setFailOnUnknownId(false);

    // Pass the FilterProvider directly to Jackson 3 via SmartHttpMessageConverter
    // hints
    return Map.of(FilterProvider.class.getName(), filters);
  }
}