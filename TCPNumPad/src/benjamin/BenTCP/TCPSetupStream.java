package benjamin.BenTCP;

public interface TCPSetupStream {
	
	public abstract void write(String str);
	public abstract String read();
	public abstract void close();
}
