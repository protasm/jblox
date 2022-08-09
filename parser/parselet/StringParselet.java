package jblox.parser.parselet;

import jblox.compiler.Compiler;

public class StringParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    Object val = (String)compiler.parser().previous().literal();

    compiler.emitConstant(val);
  }
}
