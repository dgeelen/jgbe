#ifndef USE_JAVA_MOBILE
#define nrOfShortCuts 39
class keyConfigDialog implements ComponentListener, ActionListener, KeyListener {
	private JDialog dialog;
	private JFrame owner;
	VECTOR(JTextField) editboxes = new VECTOR(JTextField)();
	JTextField tbox;
	JLabel tlabel;
	int nInputBoxen=-16;
	#define INPUTBOX(c, x) ASSERT(nInputBoxen++ < nrOfShortCuts ); \
	                    tbox = new JTextField();\
	                    tlabel = new JLabel(x);\
	                    editboxes.add(tbox);\
	                    c.add(tlabel);\
	                    c.add(tbox);\
	                    tbox.addKeyListener(this);\
	                    tbox.addActionListener(this)
	int[] keyMap;
	boolean[] keysup = new boolean[256];
	boolean canChooseNextKey = true;

	public keyConfigDialog(JFrame o, int[] km) {
		owner = o;
		keyMap= km;
		JScrollPane scroll;
		dialog= new JDialog(owner, "Key Bindings", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					dialog.setVisible(false);
			}
		});
		JPanel ppp; //*** DO NOT TOUCH, WILL EXPLODE ***
		JTabbedPane tabPane = new JTabbedPane();
		JPanel player1Keys = new JPanel();
			GridLayout player1KeysGL = new GridLayout(8,2);
			player1Keys.setLayout(player1KeysGL);
			INPUTBOX(player1Keys, "Up");
			INPUTBOX(player1Keys, "Down");
			INPUTBOX(player1Keys, "Left");
			INPUTBOX(player1Keys, "Right");
			INPUTBOX(player1Keys, "A");
			INPUTBOX(player1Keys, "B");
			INPUTBOX(player1Keys, "Start");
			INPUTBOX(player1Keys, "Select");
			ppp=new JPanel();
			ppp.setLayout(new BorderLayout());
			ppp.add(player1Keys, BorderLayout.NORTH);
 			ppp.add(new JPanel(), BorderLayout.SOUTH);
 			tabPane.add("Player 1", ppp);

		JPanel player2Keys = new JPanel();
			GridLayout player2KeysGL = new GridLayout(8,2);
			player2Keys.setLayout(player2KeysGL);
			INPUTBOX(player2Keys, "Up");
			INPUTBOX(player2Keys, "Down");
			INPUTBOX(player2Keys, "Left");
			INPUTBOX(player2Keys, "Right");
			INPUTBOX(player2Keys, "A");
			INPUTBOX(player2Keys, "B");
			INPUTBOX(player2Keys, "Start");
			INPUTBOX(player2Keys, "Select");
			ppp=new JPanel();
			ppp.setLayout(new BorderLayout());
			ppp.add(player2Keys, BorderLayout.NORTH);
 			ppp.add(new JPanel(), BorderLayout.SOUTH);
 			tabPane.add("Player 2", ppp);

		JPanel shortCuts = new JPanel();
			GridLayout shortCutsGL = new GridLayout(nrOfShortCuts,2);
			shortCuts.setLayout(shortCutsGL);
			INPUTBOX(shortCuts, "Configure Keybinds");
			INPUTBOX(shortCuts, "Open ROM");
			INPUTBOX(shortCuts, "Pause");
			INPUTBOX(shortCuts, "Reset Gameboy");
			INPUTBOX(shortCuts, "Exit Emulator");
			INPUTBOX(shortCuts, "Fullscreen");
			INPUTBOX(shortCuts, "Mix Frame");
			INPUTBOX(shortCuts, "Scale 1x");
			INPUTBOX(shortCuts, "Scale 2x");
			INPUTBOX(shortCuts, "Scale 3x");
			INPUTBOX(shortCuts, "Scale 4x");
			INPUTBOX(shortCuts, "Scale with Aspect");
			INPUTBOX(shortCuts, "Interpolate None");
			INPUTBOX(shortCuts, "Interpolate Nearest");
			INPUTBOX(shortCuts, "Interpolate BiLinear");
			INPUTBOX(shortCuts, "Interpolate BiCubic");
			INPUTBOX(shortCuts, "Increase FrameSkip");
			INPUTBOX(shortCuts, "Decrease FrameSkip");
			INPUTBOX(shortCuts, "Original Colors");
			INPUTBOX(shortCuts, "GB Pocket/Light Colors");
			INPUTBOX(shortCuts, "Black and White Colors");
			INPUTBOX(shortCuts, "Greyscale Colors");
			INPUTBOX(shortCuts, "Custom Colors");
			INPUTBOX(shortCuts, "Save state");
			INPUTBOX(shortCuts, "Load state");
			INPUTBOX(shortCuts, "Save To Oldest State");
			INPUTBOX(shortCuts, "Toggle Sound on/off");
			INPUTBOX(shortCuts, "Toggle Soundchannel 1 on/off");
			INPUTBOX(shortCuts, "Toggle Soundchannel 2 on/off");
			INPUTBOX(shortCuts, "Toggle Soundchannel 3 on/off");
			INPUTBOX(shortCuts, "Toggle Soundchannel 4 on/off");
			INPUTBOX(shortCuts, "Open Cheat Code Editor");
			INPUTBOX(shortCuts, "Toggle Cheats on/off");
			INPUTBOX(shortCuts, "Emulation speed +25%");
			INPUTBOX(shortCuts, "Emulation speed -25%");
			INPUTBOX(shortCuts, "Emulation speed *125%");
			INPUTBOX(shortCuts, "Emulation speed *75%");
			INPUTBOX(shortCuts, "Emulation speed 100%");
			INPUTBOX(shortCuts, "Emulation speed Infinite%");
			scroll = new JScrollPane(shortCuts);
			scroll.setPreferredSize(new Dimension(10,10) );
			tabPane.add("Shortcuts", scroll);

		scroll = new JScrollPane(tabPane);
		scroll.setPreferredSize(new Dimension(480,320));
		dialog.add(scroll);

		//Fill all TextFields with text
		int j = 0;
		FOREACH(JTextField, tf, editboxes) {
			int keyCode = keyMap[j]&0xff;
			int keyModifiers = keyMap[j]>>8;
			tf.setText((keyModifiers>0 ? KeyEvent.getKeyModifiersText(keyModifiers)+"+" : "") + KeyEvent.getKeyText(keyCode));
			++j;
		}

		for(int i=0; i<256; ++i) {
			keysup[i]=true;
		}

		dialog.addComponentListener(this);
		dialog.addKeyListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		dialog.pack();
		Dimension d = owner.getSize();
		Point     p = new Point();
		p.setLocation((owner.getLocation().getX()+(d.getWidth()/2)) - (dialog.getWidth()/2), (owner.getLocation().getY()+d.getHeight()/2)-(dialog.getHeight()/2));
		dialog.setLocation(p);

		dialog.addWindowListener( new WindowAdapter() {
			public void windowOpened( WindowEvent e ){
				((JTextField)editboxes.get(0)).requestFocus();
			}
		});
	}

	public int[] getKeyCodes() {
		dialog.setVisible( true );
		return null;
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int keyModifiers = e.getModifiers();
		int keyState = keyCode | (keyModifiers << 8);
		int j=0;

		if(canChooseNextKey) {
			FOREACH(JTextField, tf, editboxes) {
				if(tf.hasFocus()) {
					tf.setText((keyModifiers>0 ? KeyEvent.getKeyModifiersText(keyModifiers)+"+" : "") + KeyEvent.getKeyText(keyCode));
					((JTextField)editboxes.get(Math.min(j+1, editboxes.size()-1))).requestFocus();
					keyMap[j] = keyState;
				}
				++j;
			}
		}
		keysup[keyCode]=true;
		canChooseNextKey=true;
		for(int i=0; i<256; ++i) {
			canChooseNextKey = keysup[i] && canChooseNextKey;
		}
	}

	public void keyPressed(KeyEvent e) {
		keysup[e.getKeyCode()] = false;
	}

	public void keyTyped(KeyEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}
	public void actionPerformed( ActionEvent e ) {}
}
#endif
