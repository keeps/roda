/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.stream;

import org.apache.commons.csv.CSVFormat;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.wui.api.v2.utils.ExtraMediaType;

/**
 * Abstract CSV output stream.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public abstract class CSVOutputStream implements ConsumesOutputStream {

  /** The filename. */
  private final String filename;
  /** The CSV field delimiter. */
  private final String delimiter;

  /**
   * Constructor.
   *
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  protected CSVOutputStream(final String filename, final String delimiter) {
    this.filename = filename;
    this.delimiter = delimiter;
  }

  public String getDelimiter() {
    return delimiter;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  @Override
  public String getMediaType() {
    return ExtraMediaType.TEXT_CSV;
  }

  public abstract CSVFormat getFormat();
}
