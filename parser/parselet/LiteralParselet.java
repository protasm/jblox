package jblox.parser.parselet;

import jblox.compiler.Compiler;
import jblox.parser.Parser;
import jblox.scanner.Token;

import static jblox.compiler.OpCode.*;
import static jblox.scanner.TokenType.*;

public class LiteralParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
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
