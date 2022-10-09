package jblox.vm;

import java.lang.Math;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jblox.compiler.Chunk;
import jblox.compiler.Compiler;
import jblox.compiler.Function;
import jblox.compiler.HasArity;
import jblox.debug.Debugger;
import jblox.main.Props;
import jblox.main.PropsObserver;
import jblox.nativefn.*;

import static jblox.compiler.OpCode.*;

public class VM implements PropsObserver {
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
  private Map<String, Object> globals;
  private Object[] vStack; //Value stack
  private int vStackCount;
  private CallFrame[] fStack; //Frame stack
  private int fStackCount;
  private String initString;
  private Upvalue openUpvalues; //linked list

  //Cached properties
  private boolean debugMaster;
  private boolean debugPrintProgress;
  private boolean debugTraceExecution;

  //VM()
  public VM(Props properties, Debugger debugger) {
    this.properties = properties;
    this.debugger = debugger;

    properties.registerObserver(this);

    updateCachedProperties();

    if (debugPrintProgress)
      debugger.printProgress("Initializing VM....");

    compiler = new Compiler(properties, debugger);
    globals = new HashMap<>();
    vStack = new Object[properties.getInt("MAX_STACK")];
    fStack = new CallFrame[properties.getInt("MAX_FRAMES")];
    initString = "init";

    defineNativeFn("clock", new NativeClock());
    defineNativeFn("foo", new NativeFoo());
    defineNativeFn("print", new NativePrint());
    defineNativeFn("println", new NativePrintLn());

    reset();
  }

  //fStackTop()
  private int fStackTop() {
    return fStackCount - 1;
  }

  //getFrame(int)
  private CallFrame getFrame(int index) {
    return fStack[index];
  }

  //peekFrame()
  private CallFrame peekFrame() {
    return fStack[fStackCount - 1];
  }

  //popFrame()
  private CallFrame popFrame() {
    return fStack[(fStackCount--) - 1];
  }

  //pushFrame(CallFrame)
  private void pushFrame(CallFrame frame) {
    fStack[fStackCount++] = frame;
  }

  //vStackTop()
  private int vStackTop() {
    return vStackCount - 1;
  }

  //getValue(int)
  private Object getValue(int index) {
    return vStack[index];
  }

  //peekValue()
  private Object peekValue() {
    return vStack[vStackCount - 1];
  }

  //peekNValues(int)
  private Object peekNValues(int n) {
    return vStack[vStackCount - n];
  }

  //popValue()
  private Object popValue() {
    return vStack[(vStackCount--) - 1];
  }

  //pushValue(Object)
  private void pushValue(Object value) {
    vStack[vStackCount++] = value;
  }

  //setValue(int, Object)
  private void setValue(int index, Object value) {
    vStack[index] = value;
  }

  //reset()
  private void reset() {
    vStackCount = 0;
    fStackCount = 0;
    openUpvalues = null;
  }

  //runtimeError(String, String...)
  void runtimeError(String message, String... args) {
    System.err.println("Runtime Error: " + message);

    for (String s : args)
      System.err.println(s);

    for (int i = fStackTop(); i >= 0; i--) {
      CallFrame frame = getFrame(i);
      Function function = frame.closure().function();
      int[] lines = function.chunk().lines();
      int line = lines[frame.ip() - 1];

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
    if (!checkArity(closure.function(), argCount))
      return false;

    //CallFrame window on VM vStack begins at slot
    //occupied by function.
    int base = vStackTop() - argCount;
    int maxFrames = properties.getInt("MAX_FRAMES");

    if (fStackCount == maxFrames) {
      runtimeError("Stack overflow.");

      return false;
    }

    pushFrame(new CallFrame(closure, base));

    return true;
  }

  //callValue(Object, int)
  private boolean callValue(Object callee, int argCount) {
    //Bound Method
    if (callee instanceof BoundMethod) {
      BoundMethod bound = (BoundMethod)callee;

      setValue(vStackCount - argCount, bound.receiver());

      return call(bound.method(), argCount);
    //Class
    } else if (callee instanceof LoxClass) {
      LoxClass klass = (LoxClass)callee;
      LoxInstance instance = new LoxInstance(klass);

      setValue(vStackTop() - argCount, instance);

      Closure initializer = klass.methods().get(initString);

      if (initializer != null)
        return call(initializer, argCount);
      else if (argCount != 0) {
        runtimeError("Expected 0 arguments but got " + argCount + ".");

        return false;
      }

      return true;
    //Closure
    } else if (callee instanceof Closure)
      return call((Closure)callee, argCount);
    //Native Function
    else if (callee instanceof NativeFn) {
      NativeFn nativeFn = (NativeFn)callee;

      if (!checkArity(nativeFn, argCount))
        return false;

      Object[] args = new Object[argCount];

      for (int i = 0; i < argCount; i++)
        args[i] = peekNValues(argCount - i);

      Object result = nativeFn.execute(args);

      //pop args plus native function
      vStackCount = vStackTop() - argCount;

      pushValue(result);

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
    Object receiver = peekNValues(argCount + 1);

    if (!(receiver instanceof LoxInstance)) {
      runtimeError("Only instances have methods.");

      return false;
    }

    LoxInstance instance = (LoxInstance)receiver;

    if (instance.fields().containsKey(name)) {
      Object value = instance.fields().get(name);

      setValue(vStackTop() - argCount, value);

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
    BoundMethod bound = new BoundMethod(peekValue(), method);
    Object value = bound;

    popValue();

    pushValue(value);

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

      upvalue.setClosedValue(getValue(upvalue.location()));
      upvalue.setLocation(-1);

      //after upvalue is closed, reset head of linked list
      //to next upvalue
      openUpvalues = upvalue.next();
      upvalue.setNext(null);
    }
  }

  //defineMethod(String)
  void defineMethod(String name) {
    Closure method = (Closure)peekValue();
    LoxClass klass = (LoxClass)peekNValues(2);

    klass.methods().put(name, method);

    popValue();
  }

  //isFalsey(Object)
  boolean isFalsey(Object value) {
    //nil and false are falsey and every other value behaves like true.
    return value == null || (value instanceof Boolean && !(boolean)value);
  }

  //concatenate()
  private void concatenate() {
    String b = (String)popValue();
    String a = (String)popValue();

    pushValue(a + b);
  }

  //equate()
  private void equate() {
    Object b = popValue();
    Object a = popValue();

    if (a == null)
      pushValue(b == null);
    else
      pushValue(a.equals(b));
  }

  //interpret(String)
  public InterpretResult interpret(String source) {
    Function function = compiler.compile(source);

    if (debugPrintProgress)
      debugger.printProgress("Executing....");

    if (function == null)
      return InterpretResult.INTERPRET_COMPILE_ERROR;

    Closure closure = new Closure(function);
    Object value = closure;

    pushValue(value);

    call(closure, 0);

    return run();
  }

  //readByte(CallFrame)
  private byte readByte(CallFrame frame) {
    return (byte)frame.closure().function().chunk().codes()[frame.getAndIncrementIP()];
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

    return frame.closure().function().chunk().constants()[index];
  }

  //readString(CallFrame)
  private String readString(CallFrame frame) {
    return (String)readConstant(frame);
  }

  //run()
  private InterpretResult run() {
    CallFrame frame = peekFrame();

    //Bytecode dispatch loop.
    for (;;) {
      if (debugTraceExecution) {
        Object[] substack = Arrays.copyOfRange(vStack, 0, vStackCount);

        debugger.traceExecution(frame, globals, substack);
      }

      switch (readByte(frame)) {
        case OP_CONSTANT:
          pushValue(readConstant(frame));

          break;
        case OP_NIL:
          pushValue(null);

          break;
        case OP_TRUE:
          pushValue(true);

          break;
        case OP_FALSE:
          pushValue(false);

          break;
        case OP_POP:
          popValue();

          break;
        case OP_GET_LOCAL:
          short glSlot = readWord(frame);

          pushValue(getValue(frame.base() + glSlot));

          break;
        case OP_SET_LOCAL:
          short slSlot = readWord(frame);

          setValue(frame.base() + slSlot, peekValue());

          break;
        case OP_GET_GLOBAL:
          String ggKey = readString(frame);

          if (!globals.containsKey(ggKey))
            return error("Undefined variable '" + ggKey + "'.");

          Object global = globals.get(ggKey);

          pushValue(global);

          break;
        case OP_DEFINE_GLOBAL:
          String dgKey = readString(frame);

          globals.put(dgKey, peekValue());

          popValue();

          break;
        case OP_SET_GLOBAL:
          String sgKey = readString(frame);

          if (!globals.containsKey(sgKey))
            return error("Undefined variable '" + sgKey + "'.");

          //Peek here, not pop; assignment is an expression,
          //so we leave value vStacked in case the assignment
          //is nested inside a larger expression.
          globals.put(sgKey, peekValue());

          break;
        case OP_GET_UPVALUE:
          short guSlot = readWord(frame);
          Upvalue guUpvalue = frame.closure().upvalues()[guSlot];

          if (guUpvalue.location() != -1) //i.e., open
            pushValue(getValue(guUpvalue.location()));
          else //i.e., closed
            pushValue(guUpvalue.closedValue());

          break;
        case OP_SET_UPVALUE:
          short suSlot = readWord(frame);
          Upvalue suUpvalue = frame.closure().upvalues()[suSlot];

          if (suUpvalue.location() != -1) //i.e., open
            setValue(suUpvalue.location(), peekValue());
          else //i.e., closed
            suUpvalue.setClosedValue(peekValue());

          break;
        case OP_GET_PROPERTY:
          Object gpValue = peekValue();

          if (!(gpValue instanceof LoxInstance)) {
            runtimeError("Only instances have properties.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance gpInstance = (LoxInstance)gpValue;
          String name = readString(frame);

          if (gpInstance.fields().containsKey(name)) {
            popValue(); // Instance.

            pushValue(gpInstance.fields().get(name));

            break;
          }

          if (!bindMethod(gpInstance.klass(), name))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_SET_PROPERTY:
          Object spValue = peekNValues(2);

          if (!(spValue instanceof LoxInstance)) {
            runtimeError("Only instances have fields.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxInstance spInstance = (LoxInstance)spValue;

          spInstance.fields().put(readString(frame), peekValue());

          //pop value that was set plus instance object
          Object setObject = popValue();
          popValue();

          //push value back on vStack
          pushValue(setObject);

          break;
        case OP_GET_SUPER:
          String gsName = readString(frame);
          LoxClass gsSuperclass = (LoxClass)popValue();

          if (!bindMethod(gsSuperclass, gsName))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          break;
        case OP_EQUAL:
          equate();

          break;
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
          pushValue(isFalsey(popValue()));

          break;
        case OP_NEGATE:
          if (!oneNumericOperand())
            return errorOneNumber();

          pushValue(-(double)popValue());

          break;
        //case OP_PRINT:
        //  System.out.println(popValue());

        //  break;
        case OP_JUMP:
          short jumpOffset = readWord(frame);

          frame.setIP(frame.ip() + jumpOffset);

          break;
        case OP_JUMP_IF_FALSE:
          short jumpIfFalseOffset = readWord(frame);

          if (isFalsey(peekValue()))
            frame.setIP(frame.ip() + jumpIfFalseOffset);

          break;
        case OP_LOOP:
          short loopOffset = readWord(frame);

          frame.setIP(frame.ip() - loopOffset);

          break;
        case OP_CALL:
          int callArgCount = readByte(frame);
          Object callee = getValue(vStackTop() - callArgCount);

          if (!callValue(callee, callArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = peekFrame();

          break;
        case OP_INVOKE:
          String invMethod = readString(frame);
          int invArgCount = readByte(frame);

          if (!invoke(invMethod, invArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = peekFrame();

          break;
        case OP_SUPER_INVOKE:
          String siMethod = readString(frame);
          int siArgCount = readByte(frame);
          LoxClass siSuperclass = (LoxClass)popValue();

          if (!invokeFromClass(siSuperclass, siMethod, siArgCount))
            return InterpretResult.INTERPRET_RUNTIME_ERROR;

          frame = peekFrame();

          break;
        case OP_CLOSURE:
          Function function = (Function)readConstant(frame);
          Closure closure = new Closure(function);

          pushValue(closure);

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
          //close the upvalue at the top of the vStack
          closeUpvalues(vStackTop());

          popValue();

          break;
        case OP_RETURN:
          Object result = popValue();

          closeUpvalues(frame.base());

          popFrame();

          if (fStackCount == 0) {
            popValue();

            return InterpretResult.INTERPRET_OK;
          }

          vStackCount = frame.base();

          pushValue(result);

          frame = peekFrame();

          break;
        case OP_CLASS:
          LoxClass classLoxClass = new LoxClass(readString(frame));
          Object classVal = classLoxClass;

          pushValue(classVal);

          break;
        case OP_INHERIT:
          Object superclass = peekNValues(2);

          if (!(superclass instanceof LoxClass)) {
            runtimeError("Superclass must be a class.");

            return InterpretResult.INTERPRET_RUNTIME_ERROR;
          }

          LoxClass subclass = (LoxClass)peekValue();

          subclass.inheritMethods(((LoxClass)superclass).methods());

          popValue(); // Subclass.

          break;
        case OP_METHOD:
          defineMethod(readString(frame));

          break;
      } //switch
    } //for(;;)
  }

  //oneNumericOperand()
  private boolean oneNumericOperand() {
    return peekValue() instanceof Double;
  }

  //errorOneNumber()
  private InterpretResult errorOneNumber() {
    return error("Operand must be a number");
  }

  //twoNumericOperands()
  private boolean twoNumericOperands() {
    Object first = popValue(); //temporarily pop
    Object second = peekValue();

    pushValue(first);

    return first instanceof Double && second instanceof Double;
  }

  //errorTwoNumbers()
  private InterpretResult errorTwoNumbers() {
    return error("Operands must be two numbers.");
  }

  //twoStringOperands()
  private boolean twoStringOperands() {
    Object first = popValue();
    Object second = peekValue();

    pushValue(first); //push back again

    return first instanceof String && second instanceof String;
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
    double b = (double)popValue();
    double a = (double)popValue();

    switch (op) {
      case OPERATION_PLUS:
        pushValue(a + b);

        break;
      case OPERATION_SUBTRACT:
        pushValue(a - b);

        break;
      case OPERATION_MULT:
        pushValue(a * b);

        break;
      case OPERATION_DIVIDE:
        pushValue(a / b);

        break;
      case OPERATION_GT:
        pushValue(a > b);

        break;
      case OPERATION_LT:
        pushValue(a < b);

        break;
    } //switch
  }

  //checkArity(HasArity, int)
  private boolean checkArity(HasArity callee, int argCount) {
    int arity = callee.arity();

    if (arity < 0) {
      if (argCount > Math.abs(arity)) {
        runtimeError(
          "Expected up to " + Math.abs(arity) + " argument(s) but got " + argCount + "."
        );

        return false;
      }
    } else if (argCount != arity) {
      runtimeError(
        "Expected " + arity + " arguments but got " + argCount + "."
      );

      return false;
    }

    return true;
  }

  //notifyPropertiesChanged()
  public void notifyPropertiesChanged() {
    updateCachedProperties();
  }

  //updateCachedProperties()
  private void updateCachedProperties() {
    debugMaster = properties.getBool("DEBUG_MASTER");
    debugPrintProgress = debugMaster && properties.getBool("DEBUG_PRINT_PROGRESS");
    debugTraceExecution = debugMaster && properties.getBool("DEBUG_TRACE_EXECUTION");
  }
}
