public class Emulator {
  public static final void main( String[] args ) {
    CPU cpu = new CPU("Pokemon Blue.gb");
    TestSuite t = new TestSuite(cpu);

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