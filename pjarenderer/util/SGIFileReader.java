package pjarenderer.util;

import java.awt.Color;
import java.io.IOException;
import java.io.RandomAccessFile;

// ftp://ftp.sgi.com/graphics/grafica/sgiimage.html
public class SGIFileReader {

  public static class BadFileFormat extends IOException {
    public BadFileFormat(String msg) {
      super(msg);
    }
  }

  private int xsize, ysize;
  private int[][] offsetTable;

  private Color[][] image;

  public SGIFileReader(String filename) throws IOException {
    RandomAccessFile stream = null;
    try {
      System.out.println("Odczytywanie pliku obrazu: \"" + filename + "\" ...");
      stream = new RandomAccessFile(filename, "r");
      readHeader(stream);
      readOffsets(stream);
      createImage(stream);
    } finally {
      if (stream != null) stream.close();
    }
  }

  public int width() {
    return this.xsize;
  }

  public int height() {
    return this.ysize;
  }

  public Color colorAt(double x, double y, boolean flip) {
    if (flip) return this.image[(int) (y * this.xsize)][(int) (x * this.ysize)];

    return this.image[(int) (x * this.xsize)][(int) (y * this.ysize)];
  }

  private void readHeader(RandomAccessFile stream) throws IOException {
    short magic = stream.readShort();
    byte format = stream.readByte(), bpc = stream.readByte();
    int dim = stream.readUnsignedShort();
    this.xsize = stream.readUnsignedShort();
    this.ysize = stream.readUnsignedShort();
    int zsize = stream.readUnsignedShort();

    int minPix = stream.readInt(), maxPix = stream.readInt(), cmap;

    stream.readInt();

    byte[] tmp = new byte[80];
    stream.readFully(tmp);

    String name = new String(tmp);
    cmap = stream.readInt();

    stream.seek(512);

    if (magic != 474)
      throw new BadFileFormat("Nieznany format pliku (identyfikator naglowka != 474)");

    if (format != 1) throw new BadFileFormat("Tylko obrazy skompresowane w RLE sa obslugiwane");

    if (zsize != 3) throw new BadFileFormat("Tylko obrazy RGB sa obslugiwane");

    if (bpc != 1) throw new BadFileFormat("Tylko format 1 bajt/komponent jest obslugiwany");

    if (cmap != 0) throw new BadFileFormat("Tylko colormapid=0 jest obslugiwany");

    System.out.println("\tFormat:" + (int) format);
    System.out.println("\tBPC: " + (int) bpc);
    System.out.println("\tDim: " + dim);
    System.out.println("\tXsize: " + xsize);
    System.out.println("\tYsize: " + ysize);
    System.out.println("\tZsize: " + zsize);
    System.out.println("\tMin piks.: " + minPix);
    System.out.println("\tMax piks.: " + maxPix);
    System.out.println("\tNazwa: " + name);
    System.out.println("\tCmap id: " + cmap);
  }

  private void readOffsets(RandomAccessFile stream) throws IOException {
    int[][] data = new int[3][this.ysize];

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < this.ysize; ++j) {
        data[i][j] = stream.readInt();
      }
    }

    this.offsetTable = data;
  }

  private Color[] readRow(RandomAccessFile stream, int y) throws IOException {
    int[] r = readChan(stream, 0, y), g = readChan(stream, 1, y), b = readChan(stream, 2, y);
    Color[] out = new Color[this.xsize];
    for (int i = 0; i < this.xsize; ++i) {
      out[i] = new Color(r[i], g[i], b[i]);
    }
    return out;
  }

  private int[] readChan(RandomAccessFile stream, int chan, int y) throws IOException {
    stream.seek(this.offsetTable[chan][y]);
    int[] out = new int[this.xsize];

    int j = 0, x = this.xsize;

    while (x > 0) {
      int b = stream.readUnsignedByte();
      int count = b & 127;
      if (count == 0) break;

      if ((b & 128) != 0) {

        for (int i = 0; i < count; ++i) {
          out[j] = stream.readUnsignedByte();
          j++;
          --x;
        }
      } else {

        int val = stream.readUnsignedByte();
        for (int i = 0; i < count; ++i) {
          out[j] = val;
          j++;
          --x;
        }
      }
    }
    return out;
  }

  private void createImage(RandomAccessFile stream) throws IOException {
    this.image = new Color[this.xsize][this.ysize];
    for (int y = 0; y < this.ysize; ++y) {
      Color[] row = readRow(stream, y);
      for (int x = 0; x < this.xsize; ++x) {
        this.image[x][y] = row[x];
      }
    }

    this.offsetTable = null;
  }
}
