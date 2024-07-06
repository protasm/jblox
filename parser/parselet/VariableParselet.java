package parser.parselet;

import compiler.Compiler;

public class VariableParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    compiler.namedVariable(compiler.parser().previous(), canAssign);
  }
}
