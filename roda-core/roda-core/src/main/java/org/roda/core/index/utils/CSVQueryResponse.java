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
import java.util.Collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * This class wraps a {@link QueryResponse} and produces CSV text with the
 * results.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
class CSVQueryResponse {

  /**
   * The {@link QueryResponse} with the results.
   */
  private final QueryResponse response;
  /**
   * Cached CSV text result.
   */
  private String csvCache;

  /**
   * Constructor.
   *
   * @param response
   *          the {@link QueryResponse} with the results.
   */
  CSVQueryResponse(final QueryResponse response) {
    this.response = response;
    this.csvCache = null;
  }

  /**
   * Constructs a {@link String} of CSV text with the {@link QueryResponse}
   * results.
   *
   * @return a {@link String} of CSV text.
   * @throws IOException
   *           if some I/O error occurs.
   */
  String toCSV() throws IOException {
    if (this.csvCache == null) {
      this.csvCache = buildCSV();
    }
    return this.csvCache;
  }

  /**
   * Transforms the {@link QueryResponse} into a {@link String} with CSV text.
   *
   * @return {@link String} with CSV text
   * @throws IOException
   *           if some I/O error occurs.
   */
  private String buildCSV() throws IOException {
    try (StringWriter strWriter = new StringWriter()) {
      final SolrDocumentList docList = response.getResults();

      if (!docList.isEmpty()) {
        final Collection<String> fieldNames = docList.get(0).getFieldNames();
        fieldNames.removeIf("_version_"::equals);
        final String[] headers = fieldNames.toArray(new String[fieldNames.size()]);
        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(headers).print(strWriter);

        for (SolrDocument doc : docList) {
          for (String field : fieldNames) {
            printer.print(doc.getFieldValue(field));
          }
          printer.println();
        }
      }

      return strWriter.toString();
    }
  }

}
