all: CPU
Cardridge: cpu/Cardridge.java
	cd cpu ; javac Cardridge.java ; cd ..
CPU: Cardridge
	cd cpu ; javac CPU.java ; cd ..
clean:
	rm -f Cardridge.class cpu/CPU.class

