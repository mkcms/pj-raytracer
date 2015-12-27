package pjarenderer.u3d;

public class TargetedCamera extends Camera {
  private Point3D target;
  private double azimuth;
  private double inclination;
  private double radius;
  private double fovDeg;

  public TargetedCamera() {
    this.target = new Point3D();
    this.radius = this.azimuth = this.inclination = 0;
    this.fovDeg = 90;
  }

  public TargetedCamera(TargetedCamera cp) {
    this.target = new Point3D(cp.target);
    this.inclination = cp.inclination;
    this.azimuth = cp.azimuth;
    this.radius = cp.radius;
    this.fovDeg = cp.fovDeg;
  }

  public double azimuth() {
    return this.azimuth;
  }

  public void setAzimuth(double a) {
    this.azimuth = a % 360;
  }

  public double inclination() {
    return this.inclination;
  }

  public void setInclination(double i) {
    if (i > 90) i = 90;
    else if (i < -90) i = -90;
    this.inclination = i;
  }

  public double radius() {
    return this.radius;
  }

  public void setRadius(double r) {
    this.radius = r;
  }

  @Override
  public Point3D position() {
    double r = this.radius * Math.cos(Math.toRadians(inclination));
    double x = this.target.x + r * Math.sin(Math.toRadians(this.azimuth));
    double z = this.target.z + r * Math.cos(Math.toRadians(this.azimuth));
    double y = this.target.y + this.radius * Math.sin(Math.toRadians(inclination));
    return new Point3D(x, y, z);
  }

  @Override
  public Vector3D direction() {
    Point3D p = position();
    return this.target.sub(p).normalized();
  }

  @Override
  public double focalLength() {
    return 10;
  }

  @Override
  public double fov() {
    return this.fovDeg;
  }

  @Override
  public void setFov(double f) {
    this.fovDeg = f;
  }

  public Point3D target() {
    return this.target;
  }

  public void setTarget(Point3D p) {
    this.target = new Point3D(p);
  }

  @Override
  public Vector3D rightVector() {

    return upVector().cross(direction()).normalized();
  }

  @Override
  public Vector3D upVector() {

    TargetedCamera tmp = new TargetedCamera(this);
    tmp.inclination = this.inclination + 0.01;

    Point3D mp = this.position(), p = tmp.position();

    return p.sub(mp).normalized();
  }
}
