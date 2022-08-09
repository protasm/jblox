package jblox.parser.parselet;

import jblox.compiler.Compiler;
import jblox.scanner.TokenType;

import static jblox.compiler.OpCode.*;
import static jblox.parser.Parser.Precedence.*;
import static jblox.scanner.TokenType.*;

public class UnaryParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    TokenType operatorType = compiler.parser().previous().type();

    // jblox.Compile the operand.
    compiler.parsePrecedence(PREC_UNARY);

    // Emit the operator instruction.
    switch (operatorType) {
      case TOKEN_BANG: compiler.emitByte(OP_NOT); break;
      case TOKEN_MINUS: compiler.emitByte(OP_NEGATE); break;
      default: return; // Unreachable.
    }
  }
}
