import javax.swing.*;
import java.awt.*;

public class VideoController {
	private int VRAM[][];
	private int CurrentVRAMBank=0;
	private int OAM[];
	private int SCX=0;
	private int SCY=0;
	private CPU cpu;

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
		int lcdc=cpu.read(0xff40);
		if((lcdc&(1<<7))!=0) { //LCD enabled
			if((lcdc&(1<<5))!=0) {
			int WindowTileMap = 0x9800;
			if((lcdc&(1<<6))!=0) WindowTileMap=0x9c00;

			}
		}
	}


	public void renderImage(Graphics g) {	//g is a reference to the display
		int width = (g.getClipBounds()).width;
		int height = (g.getClipBounds()).height;
		System.out.println("Graphics g width="+width+ " height="+height);
		for(int i=0; i<height; i+=4) {
			g.drawLine(0,0,width,i);
		}
		g.drawRect(20,20,200,200);
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