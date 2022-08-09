package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.scanner.TokenType.*;

public class GroupingParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    compiler.expression();

    compiler.consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
  }
}
