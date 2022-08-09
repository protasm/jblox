package jblox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Defaults {
  private Properties properties;

  //Defaults(String)
  public Defaults(String propsFile) {
    try (InputStream input = new FileInputStream(propsFile)) {
      properties = new Properties();

      properties.load(input);
    } catch (IOException e) {
      System.err.println("Failed to load properties file '" + propsFile + "'.");
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
