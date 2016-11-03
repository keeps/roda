/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.io.Serializable;
import java.util.List;

import org.roda.wui.client.common.utils.Tree;

public class SearchField implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;
  private String id;
  private List<String> searchFields;
  private String label;
  private String type;
  private boolean fixed;
  private Tree<String> terms;
  private String suggestField;
  private boolean suggestPartial;

  public SearchField() {
    super();
  }

  public SearchField(String id, List<String> searchFields, String label, String type) {
    super();
    this.id = id;
    this.searchFields = searchFields;
    this.label = label;
    this.type = type;
    this.fixed = false;
    this.terms = null;
    this.suggestField = null;
    this.setSuggestPartial(false);
  }

  public SearchField(String id, List<String> searchFields, String label, String type, Tree<String> terms) {
    super();
    this.id = id;
    this.searchFields = searchFields;
    this.label = label;
    this.type = type;
    this.fixed = false;
    this.terms = terms;
    this.suggestField = null;
    this.setSuggestPartial(false);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getSearchFields() {
    return searchFields;
  }

  public void setSearchFields(List<String> searchFields) {
    this.searchFields = searchFields;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  public Tree<String> getTerms() {
    return terms;
  }

  public void setTerms(Tree<String> terms) {
    this.terms = terms;
  }

  public String getSuggestField() {
    return suggestField;
  }

  public void setSuggestField(String suggestField) {
    this.suggestField = suggestField;
  }

  public boolean isSuggestPartial() {
    return suggestPartial;
  }

  public void setSuggestPartial(boolean suggestPartial) {
    this.suggestPartial = suggestPartial;
  }

  @Override
  public String toString() {
    return "SearchField [id=" + id + ", searchFields=" + searchFields + ", label=" + label + ", type=" + type
      + ", fixed=" + fixed + ", terms=" + terms + ", suggestField=" + suggestField + ", suggestPartial="
      + suggestPartial + "]";
  }
}