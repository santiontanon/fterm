/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.exceptions;

/**
 *
 * @author santi
 */
public class SingletonFeatureTermException extends FeatureTermException {
	public SingletonFeatureTermException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
