package jblox.main;

import jblox.debug.Debugger;

public abstract class PropsObserver {
  protected Props properties;
  protected Debugger debugger;

  protected abstract void updateCachedProperties();

  //PropsObserver(Props, Debugger)
  public PropsObserver(Props properties, Debugger debugger) {
    this.properties = properties;
    this.debugger = debugger;

    properties.registerObserver(this);

    updateCachedProperties();
  }

  //notifyPropertiesChanged()
  public void notifyPropertiesChanged() {
    updateCachedProperties();
  }
}
