/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Tree<T> implements Serializable {
  private static final long serialVersionUID = 5586782882694784478L;
  private T label;
  private T value;
  private Tree<T> parent;
  private List<Tree<T>> children;

  public Tree() {
    super();
  }

  public Tree(T label, T value) {
    this.label = label;
    this.value = value;
    this.children = new LinkedList<>();
  }

  public Tree<T> addChild(T label, T value) {
    Tree<T> childNode = new Tree<>(label, value);
    childNode.parent = this;
    this.children.add(childNode);
    return childNode;
  }

  public T getLabel() {
    return label;
  }

  public void setLabel(T label) {
    this.label = label;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public Tree<T> getParent() {
    return parent;
  }

  public void setParent(Tree<T> parent) {
    this.parent = parent;
  }

  public List<Tree<T>> getChildren() {
    return children;
  }

  public void setChildren(List<Tree<T>> children) {
    this.children = children;
  }
}