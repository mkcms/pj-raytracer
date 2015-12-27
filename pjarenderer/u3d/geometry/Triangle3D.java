package pjarenderer.u3d.geometry;

import java.awt.Color;
import pjarenderer.u3d.Point3D;

public class Triangle3D {
  public Point3D p1, p2, p3;
  public Color c1, c2, c3;

  public Triangle3D() {
    p1 = new Point3D();
    p2 = new Point3D();
    p3 = new Point3D();
    c1 = Color.white;
    c2 = Color.white;
    c3 = Color.white;
  }

  public Triangle3D(Point3D p1, Point3D p2, Point3D p3, Color c) {
    this.p1 = new Point3D(p1);
    this.p2 = new Point3D(p2);
    this.p3 = new Point3D(p3);
    this.c1 = new Color(c.getRGB());
    this.c2 = new Color(c.getRGB());
    this.c3 = new Color(c.getRGB());
  }

  public Triangle3D(Triangle3D cp) {
    this.p1 = new Point3D(cp.p1);
    this.p2 = new Point3D(cp.p2);
    this.p3 = new Point3D(cp.p3);
    this.c1 = new Color(cp.c1.getRGB());
    this.c2 = new Color(cp.c2.getRGB());
    this.c3 = new Color(cp.c3.getRGB());
  }

  public Point3D[] points() {
    return new Point3D[] {p1, p2, p3};
  }

  public Point3D center() {
    Point3D out = new Point3D();

    out.x = (p1.x + p2.x + p3.x) / 3.0;
    out.y = (p1.y + p2.y + p3.y) / 3.0;
    out.z = (p1.z + p2.z + p3.z) / 3.0;

    return out;
  }
}
