static private int LINKmulti = 1;
static private int LINKdelay = 0; // do not change, use setDelay() instead

static private int LINKcntdwn = 0;
static private int[] LINKbuf = new int[8];
static private int LINKind = 0;
static private int LINKtimeout = 0;

static void setDelay(int ndelay) throws IOException {
	for (int i = 0; i < LINKdelay; ++i)
		LinkCableIn.readInt();
	LINKdelay = ndelay;
	for (int i = 0; i < LINKdelay; ++i)
		LinkCableOut.writeInt(0);
	LINKmulti = LINKdelay + 1;
}

static protected ServerSocket LinkCablesrvr = null;

static protected Socket LinkCablesktOut = null;
static protected Socket LinkCablesktIn = null;

static protected DataInputStream LinkCableIn = null;
static protected DataOutputStream LinkCableOut = null;

static protected boolean LinkCableSendReceive=false;

static protected int LinkCableStatus = 0; // 0 = offline, 1 = server , 2 = client

public static final void severLink() {
	try {
		if(LinkCablesrvr!=null) {
			LinkCablesrvr.close();
			LinkCablesrvr=null;
		}
		if(LinkCablesktOut!=null) {
			LinkCablesktOut.close();
			LinkCablesktOut=null;
		}
		if(LinkCablesktIn!=null) {
			LinkCablesktIn.close();
			LinkCablesktIn=null;
		}
		if(LinkCableIn!=null) {
			LinkCableIn.close();
			LinkCableIn=null;
		}
		if(LinkCableOut!=null) {
			LinkCableOut.close();
			LinkCableOut=null;
		}
	}
	catch(IOException e) {
		PRINTLN("Error while closing socket(s)");
		e.printStackTrace();
	}
	finally {
		LinkCableStatus=0;
	}
}

public static final  void serveLink() {
	if(LinkCableStatus==0) {
		try {
			LinkCablesrvr = new ServerSocket(LINK_CABLE_TCP_PORT);
			LinkCablesktOut = LinkCablesrvr.accept();
			PRINTLN("Connection established");
			LinkCablesktOut.setTcpNoDelay(true);
			LinkCableIn = new DataInputStream(LinkCablesktOut.getInputStream());
			LinkCableOut = new DataOutputStream(LinkCablesktOut.getOutputStream());
			LinkCableStatus=1;
			setDelay(0);
		}
		catch(Exception e) {
				PRINTLN("WARNING: Error starting LinkCableServer");
				e.toString();
				e.printStackTrace();
		}
		//LinkCableThread = new SendReceiveThread();
	}
	else PRINTLN("WARNING: Can't serve while not offline");
}

public static final void clientLink(String target) {
	if(LinkCableStatus==0) {
		try {
			LinkCablesktIn = new Socket(target, LINK_CABLE_TCP_PORT);
			LinkCablesktIn.setTcpNoDelay(true);
			LinkCableIn = new DataInputStream(LinkCablesktIn.getInputStream());
			LinkCableOut = new DataOutputStream(LinkCablesktIn.getOutputStream());
			LinkCableStatus=2;
			setDelay(0);
		}
		catch(Exception e) {
				PRINTLN("WARNING: Error starting LinkCableClient");
				e.toString();
				e.printStackTrace();
		}
		//LinkCableThread = new SendReceiveThread();
	}
	else PRINTLN("WARNING: Can't client while not offline");
}
