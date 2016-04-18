package jayhorn.solver.spacer;

import com.microsoft.z3.*;

import java.util.HashMap;

/**
 * Created by teme
 */
public class SpacerTest {

    public static void main(String[] args) throws Z3Exception {
    	HashMap<String, String> cfg = new HashMap<String, String>();
        Context ctx = new Context(cfg);
        Fixedpoint fp = ctx.mkFixedpoint();
    	Params params = ctx.mkParams();
		params.add("engine", "spacer");
		params.add("xform.slice", false);
		params.add("use_heavy_mev", true);
		params.add("reset_obligation_queue", true);
		params.add("pdr.flexible_trace", false);
		params.add("spacer.elim_aux", false);
		params.add("xform.inline-linear", false);
		params.add("xform.inline-eager", false);
		params.add("pdr.utvpi", false);
		params.add("print_statistics",true);
		fp.setParameters(params);
       
        FuncDecl a = ctx.mkConstDecl("a", ctx.mkBoolSort());
        fp.registerRelation(a);
        ctx.mkConst("x", ctx.mkBoolSort());
        FuncDecl b = ctx.mkConstDecl("b", ctx.mkBoolSort());
        fp.registerRelation(b);
        FuncDecl c = ctx.mkConstDecl("c", ctx.mkBoolSort());
        fp.registerRelation(c);

        fp.addRule(
                ctx.mkImplies(
                        (BoolExpr) b.apply(),
                        (BoolExpr) a.apply()
                ),
                null
        );

        fp.addRule(
                ctx.mkImplies(
                        (BoolExpr) c.apply(),
                        (BoolExpr) b.apply()
                ),
                null
        );

        System.out.println(fp);
     
        Status ans = fp.query((BoolExpr) a.apply());
        System.out.println(ans);
        //System.out.println(fp.getAnswer());

    System.out.println("====================");

    fp.addRule((BoolExpr) c.apply(), null);
    System.out.println(fp);


    ans = fp.query((BoolExpr) a.apply());
    System.out.println(ans);
    System.out.println(fp.getAnswer());


//    BoolSort b = ctx.getBoolSort();
//    BoolExpr v = (BoolExpr) ctx.mkBound(1, b);
////    BoolExpr v2 = (BoolExpr) ctx.mkBound(1, b);
//    FuncDecl p = ctx.mkFuncDecl("p", new Sort[]{b, b}, b);
//    FuncDecl p2 = ctx.mkFuncDecl("p2", new Sort[]{b, b}, b);
//    Fixedpoint fp = ctx.mkFixedpoint();
//    fp.setPredicateRepresentation(p, new Symbol[] {ctx.mkSymbol("interval_relation")});
//    fp.registerRelation(p);
//    fp.registerRelation(p2);
//    fp.addRule(ctx.mkImplies((BoolExpr) p.apply(v, v), (BoolExpr) p2.apply(v, v)), null);
//    int[] array = new int[2];
//    array[0] = 1;
//    array[1] = 0;
//    fp.addFact(p, array);
//    System.out.println(fp);

//    Status a = fp.query((BoolExpr) p2.apply(ctx.mkBool(true), ctx.mkBool(true)));
//    System.out.println(a);
//    System.out.println(fp.getAnswer());
    }
}
