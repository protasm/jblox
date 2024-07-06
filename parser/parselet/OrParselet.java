package parser.parselet;

import compiler.Compiler;

import static compiler.OpCode.*;
import static parser.Parser.Precedence.*;

public class OrParselet implements Parselet {
  //parse(compiler.Compiler, boolean)
  public void parse(compiler.Compiler compiler, boolean canAssign) {
    int elseJump = compiler.emitJump(OP_JUMP_IF_FALSE);
    int endJump = compiler.emitJump(OP_JUMP);

    compiler.patchJump(elseJump);
    compiler.emitByte(OP_POP);

    compiler.parsePrecedence(PREC_OR);
    compiler.patchJump(endJump);
  }
}
