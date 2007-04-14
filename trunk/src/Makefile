JPPFILES:=$(shell ls *.jpp)
JAVAFILES:=$(shell ls *.jpp | sed s:\.jpp:\.java:g)
CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g)
CLASSFILES+=$(shell ls *.jpp | sed s:\.jpp:\.java:g | sed s:\.java:\.class:g)

all: $(JAVAFILES) swinggui.class $(CLASSFILES)

%.class: %.java
	javac -O $<

%.java: %.jpp
	diff -u $@ .$@.bpp
	gcc -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" > $@
	cp $@ .$@.bpp

CPU.java: CPU.inc

clean:
	rm -f $(CLASSFILES)
	touch - $(JPPFILES)

