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

    // we save the offsets of the next fields and methods. Needed when updateing the
    // offsets of the child classes, so as not to traverse the parent class every time we
    // need the last offset
    int nextFieldOffset = 0; 
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
    public Map<String, ClassSymbolTable> classes = new LinkedHashMap<>();
    
    public ClassSymbolTable currentClass = null;
    public MethodSymbolTable currentMethod = null;

    // so as to count the offsets of the fields and methods
    private int fieldCounter = 0;
    private int methodCounter = 0;
    
    // interface of the symbol table as reffered in the slides
    public void enterClass(String className, String parentName) {
        currentClass = new ClassSymbolTable(className, parentName);
        classes.put(className, currentClass);
        methodCounter = 0;
        if(parentName == null){
            // for every new class we reset the field and method counters
            fieldCounter = 0;
        }
        else{
            // if there is a parent class, we need to start counting the offsets from the end of the parent class
            ClassSymbolTable parent = classes.get(parentName);
            this.fieldCounter = parent.nextFieldOffset;
        }
    }

    public void enterMethod(String returnType, String methodName) {
        currentMethod = new MethodSymbolTable(returnType, methodName);
        currentClass.methods.put(methodName, currentMethod);

        // 8 bytes for the methods (pointer)
        currentClass.methodOffsets.put(methodName, methodCounter);
        methodCounter += 8;
    }

    public void insert(String name, String type) {
        // if we are now inside a method, then this is a local variable
        if (currentMethod != null) {
            // in local variables we don't calculate offsets
            currentMethod.localVariables.put(name, type);
        }
        // if we are not inside a method but we are inside a class, then this is a field
        else if (currentClass != null) {
            currentClass.fields.put(name, type);
            currentClass.fieldOffsets.put(name, fieldCounter);

            if (type.equals("int")) {
                fieldCounter += 4;
            }
            else if (type.equals("boolean")) {
                fieldCounter += 1;
            }
            else {
                // array or any other class name
                fieldCounter += 8;
            }
            // update the next field offset of the current class
            currentClass.nextFieldOffset = fieldCounter;
        }
    }

    public void insertParameter(String name, String type) {
        if (currentMethod != null) {
            currentMethod.parameters.put(name, type);
        }
    }
    
    public void exitMethod() {
        currentMethod = null;
    }

    public void exitClass() {
        if (currentClass != null) {
            currentClass.nextFieldOffset = this.fieldCounter;
        }
        currentClass = null;
    }
}
