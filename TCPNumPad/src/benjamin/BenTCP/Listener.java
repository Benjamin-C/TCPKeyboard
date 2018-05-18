package benjamin.BenTCP;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Listener extends Thread {

	private volatile long waitTime;
	private volatile boolean running;
	
	private InputStream is;
	private TCPOnDataArrival odr;
	
	public Listener(long delay, InputStream instream, TCPOnDataArrival onData) {
		waitTime = delay;
		running = false;
		is = instream;
		odr = onData;
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				if(is.available() > 0) {
					List<Byte> dataIn = new ArrayList<Byte>();
					while(is.available() > 0) {
						dataIn.add((byte) is.read());
					}
					byte[] data = new byte[dataIn.size()];
					for(int i = 0; i < dataIn.size(); i++) {
						data[i] = dataIn.get(i);
					}
					odr.onDataArrived(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized(this) {
		        try {
		            this.wait(waitTime);
		        } catch (InterruptedException e) { }
		    }
		}
	}
	
	@Override
	@Deprecated
	public void start() {
		throw new UnsupportedOperationException();
	}
	
	public void startListening() {
		running = true;
		super.start();
	}

	public void stopListening() {
		running = false;
	}
}
