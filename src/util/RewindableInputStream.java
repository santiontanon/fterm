package util;

import java.io.IOException;
import java.io.InputStream;

public class RewindableInputStream extends InputStream {
	static public final int CHUNK_SIZE = 256;
	byte []buffer = null;
	int lastRead = 0, currentPos = 0;
	InputStream stream = null;
	
	public RewindableInputStream(InputStream a_stream) {
		stream = a_stream;
	}
	
	public int read() throws IOException {
		if (currentPos>=lastRead) {
			if (buffer == null) {
				buffer = new byte[CHUNK_SIZE];
			} else {
				if (lastRead+CHUNK_SIZE>buffer.length) {
					byte []buffer_tmp = new byte[buffer.length*2];
					for(int i = 0;i<buffer.length;i++) buffer_tmp[i]=buffer[i];
					buffer = buffer_tmp;
				}
			}
			
			int read = stream.read(buffer, currentPos, CHUNK_SIZE);				
			lastRead+=read;
				
			if (read==0) return -1;
			return buffer[currentPos++];			
		} else {
			return buffer[currentPos++];			
		}		
	}
	
	public int available() throws IOException {
		return (lastRead - currentPos) + stream.available();
	}
	
	public int position()  {
		return currentPos;
	}
	
	public void position(int pos) throws IOException  {
		if (pos>lastRead) throw new IOException("RewindableInputStream.position: pos is beyond the already read bytes!");
		currentPos = pos;
	}
	
}
