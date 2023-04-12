package com.mtv.app;

import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.ast.ASTFactory;
import com.mtv.encode.cfg.build.ControlFlowGraph;
import com.mtv.encode.constraint.ConstraintManager;
import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderGraphBuilder;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.io.IOException;
import java.util.HashMap;

public class Main {
    static String p1Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1b.c";
    static String p2Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\test\\simple.c";
    static String p3Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-1.c"; // Result: SATISFIABLE
    static String p4Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-1.c"; // Result: UNSATISFIABLE with n = 20
    static String p5Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-1.c"; // Result: UNSATISFIABLE
    public static void main(String[] args) throws Exception {
        Verify(p2Path);
    }

    private static void Verify(String filePath) throws Exception {
        ASTFactory ast = new ASTFactory(filePath);
        if (ast.getMain() == null) {
            DebugHelper.print("Main function is not detected. Abort.");
            return;
        }
        IASTFunctionDefinition func = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(func, ast, true);
        cfg.printGraph();
        EventOrderGraph eog = EventOrderGraphBuilder.Build(cfg, new HashMap<>(), false);
        eog.printEOG();
        Solver solver = ConstraintManager.BuildConstraints(eog, ast);

        /*Context ctx = ConstraintManager.ctx;*/

        /*if (filePath.equals(p1Path)) {

        } else if (filePath.equals(p2Path)){
            solver.add(ctx.mkAnd(ctx.mkEq(ctx.mkIntConst("m_1"), ctx.mkInt(2)), ctx.mkEq(ctx.mkIntConst("n_1"), ctx.mkInt(3))));
        } else if (filePath.equals(p3Path)) {
            solver.add(ctx.mkAnd(ctx.mkEq(ctx.mkIntConst("cond1_main_0"), ctx.mkInt(16)), ctx.mkEq(ctx.mkIntConst("cond2_main_0"), ctx.mkInt(5))));
        } else if (filePath.equals(p4Path)) {
            solver.add(ctx.mkGt(ctx.mkIntConst("check_x_0"), ctx.mkIntConst("n_0")));
        } else if (filePath.equals(p5Path)) {
            solver.add(ctx.mkOr(ctx.mkGt(ctx.mkIntConst("i_5"), ctx.mkInt(16)), ctx.mkGt(ctx.mkIntConst("j_10"), ctx.mkInt(16))));
        }*/

        System.out.println("Start solve");
        if (solver.check() == Status.SATISFIABLE) {

            Model model = solver.getModel();
            System.out.println(model);
        } else {
            System.out.println("UNSATISFIABLE");
        }
        System.out.println("End solve");
    }
}
