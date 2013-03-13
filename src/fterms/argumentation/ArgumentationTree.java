/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FeatureTerm;
import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author santi
 */
public class ArgumentationTree {
    static public int DEBUG = 0;

    Argument m_root;
    HashMap<Argument,List<Argument>> m_children = new HashMap<Argument,List<Argument>>();
    HashMap<Argument,Argument> m_parent = new HashMap<Argument,Argument>();
    List<Argument> m_arguments = new LinkedList<Argument>();
    
    // This list contains the set of agents who have settled for a particular argument,
    // (the agent who generated the argument might not be in the list)
    HashMap<Argument,List<String>> m_partially_settled = new HashMap<Argument,List<String>>();
    
    // This hash stores the retracted arguments. For each argument A, it stores a list with
    // all the arguments that used to attack A but were retracted.
    HashMap<Argument,List<Argument>> m_retracted = new HashMap<Argument,List<Argument>>();

    public ArgumentationTree(Argument root) {
        m_root = root;
        m_arguments.add(root);
    }

    public ArgumentationTree clone() {
        ArgumentationTree t = new ArgumentationTree(m_root);
        t.m_arguments.clear();
        for(Argument a:m_children.keySet()) {
            List<Argument> l = m_children.get(a);
            List<Argument> l2 = new LinkedList<Argument>();
            l2.addAll(l);
            t.m_children.put(a,l2);
        }
        t.m_parent.putAll(m_parent);
        t.m_arguments.addAll(m_arguments);

        for(Argument a:m_partially_settled.keySet()) {
            List<String> l = m_partially_settled.get(a);
            List<String> l2 = new LinkedList<String>();
            l2.addAll(l);
            t.m_partially_settled.put(a,l2);
        }
        t.m_retracted = new HashMap<Argument,List<Argument>>();
        for(Argument a:m_retracted.keySet()) {
            List<Argument> l = m_retracted.get(a);
            List<Argument> l2 = new LinkedList<Argument>();
            l2.addAll(l);
            t.m_retracted.put(a,l2);
        }
        return t;
    }

    public void addAttack(Argument attacked,Argument attacker) throws FeatureTermException {
        if (m_arguments.contains(attacked)) {
            List<Argument> children = m_children.get(attacked);
            if (children==null) {
                children = new LinkedList<Argument>();
                m_children.put(attacked,children);
            }
            if (!children.contains(attacker)) {
                if (DEBUG>=1) {
                    FeatureTerm a1 = attacked.m_rule.pattern;
                    FeatureTerm a2 = (attacker.m_type==Argument.ARGUMENT_EXAMPLE ? attacker.m_example:attacker.m_rule.pattern);

                    if (attacked.m_type==Argument.ARGUMENT_EXAMPLE) System.err.println("addAttack: adding an attack against an example!!!!!!");
                    if (a2.subsumes(a1)) System.err.println("addAttack: attack equal or more general than oririnal argument!!!!");
                }
                children.add(attacker);
                m_arguments.add(attacker);
                m_parent.put(attacker, attacked);
            } else {
                System.err.println("Adding a repeated attack to a tree!!!");
            }
        } else {
            System.err.println("Adding attack to argument not in the tree!!!");
        }
    }

    public Argument getRoot() {
        return m_root;
    }

    public List<Argument> getArguments() {
        return m_arguments;
    }

    // The difference of this method and the previous, is that this one also returns retracted
    // arguments:
    public List<Argument> getAllArguments() {
        List<Argument> args = new LinkedList<Argument>();
        args.addAll(m_arguments);
        for(Argument a:getArgumentsWithRetractedChildren()) {
            for(Argument a2:m_retracted.get(a)) {
                if (!args.contains(a2)) args.add(a2);
            }
        }
        return args;
    }


    public int getSize() {
        return m_arguments.size();
    }

    public boolean contains(Argument a) {
        return m_arguments.contains(a);
    }

    public boolean containsEquivalent(Argument a) throws FeatureTermException {
        for(Argument a2:m_arguments) {
            if (a2.equivalents(a)) return true;
        }
        return false;
    }

    public boolean defeatedP() {
        return defeatedP(m_root);
    }

    public boolean defeatedP(Argument a) {
        List<Argument> args = m_children.get(a);
        if (args==null) return false;
        for(Argument a2:args) {
            if (a2.m_type == Argument.ARGUMENT_RULE && !defeatedP(a2)) return true;
        }
        return false;
    }

    public List<FeatureTerm> getExamples() {
        List<FeatureTerm> examples = new LinkedList<FeatureTerm>();

        for(Argument a:m_arguments) {
            if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
                examples.add(a.m_example);
            }
        }

        return examples;
    }

    public List<FeatureTerm> getExamplesSentToAgent(String name) {
        List<FeatureTerm> examples = new LinkedList<FeatureTerm>();

        for(Argument a:m_arguments) {
            if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
                Argument p = m_parent.get(a);
                if (p!=null && p.m_agent.equals(name)) examples.add(a.m_example);
            }
        }

        return examples;
    }

    public List<Argument> getRuleArguments() {
        List<Argument> args = new LinkedList<Argument>();

        for(Argument a:m_arguments) {
            if (a.m_type == Argument.ARGUMENT_RULE) {
                args.add(a);
            }
        }

        return args;
    }

    // Get all the arguments that if defeated, the root would be warranted:
    // It does not take into account whether they are settled or not
    public List<Argument> getChallengers() {
        return getChallengers(m_root);
    }

    public List<Argument> getChallengers(Argument a) {
        List<Argument> args = new LinkedList<Argument>();
        if (defeatedP(a)) {
            for(Argument b:m_children.get(a)) {
                if (b.m_type == Argument.ARGUMENT_RULE && !defeatedP(b)) {
                    List<Argument> children = m_children.get(b);
                    if (children==null) {
                        args.add(b);
                    } else {
                        List<Argument> args2 = new LinkedList<Argument>();
                        for(Argument a2:children) {
                            if (a2.m_type == Argument.ARGUMENT_RULE) {
                                args2.addAll(getChallengers(a2));
                            }
                        }
                        if (args2.size()==0) {
                            args.add(b);
                        } else {
                            args.addAll(args2);
                        }
                    }
                }
            }
        }
        return args;
    }

    // Get all the arguments that if attacked, could make the root defeated:
    // They are returned in a leaves-to-root order (i.e. leaves first, and root node last)
    public List<Argument> getDefenders(List<ArgumentationAgent> agents) {
        return getDefenders(m_root,agents);
    }

    public List<Argument> getDefenders(Argument a, List<ArgumentationAgent> agents) {
        List<Argument> args = new LinkedList<Argument>();
        if (!defeatedP(a) || !settledP(a,agents)) {
            List<Argument> children = m_children.get(a);
            if (children==null) {
                args.add(a);
                return args;
            }
            for(Argument b:children) {
                if (b.m_type == Argument.ARGUMENT_RULE && defeatedP(b)) {
                    List<Argument> args2 = new LinkedList<Argument>();
                    for(Argument a2:m_children.get(b)) {
                        if (a2.m_type == Argument.ARGUMENT_RULE) {
                            args2.addAll(getDefenders(a2, agents));
                        }
                    }
                    args.addAll(args2);
                }
            }
            args.add(a);
        }
        return args;
    }

    
    // I've removed the concept of "settled arguments"
    // TODO: remove the function altogether, and all the things that refer to it
    public void settle(Argument a,String agent) {
        /*
        if (m_arguments.contains(a)) {
            List<String> l = m_partially_settled.get(a);
            if (l==null) {
                l = new LinkedList<String>();
                m_partially_settled.put(a,l);
            }
            l.add(agent);
        }
        * 
        */
    }

    public void unSettle(Argument a, String agent) {
        List<String> l = m_partially_settled.get(a);
        if (!a.m_agent.equals(agent) && l!=null && l.contains(agent)) {
            l.remove(agent);
            Argument p = m_parent.get(a);
            if (p!=null) unSettle(p,agent);
        }
    }

    public void unSettle() {
        m_partially_settled.clear();
    }

    public boolean settledP(Argument a,List<ArgumentationAgent> agents) {
        List<String> l = m_partially_settled.get(a);
        for(ArgumentationAgent agent:agents) {
            if (!agent.m_name.equals(a.m_agent) &&
                (l==null || !l.contains(agent.m_name))) {
                return false;
            }
        }
        return true;
    }


    public boolean settledP(Argument a,String m_agent_name) {
        List<String> l = m_partially_settled.get(a);
        if (!m_agent_name.equals(a.m_agent) &&
            (l==null || !l.contains(m_agent_name))) {
            return false;
        }
        return true;
    }

    public boolean settledP(Argument a) {
        List<String> l = m_partially_settled.get(a);
        if (l==null || l.isEmpty()) {
            return false;
        }
        return true;
    }

    
    public boolean retractUnacceptable(String agent, ArgumentAcceptability aa) throws FeatureTermException {
        List<Argument> toDelete = new LinkedList<Argument>();
        boolean retractedRoot = false;
        for(Argument a:m_arguments) {
            if (a.m_agent!=null && a.m_agent.equals(agent) && !aa.accepted(a)) toDelete.add(a);
        }

        for(Argument a:toDelete) {
            if (a==m_root) {
                retractedRoot = true;
                retractArgument(a);
            } else {
                retractArgument(a);
            }
        }
        return retractedRoot;
    }

    void retractArgument(Argument a){
        List<Argument> children = m_children.get(a);

        {
            Argument parent = m_parent.get(a);
            List<Argument> l = m_retracted.get(parent);
            if (l==null) {
                l = new LinkedList<Argument>();
                m_retracted.put(parent, l);
            }
            l.add(a);
        }

        if (children!=null) {
            List<Argument> la = new LinkedList<Argument>();
            la.addAll(children);
            for(Argument a1:la) retractArgument(a1);
            m_children.remove(a);
        }

        Argument p = m_parent.get(a);
        if (p!=null) {
            m_children.get(p).remove(a);
            unSettle(p,a.m_agent);
        }
        m_parent.remove(a);
        m_arguments.remove(a);
        m_partially_settled.remove(a);
    }

    public Set<Argument> getArgumentsWithRetractedChildren() {
        return m_retracted.keySet();
    }

    public List<Argument> getRetractedChildren(Argument a) {
        return m_retracted.get(a);
    }

    public int getDepth(Argument a) {
        if (!m_arguments.contains(a)) return -1;

        int d = 0;
        while(a!=m_root && a!=null) {
            a = m_parent.get(a);
            d++;
        }
        return d;
    }

    public Argument getParent(Argument a) {
        Argument ret = m_parent.get(a);
        if (ret==null) {
            if (a==m_root) return null;
            for(Argument a2:m_retracted.keySet()) {
                if (m_retracted.get(a2).contains(a)) return a2;
            }
        }
        return ret;
    }

    public List<Argument> getChildren(Argument a) {
        return m_children.get(a);
    }


    public String toString(List<ArgumentationAgent> agents) {
        return toString(m_root,0, agents);
    }

    
    public String toString(Argument a, int tabs, List<ArgumentationAgent> agents) {
        String tmp = "";
        for(int i = 0;i<tabs;i++) tmp+="  ";
        tmp+=a;
        if (defeatedP(a)) tmp+=" (defeated) ";
        if (settledP(a,agents)) tmp+=" (settled) ";
        tmp+="\n";
        List<Argument> children = m_children.get(a);
        if (children==null) return tmp;
        for(Argument a2:children) {
            if (m_parent.get(a2)!=a) {
                System.err.println("Inconsistency in the tree!!!! " + a.m_ID + " is not the parent of " + a2.m_ID);
            }
            tmp+=toString(a2,tabs+1,agents);
        }
        return tmp;
    }

}
