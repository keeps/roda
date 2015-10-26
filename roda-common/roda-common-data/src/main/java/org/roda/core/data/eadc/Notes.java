/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class Notes implements EadCValue, Serializable {

  private static final long serialVersionUID = -1401537653861195337L;

  private Note[] notes = null;

  /**
   * Constructs a new empty {@link Notes}
   * */
  public Notes() {
  }

  /**
   * Constructs a new {@link Notes} using the provided parameters
   * 
   * @param notes
   */
  public Notes(Note[] notes) {
    this.notes = notes;
  }

  /**
   * @return the notes
   */
  public Note[] getNotes() {
    return notes;
  }

  /**
   * @param notes
   *          the notes to set
   */
  public void setNotes(Note[] notes) {
    this.notes = notes;
  }

  @Override
  public String toString() {
    return "Notes [notes=" + Arrays.toString(notes) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(notes);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Notes)) {
      return false;
    }
    Notes other = (Notes) obj;
    if (!Arrays.equals(notes, other.notes)) {
      return false;
    }
    return true;
  }

}
