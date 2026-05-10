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
    int nextMethodOffset = 0;
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
        
        if(parentName == null){
            // for every new class we reset the field and method counters
            fieldCounter = 0;
            methodCounter = 0;
        }
        else{
            // if there is a parent class, we need to start counting the offsets from the end of the parent class
            ClassSymbolTable parent = classes.get(parentName);
            this.fieldCounter = parent.nextFieldOffset;
            this.methodCounter = parent.nextMethodOffset;
        }
    }

    public void enterMethod(String returnType, String methodName) {
        // the real insertion of the method in the symbol table will be done
        // in the exit function because we need to know the parametres for overloading
        currentMethod = new MethodSymbolTable(returnType, methodName);
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
        // we make a name this way so as to be distinguishable from other methods with the same name 
        // but different parameters (overloading)
        String nameWithParameters = currentMethod.name;
        for (String paramType : currentMethod.parameters.values()) {
            // TODO: can we print the real name? is this a problem?
            nameWithParameters += "_" + paramType;
        }

        Integer inheritedOffset = findMethodOffsetInHierarchy(currentClass.extendsFrom, nameWithParameters);
        if (inheritedOffset != null) {
            // in case of overriding do not add it in the offsets of the current class
            // TODO: not sure about this if
        }
        else{
            currentClass.methodOffsets.put(nameWithParameters, methodCounter);
            methodCounter += 8;
            currentClass.nextMethodOffset = methodCounter;
        }

        // save this method in the symbol table of the current class
        currentClass.methods.put(nameWithParameters, currentMethod);
        currentMethod = null;
    }

    public void exitClass() {
        if (currentClass != null) {
            currentClass.nextFieldOffset = this.fieldCounter;
            currentClass.nextMethodOffset = this.methodCounter;
        }
        currentClass = null;
    }

    private Integer findMethodOffsetInHierarchy(String parentName, String functionName) {
        if (parentName == null) {
            return null;
        }

        ClassSymbolTable parent = classes.get(parentName);
        if (parent == null) {
            return null;
        }

        // if parent contains the method, return its offset
        if (parent.methodOffsets.containsKey(functionName)) {
            return parent.methodOffsets.get(functionName);
        }

        // if parent does not contain the method, continue searching in the hierarchy
        return findMethodOffsetInHierarchy(parent.extendsFrom, functionName);
    }
}
