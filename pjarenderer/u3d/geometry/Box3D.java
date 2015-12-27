package pjarenderer.u3d.geometry;

import pjarenderer.u3d.Point3D;

public class Box3D {
  public Point3D min, max;

  public Box3D() {
    this.min = new Point3D(0, 0, 0);
    this.max = new Point3D(0, 0, 0);
  }

  public Box3D(Box3D cp) {
    this.min = new Point3D(cp.min);
    this.max = new Point3D(cp.max);
  }

  public Point3D center() {
    Point3D out = new Point3D();

    out.x = max.x - (xLength() / 2);
    out.y = max.y - (yLength() / 2);
    out.z = max.z - (zLength() / 2);

    return out;
  }

  public double xLength() {
    return Math.abs(max.x - min.x);
  }

  public double yLength() {
    return Math.abs(max.y - min.y);
  }

  public double zLength() {
    return Math.abs(max.z - min.z);
  }
}
