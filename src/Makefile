all: Emulator
Emulator: TestSuite
	javac Emulator.java

TestSuite: CPU TestSuite.java
	javac TestSuite.java

Cartridge: Cartridge.java
	javac Cartridge.java

CPU: Cartridge
	javac CPU.java

clean:
	rm -f Cartridge.class CPU.class main.class TestSuite.java

