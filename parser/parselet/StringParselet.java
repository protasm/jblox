package jblox.parser.parselet;

import jblox.compiler.Compiler;
import jblox.vm.Value;
import jblox.vm.Value.ValueType;

public class StringParselet implements Parselet {
  //parse(jblox.compiler.Compiler, boolean)
  public void parse(jblox.compiler.Compiler compiler, boolean canAssign) {
    //Value value = new Value(ValueType.VAL_STRING, compiler.parser().previous().literal());
    Value value = new Value((String)compiler.parser().previous().literal());

    compiler.emitConstant(value);
  }
}
