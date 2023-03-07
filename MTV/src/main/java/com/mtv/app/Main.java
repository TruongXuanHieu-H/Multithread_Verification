package com.mtv.app;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.ast.ASTFactory;
import com.mtv.graph.cfg.build.ControlFlowGraph;
import com.mtv.graph.eog.EventOrderGraph;
import com.mtv.graph.eog.EventOrderGraphBuilder;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.io.IOException;
import java.util.HashMap;

public class Main {
    static String p1Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1b.c";
    static String p2Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\test\\simple.c";
    static String p3Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-1.c";
    static String p4Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-1.c";
    public static void main(String[] args) throws IOException {
        ASTFactory ast = new ASTFactory(p4Path);
        if (ast.getMain() == null) {
            DebugHelper.print("Main function is not detected. Abort.");
            return;
        }
        IASTFunctionDefinition func = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(func, ast, true);
        cfg.unfold();
        cfg.printGraph();
        EventOrderGraph eog = EventOrderGraphBuilder.Build(cfg, new HashMap<>(), false);
        eog.printEOG();
    }
}
