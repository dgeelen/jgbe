import javax.swing.*;
import java.awt.*;

public class VideoController {
	private int VRAM[][];
	private int CurrentVRAMBank=0;
	private int OAM[];
	private int SCX=0;
	private int SCY=0;

	public VideoController() {
		VRAM = new int[2][0x2000]; //8k per bank
		OAM = new int[0xa0]; //Sprite Attribute Table
	}

	public void renderBackGroundMap(Graphics g, int i) {

	}

	public void renderImage(Graphics g) {	//g is a reference to the display
		int width = (g.getClipBounds()).width;
		int height = (g.getClipBounds()).height;
		System.out.println("Graphics g width="+width+ " height="+height);
		for(int i=0; i<height; i+=4) {
			g.drawLine(0,0,width,i);
		}
		g.drawRect(20,20,200,200);
		renderBackGroundMap(g,0);
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