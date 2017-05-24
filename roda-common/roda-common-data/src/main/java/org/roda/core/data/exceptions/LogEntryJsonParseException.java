/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class LogEntryJsonParseException extends RODAException {

  private static final long serialVersionUID = -8336065269567551582L;

  private String filename;
  private int line = 0;

  public LogEntryJsonParseException() {
    super();
  }

  public LogEntryJsonParseException(String message) {
    super(message);
  }

  public LogEntryJsonParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public LogEntryJsonParseException(Throwable cause) {
    super(cause);
  }

  public String getFilename() {
    return filename;
  }

  public LogEntryJsonParseException setFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public int getLine() {
    return line;
  }

  public LogEntryJsonParseException setLine(int line) {
    this.line = line;
    return this;
  }

}
