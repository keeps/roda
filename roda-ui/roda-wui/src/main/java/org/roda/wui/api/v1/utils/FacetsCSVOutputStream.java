/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;

/**
 * CSV output stream for {@link FacetFieldResult}.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FacetsCSVOutputStream extends CSVOutputStream {

  /** The facets to write to output stream. */
  private final List<FacetFieldResult> facets;

  /**
   * Constructor.
   *
   * @param facets
   *          the facets to write to output stream.
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public FacetsCSVOutputStream(final List<FacetFieldResult> facets, final String filename, final char delimiter) {
    super(filename, delimiter);
    this.facets = facets;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    final CSVPrinter printer = getFormat().withHeader("field", "label", "value", "count").print(writer);

    for (FacetFieldResult facet : this.facets) {
      final String field = facet.getField();
      for (FacetValue value : facet.getValues()) {
        printer.printRecord(field, value.getLabel(), value.getValue(), value.getCount());
      }
    }

    writer.flush();
  }

}
