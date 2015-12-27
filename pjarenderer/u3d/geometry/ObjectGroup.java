package pjarenderer.u3d.geometry;

import pjarenderer.u3d.Point3D;
import pjarenderer.util.DVector;
import pjarenderer.util.Intersection;
import pjarenderer.util.Ray;

public class ObjectGroup implements Intersectable {
  private DVector<Intersectable> subObjects = new DVector<>();
  public Box3D boundingBox;

  public DVector<Intersectable> subObjects() {
    return this.subObjects;
  }

  @Override
  public Intersection intersect(Ray r) {
    if (r.intersectBox(this.boundingBox()) == null) return null;

    Intersection out = null;

    double min = Double.MAX_VALUE;

    for (int i = 0; i < this.subObjects.size(); ++i) {
      Intersectable is = this.subObjects.itemAt(i);

      Intersection temp = is.intersect(r);
      if (temp != null && temp.t < min) {
        min = temp.t;
        out = temp;
      }
    }
    return out;
  }

  @Override
  public Box3D boundingBox() {
    if (this.boundingBox == null) calcBoundingBox();

    return this.boundingBox;
  }

  @Override
  public void calcBoundingBox() {
    Point3D min = new Point3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
        max = new Point3D(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);

    int n = 0;
    for (int t = 0; t < this.subObjects.size(); ++t) {
      Intersectable it = this.subObjects.itemAt(t);
      it.calcBoundingBox();
      Box3D i = it.boundingBox();

      if (i.min.x < min.x) min.x = i.min.x;
      if (i.min.y < min.y) min.y = i.min.y;
      if (i.min.z < min.z) min.z = i.min.z;

      if (i.max.x > max.x) max.x = i.max.x;
      if (i.max.y > max.y) max.y = i.max.y;
      if (i.max.z > max.z) max.z = i.max.z;
      n++;
    }

    this.boundingBox = new Box3D();
    this.boundingBox.min = n == 0 ? new Point3D() : min;
    this.boundingBox.max = n == 0 ? new Point3D() : max;
  }
}
