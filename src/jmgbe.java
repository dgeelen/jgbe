//TODO: Fix package name
//package gpl.emulation.jgbe;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class jmgbe extends MIDlet implements CommandListener {
		private Form mMainForm;

		public jmgbe() {
			mMainForm = new Form( "HelloMIDlet" );
			mMainForm.append( new StringItem( null, "Hello, MIDP!" ) );
			mMainForm.addCommand( new Command( "Exit", Command.EXIT, 0 ) );
			mMainForm.setCommandListener( this );
		}

		public void startApp() {
			Display.getDisplay( this ).setCurrent( mMainForm );
		}

		public void pauseApp() {}

		public void destroyApp( boolean unconditional ) {}

		public void commandAction( Command c, Displayable s ) {
			notifyDestroyed();
		}
}
