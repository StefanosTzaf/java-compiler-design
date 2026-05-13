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
            throwError("TYPE ERROR: Method " + currentMethodName + " expects " + returnedType + " but returns " + actualReturnType);
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
            if (!symTable.containClass(varType)){
                throwError("TYPE ERROR: Unknown class type: " + varType);
            }
        }
        return null;
    }

    // EXPRESSIONS

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, Void argu) {
        String leftType = n.f0.accept(this, argu);
        if (!leftType.equals("boolean")){
            throwError("TYPE ERROR: Operator && requires boolean operands, <" + leftType + "> was given as left operand");
        }
        String rightType = n.f2.accept(this, argu);
        if (!rightType.equals("boolean")){
            throwError("TYPE ERROR: Operator && requires boolean operands, <" + rightType + "> was given as right operand");
        }
        return "boolean";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, Void argu) {
        String leftType = n.f0.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator < requires int operands, <" + leftType + "> was given as left operand");
        }
        String rightType = n.f2.accept(this, argu);
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator < requires int operands, <" + rightType + "> was given as right operand");
        }
        return "boolean";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, Void argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator + requires int operands, <" + leftType + "> was given as left operand");
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator + requires int operands, <" + rightType + "> was given as right operand");
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, Void argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator - requires int operands, <" + leftType + "> was given as left operand");
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator - requires int operands, <" + rightType + "> was given as right operand");
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, Void argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator * requires int operands, <" + leftType + "> was given as left operand");
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator * requires int operands, <" + rightType + "> was given as right operand");
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, Void argu) {
        String arrayType = n.f0.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
        if (!arrayType.equals("int[]")) {
            throwError("TYPE ERROR: ArrayLookup into non-array type, <" + arrayType + "> was given");
        }
        if (!indexType.equals("int")) {
            throwError("TYPE ERROR: Index of array must be of type int, <" + indexType + "> was given");
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, Void argu) {
        String arrayType = n.f0.accept(this, argu);
        if (!arrayType.equals("int[]")) {
            throwError("TYPE ERROR: ArrayLength can only be applied to int arrays, <" + arrayType + "> was given");
        }
        return "int";
    }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, Void argu) {
        String className = n.f1.f0.toString();
        if (!symTable.containClass(className)) {
            throwError("TYPE ERROR: Unknown class type: " + className);
        }
        return className;
    }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(ArrayAllocationExpression n, Void argu) {
        String sizeType = n.f3.accept(this, argu);
        if (!sizeType.equals("int")) {
            throwError("Size of array must be an int, <" + sizeType + "> was given");
        }
        return "int[]";
    }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, Void argu) {
        String clauseType = n.f1.accept(this, argu);
        if (!clauseType.equals("boolean")){
           throwError("TYPE ERROR: Not expression can only be applied to boolean, <" + clauseType + "> was given");
        }
        return "boolean";
    }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, Void argu) {
        return n.f1.accept(this, argu);
    }

    // TERMINALS
    @Override
    public String visit(IntegerLiteral n, Void argu) { 
        return "int"; 
    }

    @Override
    public String visit(TrueLiteral n, Void argu) { 
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, Void argu) { 
        return "boolean";
    }

    @Override
    public String visit(ThisExpression n, Void argu) {
        if (currentClassName == null) {
            throwError("This used outside of a class");
        }
        return currentClassName;
    }

    @Override
    public String visit(Identifier n, Void argu) {
        String idName = n.f0.toString();
        String type = symTable.getVarType(currentClassName, currentMethodName, idName);
        if (type == null) {
            // VarDeclaration will catch it and print the error
            return idName;
        }
        return type;
    }

    @Override
    public String visit(IntegerType n, Void argu) { 
        return "int"; 
    }

    @Override
    public String visit(BooleanType n, Void argu) { 
        return "boolean"; 
    }

    @Override
    public String visit(ArrayType n, Void argu) { 
        return "int[]"; 
    }
}