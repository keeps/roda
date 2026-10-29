/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonTypeName("EDismaxSimpleQueryFilterParameter")
public class EDismaxSimpleQueryFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = 3614560283685151546L;

  private List<String> fields = new ArrayList<>();
  private String value;

  /**
   * Constructs an empty {@link EDismaxSimpleQueryFilterParameter}.
   */
  public EDismaxSimpleQueryFilterParameter() {
    // do nothing
  }

  /**
   * Constructs a {@link EDismaxSimpleQueryFilterParameter} cloning an existing
   * {@link EDismaxSimpleQueryFilterParameter}.
   *
   * @param eDismaxSimpleQueryFilterParameter
   *          the {@link EDismaxSimpleQueryFilterParameter} to clone.
   */
  public EDismaxSimpleQueryFilterParameter(EDismaxSimpleQueryFilterParameter eDismaxSimpleQueryFilterParameter) {
    this(eDismaxSimpleQueryFilterParameter.getFields(), eDismaxSimpleQueryFilterParameter.getValue());
  }

  /**
   * Constructs a {@link EDismaxSimpleQueryFilterParameter} with the given
   * parameters.
   *
   * @param fields
   *          the query fields (Solr <code>qf</code> entries, optionally boosted,
   *          e.g. <code>title^5</code>). <code>null</code> or empty means the
   *          default catch-all search field.
   * @param value
   *          the free-text search term, as typed by the user.
   */
  public EDismaxSimpleQueryFilterParameter(List<String> fields, String value) {
    setFields(fields);
    setValue(value);
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "EDismaxSimpleQueryFilterParameter(fields=" + getFields() + ", value=" + getValue() + ")";
  }

  /**
   * @return the query fields (Solr <code>qf</code> entries); never
   *         <code>null</code>, empty means the default catch-all search field.
   */
  public List<String> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the query fields (Solr <code>qf</code> entries) to set;
   *          <code>null</code> is stored as an empty list.
   */
  public void setFields(List<String> fields) {
    this.fields = fields != null ? new ArrayList<>(fields) : new ArrayList<>();
  }

  /**
   * @return the free-text search term.
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the free-text search term to set.
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), fields, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof EDismaxSimpleQueryFilterParameter)) {
      return false;
    }
    EDismaxSimpleQueryFilterParameter other = (EDismaxSimpleQueryFilterParameter) obj;
    return Objects.equals(fields, other.fields) && Objects.equals(value, other.value);
  }
}
