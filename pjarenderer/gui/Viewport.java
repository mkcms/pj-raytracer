package pjarenderer.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import pjarenderer.u3d.Camera;
import pjarenderer.u3d.Point3D;
import pjarenderer.u3d.Scene;
import pjarenderer.u3d.TargetedCamera;
import pjarenderer.u3d.Vector3D;
import pjarenderer.util.Pair;

public class Viewport extends Canvas
    implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
  private Renderer renderer;
  private Scene scene;
  private TargetedCamera camera;
  private Pair<Integer, Integer> lastMousePosition;

  private double resolution;
  private double fastResolution;

  private boolean refreshEventPosted = false;
  private long timeRefreshEventPosted = 0;

  private int refreshEventWait = 1;

  public Viewport() {
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.addMouseWheelListener(this);
    this.addKeyListener(this);
    this.setBackground(Color.black);

    this.camera = new TargetedCamera();
    this.camera.setRadius(75);
    this.lastMousePosition = null;
    this.resolution = 0.5;
    this.fastResolution = 0.4;
    this.camera.setInclination(45);
    this.scene = new Scene();
  }

  public void resetView() {
    this.camera = new TargetedCamera(this.scene.defaultCamera());
    this.refresh();
  }

  public Scene scene() {
    return this.scene;
  }

  public void setScene(Scene s) {
    this.scene = s;
    this.camera = new TargetedCamera(s.defaultCamera());
    this.refresh();
  }

  public Renderer renderer() {
    return this.renderer;
  }

  public void setRenderer(Renderer r) {
    this.renderer = r;
  }

  public Camera camera() {
    return this.camera;
  }

  public void refresh() {
    Cursor c = this.getCursor();
    this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    this.paint(this.getGraphics());
    this.setCursor(c);
  }

  public void refresh(double res) {
    double tmp = this.resolution;
    this.resolution = res;
    this.paint(this.getGraphics());
    this.resolution = tmp;
  }

  public void postRefreshEvent(int msWaitTime) {
    this.timeRefreshEventPosted = System.currentTimeMillis();
    if (!this.refreshEventPosted) {
      this.refreshEventWait = msWaitTime;
      java.awt.EventQueue.invokeLater(
          () -> {
            if (System.currentTimeMillis() - this.timeRefreshEventPosted >= refreshEventWait) {
              this.refresh();
              this.refreshEventPosted = false;
            } else {
              this.refreshEventPosted = false;
              long t = this.timeRefreshEventPosted;
              this.postRefreshEvent(this.refreshEventWait);
              this.timeRefreshEventPosted = t;
            }
          });
      this.refreshEventPosted = true;
    }
  }

  public void setResolution(double r) {
    this.resolution = r;
    this.postRefreshEvent(500);
  }

  public void setFastResolution(double r) {
    this.fastResolution = r;
  }

  @Override
  public void paint(Graphics g) {
    long tstart = System.currentTimeMillis(), tend;
    BufferedImage b = this.renderer.render(this.resolution);
    tend = System.currentTimeMillis();

    g.clearRect(0, 0, this.getWidth(), this.getHeight());
    g.drawImage(b, 0, 0, this.getWidth(), this.getHeight(), this);
    g.setColor(Color.yellow);
    g.drawString("Azymut: " + this.camera.azimuth(), 10, 10);
    g.drawString("Nachylenie: " + this.camera.inclination(), 10, 30);
    g.drawString("Odleglosc: " + this.camera.radius(), 10, 50);
    g.drawString("Kat widzenia: " + this.camera.fov(), 10, 70);
    g.drawString("Fps: " + (int) (1000.0 / (tend - tstart)), 10, 90);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
      this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
    } else if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {
      this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
      this.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    this.lastMousePosition = null;
    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (this.lastMousePosition == null) {
      this.lastMousePosition = new Pair(e.getX(), e.getY());
    } else {
      int x = e.getX(),
          y = e.getY(),
          lx = this.lastMousePosition.first,
          ly = this.lastMousePosition.second;

      int dx = x - lx, dy = y - ly;
      if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {

        this.camera.setAzimuth(this.camera.azimuth() + Math.toRadians(dx * 4));
        this.camera.setInclination(this.camera.inclination() + Math.toRadians(dy * 4));
      } else if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {

        Point3D p = this.camera.target();
        Vector3D up = this.camera.upVector(), right = this.camera.rightVector();

        double m = this.camera.radius() * 0.5;

        p = p.add(up.mult(dy * 0.01 * m)).add(right.mult(-dx * 0.01 * m));
        this.camera.setTarget(p);

      } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {

        this.camera.setRadius(this.camera.radius() * (1 - ((double) -dy / this.getHeight())));
      }
      if (dx != 0 || dy != 0) {
        this.refresh(this.fastResolution);
        this.postRefreshEvent(1000);
      }
      this.lastMousePosition = new Pair(x, y);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {

    boolean repaint = true;

    switch (e.getKeyCode()) {
      case KeyEvent.VK_DOWN:
        this.camera.setFov(this.camera.fov() - 1);
        break;
      case KeyEvent.VK_UP:
        this.camera.setFov(this.camera.fov() + 1);
        break;
      default:
        repaint = false;
    }
    if (repaint) {
      this.refresh(this.fastResolution);
      this.postRefreshEvent(1000);
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {

    if (e.getWheelRotation() < 0) this.camera.setRadius(this.camera.radius() * 0.99);
    else {
      this.camera.setRadius(this.camera.radius() * 1.01);
    }
    this.refresh(this.fastResolution);
    this.postRefreshEvent(1000);
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {}

  public void keyTyped(KeyEvent e) {}

  public void keyReleased(KeyEvent e) {}
}
