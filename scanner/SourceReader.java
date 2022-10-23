package jblox.scanner;

import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.main.PropsObserver;

public abstract class SourceReader extends PropsObserver {
  protected static final char EOL = '\n';
  private static final char NULL_CHAR = '\0';
  private String source;
  private int head;
  private int tail;
  private int line;

  //SourceReader(Props, Debugger)
  public SourceReader(Props properties, Debugger debugger) {
    super(properties, debugger);
  }

  //initialize(String)
  protected void initialize(String source) {
    this.source = source;

    head = 0;
    tail = 0;
    line = 1;
  }

  //source()
  protected String source() {
    return source;
  }

  //head()
  protected int head() {
    return head;
  }

  //tail()
  protected int tail() {
    return tail;
  }

  //line()
  protected int line() {
    return line;
  }

  //sync()
  protected void sync() {
    tail = head;
  }

  //match()
  protected boolean match(char expected) {
    if (peek() != expected) return false;

    advance();

    return true;
  }

  //peek()
  protected char peek() {
    if (isAtEnd()) return NULL_CHAR;

    return source.charAt(head);
  }

  //peekNext()
  protected char peekNext() {
    if (head + 1 >= source.length()) return NULL_CHAR;

    return source.charAt(head + 1);
  }

  //peekPrev()
  protected char peekPrev() {
    if (isAtStart() || isAtEnd()) return NULL_CHAR;

    return source.charAt(head - 1);
  }

  //isWhitespace(char)
  protected boolean isWhitespace(char c) {
    return (c == ' ') || (c == '\r') || (c == '\t');
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

  //isAtStart()
  protected boolean isAtStart() {
    return head == 0;
  }

  //isAtEnd()
  protected boolean isAtEnd() {
    return head >= source.length();
  }

  //advance()
  protected void advance() {
    if (peek() == EOL) line++;

    head++;
  }

  //peekAndAdvance()
  protected char peekAndAdvance() {
    char c = peek();

    advance();

    return c;
  }

  //read()
  protected String read() {
    // Read string from tail (inclusive) through
    // head (exclusive);
    // E.g. "foobar".substring(2, 5) == "oba".
    // If (tail == head) then this will return a
    // zero-length string.
    return source.substring(tail, head);
  }

  //readTrimmed()
  protected String readTrimmed() {
    return source.substring(tail + 1, head - 1);
  }

  //seek(char)
  protected boolean seek(char c) {
    while (peek() != c && !isAtEnd())
      advance();

    return !isAtEnd();
  }
}
