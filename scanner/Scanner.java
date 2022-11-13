package jblox.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.main.PropsObserver;

import static jblox.scanner.TokenType.*;

public class Scanner extends PropsObserver {
  private static final char EOL = '\n';
  private static final Map<String, TokenType> keywords;
  private static final Map<Character, TokenType> oneCharLexemes;
  private ScannableSource ss;
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

    oneCharLexemes = new HashMap<>() {{
      put('(', TOKEN_LEFT_PAREN);
      put(')', TOKEN_RIGHT_PAREN);
      put('{', TOKEN_LEFT_BRACE);
      put('}', TOKEN_RIGHT_BRACE);
      put(',', TOKEN_COMMA);
      put('.', TOKEN_DOT);
      put('-', TOKEN_MINUS);
      put('+', TOKEN_PLUS);
      put(';', TOKEN_SEMICOLON);
      put('*', TOKEN_STAR);
    }};
  }

  //Scanner(Props, Debugger)
  public Scanner(Props properties, Debugger debugger) {
    super(properties, debugger);

    if (debugPrintProgress) debugger.printProgress("Scanner initialized.");
  }

  //scan(String)
  public void scan(String source) {
    ss = new ScannableSource(source);
    tokens = new ArrayList<>();
    nextToken = 0;

    if (debugPrintSource) debugger.printSource(source);
    if (debugPrintProgress) debugger.printProgress("Scanning....");

    // Scan tokens.
    while (!ss.atEnd()) {
      // We are at the beginning of the next lexeme.
      ss.sync();

      lexToken();
    }
  }

  //lexToken()
  private void lexToken() {
    char c = ss.peekAndAdvance();

    if (oneCharLexemes.containsKey(c))
      addToken(oneCharLexemes.get(c));
    else if (isDigit(c))
      number();
    else if (isAlpha(c))
      identifier();
    else switch (c) {
      case '!':
        addToken(ss.match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);

        break;
      case '=':
        addToken(ss.match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);

        break;
      case '<':
        addToken(ss.match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);

        break;
      case '>':
        addToken(ss.match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);

        break;
      case '/':
        if (ss.match('/'))
          // A double-slash comment goes until the end of the line.
          lineComment();
        else if (ss.match('*'))
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
        unexpectedChar(c);

        break;
    } //switch
  }

  //lineComment()
  private void lineComment() {
    ss.seek(EOL);
  }

  //blockComment()
  private void blockComment() {
    while (!ss.atEnd()) {
      ss.seek('*');

      if (ss.peekPrev() == '/') {
        errorToken("Nested block comment");

        return;
      }

      ss.advance();

      if (ss.match('/')) return;
    } //while

    // Error if we get here.
    errorToken("Unterminated block comment.");
  }

  //identifier()
  private void identifier() {
    while (isAlphaNumeric(ss.peek())) ss.advance();

    TokenType type = keywords.get(ss.read());

    if (type == null) type = TOKEN_IDENTIFIER;

    addToken(type);
  }

  //number()
  private void number() {
    while (isDigit(ss.peek())) ss.advance();

    // Look for a fractional part.
    if (ss.peek() == '.' && isDigit(ss.peekNext())) {
      // Consume the '.'
      ss.advance();

      while (isDigit(ss.peek())) ss.advance();
    }

    addToken(TOKEN_NUMBER, Double.parseDouble(ss.read()));
  }

  //string()
  private void string() {
    if (!ss.seek('"')) {
      errorToken("Unterminated string.");

      return;
    }

    // The closing '"'.
    ss.advance();

    addToken(TOKEN_STRING, ss.readTrimmed()); //trim surrounding quotes
  }

  //addToken(TokenType)
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  //addToken(TokenType, Object)
  private void addToken(TokenType type, Object literal) {
    tokens.add(new Token(type, ss.read(), literal, ss.line()));
  }

  //errorToken
  private void errorToken(String message) {
    tokens.add(new Token(TOKEN_ERROR, message, null, ss.line()));
  }

  //unexpectedChar(char)
  private void unexpectedChar(char c) {
    errorToken("Unexpected character: '" + c + "'.");
  }

  //getNextToken()
  public Token getNextToken() {
    if (nextToken <= (tokens.size() - 1))
      return tokens.get(nextToken++);
    else
      return new Token(TOKEN_EOF, "", null, ss.line());
  }

  //isWhitespace(char)
  private boolean isWhitespace(char c) {
    return (c == ' ') || (c == '\r') || (c == '\t');
  }

  //isAlpha(char)
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  //isAlphaNumeric(char)
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  //isDigit(char)
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  //updateCachedProperties()
  protected void updateCachedProperties() {
    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugPrintSource = debugMaster && properties.getBool("DEBUG_PRINT_SOURCE");
  }
}
