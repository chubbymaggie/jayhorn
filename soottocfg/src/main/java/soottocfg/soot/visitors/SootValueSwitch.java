/*
 * jimple2boogie - Translates Jimple (or Java) Programs to Boogie
 * Copyright (C) 2013 Martin Schaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaeaef and Stephan Arlt
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package soottocfg.soot.visitors;

import java.util.LinkedList;
import java.util.List;

import soot.ArrayType;
import soot.Local;
import soot.NullType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MethodHandle;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;
import soottocfg.cfg.ClassVariable;
import soottocfg.cfg.SourceLocation;
import soottocfg.cfg.expression.ArrayLengthExpression;
import soottocfg.cfg.expression.BinaryExpression;
import soottocfg.cfg.expression.BinaryExpression.BinaryOperator;
import soottocfg.cfg.expression.BooleanLiteral;
import soottocfg.cfg.expression.Expression;
import soottocfg.cfg.expression.IdentifierExpression;
import soottocfg.cfg.expression.IntegerLiteral;
import soottocfg.cfg.expression.IteExpression;
import soottocfg.cfg.expression.UnaryExpression;
import soottocfg.cfg.expression.UnaryExpression.UnaryOperator;
import soottocfg.soot.memory_model.MemoryModel;
import soottocfg.soot.util.MethodInfo;
import soottocfg.soot.util.SootTranslationHelpers;

/**
 * @author schaef
 */
public class SootValueSwitch implements JimpleValueSwitch {

	private final List<Expression> expressionStack = new LinkedList<Expression>();
	private final SootStmtSwitch statementSwitch;
	private final MethodInfo methodInfo;
	private final MemoryModel memoryModel;

	// private boolean isLeftHandSide = false;

	public SootValueSwitch(SootStmtSwitch ss) {
		this.statementSwitch = ss;
		this.memoryModel = SootTranslationHelpers.v().getMemoryModel();
		this.memoryModel.setStmtSwitch(this.statementSwitch);
		this.memoryModel.setValueSwitch(this);
		this.methodInfo = this.statementSwitch.getMethodInto();
	}

	public Expression popExpression() {
		return this.expressionStack.remove(this.expressionStack.size() - 1);
	}

	protected void translateBinOp(BinopExpr arg0) {
		// this.isLeftHandSide = false;
		arg0.getOp1().apply(this);
		Expression lhs = popExpression();
		arg0.getOp2().apply(this);
		Expression rhs = popExpression();

		String op = arg0.getSymbol().trim();
		if (op.compareTo("+") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Plus, lhs, rhs));
			return;
		} else if (op.compareTo("-") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Minus, lhs, rhs));
			return;
		} else if (op.compareTo("*") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Mul, lhs, rhs));
			return;
		} else if (op.compareTo("/") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Div, lhs, rhs));
			return;
		} else if (op.compareTo("%") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Mod, lhs, rhs));
			return;
		} else if (op.compareTo("cmp") == 0 || op.compareTo("cmpl") == 0 || op.compareTo("cmpg") == 0) {
			/*
			 * Returns 0 if lhs==rhs -1 if lhs <rhs 1 if lhs >rhs We model that
			 * using ITE expressions as: (lhs<=rhs)?((lhs==rhs)?0:-1):1
			 */
			Expression ite = new IteExpression(statementSwitch.getCurrentLoc(),
					new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Le, lhs, rhs),
					new IteExpression(statementSwitch.getCurrentLoc(),
							new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Eq, lhs, rhs),
							IntegerLiteral.zero(), new UnaryExpression(statementSwitch.getCurrentLoc(),
									UnaryOperator.Neg, IntegerLiteral.one())),
					IntegerLiteral.one());
			this.expressionStack.add(ite);
			return;
		} else if (op.compareTo("==") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Eq, lhs, rhs));
			return;
		} else if (op.compareTo("<") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Lt, lhs, rhs));
			return;
		} else if (op.compareTo(">") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Gt, lhs, rhs));
			return;
		} else if (op.compareTo("<=") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Le, lhs, rhs));
			return;
		} else if (op.compareTo(">=") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Ge, lhs, rhs));
			return;
		} else if (op.compareTo("!=") == 0) {
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Ne, lhs, rhs));
			return;
		} else if (op.compareTo("&") == 0) { // bit-and
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.BAnd, lhs, rhs));
			return;
		} else if (op.compareTo("|") == 0) { // bit-or
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.BOr, lhs, rhs));
			return;
		} else if (op.compareTo("<<") == 0) { // Shiftl
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Shl, lhs, rhs));
			return;
		} else if (op.compareTo(">>") == 0) { // Shiftr
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Shr, lhs, rhs));
			return;
		} else if (op.compareTo(">>>") == 0) { // UShiftr
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Ushr, lhs, rhs));
			return;
		} else if (op.compareTo("^") == 0) { // XOR
			this.expressionStack
					.add(new BinaryExpression(statementSwitch.getCurrentLoc(), BinaryOperator.Xor, lhs, rhs));
			return;
		} else {
			throw new RuntimeException("UNKNOWN Jimple operator " + op);
		}

	}

	@Override
	public void caseClassConstant(ClassConstant arg0) {
		this.expressionStack.add(new IdentifierExpression(statementSwitch.getCurrentLoc(),this.memoryModel.lookupClassVariable(arg0)));
	}

	@Override
	public void caseDoubleConstant(DoubleConstant arg0) {
		this.expressionStack.add(this.memoryModel.mkDoubleConstant(arg0));

	}

	@Override
	public void caseFloatConstant(FloatConstant arg0) {
		this.expressionStack.add(this.memoryModel.mkFloatConstant(arg0));
	}

	@Override
	public void caseIntConstant(IntConstant arg0) {
		this.expressionStack.add(new IntegerLiteral(statementSwitch.getCurrentLoc(), arg0.value));

	}

	@Override
	public void caseLongConstant(LongConstant arg0) {
		this.expressionStack.add(new IntegerLiteral(statementSwitch.getCurrentLoc(), arg0.value));
	}

	@Override
	public void caseMethodHandle(MethodHandle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void caseNullConstant(NullConstant arg0) {
		this.expressionStack.add(this.memoryModel.mkNullConstant());
	}

	@Override
	public void caseStringConstant(StringConstant arg0) {
		this.expressionStack.add(this.memoryModel.mkStringConstant(arg0));
	}

	@Override
	public void defaultCase(Object arg0) {
		throw new RuntimeException("Not implemented " + arg0);
	}

	@Override
	public void caseAddExpr(AddExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseAndExpr(AndExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseCastExpr(CastExpr arg0) {
		// TODO this assumes that we have introduced explicit updates to the type field.
		arg0.getOp().apply(this);
//		expressionStack.add(new IdentifierExpression(statementSwitch.getCurrentLoc(), fresh));
	}

	@Override
	public void caseCmpExpr(CmpExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseCmpgExpr(CmpgExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseCmplExpr(CmplExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseDivExpr(DivExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr arg0) {
		throw new RuntimeException("Not implemented here.");
	}

	@Override
	public void caseEqExpr(EqExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseGeExpr(GeExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseGtExpr(GtExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr arg0) {
		SourceLocation loc = this.statementSwitch.getCurrentLoc();		
		Value left = arg0.getOp();
		soot.Type t = left.getType();
		SootField typeField = null;
		if (t instanceof RefType) {			
			//first make a heap-read of the type filed.
			typeField = ((RefType)t).getSootClass().getFieldByName(SootTranslationHelpers.typeFieldName);
		} else if (t instanceof ArrayType) {			
			typeField = Scene.v().getSootClass("java.lang.Object").getFieldByName(SootTranslationHelpers.typeFieldName);
		} else if (t instanceof NullType) {
			//TODO: Warn?
			this.expressionStack.add(BooleanLiteral.falseLiteral());
			return;
		} else if (t instanceof PrimType) {
			if (arg0.getCheckType() instanceof PrimType) {
				throw new RuntimeException("Not implemented. "+ arg0 + ", "+t.getClass());
			} else {
				//TODO: Warn?
				this.expressionStack.add(BooleanLiteral.falseLiteral());
				return;				
			}
		} else {			
			throw new RuntimeException("Not implemented. "+ arg0 + ", "+t.getClass());
		}
		final String localName = "$tmp"+this.statementSwitch.getMethod().getActiveBody().getLocals().size();
		Local freshLocal = Jimple.v().newLocal(localName, typeField.getType());
		this.statementSwitch.getMethod().getActiveBody().getLocals().add(freshLocal);
		FieldRef fieldRef = Jimple.v().newInstanceFieldRef(left, typeField.makeRef());
		memoryModel.mkHeapReadStatement(this.statementSwitch.getCurrentStmt(), fieldRef, freshLocal);			
		freshLocal.apply(this);
		Expression exp = this.popExpression();
		
		//now make the bla <: blub expression			
		ClassVariable cv = this.memoryModel.lookupClassVariable(SootTranslationHelpers.v().getClassConstant(arg0.getCheckType()));
		BinaryExpression instof = new BinaryExpression(loc, BinaryOperator.PoLeq, exp, new IdentifierExpression(loc, cv));
		this.expressionStack.add(instof);

	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr arg0) {
		throw new RuntimeException("Not implemented here.");
	}

	@Override
	public void caseLeExpr(LeExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseLengthExpr(LengthExpr arg0) {
		//TODO: remove
		arg0.getOp().apply(this);
		Expression inner = this.popExpression();
		this.expressionStack.add(new ArrayLengthExpression(statementSwitch.getCurrentLoc(), inner));
	}

	@Override
	public void caseLtExpr(LtExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseMulExpr(MulExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseNeExpr(NeExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseNegExpr(NegExpr arg0) {
		arg0.getOp().apply(this);
		Expression expr = popExpression();
		this.expressionStack
				.add(new UnaryExpression(statementSwitch.getCurrentLoc(), UnaryOperator.Neg /* LNot */, expr));
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr arg0) {
		this.expressionStack.add(this.memoryModel.mkNewArrayExpr(arg0));
	}

	@Override
	public void caseNewExpr(NewExpr arg0) {
		this.expressionStack.add(this.memoryModel.mkNewExpr(arg0));
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr arg0) {
		this.expressionStack.add(this.memoryModel.mkNewMultiArrayExpr(arg0));
	}

	@Override
	public void caseOrExpr(OrExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseRemExpr(RemExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseShlExpr(ShlExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseShrExpr(ShrExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr arg0) {
		throw new RuntimeException("Not implemented here.");
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr arg0) {
		throw new RuntimeException("Not implemented here.");
	}

	@Override
	public void caseSubExpr(SubExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseUshrExpr(UshrExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr arg0) {
		throw new RuntimeException("Not implemented here.");
	}

	@Override
	public void caseXorExpr(XorExpr arg0) {
		translateBinOp(arg0);
	}

	@Override
	public void caseArrayRef(ArrayRef arg0) {
//		this.expressionStack.add(this.memoryModel.mkArrayRefExpr(arg0));
		throw new RuntimeException("Should be handled in statement switch.");
	}

	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef arg0) {
		// This should have been eliminated by the exception translation.
		throw new UnsupportedOperationException("CaughtExceptionRef should have been eliminated earlier! This is a bug!");
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef arg0) {
		throw new RuntimeException("must not be called");
	}

	@Override
	public void caseParameterRef(ParameterRef arg0) {
		this.expressionStack.add(methodInfo.lookupParameterRef(arg0));
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef arg0) {
		throw new RuntimeException("must not be called");
	}

	@Override
	public void caseThisRef(ThisRef arg0) {
		this.expressionStack.add(methodInfo.getThisVariable());
	}

	@Override
	public void caseLocal(Local arg0) {
		this.expressionStack.add(methodInfo.lookupLocal(arg0));
	}

}
