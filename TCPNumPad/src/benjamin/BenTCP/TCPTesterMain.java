package benjamin.BenTCP;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class TCPTesterMain {

	public static volatile boolean running;
	public static volatile TCPServer ser;
	public static volatile TCPClient cli;
	
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		final Scanner scan = new Scanner(System.in);
		
		TCPSetupStream setupStream = new TCPSetupStream() {
			
			public void write(String str) {
				System.out.println(str);
			}
			
			public String read() {
				return scan.nextLine();
			}

			public void close() {
				System.exit(0);
			}
		};
		
		TCPOnDataArrival odr = new TCPOnDataArrival() {
			public void onDataArrived(byte[] data) {
				// TODO Auto-generated method stub
				String dataString = "";
				for(int i = 0; i < data.length; i++) {
					dataString = dataString + (char) data[i];
				}
				System.out.println("Recived: " + dataString);
			}
		};
		
		final OutputStream os;
		System.out.println("Type 'C' for client, or anything else for server");
		String in = scan.nextLine();
		if(in.length() > 0 && in.toUpperCase().charAt(0) == 'C') {
			System.out.println("Starting client");
			 cli = new TCPClient(odr, setupStream);
			os = cli.getOutputStream();
		} else {
			System.out.println("Starting server");
			ser = new TCPServer(odr, setupStream);
			os = ser.getOutputStream();
		}
		
		running = true;
		Thread speak = new Thread() {
			@Override
			public void run() {
				while(running) {
					if(scan.hasNextLine()) {
						String str = scan.nextLine();
						if(str.length() > 0) {
							if(str.charAt(0) == '/') {
								if(str.charAt(1) == '/') {
									print(str);
								}
								str = str.substring(1);
								switch(str) {
								case "end": { if(cli != null){cli.close();} if(ser != null){ser.close();} } break;
								}
							} else {
								print(str);
							}
						}
					}
				}
			}
			private void print(String str) {
				byte[] data = new byte[str.length()];
				for(int i = 0; i < str.length(); i++) {
					data[i] = (byte) str.charAt(i);
				}
				try {
					os.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.print("Sending ");
				for(int i = 0; i < data.length; i++) {
					System.out.print(toHex(data[i]) + " ");
				}
				System.out.println();
			}
		};
		speak.start();
	}
	
	private static String toHex(byte in) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(String.format("%02X", in));
	    return sb.toString();
	}
}
