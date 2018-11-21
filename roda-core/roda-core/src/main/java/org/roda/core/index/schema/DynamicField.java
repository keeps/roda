/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.solr.client.solrj.request.schema.SchemaRequest.AddDynamicField;
import org.apache.solr.client.solrj.request.schema.SchemaRequest.Update;

public class DynamicField {

  private static final String ATTR_NAME = "name";
  private static final String ATTR_TYPE = "type";
  private static final String ATTR_INDEXED = "indexed";
  private static final String ATTR_STORED = "stored";
  private static final String ATTR_DOC_VALUES = "docValues";
  private static final String ATTR_MULTI_VALUED = "multiValued";
  private static final String ATTR_REQUIRED = "required";
  private static final String ATTR_DEFAULT = "default";

  public static final String DYNAMIC_FIELD_PERMISSION_USERS = "permission_users_*";
  public static final String DYNAMIC_FIELD_PERMISSION_GROUPS = "permission_groups_*";

  private final String name;
  private final String type;
  private Optional<Boolean> indexed, stored, docValues, multiValued, required;
  private Optional<String> defaultValue;

  public DynamicField(String name, String type) {
    this.name = name;
    this.type = type;
    indexed = stored = docValues = multiValued = required = Optional.empty();
    defaultValue = Optional.empty();
  }

  public DynamicField(Map<String, Object> attributes) {
    this.name = (String) attributes.get(ATTR_NAME);
    this.type = (String) attributes.get(ATTR_TYPE);
    this.indexed = Optional.ofNullable((Boolean) attributes.get(ATTR_INDEXED));
    this.stored = Optional.ofNullable((Boolean) attributes.get(ATTR_STORED));
    this.docValues = Optional.ofNullable((Boolean) attributes.get(ATTR_DOC_VALUES));
    this.multiValued = Optional.ofNullable((Boolean) attributes.get(ATTR_MULTI_VALUED));
    this.required = Optional.ofNullable((Boolean) attributes.get(ATTR_REQUIRED));
    this.defaultValue = Optional.ofNullable((String) attributes.get(ATTR_DEFAULT));
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Optional<Boolean> getIndexed() {
    return indexed;
  }

  /**
   * 
   * If true, the value of the field can be used in queries to retrieve matching
   * documents.
   * 
   * Implicit default: true
   * 
   * @param indexed
   * @return
   */
  public DynamicField setIndexed(Optional<Boolean> indexed) {
    this.indexed = indexed;
    return this;
  }

  /**
   * 
   * If true, the value of the field can be used in queries to retrieve matching
   * documents.
   * 
   * Implicit default: true
   * 
   * @param indexed
   * @return
   */
  public DynamicField setIndexed(Boolean indexed) {
    return setIndexed(Optional.of(indexed));
  }

  public Optional<Boolean> getStored() {
    return stored;
  }

  /**
   * 
   * If true, the actual value of the field can be retrieved by queries.
   * 
   * Implicit default: true
   * 
   * @param stored
   * @return
   */
  public DynamicField setStored(Optional<Boolean> stored) {
    this.stored = stored;
    return this;
  }

  /**
   * 
   * If true, the actual value of the field can be retrieved by queries.
   * 
   * Implicit default: true
   * 
   * @param stored
   * @return
   */
  public DynamicField setStored(Boolean stored) {
    return setStored(Optional.of(stored));
  }

  public Optional<Boolean> getDocValues() {
    return docValues;
  }

  public DynamicField setDocValues(Optional<Boolean> docValues) {
    this.docValues = docValues;
    return this;
  }

  /**
   * If true, the value of the field will be put in a column-oriented DocValues
   * structure.
   * 
   * Implicit default: false
   * 
   * @param docValues
   * @return
   */
  public DynamicField setDocValues(Boolean docValues) {
    return setDocValues(Optional.of(docValues));
  }

  public Optional<Boolean> getMultiValued() {
    return multiValued;
  }

  public DynamicField setMultiValued(Optional<Boolean> multiValued) {
    this.multiValued = multiValued;
    return this;
  }

  /**
   * If true, indicates that a single document might contain multiple values for
   * this field type.
   * 
   * Implicit default: false
   * 
   * @param multivalued
   * @return
   */
  public DynamicField setMultiValued(Boolean multivalued) {
    return setMultiValued(Optional.of(multivalued));
  }

  public Optional<Boolean> getRequired() {
    return required;
  }

  public DynamicField setRequired(Optional<Boolean> required) {
    this.required = required;
    return this;
  }

  /**
   * Instructs Solr to reject any attempts to add a document which does not have
   * a value for this field. This property defaults to false.
   * 
   * Implicit default: false
   * 
   * @param required
   * @return
   */
  public DynamicField setRequired(Boolean required) {
    return setRequired(Optional.of(required));
  }

  public Optional<String> getDefaultValue() {
    return defaultValue;
  }

  public DynamicField setDefaultValue(Optional<String> defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public DynamicField setDefaultValue(String defaultValue) {
    return setDefaultValue(Optional.of(defaultValue));
  }

  protected Map<String, Object> getFieldAttributes() {
    Map<String, Object> fieldAttributes = new LinkedHashMap<>();
    fieldAttributes.put(ATTR_NAME, name);
    fieldAttributes.put(ATTR_TYPE, type);

    if (indexed.isPresent()) {
      fieldAttributes.put(ATTR_INDEXED, indexed.get());
    }

    if (stored.isPresent()) {
      fieldAttributes.put(ATTR_STORED, stored.get());
    }

    if (docValues.isPresent()) {
      fieldAttributes.put(ATTR_DOC_VALUES, docValues.get());
    }

    if (multiValued.isPresent()) {
      fieldAttributes.put(ATTR_MULTI_VALUED, multiValued.get());
    }

    if (required.isPresent()) {
      fieldAttributes.put(ATTR_REQUIRED, required.get());
    }

    if (defaultValue.isPresent()) {
      fieldAttributes.put(ATTR_DEFAULT, defaultValue.get());
    }

    return fieldAttributes;
  }

  public Update buildCreate() {
    return new AddDynamicField(getFieldAttributes());
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
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((docValues == null) ? 0 : docValues.hashCode());
    result = prime * result + ((indexed == null) ? 0 : indexed.hashCode());
    result = prime * result + ((multiValued == null) ? 0 : multiValued.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((required == null) ? 0 : required.hashCode());
    result = prime * result + ((stored == null) ? 0 : stored.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    DynamicField other = (DynamicField) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null) {
        return false;
      }
    } else if (!defaultValue.equals(other.defaultValue)) {
      return false;
    }
    if (docValues == null) {
      if (other.docValues != null) {
        return false;
      }
    } else if (!docValues.equals(other.docValues)) {
      return false;
    }
    if (indexed == null) {
      if (other.indexed != null) {
        return false;
      }
    } else if (!indexed.equals(other.indexed)) {
      return false;
    }
    if (multiValued == null) {
      if (other.multiValued != null) {
        return false;
      }
    } else if (!multiValued.equals(other.multiValued)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (required == null) {
      if (other.required != null) {
        return false;
      }
    } else if (!required.equals(other.required)) {
      return false;
    }
    if (stored == null) {
      if (other.stored != null) {
        return false;
      }
    } else if (!stored.equals(other.stored)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
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
    builder.append("DynamicField [");
    if (name != null) {
      builder.append("name=");
      builder.append(name);
      builder.append(", ");
    }
    if (type != null) {
      builder.append("type=");
      builder.append(type);
      builder.append(", ");
    }
    if (indexed != null) {
      builder.append("indexed=");
      builder.append(indexed);
      builder.append(", ");
    }
    if (stored != null) {
      builder.append("stored=");
      builder.append(stored);
      builder.append(", ");
    }
    if (docValues != null) {
      builder.append("docValues=");
      builder.append(docValues);
      builder.append(", ");
    }
    if (multiValued != null) {
      builder.append("multiValued=");
      builder.append(multiValued);
      builder.append(", ");
    }
    if (required != null) {
      builder.append("required=");
      builder.append(required);
      builder.append(", ");
    }
    if (defaultValue != null) {
      builder.append("defaultValue=");
      builder.append(defaultValue);
    }
    builder.append("]");
    return builder.toString();
  }

  public boolean isEquivalentTo(DynamicField other) {
    if (!name.equals(other.getName())) {
      return false;
    }

    if (!type.equals(other.getType())) {
      return false;
    }

    // ignore omissions
    if (indexed.isPresent() && other.getIndexed().isPresent() && !indexed.equals(other.getIndexed())) {
      return false;
    }

    if (stored.isPresent() && other.getStored().isPresent() && !stored.equals(other.getStored())) {
      return false;
    }

    if (docValues.isPresent() && other.getDocValues().isPresent() && !docValues.equals(other.getDocValues())) {
      return false;
    }

    if (multiValued.isPresent() && other.getMultiValued().isPresent() && !multiValued.equals(other.getMultiValued())) {
      return false;
    }

    if (required.isPresent() && other.getRequired().isPresent() && !required.equals(other.getRequired())) {
      return false;
    }

    // don't ignore default value omission
    if (!defaultValue.equals(other.getDefaultValue())) {
      return false;
    }

    return true;
  }

}
