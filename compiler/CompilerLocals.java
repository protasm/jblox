package jblox.compiler;

import jblox.main.Props;
import jblox.scanner.Token;
import jblox.util.LoxLocalStack;

import static jblox.scanner.TokenType.*;

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
  private LoxLocalStack locals;
  private Upvalue[] upvalues;
  private int scopeDepth;

  //CompilerLocals(Props, CompilerLocals, FunctionType)
  public CompilerLocals(
    Props properties, CompilerLocals enclosing, FunctionType type
  ) {
    this(properties, enclosing, type, null);
  }

  //CompilerLocals(Props, CompilerLocals, FunctionType, String)
  public CompilerLocals(
    Props properties, CompilerLocals enclosing, FunctionType type,
    String functionName
  ) {
    this.enclosing = enclosing;
    this.type = type;

    function = new Function(functionName);
    locals = new LoxLocalStack();
    upvalues = new Upvalue[properties.getInt("MAX_STACK")];
    scopeDepth = 0;

    //Block out stack slot zero for the function being called.
    Token token;

    if (type != FunctionType.TYPE_FUNCTION)
      token = new Token(null, "this", null, -1);
    else
      token = new Token(null, "", null, -1);

    locals.push(new Local(token, 0));
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
  public LoxLocalStack locals(){
    return locals;
  }

  //markInitialized()
  public void markInitialized() {
    locals.peek().setDepth(scopeDepth);
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
      Local local = locals.get(i);
      String lexeme = local.token().lexeme();
      int depth = local.depth();

      sb.append("[ " + lexeme + " (" + depth + ") ]");
    }

    return sb.toString();
  }
}
