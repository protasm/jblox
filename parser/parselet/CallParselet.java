package parser.parselet;

import compiler.Compiler;

import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;
import static scanner.TokenType.*;

public class CallParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    byte argCount = compiler.argumentList();

    compiler.emitByte(OP_CALL);
    compiler.emitByte(argCount);
  }
}
