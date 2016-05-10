/**
 * 
 */
package jayhorn.checker;

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
import java.util.concurrent.TimeUnit;


import java.util.Set;

import jayhorn.Log;
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
import soottocfg.cfg.Variable;
import soottocfg.cfg.expression.BinaryExpression;
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

import jayhorn.hornify.*;

/**
 * @author schaef
 *
 */
public class Checker {

	

	public boolean checkProgram(Program program) {
//		Log.info("Starting verification for " + program.getEntryPoints().length + " entry points.");
//
//		Prover p = factory.spawn();
//		p.setHornLogic(true);
//		ProverResult result = ProverResult.Unknown;
//
//		try {
//			
//			for (Method method : program.getEntryPoints()) {
//				Log.info("\tVerification from entry " + method.getMethodName());
//                
//				if (factory.getProver().equals("princess")){
//					p.push();
//				}
//					
//				for (ProverHornClause clause : clauses)
//					p.addAssertion(clause);
//
//
//				// add an entry clause from the preconditions
//				final HornPredicate entryPred = methodContracts.get(method.getMethodName()).precondition;
//				final List<ProverExpr> entryVars = new ArrayList<ProverExpr>();
//				final Map<Variable, ProverExpr> varMap = new HashMap<Variable, ProverExpr>();
//				createVarMap(p, entryPred.variables, entryVars, varMap);
//
//				final ProverExpr entryAtom = entryPred.predicate.mkExpr(entryVars.toArray(new ProverExpr[0]));
//
//				p.addAssertion(p.mkHornClause(entryAtom, new ProverExpr[0], p.mkLiteral(true)));
//
//				if (jayhorn.Options.v().getTimeout() > 0) {
//					int timeoutInMsec = (int)TimeUnit.SECONDS.toMillis(jayhorn.Options.v().getTimeout());
//					p.checkSat(false);
//					result = p.getResult(timeoutInMsec);
//				} else {
//					result = p.checkSat(true);	
//				}
//
//				if (factory.getProver().equals("princess")){
//					p.pop();
//				}
//			}
//		} catch (Throwable t) {
//			t.printStackTrace();
//			throw new RuntimeException(t);
//		} finally {
//			p.shutdown();
//		}
//		Log.info("\tResult:  " + result);
//		if (result==ProverResult.Sat) {
//			return true;
//		} else if (result==ProverResult.Unsat) {
//			return false;
//		}
//		throw new RuntimeException("Verification failed with prover code " + result);
		return true;
	}

	
}
