package pjarenderer.util;

import pjarenderer.u3d.Point3D;
import pjarenderer.u3d.geometry.Box3D;
import pjarenderer.u3d.geometry.Object3D;
import pjarenderer.u3d.geometry.ObjectGroup;
import pjarenderer.u3d.geometry.Triangle3D;

public class BVHTree {

  public static ObjectGroup generate(Object3D obj) {
    ObjectGroup out = new ObjectGroup();
    obj.calcBoundingBox();
    Box3D bbox = obj.boundingBox();
    int depth = (int) (Math.log(obj.triangles().size()) / Math.log(2));

    for (int i = 0; i < obj.triangles().size(); ++i) {
      Triangle3D tri = obj.triangles().itemAt(i);
      Box3D temp = new Box3D(bbox);
      addToTree(tri, temp, out, 0, depth);
      if (i % 1000 == 0) {
        System.out.format(
            "Generowanie drzewa BVH...: %.2f %%\n", (100 * (double) i / obj.triangles().size()));
      }
    }

    out.boundingBox = bbox;

    return out;
  }

  private static void addToTree(
      Triangle3D triangle, Box3D bbox, ObjectGroup tree, int axis, int depth) {
    if (depth <= 0) {
      if (tree.subObjects().size() == 0) tree.subObjects().add(new Object3D());

      Object3D obj = (Object3D) tree.subObjects().itemAt(0);

      obj.triangles().add(triangle);
      obj.calcBoundingBox();

    } else {

      Box3D f = null, s = null;
      boolean first = false, second = false;
      for (Point3D p : triangle.points()) {
        Box3D temp = new Box3D(bbox);

        int cell = pointCell(p, temp, axis);
        if (cell == 0) {
          first = true;
          f = temp;
        } else if (cell == 1) {
          second = true;
          s = temp;
        }
      }

      if (first) {
        if (tree.subObjects().size() == 0) tree.subObjects().add(new ObjectGroup());

        ObjectGroup left = (ObjectGroup) tree.subObjects().itemAt(0);

        addToTree(triangle, f, left, (axis + 1) % 3, depth - 1);
        left.boundingBox = f;
      }

      if (second) {
        while (tree.subObjects().size() < 2) tree.subObjects().add(new ObjectGroup());

        ObjectGroup right = (ObjectGroup) tree.subObjects().itemAt(1);
        addToTree(triangle, s, right, (axis + 1) % 3, depth - 1);
        right.boundingBox = s;
      }
    }
  }

  private static int pointCell(Point3D point, Box3D bbox, int axis) {
    Point3D center = bbox.center();

    double x = bbox.xLength() / 2, y = bbox.yLength() / 2, z = bbox.zLength() / 2;

    int ret = 1;
    switch (axis) {
      case 0:
        {
          if (point.x > center.x) bbox.min.x += x;
          else {
            ret = 0;
            bbox.max.x -= x;
          }
          break;
        }
      case 1:
        {
          if (point.y > center.y) bbox.min.y += y;
          else {
            ret = 0;
            bbox.max.y -= y;
          }
          break;
        }
      case 2:
        {
          if (point.z > center.z) bbox.min.z += z;
          else {
            ret = 0;
            bbox.max.z -= z;
          }
          break;
        }
    }
    return ret;
  }
}
