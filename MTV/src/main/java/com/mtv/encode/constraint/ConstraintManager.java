package com.mtv.encode.constraint;

import com.microsoft.z3.*;
public class ConstraintManager {
    public static void main(String[] args) {
        Context ctx = new Context();
        Solver solver = ctx.mkSolver();

        // Create two integer constants
        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");

        BoolExpr b1 = ctx.mkBoolConst("a");
        BoolExpr b2 = ctx.mkBoolConst("b");
        // Create an assertion
        BoolExpr assertion = ctx.mkAnd(ctx.mkEq(b1, ctx.mkBool(true)), ctx.mkEq(b2, b1), ctx.mkEq(b2, ctx.mkBool(false)));

        // Add the assertion to the solver
        solver.add(assertion);

        // Check if the assertion is satisfiable
        if (solver.check() == Status.SATISFIABLE) {
            // Print a satisfying assignment for x and y
            Model model = solver.getModel();
            System.out.println("x = " + model.eval(x, false) + ", y = " + model.eval(y, false));
        } else {
            System.out.println("Unsatisfiable!");
        }

        // Clean up resources
        solver.reset();
        ctx.close();
    }
}
