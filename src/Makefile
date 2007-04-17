JPPFILES:=$(shell ls *.jpp)
JAVAFILES:=$(shell ls *.jpp | sed s:\.jpp:\.java:g | grep -v jmgbe)
CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g | grep -v jmgbe)
CLASSFILES+=$(shell ls *.jpp | sed s:\.jpp:\.java:g | sed s:\.java:\.class:g | grep -v jmgbe)
J2MEPATH=/opt/sun-j2me-bin-2.2
CLASSDIR=classes
JARBUILDDIR=jar_build_dir
JMGBECLASSPATH=$(J2MEPATH)/lib/cldcapi11.jar:$(J2MEPATH)/lib/midpapi20.jar:$(CLASSDIR)
#JMGBECLASSPATH=$(shell find $(J2MEPATH) -iname '*.jar' -print0 | tr '\000' : | sed s/:$$//)
JMGBEBOOTCLASSPATH=$(J2MEPATH)/lib/midpapi20.jar
JMGBE_PREVERIFY_OPTS=-classpath $(JMGBECLASSPATH)

#HAX!
VM_DEST=-target 1.4.2
VM_SOURCE=-source 1.4
JMGBE_JAVAC_OPTS=$(JMGBE_PREVERIFY_OPTS) -bootclasspath $(JMGBEBOOTCLASSPATH) $(VM_DEST) $(VM_SOURCE) -Xlint


all: .Global.dummy $(JAVAFILES) $(CLASSDIR)/swinggui.class $(CLASSFILES)

rndCartridge: rndCartridge.cpp
	g++ -O3 rndCartridge.cpp -o rndCartridge

run: all
	cd $(CLASSDIR) && aoss java swinggui ../../roms/Teenage\ Mutant\ Ninja\ Turtles\ III\ -\ Radical\ Rescue\ \(U\).gb

%.class: $(CLASSDIR)/%.class
	true

$(CLASSDIR)/%.class: %.java
	javac  $(VM_DEST) $(VM_SOURCE) -O -d $(CLASSDIR) $<

$(CLASSDIR)/jmgbe.class: $(CLASSDIR)/AudioController.class $(CLASSDIR)/Bios.class $(CLASSDIR)/Cartridge.class $(CLASSDIR)/CPU.class $(CLASSDIR)/Tables.class $(CLASSDIR)/VideoController.class $(CLASSDIR)/FHandler.class $(CLASSDIR)/ShTables.class
	javac $(JMGBE_JAVAC_OPTS) $(VM_DEST) $(VM_SOURCE) -O -d $(CLASSDIR) jmgbe.java

%.java: %.jpp
	cat Autogen.txt > $@
	gcc -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" >> $@

.Global.dummy: Global.inc
	touch .Global.dummy
	touch *.jpp

jmgbe.java: .Global.dummy

jmgberun: jmgbe
	$(J2MEPATH)/bin/emulator -Xdescriptor:jmgbe.jad

jmgbe: jmgbejar jmgbejad

manifest.mf: MANIFEST.MF.in
	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < MANIFEST.MF.in > MANIFEST.MF

jmgbejad: jmgbe.jad.in
	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < jmgbe.jad.in > jmgbe.jad

#T3h fugly :(
jmgbejar: jmgbepreverified manifest.mf
	rm -rf $(JARBUILDDIR) && mkdir -p $(JARBUILDDIR)
	cp MANIFEST.MF $(JARBUILDDIR)
	ln -s ../roms $(JARBUILDDIR)
	ln -s ../boot.rom $(JARBUILDDIR)
	mv $(CLASSDIR)/output/*.class $(JARBUILDDIR)
	cd $(JARBUILDDIR) && jar -cfm jmgbe.jar MANIFEST.MF jmgbe.class AudioController.class Bios.class Cartridge.class CPU.class ShTables.class Tables.class VideoController.class FHandler.class boot.rom roms/turtles.gb && mv jmgbe.jar .. && cd ..
	rm -rf $(JARBUILDDIR)

jmgbepreverified: $(CLASSDIR)/jmgbe.class
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) jmgbe
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) AudioController
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Bios
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Cartridge
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) CPU
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) ShTables
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Tables
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) VideoController
	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) FHandler

clean:
	rm -f classes/*.class
	touch $(JPPFILES)

