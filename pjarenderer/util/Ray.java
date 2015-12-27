package pjarenderer.util;

import pjarenderer.u3d.*;
import pjarenderer.u3d.geometry.Box3D;
import pjarenderer.u3d.geometry.Triangle3D;

public class Ray {
  public Point3D origin;
  public Vector3D direction;

  public Ray(Point3D origin, Vector3D direction) {
    this.origin = origin;
    this.direction = direction;
  }

  public Ray() {
    this(null, null);
  }

  public Intersection intersectTriangle(Triangle3D triangle) {
    double epsilon = 0.000001;

    Vector3D dir = this.direction, edge1, edge2, tvec, pvec, qvec;
    Point3D p0 = triangle.p1, p1 = triangle.p2, p2 = triangle.p3;
    double t, u, v, det, inv_det;

    edge1 = p1.sub(p0);
    edge2 = p2.sub(p0);

    pvec = dir.cross(edge2);

    det = edge1.dot(pvec);

    if (det > -epsilon && det < epsilon) return null;

    inv_det = 1.0 / det;

    tvec = this.origin.sub(p0);
    u = tvec.dot(pvec) * inv_det;
    if (u < 0 || u > 1) return null;

    qvec = tvec.cross(edge1);
    v = dir.dot(qvec) * inv_det;

    if (v < 0 || (u + v) > 1) return null;

    t = edge2.dot(qvec) * inv_det;

    Intersection it = new Intersection();
    it.triangle = triangle;
    it.t = t;
    it.u = u;
    it.v = v;
    return it;
  }

  public Intersection intersectBox(Box3D box) {
    Vector3D df = new Vector3D(0, 0, 0);
    df.x = 1.0 / this.direction.x;
    df.y = 1.0 / this.direction.y;
    df.z = 1.0 / this.direction.z;

    double t1 = (box.min.x - this.origin.x) * df.x,
        t2 = (box.max.x - this.origin.x) * df.x,
        t3 = (box.min.y - this.origin.y) * df.y,
        t4 = (box.max.y - this.origin.y) * df.y,
        t5 = (box.min.z - this.origin.z) * df.z,
        t6 = (box.max.z - this.origin.z) * df.z;

    double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
    double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

    if (tmax < 0 || tmin > tmax) return null;

    Intersection k = new Intersection();
    k.t = tmin;
    return k;
  }
}
