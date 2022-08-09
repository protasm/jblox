package jblox.scanner;

public class Token {
  private TokenType type;
  private String lexeme;
  private Object literal;
  private int line;

  //Token()
  public Token() {
    this(null, null, null, -1);
  }

  //Token(String)
  public Token(String lexeme) {
    this(null, lexeme, null, -1);
  }

  //Token(TokenType, String, Object, int)
  public Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  //type()
  public TokenType type() {
    return type;
  }

  //lexeme()
  public String lexeme() {
    return lexeme;
  }

  //length()
  public int length() {
    return lexeme.length();
  }

  //literal()
  public Object literal() {
    return literal;
  }

  //line()
  public int line() {
    return line;
  }

  //toString()
  public String toString() {
    return type + "," + lexeme + "," + literal + " (" + line + ")";
  }
}
