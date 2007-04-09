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
		protected int TotalCycleCount = 0;

		protected int[] regs = new int[8]; //[A,B,C,D,E,F,H,L]
		protected static final int B = 1;
		protected static final int C = 2;
		protected static final int D = 3;
		protected static final int E = 4;
		protected static final int F = FLAG_REG;
		protected static final int H = 6;
		protected static final int L = 7;
		protected static final int A = 0;

		protected int[] IOP = new int[0x80]; //IO Ports
		private int[] HRAM = new int[0x7F]; //HighRAM
		private int[][] WRAM = new int[0x08][0x10000]; //8x4k InternalRAM
		private int CurrentWRAMBank=1;

		private int curcycles;

		private int DIVcntdwn = 0;
		private int TIMAcntdwn = 0;
		private int VBLANKcntdwn = 0;

		protected int PC=0;         ///< Program counter
		protected int SP=0;         ///< Stack Pointer
		protected int IE=0;         ///< Interrupt Enable (allowed interrupts)
		protected boolean IME=true; ///< Interrupt Master Enable
		protected boolean halted=false;
		//IO
		public int DirectionKeyStatus=0; //bitmask
		public int ButtonKeyStatus=0x3f; //bitmask
		//CPU Class variables
		private Cartridge cartridge;// = new Cartridge("Pokemon Blue.gb");
		private int lastException=0;
		private Disassembler deasm;
		protected VideoController VC;
		protected AudioController AC;

		public CPU( Cartridge cartridge ) {
			deasm = new Disassembler(this);
			VC = new VideoController(this);
			AC = new AudioController();
			this.cartridge = cartridge;
			reset();
		}

		final protected int read(int index) {
			/* Memorymap:
			 * 0000-3FFF   16KB ROM Bank 00     (in cartridge, fixed at bank 00)
			 * 4000-7FFF   16KB ROM Bank 01..NN (in cartridge, switchable bank number)
			 * 8000-9FFF   8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
			 * A000-BFFF   8KB External RAM     (in cartridge, switchable bank, if any)
			 * C000-CFFF   4KB Work RAM Bank 0 (WRAM)
			 * D000-DFFF   4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
			 * E000-FDFF   Same as C000-DDFF (ECHO)    (typically not used)
			 * FE00-FE9F   Sprite Attribute Table (OAM)
			 * FEA0-FEFF   Not Usable
			 * FF00-FF7F   I/O Ports
			 * FF80-FFFE   High RAM (HRAM)
			 * FFFF        Interrupt Enable Register
			 */
			curcycles+=4;
			int b=0; // b==byte read
			if(index<0) { //Invalid
				System.out.println("ERROR: CPU.read(): No negative addresses in GameBoy memorymap.");
				b=-1;
			}
			else if(index < 0x4000) { //16KB ROM Bank 00     (in cartridge, fixed at bank 00)
				b=cartridge.read(index);
			}
			else if(index < 0x8000) { //16KB ROM Bank 01..NN (in cartridge, switchable bank number)
				b=cartridge.read(index);
			}
			else if(index < 0xA000) { //8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
				b=VC.read(index);
			}
			else if(index < 0xC000) { //8KB External RAM     (in cartridge, switchable bank, if any)
				b=cartridge.read(index);
			}
			else if(index < 0xd000) { //4KB Work RAM Bank 0 (WRAM)
				b=WRAM[0][index-0xc000];
			}
			else if(index < 0xe000) { //4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
				b=WRAM[CurrentWRAMBank][index-0xd000];
			}
			else if(index < 0xfe00) { //Same as C000-DDFF (ECHO)    (typically not used)
				b=read(index-0x2000);
			}
			else if(index < 0xfea0) { //Sprite Attribute Table (OAM)
				//System.out.println("TODO: CPU.read(): Sprite Attribute Table");
				b=VC.read(index);
			}
			else if(index < 0xff00) { //Not Usable
				System.out.println("WARNING: CPU.read(): Read from unusable memory (0xfea-0xfeff)");
				b=0;
			}
			else if(index < 0xff80) { //I/O Ports
				switch(index) {
					case 0xff00: // FF00 - P1/JOYP - Joypad (R/W)
						b=IOP[index-0xff00]&0xf0;
						if((b&(1<<4))==0) { // Direction keys, 0=select
							b|=DirectionKeyStatus;
						}
						if((b&(1<<5))==0) { // Buttons, 0=select
							b|=ButtonKeyStatus;
						}
						break;
					case 0xff04: // DIV
					case 0xff05: // TIMA
					case 0xff06: // TMA
					case 0xff07: // TAC
						b = IOP[index-0xff00];
						break;
					case 0xff0f: // FF0F - IF - Interrupt Flag (R/W)
						b = IOP[0x0f];
						break;
					/* AUDIO IO PORTS */
					case 0xff10: case 0xff11: case 0xff12: case 0xff13: case 0xff14: case 0xff15: case 0xff16: case 0xff17:
					case 0xff18: case 0xff19: case 0xff1a: case 0xff1b: case 0xff1c: case 0xff1d: case 0xff1e: case 0xff1f:
					case 0xff20: case 0xff21: case 0xff22: case 0xff23: case 0xff24: case 0xff25: case 0xff26: case 0xff27:
					case 0xff28: case 0xff29: case 0xff2a: case 0xff2b: case 0xff2c: case 0xff2d: case 0xff2e: case 0xff2f:
					case 0xff30: case 0xff31: case 0xff32: case 0xff33: case 0xff34: case 0xff35: case 0xff36: case 0xff37:
					case 0xff38: case 0xff39: case 0xff3a: case 0xff3b: case 0xff3c: case 0xff3d: case 0xff3e: case 0xff3f:
						b = AC.read(index);
						break;
					case 0xff40: // LCDC register
						b = VC.LCDC;
						break;
					case 0xff41: // FF41 - STAT - LCDC Status (R/W)
						b = VC.STAT;
						break;
					case 0xff42: // SCY
						b = VC.SCY;
						break;
					case 0xff43: // SCX
						b = VC.SCX;
						break;
					case 0xff44: // LY
						b = VC.LY;
						break;
					case 0xff45: // LYC
						b = VC.LYC;
						break;
					case 0xff4a: // WY
						b = VC.WY;
						break;
					case 0xff4b: // WX
						b = VC.WX;
						break;
					case 0xff4f: // VRAM bank nr
						b = VC.getcurVRAMBank();
						break;
					case 0xff68: // BGPI
						b = VC.BGPI;
						break;
					case 0xff69: // BGPD
						b = VC.getBGColData();
						break;
					case 0xff6a: // OBPI
						b = VC.OBPI;
						break;
					case 0xff6b: // OBPD
						b = VC.getOBColData();
						break;
					default:
						System.out.printf("TODO: CPU.read(): Read from IO port $%04x\n",index);
						break;
				}
				//System.out.printf("TODO: CPU.read(): IOP[$%04x] = $%04x\n",index, b);
			}
			else if(index < 0xffff) { //High RAM (HRAM)
				b = HRAM[index-0xff80];
			}
			else if(index < 0x10000) { // Interrupt Enable Register (0xffff)
				//System.out.println("TODO: CPU.read(): Read from Interrupt Enable Register (0xffff)");
				b=IE;
			}
			else {
				System.out.println("ERROR: CPU.read(): Out of range memory access: $"+index);
				b=0;
			}
			return b;
		}

		final private void write(int index, int value) {
			/* Memorymap:
			 * 0000-3FFF   16KB ROM Bank 00     (in cartridge, fixed at bank 00)
			 * 4000-7FFF   16KB ROM Bank 01..NN (in cartridge, switchable bank number)
			 * 8000-9FFF   8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
			 * A000-BFFF   8KB External RAM     (in cartridge, switchable bank, if any)
			 * C000-CFFF   4KB Work RAM Bank 0 (WRAM)
			 * D000-DFFF   4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
			 * E000-FDFF   Same as C000-DDFF (ECHO)    (typically not used)
			 * FE00-FE9F   Sprite Attribute Table (OAM)
			 * FEA0-FEFF   Not Usable
			 * FF00-FF7F   I/O Ports
			 * FF80-FFFE   High RAM (HRAM)
			 * FFFF        Interrupt Enable Register
			 */
			curcycles+=4;
			if(index<0) { //Invalid
				System.out.println("ERROR: CPU.write(): No negative addresses in GameBoy memorymap.");
			}
			else if(index < 0x4000) { //16KB ROM Bank 00     (in cartridge, fixed at bank 00)
				cartridge.write(index, value);
			}
			else if(index < 0x8000) { //16KB ROM Bank 01..NN (in cartridge, switchable bank number)
				cartridge.write(index, value);
			}
			else if(index < 0xa000) { //8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
				VC.write(index, value);
			}
			else if(index < 0xc000) { //8KB External RAM     (in cartridge, switchable bank, if any)
				cartridge.write(index, value);
			}
			else if(index < 0xd000) { //4KB Work RAM Bank 0 (WRAM)
				WRAM[0][index-0xc000]=value;
			}
			else if(index < 0xe000) { //4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
				WRAM[CurrentWRAMBank][index-0xd000]=value;
			}
			else if(index < 0xfe00) { //Same as C000-DDFF (ECHO)    (typically not used)
				write(index-0x2000, value);
			}
			else if(index < 0xfea0) { //Sprite Attribute Table (OAM)
				VC.write(index, value);
			}
			else if(index < 0xff00) { //Not Usable
				System.out.println("TODO: CPU.write(): Write to unusable memory (0xfea-0xfeff)");
			}
			else if(index < 0xff80) { //I/O Ports
				switch(index) {
					case 0xff00: // FF00 - P1/JOYP - Joypad (R/W)
						IOP[index&0xff]=value;
						break;
					case 0xff04: // DIV
						IOP[0x04] = 0;
						break;
					case 0xff05: // TIMA
					case 0xff06: // TMA
					case 0xff07: // TAC
						IOP[index-0xff00] = value;
						break;
					case 0xff0f: // FF0F - IF - Interrupt Flag (R/W) (*Request* interrupts, and *shows* interrupts being queed)
						IOP[0x0f] = value;
						break;
					/* AUDIO IO PORTS */
					case 0xff10: case 0xff11: case 0xff12: case 0xff13: case 0xff14: case 0xff15: case 0xff16: case 0xff17:
					case 0xff18: case 0xff19: case 0xff1a: case 0xff1b: case 0xff1c: case 0xff1d: case 0xff1e: case 0xff1f:
					case 0xff20: case 0xff21: case 0xff22: case 0xff23: case 0xff24: case 0xff25: case 0xff26: case 0xff27:
					case 0xff28: case 0xff29: case 0xff2a: case 0xff2b: case 0xff2c: case 0xff2d: case 0xff2e: case 0xff2f:
					case 0xff30: case 0xff31: case 0xff32: case 0xff33: case 0xff34: case 0xff35: case 0xff36: case 0xff37:
					case 0xff38: case 0xff39: case 0xff3a: case 0xff3b: case 0xff3c: case 0xff3d: case 0xff3e: case 0xff3f:
						AC.write(index, value);
						break;
					case 0xff40: // LCDC register
						VC.LCDC = value;
						break;
					case 0xff41: // FF41 - STAT - LCDC Status (R/W)
						VC.STAT = (VC.STAT&7)|(value&~7); //lower 3bits are readonly
						break;
					case 0xff42: // SCY
						VC.SCY = value;
						break;
					case 0xff43: // SCX
						VC.SCX = value;
						break;
					case 0xff44: // LY
						VC.LY = 0; // can only be set to 0
						break;
					case 0xff45: // LYC
						VC.LYC = value;
						break;
					case 0xff46: // FF46 - DMA - DMA Transfer and Start Address (W)
						for(int i=0; i<0xa0; ++i){ //TODO : This takes TIME and needs TIMING
							write(0xfe00|i, read(i+(value<<8)));
						}
						curcycles = 4; // apps usually (and SHOULD) wait for this themselves
						break;
					case 0xff4a: // WY
						VC.WY = value;
						break;
					case 0xff4b: // WX
						VC.WX = value;
						break;
					case 0xff4f: // FF4F - VBK - CGB Mode Only - VRAM Bank
						VC.selectVRAMBank(value&1);
						break;
					case 0xff51: // FF51 - HDMA1 - CGB Mode Only - New DMA Source, High
					case 0xff52: // FF52 - HDMA2 - CGB Mode Only - New DMA Source, Low
					case 0xff53: // FF53 - HDMA3 - CGB Mode Only - New DMA Destination, High
					case 0xff54: // FF54 - HDMA4 - CGB Mode Only - New DMA Destination, Low
					case 0xff55: // FF55 - HDMA5 - CGB Mode Only - New DMA Length/Mode/Start
						System.out.println("TODO: CPU.write(): HDMA request for CGB mode (VRAM)");
						break;
					case 0xff68: // BGPI
						VC.BGPI = value;;
						break;
					case 0xff69: // BGPD
						VC.setBGColData(value);
						break;
					case 0xff6a: // OBPI
						VC.OBPI = value;;
						break;
					case 0xff6b: // OBPD
						VC.setOBColData(value);
						break;
					case 0xff70: //FF70 - SVBK - CGB Mode Only - WRAM Bank
						CurrentWRAMBank=Math.max(value&0x07, 1);
						break;
					default:
						System.out.printf("TODO: CPU.write(): Write to IO port $%04x\n",index);
						break;
				}
			}
			else if(index < 0xffff) { //High RAM (HRAM)
				HRAM[index-0xff80] = value;
			}
			else if(index < 0x10000) { // FFFF - IE - Interrupt Enable (R/W)
				//System.out.println("TODO: CPU.write(): Write to Interrupt Enable Register (0xffff)");
				IE=value; // Interrupt Enable Register
				//System.out.println("IE write: VBlank="+(IE&1)+" STAT="+((IE&2)>>1)+" Timer="+((IE&4)>>2)+" Serial="+((IE&8)>>3)+" Joypad="+((IE&16)>>4 ));
			}
			else {
				System.out.println("ERROR: CPU.write(): Out of range memory access: $"+index);
			}
		}

		final public void reset() {
			//TODO: Switch to bank 0
			PC = 0x100; //ROM Entry point on bank 0
			//AF=$01B0
			regs[A]=0x11; // CGB sets this to 0x11 instead of 0x01 for GB
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
			TotalCycleCount=0;

			//Stack Pointer=$FFFE
			SP=0xfffe;

			write(0xff05, 0x00); // [$FF05] = $00   ; TIMA
			write(0xff06, 0x00); // [$FF06] = $00   ; TMA
			write(0xff07, 0x00); // [$FF07] = $00   ; TAC
			write(0xff10, 0x80); // [$FF10] = $80   ; NR10
			write(0xff11, 0xbf); // [$FF11] = $BF   ; NR11
			write(0xff12, 0xf3); // [$FF12] = $F3   ; NR12
			write(0xff14, 0xbf); // [$FF14] = $BF   ; NR14
			write(0xff16, 0x3f); // [$FF16] = $3F   ; NR21
			write(0xff17, 0x00); // [$FF17] = $00   ; NR22
			write(0xff19, 0xbf); // [$FF19] = $BF   ; NR24
			write(0xff1a, 0x7f); // [$FF1A] = $7F   ; NR30
			write(0xff1b, 0xff); // [$FF1B] = $FF   ; NR31
			write(0xff1c, 0x9f); // [$FF1C] = $9F   ; NR32
			write(0xff1e, 0xbf); // [$FF1E] = $BF   ; NR33
			write(0xff20, 0xff); // [$FF20] = $FF   ; NR41
			write(0xff21, 0x00); // [$FF21] = $00   ; NR42
			write(0xff22, 0x00); // [$FF22] = $00   ; NR43
			write(0xff23, 0xbf); // [$FF23] = $BF   ; NR30
			write(0xff24, 0x77); // [$FF24] = $77   ; NR50
			write(0xff25, 0xf3); // [$FF25] = $F3   ; NR51
			write(0xff26, 0xf1); // [$FF26] = $F1-GB, $F0-SGB ; NR52
			write(0xff40, 0x91); // [$FF40] = $91   ; LCDC
			write(0xff42, 0x00); // [$FF42] = $00   ; SCY
			write(0xff43, 0x00); // [$FF43] = $00   ; SCX
			write(0xff45, 0x00); // [$FF45] = $00   ; LYC
			write(0xff47, 0xfc); // [$FF47] = $FC   ; BGP
			write(0xff48, 0xff); // [$FF48] = $FF   ; OBP0
			write(0xff49, 0xff); // [$FF49] = $FF   ; OBP1
			write(0xff4a, 0x00); // [$FF4A] = $00   ; WY
			write(0xff4b, 0x00); // [$FF4B] = $00   ; WX
			write(0xffff, 0x00); // [$FFFF] = $00   ; IE
		}

		final protected int cycles() {
			return TotalInstrCount;
		}

		final protected void printCPUstatus() {
			String flags = "";
			flags += (( regs[FLAG_REG] & ZF_Mask ) == ZF_Mask )?"Z ":"z ";
			flags += (( regs[FLAG_REG] & NF_Mask ) == NF_Mask )?"N ":"n ";
			flags += (( regs[FLAG_REG] & HC_Mask ) == HC_Mask )?"H ":"h ";
			flags += (( regs[FLAG_REG] & CF_Mask ) == CF_Mask )?"C ":"c ";
			flags += (( regs[FLAG_REG] & ( 1 <<3 ) ) == ( 1 <<3 ) )?"1 ":"0 ";
			flags += (( regs[FLAG_REG] & ( 1 <<2 ) ) == ( 1 <<2 ) )?"1 ":"0 ";
			flags += (( regs[FLAG_REG] & ( 1 <<1 ) ) == ( 1 <<1 ) )?"1 ":"0 ";
			flags += (( regs[FLAG_REG] & ( 1 <<0 ) ) == ( 1 <<0 ) )?"1 ":"0 ";
			System.out.println( "---CPU Status for cycle "+TotalCycleCount+" , instruction "+TotalInstrCount+"---" );
			System.out.printf( "   A=$%02x    B=$%02x    C=$%02x    D=$%02x   E=$%02x   F=$%02x   H=$%02x   L=$%02x\n", regs[A], regs[B], regs[C], regs[D], regs[E], regs[F], regs[H],regs[L] );
			System.out.printf( "  PC=$%04x SP=$%04x                           flags="+flags+"\n",PC,SP );
			System.out.println( "  "+deasm.disassemble( PC ) );
		}
		final protected int readmem8b( int H, int L ) {
			return read(( regs[H]<<8 )|regs[L] );
		}

		final protected void writemem8b( int H, int L, int val ) {
			write(( regs[H]<<8 )|regs[L], val );
		}

		final protected int checkInterrupts() { //handle interrupt priorities
			if(IME) { // If interrupts enabled
				int ir = IOP[0x0f]&IE; //First Requested interrupts
				if((ir&(1<<0))!=0) { //VBlANK
					IOP[0x0f] &= ~(1<<0);
					interrupt(0x40);
					return 1;
				}
				else if ((ir&(1<<1))!=0) { //LCD STAT
					IOP[0x0f] &= ~(1<<1);
					interrupt(0x48);
					return 1;
				}
				else if ((ir&(1<<2))!=0) { //Timer
					IOP[0x0f] &= ~(1<<2);
					interrupt(0x50);
					return 1;
				}
				else if ((ir&(1<<3))!=0) { //Serial
					IOP[0x0f] &= ~(1<<3);
					interrupt(0x58);
					return 1;
				}
				else if ((ir&(1<<4))!=0) { //Joypad
					IOP[0x0f] &= ~(1<<4);
					interrupt(0x60);
					return 1;
				}
			}
			return 0; // No interrupts to service
		}

		final protected void interrupt(int i) { //execute interrupt #i
			//System.out.println("INTERRUPT: " + i);
			IME = false;
			push(PC);
			PC = i;
		}

		final protected void triggerInterrupt(int i) { // request interrupt with bit nr #i
			IOP[0x0f] |= (1<<i);
		}

		final protected int shla(int value) {
			int res = value;
			res <<= 1;
			regs[F] = 0;
			regs[F] |= (res > 0xff) ? CF_Mask : 0;
			res &= 0xff;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int shra(int value) {
			int res = value;
			res >>= 1;
			regs[F] = 0;
			regs[F] |= ((value&1)!=0) ? CF_Mask : 0;
			res |= (value&(1<<7));
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int shrl(int value) {
			int res = value;
			res >>= 1;
			regs[F] = 0;
			regs[F] |= ((value&1)!=0) ? CF_Mask : 0;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int rol(int value) {
			int res = value;
			res <<= 1;
			res |= ((regs[F]&CF_Mask)==CF_Mask) ? 1 : 0;
			regs[F] = 0;
			regs[F] |= (res > 0xff) ? CF_Mask : 0;
			res &= 0xff;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int rolc(int value) {
			int res = value;
			res <<= 1;
			res |= (res > 0xff) ? 1 : 0;
			regs[F] = 0;
			regs[F] |= (res > 0xff) ? CF_Mask : 0;
			res &= 0xff;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int ror(int value) {
			int res = value;
			res >>= 1;
			res |= ((regs[F]&CF_Mask)==CF_Mask) ? 1<<7 : 0;
			regs[F] = 0;
			regs[F] |= ((value&1)==1) ? CF_Mask : 0;
			res &= 0xff;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected int rorc(int value) {
			int res = value;
			res >>= 1;
			res |= ((value&1)==1) ? 1<<7 : 0;
			regs[F] = 0;
			regs[F] |= ((value&1)==1) ? CF_Mask : 0;
			res &= 0xff;
			regs[F] |= (res == 0) ? ZF_Mask : 0;
			return res;
		}

		final protected void inc8b( int reg_index ) {
			// Clear & Set HC
			regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
			regs[FLAG_REG] = regs[FLAG_REG] | (((( regs[reg_index] & 0xF ) + 1 ) & 0x10 ) << 1 );

			//Update register
			regs[reg_index] = ( ++regs[reg_index] & 0xFF );

			// clear & set ZF
			regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
			regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index]==0 )?1:0 )<<ZF_Shift );

			// clear & set NF
			regs[FLAG_REG] = regs[FLAG_REG] & ~NF_Mask;
		}

		final protected void dec8b( int reg_index ) {
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

		final protected void inc16b(int ri1, int ri2 ) {
			curcycles += 4; // 16-bit add takes time
			// 16-bit inc/dec doesnt affect any flags
			++regs[ri2];
			if (regs[ri2]>0xFF) {
				regs[ri2]&=0xFF;
				++regs[ri1];
				regs[ri1]&=0xFF;
			}
		}

		final protected void dec16b(int ri1, int ri2 ) {
			curcycles += 4; // 16-bit add takes time
			// 16-bit inc/dec doesnt affect any flags
			--regs[ri2];
			if (regs[ri2]<0) {
				regs[ri2]&=0xFF;
				--regs[ri1];
				regs[ri1]&=0xFF;
			}
		}

		final protected void add8b( int dest, int val ) {
			// Clear all flags (including ZF)
			regs[FLAG_REG] = ( regs[FLAG_REG] & 0x00 );

			// Set HC
			regs[FLAG_REG] = regs[FLAG_REG] | ((((( regs[dest]&0x0f )+( val&0x0f ) )&0x10 )!=0?1:0 )<<HC_Shift );

			// Update register (part 1)
			regs[dest] = ( regs[dest] + val );

			// set CF
			regs[FLAG_REG] = regs[FLAG_REG] | ( regs[dest]>>8 )<<CF_Shift;

			// Clamp register (part 2)
			regs[dest]&=0xFF;

			// set ZF
			regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[dest]==0 )?1:0 )<<ZF_Shift );
		}

		final protected void sub8b( int dest, int val ) {
			// clear all flags except NF which is set
			regs[FLAG_REG] = NF_Mask;

			// Set HC
			regs[FLAG_REG] |= ((( regs[dest]&0x0F )-( val&0x0F ) )<0 ) ? HC_Mask : 0;

			// Update register (part 1)
			regs[dest] = ( regs[dest] - val );

			// set CF
			regs[FLAG_REG] |= ( regs[dest]<0 ) ? CF_Mask : 0;

			// Clamp register (part 2)
			regs[dest]&=0xFF;

			// set ZF
			regs[FLAG_REG] |= regs[dest]==0 ? ZF_Mask : 0;
		}

		final protected void add16bHL(int val1, int val2) {
			curcycles += 4; // 16-bit add takes time
			int fmask = regs[F] & ZF_Mask; // zero flag should be unaffected
			add8b(L, val2);
			fmask |= ((regs[F]&CF_Mask)==CF_Mask) ? HC_Mask : 0;
			adc(H, val1);
			regs[F] &= ~ZF_Mask;
			regs[F] &= ~HC_Mask;
			regs[F] |= fmask;
		}

		final protected void ld8b( int dest, int val ) {
			regs[dest] = val;
		}

		final protected void cp( int val ) {
			int i= regs[A];
			sub8b( A, val );
			regs[A] = i;
		}

		final protected void xor( int val ) {
			regs[F]=0;
			regs[A]^=val;
			regs[F]|=( regs[A]==0?ZF_Mask:0 );
		}

		final protected void or( int val ) {
			regs[F]=0;
			regs[A]|=val;
			regs[F]|=( regs[A]==0?ZF_Mask:0 );
		}

		final protected void and( int val ) {
			regs[F]=HC_Mask;
			regs[A]&=val;
			regs[F]|=( regs[A]==0?ZF_Mask:0 );
		}

		final protected void JPnn() {
			int i=read( PC++ );
			int j=read( PC++ );
			//System.out.println( "i="+i+ " j="+j );
			PC = j<<8|i; //Should be endian correct
		}

		final protected void sbc( int dest, int val ) {
			sub8b( dest, val+(( regs[FLAG_REG]&CF_Mask ) >> CF_Shift ) );
		}

		final protected void adc( int dest, int val ) {
			add8b( dest, val+(( regs[FLAG_REG]&CF_Mask ) >> CF_Shift ) );
		}

		final protected void push( int val ) {
			//Should be endian correct
			write( --SP, ( val>>8 )&0xff );
			write( --SP, val&0xff );
		}

		final protected int pop() {
			//Should be endian correct
			int l = read( SP++ );
			int h = read( SP++ );
			return (l | (h<<8));
		}

		static int nopCount=0;
		final private int execute() {
			curcycles = 0;
			boolean nop=false;
			//System.out.printf("Executing instruction $%02x\n", instr);
			if(checkInterrupts()!=0) {
				halted = false;
				return 12; // 12 cycles for this?
			}
			if (halted) return 4;
			int instr = read(PC++);
			switch ( instr ) {
				case 0x00:  // NOP
					nop=true;
					break;
				case 0x01: // LD BC, nn
					regs[C] = read( PC++ );
					regs[B] = read( PC++ );
					break;
				case 0x02: // LD (BC), A
					writemem8b(B,C, regs[A]);
					break;
				case 0x03: // INC BC
					inc16b(B, C);
					break;
				case 0x04:  // INC B
					inc8b( B );
					break;
				case 0x05:  // DEC B
					dec8b( B );
					break;
				case 0x06:  // LD  B, n
					regs[B] = read( PC++ );
					break;
				//case 0x08:  // LD (nn),SP
				//	uh? 16bit memwrite?
				case 0x07:  // RLCA
					regs[A] = rolc(regs[A]);
					regs[F] &= ~ZF_Mask;
					break;
				case 0x09:  // ADD HL, BC
					add16bHL(regs[B], regs[C]);
					break;
				case 0x0a:  // LD  A, (BC)
					regs[A] = readmem8b(B, C);
					break;
				case 0x0b:  // DEC BC
					dec16b(B, C);
					break;
				case 0x0c: // INC  C
					inc8b( C );
					break;
				case 0x0d: // DEC  C
					dec8b( C );
					break;
				case 0x0e:  // LD  C, n
					regs[C] = read( PC++ );
					break;
				case 0x0f:  // RRCA
					regs[A] = rorc(regs[A]);
					break;
				case 0x10: // STOP
					// pretend stop==halt
					if ((IE==0) || (!IME))
						System.out.println("PANIC: we will never unhalt!!!\n");
					halted = true;
					break;
				case 0x11: // LD DE, nn
					regs[E] = read( PC++ );
					regs[D] = read( PC++ );
					break;
				case 0x12: // LD (DE), A
					writemem8b(D,E, regs[A]);
					break;
				case 0x13: // INC DE
					inc16b(D, E);
					break;
				case 0x14: // INC  D
					inc8b( D );
					break;
				case 0x15: // DEC  D
					dec8b( D );
					break;
				case 0x16: // LD   D, n
					regs[D] = read( PC++ );
					break;
				case 0x17: // RLA
					regs[A] = ror(regs[A]);
					break;
				case 0x18:{// JR   &00
					int x = read( PC++ );
					PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
					curcycles += 4; // takes 12 instead of 8 cycles
				};break;
				case 0x19: // ADD HL, DE
					add16bHL(regs[D], regs[E]);
					break;
				case 0x1a: // LD A, (DE)
					regs[A] = readmem8b(D, E);
					break;
				case 0x1b: // DEC DE
					dec16b(D, E);
					break;
				case 0x1c: // INC  E
					inc8b( E );
					break;
				case 0x1d: // DEC  E
					dec8b( E );
					break;
				case 0x1e: // LD   E, n
					regs[E] = read( PC++ );
					break;
				case 0x1f: // RRA
					regs[A] = ror(regs[A]);
					break;
				case 0x20: // JR NZ, n
					curcycles += 4; // takes 12;8 instead of 8;4 cycles
					if (( regs[F]&ZF_Mask )!=ZF_Mask ) {
						int x = read( PC++ );
						PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
					}
					else ++PC;
					break;
				case 0x21: // LD HL, nn
					regs[L] = read( PC++ );
					regs[H] = read( PC++ );
					break;
				case 0x22: // LDI (HL), A
					writemem8b(H,L, regs[A]);
					inc16b(H, L);
					break;
				case 0x23: // INC HL
					inc16b(H, L);
					break;
				case 0x24: // INC H
					inc8b(H);
					break;
				case 0x25: // DEC H
					dec8b(H);
					break;
				case 0x26: // LD   H, n
					regs[H] = read( PC++ );
					break;
				//case 0x27: // DAA
				//	uhoh.... evil instr?
				case 0x28: // JR   Z, n
					curcycles += 4; // takes 12;8 instead of 8;4 cycles
					if (( regs[F]&ZF_Mask )==ZF_Mask ) {
						int x = read( PC++ );
						PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
					}
					else ++PC;
					break;
				case 0x29: // ADD HL, HL
					add16bHL(regs[H], regs[L]);
					break;
				case 0x2a: // LDI A, (HL)
					regs[A] = readmem8b(H, L);
					inc16b(H, L);
					break;
				case 0x2b:  // DEC HL
					dec16b(H, L);
					break;
				case 0x2c:  // INC L
					inc8b( L );
					break;
				case 0x2d:  // DEC  L
					dec8b( L );
					break;
				case 0x2e:  // LD   L, n
					regs[L] = read( PC++ );
					break;
				case 0x2f:  // CPL
					xor( 0xFF );
					break;
				case 0x30: // JR NC, n
					curcycles += 4; // takes 12;8 instead of 8;4 cycles
					if (( regs[F]&CF_Mask )!=CF_Mask ) {
						int x = read( PC++ );
						PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
					}
					else ++PC;
					break;
				case 0x31:{// LD SP, nn
					int l = read( PC++ );
					int h = read( PC++ );
					SP = l | (h<<8);
				};break;
				case 0x32: // LDD	(HL), A
					writemem8b(H,L, regs[A]);
					dec16b(H, L);
					break;
				case 0x33: // INC SP
					++SP; //16-bit inc/dec doesnt affect any flags
					SP&=0xffff;
					break;
				case 0x34:{// INC (HL)
					// this is a tad hacky? TODO?
					int x = regs[A];
					regs[A] = readmem8b(H, L);
					inc8b(A);
					writemem8b(H,L, regs[A]);
					regs[A] = x;
				};break;
				case 0x35:{// DEC (HL)
					// this is a tad hacky? TODO?
					int x = regs[A];
					regs[A] = readmem8b(H, L);
					dec8b(A);
					writemem8b(H,L, regs[A]);
					regs[A] = x;
				};break;
				case 0x36: // LD (HL), n
					writemem8b(H,L, read(PC++));
					break;
				case 0x37: // SCF
					regs[F] &= ZF_Mask;
					regs[F] |= CF_Mask;
					break;
				case 0x38: // JR C, n
					curcycles += 4; // takes 12;8 instead of 8;4 cycles
					if (( regs[F]&CF_Mask )==CF_Mask ) {
						int x = read( PC++ );
						PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
					}
					else ++PC;
					break;
				case 0x39: // ADD HL, SP
					add16bHL(SP >> 8, SP & 0xff);
					break;
				case 0x3a: // LDD A, (HL)
					regs[A] = readmem8b(H, L);
					dec16b(H, L);
					break;
				case 0x3b: // DEC SP
					--SP; //16-bit inc/dec doesnt affect any flags
					SP&=0xffff;
					break;
				case 0x3c: // INC A
					inc8b(A);
					break;
				case 0x3d: // DEC A
					dec8b(A);
					break;
				case 0x3e: // LD A, n
					regs[A]=read( PC++ );
					break;
				case 0x3f: // CCF
					regs[F] ^= CF_Mask;
					break;
				case 0x40: // LD B, B
					ld8b( B, regs[B] );
					break;
				case 0x41: // LD B, C
					ld8b( B, regs[C] );
					break;
				case 0x42: // LD   B,D
					ld8b( B, regs[D] );
					break;
				case 0x43: // LD   B,E
					ld8b( B, regs[E] );
					break;
				case 0x44: // LD   B,H
					ld8b( B, regs[H] );
					break;
				case 0x45: // LD   B,L
					ld8b( B, regs[L] );
					break;
				case 0x46: // LD   B,(HL)
					ld8b( B, readmem8b( H,L ) );
					break;
				case 0x47: // LD   B,A
					ld8b( B, regs[A] );
					break;
				case 0x48: // LD   C,B
					ld8b( C, regs[B] );
					break;
				case 0x49: // LD   C,C
					ld8b( C, regs[C] );
					break;
				case 0x4a: // LD   C,D
					ld8b( C, regs[D] );
					break;
				case 0x4b: // LD   C,E
					ld8b( C, regs[E] );
					break;
				case 0x4c: // LD   C,H
					ld8b( C, regs[H] );
					break;
				case 0x4d: // LD   C,L
					ld8b( C, regs[L] );
					break;
				case 0x4e: // LD   C,(HL)
					ld8b( C, readmem8b( H,L ) );
					break;
				case 0x4f: // LD   C,A
					ld8b( C, regs[A] );
					break;
				case 0x50: // LD   D,B
					ld8b( D, regs[B] );
					break;
				case 0x51: // LD   D,C
					ld8b( D, regs[C] );
					break;
				case 0x52: // LD   D,D
					ld8b( D, regs[D] );
					break;
				case 0x53: // LD   D,E
					ld8b( D, regs[E] );
					break;
				case 0x54: // LD   D,H
					ld8b( D, regs[H] );
					break;
				case 0x55: // LD   D,L
					ld8b( D, regs[L] );
					break;
				case 0x56: // LD   D,(HL)
					ld8b( D, readmem8b( H,L ) );
					break;
				case 0x57: // LD   D,A
					ld8b( D, regs[A] );
					break;
				case 0x58: // LD   E,B
					ld8b( E, regs[B] );
					break;
				case 0x59: // LD   E,C
					ld8b( E, regs[C] );
					break;
				case 0x5a: // LD   E,D
					ld8b( E, regs[D] );
					break;
				case 0x5b: // LD   E,E
					ld8b( E, regs[E] );
					break;
				case 0x5c: // LD   E,H
					ld8b( E, regs[H] );
					break;
				case 0x5d: // LD   E,L
					ld8b( E, regs[L] );
					break;
				case 0x5e: // LD   E,(HL)
					ld8b( E, readmem8b( H,L ) );
					break;
				case 0x5f: // LD   E,A
					ld8b( E, regs[A] );
					break;
				case 0x60: // LD   H,B
					ld8b( H, regs[B] );
					break;
				case 0x61: // LD   H,C
					ld8b( H, regs[C] );
					break;
				case 0x62: // LD   H,D
					ld8b( H, regs[D] );
					break;
				case 0x63: // LD   H,E
					ld8b( H, regs[E] );
					break;
				case 0x64: // LD   H,H
					ld8b( H, regs[H] );
					break;
				case 0x65: // LD   H,L
					ld8b( H, regs[L] );
					break;
				case 0x66: // LD   H,(HL)
					ld8b( H, readmem8b( H,L ) );
					break;
				case 0x67: // LD   H,A
					ld8b( H, regs[A] );
					break;
				case 0x68: // LD   L,B
					ld8b( L, regs[B] );
					break;
				case 0x69: // LD   L,C
					ld8b( L, regs[C] );
					break;
				case 0x6a: // LD   L,D
					ld8b( L, regs[D] );
					break;
				case 0x6b: // LD   L,E
					ld8b( L, regs[E] );
					break;
				case 0x6c: // LD   L,H
					ld8b( L, regs[H] );
					break;
				case 0x6d: // LD   L,L
					ld8b( L, regs[L] );
					break;
				case 0x6e: // LD   L,(HL)
					ld8b( L, readmem8b( H,L ) );
					break;
				case 0x6f: // LD   L,A
					ld8b( L, regs[A] );
					break;
				case 0x70: // LD   (HL),B
					writemem8b(H,L, regs[B]);
					break;
				case 0x71: // LD   (HL),C
					writemem8b(H,L, regs[C]);
					break;
				case 0x72: // LD   (HL),D
					writemem8b(H,L, regs[D]);
					break;
				case 0x73: // LD   (HL),E
					writemem8b(H,L, regs[E]);
					break;
				case 0x74: // LD   (HL),H
					writemem8b(H,L, regs[H]);
					break;
				case 0x75: // LD   (HL),L
					writemem8b(H,L, regs[L]);
					break;
				case 0x76: // HALT
					if ((IE==0) || (!IME))
						System.out.println("PANIC: we will never unhalt!!!\n");
					halted = true;
					break;
				case 0x77: // LD   (HL),A
					writemem8b(H,L, regs[A]);
					break;
				case 0x78: // LD   A,B
					ld8b( A, regs[B] );
					break;
				case 0x79: // LD   A,C
					ld8b( A, regs[C] );
					break;
				case 0x7a: // LD   A,D
					ld8b( A, regs[D] );
					break;
				case 0x7b: // LD   A,E
					ld8b( A, regs[E] );
					break;
				case 0x7c: // LD   A,H
					ld8b( A, regs[H] );
					break;
				case 0x7d: // LD   A,L
					ld8b( A, regs[L] );
					break;
				case 0x7e: // LD   A,(HL)
					ld8b( A, readmem8b( H,L ) );
					break;
				case 0x7f: // LD   A,A
					ld8b( A, regs[A] );
					break;
				case 0x80: // ADD  A,B
					add8b( A, regs[B] );
					break;
				case 0x81: // ADD  A,C
					add8b( A, regs[C] );
					break;
				case 0x82: // ADD  A,D
					add8b( A, regs[D] );
					break;
				case 0x83: // ADD  A,E
					add8b( A, regs[E] );
					break;
				case 0x84: // ADD  A,H
					add8b( A, regs[H] );
					break;
				case 0x85: // ADD  A,L
					add8b( A, regs[L] );
					break;
				case 0x86: // ADD  A,(HL)
					add8b( A, readmem8b( H,L ) );
					break;
				case 0x87: // ADD  A,A
					add8b( A, regs[A] );
					break;
				case 0x88: // ADC  A,B
					adc( A, regs[B] );
					break;
				case 0x89: // ADC  A,C
					adc( A, regs[C] );
					break;
				case 0x8a: // ADC  A,D
					adc( A, regs[D] );
					break;
				case 0x8b: // ADC  A,E
					adc( A, regs[E] );
					break;
				case 0x8c: // ADC  A,H
					adc( A, regs[H] );
					break;
				case 0x8d: // ADC  A,L
					adc( A, regs[L] );
					break;
				case 0x8e: // ADC  A,(HL)
					adc( A, readmem8b( H,L ) );
					break;
				case 0x8f: // ADC  A,A
					adc( A, regs[A] );
					break;
				case 0x90: // SUB  A,B
					sub8b(A, regs[B]);
					break;
				case 0x91: // SUB  A,C
					sub8b(A, regs[C]);
					break;
				case 0x92: // SUB  A,D
					sub8b(A, regs[D]);
					break;
				case 0x93: // SUB  A,E
					sub8b(A, regs[E]);
					break;
				case 0x94: // SUB  A,H
					sub8b(A, regs[H]);
					break;
				case 0x95: // SUB  A,L
					sub8b(A, regs[L]);
					break;
				case 0x96: // SUB  A,(HL)
					sub8b(A, readmem8b(H, L));
					break;
				case 0x97: // SUB  A,A
					sub8b(A, regs[A]);
					break;
				case 0x98: // SBC  A,B
					sbc( A, regs[B] );
					break;
				case 0x99: // SBC  A,C
					sbc( A, regs[C] );
					break;
				case 0x9a: // SBC  A,D
					sbc( A, regs[D] );
					break;
				case 0x9b: // SBC  A,E
					sbc( A, regs[E] );
					break;
				case 0x9c: // SBC  A,H
					sbc( A, regs[H] );
					break;
				case 0x9d: // SBC  A,L
					sbc( A, regs[L] );
					break;
				case 0x9e: // SBC  A,(HL)
					sbc( A, readmem8b( H,L ) );
					break;
				case 0x9f: // SBC  A,A
					sbc( A, regs[A] );
					break;
				case 0xa0: // AND B
					and( regs[B] );
					break;
				case 0xa1: // AND C
					and( regs[C] );
					break;
				case 0xa2: // AND D
					and( regs[D] );
					break;
				case 0xa3: // AND E
					and( regs[E] );
					break;
				case 0xa4: // AND H
					and( regs[H] );
					break;
				case 0xa5: // AND L
					and( regs[L] );
					break;
				case 0xa6: // AND (HL)
					and( readmem8b( H,L ) );
					break;
				case 0xa7: // AND A
					and( regs[A] );
					break;
				case 0xa8: // XOR B
					xor( regs[B] );
					break;
				case 0xa9: // XOR C
					xor( regs[C] );
					break;
				case 0xaa: // XOR D
					xor( regs[D] );
					break;
				case 0xab: // XOR E
					xor( regs[E] );
					break;
				case 0xac: // XOR H
					xor( regs[H] );
					break;
				case 0xad: // XOR L
					xor( regs[L] );
					break;
				case 0xae: // XOR (HL)
					xor( readmem8b( H,L ) );
					break;
				case 0xaf: // XOR A
					xor( regs[A] );
					break;
				case 0xb0: // OR  B
					or( regs[B] );
					break;
				case 0xb1: // OR  C
					or( regs[C] );
					break;
				case 0xb2: // OR  D
					or( regs[D] );
					break;
				case 0xb3: // OR  E
					or( regs[E] );
					break;
				case 0xb4: // OR  H
					or( regs[H] );
					break;
				case 0xb5: // OR  L
					or( regs[L] );
					break;
				case 0xb6: // OR  (HL)
					or( readmem8b( H,L ) );
					break;
				case 0xb7: // OR  A
					or( regs[A] );
					break;
				case 0xb8: // CP   B
					cp( regs[B] );
					break;
				case 0xb9: // CP   C
					cp( regs[C] );
					break;
				case 0xba: // CP   D
					cp( regs[D] );
					break;
				case 0xbb: // CP   E
					cp( regs[E] );
					break;
				case 0xbc: // CP   H
					cp( regs[H] );
					break;
				case 0xbd: // CP   L
					cp( regs[L] );
					break;
				case 0xbe: // CP   (HL)
					cp( readmem8b( H,L ) );
					break;
				case 0xbf: // CP   A
					cp( regs[A] );
					break;
				case 0xc0: // RET  NZ
					if ((regs[F]&ZF_Mask) != ZF_Mask)
						PC = pop();
					if (curcycles == 8) curcycles = 20;
					if (curcycles == 4) curcycles =  8;
					break;
				case 0xc1:{// POP BC
					int x = pop();
					regs[B] = x >> 8;
					regs[C] = x&0xff;
				};break;
				case 0xc2: // JMP NZ, nn
					if (( regs[FLAG_REG]&ZF_Mask )!=ZF_Mask )
						JPnn();
					else
						PC+=2;
					if (curcycles == 12) curcycles = 16;
					if (curcycles ==  4) curcycles = 12;
					break;
				case 0xc3: // JPNNNN
					JPnn();
					curcycles += 4; // takes 16 instead of 12 cycles
					break;
				case 0xc4: // CALL NZ, &0000
					if (( regs[FLAG_REG]&ZF_Mask )!=ZF_Mask ) {
						push( PC+2 );
						JPnn();
					} else
						PC += 2;
					if (curcycles == 20) curcycles = 24;
					if (curcycles ==  4) curcycles = 12;
					break;
				case 0xc5: // PUSH BC
					push( regs[B]<<8 | regs[C]);
					curcycles += 4; // takes 16 instead of 12 cycles
					break;
				case 0xc6: // ADD  A, n
					add8b(A, read(PC++));
					break;
				case 0xc7: // RST &00
					push(PC);
					PC = 0x00;
					break;
				case 0xc8: // RET  Z
					if ((regs[F]&ZF_Mask) == ZF_Mask)
						PC = pop();
					if (curcycles == 8) curcycles = 20;
					if (curcycles == 4) curcycles =  8;
					break;
				case 0xc9: // RET
					curcycles += 4; // takes 16 instead of 12 cycles
					PC = pop();
					break;
				case 0xca: // JMP Z, nn
					if (( regs[FLAG_REG]&ZF_Mask )==ZF_Mask )
						JPnn();
					else
						PC+=2;
					if (curcycles == 12) curcycles = 16;
					if (curcycles ==  4) curcycles = 12;
					break;
				//case 0xcb: prefix
				//	prefix instrs are at the end
				case 0xcc: // CALL Z, &0000
					if (( regs[FLAG_REG]&ZF_Mask )==ZF_Mask ) {
						push( PC+2 );
						JPnn();
					} else
						PC += 2;
					if (curcycles == 20) curcycles = 24;
					if (curcycles ==  4) curcycles = 12;
					break;
				case 0xcd: // CALL &0000
					curcycles += 4; // takes 24 instead of 20 cycles
					push( PC+2 );
					JPnn();
					break;
				case 0xce: // ADC  A, n
					adc(A, read(PC++));
					break;
				case 0xcf: // RST &08
					push(PC);
					PC = 0x08;
					break;
				case 0xd0: // RET  NC
					if ((regs[F]&CF_Mask) != CF_Mask)
						PC = pop();
					if (curcycles == 8) curcycles = 20;
					if (curcycles == 4) curcycles =  8;
					break;
				case 0xd1:{// POP DE
					int x = pop();
					regs[D] = x >> 8;
					regs[E] = x&0xff;
				};break;
				case 0xd2: // JMP NC, nn
					if (( regs[FLAG_REG]&CF_Mask )!=CF_Mask )
						JPnn();
					else
						PC+=2;
					if (curcycles == 12) curcycles = 16;
					if (curcycles ==  4) curcycles = 12;
					break;
				//case 0xd3
				//	missing instruction
				case 0xd4: // CALL NC, &0000
					if (( regs[FLAG_REG]&CF_Mask )==0 ) {
						push( PC+2 );
						JPnn();
					} else
						PC += 2;
					if (curcycles == 20) curcycles = 24;
					if (curcycles ==  4) curcycles = 12;
					break;
				case 0xd5: // PUSH DE
					push( regs[D]<<8 | regs[E]);
					curcycles += 4; // takes 16 instead of 12 cycles
					break;
				case 0xd6: // SUB A, n
					sub8b(A, read(PC++));
					break;
				case 0xd7: // RST &10
					push(PC);
					PC = 0x10;
					break;
				case 0xd8: // RET  C
					if ((regs[F]&CF_Mask) == CF_Mask)
						PC = pop();
					if (curcycles == 8) curcycles = 20;
					if (curcycles == 4) curcycles =  8;
					break;
				case 0xd9: // RETI
					curcycles += 4; // takes 16 instead of 12 cycles
					IME = true;
					PC = pop();
					break;
				case 0xda: // JMP C,&0000
					if (( regs[FLAG_REG]&CF_Mask )!= 0 )
						JPnn();
					else
						PC+=2;
					if (curcycles == 12) curcycles = 16;
					if (curcycles ==  4) curcycles = 12;
					break;
				//case 0xdb:
				//	missing instruction
				case 0xdc: // CALL C, &0000
					if (( regs[FLAG_REG]&CF_Mask )!=0 ) {
						push( PC+2 );
						JPnn();
					} else
						PC += 2;
					if (curcycles == 20) curcycles = 24;
					if (curcycles ==  4) curcycles = 12;
					break;
				//case 0xdd:
				//	missing instruction
				case 0xde: // SBC A, n
					sbc(A, read(PC++));
					break;
				case 0xdf: // RST &18
					push(PC);
					PC = 0x18;
					break;
				case 0xe0: // LDH  (n), A
					write( 0xff00 | read( PC++ ), regs[A] );
					break;
				case 0xe1:{// POP HL
					int x = pop();
					regs[H] = x >> 8;
					regs[L] = x&0xff;
				};break;
				case 0xe2: // LD (C), A
					write( 0xff00 | regs[C], regs[A] );
					break;
				//case 0xe3:
				//	missing instruction
				//case 0xe4:
				//	missing instruction
				case 0xe5: // PUSH HL
					push( regs[H]<<8 | regs[L]);
					curcycles += 4; // takes 16 instead of 12 cycles
					break;
				case 0xe6: // AND nn
					and(read(PC++));
					break;
				case 0xe7: // RST &20
					push(PC);
					PC = 0x20;
					break;
				case 0xe8:{// ADD SP,dd
					int o = SP;
					int x = read(PC++);
					x ^= 0x80;
					x -= 0x80;
					SP += x;
					regs[F] = 0;
					if ((SP & ~0xffff) != 0) {
						SP &= 0xffff;
						regs[F] |= CF_Mask;
					}
					if ((SP >> 8) != (o >> 8))
						regs[F] |= HC_Mask;
					curcycles += 8; // takes 16 instead of 8 cycles
				};break;
				case 0xe9: // JP  HL
					PC = (regs[H]<<8) | regs[L];
					break;
				case 0xea:{// LD (nnnn), A
					int a = read( PC++ );
					int b = read( PC++ );
					write((b<<8) | a, regs[A] );
				};break;
				//case 0xeb:
				//	missing instruction
				//case 0xec:
				//	missing instruction
				//case 0xed:
				//	missing instruction
				case 0xee: // XOR   &00
					xor( read( PC++ ) );
					break;
				case 0xef: // RST &28
					push(PC);
					PC = 0x28;
					break;
				case 0xf0: // LDH A, (n)
					regs[A] = read( 0xff00 | read( PC++ ) );
					break;
				case 0xf1:{// POP AF
					int x = pop();
					regs[A] = x >> 8;
					regs[F] = x&0xff;
				};break;
				case 0xf2: // LD  A, (C)
					// is really LDH?
					regs[A] = read( 0xff00 | regs[C] );
					break;
				case 0xf3: // DI
					IME = false;
					break;
				//case 0xf4:
				//	missing instruction
				case 0xf5: // PUSH AF
					push( regs[A]<<8 | regs[F]);
					curcycles += 4; // takes 16 instead of 12 cycles
					break;
				case 0xf6: // OR  n
					or(read(PC++));
					break;
				case 0xf7: // RST &30
					push(PC);
					PC = 0x30;
					break;
				case 0xf8:{// LD  HL, SP+dd
					regs[H] = SP >> 8;
					regs[L] = SP & 0xff;
					int x = read(PC++);
					x ^= 0x80;
					x -= 0x80;
					if (x>=0)
						add8b(L, x);
					else
						sub8b(L, -x);
					int fmask = ((regs[F]&CF_Mask)==CF_Mask) ? HC_Mask : 0;
					if (x>=0)
						adc(H, 0);
					else
						sbc(H, 0);
					regs[F] &= CF_Mask;
					regs[F] |= fmask;

					curcycles += 4; // takes 12 instead of 8 cycles
				};break;
				case 0xf9: // LD SP, HL
					SP = regs[H]<<8 | regs[L];
					curcycles += 4; // takes 8 instead of 4 cycles
					break;
				case 0xfa:{// LD A, (nn)
					int a = read( PC++ );
					int b = read( PC++ );
					regs[A] = read((b<<8) | a);
				};break;
				case 0xfb: // EI
					IME = true;
					break;
				//case 0xfc:
				//	missing instruction
				//case 0xfd:
				//	missing instruction
				case 0xfe: // CP n
					cp( read( PC++ ) );
					break;
				case 0xff: // RST &38
					push(PC);
					PC = 0x38;
					break;
				case 0xcb: // prefix instruction
					instr = read( PC++ );
					switch ( instr ) {
						case 0x00: // RLC  B
							regs[B] = rolc(regs[B]);
							break;
						case 0x01: // RLC  C
							regs[C] = rolc(regs[C]);
							break;
						case 0x02: // RLC  D
							regs[D] = rolc(regs[D]);
							break;
						case 0x03: // RLC  E
							regs[E] = rolc(regs[E]);
							break;
						case 0x04: // RLC  H
							regs[H] = rolc(regs[H]);
							break;
						case 0x05: // RLC  L
							regs[L] = rolc(regs[L]);
							break;
						case 0x06: // RLC  (HL)
							writemem8b(H, L, rolc(readmem8b(H, L)));
							break;
						case 0x07: // RLC  A
							regs[A] = rolc(regs[A]);
							break;
						case 0x08: // RRC  B
							regs[B] = rorc(regs[B]);
							break;
						case 0x09: // RRC  C
							regs[C] = rorc(regs[C]);
							break;
						case 0x0a: // RRC  D
							regs[D] = rorc(regs[D]);
							break;
						case 0x0b: // RRC  E
							regs[E] = rorc(regs[E]);
							break;
						case 0x0c: // RRC  H
							regs[H] = rorc(regs[H]);
							break;
						case 0x0d: // RRC  L
							regs[L] = rorc(regs[L]);
							break;
						case 0x0e: // RRC  (HL)
							writemem8b(H, L, rorc(readmem8b(H, L)));
							break;
						case 0x0f: // RRC  A
							regs[A] = rorc(regs[A]);
							break;
						case 0x10: // RL  B
							regs[B] = rol(regs[B]);
							break;
						case 0x11: // RL  C
							regs[C] = rol(regs[C]);
							break;
						case 0x12: // RL  D
							regs[D] = rol(regs[D]);
							break;
						case 0x13: // RL  E
							regs[E] = rol(regs[E]);
							break;
						case 0x14: // RL  H
							regs[H] = rol(regs[H]);
							break;
						case 0x15: // RL  L
							regs[L] = rol(regs[L]);
							break;
						case 0x16: // RL  (HL)
							writemem8b(H, L, rol(readmem8b(H, L)));
							break;
						case 0x17: // RL  A
							regs[A] = rol(regs[A]);
							break;
						case 0x18: // RR  B
							regs[B] = ror(regs[B]);
							break;
						case 0x19: // RR  C
							regs[C] = ror(regs[C]);
							break;
						case 0x1a: // RR  D
							regs[D] = ror(regs[D]);
							break;
						case 0x1b: // RR  E
							regs[E] = ror(regs[E]);
							break;
						case 0x1c: // RR  H
							regs[H] = ror(regs[H]);
							break;
						case 0x1d: // RR  L
							regs[L] = ror(regs[L]);
							break;
						case 0x1e: // RR  (HL)
							writemem8b(H, L, ror(readmem8b(H, L)));
							break;
						case 0x1f: // RR  A
							regs[A] = ror(regs[A]);
							break;
						case 0x20: // SLA  B
							regs[B] = shla(regs[B]);
							break;
						case 0x21: // SLA  C
							regs[C] = shla(regs[C]);
							break;
						case 0x22: // SLA  D
							regs[D] = shla(regs[D]);
							break;
						case 0x23: // SLA  E
							regs[E] = shla(regs[E]);
							break;
						case 0x24: // SLA  H
							regs[H] = shla(regs[H]);
							break;
						case 0x25: // SLA  L
							regs[L] = shla(regs[L]);
							break;
						case 0x26: // SLA  (HL)
							writemem8b(H, L, shla(readmem8b(H, L)));
							break;
						case 0x27: // SLA  A
							regs[A] = shla(regs[A]);
							break;
						case 0x28: // SRA  B
							regs[B] = shra(regs[B]);
							break;
						case 0x29: // SRA  C
							regs[C] = shra(regs[C]);
							break;
						case 0x2a: // SRA  D
							regs[D] = shra(regs[D]);
							break;
						case 0x2b: // SRA  E
							regs[E] = shra(regs[E]);
							break;
						case 0x2c: // SRA  H
							regs[H] = shra(regs[H]);
							break;
						case 0x2d: // SRA  L
							regs[L] = shra(regs[L]);
							break;
						case 0x2e: // SRA  (HL)
							writemem8b(H, L, shra(readmem8b(H, L)));
							break;
						case 0x2f: // SRA  A
							regs[A] = shra(regs[A]);
							break;
						case 0x30: // SWAP B
							regs[B] = ((regs[B]&0x0f)<< 4) | ((regs[B]&0xf0) >> 4);
							break;
						case 0x31: // SWAP C
							regs[C] = ((regs[C]&0x0f)<< 4) | ((regs[C]&0xf0) >> 4);
							break;
						case 0x32: // SWAP D
							regs[D] = ((regs[D]&0x0f)<< 4) | ((regs[D]&0xf0) >> 4);
							break;
						case 0x33: // SWAP E
							regs[E] = ((regs[E]&0x0f)<< 4) | ((regs[E]&0xf0) >> 4);
							break;
						case 0x34: // SWAP H
							regs[H] = ((regs[H]&0x0f)<< 4) | ((regs[H]&0xf0) >> 4);
							break;
						case 0x35: // SWAP L
							regs[L] = ((regs[L]&0x0f)<< 4) | ((regs[L]&0xf0) >> 4);
							break;
						case 0x36:{// SWAP (HL)
							int x = readmem8b(H, L);
							x = ((x&0x0f)<< 4) | ((x&0xf0) >> 4);
							writemem8b(H,L, x);
						};break;
						case 0x37: // SWAP A
							regs[A] = ((regs[A]&0x0f)<< 4) | ((regs[A]&0xf0) >> 4);
							break;
						case 0x38: // SRL  B
							regs[B] = shrl(regs[B]);
							break;
						case 0x39: // SRL  C
							regs[C] = shrl(regs[C]);
							break;
						case 0x3a: // SRL  D
							regs[D] = shrl(regs[D]);
							break;
						case 0x3b: // SRL  E
							regs[E] = shrl(regs[E]);
							break;
						case 0x3c: // SRL  H
							regs[H] = shrl(regs[H]);
							break;
						case 0x3d: // SRL  L
							regs[L] = shrl(regs[L]);
							break;
						case 0x3e: // SRL  (HL)
							writemem8b(H, L, shrl(readmem8b(H, L)));
							break;
						case 0x3f: // SRL  A
							regs[A] = shrl(regs[A]);
							break;
						case 0x40: // BIT 0,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x41: // BIT 0,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x42: // BIT 0,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x43: // BIT 0,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x44: // BIT 0,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x45: // BIT 0,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x46: // BIT 0,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x47: // BIT 0,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<0))==0 ? ZF_Mask : 0;
							break;
						case 0x48: // BIT 1,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x49: // BIT 1,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4a: // BIT 1,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4b: // BIT 1,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4c: // BIT 1,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4d: // BIT 1,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4e: // BIT 1,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x4f: // BIT 1,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<1))==0 ? ZF_Mask : 0;
							break;
						case 0x50: // BIT 2,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x51: // BIT 2,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x52: // BIT 2,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x53: // BIT 2,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x54: // BIT 2,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x55: // BIT 2,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x56: // BIT 2,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x57: // BIT 2,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<2))==0 ? ZF_Mask : 0;
							break;
						case 0x58: // BIT 3,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x59: // BIT 3,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5a: // BIT 3,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5b: // BIT 3,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5c: // BIT 3,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5d: // BIT 3,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5e: // BIT 3,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x5f: // BIT 3,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<3))==0 ? ZF_Mask : 0;
							break;
						case 0x60: // BIT 4,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x61: // BIT 4,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x62: // BIT 4,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x63: // BIT 4,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x64: // BIT 4,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x65: // BIT 4,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x66: // BIT 4,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x67: // BIT 4,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<4))==0 ? ZF_Mask : 0;
							break;
						case 0x68: // BIT 5,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x69: // BIT 5,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6a: // BIT 5,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6b: // BIT 5,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6c: // BIT 5,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6d: // BIT 5,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6e: // BIT 5,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x6f: // BIT 5,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<5))==0 ? ZF_Mask : 0;
							break;
						case 0x70: // BIT 6,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x71: // BIT 6,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x72: // BIT 6,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x73: // BIT 6,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x74: // BIT 6,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x75: // BIT 6,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x76: // BIT 6,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x77: // BIT 6,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<6))==0 ? ZF_Mask : 0;
							break;
						case 0x78: // BIT 7,B
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[B]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x79: // BIT 7,C
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[C]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7a: // BIT 7,D
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[D]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7b: // BIT 7,E
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[E]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7c: // BIT 7,H
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[H]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7d: // BIT 7,L
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[L]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7e: // BIT 7,(HL)
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (readmem8b(H, L)&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x7f: // BIT 7,A
							regs[F] &= CF_Mask;
							regs[F] |= HC_Mask;
							regs[F] |= (regs[A]&(1<<7))==0 ? ZF_Mask : 0;
							break;
						case 0x80: // RES 0,B
							regs[B] &= ~( 1 << 0 );
							break;
						case 0x81: // RES 0,C
							regs[C] &= ~( 1 << 0 );
							break;
						case 0x82: // RES 0,D
							regs[D] &= ~( 1 << 0 );
							break;
						case 0x83: // RES 0,E
							regs[E] &= ~( 1 << 0 );
							break;
						case 0x84: // RES 0,H
							regs[H] &= ~( 1 << 0 );
							break;
						case 0x85: // RES 0,L
							regs[L] &= ~( 1 << 0 );
							break;
						case 0x86: // RES 0,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 0 ) );
							break;
						case 0x87: // RES 0,A
							regs[A] &= ~( 1 << 0 );
							break;
						case 0x88: // RES 1,B
							regs[B] &= ~( 1 << 1 );
							break;
						case 0x89: // RES 1,C
							regs[C] &= ~( 1 << 1 );
							break;
						case 0x8a: // RES 1,D
							regs[D] &= ~( 1 << 1 );
							break;
						case 0x8b: // RES 1,E
							regs[E] &= ~( 1 << 1 );
							break;
						case 0x8c: // RES 1,H
							regs[H] &= ~( 1 << 1 );
							break;
						case 0x8d: // RES 1,L
							regs[L] &= ~( 1 << 1 );
							break;
						case 0x8e: // RES 1,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 1 ) );
							break;
						case 0x8f: // RES 1,A
							regs[A] &= ~( 1 << 1 );
							break;
						case 0x90: // RES 2,B
							regs[B] &= ~( 1 << 2 );
							break;
						case 0x91: // RES 2,C
							regs[C] &= ~( 1 << 2 );
							break;
						case 0x92: // RES 2,D
							regs[D] &= ~( 1 << 2 );
							break;
						case 0x93: // RES 2,E
							regs[E] &= ~( 1 << 2 );
							break;
						case 0x94: // RES 2,H
							regs[H] &= ~( 1 << 2 );
							break;
						case 0x95: // RES 2,L
							regs[L] &= ~( 1 << 2 );
							break;
						case 0x96: // RES 2,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 2 ) );
							break;
						case 0x97: // RES 2,A
							regs[A] &= ~( 1 << 2 );
							break;
						case 0x98: // RES 3,B
							regs[B] &= ~( 1 << 3 );
							break;
						case 0x99: // RES 3,C
							regs[C] &= ~( 1 << 3 );
							break;
						case 0x9a: // RES 3,D
							regs[D] &= ~( 1 << 3 );
							break;
						case 0x9b: // RES 3,E
							regs[E] &= ~( 1 << 3 );
							break;
						case 0x9c: // RES 3,H
							regs[H] &= ~( 1 << 3 );
							break;
						case 0x9d: // RES 3,L
							regs[L] &= ~( 1 << 3 );
							break;
						case 0x9e: // RES 3,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 3 ) );
							break;
						case 0x9f: // RES 3,A
							regs[A] &= ~( 1 << 3 );
							break;
						case 0xa0: // RES 4,B
							regs[B] &= ~( 1 << 4 );
							break;
						case 0xa1: // RES 4,C
							regs[C] &= ~( 1 << 4 );
							break;
						case 0xa2: // RES 4,D
							regs[D] &= ~( 1 << 4 );
							break;
						case 0xa3: // RES 4,E
							regs[E] &= ~( 1 << 4 );
							break;
						case 0xa4: // RES 4,H
							regs[H] &= ~( 1 << 4 );
							break;
						case 0xa5: // RES 4,L
							regs[L] &= ~( 1 << 4 );
							break;
						case 0xa6: // RES 4,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 4 ) );
							break;
						case 0xa7: // RES 4,A
							regs[A] &= ~( 1 << 4 );
							break;
						case 0xa8: // RES 5,B
							regs[B] &= ~( 1 << 5 );
							break;
						case 0xa9: // RES 5,C
							regs[C] &= ~( 1 << 5 );
							break;
						case 0xaa: // RES 5,D
							regs[D] &= ~( 1 << 5 );
							break;
						case 0xab: // RES 5,E
							regs[E] &= ~( 1 << 5 );
							break;
						case 0xac: // RES 5,H
							regs[H] &= ~( 1 << 5 );
							break;
						case 0xad: // RES 5,L
							regs[L] &= ~( 1 << 5 );
							break;
						case 0xae: // RES 5,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 5 ) );
							break;
						case 0xaf: // RES 5,A
							regs[A] &= ~( 1 << 5 );
							break;
						case 0xb0: // RES 6,B
							regs[B] &= ~( 1 << 6 );
							break;
						case 0xb1: // RES 6,C
							regs[C] &= ~( 1 << 6 );
							break;
						case 0xb2: // RES 6,D
							regs[D] &= ~( 1 << 6 );
							break;
						case 0xb3: // RES 6,E
							regs[E] &= ~( 1 << 6 );
							break;
						case 0xb4: // RES 6,H
							regs[H] &= ~( 1 << 6 );
							break;
						case 0xb5: // RES 6,L
							regs[L] &= ~( 1 << 6 );
							break;
						case 0xb6: // RES 6,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 6 ) );
							break;
						case 0xb7: // RES 6,A
							regs[A] &= ~( 1 << 6 );
							break;
						case 0xb8: // RES 7,B
							regs[B] &= ~( 1 << 7 );
							break;
						case 0xb9: // RES 7,C
							regs[C] &= ~( 1 << 7 );
							break;
						case 0xba: // RES 7,D
							regs[D] &= ~( 1 << 7 );
							break;
						case 0xbb: // RES 7,E
							regs[E] &= ~( 1 << 7 );
							break;
						case 0xbc: // RES 7,H
							regs[H] &= ~( 1 << 7 );
							break;
						case 0xbd: // RES 7,L
							regs[L] &= ~( 1 << 7 );
							break;
						case 0xbe: // RES 7,(HL)
							writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 7 ) );
							break;
						case 0xbf: // RES 7,A
							regs[A] &= ~( 1 << 7 );
							break;
						case 0xc0: // SET 0,B
							regs[B] |= ( 1 << 0 );
							break;
						case 0xc1: // SET 0,C
							regs[C] |= ( 1 << 0 );
							break;
						case 0xc2: // SET 0,D
							regs[D] |= ( 1 << 0 );
							break;
						case 0xc3: // SET 0,E
							regs[E] |= ( 1 << 0 );
							break;
						case 0xc4: // SET 0,H
							regs[H] |= ( 1 << 0 );
							break;
						case 0xc5: // SET 0,L
							regs[L] |= ( 1 << 0 );
							break;
						case 0xc6: // SET 0,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 0 ) );
							break;
						case 0xc7: // SET 0,A
							regs[A] |= ( 1 << 0 );
							break;
						case 0xc8: // SET 1,B
							regs[B] |= ( 1 << 1 );
							break;
						case 0xc9: // SET 1,C
							regs[C] |= ( 1 << 1 );
							break;
						case 0xca: // SET 1,D
							regs[D] |= ( 1 << 1 );
							break;
						case 0xcb: // SET 1,E
							regs[E] |= ( 1 << 1 );
							break;
						case 0xcc: // SET 1,H
							regs[H] |= ( 1 << 1 );
							break;
						case 0xcd: // SET 1,L
							regs[L] |= ( 1 << 1 );
							break;
						case 0xce: // SET 1,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 1 ) );
							break;
						case 0xcf: // SET 1,A
							regs[A] |= ( 1 << 1 );
							break;
						case 0xd0: // SET 2,B
							regs[B] |= ( 1 << 2 );
							break;
						case 0xd1: // SET 2,C
							regs[C] |= ( 1 << 2 );
							break;
						case 0xd2: // SET 2,D
							regs[D] |= ( 1 << 2 );
							break;
						case 0xd3: // SET 2,E
							regs[E] |= ( 1 << 2 );
							break;
						case 0xd4: // SET 2,H
							regs[H] |= ( 1 << 2 );
							break;
						case 0xd5: // SET 2,L
							regs[L] |= ( 1 << 2 );
							break;
						case 0xd6: // SET 2,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 2 ) );
							break;
						case 0xd7: // SET 2,A
							regs[A] |= ( 1 << 2 );
							break;
						case 0xd8: // SET 3,B
							regs[B] |= ( 1 << 3 );
							break;
						case 0xd9: // SET 3,C
							regs[C] |= ( 1 << 3 );
							break;
						case 0xda: // SET 3,D
							regs[D] |= ( 1 << 3 );
							break;
						case 0xdb: // SET 3,E
							regs[E] |= ( 1 << 3 );
							break;
						case 0xdc: // SET 3,H
							regs[H] |= ( 1 << 3 );
							break;
						case 0xdd: // SET 3,L
							regs[L] |= ( 1 << 3 );
							break;
						case 0xde: // SET 3,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 3 ) );
							break;
						case 0xdf: // SET 3,A
							regs[A] |= ( 1 << 3 );
							break;
						case 0xe0: // SET 4,B
							regs[B] |= ( 1 << 4 );
							break;
						case 0xe1: // SET 4,C
							regs[C] |= ( 1 << 4 );
							break;
						case 0xe2: // SET 4,D
							regs[D] |= ( 1 << 4 );
							break;
						case 0xe3: // SET 4,E
							regs[E] |= ( 1 << 4 );
							break;
						case 0xe4: // SET 4,H
							regs[H] |= ( 1 << 4 );
							break;
						case 0xe5: // SET 4,L
							regs[L] |= ( 1 << 4 );
							break;
						case 0xe6: // SET 4,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 4 ) );
							break;
						case 0xe7: // SET 4,A
							regs[A] |= ( 1 << 4 );
							break;
						case 0xe8: // SET 5,B
							regs[B] |= ( 1 << 5 );
							break;
						case 0xe9: // SET 5,C
							regs[C] |= ( 1 << 5 );
							break;
						case 0xea: // SET 5,D
							regs[D] |= ( 1 << 5 );
							break;
						case 0xeb: // SET 5,E
							regs[E] |= ( 1 << 5 );
							break;
						case 0xec: // SET 5,H
							regs[H] |= ( 1 << 5 );
							break;
						case 0xed: // SET 5,L
							regs[L] |= ( 1 << 5 );
							break;
						case 0xee: // SET 5,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 5 ) );
							break;
						case 0xef: // SET 5,A
							regs[A] |= ( 1 << 5 );
							break;
						case 0xf0: // SET 6,B
							regs[B] |= ( 1 << 6 );
							break;
						case 0xf1: // SET 6,C
							regs[C] |= ( 1 << 6 );
							break;
						case 0xf2: // SET 6,D
							regs[D] |= ( 1 << 6 );
							break;
						case 0xf3: // SET 6,E
							regs[E] |= ( 1 << 6 );
							break;
						case 0xf4: // SET 6,H
							regs[H] |= ( 1 << 6 );
							break;
						case 0xf5: // SET 6,L
							regs[L] |= ( 1 << 6 );
							break;
						case 0xf6: // SET 6,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 6 ) );
							break;
						case 0xf7: // SET 6,A
							regs[A] |= ( 1 << 6 );
							break;
						case 0xf8: // SET 7,B
							regs[B] |= ( 1 << 7 );
							break;
						case 0xf9: // SET 7,C
							regs[C] |= ( 1 << 7 );
							break;
						case 0xfa: // SET 7,D
							regs[D] |= ( 1 << 7 );
							break;
						case 0xfb: // SET 7,E
							regs[E] |= ( 1 << 7 );
							break;
						case 0xfc: // SET 7,H
							regs[H] |= ( 1 << 7 );
							break;
						case 0xfd: // SET 7,L
							regs[L] |= ( 1 << 7 );
							break;
						case 0xfe: // SET 7,(HL)
							writemem8b(H,L, readmem8b(H, L) | ( 1 << 7 ) );
							break;
						case 0xff: // SET 7,A
							regs[A] |= ( 1 << 7 );
							break;
						default:
							System.out.printf( "UNKNOWN PREFIX INSTRUCTION: $%02x\n" , instr );
							PC -= 2; // we failed to execute the instruction, so restore PC
							return 0;
					}
					break;
				default:
					System.out.printf( "UNKNOWN INSTRUCTION: $%02x\n" , instr );
					PC -= 1; // we failed to execute the instruction, so restore PC
					return 0;
			}
			PC &= 0xffff;
			SP &= 0xffff;
			++TotalInstrCount;
			TotalCycleCount += curcycles;
			++nopCount;
			if (!nop)
				nopCount=0;
			if ( nopCount>5 ) {
				System.out.println( "Executing a lot of NOPs, aborting!" );
				return 0;
			}
			return curcycles;
		}

		final public int nextinstruction() {
			int res = execute();
			lastException = (res!=0) ? 0 : 1;
			if (res > 0)  {
				//clockfreq = 4194304hz
				DIVcntdwn -= res;
				if (DIVcntdwn < 0) {
					DIVcntdwn += 256; // == 4194304/16384
					++IOP[0x04];
					IOP[0x04] &= 0xff;
				}
				int tac = IOP[0x07];
				if ((tac&4)!=0) {
					TIMAcntdwn -= res;
					if (TIMAcntdwn < 0) {
						if ((tac&3)==0) TIMAcntdwn += 1024; // == 4194304/4096
						if ((tac&3)==1) TIMAcntdwn +=   16; // == 4194304/262144
						if ((tac&3)==2) TIMAcntdwn +=   64; // == 4194304/65536
						if ((tac&3)==3) TIMAcntdwn +=  256; // == 4194304/16384
						++IOP[0x05];
						if (IOP[0x05] > 0xff) {
							IOP[0x05] = IOP[0x06];
							triggerInterrupt(2);
						}
					}
				}

				VBLANKcntdwn -= res;
				if (VBLANKcntdwn < 0) {
					VBLANKcntdwn += 456;   // 4194304/9198
					VC.renderNextScanline();
					if ((IOP[0x0f]&3)!=0) {
						//System.out.printf("triggered STAT  PC=$%04x  IF=$%02x  IE=$%02x  IME=%b\n", PC, IOP[0x0f], IE, IME);
						//System.out.println("res="+res+"  cnt="+ VBLANKcntdwn);
					}
				}
				// [0 <= VBLANKcntdwn < 456]
				// [0..80)   [80..252)   [252..456)
				// mode 2    mode 3      mode 0
				// when LY>144 then mode 1
			}
			if (res > 30)
				System.out.printf("res=%i  PC=$%04x",res, PC);

			return res;
		}

		final protected int exception() {
			return lastException;
		}

}
