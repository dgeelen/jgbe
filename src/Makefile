J2MEPATH:=/opt/sun-j2me-bin-2.2
CLASSDIR:=classes
JARBUILDDIR:=jar_build_dir
#JMGBECLASSPATH=$(J2MEPATH)/lib/cldcapi11.jar:$(CLASSDIR):$(J2MEPATH)/lib/midpapi20.jar
CLASSPATH:=.:$(shell find $(J2MEPATH) -iname '*.jar' -print0 | tr '\000' : | sed s/:$$//):$(CLASSDIR)
BOOTCLASSPATH:=$(J2MEPATH)/lib/midpapi20.jar
PREVERIFY_OPTS:=-classpath $(CLASSPATH)
#HAX!
# VM_DEST:=-target 1.4.2
# VM_SOURCE:=-source 1.4
#VM_DEST:=-target 1.5
#VM_SOURCE:=-source 1.5
JAVAC_OPTS:=$(PREVERIFY_OPTS) -bootclasspath $(BOOTCLASSPATH) $(VM_DEST) $(VM_SOURCE) -Xlint
JAVAC_OPTS:=$(VM_DEST) $(VM_SOURCE) -Xlint -O -d $(CLASSDIR)

JPPFILES  :=$(shell ls *.jpp)
JAVAFILES :=$(shell ls *.jpp | sed s:\.jpp:\.java:g)
CLASSFILES:=$(shell ls *.java | sed -e s:\.java:\.class:g -e s:.*:$(CLASSDIR)/\\0:)
CLASSFILES+=$(shell ls *.jpp | sed s:\.jpp:\.java:g | sed -e s:\.java:\.class:g -e s:.*:$(CLASSDIR)/\\0:)
CLASSFILES:=$(shell echo $(CLASSFILES) | sort | uniq)

all: .Global.dummy $(JAVAFILES) $(CLASSFILES)


# use ROMNAME='/what/ever/file.gbc' make [run|fun|silence]
#ROMNAME='/home/simon/gbroms/Bionic Commando.zip'
ROMNAME='/home/simon/gbroms/Turtles 3.zip'
#ROMNAME='/home/simon/gbroms/Metal Slug.zip'
#ROMNAME='/home/simon/gbroms/Double Dragon 3.zip'
#ROMNAME='/home/simon/gbroms/Pokemon Yellow.zip'
#ROMNAME='/home/simon/gbroms/Pokemon Diamond.zip'
#ROMNAME='/home/simon/gbroms/Pokemon Blue.zip'
#ROMNAME=../../roms/Pokemon\ -\ Yellow\ Version\ \(UE\)\ \[C\]\[\!\].gbc
#ROMNAME=../../roms/../roms/Super\ Mario\ Land\ 2\ -\ 6\ Golden\ Coins\ \(UE\)\ \(V1.0\)\ \[\!\].gb
#ROMNAME=../../roms/Castelian\ \(U\).gb
#ROMNAME=../../roms/Teenage\ Mutant\ Ninja\ Turtles\ III\ -\ Radical\ Rescue\ \(U\).gb
#ROMNAME=../../roms/Double\ Dragon\ 3.zip

fun: all
	cd $(CLASSDIR) && aoss java swinggui $(ROMNAME) -nodebug

silence: all
	cd $(CLASSDIR) && aoss java swinggui $(ROMNAME) -nodebug -nosound

run: all
	cd $(CLASSDIR) && aoss java swinggui $(ROMNAME)

debug: all
	cd $(CLASSDIR) && aoss java swinggui $(ROMNAME) -nosound

rndCartridge: rndCartridge.cpp
	g++ -O3 rndCartridge.cpp -o rndCartridge


$(CLASSDIR)/%.class: %.java
	javac $(JAVAC_OPTS) $<

# $(CLASSDIR)/%.class: %.java
# 	javac $(JAVAC_OPTS) $<

%.java: %.jpp
	cat Autogen.txt > $@
	gcc -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" >> $@

.Global.dummy: Global.inc
	touch .Global.dummy
	touch *.jpp

clean:
	rm -f classes/*.class
	touch $(JPPFILES)


# JPPFILES:=$(shell ls *.jpp)
# JAVAFILES:=$(shell ls *.jpp | sed s:\.jpp:\.java:g | grep -v jmgbe)
# CLASSFILES:=$(shell ls *.java | sed s:\.java:\.class:g | grep -v jmgbe)
# CLASSFILES+=$(shell ls *.jpp | sed s:\.jpp:\.java:g | sed s:\.java:\.class:g | grep -v jmgbe)
# J2MEPATH=/opt/sun-j2me-bin-2.2
# CLASSDIR=classes
# JARBUILDDIR=jar_build_dir
# JMGBECLASSPATH=$(J2MEPATH)/lib/cldcapi11.jar:$(CLASSDIR):$(J2MEPATH)/lib/midpapi20.jar
# #JMGBECLASSPATH=$(shell find $(J2MEPATH) -iname '*.jar' -print0 | tr '\000' : | sed s/:$$//)
# JMGBEBOOTCLASSPATH=$(J2MEPATH)/lib/midpapi20.jar
# JMGBE_PREVERIFY_OPTS=-classpath $(JMGBECLASSPATH)
#
# #HAX!
# VM_DEST=-target 1.4.2
# VM_SOURCE=-source 1.4
# JMGBE_JAVAC_OPTS=$(JMGBE_PREVERIFY_OPTS) -bootclasspath $(JMGBEBOOTCLASSPATH) $(VM_DEST) $(VM_SOURCE) -Xlint
#
#
# all: .Global.dummy $(JAVAFILES) $(CLASSDIR)/swinggui.class $(CLASSFILES)
#
# rndCartridge: rndCartridge.cpp
# 	g++ -O3 rndCartridge.cpp -o rndCartridge
#
# run: all
# 	cd $(CLASSDIR) && aoss java swinggui ../../roms/Teenage\ Mutant\ Ninja\ Turtles\ III\ -\ Radical\ Rescue\ \(U\).gb
#
# %.class: $(CLASSDIR)/%.class
# 	true
#
# $(CLASSDIR)/%.class: %.java
# 	#javac $(JMGBE_JAVAC_OPTS) $(VM_DEST) $(VM_SOURCE) -O -d $(CLASSDIR) $<
# 	javac $(JMGBE_JAVAC_OPTS) $(VM_DEST) $(VM_SOURCE) -O -d $(CLASSDIR) $<
#
# $(CLASSDIR)/jmgbe.class: $(CLASSDIR)/AudioController.class $(CLASSDIR)/Bios.class $(CLASSDIR)/Cartridge.class $(CLASSDIR)/CPU.class $(CLASSDIR)/Tables.class $(CLASSDIR)/VideoController.class $(CLASSDIR)/FHandler.class $(CLASSDIR)/ShTables.class
# 	javac $(JMGBE_JAVAC_OPTS) $(VM_DEST) $(VM_SOURCE) -O -d $(CLASSDIR) jmgbe.java
#
# %.java: %.jpp
# 	cat Autogen.txt > $@
# 	gcc -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" >> $@
#
# .Global.dummy: Global.inc
# 	touch .Global.dummy
# 	touch *.jpp
#
# jmgbe.java: .Global.dummy
#
# jmgberun: jmgbe
# 	$(J2MEPATH)/bin/emulator -Xdescriptor:jmgbe.jad -Xheapsize:4M
#
# jmgbe: jmgbe.jar jmgbe.jad
#
# MANIFEST.MF: MANIFEST.MF.in
# 	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < MANIFEST.MF.in > MANIFEST.MF
#
# jmgbe.jad: jmgbe.jad.in jmgbe.jar
# 	sed "s/.*MIDlet-Jar-Size:.*/MIDlet-Jar-Size: `ls -l jmgbe.jar | gawk '{print $$5}'`/" < jmgbe.jad.in > jmgbe.jad
#
# #T3h fugly :(
# jmgbe.jar: jmgbepreverified MANIFEST.MF
# 	rm -rf $(JARBUILDDIR) && mkdir -p $(JARBUILDDIR)
# 	cp MANIFEST.MF $(JARBUILDDIR)
# 	ln -s ../roms $(JARBUILDDIR)
# 	ln -s ../boot.rom $(JARBUILDDIR)
# 	mv $(CLASSDIR)/output/*.class $(JARBUILDDIR)
# 	cd $(JARBUILDDIR) && jar -cfm jmgbe.jar MANIFEST.MF jmgbe.class AudioController.class AudioController\$$SoundRegister.class Bios.class Cartridge.class CPU.class ShTables.class Tables.class VideoController.class FHandler.class boot.rom roms/turtles.gb && mv jmgbe.jar .. && cd ..
# 	rm -rf $(JARBUILDDIR)
#
# jmgbepreverified: $(CLASSDIR)/jmgbe.class
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) jmgbe
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) AudioController
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) AudioController\$$SoundRegister
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Bios
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Cartridge
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) CPU
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) ShTables
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) Tables
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) VideoController
# 	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) FHandler
# #	cd $(CLASSDIR) && $(J2MEPATH)/bin/preverify $(JMGBE_PREVERIFY_OPTS) FHandler\$$1
#
# clean:
# 	rm -f classes/*.class
# 	touch $(JPPFILES)
#
