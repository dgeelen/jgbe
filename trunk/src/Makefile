CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g)

all: $(CLASSFILES)

%.class: %.java
	javac -O $<

clean:
	rm -f *.class

