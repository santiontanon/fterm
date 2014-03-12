/*
 * Creator: Santi Ontanon Villar
 */

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
  
 package ftl.base.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;

import ftl.base.utils.FeatureTermException;

/**
 * The Class Symbol.
 */
public class Symbol implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4929099768950647569L;

	/** The s symbol hash. */
	static HashMap<String, StringBuffer> sSymbolHash = new HashMap<String, StringBuffer>();

	/** The m sym. */
	StringBuffer mSym;

	/**
	 * Instantiates a new symbol.
	 * 
	 * @param sym
	 *            the sym
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Symbol(String sym) throws FeatureTermException {
		if (sym == null)
			throw new FeatureTermException("null name in a Symbol!!!");
		if (sSymbolHash.containsKey(sym)) {
			mSym = sSymbolHash.get(sym);
		} else {
			mSym = new StringBuffer(sym);
			sSymbolHash.put(sym, mSym);
		}
	}

	/**
	 * Instantiates a new symbol.
	 * 
	 * @param sym
	 *            the sym
	 */
	public Symbol(Symbol sym) {
		mSym = sym.mSym;
	}

	/**
	 * Instantiates a new symbol.
	 * 
	 * @param fp
	 *            the fp
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Symbol(FileInputStream fp) throws IOException {
		mSym = null;
		load(fp);
	}

	/**
	 * Gets the.
	 * 
	 * @return the string
	 */
	public String get() {
		return mSym.toString();
	}

	/**
	 * Sets the.
	 * 
	 * @param str
	 *            the str
	 */
	public void set(String str) {
		mSym = new StringBuffer(str);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof String)
			return equals((String) o);
		else if (o instanceof StringBuffer)
			return equals((StringBuffer) o);
		else if (o instanceof Symbol)
			return equals((Symbol) o);
		return false;
	}

	/**
	 * Equals.
	 * 
	 * @param str
	 *            the str
	 * @return true, if successful
	 */
	public boolean equals(String str) {
		if (mSym == null) {
			if (str == null)
				return true;
			return false;
		} else {
			if (str == null)
				return false;
			return (mSym.toString().equals(str));
		}
	}

	/**
	 * Equals.
	 * 
	 * @param str
	 *            the str
	 * @return true, if successful
	 */
	public boolean equals(StringBuffer str) {
		if (mSym == null) {
			if (str == null)
				return true;
			return false;
		} else {
			if (str == null)
				return false;
			// System.out.println("Symbol.equals: '" + m_sym + "' == '" + str + "'? -> " +
			// m_sym.toString().equals(str.toString()));
			return (mSym.toString().equals(str.toString()));
		}
	}

	/**
	 * Equals.
	 * 
	 * @param sym
	 *            the sym
	 * @return true, if successful
	 */
	public boolean equals(Symbol sym) {
		return mSym == sym.mSym;
	}

	/**
	 * Arrange string.
	 * 
	 * @param str
	 *            the str
	 */
	static void arrangeString(StringBuffer str) {
		int len;

		while (str.charAt(0) == ' ' || str.charAt(0) == '\n' || str.charAt(0) == '\r' || str.charAt(0) == '\t')
			str = str.deleteCharAt(0);

		len = str.length();
		while (len > 1 && (str.charAt(len - 1) == ' ' || str.charAt(len - 1) == '\n' || str.charAt(len - 1) == '\r' || str.charAt(len - 1) == '\t')) {
			str = str.deleteCharAt(len - 1);
			len--;
		} /* while */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return mSym.toString();
	}

	/**
	 * From string.
	 * 
	 * @param str
	 *            the str
	 * @param pos
	 *            the pos
	 * @return the int
	 */
	public int fromString(String str, int pos) {
		int i;
		StringBuffer tmp = new StringBuffer("");

		mSym = null;

		while (str.charAt(pos) == ' ' || str.charAt(pos) == '\n' || str.charAt(pos) == '\r' || str.charAt(pos) == '\t')
			pos++;

		i = 0;
		while (str.charAt(pos) != ' ' && str.charAt(pos) != '\n' && str.charAt(pos) != '\r' && str.charAt(pos) != '\t')
			tmp.setCharAt(i++, str.charAt(pos++));

		if (tmp.equals("SYM")) {
			char c;
			StringBuffer res = new StringBuffer("");

			c = str.charAt(pos++);
			while (c == '\n' || c == ' ' || c == '\r' || c == '\t')
				c = str.charAt(pos++);
			for (i = 0; c != '\n' && c != ' ' && c != '\r' && c != '\t'; i++, c = str.charAt(pos++)) {
				switch (c) {
				case '\\':
					res.setCharAt(i, str.charAt(pos++));
					break;
				default:
					res.setCharAt(i, c);
				} /* switch */
			} /* for */

			mSym = res;
			return pos;
		} /* if */

		if (tmp.equals("NULLSYM")) {
			mSym = null;
			return pos;
		} /* if */

		return -1;
	}

	/**
	 * Load.
	 * 
	 * @param fp
	 *            the fp
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean load(FileInputStream fp) throws IOException {
		int i;
		char c;
		StringBuffer tmp = new StringBuffer("");
		mSym = null;

		do {
			c = (char) fp.read();
		} while (c == ' ' || c == '\n' || c == '\r' || c == '\t');

		i = 0;
		do {
			tmp.setCharAt(i, c);
			c = (char) fp.read();
			if (c != ' ' && c != '\n' && c != '\r' && c != '\t')
				tmp.setCharAt(i++, c);
		} while (c != ' ' && c != '\n' && c != '\r' && c != '\t');

		if (tmp.equals("SYM")) {
			StringBuffer res = new StringBuffer("");

			while (c == '\n' || c == ' ' || c == '\r' || c == '\t')
				c = (char) fp.read();
			for (i = 0; c != '\n' && c != ' ' && c != '\r' && c != '\t'; i++, c = (char) fp.read()) {
				switch (c) {
				case '\\':
					res.setCharAt(i, (char) fp.read());
					break;
				default:
					res.setCharAt(i, c);
				} /* switch */
			} /* for */

			mSym = res;
			return true;
		} /* if */

		if (tmp.equals("NULLSYM")) {
			mSym = null;
			return true;
		} /* if */

		return false;
	}

	/**
	 * Save.
	 * 
	 * @param fp
	 *            the fp
	 * @return true, if successful
	 */
	public boolean save(PrintWriter fp) {
		if (mSym != null) {
			fp.println("SYM");
			fp.println(mSym);
			return true;
		} /* if */
		fp.println("NULLSYM");
		return true;
	} /* Symbol::save */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (mSym == null)
			return 0;
		return mSym.hashCode();
	}

}
