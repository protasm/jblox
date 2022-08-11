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

  //JBLox()
  public JBLox() {
    properties = new Props("/home/ubuntu/jblox/props");
    debugger = new Debugger(properties);
    vm = new VM(properties, debugger);
  }

  //runFile(String)
  public void runFile(String path) {
    try {
      byte[] source = Files.readAllBytes(Paths.get(path));

      VM.InterpretResult result = vm.interpret(new String(source, Charset.defaultCharset()));

      if (result == VM.InterpretResult.INTERPRET_COMPILE_ERROR) shutdown(65, null);
      if (result == VM.InterpretResult.INTERPRET_RUNTIME_ERROR) shutdown(70, null);
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

    int exitCode = 1;
    String exitMessage = "Unknown error";

    for (;;) {
      System.out.print("> ");

      try {
        String line = reader.readLine();

        if (line == null || line.equals("quit") || line.equals("q")) {
          exitCode = 0;
          exitMessage = null;

          break;
        }

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

  //shutdown(int, String)
  private void shutdown(int code, String message) {
    properties.close();

    if (code == 0)
      System.exit(0);
    else {
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
