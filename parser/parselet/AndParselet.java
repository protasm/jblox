package parser.parselet;

import compiler.Compiler;

import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;

public class AndParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    int endJump = compiler.emitJump(OP_JUMP_IF_FALSE);

    compiler.emitByte(OP_POP);
    compiler.parsePrecedence(PREC_AND);

    compiler.patchJump(endJump);
  }
}
