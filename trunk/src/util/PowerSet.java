/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class PowerSet {
    public static <T extends Object> List<List<T>> powerSet(List<T> l) {
        List<List<T>> result = new LinkedList<List<T>>();

        if (l.isEmpty()) {
            result.add(new LinkedList<T>());
            return result;
        } else {
            T element = l.remove(0);
            // half of them will have "element":
            result.addAll(powerSet(l));
            for(List<T> l2:result) {
                l2.add(element);
            }
            // half of them will not::
            result.addAll(powerSet(l));
            l.add(0, element);
        }

        return result;
    }
}
