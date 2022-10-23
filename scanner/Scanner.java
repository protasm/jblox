package jblox.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.main.PropsObserver;

import static jblox.scanner.TokenType.*;

public class Scanner extends SourceReader {
  private static final Map<String, TokenType> keywords;
  private List<Token> tokens;
  private int nextToken;

  //Cached properties
  private boolean debugMaster;
  private boolean debugPrintProgress;
  private boolean debugPrintSource;

  static {
    keywords = new HashMap<>() {{
      put("and",    TOKEN_AND);
      put("class",  TOKEN_CLASS);
      put("else",   TOKEN_ELSE);
      put("false",  TOKEN_FALSE);
      put("for",    TOKEN_FOR);
      put("fun",    TOKEN_FUN);
      put("if",     TOKEN_IF);
      put("nil",    TOKEN_NIL);
      put("or",     TOKEN_OR);
      put("return", TOKEN_RETURN);
      put("super",  TOKEN_SUPER);
      put("this",   TOKEN_THIS);
      put("true",   TOKEN_TRUE);
      put("var",    TOKEN_VAR);
      put("while",  TOKEN_WHILE);
    }};
  }

  //Scanner(Props, Debugger)
  public Scanner(Props properties, Debugger debugger) {
    super(properties, debugger);

    if (debugPrintProgress) debugger.printProgress("Initializing scanner....");
  }

  //scan(String)
  public void scan(String source) {
    initialize(source);

    tokens = new ArrayList<>();
    nextToken = 0;

    if (debugPrintSource) debugger.printSource(source);
    if (debugPrintProgress) debugger.printProgress("Scanning....");

    // Scan tokens.
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      sync();

      lexToken();
    }
  }

  //getNextToken()
  public Token getNextToken() {
    if (nextToken > (tokens.size() - 1))
      return (new Token(TOKEN_EOF, "", null, line()));
    else
      return tokens.get(nextToken++);
  }

  //lexToken()
  private void lexToken() {
    char c = peekAndAdvance();

    switch (c) {
      case '(': addToken(TOKEN_LEFT_PAREN); break;
      case ')': addToken(TOKEN_RIGHT_PAREN); break;
      case '{': addToken(TOKEN_LEFT_BRACE); break;
      case '}': addToken(TOKEN_RIGHT_BRACE); break;
      case ',': addToken(TOKEN_COMMA); break;
      case '.': addToken(TOKEN_DOT); break;
      case '-': addToken(TOKEN_MINUS); break;
      case '+': addToken(TOKEN_PLUS); break;
      case ';': addToken(TOKEN_SEMICOLON); break;
      case '*': addToken(TOKEN_STAR); break;
      case '!':
        addToken(match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);

        break;
      case '=':
        addToken(match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);

        break;
      case '<':
        addToken(match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);

        break;
      case '>':
        addToken(match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);

        break;
      case '/':
        if (match('/'))
          // A double-slash comment goes until the end of the line.
          lineComment();
        else if (match('*'))
          // A slash-star comment can span multiple lines.
          blockComment();
        else
          addToken(TOKEN_SLASH);

        break;
      case ' ':
      case '\r':
      case '\t':
        break; // Ignore whitespace.
      case EOL:
        break;
      case '"':
        string();

        break;
      default:
        if (isDigit(c))
          number();
        else if (isAlpha(c))
          identifier();
        else
          unexpectedChar(c);

        break;
    }
  }

  //lineComment()
  private void lineComment() {
    seek(EOL);
  }

  //blockComment()
  private void blockComment() {
    while (!isAtEnd())
      switch(peekAndAdvance()) {
        case '/':
          if (match('*')) {
            errorToken("Nested block comment.");

            return;
          }

          break;
        case '*':
          if (match('/')) return;

          break;
        default:
          break;
      } //switch

    // Error if we get here.
    errorToken("Unterminated block comment.");
  }

  //identifier()
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    TokenType type = keywords.get(read());

    if (type == null) type = TOKEN_IDENTIFIER;

    addToken(type);
  }

  //number()
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the '.'
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(TOKEN_NUMBER, Double.parseDouble(read()));
  }

  //string()
  private void string() {
    if (!seek('"')) {
      errorToken("Unterminated string.");

      return;
    }

    // The closing '"'.
    advance();

    addToken(TOKEN_STRING, readTrimmed()); //trim surrounding quotes
  }

  //addToken(TokenType)
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  //addToken(TokenType, Object)
  private void addToken(TokenType type, Object literal) {
    tokens.add(new Token(type, read(), literal, line()));
  }

  //errorToken
  private void errorToken(String message) {
    tokens.add(new Token(TOKEN_ERROR, message, null, line()));
  }

  //unexpectedChar(char)
  private void unexpectedChar(char c) {
    errorToken("Unexpected character: '" + c + "'.");
  }

  //updateCachedProperties()
  protected void updateCachedProperties() {
    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugPrintSource = debugMaster && properties.getBool("DEBUG_PRINT_SOURCE");
  }
}
