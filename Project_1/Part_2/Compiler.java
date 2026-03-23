import java_cup.runtime.*;
import java.io.*;

// it was not named Main.java because the Main.java file will be produced by our transpiler
public class Compiler {
    public static void main(String[] args) {
        try {
            
            Reader inputReader;
            // if an argument is provided, read from the file, otherwise read from standard input
            if (args.length > 0) {
                inputReader = new FileReader(args[0]);
            } else {
                inputReader = new InputStreamReader(System.in);
            }

            Scanner scanner = new Scanner(inputReader);
            Parser parser = new Parser(scanner);
            parser.parse();

        } 
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}