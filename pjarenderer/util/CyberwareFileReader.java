package pjarenderer.util;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import pjarenderer.u3d.Point3D;
import pjarenderer.u3d.Scene;
import pjarenderer.u3d.geometry.Object3D;
import pjarenderer.u3d.geometry.ObjectGroup;
import pjarenderer.u3d.geometry.Triangle3D;

public class CyberwareFileReader {

  public static class BadFileFormat extends IOException {

    public BadFileFormat(String msg) {
      super(msg);
    }
  }

  private HashMap<String, Double> header;
  private SGIFileReader image;
  private short[][] data;

  private String name;
  private boolean flipImage;

  public CyberwareFileReader(String modelFile) throws IOException {
    FileInputStream stream = null;
    try {
      System.out.println("Odczytywanie pliku modelu: \"" + modelFile + "\" ...");
      stream = new FileInputStream(modelFile);
      readHeader(stream);
      readData(stream);
    } finally {
      if (stream != null) stream.close();
    }
  }

  public String name() {
    return this.name == null ? "" : this.name;
  }

  public void setSGIImage(SGIFileReader fr) {
    this.image = fr;
    this.flipImage = this.header.get("NLT") == fr.width();
  }

  public Scene generateScene(int res) {
    Scene out = new Scene();
    Object3D obj = new Object3D();

    int nlg = header.get("NLG").intValue(),
        nlt = header.get("NLT").intValue(),
        rshift = header.get("RSHIFT").intValue();
    double ltincr = header.get("LTINCR"), scale = header.get("SCALE"), rprop = header.get("RPROP");

    double deltaY = ltincr / scale;

    double ang = (2 * Math.PI) / nlg;

    int skip = res;
    for (int lg = 0; lg < nlg; lg += skip) {
      for (int lt = 0; lt < nlt; lt += skip) {
        int nextlat = (lt + skip) % nlt, nextlon = (lg + skip) % nlg;

        Point3D cur = pointAtLonLat(lg, lt, deltaY, ang, rshift, rprop),
            up = pointAtLonLat(lg, nextlat, deltaY, ang, rshift, rprop),
            right = pointAtLonLat(nextlon, lt, deltaY, ang, rshift, rprop),
            rightUp = pointAtLonLat(nextlon, nextlat, deltaY, ang, rshift, rprop);

        Color curc = colorAtLonLat(lg, lt, nlt, nlg),
            upc = colorAtLonLat(lg, nextlat, nlt, nlg),
            rightc = colorAtLonLat(nextlon, lt, nlt, nlg),
            rightUpc = colorAtLonLat(nextlon, nextlat, nlt, nlg);

        Triangle3D t1 = new Triangle3D(cur, up, rightUp, Color.white),
            t2 = new Triangle3D(rightUp, right, cur, Color.white);

        t1.c1 = curc;
        t1.c2 = upc;
        t1.c3 = rightUpc;
        t2.c1 = rightUpc;
        t2.c2 = rightc;
        t2.c3 = curc;

        obj.triangles().add(t1);
        obj.triangles().add(t2);
      }
    }

    ObjectGroup og = BVHTree.generate(obj);

    double s = og.boundingBox().max.distance(og.boundingBox().center());

    out.setObjectGroup(og);
    out.defaultCamera().setRadius(1.3 * s);
    out.defaultCamera().setTarget(og.boundingBox().center());

    return out;
  }

  private void readHeader(FileInputStream stream) throws IOException {
    if (readLine(stream).compareTo("Cyberware Digitizer Data") != 0)
      throw new BadFileFormat("Nieznany naglowek pliku");

    header = new HashMap<>();

    HashSet<String> recognizedKeywords =
        new HashSet<>(
            Arrays.asList(
                new String[] {
                  "NAME", "DATE", "SPACE", "COLOR", "LGINCR", "LGMIN", "LGMAX", "LTMIN", "LTMAX",
                  "RMIN", "RMAX", "LGSHIFT", "DATA", "NLG", "NLT", "LTINCR", "SCALE", "RSHIFT",
                  "RPROP",
                }));

    HashSet<String> nonNumericKeywords =
        new HashSet<>(Arrays.asList(new String[] {"NAME", "DATE", "SPACE", "COLOR"}));

    int necessaryKeywords = 0;
    String line;
    while (true) {
      line = readLine(stream);
      if (line == null) throw new BadFileFormat("Nieoczekiwany koniec pliku");

      if (line.compareTo("DATA=") == 0) break;

      Pair<String, String> pss;
      try {
        pss = split(line);
      } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
        throw new BadFileFormat("Zly format naglowka : " + line);
      }

      String kw = pss.first;

      if (!recognizedKeywords.contains(kw)) {
        throw new BadFileFormat("Nierozpoznane slowo kluczowe w naglowku: " + kw);
      }
      if (nonNumericKeywords.contains(kw)) {
        if (kw.compareTo("SPACE") == 0 && pss.second.compareTo("CYLINDRICAL") != 0)
          throw new BadFileFormat("Typ skanu (" + pss.second + ") nie jest obslugiwany");
        else if (kw.compareTo("NAME") == 0) this.name = pss.second;

      } else {
        try {
          header.put(kw, Double.parseDouble(pss.second));
        } catch (NumberFormatException e) {
          throw new BadFileFormat("Zly format liczby w naglowku: " + pss.second);
        }

        switch (kw) {
          case "NLG":
            necessaryKeywords |= 1;
            break;
          case "NLT":
            necessaryKeywords |= 1 << 1;
            break;
          case "LTINCR":
            necessaryKeywords |= 1 << 2;
            break;
          case "RSHIFT":
            necessaryKeywords |= 1 << 3;
            break;
          case "SCALE":
            necessaryKeywords |= 1 << 4;
            break;
          case "RPROP":
            necessaryKeywords |= 1 << 5;
            break;
          default:
            break;
        }
      }
    }

    if (necessaryKeywords != 63) {
      throw new BadFileFormat("Brak wszystkich wymaganych danych w pliku");
    }

    header.entrySet().stream()
        .forEach(
            (me) -> {
              System.out.println("\t" + me.getKey() + " : " + me.getValue());
            });
  }

  private void readData(FileInputStream stream) throws IOException {
    int nlg = header.get("NLG").intValue(), nlt = header.get("NLT").intValue();

    data = new short[nlg][nlt];

    byte[] temp = new byte[2];
    for (int i = 0; i < nlg; ++i) {
      for (int j = 0; j < nlt; ++j) {
        int read = stream.read(temp);
        short radius;

        if (read == -1) throw new BadFileFormat("Nieoczekiwany koniec pliku");

        radius = ByteBuffer.wrap(temp).getShort();

        data[i][j] = radius;
      }
    }
  }

  private Pair<String, String> split(String s) {
    String[] a = s.split("=");
    Pair<String, String> out = new Pair<>();
    out.first = a[0];
    out.second = a[1];
    return out;
  }

  private String readLine(FileInputStream stream) throws IOException {
    StringBuilder sb = new StringBuilder();
    boolean eof = false;
    while (true) {
      int i = stream.read();
      if (i == -1) {
        eof = true;
        break;
      }
      char c = (char) i;
      if (c == '\n' || c == '\r') break;
      sb.append(c);
    }
    return eof && sb.length() == 0 ? null : sb.toString();
  }

  private double radiusAtLonLat(int lon, int lat, int rshift, double rprop) {
    short d = data[lon][lat];
    double radius;

    if (d < 0) radius = 0;
    else {
      radius = (((long) d) << rshift) / rprop;
    }

    return radius;
  }

  private Point3D pointAtLonLat(
      int lon, int lat, double deltaY, double deltaAng, int rshift, double rprop) {
    Point3D out = new Point3D();
    double r = radiusAtLonLat(lon, lat, rshift, rprop), ang = lon * deltaAng;

    out.y = lat * deltaY;
    out.x = r * Math.cos(ang);
    out.z = r * Math.sin(ang);

    return out;
  }

  private Color colorAtLonLat(int lon, int lat, int nlt, int nlg) {
    if (this.image == null) return Color.white;

    double x = (double) lon / nlg, y = (double) lat / nlt;

    return this.image.colorAt(x, y, this.flipImage);
  }
}
