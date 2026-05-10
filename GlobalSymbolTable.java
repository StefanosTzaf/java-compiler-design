import java.util.*;

// symbol table for the classes
class ClassSymbolTable {
    String name;
    String extendsFrom;
    Map<String, String> fields = new LinkedHashMap<>();
    Map<String, MethodSymbolTable> methods = new LinkedHashMap<>();
    
    // also keep the offsets of the fields and methods
    Map<String, Integer> fieldOffsets = new LinkedHashMap<>();
    Map<String, Integer> methodOffsets = new LinkedHashMap<>();
    public ClassSymbolTable(String name, String parentName) {
        this.name = name;
        this.extendsFrom = parentName;
    }
}

// a more specific symbol table for the methods
class MethodSymbolTable {
    String name;
    String returnType;
    Map<String, String> parameters = new LinkedHashMap<>(); 
    Map<String, String> localVariables = new LinkedHashMap<>();
    
    public MethodSymbolTable(String returnType, String name) {
        this.returnType = returnType;
        this.name = name;
    }
}

// this is the main class for the symbol table, it contains a map of class names to their ClassSymbolTable
public class GlobalSymbolTable {

    Map<String, ClassSymbolTable> classes = new LinkedHashMap<>();
    
    ClassSymbolTable currentClass = null;
    MethodSymbolTable currentMethod = null;

    // interface of the symbol table as reffered in the slides
    public void enterClass(String className, String parentName) {
        currentClass = new ClassSymbolTable(className, parentName);
        classes.put(className, currentClass);
    }

    public void enterMethod(String returnType, String methodName) {
        currentMethod = new MethodSymbolTable(returnType, methodName);
        currentClass.methods.put(methodName, currentMethod);
    }

    public void insert(String name, String type) {
        // if we are now inside a method, then this is a local variable
        if (currentMethod != null) {
            currentMethod.localVariables.put(name, type);
        }
        // if we are not inside a method but we are inside a class, then this is a field
        else if (currentClass != null) {
            currentClass.fields.put(name, type);
        }
    }

    public void exitMethod() {
        currentMethod = null;
    }

    public void exitClass() {
        currentClass = null;
    }
}
