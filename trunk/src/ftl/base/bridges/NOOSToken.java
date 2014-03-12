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
  
 package ftl.base.bridges;

import java.io.IOException;

import ftl.base.utils.RewindableInputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class NOOSToken.
 */
public class NOOSToken {

	/** The token. */
	public String token = null;

	/** The type. */
	public int type = -1;

	/** The Constant TOKEN_NONE. */
	public static final int TOKEN_NONE = -1;

	/** The Constant TOKEN_LEFT_PAR. */
	public static final int TOKEN_LEFT_PAR = 0;

	/** The Constant TOKEN_RIGHT_PAR. */
	public static final int TOKEN_RIGHT_PAR = 1;

	/** The Constant TOKEN_SYMBOL. */
	public static final int TOKEN_SYMBOL = 2;

	/** The Constant TOKEN_INTEGER. */
	public static final int TOKEN_INTEGER = 3;

	/** The Constant TOKEN_STRING. */
	public static final int TOKEN_STRING = 4;

	/** The Constant TOKEN_FLOAT. */
	public static final int TOKEN_FLOAT = 5;

	/** The Constant TOKEN_ROOT. */
	public static final int TOKEN_ROOT = 6;

	/** The Constant TOKEN_PARENT. */
	public static final int TOKEN_PARENT = 7;

	/** The Constant TOKEN_INIT_VARIABLE. */
	public static final int TOKEN_INIT_VARIABLE = 8;

	/** The Constant TOKEN_REF_VARIABLE. */
	public static final int TOKEN_REF_VARIABLE = 9;

	/** The Constant TOKEN_SINGLETON. */
	public static final int TOKEN_SINGLETON = 10;

	/**
	 * Gets the token noos.
	 * 
	 * @param fp
	 *            the fp
	 * @return the token noos
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static NOOSToken getTokenNOOS(RewindableInputStream fp) throws IOException {
		NOOSToken t = new NOOSToken();
		char c;
		boolean end = false;
		int state;

		t.token = "";
		t.type = TOKEN_NONE;

		state = 0;
		while (!end) {
			if (fp.available() > 0) {
				c = (char) fp.read();
				switch (state) {
				case -2: /* block comment: */
					if (c == '|') {
						c = (char) fp.read();
						if (c == '#')
							state = 0;
					} /* if */
					break;
				case -1: /* comment */
					if (c == '\r' || c == '\n')
						state = 0;
					break;
				case 0:
					if (c == ';') {
						state = -1;
					} else if (c == '#') {
						c = (char) fp.read();
						if (c == '|') {
							state = -2;
						} else {
							return null;
						} /* if */
					} else if (c == '>') {
						t.token += c;
						c = (char) fp.read();
						if (c == '>') {
							t.token += c;
							t.type = TOKEN_ROOT;
						} /* if */
						end = true;
					} else if (c == '.') {
						t.type = TOKEN_PARENT;
						t.token += c;
						do {
							c = (char) fp.read();
							if (c == '.')
								t.token += c;
						} while (c == '.');
						fp.position(fp.position() - 1);
						end = true;
					} else if (c == '?') {
						t.type = TOKEN_INIT_VARIABLE;
						state = 4;
					} else if (c == '!') {
						t.type = TOKEN_REF_VARIABLE;
						state = 4;
					} else if (c == '(' || c == ')') {
						t.token += c;
						if (c == '(')
							t.type = TOKEN_LEFT_PAR;
						else
							t.type = TOKEN_RIGHT_PAR;
						end = true;
					} else if (c == '\"') {
						state = 3;
					} else if ((c >= '0' && c <= '9') || c == '-') {
						t.type = TOKEN_INTEGER;
						t.token += c;
						state = 2;
					} else if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
						t.type = TOKEN_SYMBOL;
						t.token += c;
						state = 1;
					} /* if */
					break;
				case 1:
					if (c != ' ' && c != '\r' && c != '\n' && c != '\t' && c != '(' && c != ')' && c != '\"') {
						t.token += c;
					} else {
						fp.position(fp.position() - 1);
						t.type = TOKEN_SYMBOL;
						end = true;
					} /* if */
					break;
				case 2:
					if ((c >= '0' && c <= '9') || c == '.') {
						t.token += c;
						if (c == '.')
							t.type = TOKEN_FLOAT;
					} else {
						fp.position(fp.position() - 1);
						end = true;
					} /* if */
					break;
				case 3:
					if (c != '\"') {
						t.token += c;
					} else {
						t.type = TOKEN_STRING;
						end = true;
					} /* if */
					break;
				case 4:
					if (c != ' ' && c != '\r' && c != '\n' && c != '\t' && c != '(' && c != ')' && c != '\"') {
						t.token += c;
					} else {
						fp.position(fp.position() - 1);
						end = true;
					} /* if */
					break;
				} /* switch */
			} else {
				end = true;
			} /* if */
		} /* while */

		if (t.token.equals("") && t.type != 4) {
			if (t.type == TOKEN_REF_VARIABLE) {
				t.type = TOKEN_SINGLETON;
				return t;
			} else {
				return null;
			} // if
		} /* if */

		if (t.type != TOKEN_STRING)
			t.token = t.token.toLowerCase();

		// System.out.println("token: " + t.type + " - " + t.token);

		return t;
	} /* FeatureTerm::getTokenNOOS */

}
