package jblox;

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
  private Defaults defaults;
  private Debugger debugger;
  private VM vm;

  //JBLox()
  public JBLox() {
    defaults = new Defaults("/home/ubuntu/jblox/props");
    debugger = new Debugger(defaults);
    vm = new VM(defaults, debugger);
  }

  //runFile(String)
  public void runFile(String path) {
    try {
      byte[] source = Files.readAllBytes(Paths.get(path));

      VM.InterpretResult result = vm.interpret(new String(source, Charset.defaultCharset()));

      if (result == VM.InterpretResult.INTERPRET_COMPILE_ERROR) System.exit(65);
      if (result == VM.InterpretResult.INTERPRET_RUNTIME_ERROR) System.exit(70);
    } catch (FileNotFoundException f) {
      System.err.println("File not found:  " + path);

      System.exit(1);
    } catch (IOException i) {
      System.err.println("IOException occurred.");

      System.exit(1);
    }
  }

  //repl()
  public void repl() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");

      String line = reader.readLine();

      if (line == null || line.equals("quit") || line.equals("q")) break;

      VM.InterpretResult result = vm.interpret(line);
    }
  }

  //main(String[])
  public static void main(String[] args) throws IOException {
    JBLox jblox = new JBLox();

    if (args.length > 1) {
      System.out.println("Usage: jblox [script]");

      System.exit(64);
    } else if (args.length == 1)
      jblox.runFile(args[0]);
    else
      jblox.repl();
  }
}
