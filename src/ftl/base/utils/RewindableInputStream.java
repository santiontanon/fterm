/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
  
 package ftl.base.utils;

import java.io.IOException;
import java.io.InputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class RewindableInputStream.
 */
public class RewindableInputStream extends InputStream {

	/** The Constant CHUNK_SIZE. */
	static public final int CHUNK_SIZE = 256;

	/** The buffer. */
	byte[] buffer = null;

	/** The current pos. */
	int lastRead = 0, currentPos = 0;

	/** The stream. */
	InputStream stream = null;

	/**
	 * Instantiates a new rewindable input stream.
	 * 
	 * @param a_stream
	 *            the a_stream
	 */
	public RewindableInputStream(InputStream a_stream) {
		stream = a_stream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if (currentPos >= lastRead) {
			if (buffer == null) {
				buffer = new byte[CHUNK_SIZE];
			} else {
				if (lastRead + CHUNK_SIZE > buffer.length) {
					byte[] buffer_tmp = new byte[buffer.length * 2];
					for (int i = 0; i < buffer.length; i++)
						buffer_tmp[i] = buffer[i];
					buffer = buffer_tmp;
				}
			}

			int read = stream.read(buffer, currentPos, CHUNK_SIZE);
			lastRead += read;

			if (read == 0)
				return -1;
			return buffer[currentPos++];
		} else {
			return buffer[currentPos++];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return (lastRead - currentPos) + stream.available();
	}

	/**
	 * Position.
	 * 
	 * @return the int
	 */
	public int position() {
		return currentPos;
	}

	/**
	 * Position.
	 * 
	 * @param pos
	 *            the pos
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void position(int pos) throws IOException {
		if (pos > lastRead)
			throw new IOException("RewindableInputStream.position: pos is beyond the already read bytes!");
		currentPos = pos;
	}

}
