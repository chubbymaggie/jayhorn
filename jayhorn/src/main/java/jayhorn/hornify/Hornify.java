/**
 * 
 */
package jayhorn.hornify;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Verify;

import jayhorn.Log;
import jayhorn.Options;
import jayhorn.solver.Prover;
import jayhorn.solver.ProverExpr;
import jayhorn.solver.ProverFactory;
import jayhorn.solver.ProverFun;
import jayhorn.solver.ProverHornClause;
import jayhorn.solver.ProverResult;
import jayhorn.solver.ProverType;
import soottocfg.cfg.ClassVariable;
import soottocfg.cfg.LiveVars;
import soottocfg.cfg.Program;
import soottocfg.cfg.SourceLocation;
import soottocfg.cfg.Variable;
import soottocfg.cfg.expression.BinaryExpression;
import soottocfg.cfg.expression.BinaryExpression.BinaryOperator;
import soottocfg.cfg.expression.BooleanLiteral;
import soottocfg.cfg.expression.Expression;
import soottocfg.cfg.expression.IdentifierExpression;
import soottocfg.cfg.expression.IntegerLiteral;
import soottocfg.cfg.expression.IteExpression;
import soottocfg.cfg.expression.UnaryExpression;
import soottocfg.cfg.method.CfgBlock;
import soottocfg.cfg.method.CfgEdge;
import soottocfg.cfg.method.Method;
import soottocfg.cfg.statement.AssertStatement;
import soottocfg.cfg.statement.AssignStatement;
import soottocfg.cfg.statement.AssumeStatement;
import soottocfg.cfg.statement.CallStatement;
import soottocfg.cfg.statement.PackStatement;
import soottocfg.cfg.statement.Statement;
import soottocfg.cfg.statement.UnPackStatement;
import soottocfg.cfg.type.BoolType;
import soottocfg.cfg.type.IntType;
import soottocfg.cfg.type.MapType;
import soottocfg.cfg.type.ReferenceType;
import soottocfg.cfg.type.Type;
import soottocfg.cfg.util.GraphUtil;

/**
 * @author teme
 *
 */
public class Hornify {

	private Map<ClassVariable, Integer> typeIds = new LinkedHashMap<ClassVariable, Integer>();
	private Map<String, MethodContract> methodContracts = new LinkedHashMap<String, MethodContract>();

	private Prover prover;
	
	public Hornify(ProverFactory fac) {
		this.prover = fac.spawn();
		
	}

	public void toHorn(Program program){
		
		Log.info("Building type hierarchy ... ");

		ClassType cType = new ClassType();
		
		for (ClassVariable var : program.getTypeGraph().vertexSet())
			cType.addClassVar(var, typeIds.size());
		
		Log.info("Generating Method Contract ... ");
		
		MethodEncoder mEncoder = new MethodEncoder(this.prover, program);
				
		for (Method method : program.getMethods()) {
			final List<Variable> inParams = new ArrayList<Variable>();
			inParams.addAll(method.getInParams());
			final List<Variable> postParams = new ArrayList<Variable>();
			postParams.addAll(method.getInParams());
			if (method.getOutParam().isPresent()) {
				postParams.add(method.getOutParam().get());
			} else if (method.getReturnType().isPresent()) {
				postParams.add(new Variable ("resultVar", method.getReturnType().get()));
			}

			final ProverFun prePred = mEncoder.freshHornPredicate(this.prover, method.getMethodName() + "_pre", inParams);
			final ProverFun postPred = mEncoder.freshHornPredicate(this.prover, method.getMethodName() + "_post", postParams);

			Log.debug("method: " + method.getMethodName());
			Log.debug("pre: " + inParams);
			Log.debug("post: " + postParams);

			final HornPredicate pre = new HornPredicate(method.getMethodName() + "_pre", inParams, prePred);
			final HornPredicate post = new HornPredicate(method.getMethodName() + "_post", postParams, postPred);

			methodContracts.put(method.getMethodName(), new MethodContract(method, pre, post));
		}
		
		Log.info("Compile Methods as Horn Clauses ... ");

		List<ProverHornClause> clauses = new LinkedList<ProverHornClause>();
		
		for (Method method : program.getMethods()) {
			mEncoder.encode(method, cType);
			clauses.addAll(mEncoder.clauses);
            Log.info("\tEncoding : " + method.getMethodName());
			Log.info("\tNumber of clauses:  " + mEncoder.clauses.size());
			for (ProverHornClause clause : mEncoder.clauses)
				Log.info("\t\t" + clause);
		}

	}

}
