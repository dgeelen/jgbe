public class Emulator {
  public static final void main( String[] args ) {
    CPU cpu = new CPU("Pokemon Blue.gb");
    TestSuite t = new TestSuite(cpu);

    if(t.diagnose(true)==0) {
      cpu.reset();
      while(cpu.exception()==0 && cpu.cycles()<16){
        cpu.nextinstruction();
      }
    }
  }
}