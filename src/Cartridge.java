import java.io.*;

public class Cartridge {
	// the size of the ROM banks in byte
	private static final int ROM_BANK_SIZE = 0x4000;

	// RAM/ROM
	private int[][] RAM;               // RAM [banknr][index]
	private int[][] ROM;               // ROM [banknr][index]
	private int[][] mem_to_read = ROM; // The memory to read from

	private String  file_name;
	private String  err_msg;            // message in case of an error

	private int MBC;                    // The MBC used in the cardridge

	private boolean ram_enabled = false;// Whether RAM is enabled to read and write
	private boolean RTCRegisterEnabled=false;
	private int     CurrentROMBank = 0;    // The ROM bank to read/write
	private int     CurrentRAMBank = 0;    // The RAM bank to read/write
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
		loadFromFile();
	}

	public String getError() {
		/**
		 * getError returns the error message if an error has occured
		 * pre: loadFromFile == true
		 * ret: Exception.getMessage()
		 */
		return err_msg;
	}

	private void loadFromFile() {
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
		System.out.println("Attempting to load ROM: `"+file_name+"'");
		try {
			FileInputStream fistream = new FileInputStream(file_name);
			BufferedInputStream bistream = new BufferedInputStream(fistream);
			DataInputStream distream = new DataInputStream(bistream);

			// load first ROM bank into memory
			for(int i = 0; i < ROM_BANK_SIZE; ++i)
				first_rom_bank[i] = distream.readUnsignedByte();
		}
		catch(Exception e) {
			err_msg = "Error while loading ROM bank #0 " + e.getMessage();
		}

		// Detect MBC type
		MBC = first_rom_bank[0x0147];

		// Determine ROM size
		switch(first_rom_bank[0x0148])	{
			case 0x00: ROM = new int[2][0x81]; System.out.println("ROM size = 32KByte (no ROM banking)"); break;
			case 0x01: ROM = new int[4][0x81]; System.out.println("ROM size = 64KByte (4 banks)"); break;
			case 0x02: ROM = new int[8][0x81]; System.out.println("ROM size = 128KByte (8 banks)"); break;
			case 0x03: ROM = new int[16][0x81]; System.out.println("ROM size = 256KByte (16 banks)"); break;
			case 0x04: ROM = new int[32][0x81]; System.out.println("ROM size = 512KByte (32 banks)"); break;
			case 0x05: ROM = new int[64][ROM_BANK_SIZE]; System.out.println("ROM size = 1MByte (64 banks) - only 63 banks used by MBC1"); break;
			case 0x06: ROM = new int[128][0x81]; System.out.println("ROM size = 2MByte (128 banks) - only 125 banks used by MBC1"); break;
			case 0x07: ROM = new int[256][0x81]; System.out.println("ROM size = 4MByte (256 banks)"); break;
			case 0x52: ROM = new int[72][0x81]; System.out.println("ROM size = 1.1MByte (72 banks)"); break;
			case 0x53: ROM = new int[80][0x81]; System.out.println("ROM size = 1.2MByte (80 banks)"); break;
			case 0x54: ROM = new int[96][0x81]; System.out.println("ROM size = 1.5MByte (96 banks)"); break;
		} // switch(header[0x0148])

		// Determine RAM size
		switch(first_rom_bank[0x0149]) {
			case 0x00: RAM = null; System.out.println("Card has no RAM"); break;
			case 0x01: RAM = new int[0][2 * 1024]; System.out.println("Card has 2KBytes of RAM"); break;
			case 0x02: RAM = new int[0][8 * 1024]; System.out.println("Card has 8Kbytes of RAM"); break;
			case 0x03: RAM = new int[4][8 * 1024]; System.out.println("Card has 32 KBytes of RAM (4 banks of 8KBytes each)"); break;
		} // switch(header[0x0149])

		String title="";
		for(int i=0; i<16; ++i) {
			if(first_rom_bank[0x0134+i]==0) break;
			title+=(char)first_rom_bank[0x0134+i];
		}
		System.out.println("ROM Name appears to be `"+title+"'");
		// load entire ROM/RAM into memory
		int t = 0;
		try	{
			FileInputStream fistream = new FileInputStream(file_name);
			BufferedInputStream bistream = new BufferedInputStream(fistream);
			DataInputStream distream = new DataInputStream(bistream);

			// load ROM into memory
			System.out.println("Trying to load "+ROM.length+" banks from ROM");
			for(int i = 0; i < ROM.length; i++) {
				for(int j = 0; j < ROM_BANK_SIZE; j++) {
					ROM[i][j] = distream.readUnsignedByte();
				}
			}

			t = 1;
			// load RAM into memory
			/*for(int i = 0; i < RAM.length; i++)
					for(int j = 0; j < RAM[i].length; j++)
						RAM[i][j] = distream.readUnsignedByte();*/

			t = 2;
			System.out.println("Cartridge is using " + (ROM.length * ROM[0].length + RAM.length * RAM[0].length)+" bytes of ROM and RAM");
		}
		catch(Exception e) {
			err_msg = "Error while loading ROM/RAM " + e.toString() + " " + t;
		}
	}

	public int read(int index) {
		switch(MBC) {
			case 0x13: //MBC3 TODO: RTC CRAP
				if(index < 0x4000) return ROM[0][index];
				if((index >= 0x4000) && (index < 0x8000)) return ROM[CurrentROMBank][index-0x4000];
				if((index >= 0xA000) && (index < 0xC000)) return RAM[CurrentRAMBank][index-0xa000];
				System.out.println("Error: Reading from cardridge with a non cardridge address!");
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
				if((index>=0xA000) && (index<0xc000)){
					if(RTCRegisterEnabled) {
						System.out.println("TODO: Cartridge.write(): writing to RAM in RTC mode");
					}
					else {
						RAM[CurrentRAMBank][index-0xa000] = value;
					}
				}
				if(((index>0x800)&&(index<0xa000)) || ((index>0xc000))) System.out.println("TODO: Cartridge.write(): Unsupported address for write");
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
				break;
		}
	}
}
