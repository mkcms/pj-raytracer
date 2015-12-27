package pjarenderer.gui;

import java.awt.Button;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import pjarenderer.u3d.Scene;
import pjarenderer.util.CyberwareFileReader;
import pjarenderer.util.Raytracer;
import pjarenderer.util.SGIFileReader;

public class Window extends Frame {

  private static int defaultRes = 50, defaultFastRes = 40, defaultPPR = 4, defaultMeshRes = 95;

  private class UI extends Panel {
    Scrollbar scrollResolution;
    Label labelResolution;
    Scrollbar scrollFastRes;
    Label labelFastRes;
    Scrollbar scrollPixelsPerRay;
    Label labelPixelsPerRay;
    Scrollbar scrollMeshRes;
    Label labelMeshRes;
    Button buttonLoadMesh;
    Button buttonResetView;

    public void setup() {
      this.setLayout(new GridBagLayout());
      GridBagConstraints constr = new GridBagConstraints();

      double r = defaultRes / 100.0, r2 = defaultFastRes / 100.0;
      int ppr = defaultPPR, mr = defaultMeshRes;

      this.scrollResolution = new Scrollbar(Scrollbar.HORIZONTAL, defaultRes, 1, 1, 101);
      this.labelResolution = new Label("Rozdzielczosc: " + r + "   ");
      this.scrollFastRes = new Scrollbar(Scrollbar.HORIZONTAL, defaultFastRes, 1, 1, 101);
      this.labelFastRes = new Label("Szyb. rozdz.: " + r2 + "   ");
      this.scrollPixelsPerRay = new Scrollbar(Scrollbar.HORIZONTAL, defaultPPR, 1, 1, 6);
      this.labelPixelsPerRay = new Label("Piks./ promien: " + ppr + " ");
      this.scrollMeshRes = new Scrollbar(Scrollbar.HORIZONTAL, defaultMeshRes, 1, 1, 101);
      this.labelMeshRes = new Label("Rozdz. siatki: " + mr + "%  ");
      this.buttonLoadMesh = new Button("Przeladuj model");
      this.buttonResetView = new Button("Resetuj widok");

      constr.weightx = 1.0;
      constr.gridx = 0;
      this.add(this.labelResolution, constr);
      constr.gridx = 1;
      this.add(this.scrollResolution, constr);
      constr.gridx = 0;
      constr.gridy = 1;
      this.add(this.labelFastRes, constr);
      constr.gridx = 1;
      this.add(this.scrollFastRes, constr);
      constr.gridx = 0;
      constr.gridy = 2;
      this.add(this.labelPixelsPerRay, constr);
      constr.gridx = 1;
      this.add(this.scrollPixelsPerRay, constr);
      constr.gridy = 3;
      constr.gridx = 0;
      this.add(this.labelMeshRes, constr);
      constr.gridx = 1;
      this.add(this.scrollMeshRes, constr);
      constr.gridx = 0;
      constr.gridy = 4;
      constr.gridwidth = 2;
      constr.weightx = 1.0;
      this.add(this.buttonLoadMesh, constr);
      constr.gridy = 5;
      constr.gridx = 0;
      this.add(this.buttonResetView, constr);
      constr.weightx = 0.0;
      constr.gridy = 6;
      constr.anchor = GridBagConstraints.WEST;
      this.add(new Label("LPM - Obrot"), constr);
      constr.gridy = 7;
      this.add(new Label("PPM/Kolko - Zblizenie"), constr);
      constr.gridy = 8;
      this.add(new Label("SPM - Kadrowanie"), constr);
      constr.gridy = 9;
      this.add(new Label("Strzalki g/d - Zmiana kata widzenia"), constr);

      this.scrollResolution.addAdjustmentListener(
          (AdjustmentEvent e) -> {
            double val = Math.max(0.1, e.getValue() / 100.0);
            this.labelResolution.setText("Rozdzielczosc: " + val);
            Window.this.viewport.setResolution(Math.max(0.1, e.getValue() / 100.0));
          });

      this.scrollFastRes.addAdjustmentListener(
          (AdjustmentEvent e) -> {
            double val = Math.max(0.1, e.getValue() / 100.0);
            this.labelFastRes.setText("Szyb. rozdz.: " + val);
            Window.this.viewport.setFastResolution(Math.max(0.05, e.getValue() / 100.0));
          });

      this.scrollPixelsPerRay.addAdjustmentListener(
          (AdjustmentEvent e) -> {
            this.labelPixelsPerRay.setText("Piks./ promien: " + e.getValue());
            Window.this.viewportRenderer.setPixelsPerRay(e.getValue());
            Window.this.viewport.postRefreshEvent(1000);
          });

      this.scrollMeshRes.addAdjustmentListener(
          (AdjustmentEvent e) -> {
            this.labelMeshRes.setText("Rozdz. siatki: " + e.getValue() + "%");
          });

      this.buttonLoadMesh.addActionListener(
          (ActionEvent e) -> {
            Window.this.reloadModel(Math.max(100 - this.scrollMeshRes.getValue(), 1));
          });

      this.buttonResetView.addActionListener(
          (ActionEvent e) -> {
            Window.this.viewport.resetView();
          });
    }
  }

  private Viewport viewport;
  private Raytracer viewportRenderer;
  private UI ui;
  private String[] args;
  private CyberwareFileReader model;

  public Window(String[] args) {
    this.viewport = new Viewport();
    this.viewportRenderer = new Raytracer(this.viewport);

    this.viewport.setRenderer(this.viewportRenderer);
    this.args = args;
  }

  public void show(int width, int height) throws IOException {
    this.setLayout(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();

    constr.gridx = 0;
    constr.gridy = 0;
    constr.anchor = GridBagConstraints.NORTHWEST;
    constr.fill = GridBagConstraints.BOTH;
    constr.weighty = 1.0;
    constr.weightx = 0.9;
    this.viewport.setSize(width, height);
    this.add(viewport, constr);
    this.ui = new UI();
    this.ui.setup();
    constr.gridx = 1;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.fill = GridBagConstraints.HORIZONTAL;
    this.add(ui, constr);
    constr.gridy = 1;
    constr.gridwidth = 2;
    constr.anchor = GridBagConstraints.EAST;
    this.add(new Label("Michal Krzywkowski PJWSTK 2015"), constr);

    this.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });

    this.model = new CyberwareFileReader(this.args[0]);

    if (this.args.length > 1)
      try {
        this.model.setSGIImage(new SGIFileReader(this.args[1]));
      } catch (IOException e) {
        System.err.println("Blad podczas wczytywania pliku obrazu: " + this.args[1]);
        System.err.println("Blad: " + e.getMessage());
      }

    this.viewport.setResolution(defaultRes / 100.0);
    this.viewport.setFastResolution(defaultFastRes / 100.0);
    this.viewportRenderer.setPixelsPerRay(defaultPPR);

    this.setTitle(this.model.name() + " (" + args[0] + ") - pjarenderer");
    this.setSize(width, height);
    this.setVisible(true);
    this.reloadModel(100 - defaultMeshRes);
  }

  private void reloadModel(int res) {
    Cursor c = this.getCursor();
    this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    Scene s = this.model.generateScene(res);
    this.viewport.setScene(s);
    this.setCursor(c);
  }
}
