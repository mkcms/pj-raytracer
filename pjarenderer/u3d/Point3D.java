package pjarenderer.u3d;

public class Point3D {
  public double x, y, z;

  public Point3D() {}

  public Point3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Point3D(Point3D cp) {
    this.x = cp.x;
    this.y = cp.y;
    this.z = cp.z;
  }

  public double distance(Point3D p) {
    return Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2) + Math.pow(p.z - z, 2));
  }

  public Point3D add(Point3D p) {
    return new Point3D(x + p.x, y + p.y, z + p.z);
  }

  public Vector3D sub(Point3D p) {
    return new Vector3D(x - p.x, y - p.y, z - p.z);
  }

  public Vector3D toVector() {
    return new Vector3D(this);
  }

  @Override
  public String toString() {
    return "P(" + x + ", " + y + ", " + z + ")";
  }
}
