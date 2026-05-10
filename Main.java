import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("Program parsed successfully.");

            SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
            root.accept(symbolTableVisitor, null);

            // printing the symbol table for debugging
            for (ClassSymbolTable c : symbolTableVisitor.symbolTable.classes.values()) {
                String name = c.name;
                if (name.equals(symbolTableVisitor.mainClassName)) {
                    continue;
                }
                for (String fieldName : c.fields.keySet()) {
                    int offset = c.fieldOffsets.get(fieldName);
                    System.out.println(name + "." + fieldName + " : " + offset);
                }
                
                for (String methodName : c.methods.keySet()) {
                    int offset = c.methodOffsets.get(methodName);
                    System.out.println(name + "." + methodName + " : " + offset);
                }
            }
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}