package pjarenderer.u3d;

import pjarenderer.util.Pair;

public abstract class Camera {

  public abstract Point3D position();

  public abstract Vector3D direction();

  public abstract Vector3D rightVector();

  public abstract Vector3D upVector();

  public abstract double focalLength();

  public abstract double fov();

  public abstract void setFov(double f);

  public Pair<Double, Double> angTan(double w, double h) {
    Pair<Double, Double> out = new Pair<>();

    double fovr = Math.toRadians(this.fov() / 2);
    double tf = Math.tan(fovr);
    if (w == h) {
      out.first = out.second = tf;
    } else if (w > h) {
      out.first = tf;
      out.second = Math.tan(h / (double) w * fovr);
    } else {
      out.first = Math.tan(w / (double) h * fovr);
      out.second = tf;
    }
    return out;
  }

  public Pair<Double, Double> imagePlaneLengths(double w, double h) {
    Pair<Double, Double> pd = angTan(w, h);
    pd.first *= 2 * this.focalLength();
    pd.second *= 2 * this.focalLength();
    return pd;
  }
}
