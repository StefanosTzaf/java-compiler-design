import java_cup.runtime.*;
import java.io.*;

public class Compiler {
    public static void main(String[] args) {
        try {
            Reader inputReader;
            if (args.length > 0) {
                inputReader = new FileReader(args[0]);
            } else {
                inputReader = new InputStreamReader(System.in);
            }

            Scanner scanner = new Scanner(inputReader);
            Parser parser = new Parser(scanner) {
                
                @Override
                public void syntax_error(Symbol cur_token) {
                    int line = cur_token.left + 1;
                    System.err.println("Syntax error at line " + line +  ": unexpected token '");
                }
            };

            parser.parse();

        } 
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}