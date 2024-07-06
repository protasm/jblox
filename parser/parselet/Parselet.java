package parser.parselet;

import compiler.Compiler;

public interface Parselet {
  //parse(compiler.Compiler, boolean);
  void parse(compiler.Compiler compiler, boolean canAssign);
}
