package parser.parselet;

import compiler.Compiler;
import scanner.TokenType;

import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;
import static scanner.TokenType.*;

public class UnaryParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    TokenType operatorType = compiler.parser().previous().type();

    // Compile the operand.
    compiler.parsePrecedence(PREC_UNARY);

    // Emit the operator instruction.
    switch (operatorType) {
      case TOKEN_BANG:
        compiler.emitByte(OP_NOT);

        break;
      case TOKEN_MINUS:
        compiler.emitByte(OP_NEGATE);

        break;
      default: //Unreachable
        return;
    }
  }
}
