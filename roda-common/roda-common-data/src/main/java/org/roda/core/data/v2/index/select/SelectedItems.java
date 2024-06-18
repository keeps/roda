/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.select;

import java.io.Serializable;

import org.roda.core.data.v2.IsRODAObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@Type(value = SelectedItemsAll.class, name = "all"),
  @Type(value = SelectedItemsNone.class, name = "none"), @Type(value = SelectedItemsList.class, name = "list"),
  @Type(value = SelectedItemsFilter.class, name = "filter")})
@FunctionalInterface
public interface SelectedItems<T extends IsRODAObject> extends Serializable {
  String getSelectedClass();
}
