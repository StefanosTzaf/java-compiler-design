import java_cup.runtime.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {

        Scanner lexer = new Scanner(new InputStreamReader(System.in));
        Symbol token;

        // just checking that scanner works
        while ((token = lexer.next_token()).sym != sym.EOF) {
            System.out.print("Token with ID: " + token.sym);
            
            if (token.value != null) {
                System.out.print("  ---> Value: " + token.value);
            }
            System.out.println();
        }
    }
}