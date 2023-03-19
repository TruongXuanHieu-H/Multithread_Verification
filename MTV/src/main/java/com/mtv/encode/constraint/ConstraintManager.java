package com.mtv.encode.constraint;

import com.microsoft.z3.*;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.eog.*;
import jxl.demo.Write;
import org.eclipse.cdt.core.dom.ast.*;
import org.javatuples.Triplet;

import java.util.ArrayList;

public class ConstraintManager {
    public static Context ctx = new Context();
    public static Solver solver = ctx.mkSolver();

    public static Solver BuildConstraints(EventOrderGraph eog) {
        if (eog.startNode == null) {
            return null;
        }

        /*
        Constraints are separated into 3 parts:
        - Write constraints
        - Read/Write link constraints
        - Order constraints
         */

        WriteConstraintsManager.CreateWriteConstraints(ctx, solver, eog);
        ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures = RWLConstraintsManager.CreateRWLC_ProgramFromProgram(ctx, solver, eog);

        OrderConstraintsManager.CreateOrderConstraints(ctx, solver, RWLSignatures, eog);
        solver.add(ctx.mkEq(ctx.mkIntConst("m_3"), ctx.mkInt(1)));
        solver.add(ctx.mkEq(ctx.mkIntConst("n_3"), ctx.mkInt(1)));

        PrintAssertions(solver);
        return solver;
    }

    public static void PrintAssertions(Solver solver) {
        for (BoolExpr boolExpr: solver.getAssertions()) {
            System.out.println(boolExpr.toString());
        }
    }


}
