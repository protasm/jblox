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
    keywords = new HashMap<>();

    keywords.put("and",    TOKEN_AND);
    keywords.put("class",  TOKEN_CLASS);
    keywords.put("else",   TOKEN_ELSE);
    keywords.put("false",  TOKEN_FALSE);
    keywords.put("for",    TOKEN_FOR);
    keywords.put("fun",    TOKEN_FUN);
    keywords.put("if",     TOKEN_IF);
    keywords.put("nil",    TOKEN_NIL);
    keywords.put("or",     TOKEN_OR);
    keywords.put("return", TOKEN_RETURN);
    keywords.put("super",  TOKEN_SUPER);
    keywords.put("this",   TOKEN_THIS);
    keywords.put("true",   TOKEN_TRUE);
    keywords.put("var",    TOKEN_VAR);
    keywords.put("while",  TOKEN_WHILE);
  }

  //Scanner(Props, Debugger)
  public Scanner(Props properties, Debugger debugger) {
    super(properties, debugger);

    if (debugPrintProgress) debugger.printProgress("Initializing scanner....");
  }

  //scan(String)
  public void scan(String source) {
    this.source = source;

    tokens = new ArrayList<>();
    start = 0;
    current = 0;
    line = 1;
    nextToken = 0;

    if (debugPrintSource) debugger.printSource(source);
    if (debugPrintProgress) debugger.printProgress("Scanning....");

    // Scan tokens.
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;

      lexToken();
    }
  }

  //getNextToken()
  public Token getNextToken() {
    if (nextToken > (tokens.size() - 1))
      return (new Token(TOKEN_EOF, "", null, line));
    else
      return tokens.get(nextToken++);
  }

  //lexToken()
  private void lexToken() {
    char c = advance();

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
          singleLineComment();
        else if (match('*'))
          // A slash-star comment can span multiple lines.
          multiLineComment();
        else
          addToken(TOKEN_SLASH);

        break;
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;

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
          tokens.add(new Token(TOKEN_ERROR, "Unexpected character.", null, line));

        break;
    }
  }

  //singleLineComment()
  private void singleLineComment() {
    while (peek() != '\n' && !isAtEnd())
      advance();
  }

  //multiLineComment()
  private void multiLineComment() {
    while (peek() != '*' && !isAtEnd())
      advance();

    if (isAtEnd()) {
      tokens.add(new Token(TOKEN_ERROR, "Unterminated multiline comment.", null, line));

      return;
    }

    //Consume the '*'.
    advance();

    if (peek() != '/')
      //Continue seeking end of comment ("*/").
      multiLineComment();
    else
      //Consume the '/'.
      advance();
  }

  //identifier()
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);

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

    addToken(TOKEN_NUMBER,
      Double.parseDouble(source.substring(start, current)));
  }

  //string()
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;

      advance();
    }

    if (isAtEnd()) {
      tokens.add(new Token(TOKEN_ERROR, "Unterminated string.", null, line));

      return;
    }

    // The closing '"'.
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);

    addToken(TOKEN_STRING, value);
  }

  //addToken()
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  //addToken(TokenType, Object)
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);

    tokens.add(new Token(type, text, literal, line));
  }

  //updateCachedProperties()
  protected void updateCachedProperties() {
    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugPrintSource = debugMaster && properties.getBool("DEBUG_PRINT_SOURCE");
  }
}
