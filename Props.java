package jblox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public final class Props {
  private String propsFile;
  private Properties properties;

  //Props(String)
  public Props(String propsFile) {
    this.propsFile = propsFile;

    try (InputStream input = new FileInputStream(propsFile)) {
      properties = new Properties();

      properties.load(input);

      input.close();
    } catch (IOException e) {
      System.err.println("Failed to load properties file '" + propsFile + "'.");
    }
  }

  //close()
  public void close() {
    try (OutputStream out = new FileOutputStream("props")) {
      properties.store(out, "---No Comment---");

      out.close();
    } catch (IOException e) {
      System.err.println("Failed to close properties file '" + propsFile + "'.");
    }
  }

  //getBool(String)
  public boolean getBool(String key) {
    String property = properties.getProperty(key);

    return Boolean.parseBoolean(property);
  }

  //getInt(String)
  public int getInt(String key) {
    String property = properties.getProperty(key);

    if (property == null) return -1;

    try {
      return Integer.parseInt(property);
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
