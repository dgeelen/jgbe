import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;

public class swinggui implements ActionListener, ItemListener, KeyListener {
		public static boolean RIGHT_TO_LEFT = false;
		private static DrawingArea grfx;
		private static JMenuBar menubar;
		protected VideoController VC;
		protected Cartridge cartridge;
		protected CPU cpu;

		private Image[]    img;
		private int which_img_to_draw_for_double_buffering=0;
		private Graphics graph;

		public class DrawingArea extends JPanel{
			VideoController VC;
			public DrawingArea(VideoController vc) {
			super();
			VC=vc;
			}
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img[which_img_to_draw_for_double_buffering],0,0, this);
    	}
		}

		public swinggui() {
			cartridge = new Cartridge("Pokemon Blue.gb");
			//cartridge = new Cartridge("../roms/Metal Slug.gb");
			//cartridge = new Cartridge("tetris.gb");
			if(cartridge.getError()!=null) {
				System.out.println("ERROR: "+cartridge.getError());
			}
			else {
				System.out.println("Succesfully loaded ROM :)");
				cpu = new CPU(cartridge);
				VC = cpu.VC;
				TestSuite t = new TestSuite(cpu);
			}
			img=new Image[2];
			img[0] = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
			img[1] = new BufferedImage(160, 144, BufferedImage.TYPE_3BYTE_BGR);
		}

		private JMenuBar createJMenuBar() {
			JMenuBar mainMenuBar;
			JMenu menuFile;
			JMenuItem menuitemExit;
			mainMenuBar = new JMenuBar();

			menuFile = new JMenu( "File" );
			menuFile.setMnemonic( KeyEvent.VK_F );
			mainMenuBar.add( menuFile );

			//Creating the MenuItems
			menuitemExit = new JMenuItem( "Exit", KeyEvent.VK_X );

//        Accelerators, offer keyboard shortcuts to bypass navigating the menu hierarchy.
			menuitemExit.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_1, ActionEvent.ALT_MASK ) );
			menuitemExit.addActionListener( this );
			menuFile.add( menuitemExit );
			return mainMenuBar;
		}

		public void addComponentsToPane( Container contentPane ) {
//      Use BorderLayout. Default empty constructor with no horizontal and vertical
//      gaps
			contentPane.setLayout( new BorderLayout( 5,5 ) );
			if ( !( contentPane.getLayout() instanceof BorderLayout ) ) {
				contentPane.add( new JLabel( "Container doesn't use BorderLayout!" ) );
				return;
			}

			if ( RIGHT_TO_LEFT ) {
				contentPane.setComponentOrientation(
				  java.awt.ComponentOrientation.RIGHT_TO_LEFT );
			}

			grfx=new DrawingArea( cpu.VC ); //doublebuffering
			grfx.setFocusable(true);
			grfx.addKeyListener(this);
			grfx.setPreferredSize( new Dimension( 160, 144 ) ); //quadruple each pixel
			contentPane.add( grfx, BorderLayout.CENTER );
		}

		private void createAndShowGUI() {
			JFrame.setDefaultLookAndFeelDecorated( true );

			JFrame frame = new JFrame( "JGameBoy Emulator V0.01" );
			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			frame.setBounds( 60,60,100,100 );

			//Set up the content pane and add swing components to it
			frame.setJMenuBar( createJMenuBar() );
			addComponentsToPane( frame.getContentPane() );

			frame.pack();
			frame.setVisible( true );

		}

		public void actionPerformed( ActionEvent e ) {
			JMenuItem source = ( JMenuItem )( e.getSource() );
			String s = "Menu Item source: " + source.getText()
			           + " (an instance of " + getClassName( source ) + ")";
			System.out.println( s );
		}

		public void itemStateChanged( ItemEvent e ) {
			JMenuItem source = ( JMenuItem )( e.getSource() );
			String s = "Menu Item source: " + source.getText()
			           + " (an instance of " + getClassName( source ) + ")"
			           + "\n"
			           + "    State of check Box: "
			           + (( e.getStateChange() == ItemEvent.SELECTED ) ?
			              "selected":"unselected" );
			System.out.println( s );
		}

		public void keyTyped(KeyEvent e) {
			System.out.println("keyTyped");
		}

		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()){
				case KeyEvent.VK_Z:
					cpu.ButtonKeyStatus|=1; // A
					break;
				case KeyEvent.VK_X:
					cpu.ButtonKeyStatus|=2; // B
					break;
				case KeyEvent.VK_MINUS:
					cpu.ButtonKeyStatus|=4; // Select
					break;
				case KeyEvent.VK_EQUALS:
					cpu.ButtonKeyStatus|=8; // Select
					break;
				case KeyEvent.VK_RIGHT:
					cpu.DirectionKeyStatus|=1; // Right
					break;
				case KeyEvent.VK_LEFT:
					cpu.DirectionKeyStatus|=2; // Left
					break;
				case KeyEvent.VK_UP:
					cpu.DirectionKeyStatus|=4; // Up
					break;
				case KeyEvent.VK_DOWN:
					cpu.DirectionKeyStatus|=8; // Down
					break;
				default:
					System.out.println("keyPressed");
					break;
				}
		}
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()){
				case KeyEvent.VK_Z:
					cpu.ButtonKeyStatus&=~1; // A
					break;
				case KeyEvent.VK_X:
					cpu.ButtonKeyStatus&=~2; // B
					break;
				case KeyEvent.VK_MINUS:
					cpu.ButtonKeyStatus&=~4; // Select
					break;
				case KeyEvent.VK_EQUALS:
					cpu.ButtonKeyStatus&=~8; // Select
					break;
				case KeyEvent.VK_RIGHT:
					cpu.DirectionKeyStatus&=~1; // Right
					break;
				case KeyEvent.VK_LEFT:
					cpu.DirectionKeyStatus&=~2; // Left
					break;
				case KeyEvent.VK_UP:
					cpu.DirectionKeyStatus&=~4; // Up
					break;
				case KeyEvent.VK_DOWN:
					cpu.DirectionKeyStatus&=~8; // Down
					break;
				default:
					System.out.println("keyReleased" + e.getKeyCode());
					break;
				}
		}


		// Returns the class name, no package info
		protected static String getClassName( Object o ) {
			String classString = o.getClass().getName();
			int dotIndex = classString.lastIndexOf( "." );
			return classString.substring( dotIndex+1 );	//Returns only Class name
		}

		public static void main( String[] args ) {
			final swinggui gui=new swinggui();
			gui.createAndShowGUI();
			gui.cpu.reset();

			/*DEBUGGER*/
			final Debugger dbgr=new Debugger(gui); //The GUI to which this debugger belongs
//			Debugger.createAndShowGUI();
			/*///END DEBUGGER
			int x = 1;
			boolean fulldebug=false;
			while(x > 0) {
				if (fulldebug) gui.cpu.printCPUstatus();
				gui.cpu.nextinstruction();
				if ((gui.cpu.TotalInstrCount % 10000) == 0) {
					if (gui.VC.renderNextScanline(gui.img[gui.which_img_to_draw_for_double_buffering^1].getGraphics())) {
						gui.which_img_to_draw_for_double_buffering^=1;
						gui.grfx.updateUI();
					}
				}
				if (gui.cpu.exception()!=0) {
					Disassembler deasm = new Disassembler( gui.cartridge, gui.cpu);
					if (!fulldebug) gui.cpu.printCPUstatus();
					String s = deasm.disassemble(gui.cpu.PC);
					if (s.charAt( 6)=='$') ++(gui.cpu.PC);
					if (s.charAt(10)=='$') ++(gui.cpu.PC);
					if (s.charAt(14)=='$') ++(gui.cpu.PC);
					--x;
					fulldebug = true;
				}
			} /* */
		}
	}
