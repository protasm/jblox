package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.compiler.OpCode.*;
import static jblox.parser.Parser.Precedence.*;
import static jblox.scanner.TokenType.*;

public class DotParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    compiler.consume(TOKEN_IDENTIFIER, "Expect property name after '.'.");

    int nameIdx = compiler.identifierConstant(compiler.parser().previous());

    if (canAssign && compiler.match(TOKEN_EQUAL)) {
      compiler.expression();

      compiler.emitByte(OP_SET_PROPERTY);
      compiler.emitWord((short)nameIdx);
    } else if (compiler.match(TOKEN_LEFT_PAREN)) {
      byte argCount = compiler.argumentList();

      compiler.emitByte(OP_INVOKE);
      compiler.emitWord((short)nameIdx);
      compiler.emitByte(argCount);
    } else {
      compiler.emitByte(OP_GET_PROPERTY);
      compiler.emitWord((short)nameIdx);
    }
  }
}
