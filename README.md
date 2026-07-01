
# Compilers Course Assignments

This repository contains three projects developed for the Compilers course of the University of Athens. The goal of these assignments is the practical application of lexical, syntax, and semantic analysis techniques, as well as code translation.

Each project is located in its own folder. Below is a brief description of each, and **for detailed instructions on compiling, running, and an explanation of the implementation, please refer to the respective `README.md` files in each folder.**

---

## 1. String Expression Evaluator

**Folder:** [`[01_string_expression_evaluator]`](./01_string_expression_evaluator)

This project implements a simple string expression evaluator[cite: 1]. The evaluator accepts expressions with the `**` operator (which concatenates its second operand twice at the end of its first operand), the `/` operator (which acts as a right-associative suffix removal), and parentheses[cite: 1]. The implementation involved refactoring the given grammar to eliminate left recursion for LL(1) parsing and building a recursive descent parser from scratch in Java[cite: 1].

## 2. String-to-Java Translator

**Folder:** [`[02_string_to_java_translator]`](./02_string_to_java_translator)

In this project, a parser and translator are implemented for a language supporting string operations, function definitions and calls, and conditionals (if-else)[cite: 1]. The parser was generated using the JavaCUP tool[cite: 1]. The program analyzes the input code and translates it into an equivalent, valid subset of Java (producing `.java` files) that can be compiled using `javac` and executed normally[cite: 1].

## 3. MiniJava Semantic Analyzer

**Folder:** [`[03_minijava_semantic_analyzer]`](./03_minijava_semantic_analyzer)

The third project focuses on building a semantic analyzer (static type checker) for MiniJava, a fully object-oriented subset of Java[cite: 1]. The program reads the abstract syntax tree produced by JavaCC and JTB tools and verifies the correctness of the code[cite: 1]. The analysis is implemented using the Visitor pattern: the first visitor constructs a hierarchical Symbol Table (storing classes, methods, local/field variables, and computing memory offsets), and the second visitor traverses the nodes to perform strict type checking, enforcing rules for inheritance, method overriding, and overloading[cite: 1, 3].
