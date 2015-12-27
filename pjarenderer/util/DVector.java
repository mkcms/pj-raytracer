package pjarenderer.util;

public class DVector<T> {

  public static class BadSize extends Exception {
    public BadSize(int k) {
      super("Zly rozmiar: " + k);
    }
  }

  private T[] array;
  private int size;

  public DVector() {
    this.array = (T[]) (new Object[8]);
    this.size = 0;
  }

  public DVector(int initial) throws BadSize {
    if (initial < 1) throw new BadSize(initial);

    this.array = (T[]) (new Object[initial]);
    this.size = 0;
  }

  public <U extends T> DVector(U[] arr) throws NullPointerException {
    if (arr == null) throw new NullPointerException();

    int n = arr.length;
    if (n == 0) n = 1;
    this.array = (T[]) (new Object[n]);
    copy(arr, this.array, 0, arr.length, 0);
    this.size = n;
  }

  public int capacity() {
    return this.array.length;
  }

  public int size() {
    return this.size;
  }

  public T itemAt(int idx) throws ArrayIndexOutOfBoundsException {
    if (idx < 0 || idx >= this.size) throw new ArrayIndexOutOfBoundsException(idx);

    return this.array[idx];
  }

  public <U extends T> void setItemAt(int idx, U elem) throws ArrayIndexOutOfBoundsException {
    if (idx < 0 || idx >= this.size) throw new ArrayIndexOutOfBoundsException(idx);

    this.array[idx] = elem;
  }

  public <U extends T> void add(U elem) {
    makeSpace(1, this.size);
    this.array[this.size - 1] = elem;
  }

  public <U extends T> void insert(int idx, U elem) throws ArrayIndexOutOfBoundsException {
    if (idx < 0 || idx > this.size) throw new ArrayIndexOutOfBoundsException(idx);

    makeSpace(1, idx);
    this.array[idx] = elem;
  }

  public void erase(int idx) throws ArrayIndexOutOfBoundsException {
    if (idx < 0 || idx >= this.size) throw new ArrayIndexOutOfBoundsException(idx);

    copy(this.array, this.array, idx + 1, this.size, -1);

    this.size--;
  }

  public void erase(int fromIdx, int toIdx) throws ArrayIndexOutOfBoundsException {
    if (fromIdx < 0 || fromIdx >= this.size || toIdx < fromIdx || toIdx >= this.size)
      throw new ArrayIndexOutOfBoundsException("" + fromIdx + " lub " + toIdx);

    int n = toIdx - fromIdx;

    copy(this.array, this.array, toIdx + 1, this.size, -n - 1);

    for (int i = this.size - n; i < this.size; ++i) {
      this.array[i] = null;
    }

    this.size -= n + 1;
  }

  @Override
  public String toString() {
    String out = "DV<" + (size() != 0 ? this.array[0].getClass().getSimpleName() : "") + ">(";
    for (int i = 0; i < this.size - 1; ++i) {
      out += this.array[i] + ", ";
    }
    out += (size() != 0 ? this.array[this.size - 1] : "") + ")";
    return out;
  }

  private void makeSpace(int n, int idx) {
    int t = capacity() - this.size;
    if (n > t) {
      int n2 = capacity() * 2;
      while (n2 - this.size < n) n2 *= 2;
      T[] a2 = (T[]) (new Object[n2]);
      copy(this.array, a2, 0, idx, 0);
      copy(this.array, a2, idx, this.size - idx, idx + n);
      this.array = a2;
    } else {
      copyBackward(this.array, idx, this.size, idx + n);
    }
    this.size += n;
  }

  private static <T> void copy(T[] from, T[] to, int fromIdx, int toIdx, int offset) {
    for (int i = fromIdx; i < toIdx; ++i) {
      to[i + offset] = from[i];
    }
  }

  private static <T> void copyBackward(T[] array, int fromIdx, int toIdx, int destIdx) {
    int n = toIdx - fromIdx, d = destIdx + n;
    for (; toIdx != fromIdx; ) {
      array[--d] = array[--toIdx];
      array[toIdx] = null;
    }
  }
}
