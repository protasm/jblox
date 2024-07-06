package scanner;

public class ScannableSource {
  private static final char EOL = '\n';
  private static final char NULL_CHAR = '\0';
  private String source;
  private int head, tail;
  private int line;

  //ScannableSource(String)
  public ScannableSource(String source) {
    this.source = source;

    reset();
  }

  //reset()
  public void reset() {
    head = 0;
    tail = 0;
    line = 1;
  }

  //sync()
  public void sync() {
    tail = head;
  }

  //source()
  public String source() {
    return source;
  }

  //head()
  public int head() {
    return head;
  }

  //tail()
  public int tail() {
    return tail;
  }

  //line()
  public int line() {
    return line;
  }

  //atStart()
  public boolean atStart() {
    return head == 0;
  }

  //atEnd()
  public boolean atEnd() {
    return head >= source.length();
  }

  //advance()
  public void advance() {
    //This should be the only place head is incremented,
    //to ensure that line is also incremented when appropriate.
    if (peek() == EOL) line++;

    head++;
  }

  //peekAndAdvance()
  public char peekAndAdvance() {
    char c = peek();

    advance();

    return c;
  }

  //read()
  public String read() {
    // Read string from tail (inclusive) through
    // head (exclusive);
    // E.g. "foobar".substring(2, 5) == "oba".
    // If (tail == head) then this will return a
    // zero-length string.
    return source.substring(tail, head);
  }

  //readTrimmed()
  public String readTrimmed() {
    return source.substring(tail + 1, head - 1);
  }

  //seek(char)
  public boolean seek(char c) {
    while (peek() != c && !atEnd())
      advance();

    return !atEnd();
  }

  //seekAndAdvance(char)
  public boolean seekAndAdvance(char c) {
    seek(c);

    advance();

    return atEnd();
  }

  //match()
  protected boolean match(char expected) {
    if (peek() != expected) return false;

    advance();

    return true;
  }

  //peek()
  protected char peek() {
    if (atEnd()) return NULL_CHAR;

    return source.charAt(head);
  }

  //peekNext()
  protected char peekNext() {
    if (head + 1 >= source.length()) return NULL_CHAR;

    return source.charAt(head + 1);
  }

  //peekPrev()
  protected char peekPrev() {
    if (atStart() || atEnd()) return NULL_CHAR;

    return source.charAt(head - 1);
  }

  @Override
  public String toString() {
    return source;
  }
}
