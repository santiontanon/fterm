/*
 * Creator: Santi Ontanon Villar
 */

package fterms;

import fterms.exceptions.FeatureTermException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Symbol {

	static HashMap<String,StringBuffer> s_symbol_hash = new HashMap<String,StringBuffer>();
	StringBuffer m_sym;
		
//	public Symbol() {
//		m_sym = null;
//	}

	public Symbol(String sym) throws FeatureTermException {
            if (sym==null) throw new FeatureTermException("null name in a Symbol!!!");
		if (s_symbol_hash.containsKey(sym)) {
			m_sym = s_symbol_hash.get(sym);
		} else {
			m_sym = new StringBuffer(sym);
			s_symbol_hash.put(sym,m_sym);
		}
	}

	public Symbol(Symbol sym) {
		m_sym = sym.m_sym;
	}

	public Symbol(FileInputStream fp) throws IOException {
		m_sym = null;
		load(fp);
	}
	
	public String get() {
		return m_sym.toString();
	}

	public void set(String str) {
		m_sym = new StringBuffer(str);
	}
	
	public boolean equals(Object o) {
		if (o instanceof String) return equals((String)o);
		else if (o instanceof StringBuffer) return equals((StringBuffer)o);
		else if (o instanceof Symbol) return equals((Symbol)o);
		return false;
	}
	
	public boolean equals(String str) {
		if (m_sym==null) {
			if (str==null) return true;
			return false;
		} else {
			if (str==null) return false;
			return (m_sym.toString().equals(str));
		}
	}

	public boolean equals(StringBuffer str) {
		if (m_sym==null) {
			if (str==null) return true;
			return false;
		} else {
			if (str==null) return false;
//			System.out.println("Symbol.equals: '" + m_sym + "' == '" + str + "'? -> " + m_sym.toString().equals(str.toString()));
			return (m_sym.toString().equals(str.toString()));
		}
	}

	
	public boolean equals(Symbol sym) {
		return m_sym==sym.m_sym;
	}
	
	static void arrangeString(StringBuffer str) {
		int len;

		while(str.charAt(0)==' ' || str.charAt(0)=='\n' || str.charAt(0)=='\r' ||
			  str.charAt(0)=='\t') str = str.deleteCharAt(0);		

		len=str.length();
		while(len>1 && (str.charAt(len-1)==' ' || str.charAt(len-1)=='\n' || str.charAt(len-1)=='\r' ||
			  str.charAt(len-1)=='\t')) {
			str = str.deleteCharAt(len-1);
			len--;
		} /* while */ 		
	}
	
	public String toString() {
		return m_sym.toString();
	}
	
	public int fromString(String str,int pos) {
		int i;
		StringBuffer tmp = new StringBuffer("");

		m_sym=null;

		while(str.charAt(pos)==' ' || str.charAt(pos)=='\n' || str.charAt(pos)=='\r' || str.charAt(pos)=='\t') pos++;

		i=0;
		while(str.charAt(pos)!=' ' && str.charAt(pos)!='\n' && str.charAt(pos)!='\r' && str.charAt(pos)!='\t') tmp.setCharAt(i++,str.charAt(pos++));

		if (tmp.equals("SYM")) {
			char c;
			StringBuffer res = new StringBuffer("");

			c=str.charAt(pos++);
			while(c=='\n' || c==' ' || c=='\r' || c=='\t') c=str.charAt(pos++);
			for(i=0;c!='\n' && c!=' ' && c!='\r' && c!='\t';i++,c=str.charAt(pos++)) {
				switch(c) {
				case '\\':res.setCharAt(i,str.charAt(pos++));
					break;
				default:res.setCharAt(i,c);
				} /* switch */ 
			} /* for */ 

			m_sym = res;
			return pos;
		} /* if */ 

		if (tmp.equals("NULLSYM")) {
			m_sym=null;
			return pos;
		} /* if */ 

		return -1;		
	}
	
	public boolean load(FileInputStream fp) throws IOException {
		int i;
		char c;
		StringBuffer tmp = new StringBuffer("");
		m_sym=null;

		do{
			c = (char)fp.read();
		}while(c==' ' || c=='\n' || c=='\r' || c=='\t');

		i=0;
		do{
			tmp.setCharAt(i, c);
			c = (char)fp.read();
			if (c!=' ' && c!='\n' && c!='\r' && c!='\t') tmp.setCharAt(i++,c);
		} while(c!=' ' && c!='\n' && c!='\r' && c!='\t');
		
		
		if (tmp.equals("SYM")) {
			StringBuffer res = new StringBuffer("");

			while(c=='\n' || c==' ' || c=='\r' || c=='\t') c=(char)fp.read();
			for(i=0;c!='\n' && c!=' ' && c!='\r' && c!='\t';i++,c=(char)fp.read()) {
				switch(c) {
				case '\\':res.setCharAt(i,(char)fp.read());
					break;
				default:res.setCharAt(i,c);
				} /* switch */ 
			} /* for */ 

			m_sym = res;
			return true;
		} /* if */ 

		if (tmp.equals("NULLSYM")) {
			m_sym=null;
			return true;
		} /* if */ 

		return false;		
	}
	
	public boolean save(PrintWriter fp)
	{
		if (m_sym!=null) {
			fp.println("SYM");
			fp.println(m_sym);
			return true;
		} /* if */ 
		fp.println("NULLSYM");
		return true;
	} /* Symbol::save */ 
	

    public int hashCode() {
    	if (m_sym==null) return 0;
    	return m_sym.hashCode();
    }

	
}	
