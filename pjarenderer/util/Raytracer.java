package pjarenderer.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import pjarenderer.gui.Renderer;
import pjarenderer.gui.Viewport;
import pjarenderer.u3d.*;
import pjarenderer.u3d.Point3D;
import pjarenderer.u3d.Scene;
import pjarenderer.u3d.Vector3D;
import pjarenderer.u3d.geometry.*;

public class Raytracer extends Renderer {

  private int pixelsPerRay;

  public Raytracer(Viewport viewport) {
    super(viewport);
    this.pixelsPerRay = 6;
  }

  public void setPixelsPerRay(int n) {
    this.pixelsPerRay = n;
  }

  @Override
  public BufferedImage render(double scale) {
    int width = Math.max(1, (int) (viewport.getWidth() * scale));
    int height = Math.max(1, (int) (viewport.getHeight() * scale));

    BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    if (width == 1 || height == 1) return outImg;

    Camera cam = this.viewport.camera();
    Pair<Double, Double> pd = cam.imagePlaneLengths(width, height);
    double focal = cam.focalLength(), r = pd.first, u = pd.second;
    Vector3D campos = cam.position().toVector(),
        camdir = cam.direction(),
        upv = cam.upVector(),
        rightv = cam.rightVector();

    SceneData sd = new SceneData();
    sd.cameraPos = campos;
    sd.cameraDir = camdir;
    sd.camRightVec = rightv;
    sd.camUpVec = upv;
    sd.focal = focal;
    sd.horPlaneLen = r;
    sd.vertPlaneLen = u;
    sd.imageHeight = height;
    sd.imageWidth = width;

    int threads = Runtime.getRuntime().availableProcessors();
    renderParallel(outImg, sd, threads, 3);

    return outImg;
  }

  private class Tile {
    int x, y, w, h;
  }
  ;

  private class SceneData {
    public Vector3D cameraPos, cameraDir, camRightVec, camUpVec;
    double vertPlaneLen, horPlaneLen, focal;
    int imageWidth, imageHeight;
  }

  private class RenderThread implements Runnable {
    private SceneData sceneData;
    Tile tile;
    Color[][] frameBuf;
    boolean drawn;

    public RenderThread(SceneData sd) {
      this.sceneData = sd;
    }

    @Override
    public void run() {
      if (frameBuf == null || frameBuf.length < tile.w || frameBuf[0].length < tile.h)
        frameBuf = new Color[tile.w][tile.h];

      drawn = false;

      for (int w = 0; w < tile.w; w++) {
        for (int h = 0; h < tile.h; ) {
          if (frameBuf[w][h] != null) {
            h++;
            continue;
          }

          int px = w + tile.x, py = h + tile.y;

          Ray ray = rayThroughPixel(px, py, sceneData);
          Pair<Triangle3D, Pair<Double, Double>> hit = intersectScene(ray);
          Color pixel = Color.black;
          if (hit != null) {
            pixel = colorAtHit(hit.first, hit.second.first, hit.second.second);
          }

          for (int i = 0; i < Raytracer.this.pixelsPerRay && w + i < tile.w; ++i) {
            for (int j = 0; j < Raytracer.this.pixelsPerRay && h + j < tile.h; ++j) {
              frameBuf[w + i][h + j] = pixel;
            }
          }
          h++;
        }
      }
    }

    void draw(BufferedImage b) {
      for (int w = 0; w < tile.w; ++w) {
        for (int h = 0; h < tile.h; ++h) {
          b.setRGB(w + tile.x, b.getHeight() - (h + tile.y) - 1, frameBuf[w][h].getRGB());
          frameBuf[w][h] = null;
        }
      }
      this.drawn = true;
    }
  }

  private void renderParallel(BufferedImage bi, SceneData sd, int numThreads, int numTiles) {
    int width = bi.getWidth(), height = bi.getHeight();
    RenderThread[] runnables;
    Thread[] threads;

    DVector<Tile> tiles = this.getTiles(width, height, numTiles);

    runnables = new RenderThread[numThreads];
    threads = new Thread[numThreads];

    for (int i = 0; i < numThreads && i < tiles.size(); ++i) {
      runnables[i] = new RenderThread(sd);
      threads[i] = new Thread(runnables[i]);
      runnables[i].tile = tiles.itemAt(i);
      threads[i].start();
    }

    int cTile = numThreads;
    while (cTile < tiles.size()) {
      Tile next = tiles.itemAt(cTile);
      for (int i = 0; i < numThreads; ++i) {
        Thread thread = threads[i];
        if (!thread.isAlive()) {
          RenderThread rn = runnables[i];

          rn.draw(bi);

          rn.tile = next;
          rn.drawn = false;
          threads[i] = new Thread(rn);
          threads[i].start();
          cTile++;
          break;
        }
      }
    }

    for (Thread t : threads) {
      try {
        if (t != null) t.join();
      } catch (InterruptedException e) {

      }
    }

    for (RenderThread rt : runnables) {
      if (rt != null && !rt.drawn) {
        rt.draw(bi);
      }
    }
  }

  private DVector<Tile> getTiles(int width, int height, int numTiles) {
    int wpt = width / numTiles,
        hpt = height / numTiles,
        remWidth = width % numTiles,
        remHeight = height % numTiles;

    DVector<Tile> tiles;

    try {
      tiles = new DVector<>(numTiles * numTiles);
    } catch (DVector.BadSize e) {
      tiles = new DVector<>();
    }

    for (int w = 0; w < numTiles; ++w) {
      int ox = w * wpt;
      for (int h = 0; h < numTiles; ++h) {
        Tile t = new Tile();
        t.x = ox;
        t.y = h * hpt;
        t.w = wpt;
        t.h = hpt;
        tiles.add(t);
      }
    }

    int mainW = width - remWidth, mainH = height - remHeight;

    if (remHeight > 0)
      for (int w = 0; w < numTiles; ++w) {
        int ox = w * wpt;
        Tile t = new Tile();
        t.x = ox;
        t.y = mainH;
        t.w = wpt;
        t.h = remHeight;
        tiles.add(t);
      }

    if (remWidth > 0)
      for (int h = 0; h < numTiles; ++h) {
        int oy = h * hpt;
        Tile t = new Tile();
        t.x = mainW;
        t.y = oy;
        t.w = remWidth;
        t.h = hpt;
        tiles.add(t);
      }

    if (remWidth > 0 && remHeight > 0) {
      Tile t = new Tile();
      t.x = mainW;
      t.y = mainH;
      t.w = remWidth;
      t.h = remHeight;
      tiles.add(t);
    }

    return tiles;
  }

  public Color colorAtHit(Triangle3D triangle, double u, double v) {
    if (u >= 0.5) return triangle.c2;
    if (v >= 0.5) return triangle.c3;
    else return triangle.c1;
  }

  private Pair<Triangle3D, Pair<Double, Double>> intersectScene(Ray ray) {
    Scene scene = this.viewport.scene();

    Intersection it = scene.objectGroup().intersect(ray);

    return it == null ? null : new Pair(it.triangle, new Pair<>(it.u, it.v));
  }

  private double pixelXtoU(int px, int width) {
    return ((px + 0.5) / width) - 0.5;
  }

  private double pixelYToV(int py, int height) {
    return ((py + 0.5) / height) - 0.5;
  }

  private Point3D pixelTo3DPoint(
      int px,
      int py,
      int width,
      int height,
      double rightPlaneLen,
      double upPlaneLen,
      double focalLen,
      Vector3D cameraPos,
      Vector3D direction,
      Vector3D upVector,
      Vector3D rightVector) {
    double u = pixelXtoU(px, width), v = pixelYToV(py, height);

    Vector3D p1 = cameraPos.add(direction.mult(focalLen));
    p1 = p1.add(rightVector.mult(rightPlaneLen).mult(u));
    p1 = p1.add(upVector.mult(upPlaneLen).mult(v));

    return p1;
  }

  private Ray rayThroughPixel(int px, int py, SceneData sd) {
    Point3D ppos =
        pixelTo3DPoint(
            px,
            py,
            sd.imageWidth,
            sd.imageHeight,
            sd.horPlaneLen,
            sd.vertPlaneLen,
            sd.focal,
            sd.cameraPos,
            sd.cameraDir,
            sd.camUpVec,
            sd.camRightVec);

    return new Ray(sd.cameraPos, ppos.sub(sd.cameraPos).normalized());
  }
}
