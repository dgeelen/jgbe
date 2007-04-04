public class Emulator {
  public static final void main( String[] args ) {
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
          while(cpu.exception()==0){
    /*        int cycles = cpu.nextinstruction();
            if(cycles == -1) {
              panic();
            } else {
              sleep(cycles);
            }*/
            cpu.nextinstruction();
          }
        }
      }
    }
  }
}