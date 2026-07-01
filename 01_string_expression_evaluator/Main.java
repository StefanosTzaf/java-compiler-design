import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        // so as to read many lines until EOF, using BufferedReader to read line by line
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        
        try {
            // reads line by line until EOF
            while ((line = reader.readLine()) != null) {
                try {
                    InputStream in = new ByteArrayInputStream(line.getBytes());
                    StringExpEvaluator evaluator = new StringExpEvaluator(in);
                    System.out.println(evaluator.eval());
                } 
                catch (ParseError e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }
}