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

    public boolean eval() throws IOException, ParseError{
        boolean value = exp();

        if(lookahead != -1 && lookahead != '\n'){
            throw new ParseError();
        }

        return value;
    }

    private boolean exp() throws IOException, ParseError{
        if(lookahead == '(' || isLetter(lookahead)){
            return term() && exp2();
        }
        throw new ParseError();
    }

    private boolean exp2() throws IOException, ParseError{
        if(lookahead == '/'){
            consume(lookahead);
            return exp();
        }
        else if(lookahead == ')' || lookahead == -1 || lookahead == '\n'){
            return true;
        }
        throw new ParseError();
    }

    private boolean term() throws IOException, ParseError{
        if(lookahead == '(' || isLetter(lookahead)){
            return factor() && term2();
        }
        throw new ParseError();
    }

    private boolean term2() throws IOException, ParseError{
        if(lookahead == '*'){
            consume(lookahead);
            if(lookahead != '*'){
                throw new ParseError();
            }
            consume(lookahead);
            // ** detected
            return factor() && term2();
        }
        else if(lookahead == '/' || lookahead == ')' || lookahead == -1 || lookahead == '\n'){
            return true;
        }
        throw new ParseError();
    }

    private boolean factor() throws IOException, ParseError{
        if(isLetter(lookahead)){
            return str();
        }
        else if(lookahead == '('){
            consume(lookahead);
            boolean result = exp();
            if(lookahead != ')'){
                throw new ParseError();
            }
            consume(lookahead);
            return result;
        }
        throw new ParseError();
    }

    private boolean str() throws IOException, ParseError{
        if(isLetter(lookahead)){
            return myChar() && str2();
        }
        throw new ParseError();
    }

    private boolean str2() throws IOException, ParseError{
        if(isLetter(lookahead)){
            return str();
        }
        else if(lookahead == '/' || lookahead == ')' || lookahead == -1 || lookahead == '\n'){
            return true;
        }
        // do not consume or check for double asterisk here, as it is handled in term2()
        // just check if the input of the Letters stop here or not
        else if (lookahead == '*'){
            return true;
        }
        throw new ParseError();
    }

    private boolean myChar() throws IOException, ParseError{
        if(isLetter(lookahead)){
            consume(lookahead);
            return true;
        }
        throw new ParseError();
    }
}