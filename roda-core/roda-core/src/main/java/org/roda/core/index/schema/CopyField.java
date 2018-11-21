/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.schema.SchemaRequest.AddCopyField;
import org.apache.solr.client.solrj.request.schema.SchemaRequest.Update;

public class CopyField {

  private static final String ATTR_SOURCE = "source";
  private static final String ATTR_DEST = "dest";

  private final String source;
  private final List<String> destinations;

  public CopyField(String source, String... destinations) {
    this(source, Arrays.asList(destinations));
  }

  public CopyField(String source, List<String> destinations) {
    super();
    this.source = source;
    this.destinations = destinations;
  }

  @SuppressWarnings("unchecked")
  public CopyField(Map<String, Object> map) {
    this.source = (String) map.get(ATTR_SOURCE);

    Object dest = map.get(ATTR_DEST);
    if (dest instanceof String) {
      this.destinations = Arrays.asList((String) dest);
    } else {
      this.destinations = (List<String>) map.get(ATTR_DEST);
    }
  }

  public String getSource() {
    return source;
  }

  public List<String> getDestinations() {
    return destinations;
  }

  public Update buildCreate() {
    return new AddCopyField(source, destinations);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((destinations == null) ? 0 : destinations.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CopyField other = (CopyField) obj;
    if (destinations == null) {
      if (other.destinations != null) {
        return false;
      }
    } else if (!destinations.equals(other.destinations)) {
      return false;
    }
    if (source == null) {
      if (other.source != null) {
        return false;
      }
    } else if (!source.equals(other.source)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("CopyField [");
    if (source != null) {
      builder.append("source=");
      builder.append(source);
      builder.append(", ");
    }
    if (destinations != null) {
      builder.append("destinations=");
      builder.append(destinations);
    }
    builder.append("]");
    return builder.toString();
  }

}
