build:
	javac MyBot.java

run: build
	java MyBot

clean:
	rm -rf *.class *.hlt *.log GPixelBot/*.class
	rm -rf HaliteParty/*.class


