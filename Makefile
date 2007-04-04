all: main
main:	TestSuite
	cd cpu ; javac main.java
TestSuite: CPU cpu/TestSuite.java
	cd cpu ; javac TestSuite.java

Cardridge: cpu/Cardridge.java
	cd cpu ; javac Cardridge.java

CPU: Cardridge
	cd cpu ; javac CPU.java

clean:
	rm -f cpu/Cardridge.class cpu/CPU.class

