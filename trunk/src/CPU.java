public class CPU
{
  protected static final int CARRY8b = 512;
  protected static final int CARRY8b_SHR = 5;


  protected static final int FLAG_REG = 5;
  protected static final int ZF_Shift = 7;
  protected static final int NF_Shift = ZF_Shift - 1;
  protected static final int HC_Shift = NF_Shift - 1;
  protected static final int CF_Shift = HC_Shift - 1;
  protected static final int ZF_Mask = 1 << ZF_Shift;
  protected static final int NF_Mask = 1 << NF_Shift;
  protected static final int HC_Mask = 1 << HC_Shift;
  protected static final int CF_Mask = 1 << CF_Shift;

private final static int cycles_table[] =
{
 1, 3, 2, 2, 1, 1, 2, 1, 5, 2, 2, 2, 1, 1, 2, 1,
 1, 3, 2, 2, 1, 1, 2, 1, 3, 2, 2, 2, 1, 1, 2, 1,
 3, 3, 2, 2, 1, 1, 2, 1, 3, 2, 2, 2, 1, 1, 2, 1,
 3, 3, 2, 2, 1, 3, 3, 3, 3, 2, 2, 2, 1, 1, 2, 1,

 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 2, 2, 2, 2, 2, 2, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1,

 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,
 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1,

 5, 3, 4, 4, 6, 4, 2, 4, 5, 4, 4, 1, 6, 6, 2, 4,
 5, 3, 4, 0, 6, 4, 2, 4, 5, 4, 4, 0, 6, 0, 2, 4,
 3, 3, 2, 0, 0, 4, 2, 4, 4, 1, 4, 0, 0, 0, 2, 4,
 3, 3, 2, 1, 0, 4, 2, 4, 3, 2, 4, 1, 0, 0, 2, 4,
};

private final static int cb_cycles_table[] =
{
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,

 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 3, 2,
 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 3, 2,
 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 3, 2,
 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 3, 2,

 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,

 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 4, 2,
};



private final static int zflag_table[] =
{
 ZF_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

private final static int incflag_table[] =
{
 ZF_Mask|HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 HC_Mask, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

private final static int decflag_table[] =
{
 ZF_Mask|NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask,
 NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask, NF_Mask|HC_Mask
};

private final static int swap_table[] =
{
 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0,
 0x01, 0x11, 0x21, 0x31, 0x41, 0x51, 0x61, 0x71, 0x81, 0x91, 0xA1, 0xB1, 0xC1, 0xD1, 0xE1, 0xF1,
 0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x82, 0x92, 0xA2, 0xB2, 0xC2, 0xD2, 0xE2, 0xF2,
 0x03, 0x13, 0x23, 0x33, 0x43, 0x53, 0x63, 0x73, 0x83, 0x93, 0xA3, 0xB3, 0xC3, 0xD3, 0xE3, 0xF3,
 0x04, 0x14, 0x24, 0x34, 0x44, 0x54, 0x64, 0x74, 0x84, 0x94, 0xA4, 0xB4, 0xC4, 0xD4, 0xE4, 0xF4,
 0x05, 0x15, 0x25, 0x35, 0x45, 0x55, 0x65, 0x75, 0x85, 0x95, 0xA5, 0xB5, 0xC5, 0xD5, 0xE5, 0xF5,
 0x06, 0x16, 0x26, 0x36, 0x46, 0x56, 0x66, 0x76, 0x86, 0x96, 0xA6, 0xB6, 0xC6, 0xD6, 0xE6, 0xF6,
 0x07, 0x17, 0x27, 0x37, 0x47, 0x57, 0x67, 0x77, 0x87, 0x97, 0xA7, 0xB7, 0xC7, 0xD7, 0xE7, 0xF7,
 0x08, 0x18, 0x28, 0x38, 0x48, 0x58, 0x68, 0x78, 0x88, 0x98, 0xA8, 0xB8, 0xC8, 0xD8, 0xE8, 0xF8,
 0x09, 0x19, 0x29, 0x39, 0x49, 0x59, 0x69, 0x79, 0x89, 0x99, 0xA9, 0xB9, 0xC9, 0xD9, 0xE9, 0xF9,
 0x0A, 0x1A, 0x2A, 0x3A, 0x4A, 0x5A, 0x6A, 0x7A, 0x8A, 0x9A, 0xAA, 0xBA, 0xCA, 0xDA, 0xEA, 0xFA,
 0x0B, 0x1B, 0x2B, 0x3B, 0x4B, 0x5B, 0x6B, 0x7B, 0x8B, 0x9B, 0xAB, 0xBB, 0xCB, 0xDB, 0xEB, 0xFB,
 0x0C, 0x1C, 0x2C, 0x3C, 0x4C, 0x5C, 0x6C, 0x7C, 0x8C, 0x9C, 0xAC, 0xBC, 0xCC, 0xDC, 0xEC, 0xFC,
 0x0D, 0x1D, 0x2D, 0x3D, 0x4D, 0x5D, 0x6D, 0x7D, 0x8D, 0x9D, 0xAD, 0xBD, 0xCD, 0xDD, 0xED, 0xFD,
 0x0E, 0x1E, 0x2E, 0x3E, 0x4E, 0x5E, 0x6E, 0x7E, 0x8E, 0x9E, 0xAE, 0xBE, 0xCE, 0xDE, 0xEE, 0xFE,
 0x0F, 0x1F, 0x2F, 0x3F, 0x4F, 0x5F, 0x6F, 0x7F, 0x8F, 0x9F, 0xAF, 0xBF, 0xCF, 0xDF, 0xEF, 0xFF,
};

private final static int daa_table[] =
{
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,

 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x60, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,

 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06, 0x06,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,

 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,

 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,
 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0, 0xA0,

 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,
 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA, 0xFA,

 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A, 0x9A,
};

private final static int daa_carry_table[] =
{
 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
 00, 00, 00, 00, 00, 00, 00, 00, CF_Mask, CF_Mask, 00, 00, 00, 00, 00, 00,
 CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask,
 CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, CF_Mask, 00, CF_Mask,
};

  protected int TotalInstrCount = 0;
  protected int TotalCycleCount = 0;

  protected int[] regs = new int[8];
  protected static final int B = 1;
  protected static final int C = 2;
  protected static final int D = 3;
  protected static final int E = 4;
  protected static final int F = FLAG_REG;
  protected static final int H = 6;
  protected static final int L = 7;
  protected static final int A = 0;

  protected int[] IOP = new int[0x80];
  private int[] HRAM = new int[0x7F];
  private int[][] WRAM = new int[0x08][0x10000];
  private int CurrentWRAMBank=1;

  boolean doublespeed = false;
  boolean speedswitch = false;

  private int DIVcntdwn = 0;
  private int TIMAcntdwn = 0;
  private int VBLANKcntdwn = 0;

  protected int PC=0;
  protected int SP=0;
  protected int IE=0;
  protected boolean IME=true;
  protected boolean halted=false;

  public int DirectionKeyStatus=0x0f;
  public int ButtonKeyStatus=0x3f;

  private Cartridge cartridge;
  private int lastException=0;
  private Disassembler deasm;
  protected VideoController VC;
  protected AudioController AC;

  public CPU( Cartridge cartridge ) {
   this.cartridge = cartridge;
   refreshMemMap();
   deasm = new Disassembler(this);
   VC = new VideoController(this, 160, 144);
   AC = new AudioController(this);
   reset();
  }

  final private int[][] rMemMap = new int[0x10][];
  final private int[][] wMemMap = new int[0x10][];

  public boolean isCGB() {

   return (read(0x0143) == 0x80) || (read(0x0143) == 0xC0);
  }

  final private void refreshMemMap() {

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

  final protected int read(int index) {
   int mm[]=rMemMap[index>>12];
   if (mm!=null)
    return mm[index&0x0FFF];
   int b=0;
   if(index<0) {
    System.out.println("ERROR: CPU.read(): No negative addresses in GameBoy memorymap.");
    b=-1;
   }
   else if(index < 0x4000) {

    b=cartridge.read(index);
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
    System.out.printf("WARNING: CPU.read(): unusable memory (0xfea-0xfeff) PC=$%04x index=$%04x\n",PC,index);
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
      b = VC.STAT;
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

  final private void write(int index, int value) {
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
    System.out.println("TODO: CPU.write(): Write to unusable memory (0xfea-0xfeff)");
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
     case 0xff46:
      for(int i=0; i<0xa0; ++i){
       write(0xfe00|i, read(i+(value<<8)));
      }
      break;
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
     case 0xff55:
      System.out.println("TODO: CPU.write(): HDMA request for CGB mode (VRAM)");
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
      CurrentWRAMBank=Math.max(value&0x07, 1);
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

  final public void reset() {

   PC = 0x100;

   regs[A]=0x11;
   regs[F]=0xb0;

   regs[B]=0x00;
   regs[C]=0x13;

   regs[D]=0x00;
   regs[E]=0xd8;

   regs[H]=0x01;
   regs[L]=0x4d;
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

  final protected int checkInterrupts() {
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

  final protected void interrupt(int i) {

   IME = false;
   push(PC);
   PC = i;
  }

  final protected void triggerInterrupt(int i) {
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

   regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
   regs[FLAG_REG] = regs[FLAG_REG] | (((( regs[reg_index] & 0xF ) + 1 ) & 0x10 ) << 1 );


   regs[reg_index] = ( ++regs[reg_index] & 0xFF );


   regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
   regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index]==0 )?1:0 )<<ZF_Shift );


   regs[FLAG_REG] = regs[FLAG_REG] & ~NF_Mask;
  }

  final protected void dec8b( int reg_index ) {

   regs[FLAG_REG] = regs[FLAG_REG] & ~HC_Mask;
   regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index] & 0xF )==0 )?HC_Mask:0 );


   regs[reg_index] = ( --regs[reg_index] & 0xFF );


   regs[FLAG_REG] = regs[FLAG_REG] & ~ZF_Mask;
   regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[reg_index]==0 )?1:0 )<<ZF_Shift );


   regs[FLAG_REG] = regs[FLAG_REG] | NF_Mask;
  }

  final protected void inc16b(int ri1, int ri2 ) {

   ++regs[ri2];
   if (regs[ri2]>0xFF) {
    regs[ri2]&=0xFF;
    ++regs[ri1];
    regs[ri1]&=0xFF;
   }
  }

  final protected void dec16b(int ri1, int ri2 ) {

   --regs[ri2];
   if (regs[ri2]<0) {
    regs[ri2]&=0xFF;
    --regs[ri1];
    regs[ri1]&=0xFF;
   }
  }

  final protected void add8b( int dest, int val ) {

   regs[FLAG_REG] = ( regs[FLAG_REG] & 0x00 );


   regs[FLAG_REG] = regs[FLAG_REG] | ((((( regs[dest]&0x0f )+( val&0x0f ) )&0x10 )!=0?1:0 )<<HC_Shift );


   regs[dest] = ( regs[dest] + val );


   regs[FLAG_REG] = regs[FLAG_REG] | ( regs[dest]>>8 )<<CF_Shift;


   regs[dest]&=0xFF;


   regs[FLAG_REG] = regs[FLAG_REG] | ((( regs[dest]==0 )?1:0 )<<ZF_Shift );
  }

  final protected void sub8b( int dest, int val ) {

   regs[FLAG_REG] = NF_Mask;


   regs[FLAG_REG] |= ((( regs[dest]&0x0F )-( val&0x0F ) )<0 ) ? HC_Mask : 0;


   regs[dest] = ( regs[dest] - val );


   regs[FLAG_REG] |= ( regs[dest]<0 ) ? CF_Mask : 0;


   regs[dest]&=0xFF;


   regs[FLAG_REG] |= regs[dest]==0 ? ZF_Mask : 0;
  }

  final protected void add16bHL(int val1, int val2) {
   int fmask = regs[F] & ZF_Mask;
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

   PC = j<<8|i;
  }

  final protected void sbc( int dest, int val ) {
   sub8b( dest, val+(( regs[FLAG_REG]&CF_Mask ) >> CF_Shift ) );
  }

  final protected void adc( int dest, int val ) {
   add8b( dest, val+(( regs[FLAG_REG]&CF_Mask ) >> CF_Shift ) );
  }

  final protected void push( int val ) {

   SP=(SP-1)&0xffff;
   write( SP, ( val>>8 )&0xff );
   SP=(SP-1)&0xffff;
   write( SP, val&0xff );
  }

  final protected int pop() {

   int l = read( SP++ );
   int h = read( SP++ );
   return (l | (h<<8));
  }

  static int nopCount=0;
  final private int execute() {
   int cycles;
   boolean nop=false;

   if(checkInterrupts()!=0) {
    halted = false;
    return 12;
   }
   if (halted) return 4;
   int op = read(PC++);
   cycles = cycles_table[op];

   switch ( op ) {
    case 0x00:
     nop=true;
     break;
    case 0x01:
     regs[C] = read( PC++ );
     regs[B] = read( PC++ );
     break;
    case 0x02:
     writemem8b(B,C, regs[A]);
     break;
    case 0x03:
     inc16b(B, C);
     break;
    case 0x04:
     inc8b( B );
     break;
    case 0x05:
     dec8b( B );
     break;
    case 0x06:
     regs[B] = read( PC++ );
     break;
    case 0x08:{

     int i=read( PC++ );
     int j=read( PC++ );
     int l=((j<<8|i)+1)&0xffff;
     writemem8b(j,i, SP&0xff);
     writemem8b(l>>8,l&0xff, SP>>8);
     }; break;
    case 0x07:
     regs[A] = rolc(regs[A]);
     regs[F] &= ~ZF_Mask;
     break;
    case 0x09:
     add16bHL(regs[B], regs[C]);
     break;
    case 0x0a:
     regs[A] = readmem8b(B, C);
     break;
    case 0x0b:
     dec16b(B, C);
     break;
    case 0x0c:
     inc8b( C );
     break;
    case 0x0d:
     dec8b( C );
     break;
    case 0x0e:
     regs[C] = read( PC++ );
     break;
    case 0x0f:
     regs[A] = rorc(regs[A]);
     break;
    case 0x10:

     if ((IE==0) || (!IME))
      System.out.println("PANIC: we will never unhalt!!!\n");

     if (speedswitch) {
      System.out.println("Speed switch!");
      doublespeed = !doublespeed;
      speedswitch = false;
     }
     break;
    case 0x11:
     regs[E] = read( PC++ );
     regs[D] = read( PC++ );
     break;
    case 0x12:
     writemem8b(D,E, regs[A]);
     break;
    case 0x13:
     inc16b(D, E);
     break;
    case 0x14:
     inc8b( D );
     break;
    case 0x15:
     dec8b( D );
     break;
    case 0x16:
     regs[D] = read( PC++ );
     break;
    case 0x17:
     regs[A] = ror(regs[A]);
     break;
    case 0x18:{
     int x = read( PC++ );
     PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
    };break;
    case 0x19:
     add16bHL(regs[D], regs[E]);
     break;
    case 0x1a:
     regs[A] = readmem8b(D, E);
     break;
    case 0x1b:
     dec16b(D, E);
     break;
    case 0x1c:
     inc8b( E );
     break;
    case 0x1d:
     dec8b( E );
     break;
    case 0x1e:
     regs[E] = read( PC++ );
     break;
    case 0x1f:
     regs[A] = ror(regs[A]);
     break;
    case 0x20:
     if (( regs[F]&ZF_Mask )!=ZF_Mask ) {
      int x = read( PC++ );
      PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
     }
     else ++PC;
     break;
    case 0x21:
     regs[L] = read( PC++ );
     regs[H] = read( PC++ );
     break;
    case 0x22:
     writemem8b(H,L, regs[A]);
     inc16b(H, L);
     break;
    case 0x23:
     inc16b(H, L);
     break;
    case 0x24:
     inc8b(H);
     break;
    case 0x25:
     dec8b(H);
     break;
    case 0x26:
     regs[H] = read( PC++ );
     break;
    case 0x27:{
     int acc=0;
     acc = daa_table[((((int)regs[F])&0x70)<<4) | regs[A]];
     acc &= 0xff;
     regs[A] += acc;
     regs[F] = (regs[F] & (NF_Mask)) | ((regs[A]==0)?ZF_Mask:0) | daa_carry_table[acc>>2];
     regs[A] &= 0xff;
    };break;
    case 0x28:
     if (( regs[F]&ZF_Mask )==ZF_Mask ) {
      int x = read( PC++ );
      PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
     }
     else ++PC;
     break;
    case 0x29:
     add16bHL(regs[H], regs[L]);
     break;
    case 0x2a:
     regs[A] = readmem8b(H, L);
     inc16b(H, L);
     break;
    case 0x2b:
     dec16b(H, L);
     break;
    case 0x2c:
     inc8b( L );
     break;
    case 0x2d:
     dec8b( L );
     break;
    case 0x2e:
     regs[L] = read( PC++ );
     break;
    case 0x2f:
     xor( 0xFF );
     break;
    case 0x30:
     if (( regs[F]&CF_Mask )!=CF_Mask ) {
      int x = read( PC++ );
      PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
     }
     else ++PC;
     break;
    case 0x31:{
     int l = read( PC++ );
     int h = read( PC++ );
     SP = l | (h<<8);
    };break;
    case 0x32:
     writemem8b(H,L, regs[A]);
     dec16b(H, L);
     break;
    case 0x33:
     ++SP;
     SP&=0xffff;
     break;
    case 0x34:{

     int x = regs[A];
     regs[A] = readmem8b(H, L);
     inc8b(A);
     writemem8b(H,L, regs[A]);
     regs[A] = x;
    };break;
    case 0x35:{

     int x = regs[A];
     regs[A] = readmem8b(H, L);
     dec8b(A);
     writemem8b(H,L, regs[A]);
     regs[A] = x;
    };break;
    case 0x36:
     writemem8b(H,L, read(PC++));
     break;
    case 0x37:
     regs[F] &= ZF_Mask;
     regs[F] |= CF_Mask;
     break;
    case 0x38:
     if (( regs[F]&CF_Mask )==CF_Mask ) {
      int x = read( PC++ );
      PC += (( x>=128 ) ? -(x^0xFF)-1 : x );
     }
     else ++PC;
     break;
    case 0x39:
     add16bHL(SP >> 8, SP & 0xff);
     break;
    case 0x3a:
     regs[A] = readmem8b(H, L);
     dec16b(H, L);
     break;
    case 0x3b:
     --SP;
     SP&=0xffff;
     break;
    case 0x3c:
     inc8b(A);
     break;
    case 0x3d:
     dec8b(A);
     break;
    case 0x3e:
     regs[A]=read( PC++ );
     break;
    case 0x3f:
     regs[F] ^= CF_Mask;
     break;
    case 0x40:
     ld8b( B, regs[B] );
     break;
    case 0x41:
     ld8b( B, regs[C] );
     break;
    case 0x42:
     ld8b( B, regs[D] );
     break;
    case 0x43:
     ld8b( B, regs[E] );
     break;
    case 0x44:
     ld8b( B, regs[H] );
     break;
    case 0x45:
     ld8b( B, regs[L] );
     break;
    case 0x46:
     ld8b( B, readmem8b( H,L ) );
     break;
    case 0x47:
     ld8b( B, regs[A] );
     break;
    case 0x48:
     ld8b( C, regs[B] );
     break;
    case 0x49:
     ld8b( C, regs[C] );
     break;
    case 0x4a:
     ld8b( C, regs[D] );
     break;
    case 0x4b:
     ld8b( C, regs[E] );
     break;
    case 0x4c:
     ld8b( C, regs[H] );
     break;
    case 0x4d:
     ld8b( C, regs[L] );
     break;
    case 0x4e:
     ld8b( C, readmem8b( H,L ) );
     break;
    case 0x4f:
     ld8b( C, regs[A] );
     break;
    case 0x50:
     ld8b( D, regs[B] );
     break;
    case 0x51:
     ld8b( D, regs[C] );
     break;
    case 0x52:
     ld8b( D, regs[D] );
     break;
    case 0x53:
     ld8b( D, regs[E] );
     break;
    case 0x54:
     ld8b( D, regs[H] );
     break;
    case 0x55:
     ld8b( D, regs[L] );
     break;
    case 0x56:
     ld8b( D, readmem8b( H,L ) );
     break;
    case 0x57:
     ld8b( D, regs[A] );
     break;
    case 0x58:
     ld8b( E, regs[B] );
     break;
    case 0x59:
     ld8b( E, regs[C] );
     break;
    case 0x5a:
     ld8b( E, regs[D] );
     break;
    case 0x5b:
     ld8b( E, regs[E] );
     break;
    case 0x5c:
     ld8b( E, regs[H] );
     break;
    case 0x5d:
     ld8b( E, regs[L] );
     break;
    case 0x5e:
     ld8b( E, readmem8b( H,L ) );
     break;
    case 0x5f:
     ld8b( E, regs[A] );
     break;
    case 0x60:
     ld8b( H, regs[B] );
     break;
    case 0x61:
     ld8b( H, regs[C] );
     break;
    case 0x62:
     ld8b( H, regs[D] );
     break;
    case 0x63:
     ld8b( H, regs[E] );
     break;
    case 0x64:
     ld8b( H, regs[H] );
     break;
    case 0x65:
     ld8b( H, regs[L] );
     break;
    case 0x66:
     ld8b( H, readmem8b( H,L ) );
     break;
    case 0x67:
     ld8b( H, regs[A] );
     break;
    case 0x68:
     ld8b( L, regs[B] );
     break;
    case 0x69:
     ld8b( L, regs[C] );
     break;
    case 0x6a:
     ld8b( L, regs[D] );
     break;
    case 0x6b:
     ld8b( L, regs[E] );
     break;
    case 0x6c:
     ld8b( L, regs[H] );
     break;
    case 0x6d:
     ld8b( L, regs[L] );
     break;
    case 0x6e:
     ld8b( L, readmem8b( H,L ) );
     break;
    case 0x6f:
     ld8b( L, regs[A] );
     break;
    case 0x70:
     writemem8b(H,L, regs[B]);
     break;
    case 0x71:
     writemem8b(H,L, regs[C]);
     break;
    case 0x72:
     writemem8b(H,L, regs[D]);
     break;
    case 0x73:
     writemem8b(H,L, regs[E]);
     break;
    case 0x74:
     writemem8b(H,L, regs[H]);
     break;
    case 0x75:
     writemem8b(H,L, regs[L]);
     break;
    case 0x76:
     if ((IE==0) || (!IME))
      System.out.println("PANIC: we will never unhalt!!!\n");
     halted = true;
     break;
    case 0x77:
     writemem8b(H,L, regs[A]);
     break;
    case 0x78:
     ld8b( A, regs[B] );
     break;
    case 0x79:
     ld8b( A, regs[C] );
     break;
    case 0x7a:
     ld8b( A, regs[D] );
     break;
    case 0x7b:
     ld8b( A, regs[E] );
     break;
    case 0x7c:
     ld8b( A, regs[H] );
     break;
    case 0x7d:
     ld8b( A, regs[L] );
     break;
    case 0x7e:
     ld8b( A, readmem8b( H,L ) );
     break;
    case 0x7f:
     ld8b( A, regs[A] );
     break;
    case 0x80:
     add8b( A, regs[B] );
     break;
    case 0x81:
     add8b( A, regs[C] );
     break;
    case 0x82:
     add8b( A, regs[D] );
     break;
    case 0x83:
     add8b( A, regs[E] );
     break;
    case 0x84:
     add8b( A, regs[H] );
     break;
    case 0x85:
     add8b( A, regs[L] );
     break;
    case 0x86:
     add8b( A, readmem8b( H,L ) );
     break;
    case 0x87:
     add8b( A, regs[A] );
     break;
    case 0x88:
     adc( A, regs[B] );
     break;
    case 0x89:
     adc( A, regs[C] );
     break;
    case 0x8a:
     adc( A, regs[D] );
     break;
    case 0x8b:
     adc( A, regs[E] );
     break;
    case 0x8c:
     adc( A, regs[H] );
     break;
    case 0x8d:
     adc( A, regs[L] );
     break;
    case 0x8e:
     adc( A, readmem8b( H,L ) );
     break;
    case 0x8f:
     adc( A, regs[A] );
     break;
    case 0x90:
     sub8b(A, regs[B]);
     break;
    case 0x91:
     sub8b(A, regs[C]);
     break;
    case 0x92:
     sub8b(A, regs[D]);
     break;
    case 0x93:
     sub8b(A, regs[E]);
     break;
    case 0x94:
     sub8b(A, regs[H]);
     break;
    case 0x95:
     sub8b(A, regs[L]);
     break;
    case 0x96:
     sub8b(A, readmem8b(H, L));
     break;
    case 0x97:
     sub8b(A, regs[A]);
     break;
    case 0x98:
     sbc( A, regs[B] );
     break;
    case 0x99:
     sbc( A, regs[C] );
     break;
    case 0x9a:
     sbc( A, regs[D] );
     break;
    case 0x9b:
     sbc( A, regs[E] );
     break;
    case 0x9c:
     sbc( A, regs[H] );
     break;
    case 0x9d:
     sbc( A, regs[L] );
     break;
    case 0x9e:
     sbc( A, readmem8b( H,L ) );
     break;
    case 0x9f:
     sbc( A, regs[A] );
     break;
    case 0xa0:
     and( regs[B] );
     break;
    case 0xa1:
     and( regs[C] );
     break;
    case 0xa2:
     and( regs[D] );
     break;
    case 0xa3:
     and( regs[E] );
     break;
    case 0xa4:
     and( regs[H] );
     break;
    case 0xa5:
     and( regs[L] );
     break;
    case 0xa6:
     and( readmem8b( H,L ) );
     break;
    case 0xa7:
     and( regs[A] );
     break;
    case 0xa8:
     xor( regs[B] );
     break;
    case 0xa9:
     xor( regs[C] );
     break;
    case 0xaa:
     xor( regs[D] );
     break;
    case 0xab:
     xor( regs[E] );
     break;
    case 0xac:
     xor( regs[H] );
     break;
    case 0xad:
     xor( regs[L] );
     break;
    case 0xae:
     xor( readmem8b( H,L ) );
     break;
    case 0xaf:
     xor( regs[A] );
     break;
    case 0xb0:
     or( regs[B] );
     break;
    case 0xb1:
     or( regs[C] );
     break;
    case 0xb2:
     or( regs[D] );
     break;
    case 0xb3:
     or( regs[E] );
     break;
    case 0xb4:
     or( regs[H] );
     break;
    case 0xb5:
     or( regs[L] );
     break;
    case 0xb6:
     or( readmem8b( H,L ) );
     break;
    case 0xb7:
     or( regs[A] );
     break;
    case 0xb8:
     cp( regs[B] );
     break;
    case 0xb9:
     cp( regs[C] );
     break;
    case 0xba:
     cp( regs[D] );
     break;
    case 0xbb:
     cp( regs[E] );
     break;
    case 0xbc:
     cp( regs[H] );
     break;
    case 0xbd:
     cp( regs[L] );
     break;
    case 0xbe:
     cp( readmem8b( H,L ) );
     break;
    case 0xbf:
     cp( regs[A] );
     break;
    case 0xc0:
     if ((regs[F]&ZF_Mask) != ZF_Mask)
      PC = pop();
     break;
    case 0xc1:{
     int x = pop();
     regs[B] = x >> 8;
     regs[C] = x&0xff;
    };break;
    case 0xc2:
     if (( regs[FLAG_REG]&ZF_Mask )!=ZF_Mask )
      JPnn();
     else
      PC+=2;
     break;
    case 0xc3:
     JPnn();
     break;
    case 0xc4:
     if (( regs[FLAG_REG]&ZF_Mask )!=ZF_Mask ) {
      push( PC+2 );
      JPnn();
     } else
      PC += 2;
     break;
    case 0xc5:
     push( regs[B]<<8 | regs[C]);
     break;
    case 0xc6:
     add8b(A, read(PC++));
     break;
    case 0xc7:
     push(PC);
     PC = 0x00;
     break;
    case 0xc8:
     if ((regs[F]&ZF_Mask) == ZF_Mask)
      PC = pop();
     break;
    case 0xc9:
     PC = pop();
     break;
    case 0xca:
     if (( regs[FLAG_REG]&ZF_Mask )==ZF_Mask )
      JPnn();
     else
      PC+=2;
     break;


    case 0xcc:
     if (( regs[FLAG_REG]&ZF_Mask )==ZF_Mask ) {
      push( PC+2 );
      JPnn();
     } else
      PC += 2;
     break;
    case 0xcd:
     push( PC+2 );
     JPnn();
     break;
    case 0xce:
     adc(A, read(PC++));
     break;
    case 0xcf:
     push(PC);
     PC = 0x08;
     break;
    case 0xd0:
     if ((regs[F]&CF_Mask) != CF_Mask)
      PC = pop();
     break;
    case 0xd1:{
     int x = pop();
     regs[D] = x >> 8;
     regs[E] = x&0xff;
    };break;
    case 0xd2:
     if (( regs[FLAG_REG]&CF_Mask )!=CF_Mask )
      JPnn();
     else
      PC+=2;
     break;


    case 0xd4:
     if (( regs[FLAG_REG]&CF_Mask )==0 ) {
      push( PC+2 );
      JPnn();
     } else
      PC += 2;
     break;
    case 0xd5:
     push( regs[D]<<8 | regs[E]);
     break;
    case 0xd6:
     sub8b(A, read(PC++));
     break;
    case 0xd7:
     push(PC);
     PC = 0x10;
     break;
    case 0xd8:
     if ((regs[F]&CF_Mask) == CF_Mask)
      PC = pop();
     break;
    case 0xd9:
     IME = true;
     PC = pop();
     break;
    case 0xda:
     if (( regs[FLAG_REG]&CF_Mask )!= 0 )
      JPnn();
     else
      PC+=2;
     break;


    case 0xdc:
     if (( regs[FLAG_REG]&CF_Mask )!=0 ) {
      push( PC+2 );
      JPnn();
     } else
      PC += 2;
     break;


    case 0xde:
     sbc(A, read(PC++));
     break;
    case 0xdf:
     push(PC);
     PC = 0x18;
     break;
    case 0xe0:
     write( 0xff00 | read( PC++ ), regs[A] );
     break;
    case 0xe1:{
     int x = pop();
     regs[H] = x >> 8;
     regs[L] = x&0xff;
    };break;
    case 0xe2:
     write( 0xff00 | regs[C], regs[A] );
     break;




    case 0xe5:
     push( regs[H]<<8 | regs[L]);
     break;
    case 0xe6:
     and(read(PC++));
     break;
    case 0xe7:
     push(PC);
     PC = 0x20;
     break;
    case 0xe8:{
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
    };break;
    case 0xe9:
     PC = (regs[H]<<8) | regs[L];
     break;
    case 0xea:{
     int a = read( PC++ );
     int b = read( PC++ );
     write((b<<8) | a, regs[A] );
    };break;






    case 0xee:
     xor( read( PC++ ) );
     break;
    case 0xef:
     push(PC);
     PC = 0x28;
     break;
    case 0xf0:
     regs[A] = read( 0xff00 | read( PC++ ) );
     break;
    case 0xf1:{
     int x = pop();
     regs[A] = x >> 8;
     regs[F] = x&0xff;
    };break;
    case 0xf2:

     regs[A] = read( 0xff00 | regs[C] );
     break;
    case 0xf3:
     IME = false;
     break;


    case 0xf5:
     push( regs[A]<<8 | regs[F]);
     break;
    case 0xf6:
     or(read(PC++));
     break;
    case 0xf7:
     push(PC);
     PC = 0x30;
     break;
    case 0xf8:{
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
    };break;
    case 0xf9:
     SP = regs[H]<<8 | regs[L];
     break;
    case 0xfa:{
     int a = read( PC++ );
     int b = read( PC++ );
     regs[A] = read((b<<8) | a);
    };break;
    case 0xfb:
     IME = true;
     break;




    case 0xfe:
     cp( read( PC++ ) );
     break;
    case 0xff:
     push(PC);
     PC = 0x38;
     break;
    case 0xcb:
     op = read( PC++ );
     cycles = cb_cycles_table[op];
     switch ( op ) {
      case 0x00:
       regs[B] = rolc(regs[B]);
       break;
      case 0x01:
       regs[C] = rolc(regs[C]);
       break;
      case 0x02:
       regs[D] = rolc(regs[D]);
       break;
      case 0x03:
       regs[E] = rolc(regs[E]);
       break;
      case 0x04:
       regs[H] = rolc(regs[H]);
       break;
      case 0x05:
       regs[L] = rolc(regs[L]);
       break;
      case 0x06:
       writemem8b(H, L, rolc(readmem8b(H, L)));
       break;
      case 0x07:
       regs[A] = rolc(regs[A]);
       break;
      case 0x08:
       regs[B] = rorc(regs[B]);
       break;
      case 0x09:
       regs[C] = rorc(regs[C]);
       break;
      case 0x0a:
       regs[D] = rorc(regs[D]);
       break;
      case 0x0b:
       regs[E] = rorc(regs[E]);
       break;
      case 0x0c:
       regs[H] = rorc(regs[H]);
       break;
      case 0x0d:
       regs[L] = rorc(regs[L]);
       break;
      case 0x0e:
       writemem8b(H, L, rorc(readmem8b(H, L)));
       break;
      case 0x0f:
       regs[A] = rorc(regs[A]);
       break;
      case 0x10:
       regs[B] = rol(regs[B]);
       break;
      case 0x11:
       regs[C] = rol(regs[C]);
       break;
      case 0x12:
       regs[D] = rol(regs[D]);
       break;
      case 0x13:
       regs[E] = rol(regs[E]);
       break;
      case 0x14:
       regs[H] = rol(regs[H]);
       break;
      case 0x15:
       regs[L] = rol(regs[L]);
       break;
      case 0x16:
       writemem8b(H, L, rol(readmem8b(H, L)));
       break;
      case 0x17:
       regs[A] = rol(regs[A]);
       break;
      case 0x18:
       regs[B] = ror(regs[B]);
       break;
      case 0x19:
       regs[C] = ror(regs[C]);
       break;
      case 0x1a:
       regs[D] = ror(regs[D]);
       break;
      case 0x1b:
       regs[E] = ror(regs[E]);
       break;
      case 0x1c:
       regs[H] = ror(regs[H]);
       break;
      case 0x1d:
       regs[L] = ror(regs[L]);
       break;
      case 0x1e:
       writemem8b(H, L, ror(readmem8b(H, L)));
       break;
      case 0x1f:
       regs[A] = ror(regs[A]);
       break;
      case 0x20:
       regs[B] = shla(regs[B]);
       break;
      case 0x21:
       regs[C] = shla(regs[C]);
       break;
      case 0x22:
       regs[D] = shla(regs[D]);
       break;
      case 0x23:
       regs[E] = shla(regs[E]);
       break;
      case 0x24:
       regs[H] = shla(regs[H]);
       break;
      case 0x25:
       regs[L] = shla(regs[L]);
       break;
      case 0x26:
       writemem8b(H, L, shla(readmem8b(H, L)));
       break;
      case 0x27:
       regs[A] = shla(regs[A]);
       break;
      case 0x28:
       regs[B] = shra(regs[B]);
       break;
      case 0x29:
       regs[C] = shra(regs[C]);
       break;
      case 0x2a:
       regs[D] = shra(regs[D]);
       break;
      case 0x2b:
       regs[E] = shra(regs[E]);
       break;
      case 0x2c:
       regs[H] = shra(regs[H]);
       break;
      case 0x2d:
       regs[L] = shra(regs[L]);
       break;
      case 0x2e:
       writemem8b(H, L, shra(readmem8b(H, L)));
       break;
      case 0x2f:
       regs[A] = shra(regs[A]);
       break;
      case 0x30:
       regs[B] = ((regs[B]&0x0f)<< 4) | ((regs[B]&0xf0) >> 4);
       break;
      case 0x31:
       regs[C] = ((regs[C]&0x0f)<< 4) | ((regs[C]&0xf0) >> 4);
       break;
      case 0x32:
       regs[D] = ((regs[D]&0x0f)<< 4) | ((regs[D]&0xf0) >> 4);
       break;
      case 0x33:
       regs[E] = ((regs[E]&0x0f)<< 4) | ((regs[E]&0xf0) >> 4);
       break;
      case 0x34:
       regs[H] = ((regs[H]&0x0f)<< 4) | ((regs[H]&0xf0) >> 4);
       break;
      case 0x35:
       regs[L] = ((regs[L]&0x0f)<< 4) | ((regs[L]&0xf0) >> 4);
       break;
      case 0x36:{
       int x = readmem8b(H, L);
       x = ((x&0x0f)<< 4) | ((x&0xf0) >> 4);
       writemem8b(H,L, x);
      };break;
      case 0x37:
       regs[A] = ((regs[A]&0x0f)<< 4) | ((regs[A]&0xf0) >> 4);
       break;
      case 0x38:
       regs[B] = shrl(regs[B]);
       break;
      case 0x39:
       regs[C] = shrl(regs[C]);
       break;
      case 0x3a:
       regs[D] = shrl(regs[D]);
       break;
      case 0x3b:
       regs[E] = shrl(regs[E]);
       break;
      case 0x3c:
       regs[H] = shrl(regs[H]);
       break;
      case 0x3d:
       regs[L] = shrl(regs[L]);
       break;
      case 0x3e:
       writemem8b(H, L, shrl(readmem8b(H, L)));
       break;
      case 0x3f:
       regs[A] = shrl(regs[A]);
       break;
      case 0x40:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x41:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x42:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x43:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x44:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x45:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x46:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x47:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<0))==0 ? ZF_Mask : 0;
       break;
      case 0x48:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x49:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4a:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4b:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4c:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4d:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4e:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x4f:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<1))==0 ? ZF_Mask : 0;
       break;
      case 0x50:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x51:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x52:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x53:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x54:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x55:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x56:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x57:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<2))==0 ? ZF_Mask : 0;
       break;
      case 0x58:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x59:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5a:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5b:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5c:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5d:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5e:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x5f:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<3))==0 ? ZF_Mask : 0;
       break;
      case 0x60:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x61:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x62:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x63:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x64:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x65:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x66:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x67:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<4))==0 ? ZF_Mask : 0;
       break;
      case 0x68:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x69:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6a:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6b:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6c:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6d:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6e:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x6f:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<5))==0 ? ZF_Mask : 0;
       break;
      case 0x70:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x71:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x72:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x73:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x74:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x75:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x76:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x77:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<6))==0 ? ZF_Mask : 0;
       break;
      case 0x78:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[B]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x79:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[C]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7a:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[D]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7b:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[E]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7c:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[H]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7d:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[L]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7e:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (readmem8b(H, L)&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x7f:
       regs[F] &= CF_Mask;
       regs[F] |= HC_Mask;
       regs[F] |= (regs[A]&(1<<7))==0 ? ZF_Mask : 0;
       break;
      case 0x80:
       regs[B] &= ~( 1 << 0 );
       break;
      case 0x81:
       regs[C] &= ~( 1 << 0 );
       break;
      case 0x82:
       regs[D] &= ~( 1 << 0 );
       break;
      case 0x83:
       regs[E] &= ~( 1 << 0 );
       break;
      case 0x84:
       regs[H] &= ~( 1 << 0 );
       break;
      case 0x85:
       regs[L] &= ~( 1 << 0 );
       break;
      case 0x86:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 0 ) );
       break;
      case 0x87:
       regs[A] &= ~( 1 << 0 );
       break;
      case 0x88:
       regs[B] &= ~( 1 << 1 );
       break;
      case 0x89:
       regs[C] &= ~( 1 << 1 );
       break;
      case 0x8a:
       regs[D] &= ~( 1 << 1 );
       break;
      case 0x8b:
       regs[E] &= ~( 1 << 1 );
       break;
      case 0x8c:
       regs[H] &= ~( 1 << 1 );
       break;
      case 0x8d:
       regs[L] &= ~( 1 << 1 );
       break;
      case 0x8e:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 1 ) );
       break;
      case 0x8f:
       regs[A] &= ~( 1 << 1 );
       break;
      case 0x90:
       regs[B] &= ~( 1 << 2 );
       break;
      case 0x91:
       regs[C] &= ~( 1 << 2 );
       break;
      case 0x92:
       regs[D] &= ~( 1 << 2 );
       break;
      case 0x93:
       regs[E] &= ~( 1 << 2 );
       break;
      case 0x94:
       regs[H] &= ~( 1 << 2 );
       break;
      case 0x95:
       regs[L] &= ~( 1 << 2 );
       break;
      case 0x96:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 2 ) );
       break;
      case 0x97:
       regs[A] &= ~( 1 << 2 );
       break;
      case 0x98:
       regs[B] &= ~( 1 << 3 );
       break;
      case 0x99:
       regs[C] &= ~( 1 << 3 );
       break;
      case 0x9a:
       regs[D] &= ~( 1 << 3 );
       break;
      case 0x9b:
       regs[E] &= ~( 1 << 3 );
       break;
      case 0x9c:
       regs[H] &= ~( 1 << 3 );
       break;
      case 0x9d:
       regs[L] &= ~( 1 << 3 );
       break;
      case 0x9e:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 3 ) );
       break;
      case 0x9f:
       regs[A] &= ~( 1 << 3 );
       break;
      case 0xa0:
       regs[B] &= ~( 1 << 4 );
       break;
      case 0xa1:
       regs[C] &= ~( 1 << 4 );
       break;
      case 0xa2:
       regs[D] &= ~( 1 << 4 );
       break;
      case 0xa3:
       regs[E] &= ~( 1 << 4 );
       break;
      case 0xa4:
       regs[H] &= ~( 1 << 4 );
       break;
      case 0xa5:
       regs[L] &= ~( 1 << 4 );
       break;
      case 0xa6:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 4 ) );
       break;
      case 0xa7:
       regs[A] &= ~( 1 << 4 );
       break;
      case 0xa8:
       regs[B] &= ~( 1 << 5 );
       break;
      case 0xa9:
       regs[C] &= ~( 1 << 5 );
       break;
      case 0xaa:
       regs[D] &= ~( 1 << 5 );
       break;
      case 0xab:
       regs[E] &= ~( 1 << 5 );
       break;
      case 0xac:
       regs[H] &= ~( 1 << 5 );
       break;
      case 0xad:
       regs[L] &= ~( 1 << 5 );
       break;
      case 0xae:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 5 ) );
       break;
      case 0xaf:
       regs[A] &= ~( 1 << 5 );
       break;
      case 0xb0:
       regs[B] &= ~( 1 << 6 );
       break;
      case 0xb1:
       regs[C] &= ~( 1 << 6 );
       break;
      case 0xb2:
       regs[D] &= ~( 1 << 6 );
       break;
      case 0xb3:
       regs[E] &= ~( 1 << 6 );
       break;
      case 0xb4:
       regs[H] &= ~( 1 << 6 );
       break;
      case 0xb5:
       regs[L] &= ~( 1 << 6 );
       break;
      case 0xb6:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 6 ) );
       break;
      case 0xb7:
       regs[A] &= ~( 1 << 6 );
       break;
      case 0xb8:
       regs[B] &= ~( 1 << 7 );
       break;
      case 0xb9:
       regs[C] &= ~( 1 << 7 );
       break;
      case 0xba:
       regs[D] &= ~( 1 << 7 );
       break;
      case 0xbb:
       regs[E] &= ~( 1 << 7 );
       break;
      case 0xbc:
       regs[H] &= ~( 1 << 7 );
       break;
      case 0xbd:
       regs[L] &= ~( 1 << 7 );
       break;
      case 0xbe:
       writemem8b( H,L, readmem8b( H,L ) & ~( 1 << 7 ) );
       break;
      case 0xbf:
       regs[A] &= ~( 1 << 7 );
       break;
      case 0xc0:
       regs[B] |= ( 1 << 0 );
       break;
      case 0xc1:
       regs[C] |= ( 1 << 0 );
       break;
      case 0xc2:
       regs[D] |= ( 1 << 0 );
       break;
      case 0xc3:
       regs[E] |= ( 1 << 0 );
       break;
      case 0xc4:
       regs[H] |= ( 1 << 0 );
       break;
      case 0xc5:
       regs[L] |= ( 1 << 0 );
       break;
      case 0xc6:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 0 ) );
       break;
      case 0xc7:
       regs[A] |= ( 1 << 0 );
       break;
      case 0xc8:
       regs[B] |= ( 1 << 1 );
       break;
      case 0xc9:
       regs[C] |= ( 1 << 1 );
       break;
      case 0xca:
       regs[D] |= ( 1 << 1 );
       break;
      case 0xcb:
       regs[E] |= ( 1 << 1 );
       break;
      case 0xcc:
       regs[H] |= ( 1 << 1 );
       break;
      case 0xcd:
       regs[L] |= ( 1 << 1 );
       break;
      case 0xce:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 1 ) );
       break;
      case 0xcf:
       regs[A] |= ( 1 << 1 );
       break;
      case 0xd0:
       regs[B] |= ( 1 << 2 );
       break;
      case 0xd1:
       regs[C] |= ( 1 << 2 );
       break;
      case 0xd2:
       regs[D] |= ( 1 << 2 );
       break;
      case 0xd3:
       regs[E] |= ( 1 << 2 );
       break;
      case 0xd4:
       regs[H] |= ( 1 << 2 );
       break;
      case 0xd5:
       regs[L] |= ( 1 << 2 );
       break;
      case 0xd6:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 2 ) );
       break;
      case 0xd7:
       regs[A] |= ( 1 << 2 );
       break;
      case 0xd8:
       regs[B] |= ( 1 << 3 );
       break;
      case 0xd9:
       regs[C] |= ( 1 << 3 );
       break;
      case 0xda:
       regs[D] |= ( 1 << 3 );
       break;
      case 0xdb:
       regs[E] |= ( 1 << 3 );
       break;
      case 0xdc:
       regs[H] |= ( 1 << 3 );
       break;
      case 0xdd:
       regs[L] |= ( 1 << 3 );
       break;
      case 0xde:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 3 ) );
       break;
      case 0xdf:
       regs[A] |= ( 1 << 3 );
       break;
      case 0xe0:
       regs[B] |= ( 1 << 4 );
       break;
      case 0xe1:
       regs[C] |= ( 1 << 4 );
       break;
      case 0xe2:
       regs[D] |= ( 1 << 4 );
       break;
      case 0xe3:
       regs[E] |= ( 1 << 4 );
       break;
      case 0xe4:
       regs[H] |= ( 1 << 4 );
       break;
      case 0xe5:
       regs[L] |= ( 1 << 4 );
       break;
      case 0xe6:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 4 ) );
       break;
      case 0xe7:
       regs[A] |= ( 1 << 4 );
       break;
      case 0xe8:
       regs[B] |= ( 1 << 5 );
       break;
      case 0xe9:
       regs[C] |= ( 1 << 5 );
       break;
      case 0xea:
       regs[D] |= ( 1 << 5 );
       break;
      case 0xeb:
       regs[E] |= ( 1 << 5 );
       break;
      case 0xec:
       regs[H] |= ( 1 << 5 );
       break;
      case 0xed:
       regs[L] |= ( 1 << 5 );
       break;
      case 0xee:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 5 ) );
       break;
      case 0xef:
       regs[A] |= ( 1 << 5 );
       break;
      case 0xf0:
       regs[B] |= ( 1 << 6 );
       break;
      case 0xf1:
       regs[C] |= ( 1 << 6 );
       break;
      case 0xf2:
       regs[D] |= ( 1 << 6 );
       break;
      case 0xf3:
       regs[E] |= ( 1 << 6 );
       break;
      case 0xf4:
       regs[H] |= ( 1 << 6 );
       break;
      case 0xf5:
       regs[L] |= ( 1 << 6 );
       break;
      case 0xf6:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 6 ) );
       break;
      case 0xf7:
       regs[A] |= ( 1 << 6 );
       break;
      case 0xf8:
       regs[B] |= ( 1 << 7 );
       break;
      case 0xf9:
       regs[C] |= ( 1 << 7 );
       break;
      case 0xfa:
       regs[D] |= ( 1 << 7 );
       break;
      case 0xfb:
       regs[E] |= ( 1 << 7 );
       break;
      case 0xfc:
       regs[H] |= ( 1 << 7 );
       break;
      case 0xfd:
       regs[L] |= ( 1 << 7 );
       break;
      case 0xfe:
       writemem8b(H,L, readmem8b(H, L) | ( 1 << 7 ) );
       break;
      case 0xff:
       regs[A] |= ( 1 << 7 );
       break;
      default:
       System.out.printf( "UNKNOWN PREFIX INSTRUCTION: $%02x\n" , op );
       PC -= 2;
       return 0;
     }
     break;
    default:
     System.out.printf( "UNKNOWN INSTRUCTION: $%02x\n" , op );
     PC -= 1;
     return 0;
   }
   PC &= 0xffff;
   SP &= 0xffff;
   ++TotalInstrCount;
   cycles *= 4;
   TotalCycleCount += cycles;
   ++nopCount;
   if (!nop)
    nopCount=0;




   return cycles;
  }

  final public int nextinstruction() {
   int res = execute();
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
     if ((IOP[0x0f]&3)!=0) {


     }
    }





   }
   if (res > 30)
    System.out.printf("res=%i  PC=$%04x",res, PC);

   return res;
  }

  final protected int exception() {
   return lastException;
  }
}
