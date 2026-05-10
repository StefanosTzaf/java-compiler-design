all: compile

compile:
	java -jar jtb133di.jar minijava.jj
	java -jar javacc5.jar minijava-jtb.jj
	javac Main.java SymbolTableVisitor.java GlobalSymbolTable.java

clean:
	rm -rf syntaxtree visitor
	rm -f *.class
	rm -f jtb.out.jj minijava-jtb.jj
	rm -f MiniJavaParser.java MiniJavaParserConstants.java \
	      MiniJavaParserTokenManager.java Token.java \
	      TokenMgrError.java ParseException.java JavaCharStream.java