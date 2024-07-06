package compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import debug.Debugger;
import main.Props;
import main.PropsObserver;
import parser.ParseRule;
import parser.Parser;
import parser.parselet.*;
import scanner.Scanner;
import scanner.Token;
import scanner.TokenType;

import static compiler.CompilerLocals.FunctionType.*;
import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;
import static scanner.TokenType.*;

public class Compiler extends PropsObserver {
  private Scanner scanner;
  private Map<TokenType, ParseRule> typeToRule;
  private Parser parser;
  private CompilerLocals currentLocals;
  private CompilerClass currentClass;

  //Cached properties
  private boolean debugMaster;
  private boolean debugPrintProgress;
  private boolean debugPrintCode;

  //Compiler
  public Compiler(Props properties, Debugger debugger) {
    super(properties, debugger);

    scanner = new Scanner(properties, debugger);
    typeToRule = new HashMap<>();

    registerTokens();

    if (debugPrintProgress) debugger.printProgress("Compiler initialized.");
  }

  //parser()
  public Parser parser() {
    return parser;
  }

  //currentClass()
  public CompilerClass currentClass() {
    return currentClass;
  }

  //compile(String)
  public Function compile(String source) {
    parser = new Parser();

    scanner.scan(source);

    currentLocals = new CompilerLocals(
      null, TYPE_SCRIPT, properties.getInt("MAX_SIGNED_BYTE")
    );

    if (debugPrintProgress)
      debugger.printProgress("Compiling....");

    advance();

    while (!match(TOKEN_EOF))
      declaration();

    Function function = endCompilation();

    return parser.hadError() ? null : function;
  }

  //advance()
  private void advance() {
    //advance to the next non-error Token (or EOF)
    parser.setPrevious(parser.current());

    for (;;) {
      parser.setCurrent(scanner.getNextToken());

      if (parser.current().type() != TOKEN_ERROR)
        break;

      errorAtCurrent(parser.current().lexeme());
    }
  }

  //consume(TokenType, String)
  public void consume(TokenType type, String message) {
    if (parser.current().type() == type) {
      advance();

      return;
    }

    errorAtCurrent(message);
  }

  //check(TokenType)
  private boolean check(TokenType type) {
    return parser.current().type() == type;
  }

  //match(TokenType)
  public boolean match(TokenType type) {
    if (!check(type)) return false;

    advance();

    return true;
  }

  //register(TokenType, Parselet, Parselet, int)
  private void register(TokenType type, Parselet prefix, Parselet infix, int precedence) {
    typeToRule.put(type, new ParseRule(prefix, infix, precedence));
  }

  //emitByte(byte)
  public void emitByte(byte b) {
    currentChunk().writeCode(b, parser.previous().line());
  }

  //emitWord(int)
  public void emitWord(int i) {
    emitWord((short)i);
  }

  //emitWord(short)
  public void emitWord(short s) {
    emitByte(highByte(s));
    emitByte(lowByte(s));
  }

  //highByte(short)
  private byte highByte(short s) {
    return (byte)((s >> 8) & 0xFF);
  }

  //lowByte(short)
  private byte lowByte(short s) {
    return (byte)(s & 0xFF);
  }

  //emitLoop(int)
  private void emitLoop(int loopStart) {
    int maxLoop = properties.getInt("MAX_LOOP");

    emitByte(OP_LOOP);

    int offset = currentChunk().codesCount() - loopStart + 2;

    if (offset > maxLoop) error("Loop body too large.");

    emitWord(offset);
  }

  //emitJump(byte)
  public int emitJump(byte instruction) {
    emitByte(instruction);

    //placeholders, later backpatched.
    emitByte((byte)0xFF);
    emitByte((byte)0xFF);

    return currentChunk().codesCount() - 2;
  }

  //emitReturn()
  private void emitReturn() {
    if (currentLocals.type() == TYPE_INITIALIZER) {
      emitByte(OP_GET_LOCAL);
      emitWord(0);
    } else
      emitByte(OP_NIL);

    emitByte(OP_RETURN);
  }

  //makeConstant(Object)
  int makeConstant(Object value) {
    int index = currentChunk().writeConstant(value);

    if (index > properties.getInt("MAX_SIGNED_SHORT")) {
      error("Too many constants in one chunk.");

      return 0;
    }

    //Return the index of the constant added.
    return index;
  }

  //emitConstant(Object)
  public void emitConstant(Object value) {
    int index = makeConstant(value);

    emitByte(OP_CONSTANT);
    emitWord(index);
  }

  //patchJump(int)
  public void patchJump(int offset) {
    int maxJump = properties.getInt("MAX_JUMP");
    // -2 to adjust for the bytecode for the jump offset itself.
    int jump = currentChunk().codesCount() - offset - 2;

    if (jump > maxJump)
      error("Too much code to jump over.");

    byte hi = highByte((short)jump);
    byte lo = lowByte((short)jump);

    currentChunk().codes()[offset] =  hi;
    currentChunk().codes()[offset + 1] = lo;
  }

  //currentChunk()
  private Chunk currentChunk() {
    return currentLocals.function().chunk();
  }

  //endCompilation()
  private Function endCompilation() {
    emitReturn();

    //Extract assembled function from temporary structure.
    Function function = currentLocals.function();

    if (!parser.hadError() && debugPrintCode)
      debugger.disassembleChunk(function.chunk(), currentLocals, function.toString());

    //Step up to locals in higher scope.
    currentLocals = currentLocals.enclosing();

    return function;
  }

  //beginScope()
  private void beginScope() {
    currentLocals.setScopeDepth(currentLocals.scopeDepth() + 1);
  }

  //endScope()
  private void endScope() {
    currentLocals.setScopeDepth(currentLocals.scopeDepth() - 1);

    while (
      currentLocals.localsCount() > 0 &&
      currentLocals.peek().depth() > currentLocals.scopeDepth()
    ) {
      if (currentLocals.locals()[currentLocals.localsCount() - 1].isCaptured())
        emitByte(OP_CLOSE_UPVALUE);
      else
        emitByte(OP_POP);

      currentLocals.pop();
    }
  }

  //getRule(TokenType)
  public ParseRule getRule(TokenType type) {
    return typeToRule.get(type);
  }

  //namedVariable(Token, boolean)
  public void namedVariable(Token token, boolean canAssign) {
    byte getOp;
    byte setOp;

    int arg = resolveLocal(currentLocals, token);

    if (arg != -1) { //local variable
      getOp = OP_GET_LOCAL;
      setOp = OP_SET_LOCAL;
    } else if ((arg = resolveUpvalue(currentLocals, token)) != -1) { //upvalue
      getOp = OP_GET_UPVALUE;
      setOp = OP_SET_UPVALUE;
    } else { //global variable
      //add token to constants and store index in arg
      arg = identifierConstant(token);

      getOp = OP_GET_GLOBAL;
      setOp = OP_SET_GLOBAL;
    }

    if (canAssign && match(TOKEN_EQUAL)) { //assignment
      expression();

      emitByte(setOp);
      emitWord(arg);
    } else { //retrieval
      emitByte(getOp);
      emitWord(arg);
    }
  }

  //parsePrecedence(int)
  public void parsePrecedence(int precedence) {
    advance();

    Parselet prefixRule = getRule(parser.previous().type()).prefix();

    if (prefixRule == null) {
      error("Expect expression.");

      return;
    }

    boolean canAssign = (precedence <= PREC_ASSIGNMENT);

    prefixRule.parse(this, canAssign);

    while (precedence <= getRule(parser.current().type()).precedence()) {
      advance();

      Parselet infixRule = getRule(parser.previous().type()).infix();

      infixRule.parse(this, canAssign);
    }

    if (canAssign && match(TOKEN_EQUAL))
      error("Invalid assignment target.");
  }

  //identifierConstant(Token)
  public int identifierConstant(Token token) {
    //return index of newly added constant
    return makeConstant(token.lexeme());
  }

  //identifiersEqual(Token, Token)
  private boolean identifiersEqual(Token a, Token b) {
    return a.lexeme().equals(b.lexeme());
  }

  //resolveLocal(CompilerLocals, Token)
  private int resolveLocal(CompilerLocals locals, Token token) {
    for (int i = locals.localsCount() - 1; i >= 0; i--) {
      Local local = locals.locals()[i];

      if (identifiersEqual(token, local.token())) {
        if (local.depth() == -1) //"sentinel" depth
          error("Can't read local variable in its own initializer.");

        return i;
      }
    }

    //No variable with the given name, therefore not a local.
    return -1;
  }

  //addUpvalue(CompilerLocals, byte, boolean)
  private int addUpvalue(CompilerLocals locals, byte index, boolean isLocal) {
    int maxClosureVariables = properties.getInt("MAX_SIGNED_BYTE");
    //isLocal controls whether closure captures a local variable or
    //an upvalue from the surrounding function
    int upvalueCount = locals.function().upvalueCount();

    for (int i = 0; i < upvalueCount; i++) {
      Upvalue upvalue = locals.getUpvalue(i);

      if (upvalue.index() == index && upvalue.isLocal() == isLocal)
        return i;
    }

    if (upvalueCount == maxClosureVariables) {
      error("Too many closure variables in function.");

      return 0;
    }

    return locals.addUpvalue(new Upvalue(index, isLocal));
  }

  //resolveUpvalue(CompilerLocals, Token)
  private int resolveUpvalue(CompilerLocals locals, Token token) {
    CompilerLocals enclosing = locals.enclosing();

    if (enclosing == null) return -1;

    int local = resolveLocal(enclosing, token);

    if (local != -1) {
      enclosing.locals()[local].setIsCaptured(true);

      return addUpvalue(locals, (byte)local, true);
    }

    int upvalue = resolveUpvalue(enclosing, token);

    if (upvalue != -1)
      return addUpvalue(locals, (byte)upvalue, false);

    return -1;
  }

  //addLocal(Token)
  private void addLocal(Token token) {
    if (currentLocals.localsCount() >= properties.getInt("MAX_SIGNED_BYTE")) {
      error("Too many local variables in function.");

      return;
    }

    currentLocals.push(new Local(token, -1));
  }

  //declareVariable()
  //In the locals, a variable is "declared" when it is
  //added to the scope.
  private void declareVariable() {
    if (currentLocals.scopeDepth() == 0)
      return;

    Token token = parser.previous();

    //Start at the end of the locals array and work backward,
    //looking for an existing variable with the same name.
    for (int i = currentLocals.localsCount() - 1; i >= 0; i--) {
      Local local = currentLocals.locals()[i];

      if (local.depth() != -1 && local.depth() < currentLocals.scopeDepth())
        break;

      if (identifiersEqual(token, local.token()))
        error("Already a variable with this name in this scope.");
    }

    //Record existence of local variable.
    addLocal(parser.previous());
  }

  //parseVariable(String)
  private int parseVariable(String errorMessage) {
    consume(TOKEN_IDENTIFIER, errorMessage);

    declareVariable();

    //Exit the function if we're in a local scope,
    //returning a dummy table index.
    if (currentLocals.scopeDepth() > 0) return 0;

    return identifierConstant(parser.previous());
  }

  //markInitialized()
  private void markInitialized() {
    if (currentLocals.scopeDepth() == 0) return;

    currentLocals.markInitialized();
  }

  //defineVariable(int)
  private void defineVariable(int index) {
    if (currentLocals.scopeDepth() > 0) {
      //In the locals, a variable is "defined" when it
      //becomes available for use.
      markInitialized();

      //No code needed to create a local variable at
      //runtime; it's on top of the stack.

      return;
    }

    emitByte(OP_DEFINE_GLOBAL);
    emitWord(index);
  }

  //syntheticToken(String)
  public Token syntheticToken(String text) {
    return new Token(text);
  }

  //argumentList()
  public byte argumentList() {
    byte argCount = 0;
    int maxSignedByte = properties.getInt("MAX_SIGNED_BYTE");

    if (!check(TOKEN_RIGHT_PAREN))
      do {
        expression();

        if (argCount == maxSignedByte)
          error("Can't have more than " + maxSignedByte + " arguments.");

        argCount++;
      } while (match(TOKEN_COMMA));

    consume(TOKEN_RIGHT_PAREN, "Expect ')' after arguments.");

    return argCount;
  }

  //expression()
  public void expression() {
    parsePrecedence(PREC_ASSIGNMENT);
  }

  //block()
  private void block() {
    while (!check(TOKEN_RIGHT_BRACE) && !check(TOKEN_EOF))
      declaration();

    consume(TOKEN_RIGHT_BRACE, "Expect '}' after block.");
  }

  //function(CompilerLocals.FunctionType)
  private void function(CompilerLocals.FunctionType type) {
    int maxSignedByte = properties.getInt("MAX_SIGNED_BYTE");

    CompilerLocals locals = new CompilerLocals(
      currentLocals, type, maxSignedByte, parser.previous().lexeme()
    );
    currentLocals = locals;

    beginScope(); 

    consume(TOKEN_LEFT_PAREN, "Expect '(' after function name.");

    if (!check(TOKEN_RIGHT_PAREN))
      do {
        locals.function().setArity(locals.function().arity() + 1);

        if (locals.function().arity() > maxSignedByte)
          errorAtCurrent("Can't have more than " + maxSignedByte + " parameters.");

        int index = parseVariable("Expect parameter name.");

        defineVariable(index);
      } while (match(TOKEN_COMMA));

    consume(TOKEN_RIGHT_PAREN, "Expect ')' after parameters.");
    consume(TOKEN_LEFT_BRACE, "Expect '{' before function body.");

    block();

    Function function = endCompilation(); //sets currentLocals to enclosing

    emitByte(OP_CLOSURE);
    emitWord(makeConstant(function));

    for (int i = 0; i < function.upvalueCount(); i++) {
      emitByte((byte)(locals.getUpvalue(i).isLocal() ? 1 : 0));
      emitByte((byte)(locals.getUpvalue(i).index()));
    }

    //No endScope() needed because CompilerLocals is ended completely
    //at the end of the function body.
  }

  //method()
  private void method() {
    consume(TOKEN_IDENTIFIER, "Expect method name.");

    int constant = identifierConstant(parser.previous());

    CompilerLocals.FunctionType type = TYPE_METHOD;

    if (parser.previous().lexeme().equals("init"))
      type = TYPE_INITIALIZER;

    function(type);

    emitByte(OP_METHOD);
    emitWord(constant);
  }

  //classDeclaration()
  private void classDeclaration() {
    consume(TOKEN_IDENTIFIER, "Expect class name.");

    Token classToken = parser.previous();

    int nameConstantIdx = identifierConstant(parser.previous());

    declareVariable();

    emitByte(OP_CLASS);
    emitWord(nameConstantIdx);

    defineVariable(nameConstantIdx);

    currentClass = new CompilerClass(currentClass, false);

    if (match(TOKEN_LESS)) {
      consume(TOKEN_IDENTIFIER, "Expect superclass name.");

      new VariableParselet().parse(this, false);

      if (identifiersEqual(classToken, parser.previous()))
        error("A class can't inherit from itself.");

      beginScope();

      addLocal(syntheticToken("super"));

      defineVariable(0x00);

      namedVariable(classToken, false);

      emitByte(OP_INHERIT);

      currentClass.setHasSuperclass(true);
    }

    //load class back onto the stack
    namedVariable(classToken, false);

    consume(TOKEN_LEFT_BRACE, "Expect '{' before class body.");

    while (!check(TOKEN_RIGHT_BRACE) && !check(TOKEN_EOF))
      method();

    consume(TOKEN_RIGHT_BRACE, "Expect '}' after class body.");

    emitByte(OP_POP);

    if (currentClass.hasSuperclass())
      endScope();

    currentClass = currentClass.enclosing();
  }

  //funDeclaration()
  private void funDeclaration() {
    int globalIdx = parseVariable("Expect function name.");

    //Function declaration's variable is marked "initialized"
    //before compiling the body so that the name can be
    //referenced inside the body without generating an error.
    markInitialized();

    function(TYPE_FUNCTION);

    defineVariable(globalIdx);
}

  //varDeclaration()
  private void varDeclaration() {
    int index = parseVariable("Expect variable name.");

    if (match(TOKEN_EQUAL))
      expression();
    else
      emitByte(OP_NIL);

    defineVariable(index);

    //handle variable declarations of the form:
    //var x = 99, y, z = "hello";
    if (match(TOKEN_COMMA)) {
      varDeclaration();

      return;
    }

    consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration(s).");
  }

  //expressionStatement()
  private void expressionStatement() {
    expression();

    consume(TOKEN_SEMICOLON, "Expect ';' after expression.");

    emitByte(OP_POP);
  }

  //forStatement()
  private void forStatement() {
    beginScope();

    consume(TOKEN_LEFT_PAREN, "Expect '(' after 'for'.");

    //Initializer clause.
    if (match(TOKEN_SEMICOLON)) {
      // No initializer.
    } else if (match(TOKEN_VAR))
      varDeclaration();
    else
      expressionStatement();

    int loopStart = currentChunk().codesCount();

     //Condition clause.
    int exitJump = -1;

    if (!match(TOKEN_SEMICOLON)) {
      expression();

      consume(TOKEN_SEMICOLON, "Expect ';' after loop condition.");

      // Jump out of the loop if the condition is false.
      exitJump = emitJump(OP_JUMP_IF_FALSE);

      emitByte(OP_POP); // Condition.
    }

    //Increment clause.
    if (!match(TOKEN_RIGHT_PAREN)) {
      int bodyJump = emitJump(OP_JUMP);
      int incrementStart = currentChunk().codesCount();

      expression();

      emitByte(OP_POP);

      consume(TOKEN_RIGHT_PAREN, "Expect ')' after for clauses.");

      emitLoop(loopStart);

      loopStart = incrementStart;

      patchJump(bodyJump);
    }

    statement();

    emitLoop(loopStart);

    if (exitJump != -1) {
      patchJump(exitJump);

      emitByte(OP_POP); // Condition.
    }

    endScope();
  }

  //ifStatement()
  private void ifStatement() {
    consume(TOKEN_LEFT_PAREN, "Expect '(' after 'if'.");

    expression();

    consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition."); 

    int thenJump = emitJump(OP_JUMP_IF_FALSE);

    emitByte(OP_POP);

    statement();

    int elseJump = emitJump(OP_JUMP);

    patchJump(thenJump);

    emitByte(OP_POP);

    if (match(TOKEN_ELSE)) statement();

    patchJump(elseJump);
  }

  //returnStatement()
  private void returnStatement() {
    if (currentLocals.type() == TYPE_SCRIPT)
      error("Can't return from top-level code.");

    if (match(TOKEN_SEMICOLON)) //Return value is optional.
      emitReturn();
    else {
      if (currentLocals.type() == TYPE_INITIALIZER)
        error("Can't return a value from an initializer.");

      expression();

      consume(TOKEN_SEMICOLON, "Expect ';' after return value.");

      emitByte(OP_RETURN);
    }
  }

  //whileStatement()
  private void whileStatement() {
    int loopStart = currentChunk().codesCount();

    consume(TOKEN_LEFT_PAREN, "Expect '(' after 'while'.");

    expression();

    consume(TOKEN_RIGHT_PAREN, "Expect ')' after condition.");

    int exitJump = emitJump(OP_JUMP_IF_FALSE);

    emitByte(OP_POP);

    statement();

    emitLoop(loopStart);

    patchJump(exitJump);

    emitByte(OP_POP);
  }

  //synchronize()
  private void synchronize() {
    parser.setPanicMode(false);

    while (parser.current().type() != TOKEN_EOF) {
      if (parser.previous().type() == TOKEN_SEMICOLON) return;

      switch (parser.current().type()) {
        case TOKEN_CLASS:
        case TOKEN_FUN:
        case TOKEN_VAR:
        case TOKEN_FOR:
        case TOKEN_IF:
        case TOKEN_WHILE:
        case TOKEN_RETURN:
          return;

        default:
          break; // Do nothing.
      } //switch

      advance();
    } //while
  }

  //declaration()
  private void declaration() {
    if (match(TOKEN_CLASS)) 
      classDeclaration();
    else if (match(TOKEN_FUN))
      funDeclaration();
    else if (match(TOKEN_VAR))
      varDeclaration();
    else
      statement();

    if (parser.panicMode())
      synchronize();
  }

  //statement()
  private void statement() {
    if (match(TOKEN_FOR))
      forStatement();
    else if (match(TOKEN_IF))
      ifStatement();
    else if (match(TOKEN_RETURN))
      returnStatement();
    else if (match(TOKEN_WHILE))
      whileStatement();
    else if (match(TOKEN_LEFT_BRACE)) {
      beginScope();

      block();

      endScope();
    } else
      expressionStatement();
  }

  //errorAtCurrent(String)
  private void errorAtCurrent(String message) {
    errorAt(parser.current(), message);
  }

  //error(String)
  public void error(String message) {
    errorAt(parser.previous(), message);
  }

  //errorAt(Token, String)
  private void errorAt(Token token, String message) {
    if (parser.panicMode()) return;

    parser.setPanicMode(true);

    System.err.print("[line " + token.line() + "] Error");

    if (token.type() == TOKEN_EOF)
      System.err.print(" at end");
    else if (token.type() == TOKEN_ERROR) {
      // Nothing.
    } else
      System.err.print(" at '" + token.lexeme() + "'");

    System.err.print(": " + message + "\n");

    parser.setHadError(true);
  }

  //registerTokens()
  private void registerTokens() {
    register(TOKEN_LEFT_PAREN,    new GroupingParselet(), new CallParselet(),  PREC_CALL);
    register(TOKEN_RIGHT_PAREN,   null,                   null,                PREC_NONE);
    register(TOKEN_LEFT_BRACE,    null,                   null,                 PREC_NONE);
    register(TOKEN_RIGHT_BRACE,   null,                   null,                 PREC_NONE);
    register(TOKEN_COMMA,         null,                   null,                 PREC_NONE);
    register(TOKEN_DOT,           null,                   new DotParselet(),    PREC_CALL);
    register(TOKEN_MINUS,         new UnaryParselet(),    new BinaryParselet(), PREC_TERM);
    register(TOKEN_PLUS,          null,                   new BinaryParselet(), PREC_TERM);
    register(TOKEN_SEMICOLON,     null,                   null,                 PREC_NONE);
    register(TOKEN_SLASH,         null,                   new BinaryParselet(), PREC_FACTOR);
    register(TOKEN_STAR,          null,                   new BinaryParselet(), PREC_FACTOR);
    register(TOKEN_BANG,          new UnaryParselet(),    null,                 PREC_NONE);
    register(TOKEN_BANG_EQUAL,    null,                   new BinaryParselet(), PREC_EQUALITY);
    register(TOKEN_EQUAL,         null,                   null,                 PREC_NONE);
    register(TOKEN_EQUAL_EQUAL,   null,                   new BinaryParselet(), PREC_EQUALITY);
    register(TOKEN_GREATER,       null,                   new BinaryParselet(), PREC_COMPARISON);
    register(TOKEN_GREATER_EQUAL, null,                   new BinaryParselet(), PREC_COMPARISON);
    register(TOKEN_LESS,          null,                   new BinaryParselet(), PREC_COMPARISON);
    register(TOKEN_LESS_EQUAL,    null,                   new BinaryParselet(), PREC_COMPARISON);
    register(TOKEN_IDENTIFIER,    new VariableParselet(), null,                 PREC_NONE);
    register(TOKEN_STRING,        new StringParselet(),   null,                 PREC_NONE);
    register(TOKEN_NUMBER,        new NumberParselet(),   null,                 PREC_NONE);
    register(TOKEN_AND,           null,                   new AndParselet(),    PREC_AND);
    register(TOKEN_CLASS,         null,                   null,                 PREC_NONE);
    register(TOKEN_ELSE,          null,                   null,                 PREC_NONE);
    register(TOKEN_FALSE,         new LiteralParselet(),  null,                 PREC_NONE);
    register(TOKEN_FOR,           null,                   null,                 PREC_NONE);
    register(TOKEN_FUN,           null,                   null,                 PREC_NONE);
    register(TOKEN_IF,            null,                   null,                 PREC_NONE);
    register(TOKEN_NIL,           new LiteralParselet(),  null,                 PREC_NONE);
    register(TOKEN_OR,            null,                   new OrParselet(),     PREC_OR);
    register(TOKEN_RETURN,        null,                   null,                 PREC_NONE);
    register(TOKEN_SUPER,         new SuperParselet(),    null,                 PREC_NONE);
    register(TOKEN_THIS,          new ThisParselet(),     null,                 PREC_NONE);
    register(TOKEN_TRUE,          new LiteralParselet(),  null,                 PREC_NONE);
    register(TOKEN_VAR,           null,                   null,                 PREC_NONE);
    register(TOKEN_WHILE,         null,                   null,                 PREC_NONE);
    register(TOKEN_ERROR,         null,                   null,                 PREC_NONE);
    register(TOKEN_EOF,           null,                   null,                 PREC_NONE);
  }

  //updateCachedProperties()
  protected void updateCachedProperties() {
    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugPrintCode = debugMaster && properties.getBool("DEBUG_PRINT_CODE");
  }
}
