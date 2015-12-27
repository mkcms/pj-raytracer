package pjarenderer.u3d;

public class Vector3D extends Point3D {

  public Vector3D(double x, double y, double z) {
    super(x, y, z);
  }

  public Vector3D(Vector3D cp) {
    super(cp);
  }

  public Vector3D(Point3D p) {
    super(p);
  }

  public Vector3D normalized() {
    double l = length();
    return new Vector3D(x / l, y / l, z / l);
  }

  public Vector3D add(double k) {
    return new Vector3D(x + k, y + k, z + k);
  }

  public Vector3D add(Vector3D v) {
    return new Vector3D(x + v.x, y + v.y, z + v.z);
  }

  public Vector3D mult(double k) {
    return new Vector3D(x * k, y * k, z * k);
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public Vector3D cross(Vector3D b) {
    Vector3D a = this, out = new Vector3D(0, 0, 0);

    out.x = (a.y * b.z) - (a.z * b.y);
    out.y = (a.z * b.x) - (a.x * b.z);
    out.z = (a.x * b.y) - (a.y * b.x);

    return out;
  }

  public double dot(Vector3D b) {
    Vector3D a = this;
    double out = 0;

    out += a.x * b.x;
    out += a.y * b.y;
    out += a.z * b.z;

    return out;
  }

  @Override
  public String toString() {
    return "V(" + x + ", " + y + ", " + z + ")";
  }
}
