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
    public void enterClass(String className, String parentName, int line) {
        if (classes.containsKey(className)) {
            throw new RuntimeException("TYPE ERROR: Class " + className + " is already defined (line " + line + ")");
        }

        currentClass = new ClassSymbolTable(className, parentName);
        // if this call did not come from an extends
        if(parentName == null){
            fieldCounter = 0;
            methodCounter = 0;
        }
        else{
            // if there is a parent class, we need to start counting the offsets from the end of the parent class
            ClassSymbolTable parent = classes.get(parentName);

            // if the parent class is not defined(even if it is defined later in the file)
            if(parent == null){
                throw new RuntimeException("TYPE ERROR: Parent class " + parentName + " is not defined (line " + line + ")");
            }

            this.fieldCounter = parent.nextFieldOffset;
            this.methodCounter = parent.nextMethodOffset;
        }

        classes.put(className, currentClass);
    }

    public void enterMethod(String returnType, String methodName) {
        // the real insertion of the method in the symbol table will be done
        // in the exit function because we need to know the parametres for overloading
        currentMethod = new MethodSymbolTable(returnType, methodName);
    }

    public void insert(String name, String type, int line) {
        // if we are now inside a method, then this is a local variable
        if (currentMethod != null) {
            // in local variables we don't calculate offsets
            if (currentMethod.localVariables.containsKey(name) || currentMethod.parameters.containsKey(name)) {
                throw new RuntimeException("TYPE ERROR: Variable " + name + " is already defined in method " + currentMethod.name + " (line " + line + ")");
            }
            currentMethod.localVariables.put(name, type);
        }
        // if we are not inside a method but we are inside a class, then this is a field
        else if (currentClass != null) {
            if (currentClass.fields.containsKey(name)) {
                throw new RuntimeException("TYPE ERROR: Field " + name + " is already defined in class " + currentClass.name + " (line " + line + ")");
            }
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

    public void insertParameter(String name, String type, int line) {
        if (currentMethod != null) {
            if (currentMethod.parameters.containsKey(name) || currentMethod.localVariables.containsKey(name)) {
                throw new RuntimeException("TYPE ERROR: Variable " + name + " is already defined in method " + currentMethod.name + " (line " + line + ")");
            }
            currentMethod.parameters.put(name, type);
        }
    }
    
    public void exitMethod(int line) {
        // we make a name this way so as to be distinguishable from other methods with the same name 
        // but different parameters (overloading)
        String nameWithParameters = currentMethod.name;
        for (String paramType : currentMethod.parameters.values()) {
            // TODO: can we print the real name? is this a problem?
            nameWithParameters += "_" + paramType;
        }

        // check this before adding the method to the symbol table because we have a map and it WON'T keep the same key two times 
        if (currentClass.methods.containsKey(nameWithParameters)) {
            throw new RuntimeException("TYPE ERROR: Method " + currentMethod.name + " is already defined with identical parameters in class " + currentClass.name + " (line " + line + ")");
        }
        
        currentClass.methods.put(nameWithParameters, currentMethod);
        // main method is static do not add it in the offsets, do not check for override and overload and do not increase the method counter
        if (currentMethod.name.equals("main")) {
            currentMethod = null;
            return;
        }
        checkOverrideAndOverload(currentClass.name, nameWithParameters, line);

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

    // ----------- Helper functions -------------

    public boolean isSubtype(String child, String parent) {
        if (child.equals(parent)) {
            return true;
        }
        
        // primitive types cannot be subtypes of any other type (if they were the same type, the above if would have found it)
        if (child.equals("int") || child.equals("boolean") || child.equals("int[]")) {
            return false;
        }

        ClassSymbolTable currentClass = classes.get(child);
        // check the hierarchy of the child class, if there is no extendsFrom break
        while (currentClass != null && currentClass.extendsFrom != null) {
            if (currentClass.extendsFrom.equals(parent)) {
                return true;
            }
            currentClass = classes.get(currentClass.extendsFrom);
        }
        // if it was a sub type or the same type, we would have found it till here
        return false;
    }

    public boolean containClass(String className) {
        return classes.containsKey(className);
    }

    // this function is used to get the type of a variable and null if it does not exist in the symbol table
    public String getVarType(String className, String methodName, String varName) {
        ClassSymbolTable c = classes.get(className);
        if (c == null){
            return null;
        }

        // search locally in the method
        if (methodName != null) {
            MethodSymbolTable currentMethod = null;
            // search for the method in the current class
            for (MethodSymbolTable m : c.methods.values()) {
                if (m.name.equals(methodName)) {
                    currentMethod = m;
                    break;
                }
            }
            // if there is a method with the given name
            if (currentMethod != null) {
                // first search in the local variables 
                if (currentMethod.localVariables.containsKey(varName)){
                    return currentMethod.localVariables.get(varName);
                }
                // then search in the parameters
                if (currentMethod.parameters.containsKey(varName)){
                    return currentMethod.parameters.get(varName);
                }
            }
        }

        // if was not found in the method, search in the fields of the class and its parents
        ClassSymbolTable currentClass = c;
        while (currentClass != null) {
            if (currentClass.fields.containsKey(varName)){
                return currentClass.fields.get(varName);
            }
        
            if (currentClass.extendsFrom != null){
                currentClass = classes.get(currentClass.extendsFrom);
            }
            else{
                currentClass = null;
            }
        }

        return null;
    }

    // Returns the return-type of the method or null if it does not exist in the symbol table
    public String getMethodReturnType(String className, String methodName, Vector<String> argTypes) {
        ClassSymbolTable c = classes.get(className);
        while (c != null) {
            // we have to search manually every method because in the map there are overloaded classes (foo_int, foo_int_int...)
            for (MethodSymbolTable m : c.methods.values()) {
                if (m.name.equals(methodName)) {
                    if (m.parameters.size() == argTypes.size()) {
                        boolean areTheSame = true;
                        int i = 0;
                        // check each parameter if it is the same or a supertype
                        for (String paramType : m.parameters.values()) {
                            if (!isSubtype(argTypes.get(i), paramType)) {
                                areTheSame = false;
                                break;
                            }
                            i++;
                        }
                        if (areTheSame){
                            return m.returnType;
                        }
                    }
                }
            }

            // if it was not found AND there is a parent class, continue searching there
            if (c.extendsFrom != null){
               c = classes.get(c.extendsFrom);  
            } 
            else {
                c = null;
            }
        
        }
        return null;
    }


    public void checkOverrideAndOverload(String className, String fullMethodName, int line) {
        ClassSymbolTable classToCheck = classes.get(className);
        MethodSymbolTable methodToCheck = classToCheck.methods.get(fullMethodName);
        String nameWithoutParameters = methodToCheck.name;

        // starting from this class and going up the hierarchy
        String currentClassName = className;
        
        while (currentClassName != null) {
            ClassSymbolTable currentClass = classes.get(currentClassName);

            for (MethodSymbolTable currentMethod : currentClass.methods.values()) {
                
                // if the name without the parameters(for example foo, not the foo_int_int) is the same
                if (currentMethod.name.equals(nameWithoutParameters)) {
                    // if it exactly the same method, skip
                    if (currentMethod == methodToCheck){
                        continue;
                    } 

                    // different number of parameters -> overloading
                    if (methodToCheck.parameters.size() != currentMethod.parameters.size()) {
                        continue;
                    }

                    // same number of parameters, we have to check
                    boolean isExactMatch = true;
                    boolean allHaveHierarchyRelationship = true;

                    // iterators to the arguments of the method we want to check and the current method in the hierarchy
                    Iterator<String> argsOfMethodToCheck = methodToCheck.parameters.values().iterator();
                    Iterator<String> currentArgs = currentMethod.parameters.values().iterator();

                    // we have a linked hash so next will give us the arguments in the order they were inserted
                    while (argsOfMethodToCheck.hasNext() && currentArgs.hasNext()) {
                        String type1 = argsOfMethodToCheck.next();
                        String type2 = currentArgs.next();

                        if (!type1.equals(type2)) {
                            isExactMatch = false;
                        }

                        // if non of them is a subtype of the other, then they do not have a hierarchy relationship
                        if (!isSubtype(type1, type2) && !isSubtype(type2, type1)) {
                            allHaveHierarchyRelationship = false;
                        }
                    }

                    // after checking all the arguments, if they are an exactly the same method ->
                    if (isExactMatch) {
                        // exactly the same in the same class -> ERROR
                        if (className.equals(currentClassName)) {
                            throw new RuntimeException("TYPE ERROR: Method " + nameWithoutParameters + " is already defined with identical arguments in class " + className + " (line " + line + ")");
                        } 
                        else {
                            // we have an override but we check the return type, if it is not the same -> ERROR
                            if (!methodToCheck.returnType.equals(currentMethod.returnType)) {
                                throw new RuntimeException("TYPE ERROR: Method " + nameWithoutParameters + " in class " + className + 
                                    " must have the same return type as the method it overrides in class " + currentClassName + " (line " + line + ")");
                            }
                        }
                    }
                    else if (allHaveHierarchyRelationship) {
                        // all the arguments have a hierarchy relationship
                        throw new RuntimeException("TYPE ERROR: Ambiguous overloading for method " + nameWithoutParameters + " in class " + className + 
                            ". All arguments have a subtype/supertype relationship with a method in class " + currentClassName + " (line " + line + ")");
                    }
                }
            }
            currentClassName = currentClass.extendsFrom;
        }
    }
}