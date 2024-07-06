package parser.parselet;

import compiler.Compiler;

import static scanner.TokenType.*;

public class GroupingParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    compiler.expression();

    compiler.consume(TOKEN_RIGHT_PAREN, "Expect ')' after expression.");
  }
}
