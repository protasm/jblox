package jblox.vm;

import jblox.compiler.Chunk;
import jblox.compiler.Compiler;
import jblox.compiler.Function;
import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.nativefn.NativeClock;
import jblox.nativefn.NativeFn;
import jblox.util.LoxArray;
import jblox.util.LoxCallFrameStack;
import jblox.util.LoxIntArray;
import jblox.util.LoxMap;
import jblox.util.LoxStack;
import jblox.util.LoxValueMap;
import jblox.util.LoxValueStack;
import jblox.vm.Value.ValueType;

import static jblox.compiler.OpCode.*;

public class VM {
  //InterpretResult
  public enum InterpretResult {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR,
  }

  //Operation
  private enum Operation {
    OPERATION_PLUS,
    OPERATION_SUBTRACT,
    OPERATION_MULT,
    OPERATION_DIVIDE,
    OPERATION_GT,
    OPERATION_LT,
  }

  private Props properties;
  private Debugger debugger;
  private Compiler compiler;
  private LoxValueMap globals;
  private LoxValueStack stack;
  private LoxCallFrameStack frames;
  private String initString;
  private boolean debugMaster;
  private boolean debugPrintProgress;
  private boolean debugTraceExecution;
  private Upvalue openUpvalues; //linked list

  //VM()
  public VM(Props properties, Debugger debugger) {
    this.properties = properties;
    this.debugger = debugger;

    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugTraceExecution = debugMaster && properties.getBool("DEBUG_TRACE_EXECUTION");

    if (debugPrintProgress) debugger.printProgress("Initializing VM....");

    compiler = new Compiler(properties, debugger);
    globals = new LoxValueMap();
    stack = new LoxValueStack();
    frames = new LoxCallFrameStack();
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
      CallFrame frame = frames.get(i);
      Function function = frame.closure().function();
      LoxIntArray lines = function.chunk().lines();
      int line = lines.get(frame.ip() - 1);

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
    globals.put(name, new Value(ValueType.VAL_NATIVEFN, nativeFn));
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
    int maxFrames = properties.getInt("MAX_FRAMES");

    if (frames.count() == maxFrames) {
      runtimeError("Stack overflow.");

      return false;
    }

    CallFrame frame = new CallFrame(closure, base);
    frames.push(frame);

    return true;
  }

  //callValue(Value, int)
  private boolean callValue(Value callee, int argCount) {
    if (callee.isBoundMethod()) {
      BoundMethod bound = callee.asBoundMethod();

      stack.set(stack.top() - argCount, bound.receiver());

      return call(bound.method(), argCount);
    } else if (callee.isLoxClass()) {
      LoxClass klass = callee.asLoxClass();
      LoxInstance instance = new LoxInstance(klass);
      Value value = new Value(ValueType.VAL_LOXINSTANCE, instance);

      stack.set(stack.top() - argCount, value);

      Closure initializer = klass.methods().get(initString);

      if (initializer != null)
        return call(initializer, argCount);
      else if (argCount != 0) {
        runtimeError("Expected 0 arguments but got " + argCount + ".");

        return false;
      }

      return true;
    } else if (callee.isClosure())
      return call(callee.asClosure(), argCount);
    else if (callee.isNativeFn()) {
      NativeFn nativeFn = callee.asNativeFn();
      Value[] args = new Value[argCount];

      for (int i = 0; i < argCount; i++)
        args[i] = stack.get(stack.count() - argCount + i);

      Value result = nativeFn.execute(args);

      //pop args plus native function
      stack.truncate(stack.top() - argCount);

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

    Closure method = klass.methods().get(name);

    return call(method, argCount);
  }

  //invoke(String, int)
  private boolean invoke(String name, int argCount) {
    Value receiver = stack.get(stack.top() - argCount);

    if (!(receiver.isLoxInstance())) {
      runtimeError("Only instances have methods.");

      return false;
    }

    LoxInstance instance = receiver.asLoxInstance();

    if (instance.fields().containsKey(name)) {
      Value value = instance.fields().get(name);

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

    Closure method = klass.methods().get(name);
    BoundMethod bound = new BoundMethod(stack.peek(), method);
    Value value = new Value(ValueType.VAL_BOUNDMETHOD, bound);

    stack.pop();

    stack.push(value);

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
    Closure method = stack.peek().asClosure();
    LoxClass klass = stack.get(stack.top() - 1).asLoxClass();

    klass.methods().put(name, method);

    stack.pop();
  }

  //isFalsey(Value)
  boolean isFalsey(Value value) {
    //nil and false are falsey and every other value behaves like true.
    return value.isNil() || (value.isBool() && !(value.asBool()));
  }

  //concatenate()
  private void concatenate() {
    String b = stack.pop().asString();
    String a = stack.pop().asString();

    //stack.push(new Value(ValueType.VAL_STRING, a + b));
    stack.push(new Value(a + b));
  }

  //equate()
  private void equate() {
    Value b = stack.pop();
    Value a = stack.pop();

    //stack.push(new Value(ValueType.VAL_BOOL, a.equals(b)));
    stack.push(new Value(a.equals(b)));
  }

  //interpret(String)
  public InterpretResult interpret(String source) {
    Function function = compiler.compile(source);

    if (debugPrintProgress)
      debugger.printProgress("Executing....");

    if (function == null)
      return InterpretResult.INTERPRET_COMPILE_ERROR;

    Closure closure = new Closure(function);
    Value value = new Value(ValueType.VAL_CLOSURE, closure);

    stack.push(value);

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
  private Value readConstant(CallFrame frame) {
    short index = readWord(frame);

    return frame.closure().function().chunk().constants().get(index);
  }

  //readString(CallFrame)
  private String readString(CallFrame frame) {
    return readConstant(frame).asString();
  }

  //run()
  private InterpretResult run() {
    CallFrame frame = frames.peek();

    //Bytecode dispatch loop.
    for (;;) {
      if (debugTraceExecution)
        debugger.traceExecution(frame, globals, stack);

      switch (readByte(frame)) {
        case OP_CONSTANT: stack.push(readConstant(frame)); break;
        //case OP_NIL:      stack.push(new Value(ValueType.VAL_NIL, null)); break;
        case OP_NIL:      stack.push(new Value()); break;
        //case OP_TRUE:     stack.push(new Value(ValueType.VAL_BOOL, true)); break;
        case OP_TRUE:     stack.push(new Value(true)); break;
        //case OP_FALSE:    stack.push(new Value(ValueType.VAL_BOOL, false)); break;
        case OP_FALSE:    stack.push(new Value(false)); break;
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

          Value global = globals.get(ggKey);

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
          Value gpValue = stack.peek();

          if (!(gpValue.isLoxInstance())) {
            runtimeError("Only instances have properties.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance gpInstance = gpValue.asLoxInstance();
          String name = readString(frame);

          if (gpInstance.fields().containsKey(name)) {
            stack.pop(); // Instance.

            stack.push(gpInstance.fields().get(name));

            break;
          }

          if (!bindMethod(gpInstance.klass(), name))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_SET_PROPERTY:
          Value spValue = stack.get(stack.top() - 1);

          if (!(spValue.isLoxInstance())) {
            runtimeError("Only instances have fields.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance spInstance = spValue.asLoxInstance();

          spInstance.fields().put(readString(frame), stack.peek());

          //pop value that was set plus instance object
          Value setValue = stack.pop();
          stack.pop();

          //push value back on stack
          stack.push(setValue);

          break;
        case OP_GET_SUPER:
          String gsName = readString(frame);
          LoxClass gsSuperclass = stack.pop().asLoxClass();

          if (!bindMethod(gsSuperclass, gsName))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_EQUAL: equate(); break;
        case OP_GREATER:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(Operation.OPERATION_GT);

          break;
        case OP_LESS:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(Operation.OPERATION_LT);

          break;
        case OP_ADD:
          if (twoStringOperands())
            concatenate();
          else if (twoNumericOperands())
            binaryOp(Operation.OPERATION_PLUS);
          else
            return errorTwoNumbersOrStrings();

          break;
        case OP_SUBTRACT:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(Operation.OPERATION_SUBTRACT);

          break;
        case OP_MULTIPLY:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(Operation.OPERATION_MULT);

          break;
        case OP_DIVIDE:
          if (!twoNumericOperands())
            return errorTwoNumbers();

          binaryOp(Operation.OPERATION_DIVIDE);

          break;
        case OP_NOT:
          //stack.push(new Value(ValueType.VAL_BOOL, isFalsey(stack.pop())));
          stack.push(new Value(isFalsey(stack.pop())));

          break;
        case OP_NEGATE:
          if (!oneNumericOperand())
            return errorOneNumber();

          //stack.push(new Value(ValueType.VAL_NUMBER, -(stack.pop().asNumber())));
          stack.push(new Value(-(stack.pop().asNumber())));

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
          Value callee = stack.get(stack.top() - callArgCount);

          if (!callValue(callee, callArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = frames.peek();

          break;
        case OP_INVOKE:
          String invMethod = readString(frame);
          int invArgCount = readByte(frame);

          if (!invoke(invMethod, invArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = frames.peek();

          break;
        case OP_SUPER_INVOKE:
          String siMethod = readString(frame);
          int siArgCount = readByte(frame);
          LoxClass siSuperclass = stack.pop().asLoxClass();

          if (!invokeFromClass(siSuperclass, siMethod, siArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = frames.peek();

          break;
        case OP_CLOSURE:
          Function function = readConstant(frame).asFunction();
          Closure closure = new Closure(function);

          stack.push(new Value(ValueType.VAL_CLOSURE, closure));

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
          Value result = stack.pop();

          closeUpvalues(frame.base());

          frames.pop();

          if (frames.count() == 0) {
            stack.pop();

            return InterpretResult.INTERPRET_OK;
          }

          stack.truncate(frame.base());

          stack.push(result);

          frame = frames.peek();

          break;
        case OP_CLASS:
          LoxClass classLoxClass = new LoxClass(readString(frame));
          Value classVal = new Value(ValueType.VAL_LOXCLASS, classLoxClass);

          stack.push(classVal);

          break;
        case OP_INHERIT:
          Value superclass = stack.get(stack.top() - 1);

          if (!(superclass.isLoxClass())) {
            runtimeError("Superclass must be a class.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxClass subclass = stack.peek().asLoxClass();

          subclass.inheritMethods(superclass.asLoxClass().methods());

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
    return stack.peek().isNumber();
  }

  //errorOneNumber()
  private InterpretResult errorOneNumber() {
    return error("Operand must be a number");
  }

  //twoNumericOperands()
  private boolean twoNumericOperands() {
    Value first = stack.pop(); //temporarily pop
    Value second = stack.peek();

    stack.push(first); //push back again

    return first.isNumber() && second.isNumber();
  }

  //errorTwoNumbers()
  private InterpretResult errorTwoNumbers() {
    return error("Operands must be two numbers.");
  }

  //twoStringOperands()
  private boolean twoStringOperands() {
    Value first = stack.pop(); //temporarily pop
    Value second = stack.peek();

    stack.push(first); //push back again

    return first.isString() && second.isString();
  }

  //errorTwoStrings()
  private InterpretResult errorTwoStrings() {
    return error("Operands must be two strings.");
  }

  //errorTwoNumbersOrStrings()
  private InterpretResult errorTwoNumbersOrStrings() {
    return error("Operands must be two numbers or two strings.");
  }

  //error(String)
  private InterpretResult error(String message) {
    runtimeError(message);

    return InterpretResult.INTERPRET_RUNTIME_ERROR;
  }

  //binaryOp(Operation)
  private void binaryOp(Operation op) {
    double b = stack.pop().asNumber();
    double a = stack.pop().asNumber();

    switch (op) {
      case OPERATION_PLUS:
        //stack.push(new Value(ValueType.VAL_NUMBER, a + b));
        stack.push(new Value(a + b));

        break;
      case OPERATION_SUBTRACT:
        //stack.push(new Value(ValueType.VAL_NUMBER, a - b));
        stack.push(new Value(a - b));

        break;
      case OPERATION_MULT:
        //stack.push(new Value(ValueType.VAL_NUMBER, a * b));
        stack.push(new Value(a * b));

        break;
      case OPERATION_DIVIDE:
        //stack.push(new Value(ValueType.VAL_NUMBER, a / b));
        stack.push(new Value(a / b));

        break;
      case OPERATION_GT:
        //stack.push(new Value(ValueType.VAL_BOOL, a > b));
        stack.push(new Value(a > b));

        break;
      case OPERATION_LT:
        //stack.push(new Value(ValueType.VAL_BOOL, a < b));
        stack.push(new Value(a < b));

        break;
    } //switch
  }
}
