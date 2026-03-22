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
               STRING_LITERAL
Main_args   -> Main_expr COMMA Main_args | Main_expr | ε
Main_cond   -> Main_expr PREFIX Main_expr | Main_expr SUFFIX Main_expr