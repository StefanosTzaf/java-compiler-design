import java_cup.runtime.*;

%%
/* -----------------Options and Declarations Section----------------- */

/* class Scanner will be generated and written to Scanner.java */
%class Scanner

/* errors will be reported with line and column numbers */
%line
%column

/* declare that this scanner will be used with a CUP parser */
%cup

%unicode

%{  
    /* StrinBuffer instead of String so as to be able to append characters to it when we are in the STRING state more efficiently */
    StringBuffer stringBuffer = new StringBuffer();
    
    /* methods so as Scanner can return Symbol objects to the parser */

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/*  Macro Declarations for Lexical Rules Section  */


LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Identifier     = [a-zA-Z_][a-zA-Z0-9_]*

/* state for scanning string literals, when it enters this state reads everything like text */
%state STRING

%%
/* ------------------------Lexical Rules Section---------------------- */

<YYINITIAL> {
    "if"         { return symbol(sym.IF); }
    "else"       { return symbol(sym.ELSE); }
    "prefix"       { return symbol(sym.PREFIX); }
    "suffix"       { return symbol(sym.SUFFIX); }

    "+" { return symbol(sym.PLUS); }
    "(" { return symbol(sym.LPAR); }
    ")" { return symbol(sym.RPAR); }
    "{" { return symbol(sym.LBRACE); }
    "}" { return symbol(sym.RBRACE); }
    "," { return symbol(sym.COMMA); }

    /* yytext is the text that was matched by the regular expression, namely the identifier */
    {Identifier} { return symbol(sym.IDENTIFIER, yytext()); }

    {WhiteSpace} { /* ignore white space */ }

    /* changes state, now reads text till it finds a " we set length to 0 each time a new string is read */
    \" { stringBuffer.setLength(0); yybegin(STRING); }
}

<STRING>{
    /* if it finds a " it means the string is finished, so change state and return the string literal */
    \"                  { yybegin(YYINITIAL);
                        return symbol(sym.STRING_LITERAL, stringBuffer.toString()); }
    [^\n\r\"\\]+        { stringBuffer.append( yytext() ); }
    \\t                 { stringBuffer.append('\t'); }
    \\n                 { stringBuffer.append('\n'); }

    \\r                 { stringBuffer.append('\r'); }
    \\\"                { stringBuffer.append('\"'); }
    \\                  { stringBuffer.append('\\'); }
}

/* error handling*/
[^]                     { throw new Error("Illegal character <" + yytext() + "> at line " + (yyline + 1) + ", column " + (yycolumn + 1)); }