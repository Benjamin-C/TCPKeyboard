package benjamin.TCPKeyboard;

public class Main {

	@SuppressWarnings("unused")
	public static void main(String args[]) {
		if(args == null) {
			args = new String[4];
		}
		NumPad n = new NumPad(args);
	}
}
