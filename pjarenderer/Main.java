package pjarenderer;

import java.io.IOException;
import pjarenderer.util.CyberwareFileReader;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      args = new String[] {"glowa", "glowa.color"};
    }

    try {
      new pjarenderer.gui.Window(args).show(700, 400);
    } catch (CyberwareFileReader.BadFileFormat e) {
      System.err.println("Blad podczas odczytu pliku modelu: " + e.getMessage());
      System.err.println("... w pliku \"" + args[0] + '\"');
      System.exit(1);
    }
  }
}
