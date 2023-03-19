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
    static String p3Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-1.c";
    static String p4Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-1.c";
    static String p5Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-1.c";
    public static void main(String[] args) throws IOException {
        ASTFactory ast = new ASTFactory(p2Path);
        if (ast.getMain() == null) {
            DebugHelper.print("Main function is not detected. Abort.");
            return;
        }
        IASTFunctionDefinition func = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(func, ast, true);
        cfg.printGraph();
        EventOrderGraph eog = EventOrderGraphBuilder.Build(cfg, new HashMap<>(), false);
        eog.printEOG();
        Solver solver = ConstraintManager.BuildConstraints(eog);

        if (solver.check() == Status.SATISFIABLE) {
            Model model = solver.getModel();
            System.out.println(model);
        } else {
            System.out.println("UNSATIFIABLE");
        }
    }
}
