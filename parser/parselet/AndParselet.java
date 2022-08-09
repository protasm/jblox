package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.compiler.OpCode.*;
import static jblox.parser.Parser.Precedence.*;

public class AndParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    int endJump = compiler.emitJump(OP_JUMP_IF_FALSE);

    compiler.emitByte(OP_POP);
    compiler.parsePrecedence(PREC_AND);

    compiler.patchJump(endJump);
  }
}
