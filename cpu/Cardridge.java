import java.io.*;

public class Cardridge
{
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

    public Cardridge(String file_name)
    /**
     * constructs a new instance of Cardridge
     * pre:  fileName is the name of a cardridge
     * post: f an error occurred while loading
     *         getError() contains the gives the message of the error
     *       else
     *         the cardridge is loaded into memory
     */
    {
        this.file_name = file_name;
        loadFromFile();
    }

    public String getError()
    /**
     * getError returns the error message if an error has occured
     * pre: loadFromFile == true
     * ret: Exception.getMessage()
     */
    {
        return err_msg;
    }

    private void loadFromFile()
    /*
     * loadFromFile loads a cardridge from a file
     * pre:  true
     * post: if an error occurred
     *         getError() contains the gives the message of the error
     *       else
     *         the cardridge is loaded into ROM/RAM
     */
    {
        /*
         * load the first ROM bank of the cardridge into memory
         * used to initialize RAM and ROM banks
         */
        int[] first_rom_bank = new int[ROM_BANK_SIZE];
        System.out.println("Attempting to load ROM: `"+file_name+"'");
        try
        {
            File card = new File(file_name);
            FileInputStream fistream = new FileInputStream(card);
            DataInputStream distream = new DataInputStream(fistream);

            // load first ROM bank into memory
            for(int i = 0; i < ROM_BANK_SIZE; ++i)
                first_rom_bank[i] = distream.readUnsignedByte();

        }
        catch(Exception e)
        {
            err_msg = "Error while loading ROM bank #0 " + e.getMessage();
        }

        // Detect MBC type
        MBC = first_rom_bank[0x0147];

        // Determine ROM size
        switch(first_rom_bank[0x0148])
        {
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
        switch(first_rom_bank[0x0149])
        {
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
        try
        {
            File card = new File(file_name);
            System.out.println("Cardsize = " + card.length() + " bytes ("+card.length()/ROM_BANK_SIZE+" banks)");
            FileInputStream fistream = new FileInputStream(card);
            DataInputStream distream = new DataInputStream(fistream);

            // load ROM into memory
            System.out.println("ROM should have "+ROM.length+" banks");
            for(int i = 0; i < ROM.length; i++) {
              System.out.println("Reading bank #"+i+", "+i*ROM_BANK_SIZE+" bytes done");
              for(int j = 0; j < ROM_BANK_SIZE; j++) {
                ROM[i][j] = distream.readUnsignedByte();
                }
              }

            t = 1;
            // load RAM into memory
            for(int i = 0; i < RAM.length; i++)
                for(int j = 0; j < RAM[i].length; j++)
                  RAM[i][j] = distream.readUnsignedByte();

            t = 2;
            System.out.println("Loaded " + (ROM.length * ROM[0].length + RAM.length * RAM[0].length)+" bytes of ROM and RAM into memory");
        }
        catch(Exception e)
        {
            err_msg = "Error while loading ROM/RAM " + e.toString() + " " + t;
        }
    }

    public int read(int index)
    {
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
     int b=0; // b==byte read
        //TODO fatsoenlijk
        if(index<0) { //Invalid
          System.out.println("ERROR: Cartridge.read(): No negative addresses in GameBoy memorymap.");
          b=0; //NOP
          }
        else if(index < 0x4000) { //16KB ROM Bank 00     (in cartridge, fixed at bank 00)
          b=ROM[0][index];
        }
        else if(index < 0x8000) { //16KB ROM Bank 01..NN (in cartridge, switchable bank number)
          System.out.println("TODO: Cartridge.read(): Bankswitching (fixed at bank 1)");
          b=ROM[1][index];
        }
        else if(index < 0xA000) { //8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
          System.out.println("TODO: Cartridge.read(): VRAM Read");
          b=0;
        }
        else if(index < 0xC00) { //8KB External RAM     (in cartridge, switchable bank, if any)
          System.out.println("TODO: Cartridge.read(): External RAM Read");
          b=0;
        }
        else if(index < 0xd000) { //4KB Work RAM Bank 0 (WRAM)
          System.out.println("TODO: Cartridge.read(): Internal RAM Read bank0");
          b=0;
        }
        else if(index < 0xe000) { //4KB Work RAM Bank 1 (WRAM)  (switchable bank 1-7 in CGB Mode)
          System.out.println("TODO: Cartridge.read(): Internal RAM Read bank1");
          b=0;
        }
        else if(index < 0xfe00) { //Same as C000-DDFF (ECHO)    (typically not used)
          System.out.println("TODO: Cartridge.read(): ECHO RAM Read");
          b=read(index-0x2000);
        }
        else if(index < 0xfea0) { //Sprite Attribute Table (OAM)
          System.out.println("TODO: Cartridge.read(): Sprite Attribute Table");
          b=0;
        }
        else if(index < 0xff00) { //Not Usable
          System.out.println("TODO: Cartridge.read(): Read from unusable memory (0xfea-0xfeff)");
          b=0;
        }
        else if(index < 0xff80) { //I/O Ports
          System.out.println("TODO: Cartridge.read(): Read from IO ports");
          b=0;
        }
        else if(index < 0xffff) { //High RAM (HRAM)
          System.out.println("TODO: Cartridge.read(): Read from High RAM (0xff80-0xfffe)");
          b=0;
        }
        else if(index < 0x10000) { // Interrupt Enable Register (0xffff)
          System.out.println("TODO: Cartridge.read(): Read from Interrupt Enable Register (0xffff)");
          b=0;
        }
        else {
          System.out.println("ERROR: Cartridge.read(): Out of range memory access: $"+index);
        }
      return b;
    }

    public void write(int index, int value)
    {
        // TODO fatsoenlijk
        // Switch RAM/ROM and bank numbers

        //
        switch (MBC)
        {
            case 0x0001:
            case 0x0002:
            case 0x0003:
                // MBC1

                if ((0xA000 <= index) && (index <= 0xBFFF))
                {
                    // RAM Bank 00-03, if any

                }
                else if ((0x0000 <= index) && (index <= 0x1FFF))
                {
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
                break;
            case 0x000F:
            case 0x0010:
            case 0x0011:
            case 0x0012:
            case 0x0013:
                // MBC3
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

    public static void main(String[] args)
    {
        Cardridge card = new Cardridge("Pokemon Blue.gb");
        if(card.getError()!=null) {
          System.out.println("ERROR: "+card.getError());
          }
        else {
          System.out.println("Succesfully loaded ROM :)");
        }
    }

}
