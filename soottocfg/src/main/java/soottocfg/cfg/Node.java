/**
 * 
 */
package soottocfg.cfg;

import java.util.Set;

/**
 * @author schaef
 *
 */
public interface Node {

	public Set<Variable> getUseVariables();

	public Set<Variable> getDefVariables();
}
