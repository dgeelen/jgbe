import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class swinggui implements ActionListener, ItemListener {
		public static boolean RIGHT_TO_LEFT = false;
		private static DrawingArea grfx;
		private static JMenuBar menubar;
		private VideoController VC;
		private Cartridge cartridge;
		private CPU cpu;
		public class DrawingArea extends JPanel{
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
    	}
		}

		public swinggui() {
			cartridge = new Cartridge("Pokemon Blue.gb");
			if(cartridge.getError()!=null) {
				System.out.println("ERROR: "+cartridge.getError());
			}
			else {
				System.out.println("Succesfully loaded ROM :)");
				cpu = new CPU(cartridge);
				TestSuite t = new TestSuite(cpu);
			}
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

			grfx=new DrawingArea(  ); //doublebuffering
			grfx.setPreferredSize( new Dimension( 160*4, 144*4 ) ); //quadruple each pixel
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

			cpu.reset();
			int x = 10;
			boolean fulldebug=false;
			while(x > 0){
				if (fulldebug) cpu.printCPUstatus();
				cpu.nextinstruction();
				if (cpu.exception()!=0) {
					Disassembler deasm = new Disassembler( cartridge, cpu);
					if (!fulldebug) cpu.printCPUstatus();
					String s = deasm.disassemble(cpu.PC);
					if (s.charAt( 6)=='$') ++(cpu.PC);
					if (s.charAt(10)=='$') ++(cpu.PC);
					if (s.charAt(14)=='$') ++(cpu.PC);
					--x;
					fulldebug = true;
				}
			}
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

		// Returns the class name, no package info
		protected static String getClassName( Object o ) {
			String classString = o.getClass().getName();
			int dotIndex = classString.lastIndexOf( "." );
			return classString.substring( dotIndex+1 );	//Returns only Class name
		}

		public static void main( String[] args ) {
			final swinggui gui=new swinggui();
			javax.swing.SwingUtilities.invokeLater( new Runnable() {
				                                        public void run() {
					                                        gui.createAndShowGUI();
				                                        }
			                                        }
			                                      );
		}
	}
