all: CPU
Cardridge: cpu/Cardridge.java
	cd cpu ; javac Cardridge.java 

CPU: Cardridge
	cd cpu ; javac CPU.java 
clean:
	rm -f cpu/Cardridge.class cpu/CPU.class

