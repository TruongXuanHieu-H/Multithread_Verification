package com.mtv.app;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.ast.ASTFactory;
import com.mtv.graph.cfg.build.ControlFlowGraph;
import com.mtv.graph.eog.EventOrderAction;
import com.mtv.graph.eog.EventOrderGraph;
import com.mtv.graph.eog.EventOrderGraphBuilder;
import com.mtv.graph.eog.EventOrderNode;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.io.IOException;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Main {
    static String p1Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1b.c";
    static String p2Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\test\\simple.c";
    public static void main(String[] args) throws IOException {
        ASTFactory ast = new ASTFactory(p2Path);
        if (ast.getMain() == null) {
            DebugHelper.print("Main function is not detected. Abort.");
            return;
        }
        IASTFunctionDefinition func = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(func, ast, true);
        cfg.unfold();
        cfg.printGraph();

        EventOrderGraphBuilder.Build(cfg);


    }
}
