import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.net.*;
import java.awt.image.BufferedImage;

public class Debugger implements ActionListener, ItemListener, KeyListener { //GUI
	public static boolean RIGHT_TO_LEFT = false;
	public JTable regs;
	public JTable mem;
	public JTable instrs;
	public JTextField cmds;
	swinggui gui;
	public Debugger(swinggui gui) {
		this.gui=gui;
		createAndShowGUI();
	}

	public void addComponentsToPane( Container contentPane ) {
		// Use BorderLayout. Default empty constructor with no horizontal and vertical
		// gaps
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		if ( RIGHT_TO_LEFT ) {
			contentPane.setComponentOrientation(
				java.awt.ComponentOrientation.RIGHT_TO_LEFT );
		}
		regs = new JTable(2,8);
		contentPane.add( new JLabel("- Registers -"), BorderLayout.LINE_END );
		contentPane.add( regs, BorderLayout.NORTH );
		contentPane.add( new JLabel("- Memory -"), BorderLayout.LINE_END );
		mem = new JTable(8,16+2);
		contentPane.add( mem, BorderLayout.LINE_END );
		contentPane.add( new JLabel("- Instructions -"), BorderLayout.LINE_END );
		instrs = new JTable(16,5);
		contentPane.add( instrs, BorderLayout.LINE_END );
		contentPane.add( new JLabel("- Commands -"), BorderLayout.LINE_END );
		cmds = new JTextField();
		contentPane.add( cmds, BorderLayout.LINE_END );
	}

	private void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated( true );
		JFrame frame = new JFrame( "JGameBoy Emulator DEBUGGER" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		//Set up the content pane and add swing components to it
		addComponentsToPane( frame.getContentPane() );
		frame.pack();
		frame.setVisible( true );
	}

	public void actionPerformed( ActionEvent e ) {
		JMenuItem source = ( JMenuItem )( e.getSource() );
		System.out.println( "Menu Item source: " + source.getText() + " (an instance of " + getClassName( source ) + ")" );
	}

	public void itemStateChanged( ItemEvent e ) {
		JMenuItem source = ( JMenuItem )( e.getSource() );
		System.out.println("Menu Item source: " + source.getText() + " (an instance of " + getClassName( source ) + ")"	+ "\n"
		                   + "    State of check Box: " + (( e.getStateChange() == ItemEvent.SELECTED ) ? "selected":"unselected" ));
	}

	public void keyTyped(KeyEvent e) {
		System.out.println("keyTyped");
	}

	public void keyPressed(KeyEvent e) {
		System.out.println("DEBUGGER: keyPressed");
	}

	public void keyReleased(KeyEvent e) {
		System.out.println("DEBUGGER: keyReleased" + e.getKeyCode());
	}

	// Returns the class name, no package info
	protected static String getClassName( Object o ) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf( "." );
		return classString.substring( dotIndex+1 );	//Returns only Class name
	}
}