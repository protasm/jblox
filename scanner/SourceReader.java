package jblox.scanner;

import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.main.PropsObserver;

public abstract class SourceReader extends PropsObserver {
  protected String source;
  protected int current;

  //SourceReader(Props, Debugger)
  public SourceReader(Props properties, Debugger debugger) {
    super(properties, debugger);
  }

  //setSource(String)
  protected void setSource(String source) {
    this.source = source;

    current = 0;
  }

  //match()
  protected boolean match(char expected) {
    if (isAtEnd()) return false;

    if (source.charAt(current) != expected) return false;

    current++;

    return true;
  }

  //peek()
  protected char peek() {
    if (isAtEnd()) return '\0';

    return source.charAt(current);
  }

  //peekNext()
  protected char peekNext() {
    if (current + 1 >= source.length()) return '\0';

    return source.charAt(current + 1);
  }

  //isAlpha(char)
  protected boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  //isAlphaNumeric(char)
  protected boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  //isDigit(char)
  protected boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  //isAtEnd()
  protected boolean isAtEnd() {
    return current >= source.length();
  }

  //advance()
  protected char advance() {
    return source.charAt(current++);
  }
}
