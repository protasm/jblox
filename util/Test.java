package jblox.util;

import jblox.vm.Value;
import jblox.vm.Value.ValueType;

class Test {
  //Test()
  public Test() {
    LoxValueStack stack = new LoxValueStack();

    stack.push(new Value(ValueType.VAL_STRING, "One"));
    stack.push(new Value(ValueType.VAL_STRING, "Two"));
    stack.push(new Value(ValueType.VAL_NUMBER, 3));
    stack.push(new Value(ValueType.VAL_BOOL, true));
    stack.push(new Value(ValueType.VAL_NIL, null));
    System.out.println(stack);

    System.out.println("stack.pop():");
    System.out.println(stack.pop());
    System.out.println(stack);

    System.out.println("stack.peek():");
    System.out.println(stack.peek());

    System.out.println("stack.push(9):");
    stack.push(new Value(ValueType.VAL_NUMBER, 9));;
    System.out.println(stack);

    System.out.println("stack.set(3, 'FOO'):");
    stack.set(3, new Value(ValueType.VAL_STRING, "FOO"));
    System.out.println(stack);

    System.out.println("stack.get(2):");
    System.out.println(stack.get(2));

    System.out.println("stack.truncate(4):");
    stack.truncate(4);
    System.out.println(stack);

    System.out.println("stack.reset():");
    stack.reset();
    System.out.println(stack);

    System.out.println("stack.push(3/2):");
    double d = (double)3/2;
    stack.push(new Value(ValueType.VAL_NUMBER, d));
    System.out.println(stack);
  }

  public static void main(String[] args) {
    Test test = new Test();
  }
}
