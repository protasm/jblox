package jblox.debug;

import java.util.Map;

import jblox.compiler.Chunk;
import jblox.compiler.Function;
import jblox.compiler.CompilerLocals;
import jblox.main.Props;
import jblox.main.PropsObserver;
import jblox.util.LoxValueMap;
import jblox.util.LoxStack;
import jblox.vm.CallFrame;

import static jblox.compiler.OpCode.*;

public class Debugger implements PropsObserver {
  private Props properties;

  //Cached properties
  private boolean printCodes;
  private boolean printConstants;
  private boolean printGlobals;
  private boolean printLines;
  private boolean printLocals;
  private boolean printOpCode;
  private boolean printStack;

  //Debugger(Props)
  public Debugger(Props properties) {
    this.properties = properties;

    properties.registerObserver(this);

    updateCachedProperties();
  }

  //printProgress(String)
  public void printProgress(String message) {
    System.out.println(message);
  }

  //printSource(String)
  public void printSource(String source) {
    printBanner("source");

    System.out.println((source.length() == 0) ? "[ no source ]" : source);
  }

  //traceExecution(CallFrame, LoxMap, LoxStack)
  public void traceExecution(CallFrame frame, LoxValueMap globals, LoxStack stack) {
    if (printGlobals)
      System.out.println("Globals: " + globals);

    if (printStack)
      System.out.println("          " + stack);

    disassembleInstruction(frame.closure().function().chunk(), frame.ip());
  }

  //disassembleChunk(Chunk, CompilerLocals, String)
  public void disassembleChunk(Chunk chunk, CompilerLocals locals, String name) {
    if (printCodes) {
      System.out.println("Codes: " + chunk.codes());

      if (printLines)
        System.out.println("Lines: " + chunk.lines());
    }

    if (printConstants)
      System.out.println("Constants: " + chunk.constants());

    if (printLocals)
      System.out.println("Locals: " + locals);

    printBanner(name);

    for (int offset = 0; offset < chunk.codes().count();)
      offset = disassembleInstruction(chunk, offset);
  }

  public void printBanner(String text) {
    System.out.println("== " + text + " ==");
  }

  //disassembleInstruction(Chunk, int)
  public int disassembleInstruction(Chunk chunk, int offset) {
    byte instruction = getCode(chunk, offset);

    System.out.print(String.format("%04d", offset));

    if (
      (offset > 0) &&
      (chunk.lines().get(offset) == chunk.lines().get(offset - 1))
    )
      System.out.print("   | ");
    else
      System.out.print(String.format("%4d ", chunk.lines().get(offset)));

    if (printOpCode)
      System.out.print("(" + String.format("0x%02X", instruction) + ") ");

    switch (instruction) {
      case OP_CONSTANT:
        return constantInstruction("OP_CONSTANT", chunk, offset);
      case OP_NIL:
        return simpleInstruction("OP_NIL", offset);
      case OP_TRUE:
        return simpleInstruction("OP_TRUE", offset);
      case OP_FALSE:
        return simpleInstruction("OP_FALSE", offset);
      case OP_POP:
        return simpleInstruction("OP_POP", offset);
      case OP_GET_LOCAL:
        return wordOperandInstruction("OP_GET_LOCAL", chunk, offset);
      case OP_SET_LOCAL:
        return wordOperandInstruction("OP_SET_LOCAL", chunk, offset);
      case OP_GET_GLOBAL:
        return constantInstruction("OP_GET_GLOBAL", chunk, offset);
      case OP_DEFINE_GLOBAL:
        return constantInstruction("OP_DEFINE_GLOBAL", chunk, offset);
      case OP_SET_GLOBAL:
        return constantInstruction("OP_SET_GLOBAL", chunk, offset);
      case OP_GET_UPVALUE:
        return wordOperandInstruction("OP_GET_UPVALUE", chunk, offset);
      case OP_SET_UPVALUE:
        return wordOperandInstruction("OP_SET_UPVALUE", chunk, offset);
      case OP_GET_PROPERTY:
        return constantInstruction("OP_GET_PROPERTY", chunk, offset);
      case OP_SET_PROPERTY:
        return constantInstruction("OP_SET_PROPERTY", chunk, offset);
      case OP_GET_SUPER:
        return constantInstruction("OP_GET_SUPER", chunk, offset);
      case OP_EQUAL:
        return simpleInstruction("OP_EQUAL", offset);
      case OP_GREATER:
        return simpleInstruction("OP_GREATER", offset);
      case OP_LESS:
        return simpleInstruction("OP_LESS", offset);
      case OP_ADD:
        return simpleInstruction("OP_ADD", offset);
      case OP_SUBTRACT:
        return simpleInstruction("OP_SUBTRACT", offset);
      case OP_MULTIPLY:
        return simpleInstruction("OP_MULTIPLY", offset);
      case OP_DIVIDE:
        return simpleInstruction("OP_DIVIDE", offset);
      case OP_NOT:
        return simpleInstruction("OP_NOT", offset);
      case OP_NEGATE:
        return simpleInstruction("OP_NEGATE", offset);
      case OP_PRINT:
        return simpleInstruction("OP_PRINT", offset);
      case OP_JUMP:
        return jumpInstruction("OP_JUMP", 1, chunk, offset);
      case OP_JUMP_IF_FALSE:
        return jumpInstruction("OP_JUMP_IF_FALSE", 1, chunk, offset);
      case OP_LOOP:
        return jumpInstruction("OP_LOOP", -1, chunk, offset);
      case OP_CALL:
        return byteOperandInstruction("OP_CALL", chunk, offset);
      case OP_INVOKE:
        return invokeInstruction("OP_INVOKE", chunk, offset);
      case OP_SUPER_INVOKE:
        return invokeInstruction("OP_SUPER_INVOKE", chunk, offset);
      case OP_CLOSURE:
        return closureInstruction("OP_CLOSURE", chunk, offset);
      case OP_CLOSE_UPVALUE:
        return simpleInstruction("OP_CLOSE_UPVALUE", offset);
      case OP_RETURN:
        return simpleInstruction("OP_RETURN", offset);
      case OP_CLASS:
        return constantInstruction("OP_CLASS", chunk, offset);
      case OP_INHERIT:
        return simpleInstruction("OP_INHERIT", offset);
      case OP_METHOD:
        return constantInstruction("OP_METHOD", chunk, offset);
      default:
        System.out.println("Unknown opcode: " + instruction);

        return offset + 1;
    }
  }

  //closureInstruction(String, Chunk, int)
  private int closureInstruction(String name, Chunk chunk, int offset) {
    short operand = getWordOperand(chunk, offset);

    System.out.print(String.format("%-16s %4d ", name, operand));

    Function function = chunk.constants().get(operand).asFunction();

    System.out.println(function);

    offset += 3;

    for (int j = 0; j < function.upvalueCount(); j++) {
      boolean isLocal = (getCode(chunk, offset++) != 0);
      byte index = getCode(chunk, offset++);

      System.out.print(String.format(
        "%04d      |                     %s %d\n",
        offset - 2, isLocal ? "local" : "upvalue", index
      ));
    }

    return offset;
  }

  //constantInstruction(String, Chunk, int)
  private int constantInstruction(String name, Chunk chunk, int offset) {
    short operand = getWordOperand(chunk, offset);

    System.out.print(String.format("%-16s %4d ", name, operand));

    Object constant = chunk.constants().get(operand);

    if (constant instanceof String)
      System.out.print("'" + constant + "'\n");
    else
      System.out.print(constant + "\n");

    return offset + 3;
  }

  //invokeInstruction(String, Chunk, int)
  private int invokeInstruction(String name, Chunk chunk, int offset) {
    short operand = getWordOperand(chunk, offset);
    Object constant = chunk.constants().get(operand);
    byte argCount = getCode(chunk, offset + 3);

    System.out.print(String.format("%-16s (%d args) %4d ", name, argCount, operand));
    System.out.print("'" + constant + "'\n");

    return offset + 4;
  }

  //simpleInstruction(String, int)
  private int simpleInstruction(String name, int offset) {
    System.out.println(String.format("%-16s", name));

    return offset + 1;
  }

  //byteOperandInstruction(String, Chunk, int)
  private int byteOperandInstruction(String name, Chunk chunk, int offset) {
    byte operand = getByteOperand(chunk, offset);

    System.out.print(String.format("%-16s %4d\n", name, operand));

    return offset + 2;
  }

  //wordOperandInstruction(String, Chunk, int)
  private int wordOperandInstruction(String name, Chunk chunk, int offset) {
    short operand = getWordOperand(chunk, offset);

    System.out.print(String.format("%-16s %4d\n", name, operand));

    return offset + 3;
  }

  //jumpInstruction(String, int, Chunk, int)
  private int jumpInstruction(String name, int sign, Chunk chunk, int offset) {
    short operand = getWordOperand(chunk, offset);

    System.out.print(String.format("%-16s %4d -> %d\n",
      name, offset, offset + 3 + (sign * operand)));

    return offset + 3;
  }

  //getCode(Chunk, int)
  private byte getCode(Chunk chunk, int offset) {
    return (byte)chunk.codes().get(offset);
  }

  //getByteOperand(Chunk, int)
  private byte getByteOperand(Chunk chunk, int offset) {
   return (byte)chunk.codes().get(offset + 1);
  }

  //getWordOperand(Chunk, int)
  private short getWordOperand(Chunk chunk, int offset) {
    byte hi = (byte)chunk.codes().get(offset + 1);
    byte lo = (byte)chunk.codes().get(offset + 2);

    return (short)(((hi & 0xFF) << 8) | (lo & 0xFF));
  }

  //notifyPropertiesChanged()
  public void notifyPropertiesChanged() {
    updateCachedProperties();
  }

  //updateCachedProperties()
  private void updateCachedProperties() {
    printCodes = properties.getBool("DEBUG_PRINT_CODES");
    printConstants = properties.getBool("DEBUG_PRINT_CONSTANTS");
    printGlobals = properties.getBool("DEBUG_PRINT_GLOBALS");
    printLines = properties.getBool("DEBUG_PRINT_LINES");
    printLocals = properties.getBool("DEBUG_PRINT_LOCALS");
    printOpCode = properties.getBool("DEBUG_PRINT_OPCODE");
    printStack = properties.getBool("DEBUG_PRINT_STACK");
  }
}
