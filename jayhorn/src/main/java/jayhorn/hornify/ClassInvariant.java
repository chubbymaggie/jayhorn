package jayhorn.hornify;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jayhorn.solver.Prover;
import jayhorn.solver.ProverFun;
import jayhorn.solver.ProverType;
import soottocfg.cfg.ClassVariable;
import soottocfg.cfg.Variable;
import soottocfg.cfg.type.BoolType;
import soottocfg.cfg.type.IntType;
import soottocfg.cfg.type.MapType;
import soottocfg.cfg.type.ReferenceType;
import soottocfg.cfg.type.Type;

public class ClassInvariant {
	
	Prover p;
	
	public ClassInvariant(Prover p){
		this.p = p; 
	}

	private Map<ClassVariable, ProverFun> classInvariants = new LinkedHashMap<ClassVariable, ProverFun>();
	
	/**
	 * Creates a ProverType from a Type.
	 * TODO: not fully implemented.
	 * 
	 * @param p
	 * @param t
	 * @return
	 */
	private ProverType getProverType(Type t) {
		if (t == IntType.instance()) {
			return this.p.getIntType();
		}
		if (t == BoolType.instance()) {
			return this.p.getBooleanType();
		}
		if (t instanceof ReferenceType) {
			return this.p.getIntType();
		}
		if (t instanceof MapType) {
			System.err.println("Warning: translating " + t + " as prover type int");
			return this.p.getIntType();
		}
		throw new IllegalArgumentException("don't know what to do with " + t);
	}
	
	private ProverFun genHornPredicate(String name, List<Variable> sortedVars) {
		final List<ProverType> types = new LinkedList<ProverType>();
		for (Variable v : sortedVars)
			types.add(getProverType(v.getType()));
		return this.p.mkHornPredicate(name, types.toArray(new ProverType[types.size()]));
	}

	public ProverFun getClassInvariant(ClassVariable sig) {
		ProverFun inv = classInvariants.get(sig);

		if (inv == null) {
			List<Variable> args = new ArrayList<Variable>();

			args.add(new Variable("ref", new ReferenceType(sig)));
			for (Variable v : sig.getAssociatedFields())
				args.add(v);

			inv = genHornPredicate("inv_" + sig.getName(), args);

			classInvariants.put(sig, inv);
		}

		return inv;
	}
}
