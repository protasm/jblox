package parser.parselet;

import compiler.Compiler;

public class StringParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    Object value = compiler.parser().previous().literal();

    compiler.emitConstant(value);
  }
}
