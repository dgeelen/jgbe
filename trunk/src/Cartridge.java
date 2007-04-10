import java.io.*;
import java.util.zip.*;

public class Cartridge {
	// the size of the ROM banks in byte
	private static final int ROM_BANK_SIZE = 0x4000;

	// RAM/ROM
	private int[][] RAM;               // RAM [banknr][index]
	private int[][] ROM;               // ROM [banknr][index]

	protected int[][] MM_ROM;
	protected int[][] MM_RAM;

	private String  file_name;
	private String  err_msg;            // message in case of an error

	private int MBC;                    // The MBC used in the cartridge

	private boolean ram_enabled = false;// Whether RAM is enabled to read and write
	private boolean RTCRegisterEnabled=false;
	protected int     CurrentROMBank = 0;    // The ROM bank to read/write
	protected int     CurrentRAMBank = 0;    // The RAM bank to read/write
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
		int[] first_rom_bank = new int[ROM_BANK_SIZE];
		DataInputStream distream = getDataStream(file_name);

		// load first ROM bank into memory
		for(int i = 0; i < ROM_BANK_SIZE; ++i)
			first_rom_bank[i] = distream.readUnsignedByte();

		// Detect MBC type
		MBC = first_rom_bank[0x0147];
		if(first_rom_bank[0x143] == 0 ) { //regular gameboy game
			System.out.println("Cartridge appears to be a GameBoy game (first_rom_bank[0x0147] = "+first_rom_bank[0x143]+")");
		}
		else {
			System.out.println("Cartridge could be a ColorGameBoy game (first_rom_bank[0x0147] = "+first_rom_bank[0x143]+")");
		}

		// Determine ROM size
		switch(first_rom_bank[0x0148])	{
			case 0x00: ROM = new int[2][ROM_BANK_SIZE]; System.out.println("ROM size = 32KByte (no ROM banking)"); break;
			case 0x01: ROM = new int[4][ROM_BANK_SIZE]; System.out.println("ROM size = 64KByte (4 banks)"); break;
			case 0x02: ROM = new int[8][ROM_BANK_SIZE]; System.out.println("ROM size = 128KByte (8 banks)"); break;
			case 0x03: ROM = new int[16][ROM_BANK_SIZE]; System.out.println("ROM size = 256KByte (16 banks)"); break;
			case 0x04: ROM = new int[32][ROM_BANK_SIZE]; System.out.println("ROM size = 512KByte (32 banks)"); break;
			case 0x05: ROM = new int[64][ROM_BANK_SIZE]; System.out.println("ROM size = 1MByte (64 banks) - only 63 banks used by MBC1"); break;
			case 0x06: ROM = new int[128][ROM_BANK_SIZE]; System.out.println("ROM size = 2MByte (128 banks) - only 125 banks used by MBC1"); break;
			case 0x07: ROM = new int[256][ROM_BANK_SIZE]; System.out.println("ROM size = 4MByte (256 banks)"); break;
			case 0x52: ROM = new int[72][ROM_BANK_SIZE]; System.out.println("ROM size = 1.1MByte (72 banks)"); break;
			case 0x53: ROM = new int[80][ROM_BANK_SIZE]; System.out.println("ROM size = 1.2MByte (80 banks)"); break;
			case 0x54: ROM = new int[96][ROM_BANK_SIZE]; System.out.println("ROM size = 1.5MByte (96 banks)"); break;
		} // switch(header[0x0148])

		// Determine RAM size
		switch(first_rom_bank[0x0149]) {
			case 0x00: RAM = new int[0][0]; System.out.println("Card has no RAM"); break;
			case 0x01: RAM = new int[1][2 * 1024]; System.out.println("Card has 2KBytes of RAM"); break;
			case 0x02: RAM = new int[1][8 * 1024]; System.out.println("Card has 8Kbytes of RAM"); break;
			case 0x03: RAM = new int[4][8 * 1024]; System.out.println("Card has 32 KBytes of RAM (4 banks of 8KBytes each)"); break;
			case 0x04: RAM = new int[16][8 * 1024]; System.out.println("Card has 128 KBytes of RAM (16 banks of 8KBytes each)"); break;
		} // switch(header[0x0149])

		String title="";
		for(int i=0; i<16; ++i) {
			if(first_rom_bank[0x0134+i]==0) break;
			title+=(char)first_rom_bank[0x0134+i];
		}
		System.out.println("ROM Name appears to be `"+title+"'");
		// load entire ROM/RAM into memory

		System.out.println("Trying to load "+ROM.length+" banks from ROM");
		System.out.printf("loading");
		for(int j = 0; j < ROM_BANK_SIZE; j++)
			ROM[0][j] = first_rom_bank[j];
		for(int i = 1; i < ROM.length; i++) {
			for(int j = 0; j < ROM_BANK_SIZE; j++) {
				ROM[i][j] = distream.readUnsignedByte();
			}
			System.out.printf(".");
		}
		System.out.printf("\n");

		//System.out.println("Cartridge is using " + (ROM.length * ROM[0].length + RAM.length * RAM[0].length)+" bytes of ROM and RAM");
		initMemMap();
		distream.close(); // lets be nice :-p
	}

	private final void initMemMap() {
		MM_ROM = new int[ROM.length<<2][ROM_BANK_SIZE>>2];
		for (int i = 0; i < ROM.length; ++i) {
			for (int j = 0; j < ROM_BANK_SIZE; ++j) {
				MM_ROM[(i*4)+(j>>12)][j&0x0FFF] = ROM[i][j];
			}
		}
		if (RAM.length>0) {
			MM_RAM = new int[RAM.length<<1][4 * 1024]; // little bit too much maybe
			for (int i = 0; i < RAM.length; ++i) {
				for (int j = 0; j < RAM[i].length; ++j) {
					MM_RAM[(i*2)+(j>>12)][j&0x0FFF] = RAM[i][j];
				}
			}
		} else {
			MM_RAM = new int[2][]; // to prevent bound errors...
		}
	}

	public int read(int index) {
		switch(MBC) {
			case 0x00: //MBC0	/*HAX*/
			case 0x13: //MBC3 TODO: RTC CRAP
			case 0x000F:
			case 0x0010:
			case 0x0011:
			case 0x0012:
				if(index < 0x4000) return ROM[0][index];
				if((index >= 0x4000) && (index < 0x8000)) return ROM[CurrentROMBank][index-0x4000];
				if((index >= 0xA000) && (index < 0xC000)) return RAM[CurrentRAMBank][index-0xa000];
				System.out.println("Error: Reading from cartridge with a non cartridge address!");
				return -1;
			case 0x0019:
			case 0x001A:
			case 0x001B:
			case 0x001C:
			case 0x001D:
			case 0x001E:
				// MBC5
				if(index < 0x4000) return ROM[0][index];
				if((index >= 0x4000) && (index < 0x8000)) return ROM[CurrentROMBank][index-0x4000];
				if((index >= 0xA000) && (index < 0xC000)) return RAM[CurrentRAMBank][index-0xa000];
				System.out.println("Error: Reading from cartridge with a non cartridge address!");
				return -1;
			default:
				System.out.println("Error: Cartridge memory bank controller type #"+ MBC +" is not implemented!");
				return -1;
		}
	}

	public void write(int index, int value) {        // TODO fatsoenlijk
		switch (MBC) {
			case 0x0001:
			case 0x0002:
			case 0x0003:
				// MBC1
				if ((0xA000 <= index) && (index <= 0xBFFF))	{
						// RAM Bank 00-03, if any
				}
				else if ((0x0000 <= index) && (index <= 0x1FFF)) {
					// RAM Enable
					// 0x0Ah enable
					if (value == 0x0A) ram_enabled = true;
					else               ram_enabled = false;
				}
				// TODO all option
				break;
			case 0x0005:
			case 0x0006:
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
			case 0x000F:
			case 0x0010:
			case 0x0011:
			case 0x0012:
			case 0x0013:
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
						RAM[CurrentRAMBank][index-0xa000] = value;
					}
				}
				if(((index>=0x8000)&&(index<0xa000)) || ((index>0xc000))) System.out.println("WARNING: Cartridge.write(): Unsupported address for write");
				break;
			case 0x0015:
			case 0x0016:
			case 0x0017:
				// MBC4
				break;
			case 0x0019:
			case 0x001A:
			case 0x001B:
			case 0x001C:
			case 0x001D:
			case 0x001E:
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
						CurrentRAMBank= value % RAM.length;
				}
				if((index>=0xa000) && (index<0xc000)){
					RAM[CurrentRAMBank][index-0xa000] = value;
				}
				if(((index>=0x6000)&&(index<0xa000)) || ((index>0xc000))) System.out.println("WARNING: Cartridge.write(): Unsupported address for write");
				break;
		}
	}
}
