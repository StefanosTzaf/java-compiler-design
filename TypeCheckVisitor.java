import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Void> { 
    
    private GlobalSymbolTable symTable;
    private String currentClassName;
    private String currentMethodName;

    public TypeCheckVisitor(GlobalSymbolTable symTable) {
        this.symTable = symTable;
        this.currentClassName = null;
        this.currentMethodName = null;
    }

    private void throwError(String msg) {
        throw new RuntimeException(msg);
    }

    // DECLARATIONS

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
        String className = n.f1.f0.toString();
        // these will be may needed for type checking in childs (for example in a statement)
        currentClassName = className;
        currentMethodName = "main";
        n.f14.accept(this, null); // VarDeclarations
        n.f15.accept(this, null); // Statements
        currentMethodName = null;
        currentClassName = null;
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
    public String visit(ClassDeclaration n, Void argu) {
        String className = n.f1.f0.toString();
        currentClassName = className;
        currentMethodName = null; 
        n.f3.accept(this, null); // VarDeclarations
        n.f4.accept(this, null); // MethodDeclarations
        currentClassName = null;
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
    public String visit(ClassExtendsDeclaration n, Void argu) {
        String className = n.f1.f0.toString();
        this.currentClassName = className;
        this.currentMethodName = null;
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        currentClassName = null;
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
    public String visit(MethodDeclaration n, Void argu) {
        currentMethodName = n.f2.f0.toString();
        String returnedType = n.f1.accept(this, null);
        n.f7.accept(this, null);
        n.f8.accept(this, null);
        String actualReturnType = n.f10.accept(this, null);

        // checks if the real return type is a subtype of the expected return type
        if (!symTable.isSubtype(actualReturnType, returnedType)) {
            throwError("Method " + currentMethodName + " expects " + returnedType + " but returns " + actualReturnType);
        }

        currentMethodName = null;
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu) {
        String varType = n.f0.accept(this, null);
        if(!varType.equals("int") && !varType.equals("boolean") && !varType.equals("int[]")) {
            if (!symTable.containsClass(varType))
                throwError("Unknown class type: " + varType);
        }
        return null;
    }
}