package pjarenderer.util;

public class Pair<T, U> {
  public T first;
  public U second;

  public Pair() {
    this(null, null);
  }

  public Pair(T f, U s) {
    this.first = f;
    this.second = s;
  }
}
