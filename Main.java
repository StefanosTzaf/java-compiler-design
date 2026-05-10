import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;
import java.util.*;

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
                // iterating over the fieldOffsets Map and NOT the fields Map because
                // the overriden methods have not been added in the methodOffsets Map(we don't want to print them)
                for (Map.Entry<String, Integer> entry : c.fieldOffsets.entrySet()) {
                    System.out.println(name + "." + entry.getKey() + " : " + entry.getValue());
                }
                for (Map.Entry<String, Integer> entry : c.methodOffsets.entrySet()) {
                    System.out.println(name + "." + entry.getKey() + " : " + entry.getValue());
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