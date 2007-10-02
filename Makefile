SRCDIR  :=./src
DEPSDIR :=./.deps
CLASSDIR:=./classes
JARDIR  :=./jar

JPPFILES  :=$(shell ls $(SRCDIR)/*.jpp)
GJAVAFILES:=$(JPPFILES:.jpp=.java)
AJAVAFILES:=$(shell ls $(SRCDIR)/*.java) $(GJAVAFILES)
AJAVAFILES:=$(shell echo $(AJAVAFILES) | sort | uniq)
CLASSFILES:=$(AJAVAFILES:$(SRCDIR)/%.java=$(CLASSDIR)/%.class)
BOOTROM   :=$(shell find -iname boot.rom | head -1)

ifeq ($(CLASSPATH),)
	CLASSPATH:=.
endif

CLASSPATH:=$(CLASSPATH):/usr/share/bcel/lib/bcel.jar
SRCPATH  :=$(SRCDIR)

AOSS:=$(shell which aoss 2> /dev/null)

-include Makefile.config
-include Makefile.inc

# compiler choose targets
help:
	@echo '**********************************************'
	@echo Use one of the following to choose a compiler:
	@echo - make javac
	@echo - make jikes
	@echo - make gcj
	@echo Use the following to display this message:
	@echo - make help
	@echo '**********************************************'

javac: clean
	@echo "-include Makefile.javac" > Makefile.inc

jikes: clean
	@echo "-include Makefile.jikes" > Makefile.inc

gcj: clean
	@echo "-include Makefile.gcj" > Makefile.inc

# common targets
preprocess: version $(GJAVAFILES)

applet: $(JARDIR)/sjgbe.jar

$(JARDIR)/sjgbe.jar: $(JARDIR)/jgbe.jar
	@echo [signing] applet
	@keytool -delete -alias signFiles -keystore jgbestore -keypass kpi135 -storepass ab987c > /dev/null || true
	@keytool -genkey -alias signFiles -keystore jgbestore -keypass kpi135 -dname "cn=JGBE" -storepass ab987c
	@jarsigner -keystore jgbestore -storepass ab987c -keypass kpi135 -signedjar jar/sjgbe.jar jar/jgbe.jar signFiles > /dev/null

fun: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart

silence: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nosound

run: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -debug

debug: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nosound -debug

oglfun: all
	cd $(CLASSDIR) && $(AOSS) java -Dsun.java2d.opengl=True swinggui -lastcart

# get PerfAnal.jar at
# http://java.sun.com/developer/technicalArticles/Programming/perfanal/PerfAnal.jar
profile: all
	cd $(CLASSDIR) && $(AOSS) java -agentlib:hprof=cpu=samples,depth=6,thread=y,interval=1 swinggui -lastcart -nosound
	java -jar PerfAnal.jar $(CLASSDIR)/java.hprof.txt

link: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -nosound

tester: all
	cd $(CLASSDIR) && java TestSuite ~/gbroms/Turtles\ 3.zip

rndCartridge: $(SRCDIR)/rndCartridge.cpp
	g++ -O3 $(SRCDIR)/rndCartridge.cpp -o rndCartridge

$(SRCDIR)/%.java: $(SRCDIR)/%.jpp
	@echo "[jpp  -> java ] $*"
	@cat $(SRCDIR)/Autogen.txt > $@
	@gcc -MD -MF $*.d -E $(PPFLAGS) -x c $< >> $@ || (rm -f $@ && false)
# remove comments (lines starting with #)
	@sed -i "s:^#.*$$::" $@
# split up cases to seperate lines
	@sed -i "s:\;[ 	]*case:\;\\n	case:g" $@
# replace ".o" with ".java"
	@sed -i "s=\.o:=\.java:=g" $*.d
# prepend $(SRCDIR)
	@sed -i "s=^[^:]*:=$(SRCDIR)/\0=" $*.d

# next line from http://make.paulandlesley.org/autodep.html
	@cp $*.d $*.dd; \
	  sed -e 's/#.*//' -e 's/^[^:]*: *//' -e 's/ *\\$$//' \
	  -e '/^$$/ d' -e 's/$$/ :/' < $*.dd >> $*.d; \
	  rm -f $*.dd
	@mkdir -p $(DEPSDIR)
	@mv $*.d $(DEPSDIR)/

jarrun: $(JARDIR)/jgbe.jar
	cd $(JARDIR) && java -jar jgbe.jar -lastcart

$(JARDIR)/jgbe.jar: $(CLASSFILES)
	@echo "[packing] jgbe.jar"
	@echo "Manifest-Version: 1.2" > $(CLASSDIR)/MANIFEST.MF.in
	@echo "Main-Class: swinggui" >> $(CLASSDIR)/MANIFEST.MF.in
	@cd $(CLASSDIR) && jar cmf MANIFEST.MF.in jgbe.jar *.class icon.gif VeraMono.ttf $(BOOTROM)
	@mkdir -p $(JARDIR)
	@mv $(CLASSDIR)/jgbe.jar $(JARDIR)/jgbe.jar

jar: $(JARDIR)/jgbe.jar

jarzip: $(CLASSFILES)
	@echo "[packing] jgbe.jar (zip) - BROKEN!!!"
	@mkdir -p $(CLASSDIR)/META-INF
	@echo -en "Manifest-Version: 1.2\r\n" > $(CLASSDIR)/META-INF/MANIFEST.MF
	@echo -en "Created-By: 1.5.0_12 (Sun Microsystems Inc.)\r\n" >> $(CLASSDIR)/META-INF/MANIFEST.MF
	@echo -en "Main-Class: swinggui\r\n" >> $(CLASSDIR)/META-INF/MANIFEST.MF
	@echo -en "\r\n" >> $(CLASSDIR)/META-INF/MANIFEST.MF
	@cd $(CLASSDIR) && zip -r jgbe.zip META-INF *.class icon.gif VeraMono.ttf $(BOOTROM)
	@mkdir -p $(JARDIR)
	@mv $(CLASSDIR)/jgbe.zip $(JARDIR)/jgbe.jar

clean:
	rm -f $(CLASSDIR)/*.class
	rm -f $(DEPSDIR)/*.d
	rm -f $(JARDIR)/jgbe.jar
	rm -f $(CLASSDIR)/jgbe.zip
	rm -f svnrev.inc
	touch $(JPPFILES)

cleaner: clean
	rm -f $(CLASSDIR)/*.class
	rm -f $(DEPSDIR)/*
	rm -f $(GJAVAFILES)
	rm -f Makefile.inc

$(SRCDIR)/svnrev.inc: version
version:
	@(\
	OLDVER=`cat $(SRCDIR)/svnrev.inc 2> /dev/null || true`; \
	NEWVER="#define JGBE_VERSION_STRING \"`sh svnversion.sh`\""; \
	if [ "$${OLDVER}" = "$${NEWVER}" ] ; then \
		true; \
	else \
		echo [version] updating...; \
		echo $${NEWVER} > $(SRCDIR)/svnrev.inc; \
	fi \
	)

-include $(JPPFILES:$(SRCDIR)/%.jpp=$(DEPSDIR)/%.d)
