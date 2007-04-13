import java.io.*;
import java.util.zip.*;

public class Cartridge {
	// the size of the ROM banks in byte
	private static final int ROM_BANK_SIZE = 0x4000;
	// size of the chunks in the memmap
	private static final int MEMMAP_SIZE = 0x1000;
	// max number of memmaps for rom/ram
	private static final int MAX_ROM_MM = 512<<2;
	private static final int MAX_RAM_MM = 32<<1;

	protected int[][] MM_ROM = new int[MAX_ROM_MM][];
	protected int[][] MM_RAM = new int[MAX_RAM_MM][];;

	protected int[] BIOS_ROM = new int[0x100];

	private String  file_name;
	private String  err_msg;            // message in case of an error

	private int MBC;                    // The MBC used in the cartridge

	private boolean ram_enabled = false;// Whether RAM is enabled to read and write
	private boolean RTCRegisterEnabled=false;
	private int RomRamModeSelect=0; // 0 = rom, 1=ram
	protected int     CurrentROMBank = 1;    // The ROM bank to read/write
	protected int     CurrentRAMBank = 1;    // The RAM bank to read/write
	private int     CurrentRTCRegister=0;

	public Cartridge(String file_name) {
		/**
		 * constructs a new instance of Cartridge
		 * pre:  fileName is the name of a cartridge
		 * post: f an error occurred while loading
		 *         getError() contains the gives the message of the error
		 *       else
		 *         the cartridge is loaded into memory
		 */
		this.file_name = file_name;
		try {
			loadFromFile();
		}
		catch (java.io.IOException e) {
			System.out.println("error loading cartridge from file!: " + e.getMessage());
			err_msg = e.getMessage();
		}

		new Bios("Gameboy Boot.gb", BIOS_ROM);
	}

	public String getError() {
		/**
		 * getError returns the error message if an error has occured
		 * pre: loadFromFile == true
		 * ret: Exception.getMessage()
		 */
		return err_msg;
	}

	private DataInputStream getDataStream(String fname) throws java.io.IOException
	{
    int dotPos = fname.lastIndexOf(".");
    String fext = fname.substring(dotPos);
		if ((fext.equals(".gb")) || (fext.equals(".cgb")) || (fext.equals(".gbc")) ) {
			// plain files
			FileInputStream fistream = new FileInputStream(fname);
			BufferedInputStream bistream = new BufferedInputStream(fistream);
			DataInputStream distream = new DataInputStream(bistream);
			return distream;
		}
		if (fext.equals(".zip")) {
			// Open the ZIP file
			FileInputStream fistream = new FileInputStream(fname);
			ZipInputStream zistream = new ZipInputStream(fistream);

			// Get the first entry
			ZipEntry entry = zistream.getNextEntry();

			BufferedInputStream bistream = new BufferedInputStream(zistream);
			DataInputStream distream = new DataInputStream(bistream);

			return distream;
		}
		throw new java.io.IOException("unknow file type");
	}


	private void loadFromFile() throws java.io.IOException {
		/**
		 * loadFromFile loads a cartidge from a file
		 * pre:  true
		 * post: if an error occurred
		 *         getError() contains the gives the message of the error
		 *       else
		 *         the cartidge is loaded into ROM/RAM
		 */
		/*
		 * load the first ROM bank of the cartridge into memory
		 * used to initialize RAM and ROM banks
		 */
		MM_ROM[0] = new int[MEMMAP_SIZE]; // init first memmap chunk

		DataInputStream distream = getDataStream(file_name);

		// load first memmap chunk into memory
		for(int i = 0; i < MEMMAP_SIZE; ++i)
			MM_ROM[0][i] = distream.readUnsignedByte();

		// Detect MBC type
		System.out.printf("Cartridge MBC type:");
		switch(MM_ROM[0][0x0147]) {
			case 0x00: MBC=0; System.out.printf("ROM ONLY"); break;
			case 0x01: MBC=1; System.out.printf("MBC1"); break;
			case 0x02: MBC=1; System.out.printf("MBC1+RAM"); break;
			case 0x03: MBC=1; System.out.printf("MBC1+RAM+BATTERY"); break;
			case 0x05: MBC=2; System.out.printf("MBC2"); break;
			case 0x06: MBC=2; System.out.printf("MBC2+BATTERY"); break;
			case 0x08: MBC=0; System.out.printf("ROM+RAM"); break;
			case 0x09: MBC=0; System.out.printf("ROM+RAM+BATTERY"); break;
			case 0x0b: MBC=-1; System.out.printf("MMM01"); break;
			case 0x0c: MBC=-1; System.out.printf("MMM01+RAM"); break;
			case 0x0d: MBC=-1; System.out.printf("MMM01+RAM+BATTERY"); break;
			case 0x0f: MBC=3; System.out.printf("MBC3+TIMER+BATTERY"); break;
			case 0x10: MBC=3; System.out.printf("MBC3+TIMER+RAM+BATTERY"); break;
			case 0x11: MBC=3; System.out.printf("MBC3"); break;
			case 0x12: MBC=3; System.out.printf("MBC3+RAM"); break;
			case 0x13: MBC=3; System.out.printf("MBC3+RAM+BATTERY"); break;
			case 0x15: MBC=4; System.out.printf("MBC4"); break;
			case 0x16: MBC=4; System.out.printf("MBC4+RAM"); break;
			case 0x17: MBC=4; System.out.printf("MBC4+RAM+BATTERY"); break;
			case 0x19: MBC=5; System.out.printf("MBC5"); break;
			case 0x1a: MBC=5; System.out.printf("MBC5+RAM"); break;
			case 0x1b: MBC=5; System.out.printf("MBC5+RAM+BATTERY"); break;
			case 0x1c: MBC=5; System.out.printf("MBC5+RUMBLE"); break;
			case 0x1d: MBC=5; System.out.printf("MBC5+RUMBLE+RAM"); break;
			case 0x1e: MBC=5; System.out.printf("MBC5+RUMBLE+RAM+BATTERY"); break;
			case 0xfc: MBC=-2; System.out.printf("POCKET CAMERA"); break;
			case 0xfd: MBC=-5; System.out.printf("BANDAI TAMA5"); break;
			case 0xfe: MBC=-42; System.out.printf("HuC3"); break;
			case 0xff: MBC=-99; System.out.printf("HuC1+RAM+BATTERY"); break;
			default:  MBC=-666; System.out.println("*UNKNOWN*"); throw new java.io.IOException("unknow MBC type");
		}
		System.out.println(" (MBC"+MBC+")");
		if(MM_ROM[0][0x0143] == 0 ) { //regular gameboy game
			System.out.println("Cartridge appears to be a GameBoy game");
		}
		else {
			System.out.println("Cartridge could be a ColorGameBoy game");
		}

		// Determine ROM size (need 4 memmap entries per bank)
		int rom_mm_size = 0;
		switch(MM_ROM[0][0x0148])	{
			case 0x00: rom_mm_size =   2 << 2; System.out.println("ROM size = 32KByte (no ROM banking)"); break;
			case 0x01: rom_mm_size =   4 << 2; System.out.println("ROM size = 64KByte (4 banks)"); break;
			case 0x02: rom_mm_size =   8 << 2; System.out.println("ROM size = 128KByte (8 banks)"); break;
			case 0x03: rom_mm_size =  16 << 2; System.out.println("ROM size = 256KByte (16 banks)"); break;
			case 0x04: rom_mm_size =  32 << 2; System.out.println("ROM size = 512KByte (32 banks)"); break;
			case 0x05: rom_mm_size =  64 << 2; System.out.println("ROM size = 1MByte (64 banks) - only 63 banks used by MBC1"); break;
			case 0x06: rom_mm_size = 128 << 2; System.out.println("ROM size = 2MByte (128 banks) - only 125 banks used by MBC1"); break;
			case 0x07: rom_mm_size = 256 << 2; System.out.println("ROM size = 4MByte (256 banks)"); break;
			case 0x08: rom_mm_size = 512 << 2; System.out.println("ROM size = 8MByte (512 banks)"); break;
			case 0x52: rom_mm_size =  72 << 2; System.out.println("ROM size = 1.1MByte (72 banks)"); break;
			case 0x53: rom_mm_size =  80 << 2; System.out.println("ROM size = 1.2MByte (80 banks)"); break;
			case 0x54: rom_mm_size =  96 << 2; System.out.println("ROM size = 1.5MByte (96 banks)"); break;
		} // switch(header[0x0148])

		// Determine RAM size (1 memmap entry per 4 KBytes (round up)
		int ram_mm_size = 0;
		switch(MM_ROM[0][0x0149]) {
			case 0x00: ram_mm_size =  0; System.out.println("Card has no RAM"); break;
			case 0x01: ram_mm_size =  1; System.out.println("Card has 2KBytes of RAM"); break;
			case 0x02: ram_mm_size =  2; System.out.println("Card has 8Kbytes of RAM"); break;
			case 0x03: ram_mm_size =  8; System.out.println("Card has 32 KBytes of RAM (4 banks of 8KBytes each)"); break;
			case 0x04: ram_mm_size = 32; System.out.println("Card has 128 KBytes of RAM (16 banks of 8KBytes each)"); break;
		} // switch(header[0x0149])

		String title="";
		for(int i=0; i<16; ++i) {
			if(MM_ROM[0][0x0134+i]==0) break;
			title+=(char)MM_ROM[0][0x0134+i];
		}
		System.out.println("ROM Name appears to be `"+title+"'");

		// load entire ROM into memory
		System.out.println("Trying to load "+(rom_mm_size>>2)+" banks of ROM");
		System.out.printf("loading");
		for (int i = 1; i < rom_mm_size; ++i) {
			MM_ROM[i] = new int[MEMMAP_SIZE];
			for(int j = 0; j < MEMMAP_SIZE; ++j) {
				MM_ROM[i][j] = distream.readUnsignedByte();
			}
			System.out.printf(".");
		}
		System.out.printf("\n");

		// initing RAM
		for (int i = 0; i < ram_mm_size; ++i)
			MM_RAM[i] = new int[MEMMAP_SIZE];

		// for protection against roms access outside of valid address space
		int dummy_mm[] = new int[MEMMAP_SIZE];
		for (int i = rom_mm_size; i < MAX_ROM_MM; ++i)
			MM_ROM[i] = dummy_mm;
		for (int i = ram_mm_size; i < MAX_RAM_MM; ++i)
			MM_RAM[i] = dummy_mm;

		distream.close(); // lets be nice :-p
	}

	public int read(int index) {
		switch(MBC) {
			case 3:
				//MBC3 TODO: RTC CRAP
				System.out.println("Error: not using memmap, or reading from cartridge with a non cartridge address!");
				return MM_ROM[index>>12][index&0x0fff];
			case 5:
				// MBC5
				System.out.println("Error: not using memmap, or reading from cartridge with a non cartridge address!");
				return -1;
			default:
				System.out.println("Error: Cartridge memory bank controller type #"+ MBC +" is not implemented!");
				return -1;
		}
	}

	public void write(int index, int value) {        // TODO fatsoenlijk
		switch (MBC) {
			case 1:
				// MBC1
				if ((index>=0x0000) && (index <0x2000)) { // RAM Enable
					if ((value&0x0f) == 0x0A) ram_enabled = true; // 0x0Ah enable
					else ram_enabled = false;
				}
				else if(index<0x4000)  { // 2000-3FFF - ROM Bank Number (Write Only)
					CurrentROMBank=Math.max(1, value&0x1f);
					//System.out.println("Selecting ROM bank"+CurrentROMBank);
				}
				else if(index<0x6000) { // 4000-5FFF - RAM Bank Number - or - Upper Bits of ROM Bank Number (Write Only)
					if(RomRamModeSelect==0) { //Select ROM
						int i,j,k;
 						i=(CurrentROMBank&0x1f)|((value&0x03)<<5);
						/*j=(CurrentROMBank&0x1f)|((value&0x03)<<4);
						k=(CurrentROMBank&0x0f)|((value&0x03)<<4);
						System.out.println("Attempting to select rombank "+i+" or "+j+ " or "+k);*/
						CurrentROMBank=i;
						//System.out.println("Selecting ROM bank"+CurrentROMBank);
					}
					else { // Select RAM
						CurrentRAMBank=Math.max(1,value&0x03);
					}
				}
				else if(index<0x8000) { //6000-7FFF - ROM/RAM Mode Select (Write Only)
					RomRamModeSelect=value&1;
				}
				else if ((index>=0xA000) && (index <= 0xBFFF))	{
						// RAM Bank 00-03, if any
						System.out.println("TODO: MBC1 writing to RAM");
				}
				else System.out.printf("TODO: Cartridge writing to $%04x\n", index); // TODO all option
				break;
			case 2:
				// MBC2
				// 0000-3FFF - ROM Bank 00 (Read Only)0000-3FFF - ROM Bank 00 (Read Only)
				// 4000-7FFF - ROM Bank 01-0F (Read Only)
				// A000-A1FF - 512x4bits RAM, built-in into the MBC2 chip (Read/Write)
				// 0000-1FFF - RAM Enable (Write Only)
				// 2000-3FFF - ROM Bank Number (Write Only)
				if ((0xA0000 <= index) && (index <= 0xA1FF))	{
						System.out.println("TODO: write to internal cartridge RAM.");
				}
				else if ((0x0000 <= index) && (index <= 0x1FFF)) {
					if ((index & 1 << 4) == 0) {
							// toggle RAM enabled
							ram_enabled = !ram_enabled;
					}
				}
				else if ((0x2000 <= index) && (index <= 0x3FFFF))	{
					if ((index & 1 << 4) == (1 << 4))	{
						// Enable set ROM bank nr
						value = (value == 0)?1:value;
						CurrentROMBank = value & 0x0F;
					}
				}
				break;
			case 3:
				// MBC3
				if((index>=0)&&(index<0x2000)) { //0000-1FFF - RAM and Timer Enable (Write Only)
					if(value==0x0a) ram_enabled = true;
					else if(value==0x00) ram_enabled = false;
					else System.out.println("WARNING: Ram enabled state UNDEFINED");
				}
				if((index>=0x2000)&&(index<0x4000)) {//2000-3FFF - ROM Bank Number (Write Only)
					CurrentROMBank=Math.max(value&0x7f,1);
				}
				if((index>=0x4000)&&(index<0x6000)) { //4000-5FFF - RAM Bank Number - or - RTC Register Select (Write Only)
					if((value>=0)&&(value<0x4)) {
						RTCRegisterEnabled=false;
						CurrentRAMBank=value;
					}
					if((value>=0x08)&&(value<0x0c)) {
						RTCRegisterEnabled=true;
						CurrentRTCRegister=value-0x08;
					}
				}
				if((index>=0x6000)&&(index<0x8000)) { //6000-7FFF - Latch Clock Data (Write Only)
					System.out.println("TODO: Cartridge.write(): Latch Clock Data!");
				}
				if((index>=0xa000) && (index<0xc000)){
					if(RTCRegisterEnabled) {
						System.out.println("TODO: Cartridge.write(): writing to RAM in RTC mode");
					}
					else {
						System.out.println("Error: not using memmap!");
					}
				}
				if(((index>=0x8000)&&(index<0xa000)) || ((index>0xc000))) System.out.println("WARNING: Cartridge.write(): Unsupported address for write");
				break;
			case 4:
				// MBC4
				break;
			case 5:
				// MBC5
				if((index>=0)&&(index<0x2000)) { //0000-1FFF - RAM and Timer Enable (Write Only)
					if(value==0x0a) ram_enabled = true;
					else if(value==0x00) ram_enabled = false;
					else System.out.println("WARNING: Ram enabled state UNDEFINED");
				}
				if((index>=0x2000)&&(index<0x3000)) {//2000-3FFF - ROM Bank Number (Write Only)
					CurrentROMBank &= 0x100;
					CurrentROMBank |= value;
				}
				if((index>=0x3000)&&(index<0x4000)) {//2000-3FFF - ROM Bank Number (Write Only)
					CurrentROMBank &= 0xff;
					CurrentROMBank |= (value&1) << 8;
				}
				if((index>=0x4000)&&(index<0x6000)) { //4000-5FFF - RAM Bank Number
					if (value < 0x10)
						CurrentRAMBank= value;
				}
				if((index>=0xa000) && (index<0xc000)){
					System.out.println("Error: not using memmap!");
				}
				if(((index>=0x6000)&&(index<0xa000)) || ((index>0xc000))) System.out.println("WARNING: Cartridge.write(): Unsupported address for write");
				break;
		}
	}
}
