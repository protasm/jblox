package parser.parselet;

import compiler.Compiler;
import parser.Parser;
import scanner.Token;

import static compiler.OpCode.*;
import static scanner.TokenType.*;

public class LiteralParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    switch (compiler.parser().previous().type()) {
      case TOKEN_FALSE:
        compiler.emitByte(OP_FALSE);

        break;
      case TOKEN_NIL:
        compiler.emitByte(OP_NIL);

        break;
      case TOKEN_TRUE:
        compiler.emitByte(OP_TRUE);

        break;
      default: //Unreachable
        return;
    }
  }
}
