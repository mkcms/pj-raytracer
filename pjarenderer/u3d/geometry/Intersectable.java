package pjarenderer.u3d.geometry;

import pjarenderer.util.Intersection;
import pjarenderer.util.Ray;

public interface Intersectable {

  public Intersection intersect(Ray r);

  public Box3D boundingBox();

  public void calcBoundingBox();
}
