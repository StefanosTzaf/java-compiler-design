# String Expression Evaluator

## Explanation of the way of thinking
The first problem I tried to solve was how to impplement the precedence of the operators. Initially,
I converted the grammar like this:
```
    exp -> exp / term | term
    term -> term ** term | (exp) | str
    ...
```
however, I realized that this grammar is ambiguous, like the example from the slides, and it can 
resault in many different leftmost derivations. For example, the string ```a**b**c``` can be derived in two different ways:
```
    exp -> term -> term ** term -> str ** term -> a ** term -> a ** term ** term -> a ** b ** c
    exp -> term -> term ** term -> term ** term ** term -> str ** term ** term -> a ** b ** c
```
So I ended up with the following grammar:
```
    exp -> exp / term | term
    term -> term ** factor | factor 
    factor -> (exp) | str
    ...
```

Then i had to deal with left and right associativity. The operator '/' is right associative, and the operator `**` is 
left associative:
```
    exp -> term / exp | term
    term -> term ** factor | factor 
    factor -> (exp) | str
    ...
```

The problem now is that the grammar is left recursive in the rule `term -> term ** factor`. To solve this problem, 
I had to eliminate left recursion as it was shown in the slides:
```
    exp -> term / exp | term
    term -> factor term2
    term2 -> ** factor term2 | ε
    factor -> (exp) | str
    ...
```

The last step to have a LL(1) grammar is to chech FIRST+ sets. The grammar with all the rules, one in each line is:
```
    exp -> term / exp 
         | term
    term -> factor term2
    term2 -> ** factor term2
         | ε
    factor -> (exp) 
         | str
    str -> char 
         | char str 
    char -> a-z 
         | A-Z
```
First+ sets of the two first rules of exp are:
```
    FIRST+(exp -> term / exp) = FIRST(term) = FIRST(factor) .... = {'(', a-z, A-Z}
    FIRST+(exp -> term) = {'(', a-z, A-Z}
```
So since the FIRST+ sets of the two rules of exp have not empty intersection, we have to refactor 
```
    exp -> term exp2
    exp2 -> / exp 
         | ε
```
After checking all the others First+ sets, the only problem was with str:

```
    FIRST+(str -> char) = FIRST(char) = {a-z, A-Z}
    FIRST+(str -> char str) = FIRST(char) = {a-z, A-Z}
```
So I had to refactor str as well:
```
    str -> char str2
    str2 -> str 
         | ε
```
And now the FIRST+ sets of the two rules of str are:
```
    FIRST+(str -> char str2) = FIRST(char) = {a-z, A-Z}
    FIRST+(str -> str) = FIRST(char) = {a-z, A-Z}
    FIRST+(str2 -> ε) = {FOLLOW(str2)} = {FOLLOW(str)} = {FOLLOW(factor)} = {FIRST(term2)} = {**, FOLLOW(term)} = {**, /, FOLLOW(exp)} = {**, /, ), $}
```

So the final grammar is:
```
    exp -> term exp2
    exp2 -> / exp | ε
    term -> factor term2
    term2 -> ** factor term2 | ε
    factor -> (exp) | str
    str -> char str2
    str2 -> str | ε
    char -> a-z | A-Z
```

## Explanation of the code
In the beggining I implemented a parser that just checks if the input string is valid according to the grammar,
and returns True or False. Then I implemented the evaluation of the expressions. Every non terminal has a function
and returns ```String``` instead of ```Boolean```.  