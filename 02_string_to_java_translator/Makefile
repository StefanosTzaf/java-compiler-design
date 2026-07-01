all: compile

compile:
	jflex scanner.flex
	java -jar java-cup-11b.jar -interface -parser Parser parser.cup
	javac -cp java-cup-11b-runtime.jar:. *.java

clean:
	rm -f *.class *~ Scanner.java Parser.java sym.java ParserItf.java