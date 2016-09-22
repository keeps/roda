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

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

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
  String toCSVString() throws IOException {
    if (this.csvCache == null) {
      this.csvCache = responseToCSV();
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
  private String responseToCSV() throws IOException {
    final StringWriter strWriter = new StringWriter();
    try (ICsvMapWriter mapWriter = new CsvMapWriter(strWriter, CsvPreference.STANDARD_PREFERENCE)) {

      final SolrDocumentList docList = response.getResults();

      if (!docList.isEmpty()) {
        final Collection<String> fieldNames = docList.get(0).getFieldNames();
        fieldNames.removeIf("_version_"::equals);
        final String[] headers = fieldNames.toArray(new String[fieldNames.size()]);
        mapWriter.writeHeader(headers);

        final CellProcessor[] processors = getProcessors(docList.get(0));

        for (SolrDocument doc : docList) {
          mapWriter.write(doc.getFieldValueMap(), headers, processors);
        }
      }

      mapWriter.flush();
      return strWriter.toString();
    }
  }

  /**
   * Return the {@link CellProcessor}s for the {@link SolrDocument}.
   *
   * @param doc
   *          the {@link SolrDocument}.
   * @return an array of {@link CellProcessor}.
   */
  private CellProcessor[] getProcessors(final SolrDocument doc) {

    final Collection<String> fieldNames = doc.getFieldNames();
    final CellProcessor[] processors = new CellProcessor[fieldNames.size()];
    for (int i = 0; i < fieldNames.size(); i++) {
      processors[i] = new Optional();
    }
    return processors;
  }
}
