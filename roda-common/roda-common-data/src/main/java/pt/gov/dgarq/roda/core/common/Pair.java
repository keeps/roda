package pt.gov.dgarq.roda.core.common;

public class Pair<K, V> {
  private K fst;
  private V snd;

  public Pair(K first, V second) {
    this.fst = first;
    this.snd = second;
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
  protected Object clone() throws CloneNotSupportedException {
    return new Pair<K, V>(this.fst, this.snd);
  }

  @Override
  public boolean equals(Object obj) {
    boolean res = true;
    if (obj != this) {
      res = false;
    }
    if (!(obj instanceof Pair)) {
      res = false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) obj;
    if (!(this.fst.equals(pair.getFirst())) || !(this.snd.equals(pair.getSecond()))) {
      res = false;
    }
    return res;
  }
}
