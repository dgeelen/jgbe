JPPFILES:=$(shell ls *.jpp)
JAVAFILES=$(shell echo $(JPPFILES) | sed s:\.jpp:\.java:g)
CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g)
CLASSFILES+=$(shell echo $(JAVAFILES) | sed s:\.java:\.class:g)

all: $(JAVAFILES) swinggui.class $(CLASSFILES)

%.class: %.java
	javac -O $<

%.java: %.jpp
	diff -u $@ .$@.bpp
	gcc -E -x c $< | grep -v "^#" > $@
	cp $@ .$@.bpp

clean:
	rm -f $(CLASSFILES)
	touch - $(JPPFILES)

