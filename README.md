# Project 2: MiniJava Type Checking (Semantic Analysis)

## Symbol Table Structure and Implementation

First of all i implemented the symbol table structure in ```GlobalSymbolTable.java```. So as to do this i created a class called `GlobalSymbolTable` that stores
all the classes with their names. We want to keep in the symbol table all the declarations (not the statements or the expressions)
so since in MiniJava everything is declared inside a class, we can just store the classes and then inside the class we can store the methods and the variables. This class also keeps two variables of the current class and the current method that we are visiting. Also
provides an interface (followed the interface of slides of the course for a symbol table) to add classes, methods and variables (either local or field variables). Insert function also computes the offset while inserting.

There are two smaller classes `MethodSymbolTable` and `ClassSymbolTable` that are used to handle the methods and the classes respectively. In my mind the symbol table is one but is tree like, with the GlobalSymbolTable as the root, then the ClassSymbolTable that contains the methods symbol tables. Actually, the final symbol table is one but is implemented in a hierarchical way.

I used LinkedHashMap to store the classes, methods and variables because it keeps the order of insertion, so when we want to compute the offsets we can just iterate over the LinkedHashMap and compute the offsets in the order of insertion. It also provides the fast lookup of the hash maps.

To handle MiniJava's method overloading rules, i insert the functions methods into the map as name_type1_type2. So every method is uniquely identified by its name and parameter types. However, the original method name is preserved inside the MethodSymbolTable object. The check for the overriding and overloading rules as described in the project specification is implemented in the ```checkOverrideAndOverload``` method.

## First Custom Visitor (SymbolTableVisitor)

This visitor just makes the symbol table and is not doing any checking (except for some checks that happen while inserting such as trying to insert a method that already exists). It also computes the offsets through the insert function. In this visitor the only methods that i had to `Override` are the ones that have declarations, so as to store everything in the symbol table before starting the checking. 

After the first implementation i realised that the offsets were computed correctly for the fields and methods in base classes but for the derived classes the offsets were not. 
So as to solve this, i just added a field in the `ClassSymbolTable` class that keeps the next field offset, so we don't have to traverse the parent class every time we want to compute the offset of a parent in a derived class. When exiting the scope, i save the offset of the final field of this class to the `nextFieldOffset` field, so when we enter a derived class we can just start counting the offsets from there. Finally, I handle the main method specially: I add it to the symbol table to allow type checking of its local variables, but I skip it during the offset computation and override/overload checks since it is static.

## Second Custom Visitor

This visitor has access to the ready symbol table and is used to do the checking. The general idea is to enter the class or the method (depending on the context) and the accept the children (accept does visit(this)) and then exit the scope. In some cases we pass string "EXPRESSION" to idinify the ```context``` of the identifier. For example if we are visiting an identifier that is part of an expression we return its type, else just pass its name. This is useful for example when we are visiting a while statement and we want to check the type of the condition (just calling accept to this child).
Ι αdded line to the error reporting. Most of the times works well and says the exact line of the error, and is really helpfull in big files for example in TreeVisitor-error.java.

## Script for testing and Makefile

There is also a script `test.sh` that runs the tests for 2 folders that have tests for valid input to check offsets and another folder that has tests for invalid input to check the errors. It does not print the output of the valid tests, just compares it with the expected output. If you want to see the output of the valid tests or the specific error messages (and the line), you can just run the command `java Main test/inputs/1_examples_passing_type_checking/BinaryTree.java` for example. There is also a Makefile that runs the JTB, JavaCC and then then compile my files. For cleaning the produced java files and class files there is the command `make clean`.

## Disclaimer for the offset prining

In the begginning i was printing the offsets with the format name_type1_type2... , in the way that are saved as keys in the symbol table.
However, so as to automate the testing, i changed the format to name : offset just as in the instructions (two methods foo(int x) and foo(boolean y) will be printed as foo : 0 and foo : 4 for example).