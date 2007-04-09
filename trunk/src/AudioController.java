import javax.sound.sampled.*;
// Sound IO ports:
// FF10 - NR10 - Channel 1 Sweep register (R/W)
// FF11 - NR11 - Channel 1 Sound length/Wave pattern duty (R/W)
// FF12 - NR12 - Channel 1 Volume Envelope (R/W)
// FF13 - NR13 - Channel 1 Frequency lo (Write Only)
// FF14 - NR14 - Channel 1 Frequency hi (R/W)
// FF15 - ???
// FF16 - NR21 - Channel 2 Sound Length/Wave Pattern Duty (R/W)
// FF17 - NR22 - Channel 2 Volume Envelope (R/W)
// FF18 - NR23 - Channel 2 Frequency lo data (W)
// FF19 - NR24 - Channel 2 Frequency hi data (R/W)
// FF1A - NR30 - Channel 3 Sound on/off (R/W)
// FF1B - NR31 - Channel 3 Sound Length
// FF1C - NR32 - Channel 3 Select output level (R/W)
// FF1D - NR33 - Channel 3 Frequency's lower data (W)
// FF1E - NR34 - Channel 3 Frequency's higher data (R/W)
// FF1F - ???
// FF20 - NR41 - Channel 4 Sound Length (R/W)
// FF21 - NR42 - Channel 4 Volume Envelope (R/W)
// FF22 - NR43 - Channel 4 Polynomial Counter (R/W)
// FF23 - NR44 - Channel 4 Counter/consecutive; Inital (R/W)
// FF24 - NR50 - Channel control / ON-OFF / Volume (R/W)
// FF25 - NR51 - Selection of Sound output terminal (R/W)
// FF26 - NR52 - Sound on/off
// FF27 - ???
// FF28 - ???
// FF29 - ???
// FF2A - ???
// FF2B - ???
// FF2C - ???
// FF2D - ???
// FF2E - ???
// FF2F - ???
// FF30-FF3F - Wave Pattern RAM
public class AudioController {
	private boolean isEnabled=true;
	private AudioFormat myAudioFormat;
	private DataLine.Info myLineInfo;
	private SourceDataLine audioSource;
	private byte audioBuffer[];
	private int audioBufferIndex;
	private int IO[];
	private final int sampleRate=22050;
	private int cyclesLeftToRender;
	public class SoundRegister {
		// Shared by more than one
		public int WaveDuty;
		public int SoundLength;
		public boolean UseSoundLength;
		public int EnvelopeVolume;
		public int EnvelopeDirection;
		public int EnvelopeSweepCount;
		public boolean Consecutive;
		public int Frequency;
		// S01 specific
		// Sweep
		public int SweepTime;
		public int SweepDir;
		public int ShiftCount;
		// S02 specific
		// S03 specific
		public boolean Enabled;
		public int OutputLevel; // ShiftCount
		// S04 specific //meh, check this later
		public int ShiftFrequency;
		public boolean use15bitcounter;
		public int DividingRatio;
	}
	private SoundRegister SR1;
	private SoundRegister SR2;
	private SoundRegister SR3;
	private SoundRegister SR4;
	//private SoundRegister SR5; //On cartridge, not used atm

	public AudioController() {
		try {
			myAudioFormat = new AudioFormat((float)sampleRate,8,1,true,true);
			myLineInfo = new DataLine.Info(SourceDataLine.class,myAudioFormat);
			audioSource = (SourceDataLine)AudioSystem.getLine(myLineInfo);
			audioSource.open(myAudioFormat);
			audioSource.start();
			audioBuffer = new byte[sampleRate]; //allocate enough buffer for 1 second
			int audioBufferIndex=0;
			IO = new int[0x30];
/*			source.drain();
			source.stop();
			source.close(); */
		}
		catch (Exception e) {
				System.out.println("Error while opening sound output, sound will be unavailable ("+e+")");
		}
		cyclesLeftToRender=0;
  	SR1=new SoundRegister();
		SR2=new SoundRegister();
		SR3=new SoundRegister();
		SR4=new SoundRegister();
	}
	static byte bla=0;
	public void render(int nrCycles) { //Gameboy runs at 4194304hz, so we render sampleRate/4194304mhz bytes every cycle
		cyclesLeftToRender+=nrCycles;
		float rate=(4194304/(float)sampleRate);
		if(cyclesLeftToRender/rate >= 1.0) {
			//DO SOMETHING CLEVER WITH ALL AUDIO REGISTERS

			bla=(byte)(((int)bla)^cyclesLeftToRender);
			audioBuffer[audioBufferIndex++]=(byte)(cyclesLeftToRender);
			cyclesLeftToRender-=rate;
		}
		if(audioBufferIndex>((audioBuffer.length)>>5)) { //every 1/32 sec
			audioSource.write(audioBuffer, 0, audioBufferIndex);
			audioBufferIndex=0;
			}
	}

	public int read(int index) {
		int i=(index&0xff)-0x10;
		if((i<0)||(i>0x3f)) {
			System.out.println("SoundController: Error: reading from non sound-address:"+index);
			return -1;
			}
		else if((i!=0x16) && ((IO[0x16]&0x80)==0)) { //sound disabled, can only read/write 0xff26
			System.out.println("Sound disabled: Reads are undefined!");
			return 0;
		}
		else if((i==0x05)||(i==0x0f)||((i>0x16)&&(i<0x20))) {
			System.out.println("Warning: read from unknown IO address, acting as normal RAM...");
		}
		return IO[i];
	}

	public void write(int index, int value) {
		int i=(index&0xff)-0x10;
		if((i<0)||(i>0x3f)) {
			System.out.println("SoundController: Error: writing to non sound-address:"+index);
			return;
			}
		else if((i!=0x16) && ((IO[0x16]&0x80)==0)) { //sound disabled, can only read/write 0xff26
			System.out.println("Sound disabled: Writes are undefined!");
		}
		else if((i==0x05)||(i==0x0f)||((i>0x16)&&(i<0x20))) {
			System.out.println("Warning: writing to unknown IO address, acting as normal RAM...");
		}
		switch(i) {
			case 0x00: // FF10 - NR10 - Channel 1 Sweep register (R/W)

			case 0x01: // FF11 - NR11 - Channel 1 Sound length/Wave pattern duty (R/W)
			case 0x02: // FF12 - NR12 - Channel 1 Volume Envelope (R/W)
			case 0x03: // FF13 - NR13 - Channel 1 Frequency lo (Write Only)
			case 0x04: // FF14 - NR14 - Channel 1 Frequency hi (R/W)
		}
		IO[i]=value;
	}
}