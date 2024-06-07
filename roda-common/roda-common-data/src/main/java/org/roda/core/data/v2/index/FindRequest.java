/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index;

import java.io.Serial;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.index.collapse.Collapse;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * A request to a find operation.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
@JsonDeserialize(builder = FindRequest.FindRequestBuilder.class)
public class FindRequest extends CountRequest {

  @Serial
  private static final long serialVersionUID = 5997470558754294987L;

  /** Sorter. */
  @JsonProperty("sorter")
  private Sorter sorter;
  /** Sublist (paging). */
  @JsonProperty("sublist")
  private Sublist sublist;
  /** Facets to return. */
  @JsonProperty("facets")
  private Facets facets;
  /** For CSV results, export only facets? */
  @JsonProperty("exportFacets")
  private boolean exportFacets;
  /** The filename for exported CSV. */
  @JsonProperty("filename")
  private String filename;
  /** The index fields to return and use to construct the indexed object. */
  @JsonProperty("fieldsToReturn")
  private List<String> fieldsToReturn;
  /** The filter querying options for collapsing results */
  @JsonProperty("collapse")
  private Collapse collapse;
  @JsonProperty("children")
  private boolean children;

  // Private constructor for Jackson deserialization
  private FindRequest(FindRequestBuilder builder) {
    super(builder.filter, builder.onlyActive);
    this.sorter = builder.sorter;
    this.sublist = builder.sublist;
    this.facets = builder.facets;
    this.exportFacets = builder.exportFacets;
    this.filename = builder.filename;
    this.fieldsToReturn = builder.fieldsToReturn;
    this.collapse = builder.collapse;
    this.children = builder.children;
  }

  public Sorter getSorter() {
    return sorter;
  }

  public Sublist getSublist() {
    return sublist;
  }

  public Facets getFacets() {
    return facets;
  }

  public boolean isExportFacets() {
    return exportFacets;
  }

  public String getFilename() {
    return filename;
  }

  public List<String> getFieldsToReturn() {
    return fieldsToReturn;
  }

  public Collapse getCollapse() {
    return collapse;
  }

  public boolean getChildren() {
    return children;
  }

  public static FindRequestBuilder getBuilder(final Filter filter, boolean onlyActive) {
    return new FindRequestBuilder(filter, onlyActive);
  }

  @JsonPOJOBuilder
  public static class FindRequestBuilder {
    private final Filter filter;
    private final boolean onlyActive;
    private Sorter sorter;
    private Sublist sublist;
    private Facets facets;
    private boolean exportFacets;
    private String filename = null;
    private List<String> fieldsToReturn;
    private Collapse collapse;
    private boolean children;

    public FindRequestBuilder(@JsonProperty("filter") final Filter filter, @JsonProperty("onlyActive") boolean onlyActive) {
      // mandatory
      this.filter = filter;
      this.onlyActive = onlyActive;
      // optional with defaults if not set
      this.sorter = Sorter.NONE;
      this.sublist = new Sublist(0, 100);
      this.facets = Facets.NONE;
      this.exportFacets = false;
      this.fieldsToReturn = Collections.emptyList();
      this.collapse = null;
      this.children = false;
    }

    public FindRequest build() {
      return new FindRequest(this);
    }

    public FindRequestBuilder withSorter(Sorter sorter) {
      this.sorter = sorter;
      return this;
    }

    public FindRequestBuilder withSublist(Sublist sublist) {
      this.sublist = sublist;
      return this;
    }

    public FindRequestBuilder withFacets(Facets facets) {
      this.facets = facets;
      return this;
    }

    public FindRequestBuilder withExportFacets(boolean exportFacets) {
      this.exportFacets = exportFacets;
      return this;
    }

    public FindRequestBuilder withFilename(String filename) {
      this.filename = filename;
      return this;
    }

    public FindRequestBuilder withFieldsToReturn(List<String> fieldsToReturn) {
      this.fieldsToReturn = fieldsToReturn;
      return this;
    }

    public FindRequestBuilder withCollapse(Collapse collapse) {
      this.collapse = collapse;
      return this;
    }

    public FindRequestBuilder withChildren(boolean children) {
      this.children = children;
      return this;
    }
  }
}
