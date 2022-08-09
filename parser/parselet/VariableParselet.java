package jblox.parser.parselet;

import jblox.compiler.Compiler;

public class VariableParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    compiler.namedVariable(compiler.parser().previous(), canAssign);
  }
}
