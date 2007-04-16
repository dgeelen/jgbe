
public class CPU
{
  static private CPU singleton = null;
  protected static final int CARRY8b = 512;
  protected static final int CARRY8b_SHR = 5;


  protected static final int ZF_Shift = 7;
  protected static final int NF_Shift = ZF_Shift - 1;
  protected static final int HC_Shift = NF_Shift - 1;
  protected static final int CF_Shift = HC_Shift - 1;
  protected static final int ZF_Mask = 1 << ZF_Shift;
  protected static final int NF_Mask = 1 << NF_Shift;
  protected static final int HC_Mask = 1 << HC_Shift;
  protected static final int CF_Mask = 1 << CF_Shift;


  static protected int TotalInstrCount = 0;
  static protected int TotalCycleCount = 0;

  protected static int B = 1;
  protected static int C = 2;
  protected static int D = 3;
  protected static int E = 4;
  protected static int F = 5;
  protected static int H = 6;
  protected static int L = 7;
  protected static int A = 0;
  static private int[] IOP = new int[0x80];
  static private int[] HRAM = new int[0x7F];
  static private int[][] WRAM = new int[0x08][0x10000];
  static private int CurrentWRAMBank=1;

  static private boolean doublespeed = false;
  static private boolean speedswitch = false;

  static private int DIVcntdwn = 0;
  static private int TIMAcntdwn = 0;
  static private int VBLANKcntdwn = 0;

  protected static int PC=0;
  protected static int SP=0;
  protected static int IE=0;
  protected static boolean IME=true;
  protected static boolean halted=false;

  static public int DirectionKeyStatus=0x0f;
  static public int ButtonKeyStatus=0x3f;

  static private Cartridge cartridge;
  static private int lastException=0;
  static private Disassembler deasm;
  static protected VideoController VC;
  static protected AudioController AC;

  public CPU( Cartridge cartridge ) {
   if (singleton != null) {
    System.out.println("FATAL: trying to instantiate second instance of CPU singleton class");
    int x[]=null;
    x[0] = 0;
   }
   singleton = this;
   this.cartridge = cartridge;
   refreshMemMap();
   deasm = new Disassembler(this);
   VC = new VideoController(this, 160, 144);
   AC = new AudioController(this);
   reset();
  }

  static private int[][] rMemMap = new int[0x10][];
  static private int[][] wMemMap = new int[0x10][];

  static public boolean isCGB() {

   return (read(0x0143) == 0x80) || (read(0x0143) == 0xC0);
  }

  static boolean BIOS_enabled = false;

  static final private void refreshMemMap() {
   if (BIOS_enabled)
    rMemMap[0x0] = null;
   else
    rMemMap[0x0] = cartridge.MM_ROM[0];

   rMemMap[0x1] = cartridge.MM_ROM[1];
   rMemMap[0x2] = cartridge.MM_ROM[2];
   rMemMap[0x3] = cartridge.MM_ROM[3];


   rMemMap[0x4] = cartridge.MM_ROM[(cartridge.CurrentROMBank<<2)|0];
   rMemMap[0x5] = cartridge.MM_ROM[(cartridge.CurrentROMBank<<2)|1];
   rMemMap[0x6] = cartridge.MM_ROM[(cartridge.CurrentROMBank<<2)|2];
   rMemMap[0x7] = cartridge.MM_ROM[(cartridge.CurrentROMBank<<2)|3];





   rMemMap[0xA] = wMemMap[0xA] = cartridge.MM_RAM[(cartridge.CurrentRAMBank<<1)|0];
   rMemMap[0xB] = wMemMap[0xB] = cartridge.MM_RAM[(cartridge.CurrentRAMBank<<1)|1];


   rMemMap[0xC] = wMemMap[0xC] = WRAM[0];
   rMemMap[0xD] = wMemMap[0xD] = WRAM[CurrentWRAMBank];


   rMemMap[0xE] = wMemMap[0xE] = rMemMap[0xC];


  }

  static final protected int read(int index) {
   int mm[]=rMemMap[index>>12];
   if (mm!=null)
    return mm[index&0x0FFF];
   return read_slow(index);
  }

  static final private int read_slow(int index) {
   int b;

   if(index<0) {
    System.out.println("ERROR: CPU.read(): No negative addresses in GameBoy memorymap.");
    b=-1;
   }
   else if(index < 0x4000) {
    if (index < 0x100) {
     b = cartridge.BIOS_ROM[index];

    }
    else if (index == 0x100) {
     System.out.println("reading from 0x100, disableing BIOS rom (PC="+PC+")");
     reset(false);
     b = read(index);
    }
    else {
     b = cartridge.MM_ROM[0][index];

    }
   }
   else if(index < 0x8000) {
    b=cartridge.read(index);
   }
   else if(index < 0xa000) {
    b=VC.read(index);
   }
   else if(index < 0xc000) {
    b=cartridge.read(index);
   }
   else if(index < 0xd000) {
    b=WRAM[0][index-0xc000];
   }
   else if(index < 0xe000) {
    b=WRAM[CurrentWRAMBank][index-0xd000];
   }
   else if(index < 0xfe00) {
    b=read(index-0x2000);
   }
   else if(index < 0xfea0) {

    b=VC.read(index);
   }
   else if(index < 0xff00) {
    System.out.println("WARNING: CPU.read(): Read from unusable memory (0xfea-0xfeff)");
    b=0;
   }
   else if(index < 0xff80) {
    switch(index) {
     case 0xff00:
      b=IOP[index-0xff00]&0xf0;
      if((b&(1<<4))==0) {
       b|=DirectionKeyStatus;
      }
      if((b&(1<<5))==0) {
       b|=ButtonKeyStatus;
      }
      break;
     case 0xff01:
     case 0xff02:


     case 0xff04:
     case 0xff05:
     case 0xff06:
     case 0xff07:
      b = IOP[index-0xff00];
      break;
     case 0xff0f:
      b = IOP[0x0f];
      break;

     case 0xff10: case 0xff11: case 0xff12: case 0xff13: case 0xff14: case 0xff15: case 0xff16: case 0xff17:
     case 0xff18: case 0xff19: case 0xff1a: case 0xff1b: case 0xff1c: case 0xff1d: case 0xff1e: case 0xff1f:
     case 0xff20: case 0xff21: case 0xff22: case 0xff23: case 0xff24: case 0xff25: case 0xff26: case 0xff27:
     case 0xff28: case 0xff29: case 0xff2a: case 0xff2b: case 0xff2c: case 0xff2d: case 0xff2e: case 0xff2f:
     case 0xff30: case 0xff31: case 0xff32: case 0xff33: case 0xff34: case 0xff35: case 0xff36: case 0xff37:
     case 0xff38: case 0xff39: case 0xff3a: case 0xff3b: case 0xff3c: case 0xff3d: case 0xff3e: case 0xff3f:
      b = AC.read(index);
      break;
     case 0xff40:
      b = VC.LCDC;
      break;
     case 0xff41:

      b = VC.STAT|(((new java.util.Random()).nextInt(2))<<1);
      break;
     case 0xff42:
      b = VC.SCY;
      break;
     case 0xff43:
      b = VC.SCX;
      break;
     case 0xff44:
      b = VC.LY;
      break;
     case 0xff45:
      b = VC.LYC;
      break;
     case 0xff47:
     case 0xff48:
     case 0xff49:
      b = IOP[index-0xff00];
      break;
     case 0xff4a:
      b = VC.WY;
      break;
     case 0xff4b:
      b = VC.WX;
      break;
     case 0xff4d:
      b = doublespeed ? (1<<7) : 0;
      break;
     case 0xff4f:
      b = VC.getcurVRAMBank();
      break;
     case 0xff51:
     case 0xff52:
     case 0xff53:
     case 0xff54:
     case 0xff55:
      b = IOP[index-0xff00];
      break;
     case 0xff68:
      b = VC.BGPI;
      break;
     case 0xff69:
      b = VC.getBGColData();
      break;
     case 0xff6a:
      b = VC.OBPI;
      break;
     case 0xff6b:
      b = VC.getOBColData();
      break;
     case 0xff70:
      b = CurrentWRAMBank;
      break;
     default:
      System.out.printf("TODO: CPU.read(): Read from IO port $%04x\n",index);
      b=0xff;
      break;
    }

   }
   else if(index < 0xffff) {
    b = HRAM[index-0xff80];
   }
   else if(index < 0x10000) {

    b=IE;
   }
   else {
    System.out.println("ERROR: CPU.read(): Out of range memory access: $"+index);
    b=0;
   }
   return b;
  }

  static final private void write(int index, int value) {
   int mm[]=wMemMap[index>>12];
   if (mm!=null) {
    mm[index&0x0FFF] = value;
    return;
   }
   if(index<0) {
    System.out.println("ERROR: CPU.write(): No negative addresses in GameBoy memorymap.");
   }
   else if(index < 0x8000) {
    cartridge.write(index, value);

    refreshMemMap();
   }
   else if(index < 0xa000) {
    VC.write(index, value);
   }
   else if(index < 0xc000) {
    cartridge.write(index, value);
   }
   else if(index < 0xd000) {
    WRAM[0][index-0xc000]=value;
   }
   else if(index < 0xe000) {
    WRAM[CurrentWRAMBank][index-0xd000]=value;
   }
   else if(index < 0xfe00) {
    write(index-0x2000, value);
   }
   else if(index < 0xfea0) {
    VC.write(index, value);
   }
   else if(index < 0xff00) {
    System.out.printf("TODO: CPU.write(): Write to unusable memory (0xfea-0xfeff) index=$%04x, value=$%02x\n",index,value);
   }
   else if(index < 0xff80) {
    switch(index) {
     case 0xff00:
      IOP[index&0xff]=value;
      break;
     case 0xff01:
      IOP[0x01]=value;
      break;
     case 0xff02:
      IOP[0x02]=value;
      if ((value&(1<<7))!=0) {

       if ((value&(1<<0))!=0) {

        IOP[0x01] = 0xFF;
        IOP[0x02] &= ~(1<<7);
        triggerInterrupt(3);
       }
       else {

       }
      }
      break;
     case 0xff04:
      IOP[0x04] = 0;
      break;
     case 0xff05:
     case 0xff06:
     case 0xff07:
      IOP[index-0xff00] = value;
      break;
     case 0xff0f:
      IOP[0x0f] = value;
      break;

     case 0xff10: case 0xff11: case 0xff12: case 0xff13: case 0xff14: case 0xff15: case 0xff16: case 0xff17:
     case 0xff18: case 0xff19: case 0xff1a: case 0xff1b: case 0xff1c: case 0xff1d: case 0xff1e: case 0xff1f:
     case 0xff20: case 0xff21: case 0xff22: case 0xff23: case 0xff24: case 0xff25: case 0xff26: case 0xff27:
     case 0xff28: case 0xff29: case 0xff2a: case 0xff2b: case 0xff2c: case 0xff2d: case 0xff2e: case 0xff2f:
     case 0xff30: case 0xff31: case 0xff32: case 0xff33: case 0xff34: case 0xff35: case 0xff36: case 0xff37:
     case 0xff38: case 0xff39: case 0xff3a: case 0xff3b: case 0xff3c: case 0xff3d: case 0xff3e: case 0xff3f:
      AC.write(index, value);
      break;
     case 0xff40:
      VC.LCDC = value;
      break;
     case 0xff41:
      VC.STAT = (VC.STAT&7)|(value&~7);
      break;
     case 0xff42:
      VC.SCY = value;
      break;
     case 0xff43:
      VC.SCX = value;
      break;
     case 0xff44:
      VC.LY = 0;
      break;
     case 0xff45:
      VC.LYC = value;

      break;
     case 0xff46:{
      for(int i=0; i<0xa0; ++i){
       write(0xfe00|i, read(i+(value<<8)));
      }






      } break;
     case 0xff47:
     case 0xff48:
     case 0xff49:
      IOP[index-0xff00] = value;
      VC.setMonoColData(index-0xff47, value);
      break;
     case 0xff4a:
      VC.WY = value;
      break;
     case 0xff4b:
      VC.WX = value;
      break;
     case 0xff4d:
      speedswitch = ((value&1)!=0);
      break;
     case 0xff4f:
      VC.selectVRAMBank(value&1);
      break;
     case 0xff51:
     case 0xff52:
     case 0xff53:
     case 0xff54:
      IOP[index-0xff00] = value;
      break;
     case 0xff55:
      int src = ((IOP[0x51]<<8)|IOP[0x52]) & 0xfff0;
      int dst = (((IOP[0x53]<<8)|IOP[0x54]) & 0x1ff0) | 0x8000;
      int len = ((value & 0x7f)+1)<<4;
      int mode = value >> 7;
      if (mode == 0) {


       for (int i = 0; i < len; ++i)
        VC.write(dst++, read(src++));
       IOP[0x55] = 0xff;
      } else {
       System.out.printf("TODO: HDMA HBlank transfer src=$%04x dst=$%04x len=$%04x\n", src, dst, len);
      }
      break;
     case 0xff68:
      VC.BGPI = value;;
      break;
     case 0xff69:
      VC.setBGColData(value);
      break;
     case 0xff6a:
      VC.OBPI = value;;
      break;
     case 0xff6b:
      VC.setOBColData(value);
      break;
     case 0xff70:
      CurrentWRAMBank=((value&0x07)<(1)?(1):(value&0x07));
      refreshMemMap();
      break;
     default:
      System.out.printf("TODO: CPU.write(): Write to IO port $%04x\n",index);
      break;
    }
   }
   else if(index < 0xffff) {
    HRAM[index-0xff80] = value;
   }
   else if(index < 0x10000) {

    IE=value;

   }
   else {
    System.out.println("ERROR: CPU.write(): Out of range memory access: $"+index);
   }
  }


  static final public void reset() {
   reset(true);
  }

  static final public void reset(boolean bios) {
   BIOS_enabled = bios;
   VC.isCGB = bios ? false : isCGB();
   refreshMemMap();

   if (bios)
    PC=0x100;


   A=VC.isCGB?0x11:0x01;
   F=0xb0;

   B=0x00;
   C=0x13;

   D=0x00;
   E=0xd8;

   H=0x01;
   L=0x4d;
   TotalInstrCount=0;
   TotalCycleCount=0;


   SP=0xfffe;

   write(0xff05, 0x00);
   write(0xff06, 0x00);
   write(0xff07, 0x00);
   write(0xff26, 0xf1);
   AC.sound_off();
   write(0xff40, 0x91);
   write(0xff42, 0x00);
   write(0xff43, 0x00);
   write(0xff45, 0x00);
   write(0xff47, 0xfc);
   write(0xff48, 0xff);
   write(0xff49, 0xff);
   write(0xff4a, 0x00);
   write(0xff4b, 0x00);
   write(0xffff, 0x00);
  }

  static final protected int cycles() {
   return TotalInstrCount;
  }

  static final protected void printCPUstatus() {
   String flags = "";
   flags += (( F & ZF_Mask ) == ZF_Mask )?"Z ":"z ";
   flags += (( F & NF_Mask ) == NF_Mask )?"N ":"n ";
   flags += (( F & HC_Mask ) == HC_Mask )?"H ":"h ";
   flags += (( F & CF_Mask ) == CF_Mask )?"C ":"c ";
   flags += (( F & ( 1 <<3 ) ) == ( 1 <<3 ) )?"1 ":"0 ";
   flags += (( F & ( 1 <<2 ) ) == ( 1 <<2 ) )?"1 ":"0 ";
   flags += (( F & ( 1 <<1 ) ) == ( 1 <<1 ) )?"1 ":"0 ";
   flags += (( F & ( 1 <<0 ) ) == ( 1 <<0 ) )?"1 ":"0 ";
   System.out.println( "---CPU Status for cycle "+TotalCycleCount+" , instruction "+TotalInstrCount+"---" );
   System.out.printf( "   A=$%02x    B=$%02x    C=$%02x    D=$%02x   E=$%02x   F=$%02x   H=$%02x   L=$%02x\n", A, B, C, D, E, F, H,L );
   System.out.printf( "  PC=$%04x SP=$%04x                           flags="+flags+"\n",PC,SP );
   System.out.println( "  "+deasm.simple_disasm( PC ) );
  }

  static final protected int checkInterrupts() {
   if(IME) {
    int ir = IOP[0x0f]&IE;
    if((ir&(1<<0))!=0) {
     IOP[0x0f] &= ~(1<<0);
     interrupt(0x40);
     return 1;
    }
    else if ((ir&(1<<1))!=0) {
     IOP[0x0f] &= ~(1<<1);
     interrupt(0x48);
     return 1;
    }
    else if ((ir&(1<<2))!=0) {
     IOP[0x0f] &= ~(1<<2);
     interrupt(0x50);
     return 1;
    }
    else if ((ir&(1<<3))!=0) {
     IOP[0x0f] &= ~(1<<3);
     interrupt(0x58);
     return 1;
    }
    else if ((ir&(1<<4))!=0) {
     IOP[0x0f] &= ~(1<<4);
     interrupt(0x60);
     return 1;
    }
   }
   return 0;
  }

  static final protected void interrupt(int i) {

   IME = false;
   { SP=(SP-1)&0xffff; write( SP, ( (PC)>>8 )&0xff ); SP=(SP-1)&0xffff; write( SP, (PC)&0xff ); };
   PC = i;
  }

  static final protected void triggerInterrupt(int i) {
   IOP[0x0f] |= (1<<i);
  }

  static final protected int execute() {
   int cycles;
   int t_mm[]; int t_mi; int t_w16; int t_acc; int t_vol; int t_mask;;
   if ((IME && ((IOP[0x0f]&IE)!=0))) {
    checkInterrupts();
    halted = false;

   }
   if (halted) return 4;
   int op = (read(PC++));
   cycles = Tables.cycles[op];


   switch ( op ) {
    case 0x00: return cycles;
    case 0xf3: IME=false; return cycles;
    case 0xfb: IME=true; return cycles;
    case 0xea: write(((read(PC++))|((read(PC++))<<8)), A); return cycles;
    case 0xfa: A = read(((read(PC++))|((read(PC++))<<8))); return cycles;
    case 0xe0: write(((read(PC++))) | 0xff00, A); return cycles;
    case 0xf0: A = (read_slow(((read(PC++))) | 0xff00)); return cycles;
    case 0xe2: write(C | 0xff00, A); return cycles;
    case 0xf2: A = (read_slow(C | 0xff00)); return cycles;
    case 0xf9: SP = ((H<<8)|L); return cycles;
    case 0x22: write(((H<<8)|L), A); { { H = (t_w16=(((H<<8)|L) + 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
    case 0x2a: A = read(((H<<8)|L)); { { H = (t_w16=(((H<<8)|L) + 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
    case 0x32: write(((H<<8)|L), A); { { H = (t_w16=(((H<<8)|L) - 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
    case 0x3a: A = read(((H<<8)|L)); { { H = (t_w16=(((H<<8)|L) - 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
    case 0xc3: { PC = ((read(PC++))|((read(PC++))<<8)); }; return cycles;
    case 0xc2: { if ((F&ZF_Mask)==0) { PC = ((read(PC++))|((read(PC++))<<8)); } else { --cycles; PC+=2; }; };; return cycles;
    case 0xca: { if ((F&ZF_Mask)!=0) { PC = ((read(PC++))|((read(PC++))<<8)); } else { --cycles; PC+=2; }; };; return cycles;
    case 0xd2: { if ((F&CF_Mask)==0) { PC = ((read(PC++))|((read(PC++))<<8)); } else { --cycles; PC+=2; }; };; return cycles;
    case 0xda: { if ((F&CF_Mask)!=0) { PC = ((read(PC++))|((read(PC++))<<8)); } else { --cycles; PC+=2; }; };; return cycles;
    case 0xcd: { { SP=(SP-1)&0xffff; write(SP, (PC+2)>>8); SP=(SP-1)&0xffff; write(SP, (PC+2)&0xff); }; { PC = ((read(PC++))|((read(PC++))<<8)); }; }; return cycles;
    case 0xc4: { if ((F&ZF_Mask)==0) { { SP=(SP-1)&0xffff; write(SP, (PC+2)>>8); SP=(SP-1)&0xffff; write(SP, (PC+2)&0xff); }; { PC = ((read(PC++))|((read(PC++))<<8)); }; } else { cycles-=3; PC+=2; }; };; return cycles;
    case 0xcc: { if ((F&ZF_Mask)!=0) { { SP=(SP-1)&0xffff; write(SP, (PC+2)>>8); SP=(SP-1)&0xffff; write(SP, (PC+2)&0xff); }; { PC = ((read(PC++))|((read(PC++))<<8)); }; } else { cycles-=3; PC+=2; }; };; return cycles;
    case 0xd4: { if ((F&CF_Mask)==0) { { SP=(SP-1)&0xffff; write(SP, (PC+2)>>8); SP=(SP-1)&0xffff; write(SP, (PC+2)&0xff); }; { PC = ((read(PC++))|((read(PC++))<<8)); }; } else { cycles-=3; PC+=2; }; };; return cycles;
    case 0xdc: { if ((F&CF_Mask)!=0) { { SP=(SP-1)&0xffff; write(SP, (PC+2)>>8); SP=(SP-1)&0xffff; write(SP, (PC+2)&0xff); }; { PC = ((read(PC++))|((read(PC++))<<8)); }; } else { cycles-=3; PC+=2; }; };; return cycles;
    case 0x18: { PC += 1+((read(PC))^0x80)-0x80; }; return cycles;
    case 0x20: { if ((F&ZF_Mask)==0) { PC += 1+((read(PC))^0x80)-0x80; } else { --cycles; ++PC; }; };; return cycles;
    case 0x28: { if ((F&ZF_Mask)!=0) { PC += 1+((read(PC))^0x80)-0x80; } else { --cycles; ++PC; }; };; return cycles;
    case 0x30: { if ((F&CF_Mask)==0) { PC += 1+((read(PC))^0x80)-0x80; } else { --cycles; ++PC; }; };; return cycles;
    case 0x38: { if ((F&CF_Mask)!=0) { PC += 1+((read(PC))^0x80)-0x80; } else { --cycles; ++PC; }; };; return cycles;
    case 0xc9: { PC = (read(SP++)|(read(SP++)<<8)); }; return cycles;
    case 0xc0: { if ((F&ZF_Mask)==0) { PC = (read(SP++)|(read(SP++)<<8)); } else { cycles-=3; }; };; return cycles;
    case 0xc8: { if ((F&ZF_Mask)!=0) { PC = (read(SP++)|(read(SP++)<<8)); } else { cycles-=3; }; };; return cycles;
    case 0xd0: { if ((F&CF_Mask)==0) { PC = (read(SP++)|(read(SP++)<<8)); } else { cycles-=3; }; };; return cycles;
    case 0xd8: { if ((F&CF_Mask)!=0) { PC = (read(SP++)|(read(SP++)<<8)); } else { cycles-=3; }; };; return cycles;
    case 0x02: write(((B<<8)|C), A); return cycles;
    case 0x0A: A = read(((B<<8)|C)); return cycles;
    case 0x12: write(((D<<8)|E), A); return cycles;
    case 0x1A: A = read(((D<<8)|E)); return cycles;
    case 0x70: write(((H<<8)|L), B); return cycles;
    case 0x71: write(((H<<8)|L), C); return cycles;
    case 0x72: write(((H<<8)|L), D); return cycles;
    case 0x73: write(((H<<8)|L), E); return cycles;
    case 0x74: write(((H<<8)|L), H); return cycles;
    case 0x75: write(((H<<8)|L), L); return cycles;
    case 0x77: write(((H<<8)|L), A); return cycles;
    case 0x76: halted = true; return cycles;
    case 0xd9: IME = true; { PC = (read(SP++)|(read(SP++)<<8)); }; return cycles;
    case 0xc1: { B = (t_w16=(read(SP++)|(read(SP++)<<8))) >> 8; C = t_w16 & 0xFF; }; return cycles;
    case 0xd1: { D = (t_w16=(read(SP++)|(read(SP++)<<8))) >> 8; E = t_w16 & 0xFF; }; return cycles;
    case 0xe1: { H = (t_w16=(read(SP++)|(read(SP++)<<8))) >> 8; L = t_w16 & 0xFF; }; return cycles;
    case 0xf1: { A = (t_w16=(read(SP++)|(read(SP++)<<8))) >> 8; F = t_w16 & 0xFF; }; return cycles;
    case 0xc5: { SP=(SP-1)&0xffff; write(SP, (t_w16=((B<<8)|C))>>8); SP=(SP-1)&0xffff; write(SP, (t_w16)&0xff); }; return cycles;
    case 0xd5: { SP=(SP-1)&0xffff; write(SP, (t_w16=((D<<8)|E))>>8); SP=(SP-1)&0xffff; write(SP, (t_w16)&0xff); }; return cycles;
    case 0xe5: { SP=(SP-1)&0xffff; write(SP, (t_w16=((H<<8)|L))>>8); SP=(SP-1)&0xffff; write(SP, (t_w16)&0xff); }; return cycles;
    case 0xf5: { SP=(SP-1)&0xffff; write(SP, (t_w16=((A<<8)|F))>>8); SP=(SP-1)&0xffff; write(SP, (t_w16)&0xff); }; return cycles;
    case 0x09: { F &= ZF_Mask; L += C; H += B; if (L > 0xff) { L &= 0xff; ++H; F |= HC_Mask; } if (H > 0xff) { H &= 0xff; F |= CF_Mask; } }; return cycles;
    case 0x19: { F &= ZF_Mask; L += E; H += D; if (L > 0xff) { L &= 0xff; ++H; F |= HC_Mask; } if (H > 0xff) { H &= 0xff; F |= CF_Mask; } }; return cycles;
    case 0x29: { F &= ZF_Mask; L += L; H += H; if (L > 0xff) { L &= 0xff; ++H; F |= HC_Mask; } if (H > 0xff) { H &= 0xff; F |= CF_Mask; } }; return cycles;
    case 0x39: { F &= ZF_Mask; L += (SP&0xff); H += (SP>>8); if (L > 0xff) { L &= 0xff; ++H; F |= HC_Mask; } if (H > 0xff) { H &= 0xff; F |= CF_Mask; } }; return cycles;
    case 0xe9: PC = ((H<<8)|L); return cycles;
    case 0x2f: A ^= 0xFF; F |= (NF_Mask|HC_Mask); return cycles;
    case 0x36: write(((H<<8)|L), ((read(PC++)))); return cycles;
    case 0x07: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (A) = ShTables. RLC_val[t_acc]; }; F &= CF_Mask; return cycles;
    case 0x17: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (A) = ShTables. RL_val[t_acc]; }; F &= CF_Mask; return cycles;
    case 0x0f: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (A) = ShTables. RRC_val[t_acc]; }; F &= CF_Mask; return cycles;
    case 0x1f: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (A) = ShTables. RR_val[t_acc]; }; F &= CF_Mask; return cycles;
    case 0xc7: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x00; return cycles;
    case 0xcf: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x08; return cycles;
    case 0xd7: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x10; return cycles;
    case 0xdf: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x18; return cycles;
    case 0xe7: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x20; return cycles;
    case 0xef: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x28; return cycles;
    case 0xf7: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x30; return cycles;
    case 0xff: { SP=(SP-1)&0xffff; write(SP, (PC)>>8); SP=(SP-1)&0xffff; write(SP, (PC)&0xff); }; PC = 0x38; return cycles;
    case 0x37: F &= ZF_Mask; F |= CF_Mask; return cycles;
    case 0x3f: F &= (ZF_Mask|CF_Mask); F ^= CF_Mask; return cycles;
    case 0x08: {
     t_acc = ((read(PC++))|((read(PC++))<<8));
     write(t_acc, SP>>8);
     write((t_acc+1)&0xffff, SP&0xff);
    }; return cycles;
    case 0xf8:{
     { H = SP >> 8; L = SP & 0xFF; };
     L += (((((read(PC++))))^0x80)-0x80);
     F = 0;
     if (L > 0xff) {
      L &= 0xff;
      F |= HC_Mask;
      ++H;
      if (H > 0xff) {
       H &= 0xff;
       F |= CF_Mask;
      }
     }
     else if (L < 0) {
      L &= 0xff;
      F |= HC_Mask;
      --H;
      if (H < 0) {
       H &= 0xff;
       F |= CF_Mask;
      }
     }
    };return cycles;
    case 0x27:{
     t_acc = Tables.daa[(((F)&0x70)<<4) | A];
     A += t_acc;
     F = (F & (NF_Mask)) | ((A==0)?ZF_Mask:0) | Tables.daa_carry[t_acc>>2];
     A &= 0xff;
    };return cycles;
    case 0xe8:{
     t_acc = SP;
     SP += (((((read(PC++))))^0x80)-0x80);
     F = ((SP >> 8) != (t_acc >> 8)) ? HC_Mask : 0;
     if ((SP & ~0xffff) != 0) {
      SP &= 0xffff;
      F |= CF_Mask;
     }
    };return cycles;
    case 0x10: if (speedswitch) {
     System.out.println("Speed switch!");
     doublespeed = !doublespeed;
     ++PC;
     speedswitch = false;
    }; return cycles;
    case (0xfe) : { t_vol = ((read(PC++))); { t_acc = A - (t_vol); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; }; return cycles;
	case (0xb8) : { t_acc = A - (B); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((B)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+1: { t_acc = A - (C); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((C)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+2: { t_acc = A - (D); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((D)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+3: { t_acc = A - (E); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((E)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+4: { t_acc = A - (H); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((H)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+5: { t_acc = A - (L); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((L)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
	case (0xb8)+6: { t_vol = (read(((H<<8)|L))); { t_acc = A - (t_vol); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; }; return cycles;
	case (0xb8)+7: { t_acc = A - (A); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ ((A)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; return cycles;
    case (0xe6) : { A &= ((read(PC++))); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0) : { A &= (B); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+1: { A &= (C); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+2: { A &= (D); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+3: { A &= (E); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+4: { A &= (H); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+5: { A &= (L); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+6: { A &= (read(((H<<8)|L))); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa0)+7: { A &= (A); F = HC_Mask | ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
    case (0xee) : { A ^= ((read(PC++))); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8) : { A ^= (B); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+1: { A ^= (C); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+2: { A ^= (D); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+3: { A ^= (E); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+4: { A ^= (H); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+5: { A ^= (L); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+6: { A ^= (read(((H<<8)|L))); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xa8)+7: { A ^= (A); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
    case (0xf6) : { A |= ((read(PC++))); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0) : { A |= (B); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+1: { A |= (C); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+2: { A |= (D); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+3: { A |= (E); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+4: { A |= (H); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+5: { A |= (L); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+6: { A |= (read(((H<<8)|L))); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0xb0)+7: { A |= (A); F = ( (A) != 0 ? 0 : ZF_Mask ); }; return cycles;
    case (0xC6) : { t_vol = ((read(PC++))); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x80) : { t_acc = A + (B); F=(HC_Mask&((A ^ ((B)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+1: { t_acc = A + (C); F=(HC_Mask&((A ^ ((C)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+2: { t_acc = A + (D); F=(HC_Mask&((A ^ ((D)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+3: { t_acc = A + (E); F=(HC_Mask&((A ^ ((E)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+4: { t_acc = A + (H); F=(HC_Mask&((A ^ ((H)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+5: { t_acc = A + (L); F=(HC_Mask&((A ^ ((L)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
	case (0x80)+6: { t_vol = (read(((H<<8)|L))); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x80)+7: { t_acc = A + (A); F=(HC_Mask&((A ^ ((A)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; return cycles;
    case (0xCE) : { t_vol = ((read(PC++)))+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88) : { t_vol = (B)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+1: { t_vol = (C)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+2: { t_vol = (D)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+3: { t_vol = (E)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+4: { t_vol = (H)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+5: { t_vol = (L)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+6: { t_vol = (read(((H<<8)|L)))+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
	case (0x88)+7: { t_vol = (A)+((F>>CF_Shift)&1); { t_acc = A + (t_vol); F=(HC_Mask&((A ^ ((t_vol)-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)); if ( t_acc>0xff ) { F |= CF_Mask; } F |= ( (A) != 0 ? 0 : ZF_Mask ); (A) = t_acc&0xff;};; }; return cycles;
    case (0xD6) : { t_vol = ((read(PC++))); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x90) : { { t_acc = A - ((B)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((B))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+1: { { t_acc = A - ((C)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((C))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+2: { { t_acc = A - ((D)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((D))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+3: { { t_acc = A - ((E)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((E))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+4: { { t_acc = A - ((H)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((H))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+5: { { t_acc = A - ((L)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((L))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
	case (0x90)+6: { t_vol = (read(((H<<8)|L))); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x90)+7: { { t_acc = A - ((A)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((A))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; return cycles;
    case (0xDE) : { t_vol = ((read(PC++)))+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98) : { t_vol = (B)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+1: { t_vol = (C)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+2: { t_vol = (D)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+3: { t_vol = (E)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+4: { t_vol = (H)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+5: { t_vol = (L)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+6: { t_vol = (read(((H<<8)|L)))+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
	case (0x98)+7: { t_vol = (A)+((F>>CF_Shift)&1); { { t_acc = A - ((t_vol)); F = NF_Mask | (( (((t_acc)&0xff)) != 0 ? 0 : ZF_Mask )) | (HC_Mask&((A ^ (((t_vol))-((F&CF_Mask)>>CF_Shift)) ^ ((t_acc)&0xff)) << 1)) | (CF_Mask&(t_acc>>8)); }; A = ((t_acc)&0xff); }; }; return cycles;
    case (0x04+(0<<3)): { (B) = (((B)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(B)]; }; return cycles;
	case (0x05+(0<<3)): { (B) = (((B)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(B)]; }; return cycles;
	case (0x04+(1<<3)): { (C) = (((C)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(C)]; }; return cycles;
	case (0x05+(1<<3)): { (C) = (((C)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(C)]; }; return cycles;
	case (0x04+(2<<3)): { (D) = (((D)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(D)]; }; return cycles;
	case (0x05+(2<<3)): { (D) = (((D)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(D)]; }; return cycles;
	case (0x04+(3<<3)): { (E) = (((E)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(E)]; }; return cycles;
	case (0x05+(3<<3)): { (E) = (((E)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(E)]; }; return cycles;
	case (0x04+(4<<3)): { (H) = (((H)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(H)]; }; return cycles;
	case (0x05+(4<<3)): { (H) = (((H)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(H)]; }; return cycles;
	case (0x04+(5<<3)): { (L) = (((L)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(L)]; }; return cycles;
	case (0x05+(5<<3)): { (L) = (((L)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(L)]; }; return cycles;
	case (0x04+(7<<3)): { (A) = (((A)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(A)]; }; return cycles;
	case (0x05+(7<<3)): { (A) = (((A)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(A)]; }; return cycles;
	case 0x34: { t_acc = read(((H<<8)|L)); { (t_acc) = (((t_acc)+1)&0xff); F &= CF_Mask; F |= Tables.incflag[(t_acc)]; }; write(((H<<8)|L), t_acc); }; return cycles;
	case 0x35: { t_acc = read(((H<<8)|L)); { (t_acc) = (((t_acc)-1)&0xff); F &= CF_Mask; F |= Tables.decflag[(t_acc)]; }; write(((H<<8)|L), t_acc); }; return cycles;
    case (0x03+(0<<4)): { { B = (t_w16=(((B<<8)|C) + 1) & 0xffff) >> 8; C = t_w16 & 0xFF; }; }; return cycles;
	case (0x0b+(0<<4)): { { B = (t_w16=(((B<<8)|C) - 1) & 0xffff) >> 8; C = t_w16 & 0xFF; }; }; return cycles;
	case (0x03+(1<<4)): { { D = (t_w16=(((D<<8)|E) + 1) & 0xffff) >> 8; E = t_w16 & 0xFF; }; }; return cycles;
	case (0x0b+(1<<4)): { { D = (t_w16=(((D<<8)|E) - 1) & 0xffff) >> 8; E = t_w16 & 0xFF; }; }; return cycles;
	case (0x03+(2<<4)): { { H = (t_w16=(((H<<8)|L) + 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
	case (0x0b+(2<<4)): { { H = (t_w16=(((H<<8)|L) - 1) & 0xffff) >> 8; L = t_w16 & 0xFF; }; }; return cycles;
	case (0x03+(3<<4)): { { SP = ((SP) + 1) & 0xffff; }; }; return cycles;
	case (0x0b+(3<<4)): { { SP = ((SP) - 1) & 0xffff; }; }; return cycles;
    case (0x40+(0<<3)+(0)): { B = B; }; return cycles;
	case (0x40+(0<<3)+(1)): { B = C; }; return cycles;
	case (0x40+(0<<3)+(2)): { B = D; }; return cycles;
	case (0x40+(0<<3)+(3)): { B = E; }; return cycles;
	case (0x40+(0<<3)+(4)): { B = H; }; return cycles;
	case (0x40+(0<<3)+(5)): { B = L; }; return cycles;
	case (0x40+(0<<3)+(7)): { B = A; }; return cycles;
	case (0x06+(0<<3)): { B = ((read(PC++))); }; return cycles;
	case (0x46+(0<<3)): (B) = read(((H<<8)|L)); return cycles;
	case (0x40+(1<<3)+(0)): { C = B; }; return cycles;
	case (0x40+(1<<3)+(1)): { C = C; }; return cycles;
	case (0x40+(1<<3)+(2)): { C = D; }; return cycles;
	case (0x40+(1<<3)+(3)): { C = E; }; return cycles;
	case (0x40+(1<<3)+(4)): { C = H; }; return cycles;
	case (0x40+(1<<3)+(5)): { C = L; }; return cycles;
	case (0x40+(1<<3)+(7)): { C = A; }; return cycles;
	case (0x06+(1<<3)): { C = ((read(PC++))); }; return cycles;
	case (0x46+(1<<3)): (C) = read(((H<<8)|L)); return cycles;
	case (0x40+(2<<3)+(0)): { D = B; }; return cycles;
	case (0x40+(2<<3)+(1)): { D = C; }; return cycles;
	case (0x40+(2<<3)+(2)): { D = D; }; return cycles;
	case (0x40+(2<<3)+(3)): { D = E; }; return cycles;
	case (0x40+(2<<3)+(4)): { D = H; }; return cycles;
	case (0x40+(2<<3)+(5)): { D = L; }; return cycles;
	case (0x40+(2<<3)+(7)): { D = A; }; return cycles;
	case (0x06+(2<<3)): { D = ((read(PC++))); }; return cycles;
	case (0x46+(2<<3)): (D) = read(((H<<8)|L)); return cycles;
	case (0x40+(3<<3)+(0)): { E = B; }; return cycles;
	case (0x40+(3<<3)+(1)): { E = C; }; return cycles;
	case (0x40+(3<<3)+(2)): { E = D; }; return cycles;
	case (0x40+(3<<3)+(3)): { E = E; }; return cycles;
	case (0x40+(3<<3)+(4)): { E = H; }; return cycles;
	case (0x40+(3<<3)+(5)): { E = L; }; return cycles;
	case (0x40+(3<<3)+(7)): { E = A; }; return cycles;
	case (0x06+(3<<3)): { E = ((read(PC++))); }; return cycles;
	case (0x46+(3<<3)): (E) = read(((H<<8)|L)); return cycles;
	case (0x40+(4<<3)+(0)): { H = B; }; return cycles;
	case (0x40+(4<<3)+(1)): { H = C; }; return cycles;
	case (0x40+(4<<3)+(2)): { H = D; }; return cycles;
	case (0x40+(4<<3)+(3)): { H = E; }; return cycles;
	case (0x40+(4<<3)+(4)): { H = H; }; return cycles;
	case (0x40+(4<<3)+(5)): { H = L; }; return cycles;
	case (0x40+(4<<3)+(7)): { H = A; }; return cycles;
	case (0x06+(4<<3)): { H = ((read(PC++))); }; return cycles;
	case (0x46+(4<<3)): (H) = read(((H<<8)|L)); return cycles;
	case (0x40+(5<<3)+(0)): { L = B; }; return cycles;
	case (0x40+(5<<3)+(1)): { L = C; }; return cycles;
	case (0x40+(5<<3)+(2)): { L = D; }; return cycles;
	case (0x40+(5<<3)+(3)): { L = E; }; return cycles;
	case (0x40+(5<<3)+(4)): { L = H; }; return cycles;
	case (0x40+(5<<3)+(5)): { L = L; }; return cycles;
	case (0x40+(5<<3)+(7)): { L = A; }; return cycles;
	case (0x06+(5<<3)): { L = ((read(PC++))); }; return cycles;
	case (0x46+(5<<3)): (L) = read(((H<<8)|L)); return cycles;
	case (0x40+(7<<3)+(0)): { A = B; }; return cycles;
	case (0x40+(7<<3)+(1)): { A = C; }; return cycles;
	case (0x40+(7<<3)+(2)): { A = D; }; return cycles;
	case (0x40+(7<<3)+(3)): { A = E; }; return cycles;
	case (0x40+(7<<3)+(4)): { A = H; }; return cycles;
	case (0x40+(7<<3)+(5)): { A = L; }; return cycles;
	case (0x40+(7<<3)+(7)): { A = A; }; return cycles;
	case (0x06+(7<<3)): { A = ((read(PC++))); }; return cycles;
	case (0x46+(7<<3)): (A) = read(((H<<8)|L)); return cycles;
	case (0x01+(0<<4)): { C = ((read(PC++))); B = ((read(PC++))); } ; return cycles;
	case (0x01+(1<<4)): { E = ((read(PC++))); D = ((read(PC++))); } ; return cycles;
	case (0x01+(2<<4)): { L = ((read(PC++))); H = ((read(PC++))); } ; return cycles;
	case (0x01+(3<<4)): { SP = ((read(PC++))|((read(PC++))<<8)); } ; return cycles;
    case 0xcb:
     op = (read(PC++));
     cycles = Tables.cb_cycles[op];
     switch ( op ) {
      case (0x40)+(0<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(0<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 0)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(1<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(1<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 1)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(2<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(2<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 2)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(3<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(3<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 3)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(4<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(4<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 4)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(5<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(5<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 5)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(6<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(6<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 6)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
	case (0x40)+(7<<3)+0: { F = (F & CF_Mask) | HC_Mask | ( ((B) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+1: { F = (F & CF_Mask) | HC_Mask | ( ((C) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+2: { F = (F & CF_Mask) | HC_Mask | ( ((D) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+3: { F = (F & CF_Mask) | HC_Mask | ( ((E) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+4: { F = (F & CF_Mask) | HC_Mask | ( ((H) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+5: { F = (F & CF_Mask) | HC_Mask | ( ((L) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+7: { F = (F & CF_Mask) | HC_Mask | ( ((A) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; return cycles;
	case (0x40)+(7<<3)+6: { { F = (F & CF_Mask) | HC_Mask | ( ((read(((H<<8)|L))) & (1 << 7)) != 0 ? 0 : ZF_Mask ); }; }; return cycles;
      case (0x80)+(0<<3)+0: { (B) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+1: { (C) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+2: { (D) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+3: { (E) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+4: { (H) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+5: { (L) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+7: { (A) &= ~(1 << 0); }; return cycles;
	case (0x80)+(0<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 0)); }; return cycles;
	case (0x80)+(1<<3)+0: { (B) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+1: { (C) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+2: { (D) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+3: { (E) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+4: { (H) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+5: { (L) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+7: { (A) &= ~(1 << 1); }; return cycles;
	case (0x80)+(1<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 1)); }; return cycles;
	case (0x80)+(2<<3)+0: { (B) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+1: { (C) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+2: { (D) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+3: { (E) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+4: { (H) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+5: { (L) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+7: { (A) &= ~(1 << 2); }; return cycles;
	case (0x80)+(2<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 2)); }; return cycles;
	case (0x80)+(3<<3)+0: { (B) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+1: { (C) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+2: { (D) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+3: { (E) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+4: { (H) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+5: { (L) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+7: { (A) &= ~(1 << 3); }; return cycles;
	case (0x80)+(3<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 3)); }; return cycles;
	case (0x80)+(4<<3)+0: { (B) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+1: { (C) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+2: { (D) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+3: { (E) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+4: { (H) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+5: { (L) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+7: { (A) &= ~(1 << 4); }; return cycles;
	case (0x80)+(4<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 4)); }; return cycles;
	case (0x80)+(5<<3)+0: { (B) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+1: { (C) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+2: { (D) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+3: { (E) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+4: { (H) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+5: { (L) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+7: { (A) &= ~(1 << 5); }; return cycles;
	case (0x80)+(5<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 5)); }; return cycles;
	case (0x80)+(6<<3)+0: { (B) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+1: { (C) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+2: { (D) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+3: { (E) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+4: { (H) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+5: { (L) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+7: { (A) &= ~(1 << 6); }; return cycles;
	case (0x80)+(6<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 6)); }; return cycles;
	case (0x80)+(7<<3)+0: { (B) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+1: { (C) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+2: { (D) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+3: { (E) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+4: { (H) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+5: { (L) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+7: { (A) &= ~(1 << 7); }; return cycles;
	case (0x80)+(7<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) & ~(1 << 7)); }; return cycles;
      case (0xc0)+(0<<3)+0: { (B) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+1: { (C) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+2: { (D) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+3: { (E) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+4: { (H) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+5: { (L) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+7: { (A) |= (1 << 0); }; return cycles;
	case (0xc0)+(0<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 0)); }; return cycles;
	case (0xc0)+(1<<3)+0: { (B) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+1: { (C) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+2: { (D) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+3: { (E) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+4: { (H) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+5: { (L) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+7: { (A) |= (1 << 1); }; return cycles;
	case (0xc0)+(1<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 1)); }; return cycles;
	case (0xc0)+(2<<3)+0: { (B) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+1: { (C) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+2: { (D) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+3: { (E) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+4: { (H) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+5: { (L) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+7: { (A) |= (1 << 2); }; return cycles;
	case (0xc0)+(2<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 2)); }; return cycles;
	case (0xc0)+(3<<3)+0: { (B) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+1: { (C) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+2: { (D) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+3: { (E) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+4: { (H) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+5: { (L) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+7: { (A) |= (1 << 3); }; return cycles;
	case (0xc0)+(3<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 3)); }; return cycles;
	case (0xc0)+(4<<3)+0: { (B) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+1: { (C) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+2: { (D) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+3: { (E) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+4: { (H) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+5: { (L) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+7: { (A) |= (1 << 4); }; return cycles;
	case (0xc0)+(4<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 4)); }; return cycles;
	case (0xc0)+(5<<3)+0: { (B) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+1: { (C) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+2: { (D) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+3: { (E) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+4: { (H) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+5: { (L) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+7: { (A) |= (1 << 5); }; return cycles;
	case (0xc0)+(5<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 5)); }; return cycles;
	case (0xc0)+(6<<3)+0: { (B) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+1: { (C) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+2: { (D) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+3: { (E) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+4: { (H) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+5: { (L) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+7: { (A) |= (1 << 6); }; return cycles;
	case (0xc0)+(6<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 6)); }; return cycles;
	case (0xc0)+(7<<3)+0: { (B) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+1: { (C) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+2: { (D) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+3: { (E) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+4: { (H) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+5: { (L) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+7: { (A) |= (1 << 7); }; return cycles;
	case (0xc0)+(7<<3)+6: { write(((H<<8)|L), read(((H<<8)|L)) | (1 << 7)); }; return cycles;
      case (0x00)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (B) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (C) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (D) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (E) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (H) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (L) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; (A) = ShTables. RLC_val[t_acc]; }; return cycles;
	case (0x00)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. RLC_flag[t_acc]; write(((H<<8)|L), ShTables. RLC_val[t_acc]); }; return cycles;
      case (0x08)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (B) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (C) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (D) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (E) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (H) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (L) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; (A) = ShTables. RRC_val[t_acc]; }; return cycles;
	case (0x08)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. RRC_flag[t_acc]; write(((H<<8)|L), ShTables. RRC_val[t_acc]); }; return cycles;
      case (0x10)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (B) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (C) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (D) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (E) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (H) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (L) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; (A) = ShTables. RL_val[t_acc]; }; return cycles;
	case (0x10)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. RL_flag[t_acc]; write(((H<<8)|L), ShTables. RL_val[t_acc]); }; return cycles;
      case (0x18)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (B) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (C) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (D) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (E) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (H) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (L) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; (A) = ShTables. RR_val[t_acc]; }; return cycles;
	case (0x18)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. RR_flag[t_acc]; write(((H<<8)|L), ShTables. RR_val[t_acc]); }; return cycles;
      case (0x20)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (B) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (C) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (D) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (E) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (H) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (L) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; (A) = ShTables. SLA_val[t_acc]; }; return cycles;
	case (0x20)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. SLA_flag[t_acc]; write(((H<<8)|L), ShTables. SLA_val[t_acc]); }; return cycles;
      case (0x28)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (B) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (C) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (D) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (E) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (H) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (L) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; (A) = ShTables. SRA_val[t_acc]; }; return cycles;
	case (0x28)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. SRA_flag[t_acc]; write(((H<<8)|L), ShTables. SRA_val[t_acc]); }; return cycles;
      case (0x38)+0: { t_acc = (B) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (B) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+1: { t_acc = (C) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (C) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+2: { t_acc = (D) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (D) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+3: { t_acc = (E) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (E) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+4: { t_acc = (H) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (H) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+5: { t_acc = (L) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (L) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+7: { t_acc = (A) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; (A) = ShTables. SRL_val[t_acc]; }; return cycles;
	case (0x38)+6: { t_acc = read(((H<<8)|L)) | ((F&CF_Mask)<<4); F = ShTables. SRL_flag[t_acc]; write(((H<<8)|L), ShTables. SRL_val[t_acc]); }; return cycles;
      case 0x30+0: B = Tables.swap[B]; F &= ZF_Mask; return cycles;
	case 0x30+1: C = Tables.swap[C]; F &= ZF_Mask; return cycles;
	case 0x30+2: D = Tables.swap[D]; F &= ZF_Mask; return cycles;
	case 0x30+3: E = Tables.swap[E]; F &= ZF_Mask; return cycles;
	case 0x30+4: H = Tables.swap[H]; F &= ZF_Mask; return cycles;
	case 0x30+5: L = Tables.swap[L]; F &= ZF_Mask; return cycles;
	case 0x30+7: A = Tables.swap[A]; F &= ZF_Mask; return cycles;
	case 0x30+6: write(((H<<8)|L), Tables.swap[read(((H<<8)|L))]); F &= ZF_Mask; return cycles;
       default:
      System.out.printf( "UNKNOWN PREFIX INSTRUCTION: $%02x\n" , op );
      PC -= 2;
      return 0;
     }
    default:
     System.out.printf( "UNKNOWN INSTRUCTION: $%02x\n" , op );
     PC -= 1;
     return 0;
   }


  }

  static long lastns = 0;
  static long lastuf = 0;
  static int samplesLeft = 0;

  final public int nextinstruction() {
   int res = 4*execute();
   SP &= 0xffff;
   PC &= 0xffff;
   ++TotalInstrCount;
   TotalCycleCount += res;
   lastException = (res!=0) ? 0 : 1;
   if (res > 0) {

    DIVcntdwn -= res;
    if (DIVcntdwn < 0) {
     DIVcntdwn += 256;
     ++IOP[0x04];
     IOP[0x04] &= 0xff;
    }
    int tac = IOP[0x07];
    if ((tac&4)!=0) {
     TIMAcntdwn -= res;
     if (TIMAcntdwn < 0) {
      if ((tac&3)==0) TIMAcntdwn += 1024;
      if ((tac&3)==1) TIMAcntdwn += 16;
      if ((tac&3)==2) TIMAcntdwn += 64;
      if ((tac&3)==3) TIMAcntdwn += 256;
      ++IOP[0x05];
      if (IOP[0x05] > 0xff) {
       IOP[0x05] = IOP[0x06];
       triggerInterrupt(2);
      }
     }
    }

    if (doublespeed) {
     VBLANKcntdwn -= res/2;
     AC.render(res>>1);
    }
     else {
     VBLANKcntdwn -= res;
     AC.render(res);
    }
    if (VBLANKcntdwn < 0) {
     VBLANKcntdwn += 456;
     VC.renderNextScanline();
    }





   }

   return res;
  }

  final protected int exception() {
   return lastException;
  }
}
