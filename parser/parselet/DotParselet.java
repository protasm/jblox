package parser.parselet;

import compiler.Compiler;

import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;
import static scanner.TokenType.*;

public class DotParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
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
