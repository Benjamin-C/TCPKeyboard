package tcpNumPad;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import benjamin.BenTCP.TCPClient;
import benjamin.BenTCP.TCPOnDataArrival;
import benjamin.BenTCP.TCPSetupStream;

public class NumPad {
	
	private JLabel cueLabel;
	private JLabel type;
	private List<Integer> cue;
	private int num;
	
	private TCPClient cli;
	private final String TYPE_DEFUALUT_TEXT = "0x";
	
	private String[] args;
	
	private TCPOnDataArrival odr;
	private TCPSetupStream tss;
	
	private JFrame jframe;
	
	private JTextField ip;
	private JTextField port;
	
	private JLabel startLabel;
	
	private boolean console;
	public NumPad() {
		this(new String[] {"", "", ""});
	}
	
	public NumPad(String ardgs[]) {
		args = new String[4];//ardgs.length];
		for(int i = 0; i < Math.min(args.length, ardgs.length); i++) {
			args[i] = ardgs[i];
		}
		
		odr = new TCPOnDataArrival() {
			@Override
			public void onDataArrived(byte[] data) {
				// TODO Auto-generated method stub
				String temp = "";
				for(int i = 0; i < data.length; i++) {
					temp = temp + toHex(data[i]);
				}
			}
			private String toHex(byte in) {
			    StringBuilder sb = new StringBuilder();
			    sb.append(String.format("%02X", in) + "");
			    return sb.toString();
			}
		};
		final Scanner s = new Scanner(System.in);
		tss = new TCPSetupStream() {
			
			private List<String> next = new ArrayList<String>();
			
			@Override
			public void write(String data) {
				System.out.println(data);
				if(data.contains("Would you like to try a new IP? (y/n)") || data.contains("Please")) {
					tryAgain();
				}
			}
			
			@Override
			public String read() {
				return s.nextLine();
			}
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				System.out.println("SetupStram closing");
				s.close();
			}
		};
		jframe = new JFrame("TCP Keys");
		jframe.setResizable(false);
		ImageIcon ico = new ImageIcon("lib/icon.png");
		jframe.setIconImage(ico.getImage());
		
		if(args[0].equals("") || args[1].equals("") || args[2].equals("wait")) {
			if(args[0].equals("")) {
				args[0] = "192.168.000.000";
			}
			if(args[1].equals("")) {
				args[1] = "00000001";
			}
			JPanel tempjpanel = new JPanel();
			startLabel = new JLabel("Waiting for TCP | ");
			tempjpanel.add(startLabel);
			JLabel iptext = new JLabel("ip:");
			ip = new JTextField(args[0]);
			tempjpanel.add(iptext);
			tempjpanel.add(ip);
			JLabel porttext = new JLabel("port:");
			tempjpanel.add(porttext);
			port = new JTextField(args[1]);
			tempjpanel.add(port);
			JButton ready = new JButton("Click when ready");
			JButton def = new JButton("localhost:128");
			JButton clear = new JButton("Clear");
			ready.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {setup(ip.getText(), port.getText());} });
			def.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {ip.setText("localhost");port.setText("128");} });
			clear.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {ip.setText("192.168.000.000");port.setText("00000001");} });
			tempjpanel.add(ready);
			tempjpanel.add(def);
			tempjpanel.add(clear);
			jframe.add(tempjpanel);
			jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jframe.pack();
			jframe.setVisible(true);
		} else {
			setup(args[0], args[1]);
		}
	}
	
	public void tryAgain() {
		System.exit(0);
	}
	
	public void setup(String ip, String port) {
		cli = new TCPClient(ip, Integer.parseInt(port), odr, tss, 1, "me");
		if(cli.getOutputStream() == null) {
			System.out.println("Retry");
			tryAgain();
		} else {
			run();
		}
	}
	
	public void run() {
		
		String[][] text = { {"C", "D", "E", "F"}, 
							{"8", "9", "A", "B"}, 
							{"4", "5", "6", "7"}, 
							{"0", "1", "2", "3"},
							{"<", "Q", "G", "C"} };
		ActionListener[][] todo = { {listen(0x0C), listen(0x0D), listen(0x0E), listen(0x0F)},
									{listen(0x08), listen(0x09), listen(0x0A),listen(0x0B)},
									{listen(0x04), listen(0x05), listen(0x06), listen(0x07)},
									{listen(0x00), listen(0x01), listen(0x02), listen(0x03)},
									{backspace(), queue(), send(), clear()}};
		int maxSize = 0;
		cue = new ArrayList<Integer>();
		JPanel mainPanel = new JPanel(new GridLayout(2 + text.length, 2, 0, 0));
		for(String[] str : text) {
			maxSize = Math.max(maxSize,  str.length);
		}
		//JPanel buttonPanel = new JPanel(new GridLayout(text.length, maxSize, 4, 4));
		Font defFont = new Font("arial", 16, 16);
		cueLabel = new JLabel(""); cueLabel.setFont(defFont); mainPanel.add(cueLabel);
		type = new JLabel(TYPE_DEFUALUT_TEXT); type.setFont(defFont); mainPanel.add(type);
		for(int i = 0; i < text.length; i++) {
			maxSize = Math.max(maxSize, text[i].length);
			JPanel row = new JPanel(new GridLayout(1, maxSize, 0, 0));
			for(int j = 0; j < text[i].length; j++) {
				JButton b=new JButton(text[i][j]); 
				b.setFont(defFont);
				b.setPreferredSize(new Dimension(50, 50));
				b.addActionListener(todo[i][j]);
				row.add(b);
			}
			mainPanel.add(row);
		}
		jframe.setContentPane(mainPanel);
		jframe.pack();
		jframe.setVisible(true);
	}
	
	public ActionListener listen(final int data) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				num = num << 4;
				num = num & 0xFF;
				num = num + (data & 0xFF);
				type.setText("0x" + Integer.toHexString(num));
			}
		};
	}
	public ActionListener queue() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				type.setText(TYPE_DEFUALUT_TEXT);
				cue.add(num);
				num = 0;
				showCue();
			}
		};
	}
	public ActionListener backspace() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(num == 0) {
					if(cue.size() > 0) {
						num = cue.remove(cue.size() - 1);
						type.setText("0x" + Integer.toHexString(num));
						showCue();
					}
				} else {
					num = (num & 0xFF) >> 4;
					type.setText("0x" + Integer.toHexString(num));
				}
			}
		};
	}
	public ActionListener clear() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				type.setText(TYPE_DEFUALUT_TEXT);
				num = 0;
				cue.clear();
				showCue();
			}
		};
	}
	public ActionListener send() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(cue.size() > 0) {
					byte bt[] = new byte[cue.size()];
					for(int i = 0; i < cue.size(); i++) {
						int t = cue.get(i);
						bt[i] = (byte) t;
					}
					String temp = "";
					for(int i = 0; i < bt.length; i++) {
						temp = temp + ", 0x" + Integer.toHexString((int) bt[i]);
					}
					try {
						cli.getOutputStream().write(bt);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					type.setText(TYPE_DEFUALUT_TEXT);
					cue.clear();
					showCue();
				}
			}
		};
	}
	public void showType() {
		String temp = Integer.toHexString(num);
		if(num == 0) {
			temp = "";
		}
		type.setText("0x" + temp);
	}
	public void showCue() {
		String temp = "";
		if(cue.size() > 0) {
			for(int i = cue.size() - 1; i > cue.size() - 5; i--) {
				if(i >= 0) {
					String temp2 = Integer.toHexString(cue.get(i));
					if(temp2.length() == 1) {
						temp2 = "0" + temp2;
					}
					temp = ", 0x" + temp2 + temp;
				}
			}
			if(cue.size() > 4) {
				temp = "..." + temp;
			} else {
				temp = temp.substring(2);
			}
		}
		cueLabel.setText(temp);
	}
}
