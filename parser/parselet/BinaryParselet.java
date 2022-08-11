package jblox.parser.parselet;

import jblox.compiler.Compiler;
import jblox.parser.ParseRule;
import jblox.scanner.TokenType;

import static jblox.compiler.OpCode.*;
import static jblox.scanner.TokenType.*;

public class BinaryParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    TokenType operatorType = compiler.parser().previous().type();
    ParseRule rule = compiler.getRule(operatorType);

    compiler.parsePrecedence(rule.precedence() + 1);

    switch (operatorType) {
      case TOKEN_BANG_EQUAL:
        compiler.emitByte(OP_EQUAL);
        compiler.emitByte(OP_NOT);

        break;
      case TOKEN_EQUAL_EQUAL:
        compiler.emitByte(OP_EQUAL);

        break;
      case TOKEN_GREATER:
        compiler.emitByte(OP_GREATER);

        break;
      case TOKEN_GREATER_EQUAL:
        compiler.emitByte(OP_LESS);
        compiler.emitByte(OP_NOT);

        break;
      case TOKEN_LESS:
        compiler.emitByte(OP_LESS);

        break;
      case TOKEN_LESS_EQUAL:
        compiler.emitByte(OP_GREATER);
        compiler.emitByte(OP_NOT);

        break;
      case TOKEN_PLUS:
        compiler.emitByte(OP_ADD);

        break;
      case TOKEN_MINUS:
        compiler.emitByte(OP_SUBTRACT);

        break;
      case TOKEN_STAR:
        compiler.emitByte(OP_MULTIPLY);

        break;
      case TOKEN_SLASH:
        compiler.emitByte(OP_DIVIDE);

        break;
      default:  //Unreachable
        return;
    }
  }
}
