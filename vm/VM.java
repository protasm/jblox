package jblox.vm;

import jblox.Defaults;
import jblox.compiler.Chunk;
import jblox.compiler.Compiler;
import jblox.compiler.Function;
import jblox.debug.Debugger;
import jblox.nativefn.NativeClock;
import jblox.nativefn.NativeFn;
import jblox.util.LoxArray;
import jblox.util.LoxMap;
import jblox.util.LoxStack;
import jblox.vm.Closure;

import static jblox.compiler.OpCode.*;

public class VM {
  //InterpretResult
  public static enum InterpretResult {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
  }

  //Operation
  private static enum Operation {
    OPERATION_PLUS,
    OPERATION_SUBTRACT,
    OPERATION_MULT,
    OPERATION_DIVIDE,
    OPERATION_GT,
    OPERATION_LT,
  }

  private Defaults defaults;
  private Debugger debugger;
  private Compiler compiler;
  private LoxMap globals;
  private LoxStack stack;
  private LoxStack frames;
  private String initString;
  private boolean debugMaster;
  private boolean debugPrintProgress;
  private boolean debugTraceExecution;
  private Upvalue openUpvalues; //linked list

  //VM()
  public VM(Defaults defaults, Debugger debugger) {
    this.defaults = defaults;
    this.debugger = debugger;

    debugMaster = defaults.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && defaults.getBool("DEBUG_PRINT_PROGRESS");
    debugTraceExecution = debugMaster && defaults.getBool("DEBUG_TRACE_EXECUTION");

    if (debugPrintProgress) debugger.printProgress("Initializing VM....");

    compiler = new Compiler(defaults, debugger);
    globals = new LoxMap();
    stack = new LoxStack(defaults.getInt("MAX_STACK"));
    frames = new LoxStack(defaults.getInt("MAX_FRAMES"));
    initString = "init";

    defineNativeFn("clock", new NativeClock());

    reset();
  }

  //reset()
  private void reset() {
    stack.reset();
    frames.reset();
    openUpvalues = null;
  }

  //runtimeError(String, String...)
  void runtimeError(String message, String... args) {
    System.err.println("Runtime Error: " + message);

    for (String s : args)
      System.out.println(s);

    for (int i = frames.count() - 1; i >= 0; i--) {
      CallFrame frame = (CallFrame)frames.get(i);
      Function function = frame.closure().function();
      LoxArray lines = function.chunk().lines();
      int line = (int)lines.get(frame.ip() - 1);

      System.err.print("[line " + line + "] in ");

      if (function.name() == null)
        System.err.print("script.\n");
      else
        System.err.print(function.name() + "().\n");
    }

    reset();
  }

  //defineNativeFn(String, NativeFn)
  private void defineNativeFn(String name, NativeFn nativeFn) {
    globals.put(name, nativeFn);
  }

  //call(Closure, int)
  private boolean call(Closure closure, int argCount) {
    int arity = closure.function().arity();

    if (argCount != arity) {
      runtimeError(
        "Expected " + arity + " arguments but got " + argCount + "."
      );

      return false;
    }

    //CallFrame window on VM stack begins at slot
    //occupied by function.
    int base = stack.top() - argCount;
    int maxFrames = defaults.getInt("MAX_FRAMES");

    if (frames.count() == maxFrames) {
      runtimeError("Stack overflow.");

      return false;
    }

    frames.push(new CallFrame(closure, base));

    return true;
  }

  //callValue(Object, int)
  private boolean callValue(Object callee, int argCount) {
    if (callee instanceof BoundMethod) {
      BoundMethod bound = (BoundMethod)callee;

      //vm.stackTop[-argCount - 1] = bound->receiver;
      stack.set(stack.top() - argCount, bound.receiver());

      return call(bound.method(), argCount);
    } else if (callee instanceof LoxClass) {
      LoxClass klass = (LoxClass)callee;
      LoxInstance instance = new LoxInstance(klass);

      stack.set(stack.top() - argCount, instance);

      Object initializer = klass.methods().get(initString);

      if (initializer != null)
        return call((Closure)initializer, argCount);
      else if (argCount != 0) {
        runtimeError("Expected 0 arguments but got " + argCount + ".");

        return false;
      }

      return true;
    } else if (callee instanceof Closure)
      return call((Closure)callee, argCount);
    else if (callee instanceof NativeFn) {
      NativeFn nativeFn = (NativeFn)callee;
      Object[] args;

      if (argCount == 0)
        args = new Object[0];
      else
        args = stack.peek(argCount, true);

      Object result = nativeFn.execute(args);

      //pop args plus native function
      stack.pop(argCount + 1, false);

      stack.push(result);

      return true;
    }

    runtimeError("Can only call functions and classes.");

    return false;
  }

  //invokeFromClass(LoxClass, String, int)
  private boolean invokeFromClass(LoxClass klass, String name, int argCount) {
    if (!(klass.methods().containsKey(name))) {
      runtimeError("Undefined property '" + name + "'.");

      return false;
    }

    Object method = klass.methods().get(name);

    return call((Closure)method, argCount);
  }

  //invoke(String, int)
  private boolean invoke(String name, int argCount) {
    Object receiver = stack.get(stack.top() - argCount);

    if (!(receiver instanceof LoxInstance)) {
      runtimeError("Only instances have methods.");

      return false;
    }

    LoxInstance instance = (LoxInstance)receiver;

    if (instance.fields().containsKey(name)) {
      Object value = instance.fields().get(name);

      stack.set(stack.top() - argCount, value);

      return callValue(value, argCount);
    }

    return invokeFromClass(instance.klass(), name, argCount);
  }

  //bindMethod(LoxClass, String)
  private boolean bindMethod(LoxClass klass, String name) {
    if (!klass.methods().containsKey(name)) {
      runtimeError("Undefined property '" + name + "'.");

      return false;
    }

    Object method = klass.methods().get(name);

    BoundMethod bound = new BoundMethod(stack.peek(), (Closure)method);

    stack.pop();

    stack.push(bound);

    return true;
  }

  //captureUpvalue(int)
  Upvalue captureUpvalue(int location) {
    Upvalue prevUpvalue = null;
    Upvalue currUpvalue = openUpvalues; //start at head of list

    while (currUpvalue != null && currUpvalue.location() > location) {
      prevUpvalue = currUpvalue;

      currUpvalue = currUpvalue.next();
    }

    if (currUpvalue != null && currUpvalue.location() == location)
      return currUpvalue;

    //create new Upvalue and insert into linked list of
    //open upvalues between previous and current
    Upvalue createdUpvalue = new Upvalue(location);

    createdUpvalue.setNext(currUpvalue);

    if (prevUpvalue == null)
      openUpvalues = createdUpvalue; //new head of list
    else
      prevUpvalue.setNext(createdUpvalue); //insert into list

    return createdUpvalue;
  }

  //closeUpvalues(int)
  void closeUpvalues(int last) {
    while (openUpvalues != null && openUpvalues.location() >= last) {
      Upvalue upvalue = openUpvalues;

      upvalue.setClosedValue(stack.get(upvalue.location()));
      upvalue.setLocation(-1);

      //after upvalue is closed, reset head of linked list
      //to next upvalue
      openUpvalues = upvalue.next();
      upvalue.setNext(null);
    }
  }

  //defineMethod(String)
  void defineMethod(String name) {
    Closure method = (Closure)stack.peek();
    LoxClass klass = (LoxClass)stack.get(stack.top() - 1);

    klass.methods().put(name, method);

    stack.pop();
  }

  //isFalsey(Object)
  boolean isFalsey(Object value) {
    //nil and false are falsey and every other value behaves like true.
    return value == null || (value instanceof Boolean) && !((boolean)value);
  }

  //concatenate()
  private void concatenate() {
    String b = (String)stack.pop();
    String a = (String)stack.pop();

    stack.push(a + b);
  }

  //equate()
  private void equate() {
    Object b = stack.pop();
    Object a = stack.pop();

    if (a == null)
      stack.push(a == b);
    else
      stack.push(a.equals(b));
  }

  //interpret(String)
  public VM.InterpretResult interpret(String source) {
    Function function = compiler.compile(source);

    if (debugPrintProgress)
      debugger.printProgress("Executing....");

    if (function == null)
      return VM.InterpretResult.INTERPRET_COMPILE_ERROR;

    Closure closure = new Closure(function);

    stack.push(closure);

    call(closure, 0);

    return run();
  }

  //readByte(CallFrame)
  private byte readByte(CallFrame frame) {
    return (byte)frame.closure().function().chunk().codes().get(frame.getAndIncrementIP());
  }

  //readWord(CallFrame)
  private short readWord(CallFrame frame) {
    byte hi = readByte(frame);
    byte lo = readByte(frame);

    short s = (short)(((hi & 0xFF) << 8) | (lo & 0xFF));

    return s;
  }

  //readConstant(CallFrame)
  private Object readConstant(CallFrame frame) {
    short index = readWord(frame);

    return frame.closure().function().chunk().constants().get(index);
  }

  //readString(CallFrame)
  private String readString(CallFrame frame) {
    return (String)readConstant(frame);
  }

  //run()
  private VM.InterpretResult run() {
    CallFrame frame = (CallFrame)frames.peek();

    //Bytecode dispatch loop.
    for (;;) {
      if (debugTraceExecution)
        debugger.traceExecution(frame, globals, stack);

      switch (readByte(frame)) {
        case OP_CONSTANT: stack.push(readConstant(frame)); break;
        case OP_NIL:      stack.push((Object)null); break;
        case OP_TRUE:     stack.push(true); break;
        case OP_FALSE:    stack.push(false); break;
        case OP_POP:      stack.pop(); break;
        case OP_GET_LOCAL:
          short glSlot = readWord(frame);

          stack.push(stack.get(frame.base() + glSlot));

          break;
        case OP_SET_LOCAL:
          short slSlot = readWord(frame);

          stack.set(frame.base() + slSlot, stack.peek());

          break;
        case OP_GET_GLOBAL:
          String ggKey = readString(frame);

          if (!globals.containsKey(ggKey))
            return error("Undefined variable '" + ggKey + "'.");

          Object global = globals.get(ggKey);

          stack.push(global);

          break;
        case OP_DEFINE_GLOBAL:
          String dgKey = readString(frame);

          globals.put(dgKey, stack.peek());

          stack.pop();

          break;
        case OP_SET_GLOBAL:
          String sgKey = readString(frame);

          if (!globals.containsKey(sgKey))
            return error("Undefined variable '" + sgKey + "'.");

          //Peek here, not pop; assignment is an expression,
          //so we leave value stacked in case the assignment
          //is nested inside a larger expression.
          globals.put(sgKey, stack.peek());

          break;
        case OP_GET_UPVALUE:
          short guSlot = readWord(frame);
          Upvalue guUpvalue = frame.closure().upvalues()[guSlot];

          if (guUpvalue.location() != -1) //i.e., open
            stack.push(stack.get(guUpvalue.location()));
          else //i.e., closed
            stack.push(guUpvalue.closedValue());

          break;
        case OP_SET_UPVALUE:
          short suSlot = readWord(frame);
          Upvalue suUpvalue = frame.closure().upvalues()[suSlot];

          if (suUpvalue.location() != -1) //i.e., open
            stack.set(suUpvalue.location(), stack.peek());
          else //i.e., closed
            suUpvalue.setClosedValue(stack.peek());

          break;
        case OP_GET_PROPERTY:
          Object gpValue = stack.peek();

          if (!(gpValue instanceof LoxInstance)) {
            runtimeError("Only instances have properties.");

            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance gpInstance = (LoxInstance)gpValue;
          String name = readString(frame);

          if (gpInstance.fields().containsKey(name)) {
            stack.pop(); // Instance.

            stack.push(gpInstance.fields().get(name));

            break;
          }

          if (!bindMethod(gpInstance.klass(), name))
            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_SET_PROPERTY:
          Object spValue = stack.get(stack.top() - 1);

          if (!(spValue instanceof LoxInstance)) {
            runtimeError("Only instances have fields.");

            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance spInstance = (LoxInstance)spValue;

          spInstance.fields().put(readString(frame), stack.peek());

          //pop value that was set plus instance object
          Object[] values = stack.pop(2, false);

          //push value back on stack
          stack.push(values[0]);

          break;
        case OP_GET_SUPER:
          String gsName = readString(frame);
          LoxClass gsSuperclass = (LoxClass)stack.pop();

          if (!bindMethod(gsSuperclass, gsName))
            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_EQUAL: equate(); break;
        case OP_GREATER:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(VM.Operation.OPERATION_GT);

          break;
        case OP_LESS:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(VM.Operation.OPERATION_LT);

          break;
        case OP_ADD:
          if (twoStringOperands())
            concatenate();
          else if (twoNumericOperands())
            binaryOp(VM.Operation.OPERATION_PLUS);
          else
            return errorTwoNumbersOrStrings();

          break;
        case OP_SUBTRACT:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(VM.Operation.OPERATION_SUBTRACT);

          break;
        case OP_MULTIPLY:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(VM.Operation.OPERATION_MULT);

          break;
        case OP_DIVIDE:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(VM.Operation.OPERATION_DIVIDE);

          break;
        case OP_NOT:
          stack.push(isFalsey(stack.pop()));

          break;
        case OP_NEGATE:
          if (!oneNumericOperand())
            return errorOneNumber();

          stack.push(-((Double)stack.pop()));

          break;
        case OP_PRINT:
          System.out.println(stack.pop());

          break;
        case OP_JUMP:
          short jumpOffset = readWord(frame);

          frame.setIP(frame.ip() + jumpOffset);

          break;
        case OP_JUMP_IF_FALSE:
          short jumpIfFalseOffset = readWord(frame);

          if (isFalsey(stack.peek()))
            frame.setIP(frame.ip() + jumpIfFalseOffset);

          break;
        case OP_LOOP:
          short loopOffset = readWord(frame);

          frame.setIP(frame.ip() - loopOffset);

          break;
        case OP_CALL:
          int callArgCount = readByte(frame);
          Object callee = stack.get(stack.top() - callArgCount);

          if (!callValue(callee, callArgCount))
            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = (CallFrame)frames.peek();

          break;
        case OP_INVOKE:
          String invMethod = readString(frame);
          int invArgCount = readByte(frame);

          if (!invoke(invMethod, invArgCount))
            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = (CallFrame)frames.peek();

          break;
        case OP_SUPER_INVOKE:
          String siMethod = readString(frame);
          int siArgCount = readByte(frame);
          LoxClass siSuperclass = (LoxClass)stack.pop();

          if (!invokeFromClass(siSuperclass, siMethod, siArgCount))
            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = (CallFrame)frames.peek();

          break;
        case OP_CLOSURE:
          Function function = (Function)readConstant(frame);
          Closure closure = new Closure(function);

          stack.push(closure);

          for (int i = 0; i < closure.upvalueCount(); i++) {
            byte isLocal = readByte(frame);
            byte index = readByte(frame);

            if (isLocal != 0)
              closure.upvalues()[i] = captureUpvalue(frame.base() + index);
            else
              closure.upvalues()[i] = frame.closure().upvalues()[index];
          }

          break;
        case OP_CLOSE_UPVALUE:
          //close the upvalue at the top of the stack
          closeUpvalues(stack.top());

          stack.pop();

          break;
        case OP_RETURN:
          Object result = stack.pop();

          closeUpvalues(frame.base());

          frames.pop();

          if (frames.count() == 0) {
            stack.pop();

            return VM.InterpretResult.INTERPRET_OK;
          }

          stack.truncate(frame.base());

          stack.push(result);

          frame = (CallFrame)frames.peek();

          break;
        case OP_CLASS:
          stack.push(new LoxClass(readString(frame)));

          break;
        case OP_INHERIT:
          Object superclass = stack.get(stack.top() - 1);

          if (!(superclass instanceof LoxClass)) {
            runtimeError("Superclass must be a class.");

            return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxClass subclass = (LoxClass)stack.peek();

          subclass.inheritMethods(((LoxClass)superclass).methods());

          stack.pop(); // Subclass.

          break;
        case OP_METHOD:
          defineMethod(readString(frame));

          break;
      } //switch
    } //for(;;)
  }

  //oneNumericOperand()
  private boolean oneNumericOperand() {
    return stack.peek() instanceof Number;
  }

  //errorOneNumber()
  private VM.InterpretResult errorOneNumber() {
    return error("Operand must be a number");
  }

  //twoNumericOperands()
  private boolean twoNumericOperands() {
    Object first = stack.pop(); //temporarily pop
    Object second = stack.peek();

    stack.push(first); //push back again

    return (
      first instanceof Number &&
      second instanceof Number
    );
  }

  //errorTwoNumbers()
  private VM.InterpretResult errorTwoNumbers() {
    return error("Operands must be two numbers.");
  }

  //twoStringOperands()
  private boolean twoStringOperands() {
    Object first = stack.pop(); //temporarily pop
    Object second = stack.peek();

    stack.push(first); //push back again

    return (
      first instanceof String &&
      second instanceof String
    );
  }

  //errorTwoStrings()
  private VM.InterpretResult errorTwoStrings() {
    return error("Operands must be two strings.");
  }

  //errorTwoNumbersOrStrings()
  private VM.InterpretResult errorTwoNumbersOrStrings() {
    return error("Operands must be two numbers or two strings.");
  }

  //error(String)
  private VM.InterpretResult error(String message) {
    runtimeError(message);

    return VM.InterpretResult.INTERPRET_RUNTIME_ERROR;
  }

  //binaryOp(VM.Operation)
  private void binaryOp(VM.Operation op) {
    Double b = ((Number)stack.pop()).doubleValue();
    Double a = ((Number)stack.pop()).doubleValue();

    switch (op) {
      case OPERATION_PLUS:
        stack.push(a + b); break;
      case OPERATION_SUBTRACT:
        stack.push(a - b); break;
      case OPERATION_MULT:
        stack.push(a * b); break;
      case OPERATION_DIVIDE:
        stack.push(a / b); break;
      case OPERATION_GT:
        stack.push(a > b); break;
      case OPERATION_LT:
        stack.push(a < b); break;
    } //switch
  }
}
