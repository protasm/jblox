package jblox.parser.parselet;

import jblox.compiler.Compiler;

import static jblox.compiler.OpCode.*;
import static jblox.parser.Parser.Precedence.*;

public class OrParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    int elseJump = compiler.emitJump(OP_JUMP_IF_FALSE);
    int endJump = compiler.emitJump(OP_JUMP);

    compiler.patchJump(elseJump);
    compiler.emitByte(OP_POP);

    compiler.parsePrecedence(PREC_OR);
    compiler.patchJump(endJump);
  }
}
