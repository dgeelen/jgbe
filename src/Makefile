CLASSDIR:=classes
DEPSDIR:=.deps

JPPFILES  :=$(shell ls *.jpp)
GJAVAFILES:=$(JPPFILES:.jpp=.java)
AJAVAFILES:=$(shell ls *.java) $(GJAVAFILES)
AJAVAFILES:=$(shell echo $(AJAVAFILES) | sort | uniq)
CLASSFILES:=$(AJAVAFILES:%.java=$(CLASSDIR)/%.class)

AOSS:=$(shell which aoss 2> /dev/null)

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
preprocess: $(GJAVAFILES)

fun: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nodebug

silence: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nodebug -nosound

run: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart

debug: all
	cd $(CLASSDIR) && $(AOSS) java swinggui -lastcart -nosound

rcrun: all
#	cd $(CLASSDIR) && java -classpath ../../../bcel-5.2/bcel-5.2.jar:.  org.apache.bcel.util.JavaWrapper swinggui -lastcart -nosound -nodebug
#	cd $(CLASSDIR) && java -classpath ../../../bcel-5.2/bcel-5.2.jar:.  org.apache.bcel.util.JavaWrapper swinggui -lastcart -nosound
#	cd $(CLASSDIR) && java -classpath ../../../bcel-5.2/bcel-5.2.jar:.  org.apache.bcel.util.JavaWrapper CPURecompiler
	cd $(CLASSDIR) && java -classpath ../../../bcel-5.2/bcel-5.2.jar:.  org.apache.bcel.util.JavaWrapper TestSuite ~/gbroms/Turtles\ 3.zip

rndCartridge: rndCartridge.cpp
	g++ -O3 rndCartridge.cpp -o rndCartridge

%.java: %.jpp
	@echo "[jpp  -> java ] $*"
	@cat Autogen.txt > $@
	@gcc -MD -E $(PPFLAGS) -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" >> $@
	@sed -i "s=\.o:=\.java:=g" $*.d
# next line from http://make.paulandlesley.org/autodep.html
	@cp $*.d $*.dd; \
	  sed -e 's/#.*//' -e 's/^[^:]*: *//' -e 's/ *\\$$//' \
	  -e '/^$$/ d' -e 's/$$/ :/' < $*.dd >> $*.d; \
	  rm -f $*.dd
	@mv $*.d $(DEPSDIR)/

clean:
	rm -f $(CLASSDIR)/*.class
	rm -f $(DEPSDIR)/*.d
	touch $(JPPFILES)

cleaner: clean
	rm -f *.class
	rm -f $(DEPSDIR)/*
	rm -f $(GJAVAFILES)
	rm -f Makefile.inc

-include $(JPPFILES:%.jpp=$(DEPSDIR)/%.d)
