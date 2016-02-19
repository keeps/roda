/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

public class Pair<K, V> {

  private K fst;
  private V snd;

  public Pair(K first, V second) {
    this.fst = first;
    this.snd = second;
  }

  public static <K, V> Pair<K, V> create(K key, V value) {
    return new Pair<K, V>(key, value);
  }

  public K getFirst() {
    return this.fst;
  }

  public void setFirst(K value) {
    this.fst = value;
  }

  public V getSecond() {
    return this.snd;
  }

  public void setSecond(V value) {
    this.snd = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fst == null) ? 0 : fst.hashCode());
    result = prime * result + ((snd == null) ? 0 : snd.hashCode());
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
    if (!(obj instanceof Pair)) {
      return false;
    }
    Pair<?, ?> other = (Pair<?, ?>) obj;
    if (fst == null) {
      if (other.fst != null) {
        return false;
      }
    } else if (!fst.equals(other.fst)) {
      return false;
    }
    if (snd == null) {
      if (other.snd != null) {
        return false;
      }
    } else if (!snd.equals(other.snd)) {
      return false;
    }
    return true;
  }

}
