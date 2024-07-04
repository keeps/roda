/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.index.utils.IterableIndexResult;

/**
 * CSV output stream for {@link IterableIndexResult}.
 *
 * @param <T>
 *          the type of results.
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class ResultsCSVOutputStream<T extends IsIndexed> extends CSVOutputStream {

  /** The results to write to output stream. */
  private final IndexResult<T> results;

  /**
   * Constructor.
   *
   * @param results
   *          the results to write to output stream.
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public ResultsCSVOutputStream(final IndexResult<T> results, final String filename, final String delimiter) {
    super(filename, delimiter);
    this.results = results;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;
    boolean isFirst = true;
    for (final T result : this.results.getResults()) {
      if (isFirst) {
        printer = getFormat(result.toCsvHeaders().toArray(new String[0])).print(writer);
        isFirst = false;
      }
      printer.printRecord(result.toCsvValues());
    }
    writer.flush();
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Override
  public long getSize() {
    return -1;
  }

  @Override
  public CSVFormat getFormat() {
    return CSVFormat.Builder.create(CSVFormat.EXCEL).setDelimiter(getDelimiter()).build();
  }

  public CSVFormat getFormat(String[] headers) {
    return CSVFormat.Builder.create(CSVFormat.EXCEL).setHeader(headers).setDelimiter(getDelimiter()).build();
  }
}
