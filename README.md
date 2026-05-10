# Project 2: MiniJava Static Checking (Semantic Analysis)

First aff all i implemented the symbol table structure. So as to do this i created a class called `GlobalSymbolTable` that stores
all the classes with their names. We want to keep in the symbol table all the declarations (not the statements or the expressions)
so since in java everythin is declared inside a class, we can just store the classes and then inside the class we can store the methods and the variables. These class also keeps two variables of the current class and the current method that we are visiting. Also
provides an interface to add classes, methods and variables (either local or field variables). Insert function also computes the offset
while inserting.


There are two smaller classes `MethodSymbolTable` and `ClassSymbolTable` that are used to handle the methods and the classes respectively.

## First Custom Visitor (SymbolTableVisitor)

This visitor just makes the symbol table and is not doing any checking. It also computes the offsets through the insert function.
In this visitor the only methods that i had to `Override` are the ones that have declarations, so as to store everything in the symbol table before starting the checking. 


After the first implementation i realised that the offsets were computed correctly for the fields and methods in base classes but for the derived classes the offsets were not. 

For the fields, i just added a field in the `ClassSymbolTable` class that keeps the next field offset, so we don't have to traverse the parent class every time we want to compute the offset of a parent in a derived class. When exiting the scope, i save the offset of the final field of this class to the `nextFieldOffset` field, so when we enter a derived class we can just start counting the offsets from there. 

## Second Custom Visitor
