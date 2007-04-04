public class CPU
  {
    protected static final int CARRY8b    = 512;
    protected static final int CARRY8b_SHR = 5;

    //FLAGS
    protected static final int FLAG_REG = 5;
    protected static final int ZF_Shift = 7;
    protected static final int NF_Shift = ZF_Shift - 1;
    protected static final int HC_Shift = NF_Shift - 1;
    protected static final int CF_Shift = HC_Shift - 1;
    protected static final int ZF_Mask  = 1 << ZF_Shift;
    protected static final int NF_Mask  = 1 << NF_Shift;
    protected static final int HC_Mask  = 1 << HC_Shift;
    protected static final int CF_Mask  = 1 << CF_Shift;

    protected int TotalInstrCount = 0;

    protected int[] regs = new int[8]; //[A,B,C,D,E,F,H,L]
    protected static final int A = 0;
    protected static final int B = 1;
    protected static final int C = 2;
    protected static final int D = 3;
    protected static final int E = 4;
    protected static final int F = FLAG_REG;
    protected static final int H = 6;
    protected static final int L = 7;

    protected int PC;
    protected int SP;
    //CPU Class variables
    private Cartridge cartridge;// = new Cartridge("Pokemon Blue.gb");
    private int lastException=0;
    private Disassembler deasm;

    public CPU(Cartridge cartridge) {
      deasm = new Disassembler(cartridge, this);
      this.cartridge = cartridge;
      reset();
    }

    public void reset() {
    //TODO: Switch to bank 0
      PC = 0x100; //ROM Entry point on bank 0
      //AF=$01B0
      regs[A]=0x01;
      regs[F]=0xb0;
      //BC=$0013
      regs[B]=0x00;
      regs[C]=0x13;
      //DE=$00D8
      regs[D]=0x00;
      regs[E]=0xd8;
      //HL=$014D
      regs[H]=0x01;
      regs[L]=0x4d;
      TotalInstrCount=0;

      //Stack Pointer=$FFFE
      SP=0xfffe;
/*TODO:INTERNAL RAM
      [$FF05] = $00   ; TIMA
      [$FF06] = $00   ; TMA
      [$FF07] = $00   ; TAC
      [$FF10] = $80   ; NR10
      [$FF11] = $BF   ; NR11
      [$FF12] = $F3   ; NR12
      [$FF14] = $BF   ; NR14
      [$FF16] = $3F   ; NR21
      [$FF17] = $00   ; NR22
      [$FF19] = $BF   ; NR24
      [$FF1A] = $7F   ; NR30
      [$FF1B] = $FF   ; NR31
      [$FF1C] = $9F   ; NR32
      [$FF1E] = $BF   ; NR33
      [$FF20] = $FF   ; NR41
      [$FF21] = $00   ; NR42
      [$FF22] = $00   ; NR43
      [$FF23] = $BF   ; NR30
      [$FF24] = $77   ; NR50
      [$FF25] = $F3   ; NR51
      [$FF26] = $F1-GB, $F0-SGB ; NR52
      [$FF40] = $91   ; LCDC
      [$FF42] = $00   ; SCY
      [$FF43] = $00   ; SCX
      [$FF45] = $00   ; LYC
      [$FF47] = $FC   ; BGP
      [$FF48] = $FF   ; OBP0
      [$FF49] = $FF   ; OBP1
      [$FF4A] = $00   ; WY
      [$FF4B] = $00   ; WX
      [$FFFF] = $00   ; IE
*/
    }

    protected int cycles() {
      return TotalInstrCount;
    }

    private String disassembleinstruction() {
      //cartridge.write(PC,14);
      //cartridge.write(PC,10);
      //cartridge.write(PC,9);
      int instr = cartridge.read(PC);
      String s = String.format("$%02x\t", instr);
      // TODO take count of BC
      s+=deasm.disassemble(PC);
      return s;
    }

    protected void printCPUstatus()
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
        System.out.printf("   A=$%02x    B=$%02x    C=$%02x    D=$%02x   E=$%02x   F=$%02x   H=$%02x   L=$%02x\n", regs[A], regs[B], regs[C], regs[D], regs[E], regs[F], regs[H],regs[L]);
        System.out.printf("  PC=$%04x SP=$%04x                           flags="+flags+"\n",PC,SP);
        System.out.printf("  $%04x %s\n", PC, disassembleinstruction());
    }
    protected int readmem8b(int H, int L) {
      return cartridge.read((regs[H]<<8)|regs[L]);
    }

    protected void inc8b(int reg_index)
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

    protected void dec8b( int reg_index ) {
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

    protected void inc16b() {}

    protected void add8b(int dest, int val) {
      // Clear all flags & set NF
      regs[FLAG_REG] = (regs[FLAG_REG] & 0x0f) | NF_Mask;

      // Set HC
      regs[FLAG_REG] = regs[FLAG_REG] | (((((regs[dest]&0x0f)+(val&0x0f))&0x10)!=0?1:0)<<HC_Shift);

      // Update register (part 1)
      regs[dest] = (regs[dest] + val);

      // set CF
      regs[FLAG_REG] = regs[FLAG_REG] | (regs[dest]>>8)<<CF_Shift;

      // Clamp register (part 2)
      regs[dest]&=0xFF;

      // set ZF
      regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[dest]==0 )?1:0 )<<ZF_Shift );
    }

    protected void ld8b(int dest, int val) {
      regs[dest] = val;
    }

    protected void cp(int val) {
      //Set NF
      regs[FLAG_REG]|=HC_Mask;

      int i=regs[A]-val;
      int d=i-val;

      //Set ZF
      regs[FLAG_REG]&=~ZF_Mask;
      regs[FLAG_REG]|=((d==0)?1:0)<<ZF_Shift;

      //Set HC
      regs[FLAG_REG]&=~HC_Mask;
      regs[FLAG_REG]|=((((regs[A]&0x0f)-(val&0x0f))&0x10)>>4)<<HC_Shift;

      //Set CF
      regs[FLAG_REG]&=~CF_Mask;
      regs[FLAG_REG]|=((d&0x100)>>8)<<CF_Shift;
    }

    protected void JPnn() {
      int i=cartridge.read(++PC);
      int j=cartridge.read(++PC);
      PC = i<<8|j;
      }

    protected void JPccnn( boolean cc, int nn ) {
      if ( cc ) JPnn();
      }

    protected void JRe( int e ) {
      PC += e;
      }

    protected void JRcce( boolean cc, int e ) {
      if ( cc ) JRe( e );
      }

    protected void push(int val, boolean b16) {
      if(b16) {
        cartridge.write(SP--, val&0xff);
        cartridge.write(SP--, (val>>8)&0xff);
      }
      else {
        cartridge.write(SP--, val&0xff);
      }
    }

    protected int fetch()
    {
        return cartridge.read(PC);
    }

    private boolean execute( int instr ) {
      //System.out.printf("Executing instruction $%02x\n", instr);
      ++PC;  //FIXME: Is de PC niet ook een register in de CPU?
      switch ( instr ) {
        case 0x00:  // NOP
          break;
/*        case 0x01:  // LD BC,&0000
          // TODO
          break;
        case 0x02:  // LD (BC),A
          // TODO
          break;
        case 0x03:  // INC BC
          // TODO
          break;*/
        case 0x04:  // INC B
          inc8b( B );
          break;
        case 0x05:  // DEC B
          dec8b( B );
          break;
        case 0x0c: // INC  C
          inc8b( C );
          break;
        case 0x2c:  // INC L
          inc8b( L );
          break;
        case 0x2d:  // DEC  L
          dec8b( L );
          break;
        case 0x40:  // DEC H
          dec8b( H );
          break;
        case 0x41: // LD B, C
          ld8b(B, regs[C]);
          break;
        case 0x42: // LD   B,D
          ld8b(B, regs[D]);
          break;
        case 0x44: // LD   B,H
          ld8b(B, regs[H]);
          break;
        case 0x45: // LD   B,L
          ld8b(B, regs[L]);
          break;
        case 0x46: //LD   B,(HL)
          ld8b(B, readmem8b(H,L));
          break;
        case 0x48: // LD   C,B
          ld8b(C, regs[B]);
          break;
        case 0x49: // LD   C,C
          ld8b(C, regs[C]);
          break;
        case 0x4a: // LD   C,D
          ld8b(C, regs[D]);
          break;
        case 0x6d: // LD   L,L
          ld8b(L, regs[L]);
          break;
        case 0x80: // ADD  A,B
          add8b(A,regs[B]);
          break;
        case 0x81: // ADD  A,C
          add8b(A, regs[C]);
          break;
        case 0x86: // ADD  A,(HL)
          add8b(A, readmem8b(H,L));
          break;
//        case 0x99: // SBC  A,C
//WIP
//          break;
        case 0xbe: // CP   (HL)
          cp(readmem8b(H,L));
          break;
        case 0xc3: // JPNNNN
          JPnn();
          break;
        case 0xd4: //D4 CALL NC,&0000
          if((regs[FLAG_REG]&CF_Mask)==CF_Mask) { //call to nn, SP=SP-2, (SP)=PC, PC=nn
            push(PC, true);
            JPnn();
            }
          break;
        default:
          System.out.printf( "UNKNOWN INSTRUCTION: $%02x\n" , instr );
          return false;
        }
      ++TotalInstrCount;
      return true;
      }

      protected boolean nextinstruction() {
        printCPUstatus();
        lastException = execute(fetch()) ? 0 : 1;
        return lastException==0;
      }

      protected int exception() {
        return lastException;
      }

  }
