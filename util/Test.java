package jblox.util;

class Test {
  //Test()
  public Test() {
    LoxStack stack = new LoxStack(1024);

    stack.push(new String[] { "One", "Two", "Three", "Four", "Five" } );
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.pop():");
    System.out.println(stack.pop());
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.peek():");
    System.out.println(stack.peek());
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.push('Six'):");
    stack.push("Six");
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.peek(2, false):");
    Object[] values = stack.peek(2, false);
    System.out.println(java.util.Arrays.toString(values));
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.peek(2, true):");
    values = stack.peek(2, true);
    System.out.println(java.util.Arrays.toString(values));
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.pop(2, false):");
    values = stack.pop(2, false);
    System.out.println(java.util.Arrays.toString(values));
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.push({ 'Seven', 'Eight', 'Nine' }):");
    stack.push(new String[] { "Seven", "Eight", "Nine"} );
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.get(2):");
    System.out.println(stack.get(2));
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.set(3, 'FOO'):");
    stack.set(3, "FOO");
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");

    System.out.println("stack.truncate(4):");
    stack.truncate(4);
    System.out.println(stack);
    System.out.println("Count: " + stack.count() + "\n");
  }

  public static void main(String[] args) {
    Test test = new Test();
  }
}
