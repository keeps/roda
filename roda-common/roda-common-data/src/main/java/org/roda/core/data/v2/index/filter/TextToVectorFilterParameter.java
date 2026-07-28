/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Semantic search parameter that vectorizes {@link #getQuery()} at query time
 * (via Solr's text-to-vector module) and restricts results to the
 * {@link #getTopK()} nearest neighbours of {@link #getField()}. Vectors
 * themselves are populated out-of-band by an external enrichment service, not
 * by RODA.
 */
@JsonTypeName("TextToVectorFilterParameter")
public class TextToVectorFilterParameter extends FilterParameter {
  @Serial
  private static final long serialVersionUID = 1L;

  private String field;
  private String query;
  private String model;
  private int topK;

  /**
   * Constructs an empty {@link TextToVectorFilterParameter}.
   */
  public TextToVectorFilterParameter() {
    // do nothing
  }

  public TextToVectorFilterParameter(String field, String query, String model, int topK) {
    setField(field);
    setQuery(query);
    setModel(model);
    setTopK(topK);
  }

  public TextToVectorFilterParameter(TextToVectorFilterParameter other) {
    this(other.getField(), other.getQuery(), other.getModel(), other.getTopK());
  }

  /**
   * @return the name of the {@code knn_vector} field to search against.
   */
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  /**
   * @return the free-text query to vectorize.
   */
  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * @return the name the embedding model is registered under in Solr's
   *         text-to-vector-model-store.
   */
  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  /**
   * @return the number of nearest neighbours to retrieve.
   */
  public int getTopK() {
    return topK;
  }

  public void setTopK(int topK) {
    this.topK = topK;
  }

  @Override
  public String toString() {
    return "TextToVectorFilterParameter(field=" + field + ", query=" + query + ", model=" + model + ", topK=" + topK
      + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + ((query == null) ? 0 : query.hashCode());
    result = prime * result + ((model == null) ? 0 : model.hashCode());
    result = prime * result + topK;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof TextToVectorFilterParameter other)) {
      return false;
    }
    if (topK != other.topK) {
      return false;
    }
    if (field == null ? other.field != null : !field.equals(other.field)) {
      return false;
    }
    if (query == null ? other.query != null : !query.equals(other.query)) {
      return false;
    }
    return model == null ? other.model == null : model.equals(other.model);
  }
}
