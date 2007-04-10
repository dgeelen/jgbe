CFILES:=$(shell ls *.java | sed s:\.java:\.class:g)

all: $(CFILES)

%.class: %.java
	javac -O $<

clean:
	rm -f *.class

