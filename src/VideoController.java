import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoController {
	private JPanel listener = null;
	private Image drawImg[];
	private int curDrawImg = 0;
	private int blitImg[][]=new int[160][144];

	private int CurrentVRAMBank=0;
	//private int VRAM[][]=new int[2][0x2000]; //8k per bank;
	private int VRAM[]=new int[0x4000]; //8k per bank;
	private int OAM[]=new int[0xa0]; //Sprite Attribute Table;

	protected int LY=0;
	protected int LYC=0;
	protected int SCX=0;
	protected int SCY=0;
	protected int WX=0;
	protected int WY=0;
	protected int LCDC=0;
	protected int STAT=0; // FF41 - STAT - LCDC Status (R/W)
	
	protected int BGPI=0;              //BCPS/BGPI - CGB Mode Only - Background Palette Index
	private int BGPD[]=new int[8*4*2]; //BCPD/BGPD - CGB Mode Only - Background Palette Data

	protected int OBPI=0;              //OCPS/OBPI - CGB Mode Only - Sprite Palette Index
	private int OBPD[]=new int[8*4*2]; //OCPD/OBPD - CGB Mode Only - Sprite Palette Data

	/* caching vars */
	private Color Colors[] = new Color[8*4*2];
	private int patpix[][][] = new int[4096][8][8]; // see updatepatpix()
	private boolean patdirty[] = new boolean[1024]; // see updatepatpix()
	private boolean anydirty = true;                // see updatepatpix()
	private boolean alldirty = true;                // see updatepatpix()

	private CPU cpu; // dont think we need this... //yes we do, we need interrupts
	private Color Gray[];

	public VideoController(CPU cpu) {
		this.cpu = cpu;
		Gray = new Color[4];
		Gray[0]=new Color(0,0,0);
		Gray[1]=new Color(64,64,64);
		Gray[2]=new Color(128,128,128);
		Gray[3]=new Color(192,192,192);
		drawImg=new Image[2];
		drawImg[0]=new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		drawImg[1]=new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
	}

	public void addListener(JPanel panel)
	{
		listener = panel; // only 1 listener at a time currently :-p
	}

	public Image getImage() {
		return drawImg[curDrawImg]; // display image not being drawn to
	}

	private void drawPixel(int x, int y, int pal, int col) {
		blitImg[x][y] = (pal << 2) | col;
	}

	private void blitImage() {
		Graphics g = drawImg[curDrawImg^1].getGraphics();
		for (int x = 0; x < 160; ++x) {
			for (int y = 0; y < 144; ++y) {
				int col = blitImg[x][y];
				if ((col >= 0) && (col < (8*4*2)))
					g.setColor(Colors[col]);
				g.drawRect(x, y, 0, 0);
			}
		}
		curDrawImg ^= 1;
	}

	public void setBGColData(int value) {
		BGPD[BGPI&0x3f] = value;

		// calculate color now
		int base = (BGPI & 0x3e);
		int data = BGPD[base] | (BGPD[base+1]<<8);
		int palnum = base >> 3;
		int colnum = (base >> 1) & 3;
		int r = (data >>  0) & 0x1F;
		int g = (data >>  5) & 0x1F;
		int b = (data >> 10) & 0x1F;
		
		r <<= 3; r |= (r >> 5);
		g <<= 3; g |= (g >> 5);
		b <<= 3; b |= (b >> 5);
		// TODO gb->vga rgb conv
		// i think that was it... gnuboy doesnt seem
		// to do anything more and there it works
		// fading issue is somethere else, maybe int timing issue?

		Colors[(palnum << 2) | colnum | 0x20] = new Color(r, g, b);

		if ((BGPI&(1<<7))!=0)
			++BGPI;
	}

	public int getBGColData() {
		return BGPD[BGPI&0x3f];
	}

	public void setOBColData(int value) {
		OBPD[OBPI&0x3f] = value;

		// calculate color now
		int base = (OBPI & 0x3e);
		int data = OBPD[base] | (OBPD[base+1]<<8);
		int palnum = base >> 3;
		int colnum = (base >> 1) & 3;
		int r = (data >>  0) & 0x1F;
		int g = (data >>  5) & 0x1F;
		int b = (data >> 10) & 0x1F;

		r <<= 3; r |= (r >> 5);
		g <<= 3; g |= (g >> 5);
		b <<= 3; b |= (b >> 5);

		Colors[(palnum << 2) | colnum] = new Color(r, g, b);

		if ((OBPI&(1<<7))!=0)
			++OBPI;
	}

	public int getOBColData() {
		return OBPD[OBPI&0x3f];
	}

	/** updates contents of patpix[][][] when it is outdated
	 *
	 *  when anydirty is false nothing will be done
	 *  when alldirty is true all patterns will be updated
	 *  when patdirty[i] is true pattern i will be updates
	 *
	 *  patpix[i] is an 8x8 (y,x) matrix with colors of the tile
	 *  i needs to 0..384-1 or 512..896
	 *
	 *  patpix[   0..1024-1] hold normal patterns
	 *  patpix[1024..2048-1] hold patterns flipped x-wise
	 *  patpix[2048..3072-1] hold patterns flipped y-wise
	 *  patpix[3072..3072-1] hold patterns flipped x-wise and y-wise
	 */
	private void updatepatpix() {
		if (!anydirty/* && !alldirty*/)
			return;

		for (int i = 0; i < 1024; ++i)
		{
			if (i == 384) i = 512;
			if (i == 896) break;
			if (!patdirty[i]/* && !alldirty*/) continue;
			patdirty[i] = false;

			for (int y = 0; y < 8; ++y) {
				int lineofs = (i*16) + (y*2);
				//if ((i&0xC0)!=0) {
				//	lineofs = (i*16) + (y*2) + 0x800;
				//}
				for (int x = 0; x < 8; ++x) { // not really x, but 7-x
					// this info is always in bank 0, so read directly from VRAM[0]
					int col = (VRAM[lineofs]>>x)&1;
					col |= ((VRAM[lineofs+1]>>x)&1)<<1;
					patpix[i]     [y]  [7-x] = col;
					patpix[i+1024][y]  [x]   = col;
					patpix[i+2048][7-y][7-x] = col;
					patpix[i+3072][7-y][x]   = col;
				}
			}
		}
		anydirty = false;
		//alldirty = false;
	}

	public void renderScanLine(int linenumber) {
		/* FF40 - LCDC - LCD Control (R/W)
		* Bit 7 - LCD Display Enable             (0=Off, 1=On)
		* Bit 6 - Window Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
		* Bit 5 - Window Display Enable          (0=Off, 1=On)
		* Bit 4 - BG & Window Tile Data Select   (0=8800-97FF, 1=8000-8FFF)
		* Bit 3 - BG Tile Map Display Select     (0=9800-9BFF, 1=9C00-9FFF)
		* Bit 2 - OBJ (Sprite) Size              (0=8x8, 1=8x16)
		* Bit 1 - OBJ (Sprite) Display Enable    (0=Off, 1=On)
		* Bit 0 - BG Display (for CGB see below) (0=Off, 1=On)
		*/
		int prevrambank = getcurVRAMBank();
		if((LCDC&(1<<7))!=0) { //LCD enabled

			updatepatpix();

			//System.out.println("rendering scanline");
			int TileData = ((LCDC&(1<<4))==0) ? 0x8800 : 0x8000;

			int BGTileMap = ((LCDC&(1<<3))==0) ? 0x9800 : 0x9c00;

			/* When Bit 0 is cleared, the background and window lose their priority - the sprites will be always
			 * displayed on top of background and window, independently of the priority flags in OAM and BG Map
			 * attributes.
			 */
			int BGPrio = (LCDC&(1<<0)); // ok dunno what exactly this does atm

			int wndX = 160;
			if(((LCDC&(1<<5))!=0)			 //window display enabled
			&& (WX >= 0) && (WX < 167) // yes this is 160+7
			&& (WY >= 0) && (WY < 144)
			&& (linenumber >= WY))
				wndX = (WX - 7);         // [-8 < wndX < 160]

			int ry = (SCY+linenumber)&0xFF;
			int rty = ry >> 3; // tile x
			int rsy = ry & 7; // x offs
			for (int x = 0; x < wndX; ++x) { // dont draw bg where window starts
				int rx = (SCX+x)&0xff; // it wraps, too
				int rtx = rx >> 3; // tile x
				int rsx = rx & 7; // x offs

				selectVRAMBank(0);	// should read directly form VRAM[][]? need to change all offsets
				int TileNum = read(BGTileMap + rtx + (rty*32)); // get number of current tile
				if (TileData == 0x8800) {
					TileNum ^= 0x80; // this should do: -128 -> 0 ; 0 -> 128 ; -1 -> 127 ; 1 -> 129 ; 127 -> 255
					TileNum += 0x80;
				}

				selectVRAMBank(1);
				int TileAttr = read(BGTileMap + rtx + (rty*32)); // get attributes of current tile

				if ((TileAttr&(1<<3))!=0) TileNum |= (1<<9);  // bank select
				if ((TileAttr&(1<<5))!=0) TileNum |= (1<<10); // horiz flip
				if ((TileAttr&(1<<6))!=0) TileNum |= (1<<11); // vert flip

				int palnr = TileAttr & 7;

				int col = patpix[TileNum][rsy][rsx];

				drawPixel(x, linenumber, palnr | 0x08, col);
			}

			if (wndX < 160) { // window doesnt have height, width
				int WindowTileMap = ((LCDC&(1<<6))==0) ? 0x9800 : 0x9c00;
				ry  = linenumber - WY;
				rty = ry >> 3; // tile y
				rsy = ry & 7;  // y offs
				for (int x = Math.max(wndX, 0); x < 160; ++x) { // [wndX <= x < 160]
					int rx = x - wndX; // [-8 < wndX < 160] && [wndX <= x < 160] => [0 <= rx < 167]
					int rtx = rx >> 3; // tile x
					int rsx = rx & 7;  // x offs

					selectVRAMBank(0);	// should read directly form VRAM[][]? need to change all offsets
					int TileNum = read(WindowTileMap + rtx + (rty*32)); // get number of current tile
					if (TileData == 0x8800) {
						TileNum ^= 0x80; // this should do: -128 -> 0 ; 0 -> 128 ; -1 -> 127 ; 1 -> 129 ; 127 -> 255
						TileNum += 0x80;
					}

					selectVRAMBank(1);
					int TileAttr = read(WindowTileMap + rtx + (rty*32)); // get attributes of current tile

					if ((TileAttr&(1<<3))!=0) TileNum |= (1<<9);  // bank select
					if ((TileAttr&(1<<5))!=0) TileNum |= (1<<10); // horiz flip
					if ((TileAttr&(1<<6))!=0) TileNum |= (1<<11); // vert flip

					int palnr = TileAttr & 7;

					int col = patpix[TileNum][rsy][rsx];

					drawPixel(x, linenumber, palnr | 0x08, col);
				}
			}

			if((LCDC&(1<<1))!=0) { // sprites enabled
				boolean spr8x16 = ((LCDC&(1<<2))!=0);

				int sprOAM = 0xfe00;
				int sprPat = 0x8000;

				for (int spr = 0; spr < 40; ++spr) {
					int sprY    = read(sprOAM + (spr*4) + 0);
					int sprX    = read(sprOAM + (spr*4) + 1);
					int sprNum  = read(sprOAM + (spr*4) + 2);
					int sprAttr = read(sprOAM + (spr*4) + 3);
					
					int ofsY = linenumber - sprY + 16;

					//check if sprite is visible on this scanline
					if ((ofsY >= 0) && (ofsY < (spr8x16 ? 16 : 8))
					&&  (sprX > 0) && (sprX < 168)) {
						if ((sprAttr&(1<<6))!=0) ofsY = (spr8x16 ? 15 : 7) - ofsY;  // vert  flip
						if (spr8x16) {
							sprNum &= ~1;
							sprNum |= (ofsY >= 8) ? 1 : 0;
							ofsY &= 7;
						}
						for (int x = 0; x < 8; ++x) {
							int ofsX = x;

							if ((sprAttr&(1<<3))!=0) sprNum |= (1<<9);  // bank select
							if ((sprAttr&(1<<5))!=0) sprNum |= (1<<10); // horiz flip
							//if ((sprAttr&(1<<6))!=0) sprNum |= (1<<11); // vert flip

							int palnr = sprAttr & 7;

							int col = patpix[sprNum][ofsY][ofsX];
					
							int rx = sprX - 8 + x;
							// 0 is transparent color
							if((col != 0) && (rx >= 0) && (rx < 160)) {
								drawPixel(rx, linenumber, palnr, col);
							}
						}
					}
				}
			}
		}
		selectVRAMBank(prevrambank);
	}

	public boolean renderNextScanline() {
		++LY;
		if (LY >= 154)
			LY = 0;

		STAT &= ~(1<<2);             // clear coincidence bit
		if (LY==LYC) {               // if equal
			STAT |= 1<<2;              // then set it
			if ((STAT&(1<<6))!=0)      // if LYC=LY is enabled in STAT reg
				cpu.triggerInterrupt(1); // request int STAT/LYC=LY
		}

		if (LY < 144) {              // HBLANK
			renderScanLine(LY);
			STAT &= ~(3);              // mode=0
			if ((STAT&(1<<3))!=0)      // if HBlank is enabled in STAT reg
				cpu.triggerInterrupt(1); // request int STAT/HBlank
		}

		if (LY == 144) {             // VBLANK
			blitImage();
			if (listener != null) listener.updateUI();
			STAT &= ~(3);
			STAT |= 1;                 // mode=1
			if ((STAT&(1<<4))!=0)      // if VBlank is enabled in STAT reg
				cpu.triggerInterrupt(1); // request int STAT/VBlank
			cpu.triggerInterrupt(0);   // always request int VBLANK
		}

		return (LY == 144);
	}

	public int read(int index) {
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

	public void write(int index, int value) {
		if(index<0x8000) {
			System.out.println("Error: VideoController.write(): Writing to non VideoController-Address "+index+" value="+value);
		}
		else if(index < 0xa000) {
			VRAM[index-0x8000+CurrentVRAMBank]=value;
			patdirty[(CurrentVRAMBank>>4)+((index-0x8000)>>4)] = true; // nicked from gnuboy...
			anydirty = true;
			//alldirty = true;
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

	public void selectVRAMBank(int i) {
		CurrentVRAMBank=i*0x2000;
		if ((i <0) || (i > 1))
			System.out.printf("current offset=%x\n",CurrentVRAMBank);
	}
	
	public int getcurVRAMBank() {
		return CurrentVRAMBank/0x2000;
	}
}