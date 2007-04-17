import javax.swing.*;
import java.awt.*;
import java.awt.image.*;


public class VideoController {
 private static final int MIN_WIDTH = 160;
 private static final int MIN_HEIGHT = 144;

 private JPanel listener = null;
 private Image drawImg[];
 private int curDrawImg = 0;
 private Object blitImg[][]=new Object[144][160];

 private int CurrentVRAMBank=0;

 protected int VRAM[]=new int[0x4000];
 protected int OAM[]=new int[0xa0];

 protected boolean isCGB;

 protected int LY=0;
 protected int LYC=0;
 protected int SCX=0;
 protected int SCY=0;
 protected int WX=0;
 protected int WY=0;
 protected int LCDC=0;
 protected int STAT=0;
  protected int GRAYSHADES[][] = { {0xa0, 0xe0, 0x20},
                                   {0x70, 0xb0, 0x40},
                                   {0x40, 0x70, 0x32},
                                   {0x10, 0x50, 0x26} };

 protected int BGPI=0;
 private int BGPD[]=new int[8*4*2];

 protected int OBPI=0;
 private int OBPD[]=new int[8*4*2];


 private int intColors[][] = new int[8*4*2][3];
 private Color colColors[] = new Color[8*4*2];
 private Object objColors[] = new Object[8*4*2];
 private int patpix[][][] = new int[4096][8][8];
 private boolean patdirty[] = new boolean[1024];
 private boolean anydirty = true;
 private boolean alldirty = true;

 private CPU cpu;

 private long pfreq;
 private long ptick;
 private long ftick;

 public int scale = 3;
 private int cfskip = 0;
 private int fskip = 1;

 public VideoController(CPU cpu, int image_width, int image_height) {
  this.cpu = cpu;
  drawImg=new Image[2];
  scale (image_width, image_height);
  this.isCGB = cpu.isCGB();

  long x = System.nanoTime();
  for (int i = 0; i < objColors.length; ++i)
   objColors[i] =
    ((BufferedImage)drawImg[curDrawImg^1]).
    getColorModel().
    getDataElements(0, null);
  for (int ty = 0; ty < 144; ++ty)
   for (int tx = 0; tx < 160; ++tx)
    blitImg[ty][tx] =
     ((BufferedImage)drawImg[curDrawImg^1]).
     getColorModel().
     getDataElements(0, null);
 }

 final public void addListener(JPanel panel)
 {
  listener = panel;

  this.scale(drawImg[0].getWidth(null), drawImg[0].getHeight(null));
 }

 public void scale(int width, int height) {
  if (width < scale*MIN_WIDTH) width = scale*MIN_WIDTH;
  if (height < scale*MIN_HEIGHT) height = scale*MIN_HEIGHT;

  if (listener == null) {
   System.out.println("creating BufferedImage's");
   drawImg[0]=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   drawImg[1]=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  } else {
   System.out.println("creating VolatileImage's");
   drawImg[0]=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   drawImg[1]=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  }
 }

 final public Image getImage() {

  return drawImg[curDrawImg];
 }

 final private void palQuadify(int[] col) {
  for (int i=0; i<3; ++i) {
   col[3+i] = col[i];
   col[6+i] = col[i];
   col[9+i] = col[i];
  }
 }

 final private void palChange(int palcol, int r, int g, int b) {
  intColors[palcol][0] = r;
  intColors[palcol][1] = g;
  intColors[palcol][2] = b;

  colColors[palcol] = new Color(r,g,b);

  objColors[palcol] =
   ((BufferedImage)drawImg[curDrawImg^1]).
   getColorModel().
   getDataElements((r<<16)|(g<<8)|(b<<0), null);
 }
 static long lastms;
 static int fps23fix=0;
 final private void blitImage() {

  WritableRaster wr = ((BufferedImage)drawImg[curDrawImg^1]).getRaster();
  if (scale == 1) {
   for (int y = 0; y < 144; ++y) {
    Object blitLine[] = blitImg[y];
    for (int x = 0; x < 160; ++x) {
     wr.setDataElements(x,y, blitLine[x]);
    }
   }
  }
  else if (scale == 2) {
   for (int y = 0; y < 144; ++y) {
    int yn = (y==0 )?0 :y-1;
    int yp = (y==143)?143:y+1;
    Object blitLine2[] = blitImg[y];
    Object blitLine1[] = blitImg[yn];
    Object blitLine3[] = blitImg[yp];
    for (int x = 0; x < 160; ++x) {
     int xn = (x==0 )?0 :x-1;
     int xp = (x==159)?159:x+1;
     if (!((blitLine2[xn]).equals(blitLine2[xp]))
     && !((blitLine1[x]).equals(blitLine3[x]))) {
      wr.setDataElements(x*2,y*2, ((blitLine1[x]).equals(blitLine2[xn])) ? blitLine2[xn] : blitLine2[x]);
      wr.setDataElements(x*2+1,y*2, ((blitLine1[x]).equals(blitLine2[xp])) ? blitLine2[xp] : blitLine2[x]);
      wr.setDataElements(x*2,y*2+1, ((blitLine3[x]).equals(blitLine2[xn])) ? blitLine2[xn] : blitLine2[x]);
      wr.setDataElements(x*2+1,y*2+1, ((blitLine3[x]).equals(blitLine2[xp])) ? blitLine2[xp] : blitLine2[x]);
     }
     else {
      Object col = blitLine2[x];
      wr.setDataElements(x*2,y*2, col);
      wr.setDataElements(x*2+1,y*2, col);
      wr.setDataElements(x*2,y*2+1, col);
      wr.setDataElements(x*2+1,y*2+1, col);
     }
    }
   }
  } else if (scale == 3) {

   for (int y = 0; y < 144; ++y) {
    int yn = (y==0 )?0 :y-1;
    int yp = (y==143)?143:y+1;
    Object blitLine2[] = blitImg[y];
    Object blitLine1[] = blitImg[yn];
    Object blitLine3[] = blitImg[yp];
    for (int x = 0; x < 160; ++x) {
     int xn = (x==0 )?0 :x-1;
     int xp = (x==159)?159:x+1;
     if (!((blitLine1[x]).equals(blitLine3[x])) && !((blitLine2[xn]).equals(blitLine2[xp]))) {
      wr.setDataElements(x*3,y*3, ((blitLine2[xn]).equals(blitLine1[x])) ? blitLine2[xn] : blitLine2[x]);
      wr.setDataElements(x*3+1,y*3, (((blitLine2[xn]).equals(blitLine1[x])) && !((blitLine2[x]).equals(blitLine1[xp]))) || (((blitLine1[x]).equals(blitLine2[xp])) && !((blitLine2[x]).equals(blitLine1[xn])))? blitLine1[x] : blitLine2[x]);
      wr.setDataElements(x*3+2,y*3, ((blitLine1[x]).equals(blitLine2[xp])) ? blitLine2[xp] : blitLine2[x]);
      wr.setDataElements(x*3,y*3+1, (((blitLine2[xn]).equals(blitLine1[x])) && !((blitLine2[x]).equals(blitLine3[xn]))) || (((blitLine2[xn]).equals(blitLine3[x])) && !((blitLine2[x]).equals(blitLine1[xn])))? blitLine2[xn] : blitLine2[x]);
      wr.setDataElements(x*3+1,y*3+1, blitLine2[x]);
      wr.setDataElements(x*3+2,y*3+1, (((blitLine1[x]).equals(blitLine2[xp])) && !((blitLine2[x]).equals(blitLine3[xp]))) || (((blitLine3[x]).equals(blitLine2[xp])) && !((blitLine2[x]).equals(blitLine1[xp])))? blitLine2[xp] : blitLine2[x]);
      wr.setDataElements(x*3,y*3+2, ((blitLine2[xn]).equals(blitLine3[x])) ? blitLine2[xn] : blitLine2[x]);
      wr.setDataElements(x*3+1,y*3+2, (((blitLine2[xn]).equals(blitLine3[x])) && !((blitLine2[x]).equals(blitLine3[xp]))) || (((blitLine3[x]).equals(blitLine2[xp])) && !((blitLine2[x]).equals(blitLine3[xn])))? blitLine3[x] : blitLine2[x]);
      wr.setDataElements(x*3+2,y*3+2, ((blitLine3[x]).equals(blitLine2[xp])) ? blitLine2[xp] : blitLine2[x]);
     } else {
      Object col = blitLine2[x];
      wr.setDataElements(x*3,y*3, col);
      wr.setDataElements(x*3+1,y*3, col);
      wr.setDataElements(x*3+2,y*3, col);
      wr.setDataElements(x*3,y*3+1, col);
      wr.setDataElements(x*3+1,y*3+1, col);
      wr.setDataElements(x*3+2,y*3+1, col);
      wr.setDataElements(x*3,y*3+2, col);
      wr.setDataElements(x*3+1,y*3+2, col);
      wr.setDataElements(x*3+2,y*3+2, col);
     }
    }
   }
  }
  curDrawImg ^= 1;
 }

 final public void setMonoColData(int index, int value) {




  if (isCGB) return;

  if (index==0) index= (0x20>>2);
  else --index;
  int temp[] = new int[3];
  temp = GRAYSHADES[(value>>0)&3]; palChange((index<<2) | 0, temp[0], temp[1], temp[2]);
  temp = GRAYSHADES[(value>>2)&3]; palChange((index<<2) | 1, temp[0], temp[1], temp[2]);
  temp = GRAYSHADES[(value>>4)&3]; palChange((index<<2) | 2, temp[0], temp[1], temp[2]);
  temp = GRAYSHADES[(value>>6)&3]; palChange((index<<2) | 3, temp[0], temp[1], temp[2]);
 }

 final public void setBGColData(int value) {
  BGPD[BGPI&0x3f] = value;


  int base = (BGPI & 0x3e);
  int data = BGPD[base] | (BGPD[base+1]<<8);
  int palnum = base >> 3;
  int colnum = (base >> 1) & 3;
  int r = (data >> 0) & 0x1F;
  int g = (data >> 5) & 0x1F;
  int b = (data >> 10) & 0x1F;

  r <<= 3; r |= (r >> 5);
  g <<= 3; g |= (g >> 5);
  b <<= 3; b |= (b >> 5);





  palChange((palnum << 2) | colnum | 0x20, r, g, b);

  if ((BGPI&(1<<7))!=0)
   ++BGPI;
 }

 final public int getBGColData() {
  return BGPD[BGPI&0x3f];
 }

 final public void setOBColData(int value) {
  OBPD[OBPI&0x3f] = value;


  int base = (OBPI & 0x3e);
  int data = OBPD[base] | (OBPD[base+1]<<8);
  int palnum = base >> 3;
  int colnum = (base >> 1) & 3;
  int r = (data >> 0) & 0x1F;
  int g = (data >> 5) & 0x1F;
  int b = (data >> 10) & 0x1F;

  r <<= 3; r |= (r >> 5);
  g <<= 3; g |= (g >> 5);
  b <<= 3; b |= (b >> 5);

  palChange((palnum << 2) | colnum, r, g, b);

  if ((OBPI&(1<<7))!=0)
   ++OBPI;
 }

 final public int getOBColData() {
  return OBPD[OBPI&0x3f];
 }
 final private void updatepatpix() {
  if (!anydirty )
   return;

  for (int i = 0; i < 1024; ++i)
  {
   if (i == 384) i = 512;
   if (i == 896) break;
   if (!patdirty[i] ) continue;
   patdirty[i] = false;

   for (int y = 0; y < 8; ++y) {
    int lineofs = (i*16) + (y*2);
    for (int x = 0; x < 8; ++x) {

     int col = (VRAM[lineofs]>>x)&1;
     col |= ((VRAM[lineofs+1]>>x)&1)<<1;
     patpix[i] [y] [7-x] = col;
     patpix[i+1024][y] [x] = col;
     patpix[i+2048][7-y][7-x] = col;
     patpix[i+3072][7-y][x] = col;
    }
   }
  }
  anydirty = false;

 }

 final public boolean renderNextScanline() {
  ++LY;
  if (LY >= 154)
   LY = 0;

  STAT &= ~(1<<2);
  if (LY==LYC) {
   STAT |= 1<<2;
   if ((STAT&(1<<6))!=0)
    cpu.triggerInterrupt(1);
  }

  if (LY < 144) {
   if (cfskip == 0)
    renderScanLine();
   STAT &= ~(3);
   if ((STAT&(1<<3))!=0)
    cpu.triggerInterrupt(1);
  }

  if (LY == 144) {
   if (cfskip == 0) {
    blitImage();
    if (listener != null) listener.updateUI();
   }
   cfskip--;
   if (cfskip < 0) cfskip += fskip;
   STAT &= ~(3);
   STAT |= 1;
   if ((STAT&(1<<4))!=0)
    cpu.triggerInterrupt(1);
   cpu.triggerInterrupt(0);
  }
  return (LY == 144);
 }

 final public int read(int index) {
  if(index<0x8000) {
   System.out.println("Error: VideoController.read(): Reading from non VideoController-Address "+index);
  }
  else if(index < 0xa000) {
   return VRAM[index-0x8000+CurrentVRAMBank];
  }
  else if(index<0xfe00) {
   System.out.println("Error: VideoController.read(): Reading from non VideoController-Address "+index);
  }
  else if(index<0xfea0) {
   return OAM[index-0xfe00];
  }
  else {
   System.out.println("Error: VideoController.read(): Reading from non VideoController-Address "+index);
  }
  return VRAM[index+CurrentVRAMBank];
 }

 final public void write(int index, int value) {
  if(index<0x8000) {
   System.out.println("Error: VideoController.write(): Writing to non VideoController-Address "+index+" value="+value);
  }
  else if(index < 0xa000) {
   VRAM[index-0x8000+CurrentVRAMBank]=value;
   patdirty[(CurrentVRAMBank>>4)+((index-0x8000)>>4)] = true;
   anydirty = true;

  }
  else if(index<0xfe00) {
   System.out.println("Error: VideoController.write(): Writing to non VideoController-Address "+index+" value="+value);
  }
  else if(index<0xfea0) {
   OAM[index-0xfe00]=value;
  }
  else {
   System.out.println("Error: VideoController.write(): Writing to non VideoController-Address "+index+" value="+value);
  }
 }

 final public void selectVRAMBank(int i) {
  CurrentVRAMBank=i*0x2000;
  if ((i <0) || (i > 1))
   System.out.printf("current offset=%x\n",CurrentVRAMBank);
 }

 final public int getcurVRAMBank() {
  return CurrentVRAMBank/0x2000;
 }




 private static int TileData;
 private static int BGTileMap;
 private static int WindowTileMap;
 private static int bgY;
 private static int bgTileY;
 private static int bgOffsY;
 private static int bgX;
 private static int bgTileX;
 private static int bgOffsX;
 private static int windX;
 private static int tilebufBG[] = new int[0x200];
 private static Object blitLine[];



 final private void renderScanLine() {
  if((LCDC&(1<<7))!=0) {

   updatepatpix();

   blitLine = blitImg[LY];

   TileData = ((LCDC&(1<<4))==0) ? 0x0800 : 0x0000;
   BGTileMap = ((LCDC&(1<<3))==0) ? 0x1800 : 0x1c00;
   WindowTileMap = ((LCDC&(1<<6))==0) ? 0x1800 : 0x1c00;





   int BGPrio = (LCDC&(1<<0));

   windX = 160;
   if(((LCDC&(1<<5))!=0)
   && (WX >= 0) && (WX < 167)
   && (WY >= 0) && (WY < 144)
   && (LY >= WY))
    windX = (WX - 7);

   renderScanlineBG();

   if (windX < 160) {
    renderScanlineWindow();
   }

   if((LCDC&(1<<1))!=0) {
    renderScanlineSprites();
   }
  }
 }

 private int lbgTileY = -1;

 final private void calcBGTileBuf() {





  int tileMap = BGTileMap + bgTileX + (bgTileY*32);
  int attrMap = tileMap + 0x2000;
  int bufMap = 0;
  int cnt = ((windX+7) >> 3) + 1;

  for (int i = 0; i < cnt; ++i) {
   int tile = VRAM[tileMap++];
   int attr = VRAM[attrMap++];
   if (TileData == 0x0800) {
    tile ^= 0x80;
    tile += 0x80;
   }
   tilebufBG[bufMap++] = tile |
    ((attr & 0x08) << 6) |
    ((attr & 0x60) << 5);
   tilebufBG[bufMap++] = ((attr&7) | 0x08) << 2;
   if ((tileMap&31)==0) {
    tileMap -= 32;
    attrMap -= 32;
   }
  }
 }

 final private void renderScanlineBG() {
  int bufMap = 0;
  int cnt = windX;
  if (cnt == 0) return;

  bgY = (SCY+LY)&0xFF;
  bgTileY = bgY >> 3;
  bgOffsY = bgY & 7;
  bgX = SCX;
  bgTileX = bgX >> 3;
  bgOffsX = bgX & 7;

  calcBGTileBuf();

  int PatLine[] = patpix[tilebufBG[bufMap++]][bgOffsY];
  int TilePal = tilebufBG[bufMap++];
  int curX = 0;

  for (int t = bgOffsX; t < 8; ++t, --cnt)
   { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };

  if (cnt == 0) return;

  while (cnt>=8) {
   PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
   TilePal = tilebufBG[bufMap++];
   for (int t = 0; t < 8; ++t)
    { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };
   cnt -= 8;
  }
  PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
  TilePal = tilebufBG[bufMap++];
  for (int t = 0; cnt > 0; --cnt, ++t)
   { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };
 }

 final private void calcWindTileBuf() {
  int tileMap = WindowTileMap + (bgTileY*32);
  int attrMap = tileMap + 0x2000;
  int bufMap = 0;
  int cnt = ((160-(windX+7)) >> 3) + 2;

  for (int i = 0; i < cnt; ++i) {
   int tile = VRAM[tileMap++];
   int attr = VRAM[attrMap++];
   if (TileData == 0x0800) {
    tile ^= 0x80;
    tile += 0x80;
   }
   tilebufBG[bufMap++] = tile |
    ((attr & 0x08) << 6) |
    ((attr & 0x60) << 5);
   tilebufBG[bufMap++] = ((attr&7) | 0x8) << 2;
   if ((tileMap&31)==0) {
    tileMap -= 32;
    attrMap -= 32;
   }
  }
 }

 final private void renderScanlineWindow() {
  int bufMap = 0;
  int curX = ((windX)<(0)?(0):(windX));
  int cnt = 160-curX;
  if (cnt == 0)
   return;
  bgY = LY - WY;
  bgTileY = bgY >> 3;
  bgOffsY = bgY & 7;

  bgOffsX = curX - windX;

  calcWindTileBuf();

  int PatLine[] = patpix[tilebufBG[bufMap++]][bgOffsY];
  int TilePal = tilebufBG[bufMap++];

  for (int t = bgOffsX; (t < 8) && (cnt > 0); ++t, --cnt)
   { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };

  while (cnt>=8) {
   PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
   TilePal = tilebufBG[bufMap++];
   for (int t = 0; t < 8; ++t)
    { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };
   cnt -= 8;
  }
  PatLine = patpix[tilebufBG[bufMap++]][bgOffsY];
  TilePal = tilebufBG[bufMap++];
  for (int t = 0; cnt > 0; --cnt, ++t)
   { blitLine[curX++] = objColors[TilePal | PatLine[t]]; };
 }

 final private void renderScanlineSprites() {
  boolean spr8x16 = ((LCDC&(1<<2))!=0);

  for (int spr = 0; spr < 40; ++spr) {
   int sprY = OAM[(spr*4) + 0];
   int sprX = OAM[(spr*4) + 1];
   int sprNum = OAM[(spr*4) + 2];
   int sprAttr = OAM[(spr*4) + 3];

   int ofsY = LY - sprY + 16;


   if ((ofsY >= 0) && (ofsY < (spr8x16 ? 16 : 8))
   && (sprX > 0) && (sprX < 168)) {
    if ((sprAttr&(1<<6))!=0) ofsY = (spr8x16 ? 15 : 7) - ofsY;
    if (spr8x16) {
     sprNum &= ~1;
     sprNum |= (ofsY >= 8) ? 1 : 0;
     ofsY &= 7;
    }

    if ((sprAttr&(1<<3))!=0) sprNum |= (1<<9);
    if ((sprAttr&(1<<5))!=0) sprNum |= (1<<10);

    boolean prio = ((sprAttr&(1<<7))==0);

    int palnr;
    if (isCGB)
     palnr = sprAttr & 7;
    else
     palnr = (sprAttr>>4)&1;

    int[] PatLine = patpix[sprNum][ofsY];

    for (int ofsX = 0; ofsX < 8; ++ofsX) {
     int rx = sprX - 8 + ofsX;

     int col = PatLine[ofsX];
     if((col != 0) && (rx >= 0) && (rx < 160)
                                                  ) {
      { blitLine[rx] = objColors[(palnr << 2) | col]; };
     }
    }
   }
  }
 }
}
