package util;

public class Pair<T1, T2> {

    public T1 m_a;
    public T2 m_b;

    public Pair(T1 a, T2 b) {
        m_a = a;
        m_b = b;
    }

    
    public String toString() {
        return "<" + m_a + "," + m_b + ">";
    }

    
    public int compareTo(Object arg0) {
        if (!(arg0 instanceof Pair<?, ?>)) return -1;
        Pair<T1, T2> arg = (Pair<T1, T2>) arg0;

        if (arg.m_a.equals(m_a) && arg.m_b.equals(m_b)) return 0;

        return -1;
    }

    
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Pair<?, ?>)) return false;

        return this.compareTo(arg0) == 0;
    }
    

    public int hashCode() {
        return m_a.hashCode() + m_b.hashCode();
    }
}
