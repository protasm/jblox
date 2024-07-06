package compiler;

import main.Props;
import scanner.Token;

import static scanner.TokenType.*;

public class CompilerLocals {
  public enum FunctionType {
    TYPE_SCRIPT,
    TYPE_FUNCTION,
    TYPE_INITIALIZER,
    TYPE_METHOD,
  }

  private CompilerLocals enclosing;
  private FunctionType type;
  private Function function;
  private Local[] locals;
  private int localsCount;
  private Upvalue[] upvalues;
  private int scopeDepth;

  //CompilerLocals(CompilerLocals, FunctionType, int)
  public CompilerLocals(CompilerLocals enclosing, FunctionType type, int stackSize) {
    this(enclosing, type, stackSize, null);
  }

  //CompilerLocals(Props, CompilerLocals, FunctionType, int, String)
  public CompilerLocals(
    CompilerLocals enclosing, FunctionType type, int stackSize, String functionName
  ) {
    this.enclosing = enclosing;
    this.type = type;

    function = new Function(functionName);
    locals = new Local[stackSize];
    localsCount = 0;
    upvalues = new Upvalue[stackSize];
    scopeDepth = 0;

    //Block out stack slot zero for the function being called.
    Token token;

    if (type != FunctionType.TYPE_FUNCTION)
      token = new Token(null, "this", null, -1);
    else
      token = new Token(null, "", null, -1);

    locals[localsCount++] = new Local(token, 0);
  }

  //enclosing()
  public CompilerLocals enclosing() {
    return enclosing;
  }

  //type()
  public FunctionType type() {
    return type;
  }

  //function()
  public Function function() {
    return function;
  }

  //scopeDepth()
  public int scopeDepth() {
    return scopeDepth;
  }

  //setScopeDepth(int)
  public void setScopeDepth(int scopeDepth) {
    this.scopeDepth = scopeDepth;
  }

  //locals()
  public Local[] locals(){
    return locals;
  }

  //localsCount()
  public int localsCount() {
    return localsCount;
  }

  //peek()
  public Local peek() {
    return locals[localsCount - 1];
  }

  //pop()
  public Local pop() {
    return locals[(localsCount--) - 1];
  }

  //push(Local)
  public void push(Local local) {
    locals[localsCount++] = local;
  }

  //markInitialized()
  public void markInitialized() {
    locals[localsCount - 1].setDepth(scopeDepth);
  }

  //addUpvalue(Upvalue)
  public int addUpvalue(Upvalue upvalue) {
    int count = function.upvalueCount();

    upvalues[count] = upvalue;

    function.incrementUpvalueCount();

    return count;
  }

  //getUpvalue(int)
  public Upvalue getUpvalue(int index) {
    return upvalues[index];
  }

  //toString()
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < localsCount; i++) {
      Local local = locals[i];
      String lexeme = local.token().lexeme();
      int depth = local.depth();

      sb.append("[ " + lexeme + " (" + depth + ") ]");
    }

    return sb.toString();
  }
}
