package jblox.compiler;

import jblox.Props;
import jblox.scanner.Token;
import jblox.util.LoxStack;

import static jblox.scanner.TokenType.*;

public class LocalsStackSim {
  public static enum FunctionType {
    TYPE_SCRIPT,
    TYPE_FUNCTION,
    TYPE_INITIALIZER,
    TYPE_METHOD,
  }

  private LocalsStackSim enclosing;
  private LocalsStackSim.FunctionType type;
  private Function function;
  private LoxStack locals;
  private Upvalue[] upvalues;
  private int scopeDepth;

  //LocalsStackSim(Props, LocalsStackSim, LocalsStackSim.FunctionType)
  public LocalsStackSim(
    Props properties, LocalsStackSim enclosing,
    LocalsStackSim.FunctionType type
  ) {
    this(properties, enclosing, type, null);
  }

  //LocalsStackSim(Props, LocalsStackSim, LocalsStackSim.FunctionType, String)
  public LocalsStackSim(
    Props properties, LocalsStackSim enclosing,
    LocalsStackSim.FunctionType type, String functionName
  ) {
    this.enclosing = enclosing;
    this.type = type;

    function = new Function(functionName);
    locals = new LoxStack(properties.getInt("MAX_LOCALS"));
    upvalues = new Upvalue[properties.getInt("MAX_STACK")];
    scopeDepth = 0;

    //Block out stack slot zero for the function being called.
    Token token;

    if (type != LocalsStackSim.FunctionType.TYPE_FUNCTION)
      token = new Token(null, "this", null, -1);
    else
      token = new Token(null, "", null, -1);

    push(new Local(token, 0));
  }

  //enclosing()
  public LocalsStackSim enclosing() {
    return enclosing;
  }

  //type()
  public LocalsStackSim.FunctionType type() {
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

  //count()
  public int count() {
    return locals.count();
  }

  //push(Local)
  public void push(Local local) {
    locals.push(local);
  }

  //peek()
  public Local peek() {
    return (Local)locals.peek();
  }

  //pop()
  public void pop() {
    locals.pop();
  }

  //get(int)
  public Local get(int index) {
    return (Local)locals.get(index);
  }

  //markInitialized()
  public void markInitialized() {
    peek().setDepth(scopeDepth);
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

    for (int i = 0; i < locals.count(); i++) {
      Local local = (Local)locals.get(i);
      String lexeme = local.token().lexeme();
      int depth = local.depth();

      sb.append("[ " + lexeme + " (" + depth + ") ]");
    }

    return sb.toString();
  }
}
