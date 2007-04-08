import javax.sound.sampled.*;

public class AudioController {

	public static void main(String[] args) {
		int seconds = 2;
		int sampleRate = 22050;
		double frequency = 1000.0;
		double RAD = 2.0 * Math.PI;
		try {
			AudioFormat af = new AudioFormat((float)sampleRate,8,1,true,true);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,af);
			SourceDataLine source = (SourceDataLine)AudioSystem.getLine(info);
			source.open(af);
			source.start();
			byte[] buf = new byte[sampleRate * seconds];
/*			for (int i=0; i<buf.length; i++) {
			buf[i] = (byte)(Math.sin(RAD*frequency/sampleRate*i)*127.0);
	//                System.out.println(buf[i]);
			} */
			int x=0x11;
			int y=0x11;
			int z=0x11;
			while(true) {
				x=y^z;
				z=(y+z)&0xff;
				y=(x<<2)^(x>>2);
				buf[0]=(byte)(x>127?0:127);
				source.write(buf,0,1);
				}
/*			source.drain();
			source.stop();
			source.close(); */
		}
		catch (Exception e) {
				System.out.println(e);
			}
		System.exit(0);
	}
}