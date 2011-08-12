/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.SetFeatureTerm;
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

class FTSRecord {
    FeatureTerm f1,f2;
    long time;  // time with normal subsumption
    long CSPtime;  // time with CSP subsumption

    public FTSRecord(FeatureTerm a_f1, FeatureTerm a_f2, long a_time, long a_CSPtime) {
        f1 = a_f1;
        f2 = a_f2;
        time = a_time;
        CSPtime = a_CSPtime;
    }
}

public class FTSubsumptionRecord {

    static int count = 0;
    static int binMax = 10; // we will store 10 terms in each bin

    static List<Pair<Integer,List<FTSRecord>>> positive = new LinkedList<Pair<Integer,List<FTSRecord>>>();
    static List<Pair<Integer,List<FTSRecord>>> negative = new LinkedList<Pair<Integer,List<FTSRecord>>>();

    public static void register(FeatureTerm f1, FeatureTerm f2, long time, long CSPtime, boolean result) {
        if (time>0) {
//            System.out.println(time + " - " + result);
            List<FTSRecord> bin = getBin(Math.max(time,CSPtime),result);

            if (bin.size()<binMax) {
                bin.add(new FTSRecord(f1,f2, time, CSPtime));

                // save test to summary file:
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
        for(Pair<Integer,List<FTSRecord>> bin:positive) {
            System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
        }
        System.out.println("With negative result:");
        for(Pair<Integer,List<FTSRecord>> bin:negative) {
            System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
        }
    }

    public static void dumpStatistics() {
        for(Pair<Integer,List<FTSRecord>> bin:positive) {
            for(FTSRecord r:bin.m_b) {
                System.out.println(FTRefinement.variables(r.f1).size() + ", " + FTRefinement.variables(r.f2).size() + ", " + maxSet(r.f1) + ", " + maxSet(r.f2) + ", " + totalSetSize(r.f1) + ", " + totalSetSize(r.f2) + ", " + r.time + ", " + r.CSPtime + ", true");
            }
        }
        for(Pair<Integer,List<FTSRecord>> bin:negative) {
            for(FTSRecord r:bin.m_b) {
                System.out.println(FTRefinement.variables(r.f1).size() + ", " + FTRefinement.variables(r.f2).size() + ", " + maxSet(r.f1) + ", " + maxSet(r.f2) + ", " + totalSetSize(r.f1) + ", " + totalSetSize(r.f2) + ", " + r.time + ", " + r.CSPtime + ", false");
            }
        }
    }

    public static void dumpTests(FTKBase dm) {
        System.out.println(";; With positive result:");
        for(Pair<Integer,List<FTSRecord>> bin:positive) {
            System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
            for(FTSRecord record:bin.m_b) {
                System.out.println(record.f1.toStringNOOS(dm));
                System.out.println(record.f2.toStringNOOS(dm));
            }
        }
        System.out.println(";; With negative result:");
        for(Pair<Integer,List<FTSRecord>> bin:negative) {
            System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
            for(FTSRecord record:bin.m_b) {
                System.out.println(record.f1.toStringNOOS(dm));
                System.out.println(record.f2.toStringNOOS(dm));
            }
        }
    }

    public static int maxSet(FeatureTerm t) {
        List<SetFeatureTerm> l = FTRefinement.sets(t);
        int maxSize = 0;

        for(SetFeatureTerm s:l) {
            if (s.getSetValues().size()>maxSize) maxSize = s.getSetValues().size();
        }

        return maxSize;
    }

    public static int totalSetSize(FeatureTerm t) {
        List<SetFeatureTerm> l = FTRefinement.sets(t);
        int size = 0;

        for(SetFeatureTerm s:l) {
            size += s.getSetValues().size();
        }

        return size;
    }

    public static List<FTSRecord> getBin(long time, boolean result) {
        List<Pair<Integer,List<FTSRecord>>> list = null;
        int binID = (int)(time/100);
        if (result) list = positive;
               else list = negative;

        for(Pair<Integer,List<FTSRecord>> bin:list) {
            if (bin.m_a==binID) {
                return bin.m_b;
            }
        }

        Pair<Integer,List<FTSRecord>> bin = new Pair<Integer,List<FTSRecord>>(binID,new LinkedList<FTSRecord>());
        list.add(bin);
        System.out.println("New bin created for " + binID);
        return bin.m_b;
    }
}
