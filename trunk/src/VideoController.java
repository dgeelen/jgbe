import javax.swing.*;
import java.awt.*;

public class VideoController {
	private int VRAM[][];
	private int CurrentVRAMBank=0;
	private int OAM[];
	private int SCX=0;
	private int SCY=0;
	protected int LCDC=0;
	private CPU cpu; // dont think we need this...

	public VideoController(CPU cpu) {
		VRAM = new int[2][0x2000]; //8k per bank
		OAM = new int[0xa0]; //Sprite Attribute Table
		this.cpu = cpu;
	}

	public void renderBackGroundMap(Graphics g) {
		for(int i=0; i<160; ++i) {
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
		if((LCDC&(1<<7))!=0) { //LCD enabled
			int TileData = ((LCDC&(1<<4))==0) ? 0x8800 : 0x8000;
			// BG display
			int CurBG = LCDC & 1;
			int BGTileMap = ((LCDC&(1<<3))==0) ? 0x9800 : 0x9c00;

			int ry = (SCY+linenumber)&0xFF;
			int rty = ry >> 5; // tile x
			int rsy = ry & 31; // x offs
			for (int x = SCX; x < SCX + 160; ++x) {
				int rx = x & 0xff; // it wraps, too
				int rtx = rx >> 5; // tile x
				int rsx = rx & 31; // x offs
				int TileNum = read(BGTileMap + rtx + (rty*32)); // get number of current tile
				int offset = (TileNum*16) + (rsy*2); // start with offset that describes that tile, and our line
				int d1 = read(TileData + offset);     // lsb bit of col is in here
				int d2 = read(TileData + offset + 1); // msb bit of col is in here
				int col = ((d1>>(7-rsx))&1) + (((d2>>(7-rsx))&1)<<1);
				//now we should do some pallete stuff....
				g.setColor(new Color(col&1,col>>1,0));
				g.drawRect(x-SCX, linenumber, x-SCX, linenumber);
			}
			
			if((LCDC&(1<<5))!=0) { //window display
				int WindowTileMap = ((LCDC&(1<<6))==0) ? 0x9800 : 0x9c00;

			}
		}
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