import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import syntaxtree.*;
import java.util.*;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ...");
            System.exit(1);
        }

        for (String filename : args) {
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(filename);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();

                SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
                root.accept(symbolTableVisitor, null);

                TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(symbolTableVisitor.symbolTable);
                root.accept(typeCheckVisitor, null);

                // do it this way to avoid printing the file path, just the file name
                String nameFile = new File(filename).getName();
                System.out.println("-----File : " + nameFile + " ------");
                // printing the offsets
                for (ClassSymbolTable c : symbolTableVisitor.symbolTable.classes.values()) {
                    String name = c.name;
                    if (name.equals(symbolTableVisitor.mainClassName)) {
                        continue;
                    }  
                    for (Map.Entry<String, Integer> entry : c.fieldOffsets.entrySet()) {
                        System.out.println(name + "." + entry.getKey() + " : " + entry.getValue());
                    }
                    
                    for (Map.Entry<String, Integer> entry : c.methodOffsets.entrySet()) {
                        // real name?
                        String mangledName = entry.getKey();
                        String realName = c.methods.get(mangledName).name; 
                        
                        System.out.println(name + "." + realName + " : " + entry.getValue());
                    }
                }
                System.out.println();
            }
            catch(ParseException ex){
                System.out.println("PARSE ERROR: " + ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println("File not found: " + ex.getMessage());
            }
            catch(RuntimeException ex){
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
}