package jblox.parser.parselet;

import jblox.compiler.Compiler;

public interface Parselet {
  //parse(jblox.compiler.Compiler, boolean);
  void parse(jblox.compiler.Compiler compiler, boolean canAssign);
}
