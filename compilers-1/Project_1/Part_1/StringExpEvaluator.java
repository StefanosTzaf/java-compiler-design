import java.io.InputStream;
import java.io.IOException;

class StringExpEvaluator{
    
    // final ~ don't change the input stream after initialization
    private final InputStream in;

    // next character to be processed
    private int lookahead;
    
    public StringExpEvaluator(InputStream in) throws IOException{
        this.in = in;
        lookahead = in.read();
    }
    
    private void consume(int symbol) throws IOException, ParseError{
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isLetter(int c){
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    // we want to skip spaces before between tokens
    private void skipSpaces() throws IOException {
        while (lookahead == ' ' || lookahead == '\t' || lookahead == '\r') {
            lookahead = in.read();
        }
    }

    private String operatorStars(String a, String b){
        return a + b + b;
    }

    private String operatorDivision(String a, String b){
        // if b is not a suffix of a, return a
        if(! a.endsWith(b)){
            return a;
        }
        else{
            return a.substring(0, a.length() - b.length());
        }
    }

    public String eval() throws IOException, ParseError{
        String value = exp();

        skipSpaces();

        if(lookahead != -1 && lookahead != '\n'){
            throw new ParseError();
        }

        return value;
    }

    private String exp() throws IOException, ParseError{
        skipSpaces();
        if(lookahead == '(' || isLetter(lookahead)){
            String termResult = term();
            // exp2 needs the left operand for the division operator
            // if no division operator is detected, it will just return the term result
            return exp2(termResult);
        }
        throw new ParseError();
    }

    private String exp2(String leftOperand) throws IOException, ParseError{
        skipSpaces();
        if(lookahead == '/'){
            consume(lookahead);
            return operatorDivision(leftOperand, exp());
        }
        else if(lookahead == ')' || lookahead == -1 || lookahead == '\n'){
            return leftOperand;
        }
        throw new ParseError();
    }

    private String term() throws IOException, ParseError{
        skipSpaces();
        if(lookahead == '(' || isLetter(lookahead)){
            String factorResult = factor();
            // same logic as exp2
            return term2(factorResult);
        }
        throw new ParseError();
    }

    private String term2(String leftOperand) throws IOException, ParseError{
        skipSpaces();
        if(lookahead == '*'){
            consume(lookahead);
            if(lookahead != '*'){
                throw new ParseError();
            }
            consume(lookahead);
            // ** detected
            // here we can directly evaluate the result of the ** operator to ensure the left associativity
            String result = operatorStars(leftOperand, factor());
            return term2(result);
        }
        else if(lookahead == '/' || lookahead == ')' || lookahead == -1 || lookahead == '\n'){
            return leftOperand;
        }
        throw new ParseError();
    }

    private String factor() throws IOException, ParseError{
        skipSpaces();
        if(isLetter(lookahead)){
            return str();
        }
        else if(lookahead == '('){
            consume(lookahead);
            String result = exp();
            
            skipSpaces();
            if(lookahead != ')'){
                throw new ParseError();
            }
            consume(lookahead);
            // do not return the parentheses, the priority has been given
            return result;
        }
        throw new ParseError();
    }

    private String str() throws IOException, ParseError{
        // no skip spaces here because spaces are not allowed in strings
        if(isLetter(lookahead)){
            return myChar() + str2();
        }
        throw new ParseError();
    }

    private String str2() throws IOException, ParseError{
        if(isLetter(lookahead)){
            return str();
        }
        else if(lookahead == '/' || lookahead == ')' || lookahead == -1 || lookahead == '\n' || lookahead == '\r' || lookahead == ' ' || lookahead == '\t'){
            return "";
        }
        // do not consume or check for double asterisk here, as it is handled in term2()
        // just check if the input of the Letters stop here or not
        else if (lookahead == '*'){
            return "";
        }
        throw new ParseError();
    }

    private String myChar() throws IOException, ParseError{
        if(isLetter(lookahead)){
            // could not cast to String directly because is an Object
            char ch = (char) lookahead;
            consume(lookahead);
            // return the character as a string
            return String.valueOf(ch);
        }
        throw new ParseError();
    }
}