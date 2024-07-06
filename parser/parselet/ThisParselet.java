package parser.parselet;

import compiler.Compiler;

public class ThisParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    if (compiler.currentClass() == null) {
      compiler.error("Can't use 'this' outside of a class.");

      return;
    }

    new VariableParselet().parse(compiler, false);
  }
}
