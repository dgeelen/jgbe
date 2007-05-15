#ifndef USE_JAVA_MOBILE
public class keyConfigDialog implements ComponentListener, ActionListener {
	JDialog dialog;
	JFrame owner;
	DrawingArea DA;

	public class DrawingArea extends JPanel{
		private static final long serialVersionUID = 1L; // prevent warning
		public DrawingArea() { super(); }
		public String txt="";
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawString(txt, getWidth(),0);
		}
	}


	public keyConfigDialog(JFrame o) {
		owner = o;
		JScrollPane scroll;
		dialog= new JDialog(owner, "Key Bindings", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					dialog.setVisible(false);
			}
		});

		DA = new DrawingArea();
		scroll  = new JScrollPane(DA);
		scroll.setPreferredSize(new Dimension(480, 240));
		dialog.add(scroll,BorderLayout.CENTER);

		dialog.addComponentListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		dialog.pack();
		Dimension d = owner.getSize();
		Point     p = new Point();
		p.setLocation((owner.getLocation().getX()+(d.getWidth()/2)) - (dialog.getWidth()/2), (owner.getLocation().getY()+d.getHeight()/2)-(dialog.getHeight()/2));
		dialog.setLocation(p);

	}

	public int[] getKeyCodes() {
			dialog.setVisible( true );

			return null;
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}

	public void actionPerformed( ActionEvent e ) {
	}



}
#endif

