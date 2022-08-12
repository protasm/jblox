package jblox.util;

import jblox.vm.Value;
import jblox.vm.Value.ValueType;

class Test {
  //Test()
  public Test() {
    LoxValueStack stack = new LoxValueStack();

    stack.push(new Value("One"));
    stack.push(new Value("Two"));
    stack.push(new Value(3));
    stack.push(new Value(true));
    stack.push(new Value());
    stack.push(new Value((double)10/3));
    System.out.println("Start: \"One\", \"Two\", 3, true, null, 10/3");
    System.out.println(stack + "\n");

    System.out.print("stack.pop(): ");
    System.out.print(stack.pop() + "\n");
    System.out.println(stack + "\n");

    System.out.print("stack.peek(): ");
    System.out.print(stack.peek() + "\n");
    System.out.println(stack + "\n");

    System.out.println("stack.push(9):");
    stack.push(new Value(9));
    System.out.println(stack + "\n");

    System.out.println("stack.set(3, 'FOO'):");
    stack.set(3, new Value("FOO"));
    System.out.println(stack + "\n");

    System.out.print("stack.get(2): ");
    System.out.print(stack.get(2) + "\n");
    System.out.println(stack + "\n");

    System.out.println("stack.truncate(4):");
    stack.truncate(4);
    System.out.println(stack + "\n");

    System.out.println("stack.reset():");
    stack.reset();
    System.out.println(stack + "\n");

    System.out.println("stack.push(3/2):");
    stack.push(new Value((double)3/2));
    System.out.println(stack + "\n");
  }

  public static void main(String[] args) {
    Test test = new Test();
  }
}
