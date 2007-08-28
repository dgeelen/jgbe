CLASSDIR:=classes
DEPSDIR:=.deps

JPPFILES  :=$(shell ls *.jpp)
GJAVAFILES:=$(JPPFILES:.jpp=.java)
AJAVAFILES:=$(shell ls *.java) $(GJAVAFILES)
AJAVAFILES:=$(shell echo $(AJAVAFILES) | sort | uniq)
CLASSFILES:=$(AJAVAFILES:%.java=$(CLASSDIR)/%.class)
BOOTROM   :=$(shell find -iname boot.rom | head -1)

ifeq ($(CLASSPATH),)
	CLASSPATH:=.
endif

CLASSPATH:=$(CLASSPATH):/usr/share/bcel/lib/bcel.jar

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

applet: jar
	keytool -delete -alias signFiles -keystore jgbestore -keypass kpi135 -storepass ab987c || true
	keytool -genkey -alias signFiles -keystore jgbestore -keypass kpi135 -dname "cn=JGBE" -storepass ab987c
	jarsigner -keystore jgbestore -storepass ab987c -keypass kpi135 -signedjar jar/sjgbe.jar jar/jgbe.jar signFiles

fun: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart

silence: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nosound

run: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -debug

debug: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nosound -debug

link: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -nosound

tester: all
	cd $(CLASSDIR) && java TestSuite ~/gbroms/Turtles\ 3.zip

rndCartridge: rndCartridge.cpp
	g++ -O3 rndCartridge.cpp -o rndCartridge

%.java: %.jpp
	@echo "[jpp  -> java ] $*"
	@cat Autogen.txt > $@
	@gcc -MD -E $(PPFLAGS) -x c $< | grep -v "^#" | sed "s:\;[ 	]*case:\;\\n	case:g" >> $@
	@sed -i "s=\.o:=\.java:=g" $*.d
# next line from http://make.paulandlesley.org/autodep.html
	@cp $*.d $*.dd; \
	  sed -e 's/#.*//' -e 's/^[^:]*: *//' -e 's/ *\\$$//' \
	  -e '/^$$/ d' -e 's/$$/ :/' < $*.dd >> $*.d; \
	  rm -f $*.dd
	@mv $*.d $(DEPSDIR)/

jarrun: jar
	cd jar && java -jar jgbe.jar -lastcart

jar: all
	cd classes && jar cmf ../MANIFEST.MF.in jgbe.jar *.class icon.gif instrs.txt VeraMono.ttf $(BOOTROM)
	mkdir -p jar
	cp classes/jgbe.jar jar

jarzip: all
	mkdir -p classes/META-INF
	cp MANIFEST.MF.in classes/META-INF/MANIFEST.MF
	cd classes && zip -r jgbe.zip META-INF *.class icon.gif instrs.txt VeraMono.ttf
	mkdir -p jar
	cp classes/jgbe.zip jar/jgbe.jar

clean:
	rm -f $(CLASSDIR)/*.class
	rm -f $(DEPSDIR)/*.d
	rm -f jar/jgbe.jar
	rm -f classes/jgbe.zip
	rm -f svnrev.inc
	touch $(JPPFILES)

cleaner: clean
	rm -f *.class
	rm -f $(DEPSDIR)/*
	rm -f $(GJAVAFILES)
	rm -f Makefile.inc

version:
	@(\
	OLDVER=`cat svnrev.inc || true`; \
	NEWVER="#define JGBE_VERSION_STRING \"`svnversion`\""; \
	if [ "$${OLDVER}" == "$${NEWVER}" ] ; then \
		true; \
	else \
		@echo [version] updating...; \
		echo $${NEWVER} > svnrev.inc; \
	fi \
	)

-include $(JPPFILES:%.jpp=$(DEPSDIR)/%.d)
