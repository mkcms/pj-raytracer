package pjarenderer.u3d;

import pjarenderer.u3d.geometry.ObjectGroup;

public class Scene {
  private ObjectGroup objectGroup;
  private TargetedCamera defaultCamera;

  public Scene() {
    this.defaultCamera = new TargetedCamera();
    this.objectGroup = new ObjectGroup();
  }

  public ObjectGroup objectGroup() {
    return this.objectGroup;
  }

  public void setObjectGroup(ObjectGroup og) {
    this.objectGroup = og;
  }

  public TargetedCamera defaultCamera() {
    return this.defaultCamera;
  }
}
