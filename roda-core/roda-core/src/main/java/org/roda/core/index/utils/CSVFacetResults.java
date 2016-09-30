/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;

/**
 * This class wraps a list of {@link FacetFieldResult} and produces CSV text
 * with the facet results.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CSVFacetResults {

  /**
   * The {@link QueryResponse} with the results.
   */
  private final List<FacetFieldResult> facetResults;
  /**
   * Cached CSV text.
   */
  private String csvCache;

  /**
   * Constructor.
   *
   * @param facetResults
   *          the {@link FacetFieldResult}s.
   */
  public CSVFacetResults(final List<FacetFieldResult> facetResults) {
    this.facetResults = facetResults;
    this.csvCache = null;
  }

  /**
   * Constructs a {@link String} of CSV text with the {@link FacetFieldResult}s.
   * 
   * @return a {@link String} of CSV text.
   * @throws IOException
   *           if some I/O error occurs.
   */
  public String toCSV() throws IOException {
    if (this.csvCache == null) {
      this.csvCache = buildCSV();
    }
    return this.csvCache;
  }

  /**
   * Transforms the {@link QueryResponse} facets into a {@link String} with CSV
   * text.
   * 
   * @return {@link String} with CSV text
   * @throws IOException
   *           if some I/O error occurs.
   */
  private String buildCSV() throws IOException {
    try (StringWriter strWriter = new StringWriter()) {

      if (!this.facetResults.isEmpty()) {
        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader("field", "label", "value", "count").print(strWriter);

        for (FacetFieldResult facet : this.facetResults) {
          final String field = facet.getField();
          for (FacetValue value : facet.getValues()) {
            printer.printRecord(field, value.getLabel(), value.getValue(), value.getCount());
          }
        }
      }

      return strWriter.toString();
    }
  }

}
