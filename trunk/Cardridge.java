import java.io.*;

public class Cardridge
{
    // the size of the ROM banks in byte
    private static final int ROM_BANK_SIZE = 0x4000;

    // RAM/ROM
    private int[][] RAM;               // RAM [banknr][index]
    private int[][] ROM;               // ROM [banknr][index]

    private String  file_name;
    private String  err_msg;            // message in case of an error

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

        try
        {
            File card = new File(file_name);
            FileInputStream fistream = new FileInputStream(card);
            DataInputStream distream = new DataInputStream(fistream);

            // load first ROM bank into memory
            for(int i = 0; i < ROM_BANK_SIZE; i++)
                first_rom_bank[i] = distream.readUnsignedByte();

        }
        catch(Exception e)
        {
            err_msg = "Error while loading ROM bank #0 " + e.getMessage();
        }

        // Determine ROM size
        switch(first_rom_bank[0x0148])
        {
            case 0x00: ROM = new int[2][0x81]; break;   /* 32KByte (no ROM banking) */
            case 0x01: ROM = new int[4][0x81]; break;   /* 64KByte (4 banks) */
            case 0x02: ROM = new int[8][0x81]; break;   /* 128KByte (8 banks) */
            case 0x03: ROM = new int[16][0x81]; break;  /* 256KByte (16 banks) */
            case 0x04: ROM = new int[32][0x81]; break;  /* 512KByte (32 banks) */
            case 0x05: ROM = new int[64][0x81]; break;  /* 1MByte (64 banks)  - only 63 banks used by MBC1 */
            case 0x06: ROM = new int[128][0x81]; break; /* 2MByte (128 banks) - only 125 banks used by MBC1 */
            case 0x07: ROM = new int[256][0x81]; break; /* 4MByte (256 banks) */
            case 0x52: ROM = new int[72][0x81]; break;  /* 1.1MByte (72 banks) */
            case 0x53: ROM = new int[80][0x81]; break;  /* 1.2MByte (80 banks) */
            case 0x54: ROM = new int[96][0x81]; break;  /* 1.5MByte (96 banks) */
        } // switch(header[0x0148])

        // Determine RAM size
        switch(first_rom_bank[0x0149])
        {
            case 0x00: RAM = null; break;               /* None */
            case 0x01: RAM = new int[0][2 * 1024]; break; /* 2KBytes */
            case 0x02: RAM = new int[0][8 * 1024]; break; /* 8Kbytes */
            case 0x03: RAM = new int[4][8 * 1024]; break; /* 32 KBytes (4 banks of 8KBytes each) */
        } // switch(header[0x0149])

        // load entire ROM/RAM into memory
        int t = 0;
        try
        {
            File card = new File(file_name);
            System.out.println("" + card.length());
            FileInputStream fistream = new FileInputStream(card);
            DataInputStream distream = new DataInputStream(fistream);

            // load ROM into memory
            for(int i = 0; i < ROM.length; i++)
                for(int j = 0; j < 0x81; j++)
                  ROM[i][j] = distream.readUnsignedByte();

            t = 1;
            // load RAM into memory
            for(int i = 0; i < RAM.length; i++)
                for(int j = 0; j < RAM[i].length; j++)
                  RAM[i][j] = distream.readUnsignedByte();

            t = 2;
            System.out.println("" + (ROM.length * ROM[0].length + RAM.length * RAM[0].length));
        }
        catch(Exception e)
        {
            err_msg = "Error while loading ROM/RAM " + e.toString() + " " + t;
        }
    }

    public static void main(String[] args)
    {
        Cardridge card = new Cardridge("../Pokemon Blue.gb");
        System.out.println(card.getError());
    }

}