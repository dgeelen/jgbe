public class Emulator {
	public static final void main( String[] args ) {
		boolean fulldebug = true;
		for (int i = 0; i < args.length; ++i) {
			System.out.println("args[" + i + "] = " + args[i]);
			if (args[i].equals("--no-full-debug"))
				fulldebug = false;
			if (args[i].equals("--full-debug"))
				fulldebug = true;
		}
	
		Cartridge cartridge = new Cartridge("Pokemon Blue.gb");
		if(cartridge.getError()!=null) {
			System.out.println("ERROR: "+cartridge.getError());
		}
		else {
			System.out.println("Succesfully loaded ROM :)");
			CPU cpu = new CPU(cartridge);
			TestSuite t = new TestSuite(cpu);
			if(true){
				if(t.diagnose(true)==0) {
					cpu.reset();
					int x = 1;
					while(x > 0){
						if (fulldebug) cpu.printCPUstatus();
						cpu.nextinstruction();
						if (cpu.exception()!=0) {
							if (!fulldebug) cpu.printCPUstatus();
							++(cpu.PC); // hope its a single byte instruction? ask disassembler how big the instruction is?
							--x;
						}
					}
				}
			}
		}
	}
}