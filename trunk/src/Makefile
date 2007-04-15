JPPFILES:=$(shell ls *.jpp)
JAVAFILES:=$(shell ls *.jpp | sed s:\.jpp:\.java:g | grep -v jmgbe)
CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g | grep -v jmgbe)
CLASSFILES+=$(shell ls *.jpp | sed s:\.jpp:\.java:g | sed s:\.java:\.class:g | grep -v jmgbe)
J2MEPATH=/opt/sun-j2me-bin-2.2
JMGBECLASSPATH=$(J2MEPATH)/lib/cldcapi11.jar:$(J2MEPATH)/lib/midpapi20.jar
JMGBEBOOTCLASSPATH=API=$(J2MEPATH)/lib/midpapi20.jar
JMGBE_PREVERIFY_OPTS=-classpath $(JMGBECLASSPATH)
JMGBE_JAVAC_OPTS=$(JMGBE_PREVERIFY_OPTS) -bootclasspath $(JMGBEBOOTCLASSPATH)


all: $(JAVAFILES) swinggui.class $(CLASSFILES)

%.class: %.java
	javac -O $<

%.java: %.jpp
	diff -u $@ .$@.bpp
	gcc -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" > $@
	cp $@ .$@.bpp

CPU.java: CPU.inc

jmgberun: jmgbe
	$(J2MEPATH)/bin/emulator -Xdescriptor:jmgbe.jad

jmgbe: jmgbejar jmgbejad

manifest.mf: MANIFEST.MF.in
	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < MANIFEST.MF.in > MANIFEST.MF

jmgbejad: jmgbe.jad.in
	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < jmgbe.jad.in > jmgbe.jad

jmgbejar: output/jmgbe.class manifest.mf
	jar -cfm jmgbe.jar MANIFEST.MF output/jmgbe.class

jmgbepreverified: jmgbe.class
	$(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) jmgbe

jmgbe.class: jmgbe.java
	javac $(JMGBE_JAVAC_OPTS) jmgbe.java

clean:
	rm -f *.class
	touch $(JPPFILES)

