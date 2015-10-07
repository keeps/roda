package org.roda.core.data;

import java.io.Serializable;

/**
 * This is a search result object returned by the Search service inside a
 * {@link SearchResult}.
 * 
 * @author Rui Castro
 */
public class SearchResultObject implements Serializable {
  private static final long serialVersionUID = 4975828885923461514L;

  private int number = -1;
  private float score = -1;

  private DescriptionObject descriptionObject = null;

  /**
   * Constructs an empty SearchResultObject.
   */
  public SearchResultObject() {
  }

  /**
   * Constructs a new SearchResultObject cloning an existing SearchResultObject.
   * 
   * @param searchResultObject
   */
  public SearchResultObject(SearchResultObject searchResultObject) {
    this(searchResultObject.getNumber(), searchResultObject.getScore(), searchResultObject.getDescriptionObject());
  }

  /**
   * Constructs a SearchResultObject with the given parameters.
   * 
   * @param number
   * @param score
   * @param descriptionObject
   */
  public SearchResultObject(int number, float score, DescriptionObject descriptionObject) {
    setNumber(number);
    setScore(score);
    setDescriptionObject(descriptionObject);
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SearchResultObject (" + getNumber() + ", " + getScore() + ", " + getDescriptionObject() + ")";
  }

  /**
   * @return the number
   */
  public int getNumber() {
    return number;
  }

  /**
   * @param number
   *          the number to set
   */
  public void setNumber(int number) {
    this.number = number;
  }

  /**
   * @return the score
   */
  public float getScore() {
    return score;
  }

  /**
   * @param score
   *          the score to set
   */
  public void setScore(float score) {
    this.score = score;
  }

  /**
   * @return the descriptionObject
   */
  public DescriptionObject getDescriptionObject() {
    return descriptionObject;
  }

  /**
   * @param descriptionObject
   *          the descriptionObject to set
   */
  public void setDescriptionObject(DescriptionObject descriptionObject) {
    this.descriptionObject = descriptionObject;
  }

}
