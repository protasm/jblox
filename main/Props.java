package jblox.main;

import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public final class Props {
  private String propsFile;
  private Properties properties;
  private List<PropsObserver> observers;

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

    observers = new ArrayList<>();
  }

  //close()
  public void close() {
    store();
  }

  //store()
  private void store() {
    try (OutputStream out = new FileOutputStream(propsFile)) {
      properties.store(out, "---No Comment---");

      out.close();
    } catch (IOException e) {
      System.err.println("Failed to store properties file '" + propsFile + "'.");
    }
  }

  //getBool(String)
  public boolean getBool(String key) {
    String property = properties.getProperty(key);

    return Boolean.parseBoolean(property);
  }

  //toggleBool(String)
  public boolean toggleBool(String key) {
    boolean newState = !getBool(key);

    properties.setProperty(key, String.valueOf(newState));

    store();

    for (PropsObserver observer : observers)
      observer.notifyPropertiesChanged();

    return newState;
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

  //registerObserver(PropsObserver)
  public void registerObserver(PropsObserver observer) {
    observers.add(observer);
  }
}
