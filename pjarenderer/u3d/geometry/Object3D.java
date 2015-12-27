package pjarenderer.u3d.geometry;

import pjarenderer.u3d.Point3D;
import pjarenderer.util.DVector;
import pjarenderer.util.Intersection;
import pjarenderer.util.Ray;

public class Object3D implements Intersectable {
  private DVector<Triangle3D> triangles = new DVector();
  private Box3D boundingBox;

  public DVector<Triangle3D> triangles() {
    return this.triangles;
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
    for (int t = 0; t < this.triangles.size(); ++t) {
      Triangle3D tri = this.triangles.itemAt(t);

      for (Point3D p : tri.points()) {
        if (p.x < min.x) min.x = p.x;
        if (p.y < min.y) min.y = p.y;
        if (p.z < min.z) min.z = p.z;

        if (p.x > max.x) max.x = p.x;
        if (p.y > max.y) max.y = p.y;
        if (p.z > max.z) max.z = p.z;
      }
      n++;
    }

    this.boundingBox = new Box3D();
    this.boundingBox.min = n == 0 ? new Point3D() : min;
    this.boundingBox.max = n == 0 ? new Point3D() : max;
  }

  @Override
  public Intersection intersect(Ray r) {
    if (r.intersectBox(this.boundingBox()) == null) return null;

    Intersection out = null;
    double min = Double.MAX_VALUE;

    for (int t = 0; t < this.triangles.size(); ++t) {
      Triangle3D tri = this.triangles.itemAt(t);

      Intersection temp = r.intersectTriangle(tri);
      if (temp != null && temp.t < min) {
        out = temp;
        min = temp.t;
      }
    }
    return out;
  }
}
