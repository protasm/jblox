package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.compiler.OpCode.*;
import static jblox.parser.Parser.Precedence.*;
import static jblox.scanner.TokenType.*;

public class CallParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    byte argCount = compiler.argumentList();

    compiler.emitByte(OP_CALL);
    compiler.emitByte(argCount);
  }
}
