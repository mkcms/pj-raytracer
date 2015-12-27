package pjarenderer.gui;

import java.awt.image.BufferedImage;

public abstract class Renderer {

  protected Viewport viewport;

  public Renderer(Viewport viewport) {
    this.viewport = viewport;
  }

  public abstract BufferedImage render(double scale);
}
