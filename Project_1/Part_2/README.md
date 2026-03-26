# Project 1 - Part 2

## How to compile and run the project:

* There is a Makefile to compile the project.
* To run the project, use the command ```java -cp java-cup-11b-runtime.jar:. Compiler``` 
* There is a Script that runs some examples of the examples folder, and you can see the results in the results folder.
  just run the command ```./run_examples.sh``` in the terminal. In every case you can run it yourself with the command above
  and the input file you want, for example: ```java -cp java-cup-11b-runtime.jar:. Compiler < examples/example1.txt > results/example1.java```
## Grammar

This was the first try to make the grammar. I ended up to separate Func_expr and Main_expr because
in the calls' section we cant have an identifier which is not a call of a function.

Program     -> Dec_list Calls

Dec_list    -> Declaration Dec_list | ε
Declaration -> IDENTIFIER LPAR Parameters RPAR LBRACE Func_expr RBRACE
Parameters  -> IDENTIFIER COMMA Parameters | IDENTIFIER | ε
Func_expr   -> Func_expr PLUS Func_expr | IDENTIFIER LPAR Func_args RPAR |
               IF LPAR Func_cond RPAR Func_expr ELSE Func_expr | STRING_LITERAL | IDENTIFIER
Func_args   -> Func_expr COMMA Func_args | Func_expr | ε
Func_cond   -> Func_expr PREFIX Func_expr | Func_expr SUFFIX Func_expr


Calls       -> Main_expr Calls | Main_expr
Main_expr   -> Main_expr PLUS Main_expr | IDENTIFIER LPAR Main_args RPAR |
               STRING_LITERAL | IF LPAR Main_cond RPAR Main_expr ELSE Main_expr
Main_args   -> Main_expr COMMA Main_args | Main_expr | ε
Main_cond   -> Main_expr PREFIX Main_expr | Main_expr SUFFIX Main_expr



I transformed the grammar into cup file but i take these conflicts:
```
Warning : *** Shift/Reduce conflict found in state #0
  between dec_list ::= (*) 
  and     declaration ::= (*) IDENTIFIER LPAR parameters RPAR LBRACE func_expr RBRACE 
  under symbol IDENTIFIER
  Resolved in favor of shifting.

Warning : *** Shift/Reduce conflict found in state #4
  between dec_list ::= (*) 
  and     declaration ::= (*) IDENTIFIER LPAR parameters RPAR LBRACE func_expr RBRACE 
  under symbol IDENTIFIER
  Resolved in favor of shifting.


Error : *** More conflicts encountered than expected -- parser generation aborted 
```

The problem as i found out was that when the grammar finds a IDENTIFIER , for
example ```foo()``` it could not decide if it is a declaration or a call because both of them start with IDENTIFIER LPAR. 
So i had somehow to unify them in the beggining to a "common" item

```
...
Program       -> Item_list

Item_list     -> Item Item_list 
               | Item

Item          -> Declaration 
               | Main_expr
...

```


But in this way it will give the ability to have calls and 
declarations mixed (not all the declarations at the beggining of the program). The solution i found to this, 
was to use an array (in cup file) with two fields, one for the declarations and one for the calls, 
so now our Item will return an array of two fields and when we construct the final
output we will concatanate with the right order the declarations and the calls.

Now the conflicts moved to the Parameters :
```
Warning : *** Reduce/Reduce conflict found in state #30
  between parameters ::= (*) 
  and     main_args ::= (*) 
  under symbols: {RPAR}
  Resolved in favor of the first production.

Warning : *** Shift/Reduce conflict found in state #30
  between parameters ::= (*) 
  under symbol RPAR
  Resolved in favor of shifting.

Warning : *** Shift/Reduce conflict found in state #30
  between main_args ::= (*) 
  under symbol RPAR
  Resolved in favor of shifting.

Error : *** More conflicts encountered than expected -- parser generation aborted
```
That happened because if we have a declaration like ```foo()``` the parser can not decide if it is a declaration with no 
parameters or a declaration no parameters because both parameters and main_args can be empty. The solution i found was to move
the posibility of empty arguments functions into func_expr and main_expr and declaration rules. So now it is clear earlier which case is it.


Program -> Item_list

Item_list -> Item Item_list | Item
Item -> Declaration | Main_expr

Declaration -> IDENTIFIER LPAR Parameters RPAR LBRACE Func_expr RBRACE
             | IDENTIFIER LPAR RPAR LBRACE Func_expr RBRACE

Parameters -> IDENTIFIER COMMA Parameters | IDENTIFIER

Func_expr -> Func_expr PLUS Func_expr | IDENTIFIER LPAR Func_args RPAR 
           | IF LPAR Func_cond RPAR Func_expr ELSE Func_expr | STRING_LITERAL | IDENTIFIER

Func_args -> Func_expr COMMA Func_args | Func_expr
Func_cond -> Func_expr PREFIX Func_expr | Func_expr SUFFIX Func_expr

Main_expr -> Main_expr PLUS Main_expr | IDENTIFIER LPAR Main_args RPAR
           | STRING_LITERAL | IF LPAR Main_cond RPAR Main_expr ELSE Main_expr

Main_args -> Main_expr COMMA Main_args | Main_expr
Main_cond -> Main_expr PREFIX Main_expr | Main_expr SUFFIX Main_expr