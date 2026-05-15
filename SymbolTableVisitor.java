import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;


public class SymbolTableVisitor extends GJDepthFirst<String, Void> {

    public GlobalSymbolTable symbolTable = new GlobalSymbolTable();
    public String mainClassName = null;
   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    @Override
    public String visit(MainClass n, Void argu) {
        // name of the main class
        String className = n.f1.accept(this, null);
        // pass the line of the class name
        symbolTable.enterClass(className, null, n.f1.f0.beginLine);
        mainClassName = className;

        // main has no methods is static but we put it in the symbol table for later checks
        symbolTable.enterMethod("void", "main");
        String argName = n.f11.accept(this, null);
        // pass the line of the parameter of the main method
        symbolTable.insertParameter(argName, "String[]", n.f11.f0.beginLine);

        // f14 is a list of variable declarations, we need to visit them to add them to the symbol table
        n.f14.accept(this, null);
        
        // pass the line of the "main" token (f6)
        symbolTable.exitMethod(n.f6.beginLine);
        symbolTable.currentClass.methodOffsets.clear();
        // so as all the classes derived from the main class to have the correct offsets (found out from test73)
        symbolTable.currentClass.nextMethodOffset = 0;
        symbolTable.exitClass();
        
        return null;
    }


   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public String visit(ClassDeclaration n, Void argu){
        String className = n.f1.accept(this, null);
        symbolTable.enterClass(className, null, n.f1.f0.beginLine);
        // so as to visit the variable and method declarations
        n.f3.accept(this, null);
        n.f4.accept(this, null);
        symbolTable.exitClass();
        return null;
    }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu){
        String className = n.f1.accept(this, null);
        String parentName = n.f3.accept(this, null);
        // now there is a parent-child relationship
        symbolTable.enterClass(className, parentName, n.f1.f0.beginLine);

        n.f5.accept(this, null);
        n.f6.accept(this, null);
        symbolTable.exitClass();
        return null;
    }

    /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    @Override
    public String visit(MethodDeclaration n, Void argu){
        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);
        symbolTable.enterMethod(returnType, methodName);
        
        // visit the parameters and the variable declarations to add them to the symbol table
        n.f4.accept(this, null);
        n.f7.accept(this, null);
        
        symbolTable.exitMethod(n.f2.f0.beginLine);
        return null;
    }


   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
    public String visit(FormalParameter n, Void argu){
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        symbolTable.insertParameter(name, type, n.f1.f0.beginLine);
        return null;
    }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu){
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        // insert method can decides if the parameter is a local variable or a field based on the current scope
        symbolTable.insert(name, type, n.f1.f0.beginLine);
        return null;
    }

    @Override
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}