

static protected ServerSocket LinkCablesrvr = null;

static protected Socket LinkCablesktOut = null;
static protected Socket LinkCablesktIn = null;

static protected DataInputStream LinkCableIn = null;
static protected DataOutputStream LinkCableOut = null;

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
	LinkCableStatus=0;
}

public static final  void serveLink() {
	if(LinkCableStatus==0) {
		try {
				LinkCablesrvr = new ServerSocket(LINK_CABLE_TCP_PORT);
				LinkCablesktOut = LinkCablesrvr.accept();
				PRINTLN("Connection established");
				LinkCableIn = new DataInputStream(LinkCablesktOut.getInputStream());
				LinkCableOut = new DataOutputStream(LinkCablesktOut.getOutputStream());
		}
		catch(Exception e) {
				PRINTLN("WARNING: Error starting LinkCableServer");
		}
		LinkCableStatus=1;
	}
	else PRINTLN("WARNING: Can't serve while not offline");
}


public static final void clientLink(String target) {
	if(LinkCableStatus==0) {
		try {
				LinkCablesktIn = new Socket(target, LINK_CABLE_TCP_PORT);
				LinkCableIn = new DataInputStream(LinkCablesktIn.getInputStream());
				LinkCableOut = new DataOutputStream(LinkCablesktIn.getOutputStream());
		}
		catch(Exception e) {
				PRINTLN("WARNING: Error starting LinkCableClient");
		}
		LinkCableStatus=2;
	}
	else PRINTLN("WARNING: Can't serve while not offline");
}
