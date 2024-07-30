package org.roda.core.data.v2.generics.select;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(value = SelectedItemsListRequest.class, name = "SelectedItemsListRequest"),
  @JsonSubTypes.Type(value = SelectedItemsFilterRequest.class, name = "SelectedItemsFilterRequest"),
  @JsonSubTypes.Type(value = SelectedItemsNoneRequest.class, name = "SelectedItemsNoneRequest"),
  @JsonSubTypes.Type(value = SelectedItemsAllRequest.class, name = "SelectedItemsAllRequest")})
@Schema(type = "object", subTypes = {SelectedItemsListRequest.class,
  SelectedItemsFilterRequest.class, SelectedItemsNoneRequest.class,
  SelectedItemsAllRequest.class}, discriminatorMapping = {
    @DiscriminatorMapping(value = "SelectedItemsListRequest", schema = SelectedItemsListRequest.class),
    @DiscriminatorMapping(value = "SelectedItemsFilterRequest", schema = SelectedItemsFilterRequest.class),
    @DiscriminatorMapping(value = "SelectedItemsNoneRequest", schema = SelectedItemsNoneRequest.class),
    @DiscriminatorMapping(value = "SelectedItemsAllRequest", schema = SelectedItemsAllRequest.class)}, discriminatorProperty = "@type")
public interface SelectedItemsRequest extends Serializable {
}
