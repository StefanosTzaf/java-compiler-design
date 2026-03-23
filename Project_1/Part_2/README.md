## Grammar

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
It cannot understand if the IDENTIFIER is a declaration of a function or a call of a function. 