/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fterms.translators;

import java.util.Vector;
import util.Pair;

/**
 *
 * @author santi
 */
public class Clause {

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_ID = 0;
    public static final int TYPE_SYMBOL = 1;
    public static final int TYPE_INTEGER = 2;
    public static final int TYPE_FLOAT = 3;
    String head;
    Vector<Pair<Integer, String>> parameters;

    public Clause(String h) {
        head = h;
        parameters = new Vector<Pair<Integer, String>>();
    }

    public void setParameter(int i, int type, String value) {
        while (parameters.size() <= i) {
            parameters.add(new Pair<Integer, String>(TYPE_UNKNOWN, ""));
        }
        parameters.get(i).m_a = type;
        parameters.get(i).m_b = value;
    }

    public int getParameterType(int i) {
        return parameters.get(i).m_a;
    }

    public String getParameterValue(int i) {
        return parameters.get(i).m_b;
    }

    public int getNumberParameters() {
        return parameters.size();
    }

    public String toString() {
        String tmp = head + "(";

        for (int i = 0; i < parameters.size(); i++) {
            Pair<Integer, String> p = parameters.get(i);
            if (i < parameters.size() - 1) {
                tmp += p.m_b + ",";
            } else {
                tmp += p.m_b;
            }
        }
        tmp += ")";
        return tmp;
    }

    public String getHead() {
        return head;
    }
}
