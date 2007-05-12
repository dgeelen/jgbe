static private int LINKcntdwn = 0;
static private boolean LINKreq = false;

static class SendReceiveThread implements CPURunner, Runnable {
	private volatile int threadStatus = 0;
	private Thread srthread;
	// 0 means suspended
	// 1 means suspend->running requested
	// 2 means running
	// 3 means running->suspended requested
	synchronized public void suspend() {
		while (threadStatus != 0) {
			threadStatus = 3;
			while (threadStatus == 3) { YIELD; };
		}
	}

	synchronized public void resume() {
				threadStatus = 1;
/*		while (threadStatus != 2) {
			threadStatus = 1;
			while (threadStatus == 1) { YIELD; };
		}*/
	}

	public boolean isRunning() {
		return (threadStatus != 0);
	}

	SendReceiveThread() {
		srthread = new Thread(this);
		threadStatus=3; //start suspend
		srthread.start();
		while (!srthread.isAlive()) { YIELD; }; // wait for thread to actually start
	}

	public void run() {
		while (true) {
			while (threadStatus == 3) { YIELD; };
			try {
				PRINTF("Sending (as %d) $%02x ...",(IOP[0x02]&(1<<0)),IOP[0x01]);
				LinkCableOut.writeByte(IOP[0x01]);
				PRINTLN("done");
				PRINT("Receiving...");
				IOP[0x01] = LinkCableIn.readUnsignedByte();
				PRINTF("got $%02x\n",IOP[0x01]);
				triggerInterrupt(3);
				IOP[0x02] &= ~(1<<7); // 'transfer' finished
				threadStatus = 3;
				Thread.yield();
			}
			catch (IOException e) {
				PRINTLN("Error while transferring byte!");
				severLink();
			}




/*

			// suspended
			while (threadStatus == 0) { YIELD; };
			// suspend -> running
			if (threadStatus == 1) threadStatus = 2;
			// running
			while ((threadStatus == 2) && (LinkCableStatus!=0)) {
				try {
					threadStatus = 3;
					PRINTF("Sending (as %d) $%02x ...",(IOP[0x02]&(1<<0)),IOP[0x01]);
					LinkCableOut.writeByte(IOP[0x01]);
					PRINTLN("done");
					PRINT("Receiving...");
					IOP[0x01] = LinkCableIn.readUnsignedByte();
					PRINTF("got $%02x\n",IOP[0x01]);
					triggerInterrupt(3);
					IOP[0x02] &= ~(1<<7); // 'transfer' finished
					Thread.yield();
				}
				catch (IOException e) {
					PRINTLN("Error while transferring byte!");
					severLink();
				}
			};
			if (threadStatus == 2) threadStatus = 3; // CPU returned exception
			// running -> suspended
			if (threadStatus == 3) threadStatus = 0;
			// suspended
		*/}
	}
}



static protected ServerSocket LinkCablesrvr = null;

static protected Socket LinkCablesktOut = null;
static protected Socket LinkCablesktIn = null;

static protected DataInputStream LinkCableIn = null;
static protected DataOutputStream LinkCableOut = null;

static protected boolean LinkCableSendReceive=false;

static protected int LinkCableStatus = 0; // 0 = offline, 1 = server , 2 = client
static protected SendReceiveThread LinkCableThread=null;

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
		LinkCableThread=null;
	}
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
		//LinkCableThread = new SendReceiveThread();
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
		//LinkCableThread = new SendReceiveThread();
		LinkCableStatus=2;
	}
	else PRINTLN("WARNING: Can't serve while not offline");
}
