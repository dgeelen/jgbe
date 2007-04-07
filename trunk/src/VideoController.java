import javax.swing.*;
import java.awt.*;

public class VideoController {
	private int VRAM[][];
	private int CurrentVRAMBank=0;
	protected int OAM[];
	protected int LY=0;
	protected int SCX=0;
	protected int SCY=0;
	protected int WX=0;
	protected int WY=0;
	protected int LCDC=0;
	protected int STAT=0; // FF41 - STAT - LCDC Status (R/W)
	protected int BGPI=0;    //BCPS/BGPI - CGB Mode Only - Background Palette Index
	private int BGPD[];  //CPD/BGPD - CGB Mode Only - Background Palette Data
	private Color BGPC[][];
	private CPU cpu; // dont think we need this... //yes we do, we need interrupts
	private Color Gray[];

	public VideoController(CPU cpu) {
		VRAM = new int[2][0x2000]; //8k per bank
		OAM = new int[0xa0]; //Sprite Attribute Table
		BGPD = new int[8*4*2];
		BGPC = new Color[8][4];
		this.cpu = cpu;
		Gray = new Color[4];
		Gray[0]=new Color(0,0,0);
		Gray[1]=new Color(64,64,64);
		Gray[2]=new Color(128,128,128);
		Gray[3]=new Color(192,192,192);
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

		BGPC[palnum][colnum] = new Color(r<<3, g<<3, b<<3); // TODO gb->vga rgb conv

		if ((BGPI&(1<<7))!=0)
			++BGPI;
		System.out.println("setting GBC pal data");
	}

	public int getBGColData() {
		return BGPD[BGPI&0x3f];
	}

	public void renderBackGroundMap(Graphics g) {
		for(int i=0; i<144; ++i) {
			renderScanLine(g, i);
		}
	}

	public void renderScanLine(Graphics g, int linenumber) {
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
		int prevrambank = CurrentVRAMBank;
		if((LCDC&(1<<7))!=0) { //LCD enabled
			//System.out.println("rendering scanline");
			int TileData = ((LCDC&(1<<4))==0) ? 0x8800 : 0x8000;

			int BGTileMap = ((LCDC&(1<<3))==0) ? 0x9800 : 0x9c00;

			/* When Bit 0 is cleared, the background and window lose their priority - the sprites will be always
			 * displayed on top of background and window, independently of the priority flags in OAM and BG Map
			 * attributes.
			 */
			int BGPrio = (LCDC&(1<<0)); // ok dunno what exactly this does atm

			int ry = (SCY+linenumber)&0xFF;
			int rty = ry >> 3; // tile x
			int rsy = ry & 7; // x offs
			for (int x = 0; x < 160; ++x) {
				int rx = (SCX+x)&0xff; // it wraps, too
				int rtx = rx >> 3; // tile x
				int rsx = rx & 7; // x offs

				selectVRAMBank(0);	// should read directly form VRAM[][]? need to change all offsets
				int TileNum = read(BGTileMap + rtx + (rty*32)); // get number of current tile
				if (TileData == 0x8800)
					TileNum ^= 0x80; // this should do: -128 -> 0 ; 0 -> 128 ; -1 -> 127 ; 1 -> 129 ; 127 -> 255

				selectVRAMBank(1);
				int TileAttr = read(BGTileMap + rtx + (rty*32)); // get attributes of current tile
				selectVRAMBank((TileAttr>>3)&1);          // vram bank nr
				if ((TileAttr&(1<<5))!=0) rsx = 7 - rsx;  // horiz flip
				if ((TileAttr&(1<<6))!=0) rsy = 7 - rsy;  // vert  flip

				int offset = (TileNum*16) + (rsy*2); // start with offset that describes that tile, and our line
				int d1 = read(TileData + offset);     // lsb bit of col is in here
				int d2 = read(TileData + offset + 1); // msb bit of col is in here
				int col = ((d1>>(7-rsx))&1) + (((d2>>(7-rsx))&1)<<1);
				//now we should do some pallete stuff....
				g.setColor(Gray[col]);// System.out.println((col&1)|(col&2));
				//g.setColor(new Color((col&1)*255,(col>>1)*255,255));
				g.drawRect(x, linenumber, x, linenumber);
				//System.out.println("drawing rect Color(" + (col&1)+","+(col>>1)+",1) at (" +(x-SCX) + "," + linenumber +")");
			}

			if(((LCDC&(1<<5))!=0)
			&& (WX >= 0) && (WX < 167) // yes this is 160+7
			&& (WY >= 0) && (WY < 144)){ //window display enabled
				int WindowTileMap = ((LCDC&(1<<6))==0) ? 0x9800 : 0x9c00;
				if (linenumber >= WY) { // does window have height, width? doest seem so...
					ry  = linenumber - WY;
					rty = ry >> 3; // tile x
					rsy = ry & 7; // x offs
					for (int x = 0; x < 160; ++x) {
						int rx = WX - 7 - x; // no wrapping here?
						if ((rx >= 0) && (rx < 255)) { // bound correct?
							int rtx = rx >> 3; // tile x
							int rsx = rx & 7; // x offs

							selectVRAMBank(0);	// should read directly form VRAM[][]? need to change all offsets
							int TileNum = read(WindowTileMap + rtx + (rty*32)); // get number of current tile
							if (TileData == 0x8800)
								TileNum ^= 0x80; // this should do: -128 -> 0 ; 0 -> 128 ; -1 -> 127 ; 1 -> 129 ; 127 -> 255

							// window has tile attrs?
							/*
							selectVRAMBank(1);
							int TileAttr = read(WindowTileMap + rtx + (rty*32)); // get attributes of current tile
							selectVRAMBank((TileAttr>>3)&1);          // vram bank nr
							if ((TileAttr&(1<<5))!=0) rsx = 7 - rsx;  // horiz flip
							if ((TileAttr&(1<<6))!=0) rsy = 7 - rsy;  // vert  flip
							*/

							int offset = (TileNum*16) + (rsy*2); // start with offset that describes that tile, and our line
							int d1 = read(TileData + offset);     // lsb bit of col is in here
							int d2 = read(TileData + offset + 1); // msb bit of col is in here
							int col = ((d1>>(7-rsx))&1) + (((d2>>(7-rsx))&1)<<1);

							//now we should do some pallete stuff....
							g.setColor(Gray[col]);// System.out.println((col&1)|(col&2));

							g.drawRect(x, linenumber, x, linenumber);
							System.out.println("drawing window rect Color(" + (col&1)+","+(col>>1)+",1) at (" +(x-SCX) + "," + linenumber +")");
						}
					}
				}
			}
		}
		selectVRAMBank(prevrambank);
		//System.out.println("VC: Requesting STAT");
		STAT&=0xfc; //Set HBlank, mode=0
		cpu.triggerInterrupt(1); //request STAT
	}

	public boolean renderNextScanline(Graphics g) {
		if (LY < 144) renderScanLine(g, LY);
		++LY;
		if (LY == 154) LY = 0;
		return (LY == 144);
	}

	public void renderImage(Graphics g) {	//g is a reference to the display
/*		int width = (g.getClipBounds()).width;
		int height = (g.getClipBounds()).height;
		System.out.println("Graphics g width="+width+ " height="+height);
		for(int i=0; i<height; i+=4) {
			g.drawLine(0,0,width,i);
		}
		g.drawRect(20,20,20,20);*/
		renderBackGroundMap(g);
	}

	public int read(int index) {
		if(index<0x8000) {
			System.out.println("Error: VideoController.read(): Reading from non VideoController-Address "+index);
		}
		else if(index < 0xa000) {
			return VRAM[CurrentVRAMBank][index-0x8000];
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
		return VRAM[CurrentVRAMBank][index];
	}

	public void write(int index, int value) {
		if(index<0x8000) {
			System.out.println("Error: VideoController.write(): Writing to non VideoController-Address "+index+" value="+value);
		}
		else if(index < 0xa000) {
			VRAM[CurrentVRAMBank][index-0x8000]=value;
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
		CurrentVRAMBank=i;
	}
}