public class CPU
  {
    private static final int CARRY8b    = 512;
    private static final int CARRY8b_SHR = 5;

    //FLAGS
    private static final int FLAG_REG = 5;
    private static final int ZF_Shift = 7;
    private static final int NF_Shift = ZF_Shift - 1;
    private static final int HC_Shift = NF_Shift - 1;
    private static final int CF_Shift = HC_Shift - 1;
    private static final int ZF_Mask  = 1 << ZF_Shift;
    private static final int NF_Mask  = 1 << NF_Shift;
    private static final int HC_Mask  = 1 << HC_Shift;
    private static final int CF_Mask  = 1 << CF_Shift;

    private int TotalInstrCount = 0;

    private int[] regs = new int[8]; //[A,B,C,D,E,F,H,L]
    private static final int A = 0;
    private static final int B = 1;
    private static final int C = 2;
    private static final int D = 3;
    private static final int E = 4;
    private static final int F = FLAG_REG;
    private static final int H = 6;
    private static final int L = 7;

    private int PC;

    //CPU Class variables
    private Cardridge cardridge;// = new Cardridge("Pokemon Blue.gb");
    private int lastException=0;

    public CPU(String filename) {
      cardridge = new Cardridge(filename);
      if(cardridge.getError()!=null) {
        System.out.println("ERROR: "+cardridge.getError());
      }
      else {
        System.out.println("Succesfully loaded ROM :)");
      }
      reset();
    }

    public void reset() {
    //TODO: Switch to bank 0
      PC = 0x100; //ROM Entry point on bank 0
      for(int i=0; i<8 ; ++i) regs[i]=0;
      TotalInstrCount=0;
    }

    private int cycles() {
      return TotalInstrCount;
    }

    private String disassembleinstruction() {
      int instr = cardridge.read(PC);
      String s = String.format("$%02x ", instr);
      // TODO take count of BC
      s+=Disassembler.disassemble(instr, false);;
      return s;
    }

    private void printCPUstatus()
    {
        String flags = "";
        flags += ((regs[FLAG_REG] & ZF_Mask) == ZF_Mask)?"Z ":"z ";
        flags += ((regs[FLAG_REG] & NF_Mask) == NF_Mask)?"N ":"n ";
        flags += ((regs[FLAG_REG] & HC_Mask) == HC_Mask)?"H ":"h ";
        flags += ((regs[FLAG_REG] & CF_Mask) == CF_Mask)?"C ":"c ";
        flags += ((regs[FLAG_REG] & (1 <<3)) == (1 <<3))?"1 ":"0 ";
        flags += ((regs[FLAG_REG] & (1 <<2)) == (1 <<2))?"1 ":"0 ";
        flags += ((regs[FLAG_REG] & (1 <<1)) == (1 <<1))?"1 ":"0 ";
        flags += ((regs[FLAG_REG] & (1 <<0)) == (1 <<0))?"1 ":"0 ";
        System.out.println("---CPU Status for cycle "+TotalInstrCount+"---");
        System.out.printf("A=$%02x\tB=$%02x\tC=$%02x\tD=$%02x\tE=$%02x\tF=$%02x\n", regs[A], regs[B], regs[C], regs[D], regs[E], regs[F]);
        System.out.printf("H=$%04x\tL=$04x\t\tflags="+flags+"\n",regs[H],regs[L]);
        System.out.printf("PC=$%2x\t%s\n", PC, disassembleinstruction());
    }

    private void inc8b(int reg_index)
    {
      // Clear & Set HC
      regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
      regs[FLAG_REG] = regs[FLAG_REG] | ((((regs[reg_index] & 0xF) + 1) & 0x10) << 1);

      //Update register
      regs[reg_index] = ( ++regs[reg_index] & 0xFF );

      // clear & set ZF
      regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
      regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index]==0 )?1:0 )<<ZF_Shift );

      // clear & set NF
      regs[FLAG_REG] = regs[FLAG_REG] & ~NF_Mask;
      }

    private void dec8b( int reg_index ) {
      // Clear & Set HC
      regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
      regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index] & 0xF )==0 )?HC_Mask:0 );

      //Update register
      regs[reg_index] = ( --regs[reg_index] & 0xFF );

      // clear & set ZF
      regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
      regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index]==0 )?1:0 )<<ZF_Shift );

      // clear & set NF
      regs[FLAG_REG] = regs[FLAG_REG] | NF_Mask;
      }

    private void inc16b() {}

    private void add8b(int dest, int src) {
      // Clear all flags
      regs[FLAG_REG] = regs[FLAG_REG] & 0x0f;
      // Set HC
      regs[FLAG_REG] = regs[FLAG_REG] | ((((src&0x0f)+(dest&0x0f))&0x10)>0?1:0);

      // Update register (part 1)
      regs[dest] = (regs[dest] + regs[src]);

      // set CF
      regs[FLAG_REG] = regs[FLAG_REG] | (regs[dest]>>8)<<CF_Shift;

      // Clamp register (part 2)
      regs[dest]&=0xFF;

      // set ZF
      regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[dest]==0 )?1:0 )<<ZF_Shift );
    }

    private void JPnn( ) {
      int i=cardridge.read(++PC);
      int j=cardridge.read(++PC);
      PC = i<<8|j;
      }

    private void JPccnn( boolean cc, int nn ) {
      if ( cc ) JPnn();
      }

    private void JRe( int e ) {
      PC += e;
      }

    private void JRcce( boolean cc, int e ) {
      if ( cc ) JRe( e );
      }

    private int fetch()
    {
        return cardridge.read(PC);
    }

    private boolean execute( int instr ) {
      System.out.printf("Executing instruction $%02x\n", instr);
      ++PC;  //FIXME: Is de PC niet ook een register in de CPU?
      switch ( instr ) {
        case 0x00:  // NOP
          break;
        case 0x01:  // LD BC,&0000
          // TODO
          break;
        case 0x02:  // LD (BC),A
          // TODO
          break;
        case 0x03:  // INC BC
          // TODO
          break;
        case 0x04:  // INC B
          inc8b( B );
          break;
        case 0x05:  // DEC B
          dec8b( B );
          break;
        case 0x81: // ADD  A,C
          add8b(A,C);
          break;
        case 0xc3: // JPNNNN
          JPnn();
          break;
        default:
          System.out.printf( "UNKNOWN INSTRUCTION: $%02x\n" , instr );
          return false;
        }
      ++TotalInstrCount;
      return true;
      }

      private boolean nextinstruction() {
        printCPUstatus();
        lastException = execute(fetch()) ? 0 : 1;
        return lastException==0;
      }

      private int exception() {
        return lastException;
      }

    private boolean dec8b_diag() {
      /***************************************************************************************************************
      * Test DEC_8b
      * Tests 0x01 - 1, 0x00 - 1, 0x10 - 1 for setting AND clearing of flags
      */
      boolean status=true;

      regs[A]=0x01;
      regs[FLAG_REG] = 0x00; // clear all flags
      dec8b( A );
      if ( regs[A]!=0x00 ) {
        System.out.println( "Error: 1 - 1 != 0x00" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) != ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x01->0x00 and ZF is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) == HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x01->0x00 and HC is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }
      regs[A]=0x01;
      regs[FLAG_REG] = 0xf0; // set all flags
      dec8b( A );
      if ( regs[A]!=0x00 ) {
        System.out.println( "Error: 1 - 1 != 0x00" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) != ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x01->0x00 and ZF is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) == HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x01->0x00 and HC is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }

      regs[A]=0;
      regs[FLAG_REG] = 0x00; // clear all flags
      dec8b( A );
      if ( regs[A]!=0xff ) {
        System.out.println( "Error: 0 - 1 != 0xff" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x00->0xff and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x00->0xff and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }
      regs[A]=0;
      regs[FLAG_REG] = 0xf0; // set all flags
      dec8b( A );
      if ( regs[A]!=0xff ) {
        System.out.println( "Error: 0 - 1 != 0xff" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x00->0xff and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x00->0xff and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }

      regs[A]=0x10;
      regs[FLAG_REG] = 0; // clear all flags
      dec8b( A );
      if ( regs[A]!=0x0f ) {
        System.out.println( "Error: 0x10 - 1 != 0x0f" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x10->0x0f and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x10->0x0f and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }
      regs[A]=0x10;
      regs[FLAG_REG] = 0xf0; // set all flags
      dec8b( A );
      if ( regs[A]!=0x0f ) {
        System.out.println( "Error: 0x10 - 1 != 0x0f" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: DEC8b: A:0x10->0x0f and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: DEC8b: A:0x10->0x0f and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) != NF_Mask ) {
        System.out.println( "Error: DEC8b: DEC'd and NF not set" );
        status = status && false;
        }
      return status;
      }

    private boolean inc8b_diag() {
      /***************************************************************************************************************
      * Test INC_8b
      * Tests 0x00 + 1, 0x0f + 1, 0xff + 1 for setting AND clearing of flags
      */
      boolean status=true;
      regs[A]=0;
      regs[FLAG_REG] = 0; // clear all flags
      inc8b( A );
      if ( regs[A]!=1 ) {
        System.out.println( "Error: 0 + 1 != 1" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0->1 and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) == HC_Mask ) {
        System.out.println( "Error: INC8b: A:0->1 and HC is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }

      regs[A]=0;
      regs[FLAG_REG] = 0xf0; // set all flags
      inc8b( A );
      if ( regs[A]!=1 ) {
        System.out.println( "Error: 0 + 1 != 1" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0->1 and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) == HC_Mask ) {
        System.out.println( "Error: INC8b: A:0->1 and HC is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }

      regs[A]=0x0f;
      regs[FLAG_REG] = 0; // clear all flags
      inc8b( A );
      if ( regs[A]!=0x10 ) {
        System.out.println( "Error: 0x0f + 1 != 0x10" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0x0f->0x10 and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: INC8b: A:0x0f->0x10 and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }

      regs[A]=0x0f;
      regs[FLAG_REG] = 0xf0; // set all flags
      inc8b( A );
      if ( regs[A]!=0x10 ) {
        System.out.println( "Error: 0x0f + 1 != 0x10" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) == ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0x0f->0x10 and ZF is set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: INC8b: A:0x0f->0x10 and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }

      regs[A]=0xff;
      regs[FLAG_REG] = 0; // clear all flags
      inc8b( A );
      if ( regs[A]!=0x00 ) {
        System.out.println( "Error: 0xff + 1 != 0x00" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) != ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0xff->0x00 and ZF is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: INC8b: A:0xff->0x00 and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }

      regs[A]=0xff;
      regs[FLAG_REG] = 0xf0; // set all flags
      inc8b( A );
      if ( regs[A]!=0x00 ) {
        System.out.println( "Error: 0xff + 1 != 0x00" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&ZF_Mask ) != ZF_Mask ) {
        System.out.println( "Error: INC8b: A:0xff->0x00 and ZF is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&HC_Mask ) != HC_Mask ) {
        System.out.println( "Error: INC8b: A:0xff->0x00 and HC is NOT set" );
        status = status && false;
        }
      if (( regs[FLAG_REG]&NF_Mask ) == NF_Mask ) {
        System.out.println( "Error: INC8b: Inc'd and NF set" );
        status = status && false;
        }
      return status;
      }
    private int diagnose( boolean verbose ) {
      boolean result;
      int count=0;
      result = inc8b_diag();
      if ( verbose && result ) {
        System.out.println( "INC8b instruction appears to work ok" );
        }
      else {
        System.out.println( "*ERROR* IN INC8b INSTRUCTION!" );
        ++count;
        }
      result = dec8b_diag();
      if ( verbose && result ) {
        System.out.println( "DEC8b instruction appears to work ok" );
        }
      else {
        System.out.println( "*ERROR* IN DEC8b INSTRUCTION!" );
        ++count;
        }
      if ( verbose || count>0 ) System.out.println( "There were errors in "+count+" instructions" );
      return count;
      }

    public static final void main( String[] args ) {
      CPU cpu = new CPU("Pokemon Blue.gb");
      if(cpu.diagnose(true)==0) {
        cpu.reset();
        while(cpu.exception()==0 && cpu.cycles()<6){
          cpu.nextinstruction();
        }
      }
    }
  }
