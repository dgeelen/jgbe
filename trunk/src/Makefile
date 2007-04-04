all: emulator
emulator: TestSuite
	javac emulator.java

TestSuite: CPU TestSuite.java
	javac TestSuite.java

Cardridge: Cardridge.java
	javac Cardridge.java

CPU: Cardridge
	javac CPU.java

clean:
	rm -f Cardridge.class CPU.class main.class TestSuite.java

