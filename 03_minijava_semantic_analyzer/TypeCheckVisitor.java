import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

public class TypeCheckVisitor extends GJDepthFirst<String, String> { 
    
    private GlobalSymbolTable symTable;
    private String currentClassName;
    private String currentMethodName;

    public TypeCheckVisitor(GlobalSymbolTable symTable) {
        this.symTable = symTable;
        this.currentClassName = null;
        this.currentMethodName = null;
    }

    private void throwError(String msg, int line) {
        throw new RuntimeException(msg + " (line " + line + ")");
    }

    // ------------------------------- DECLARATIONS -------------------------------

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
    public String visit(MainClass n, String argu) {
        String className = n.f1.accept(this, argu);
        // these will be may needed for type checking in childs (for example in a statement)
        currentClassName = className;
        currentMethodName = "main";
        n.f14.accept(this, argu); // VarDeclarations
        n.f15.accept(this, argu); // Statements
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
    public String visit(ClassDeclaration n, String argu) {
        String className = n.f1.accept(this, argu);
        currentClassName = className;
        currentMethodName = null; 
        n.f3.accept(this, argu); // VarDeclarations
        n.f4.accept(this, argu); // MethodDeclarations
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
    public String visit(ClassExtendsDeclaration n, String argu) {
        String className = n.f1.accept(this, argu);
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
    public String visit(MethodDeclaration n, String argu) {
        currentMethodName = n.f2.accept(this, argu);
        String returnedType = n.f1.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        // we pass "EXPRESSION" as argu to know that we are in an expression context, so later
        // the Identifier visitor will know that it has to return the type of the identifier instead of its name
        String actualReturnType = n.f10.accept(this, "EXPRESSION");

        // checks if the real return type is a subtype of the expected return type
        if (!symTable.isSubtype(actualReturnType, returnedType)) {
            throwError("TYPE ERROR: Method " + currentMethodName + " expects " + returnedType + " but returns " + actualReturnType, n.f2.f0.beginLine);
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
    public String visit(VarDeclaration n, String argu) {
        String varType = n.f0.accept(this, argu);
        if(!varType.equals("int") && !varType.equals("boolean") && !varType.equals("int[]")) {
            if (!symTable.containClass(varType)){
                throwError("TYPE ERROR: Unknown class type: " + varType, n.f1.f0.beginLine);
            }
        }
        return null;
    }

    // -------------------- EXPRESSIONS --------------------

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, String argu) {
        String leftType = n.f0.accept(this, argu);
        if (!leftType.equals("boolean")){
            throwError("TYPE ERROR: Operator && requires boolean operands, <" + leftType + "> was given as left operand", n.f1.beginLine);
        }
        String rightType = n.f2.accept(this, argu);
        if (!rightType.equals("boolean")){
            throwError("TYPE ERROR: Operator && requires boolean operands, <" + rightType + "> was given as right operand", n.f1.beginLine);
        }
        return "boolean";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) {
        String leftType = n.f0.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator < requires int operands, <" + leftType + "> was given as left operand", n.f1.beginLine);
        }
        String rightType = n.f2.accept(this, argu);
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator < requires int operands, <" + rightType + "> was given as right operand", n.f1.beginLine);
        }
        return "boolean";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator + requires int operands, <" + leftType + "> was given as left operand", n.f1.beginLine);
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator + requires int operands, <" + rightType + "> was given as right operand", n.f1.beginLine);
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator - requires int operands, <" + leftType + "> was given as left operand", n.f1.beginLine);
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator - requires int operands, <" + rightType + "> was given as right operand", n.f1.beginLine);
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) {
        String leftType = n.f0.accept(this, argu);
        String rightType = n.f2.accept(this, argu);
        if (!leftType.equals("int")){
            throwError("TYPE ERROR: Operator * requires int operands, <" + leftType + "> was given as left operand", n.f1.beginLine);
        }
        if (!rightType.equals("int")){
            throwError("TYPE ERROR: Operator * requires int operands, <" + rightType + "> was given as right operand", n.f1.beginLine);
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
    public String visit(ArrayLookup n, String argu) {
        String arrayType = n.f0.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
        if (!arrayType.equals("int[]")) {
            throwError("TYPE ERROR: ArrayLookup into non-array type, <" + arrayType + "> was given", n.f1.beginLine);
        }
        if (!indexType.equals("int")) {
            throwError("TYPE ERROR: Index of array must be of type int, <" + indexType + "> was given", n.f1.beginLine);
        }
        return "int";
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) {
        String arrayType = n.f0.accept(this, argu);
        if (!arrayType.equals("int[]")) {
            throwError("TYPE ERROR: ArrayLength can only be applied to int arrays, <" + arrayType + "> was given", n.f1.beginLine);
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
    public String visit(AllocationExpression n, String argu) {
        String className = n.f1.accept(this, null);
        if (!symTable.containClass(className)) {
            throwError("TYPE ERROR: Unknown class type: " + className, n.f0.beginLine);
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
    public String visit(ArrayAllocationExpression n, String argu) {
        String sizeType = n.f3.accept(this, "EXPRESSION");
        if (!sizeType.equals("int")) {
            throwError("TYPE ERROR: Size of array must be an int, <" + sizeType + "> was given", n.f0.beginLine);
        }
        return "int[]";
    }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, String argu) {
        String clauseType = n.f1.accept(this, argu);
        if (!clauseType.equals("boolean")){
           throwError("TYPE ERROR: Not expression can only be applied to boolean, <" + clauseType + "> was given", n.f0.beginLine);
        }
        return "boolean";
    }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) {
        return n.f1.accept(this, argu);
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public String visit(MessageSend n, String argu) {
        // remember that literals return their own type! We have to pass EXPRESSION as argu in case of an identifier
        String objectType = n.f0.accept(this, "EXPRESSION");
        String methodName = n.f2.accept(this, null);
        if (objectType.equals("int") || objectType.equals("boolean") || objectType.equals("int[]")) {
            throwError("TYPE ERROR: Cannot call method " + methodName + " on basic type " + objectType, n.f2.f0.beginLine);
        }

        Vector<String> argumentTypes = new Vector<>();
        // f4 is optional so we need to check if it is present before trying to access it
        if (n.f4.present()) {
            // take the types of the arguments and put them in a vector
            String typesOfArguments = n.f4.accept(this, "EXPRESSION");
            argumentTypes.addAll(Arrays.asList(typesOfArguments.split(",")));
        }

        String returnType = symTable.getMethodReturnType(objectType, methodName, argumentTypes);
        if (returnType == null) {
            throwError("TYPE ERROR: Method " + methodName + " with given argument types not found in class " + objectType, n.f2.f0.beginLine);
        }
        return returnType;
    }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) {
        // the first type 
        String firstType = n.f0.accept(this, argu);
        // expression tail will return the rest of the types separated by comma
        String restTypes = n.f1.accept(this, argu); 
        
        if (restTypes != null && !restTypes.isEmpty()) {
            return firstType + "," + restTypes;
        }
        return firstType;
    }

   /**
    * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) {
        List<String> listOfTypes = new ArrayList<>();
        
        if (n.f0.present()) {
            // for every expression term (because of *)
            for (int i = 0; i < n.f0.size(); i++) {
                // send firstly accept to ExpressionTerm
                String type = n.f0.elementAt(i).accept(this, argu);
                listOfTypes.add(type);
            }
        }
        return String.join(",", listOfTypes);
    }


   /**
    * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, String argu) {
        // returns the type of the expression
        return n.f1.accept(this, argu);
    }

    // ---------------------- TERMINALS ----------------------
    @Override
    public String visit(IntegerLiteral n, String argu) { 
        return "int"; 
    }

    @Override
    public String visit(TrueLiteral n, String argu) { 
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, String argu) { 
        return "boolean";
    }

    @Override
    public String visit(ThisExpression n, String argu) {
        if (currentClassName == null) {
            throwError("TYPE ERROR: <This> used outside of a class", n.f0.beginLine);
        }
        return currentClassName;
    }


    // IMPORTANT: gets context as argu to know if the identifier is used in an expression or not. If it is used in an expression, 
    // we need to return its type, otherwise we just return its name (for example in ClassDeclaration)   
    @Override
    public String visit(Identifier n, String argu) {
        String idName = n.f0.toString();
        
        // if the identifier is used in an expression, we need to check if it is a variable and return its type
        if (argu != null && argu.toString().equals("EXPRESSION")) {
            String type = symTable.getVarType(currentClassName, currentMethodName, idName);
            if (type == null) {
                throwError("TYPE ERROR: Variable " + idName + " not found in current scope", n.f0.beginLine);
            }
            return type;
        }
        return idName;
    }


    @Override
    public String visit(IntegerType n, String argu) { 
        return "int"; 
    }

    @Override
    public String visit(BooleanType n, String argu) { 
        return "boolean"; 
    }

    @Override
    public String visit(ArrayType n, String argu) { 
        return "int[]"; 
    }

    // -------------------- STATEMENTS --------------------

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) {
        String leftType = n.f0.accept(this, "EXPRESSION"); 
        String rightType= n.f2.accept(this, "EXPRESSION"); 

        if (!symTable.isSubtype(rightType, leftType)) {
            throwError("TYPE ERROR: " + rightType + " cannot be assigned to " + leftType, n.f1.beginLine);
        }
        return null;
    }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   @Override 
    public String visit(ArrayAssignmentStatement n, String argu) {
        String arrayType = n.f0.accept(this, "EXPRESSION");
        String indexType = n.f2.accept(this, "EXPRESSION");
        String rightType = n.f5.accept(this, "EXPRESSION");

        if (!arrayType.equals("int[]")) {
            throwError("TYPE ERROR: Left side of array assignment must be of type int[], <" + arrayType + "> was given", n.f0.f0.beginLine);
        }
        if (!indexType.equals("int")) {
            throwError("TYPE ERROR: Index of array assignment must be of type int, <" + indexType + "> was given", n.f1.beginLine);
        }
        if (!rightType.equals("int")) {
            throwError("TYPE ERROR: Right side of array assignment must be of type int, <" + rightType + "> was given", n.f4.beginLine);
        }
        return null;
    }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, String argu) {
        String conditionType = n.f2.accept(this, "EXPRESSION");
        if (!conditionType.equals("boolean")){
            throwError("TYPE ERROR: Condition of if statement must be boolean, <" + conditionType + "> was given", n.f0.beginLine);
        }
        n.f4.accept(this, "EXPRESSION");
        n.f6.accept(this, "EXPRESSION");
        return null;
    }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
    public String visit(WhileStatement n, String argu) {
        String conditionType = n.f2.accept(this, "EXPRESSION");
        if (!conditionType.equals("boolean")){
            throwError("TYPE ERROR: Condition of while statement must be boolean, <" + conditionType + "> was given", n.f0.beginLine);
        }
        n.f4.accept(this, argu);
        return null;
    }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */

   @Override
    public String visit(PrintStatement n, String argu) {
        String exprType = n.f2.accept(this, "EXPRESSION");
        // as said in piazza, printing only int
        if (!exprType.equals("int")){
            throwError("TYPE ERROR: Print statement can only print int, <" + exprType + "> was given", n.f0.beginLine);
        }
        return null;
    }

}