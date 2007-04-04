all: Emulator.class

Emulator.class: TestSuite.class

TestSuite.class: CPU.class

CPU.class: Cartridge.class

%.class: %.java
	javac $<

clean:
	rm -f *.class

