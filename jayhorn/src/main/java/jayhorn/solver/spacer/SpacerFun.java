package jayhorn.solver.spacer;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;

import jayhorn.solver.BoolType;
import jayhorn.solver.ProverExpr;
import jayhorn.solver.ProverFun;
import jayhorn.solver.ProverType;


class SpacerFun implements ProverFun {

	private final FuncDecl fun;
	private final ProverType resType;
	private final Context ctx;

	public SpacerFun(FuncDecl fun, Context ctx, ProverType resType) {
		this.fun = fun;
		this.resType = resType;
		this.ctx = ctx;
		
	}

	public FuncDecl getFun() {
		return this.fun;
	}
	
	public ProverExpr mkExpr(ProverExpr[] args) {
		final Expr[] z3args = new Expr[args.length];
		for (int i=0; i<args.length; i++) {
			if (args[i] instanceof SpacerTermExpr) {
				z3args[i]= ((SpacerTermExpr)args[i]).getExpr();
			} else if (args[i] instanceof SpacerBoolExpr) {
				z3args[i]= ((SpacerBoolExpr)args[i]).getExpr();	
			}			
		}
		
		try {
			if (this.resType == BoolType.INSTANCE) {
				System.out.println("Here");
				System.out.println(this.fun);
				System.out.println(z3args);
				System.out.println(ctx.toString());
				if (z3args == null){ System.out.println("isnull");}
				SpacerBoolExpr spbool = new SpacerBoolExpr((BoolExpr) ctx.mkApp(this.fun, z3args));
				System.out.println(spbool.toString());
				return new SpacerBoolExpr((BoolExpr) ctx.mkApp(this.fun, z3args));
			} else {
				System.out.println("There");
				return new SpacerTermExpr(ctx.mkApp(this.fun, z3args), this.resType);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public String toString() {
		return fun.toString();
	}

}
