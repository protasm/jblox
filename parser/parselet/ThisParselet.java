package jblox.parser.parselet;

import jblox.compiler.Compiler;

public class ThisParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    if (compiler.currentClass() == null) {
      compiler.error("Can't use 'this' outside of a class.");

      return;
    }

    new VariableParselet().parse(compiler, false);
  }
}
