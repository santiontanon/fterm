package fterms;

import java.io.IOException;

import util.RewindableInputStream;

public class NOOSToken {
	public String token = null;
	public int type = -1;

	public static final int TOKEN_NONE = -1;
	public static final int TOKEN_LEFT_PAR = 0;
	public static final int TOKEN_RIGHT_PAR = 1;
	public static final int TOKEN_SYMBOL = 2;
	public static final int TOKEN_INTEGER = 3;
	public static final int TOKEN_STRING = 4;
	public static final int TOKEN_FLOAT = 5;
	public static final int TOKEN_ROOT = 6;
	public static final int TOKEN_PARENT = 7;
	public static final int TOKEN_INIT_VARIABLE = 8;
	public static final int TOKEN_REF_VARIABLE = 9;
	public static final int TOKEN_SINGLETON = 10;
	
	static NOOSToken getTokenNOOS(RewindableInputStream fp) throws IOException
	{
		NOOSToken t = new NOOSToken();
		char c;
		boolean end=false;
		int state;
		
		t.token="";
		t.type=TOKEN_NONE;

		state=0;
		while(!end) {
			if (fp.available()>0) {
				c=(char)fp.read();
				switch(state) {
				case -2: /* block comment: */ 
					if (c=='|') {
						c=(char)fp.read();
						if (c=='#') state=0;
					} /* if */ 
					break;
				case -1: /* comment */ 
					if (c=='\r' || c=='\n') state=0;
					break;
				case 0:
					if (c==';') {
						state=-1;
					} else if (c=='#') {
						c=(char)fp.read();
						if (c=='|') {
							state=-2;
						} else {
							return null;
						} /* if */ 
					} else if (c=='>') {
						t.token+=c;
						c=(char)fp.read();
						if (c=='>') {
							t.token+=c;
							t.type=TOKEN_ROOT;
						} /* if */ 
						end=true;
					} else if (c=='.') {
						t.type=TOKEN_PARENT;
						t.token+=c;
						do {
							c=(char)fp.read();
							if (c=='.') t.token+=c;
						}while(c=='.');
						fp.position(fp.position()-1);
						end=true;
					} else if (c=='?') {
						t.type=TOKEN_INIT_VARIABLE;
						state=4;				
					} else if (c=='!') {
						t.type=TOKEN_REF_VARIABLE;
						state=4;									
					} else if (c=='(' || c==')') {
						t.token+=c;
						if (c=='(') t.type=TOKEN_LEFT_PAR;
							   else t.type=TOKEN_RIGHT_PAR;
						end=true;
					} else if (c=='\"') {
						state=3;
					} else if ((c>='0' && c<='9') || c=='-') {
						t.type=TOKEN_INTEGER;
						t.token+=c;
						state=2;
					} else if (c!=' ' && c!='\r' && c!='\n' && c!='\t') {
						t.type=TOKEN_SYMBOL;
						t.token+=c;
						state=1;
					} /* if */ 
					break;
				case 1:
					if (c!=' ' && c!='\r' && c!='\n' && c!='\t' && 
						c!='(' && c!=')' && c!='\"') {
						t.token+=c;
					} else {
						fp.position(fp.position()-1);
						t.type=TOKEN_SYMBOL;
						end=true;
					} /* if */ 
					break;
				case 2:
					if ((c>='0' && c<='9') || c=='.') {
						t.token+=c;
						if (c=='.') t.type=TOKEN_FLOAT;
					} else {
						fp.position(fp.position()-1);
						end=true;
					} /* if */ 
					break;
				case 3:
					if (c!='\"') {
						t.token+=c;
					} else {
						t.type=TOKEN_STRING;
						end=true;
					} /* if */ 
					break;
				case 4:
					if (c!=' ' && c!='\r' && c!='\n' && c!='\t' && 
						c!='(' && c!=')' && c!='\"') {
						t.token+=c;
					} else {
						fp.position(fp.position()-1);
						end=true;
					} /* if */ 
					break;
				} /* switch */ 
			} else {
				end=true;
			} /* if */ 
		} /* while */ 

		if (t.token.equals("") && t.type!=4) {
			if (t.type==TOKEN_REF_VARIABLE) {
				t.type=TOKEN_SINGLETON;
				return t;
			} else {
				return null;
			} // if 
		} /* if */ 

		if (t.type!=TOKEN_STRING) t.token = t.token.toLowerCase();

//		System.out.println("token: " + t.type + " - " + t.token);

		return t;
	} /* FeatureTerm::getTokenNOOS */ 

}
