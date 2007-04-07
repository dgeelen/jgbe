//------------------------------------------
// IO.java, test methods of getting user
//    input.
//------------------------------------------

public class Emulator {
    public static String readLn() {
        //set aside memory for the keyboard input
        byte [] byteArray = new byte[300];
        
        //things which could fail should be enclosed
        // in a try/catch statement
        try {
            // read stdin into the byte array.
            System.in.read(byteArray);
        } catch (Exception e) {
            // if there is a failure, print an error
            e.printStackTrace();
        }
        
        //make a String from the array of bytes
        String result = new String(byteArray);

        //check for a return character, and only
        //keep the characters up to the return
        int newLineIndex = result.indexOf("\n");
        if (newLineIndex>-1)
            result = result.substring(0,newLineIndex);
        newLineIndex = result.indexOf("\r");
        if (newLineIndex>-1)
            result = result.substring(0,newLineIndex);
        
        return result;
    }

	
	
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
			Disassembler deasm = new Disassembler(cpu);
			if(true){
				if(t.diagnose(true)==0) {
					cpu.reset();
					int x = 10;
					String stopstr = "";
					while(x > 0){
						if (fulldebug && stopstr.equals("")) {
							cpu.printCPUstatus();
							//System.out.println(" > " + deasm.disassemble(cpu.PC));
							stopstr = readLn();
						}
						cpu.nextinstruction();
						if (!stopstr.equals("")) {
							String s = deasm.disassemble(cpu.PC);
							if (s.indexOf(stopstr) > -1)
								stopstr = "";
						}
						if (cpu.exception()!=0) {
							if (!fulldebug) cpu.printCPUstatus();
							String s = deasm.disassemble(cpu.PC);
							if (s.charAt( 6)=='$') ++(cpu.PC);
							if (s.charAt(10)=='$') ++(cpu.PC);
							if (s.charAt(14)=='$') ++(cpu.PC);
							--x;
							stopstr = "";
							fulldebug = true;
						}
					}
					System.out.println("Too many errors, aborting.");
					System.exit(-1); //FIXME: We should 'avoid System.exit() like the plague'
					System.out.println("You are *NOT* seeing this message");
				}
			}
		}
	}
}