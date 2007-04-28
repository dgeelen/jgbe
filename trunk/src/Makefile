J2MEPATH:=/opt/sun-j2me-bin-2.2
CLASSDIR:=classes
DEPSDIR:=.deps
JARBUILDDIR:=jar_build_dir
#JMGBECLASSPATH=$(J2MEPATH)/lib/cldcapi11.jar:$(CLASSDIR):$(J2MEPATH)/lib/midpapi20.jar
CLASSPATH:=.:$(shell find $(J2MEPATH) -iname '*.jar' -print0 | tr '\000' : | sed s/:$$//):$(CLASSDIR)
BOOTCLASSPATH:=$(J2MEPATH)/lib/midpapi20.jar
PREVERIFY_OPTS:=-classpath $(CLASSPATH)
#HAX!
# VM_DEST:=-target 1.4.2
# VM_SOURCE:=-source 1.4
VM_DEST:=-target 1.5
VM_SOURCE:=-source 1.5
JAVAC_OPTS:=$(PREVERIFY_OPTS) -bootclasspath $(BOOTCLASSPATH) $(VM_DEST) $(VM_SOURCE) -Xlint
JAVAC_OPTS:=$(VM_DEST) $(VM_SOURCE) -Xlint -O -d $(CLASSDIR)

JPPFILES  :=$(shell ls *.jpp)
GJAVAFILES:=$(JPPFILES:.jpp=.java)
AJAVAFILES:=$(shell ls *.java) $(GJAVAFILES)
AJAVAFILES:=$(shell echo $(AJAVAFILES) | sort | uniq)

CLASSFILES:=$(AJAVAFILES:%.java=$(CLASSDIR)/%.class)
#swinggui.class first class file to compile to speed up building
CLASSFILES:=$(CLASSDIR)/swinggui.class $(CLASSFILES)

all: preprocess $(CLASSFILES)

preprocess: $(GJAVAFILES)

fun: all
	cd $(CLASSDIR) && aoss java -ea swinggui -lastcart -nodebug

silence: all
	cd $(CLASSDIR) && aoss java -ea swinggui -lastcart -nodebug -nosound

run: all
	cd $(CLASSDIR) && aoss java -ea swinggui -lastcart

debug: all
	cd $(CLASSDIR) && aoss java -ea swinggui -lastcart -nosound

rndCartridge: rndCartridge.cpp
	g++ -O3 rndCartridge.cpp -o rndCartridge

$(CLASSDIR)/%.class: %.java
	@echo "[java -> class] $*"
	@javac $(JAVAC_OPTS) $<

%.java: %.jpp
	@echo "[jpp  -> java ] $*"
	@cat Autogen.txt > $@
	@gcc -MD -E -x c $< | grep -v "^#" | sed "s:cycles\;[ 	]*case:cycles\;\\n	case:g" >> $@
	@sed -i "s=\.o:=\.java:=g" $*.d
	@mv $*.d $(DEPSDIR)/

clean:
	rm -f $(CLASSDIR)/*.class
	rm -f $(DEPSDIR)/*.d
	touch $(JPPFILES)

cleaner: clean
	rm -f *.class
	rm -f $(DEPSDIR)/*
	rm -f $(GJAVAFILES)

-include $(JPPFILES:%.jpp=$(DEPSDIR)/%.d)