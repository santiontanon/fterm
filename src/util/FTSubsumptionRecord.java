/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import fterms.FTKBase;
import fterms.FeatureTerm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 *
 * This class is used to collect subsumption examples
 */
public class FTSubsumptionRecord {

    static int binMax = 10; // we will store 10 terms in each bin

    static List<Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>> positive = new LinkedList<Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>>();
    static List<Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>> negative = new LinkedList<Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>>();

    public static void register(FeatureTerm f1, FeatureTerm f2, long time, boolean result) {
        if (time>0) {
//            System.out.println(time + " - " + result);
            List<Pair<FeatureTerm,FeatureTerm>> bin = getBin(time,result);

            if (bin.size()<binMax) {
                bin.add(new Pair<FeatureTerm,FeatureTerm>(f1,f2));

                // append to the tile with results:
                try{
                    FileWriter fstream = new FileWriter("subsumption-.txt",true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(";; " + time + " " + result + "\n");
                    out.write(f1.toStringNOOS());
                    out.write("\n");
                    out.write(f2.toStringNOOS());
                    out.write("\n");
                    out.flush();
                    out.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void dumpSummary() {
        System.out.println("With positive result:");
        for(Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin:positive) {
            System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
        }
        System.out.println("With negative result:");
        for(Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin:negative) {
            System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
        }
    }

    public static void dumpTests(FTKBase dm) {
        System.out.println(";; With positive result:");
        for(Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin:positive) {
            System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
            for(Pair<FeatureTerm,FeatureTerm> pair:bin.m_b) {
                System.out.println(pair.m_a.toStringNOOS(dm));
                System.out.println(pair.m_b.toStringNOOS(dm));
            }
        }
        System.out.println(";; With negative result:");
        for(Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin:negative) {
            System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
            for(Pair<FeatureTerm,FeatureTerm> pair:bin.m_b) {
                System.out.println(pair.m_a.toStringNOOS(dm));
                System.out.println(pair.m_b.toStringNOOS(dm));
            }
        }

    }

    public static List<Pair<FeatureTerm,FeatureTerm>> getBin(long time, boolean result) {
        List<Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>> list = null;
        int binID = (int)(time/1000);
        if (result) list = positive;
               else list = negative;

        for(Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin:list) {
            if (bin.m_a==binID) {
                return bin.m_b;
            }
        }

        Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>> bin = new Pair<Integer,List<Pair<FeatureTerm,FeatureTerm>>>(binID,new LinkedList<Pair<FeatureTerm,FeatureTerm>>());
        list.add(bin);
        System.out.println("New bin created for " + binID);
        return bin.m_b;
    }
}
