package jblox.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import jblox.debug.Debugger;
import jblox.vm.VM;

public class JBLox {
  private Props properties;
  private Debugger debugger;
  private VM vm;
  private int exitCode;
  private String exitMessage;

  //JBLox()
  public JBLox() {
    properties = new Props("/home/ubuntu/jblox/main/props");
    debugger = new Debugger(properties);
    vm = new VM(properties, debugger);
  }

  //runFile(String)
  public void runFile(String path) {
    try {
      byte[] source = Files.readAllBytes(Paths.get(path));

      VM.InterpretResult result = vm.interpret(new String(source, Charset.defaultCharset()));

      if (result == VM.InterpretResult.INTERPRET_COMPILE_ERROR)
        shutdown(65, null);

      if (result == VM.InterpretResult.INTERPRET_RUNTIME_ERROR)
        shutdown(70, null);
    } catch (FileNotFoundException f) {
      shutdown(1, "File not found: " + path);
    } catch (IOException i) {
      shutdown(1, "IOException occurred.");
    }
  }

  //repl()
  public void repl() {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");

      try {
        String line = reader.readLine();

        if (line == null) {
          exitCode = 1;
          exitMessage = "Unknown error: null input line.";

          break;
        }

       //treat line prefixed with ':' as a REPL command
       if (line.length() > 0 && line.charAt(0) == ':') {
         if (handleREPLCommand(line.substring(1)))
           continue;
         else
           break;
       }

        //send line to VM for interpreting
        VM.InterpretResult result = vm.interpret(line);
      } catch (IOException e) {
        exitCode = 1;
        exitMessage = "IOException reading line.";

        break;
      }
    }

    try {
      input.close();
      reader.close();
    } catch (IOException e) {
      System.err.println(e);
    }

    shutdown(exitCode, exitMessage);
  }

  //handleREPLCommand(String)
  private boolean handleREPLCommand(String command) {
    boolean continueREPL = true; //continue by default

    if (command.equals("quit") || command.equals("q")) {
      exitCode = 0;
      exitMessage = "Goodbye.";

      continueREPL = false;
    } else if (command.equals("debug")) {
      System.out.println("Master: " + properties.getBool("DEBUG_MASTER"));
      System.out.println("Print Stack: " + properties.getBool("DEBUG_PRINT_STACK"));
      System.out.println("Trace Execution: " + properties.getBool("DEBUG_TRACE_EXECUTION"));
      System.out.println("Print Progress: " + properties.getBool("DEBUG_PRINT_PROGRESS"));
      System.out.println("Print Constants: " + properties.getBool("DEBUG_PRINT_CONSTANTS"));
      System.out.println("Print Globals: " + properties.getBool("DEBUG_PRINT_GLOBALS"));
      System.out.println("Print Locals: " + properties.getBool("DEBUG_PRINT_LOCALS"));
      System.out.println("Print Source: " + properties.getBool("DEBUG_PRINT_SOURCE"));
      System.out.println("Print OpCode: " + properties.getBool("DEBUG_PRINT_OPCODE"));
      System.out.println("Print Code: " + properties.getBool("DEBUG_PRINT_CODE"));
      System.out.println("Print Codes: " + properties.getBool("DEBUG_PRINT_CODES"));
    } else if (command.equals("master"))
      System.out.println(
        "Master " +
        (properties.toggleBool("DEBUG_MASTER") ? "ON" : "OFF")
      );
    else if (command.equals("printstack"))
      System.out.println(
        "Print Stack " +
        (properties.toggleBool("DEBUG_PRINT_STACK") ? "ON" : "OFF")
      );
    else if (command.equals("traceexecution"))
      System.out.println(
        "Trace Execution " +
        (properties.toggleBool("DEBUG_TRACE_EXECUTION") ? "ON" : "OFF")
      );
    else if (command.equals("printprogress"))
      System.out.println(
        "Print Progress " +
        (properties.toggleBool("DEBUG_PRINT_PROGRESS") ? "ON" : "OFF")
      );
    else if (command.equals("printconstants"))
      System.out.println(
        "Print Constants " +
        (properties.toggleBool("DEBUG_PRINT_CONSTANTS") ? "ON" : "OFF")
      );
    else if (command.equals("printglobals"))
      System.out.println(
        "Print Globals " +
        (properties.toggleBool("DEBUG_PRINT_GLOBALS") ? "ON" : "OFF")
      );
    else if (command.equals("printlocals"))
      System.out.println(
        "Print Locals " +
        (properties.toggleBool("DEBUG_PRINT_LOCALS") ? "ON" : "OFF")
      );
    else if (command.equals("printsource"))
      System.out.println(
        "Print Source " +
        (properties.toggleBool("DEBUG_PRINT_SOURCE") ? "ON" : "OFF")
      );
    else if (command.equals("printopcode"))
      System.out.println(
        "Print OpCode " +
        (properties.toggleBool("DEBUG_PRINT_OPCODE") ? "ON" : "OFF")
      );
    else if (command.equals("printcode"))
      System.out.println(
        "Print Code " +
        (properties.toggleBool("DEBUG_PRINT_CODE") ? "ON" : "OFF")
      );
    else if (command.equals("printcodes"))
      System.out.println(
        "Print Codes " +
        (properties.toggleBool("DEBUG_PRINT_CODES") ? "ON" : "OFF")
      );
    else
      System.out.println("Unknown REPL command: '" + command + "'");

    return continueREPL;
  }

  //shutdown(int, String)
  private void shutdown(int code, String message) {
    properties.close();

    if (code == 0) {
      if (message != null)
        System.out.println(message);

        System.exit(code);
    } else {
      if (message != null)
        System.err.println(message);

      System.exit(code);
    }
  }

  //main(String[])
  public static void main(String[] args) throws IOException {
    JBLox jblox = new JBLox();

    if (args.length > 1)
      jblox.shutdown(64, "Usage: jblox [script]");
    else if (args.length == 1)
      jblox.runFile(args[0]);
    else
      jblox.repl();
  }
}
