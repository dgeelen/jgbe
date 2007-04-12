import java.io.*;
import java.util.zip.*;

public class Bios {

	public Bios(String fname, int[] location) {
		try {
			loadFromFile(fname, location);
		}
		catch (IOException ioe) {
			System.out.println("Bios could not be loaded (message: " + ioe.getMessage() + ").");
			System.out.println("Emulator will go on like nothing happened.");
			location[0]=0xc3;
			location[1]=0x00;
			location[2]=0x01;
		}
	}

	private DataInputStream getDataStream(String fname) throws IOException {
    	int dotPos = fname.lastIndexOf(".");
    	String fext = fname.substring(dotPos);

		if ( !fext.equals(".zip") ) {
			// plain files
			FileInputStream fistream = new FileInputStream(fname);
			BufferedInputStream bistream = new BufferedInputStream(fistream);
			DataInputStream distream = new DataInputStream(bistream);
			return distream;
		}
		else {
			// Open the ZIP file
			FileInputStream fistream = new FileInputStream(fname);
			ZipInputStream zistream = new ZipInputStream(fistream);

			// Get the first entry
			ZipEntry entry = zistream.getNextEntry();

			BufferedInputStream bistream = new BufferedInputStream(zistream);
			DataInputStream distream = new DataInputStream(bistream);

			return distream;
		}
	}

	public void loadFromFile(String fname, int[] location) throws IOException {
		// filesize in bytes
		long fsize = (new File(fname)).length();
		DataInputStream distream = getDataStream(fname);

		for (int i = 0; i < fsize; ++i) {
			location[i] = distream.readUnsignedByte();
		}
	}
}