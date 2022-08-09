package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.compiler.OpCode.*;
import static jblox.scanner.TokenType.*;

public class SuperParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    if (compiler.currentClass() == null)
      compiler.error("Can't use 'super' outside of a class.");
    else if (!(compiler.currentClass().hasSuperclass()))
      compiler.error("Can't use 'super' in a class with no superclass.");

    compiler.consume(TOKEN_DOT, "Expect '.' after 'super'.");

    compiler.consume(TOKEN_IDENTIFIER, "Expect superclass method name.");

    int nameIdx = compiler.identifierConstant(compiler.parser().previous());

    compiler.namedVariable(compiler.syntheticToken("this"), false);

    if (compiler.match(TOKEN_LEFT_PAREN)) {
      byte argCount = compiler.argumentList();
      compiler.namedVariable(compiler.syntheticToken("super"), false);

      compiler.emitByte(OP_SUPER_INVOKE);
      compiler.emitWord((short)nameIdx);
      compiler.emitByte(argCount);
    } else {
      compiler.namedVariable(compiler.syntheticToken("super"), false);
      compiler.emitByte(OP_GET_SUPER);
      compiler.emitWord((short)nameIdx);
    }
  }
}
