package com.mtv.app;

import com.microsoft.z3.*;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.ast.ASTFactory;
import com.mtv.encode.cfg.build.ControlFlowGraph;
import com.mtv.encode.constraint.ConstraintManager;
import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderGraphBuilder;
import com.mtv.output.ExcellReporter;
import com.mtv.output.ExportToExcell;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class Main {
    static String p1Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1b.c";
    static String p2Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1.c";
    static String p3Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-1.c"; // Result: SATISFIABLE
    static String p4Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-2.c"; // Result: SATISFIABLE
    static String p5Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-1.c"; // Result: UNSATISFIABLE with n = 20

    static String p6Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-2.c";
    static String p7Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-1.c"; // Result: UNSATISFIABLE
    static String p8Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-2.c";
    static String p9Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longer-1.c";
    static String p10Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longer-2.c";
    static String p11Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longest-1.c";
    static String p12Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longest-2.c";


    static ArrayList<String> paths = new ArrayList<>(Arrays.asList(
//            p1Path, p2Path, p3Path, p4Path, p5Path, p6Path, p7Path, p8Path, p9Path, p10Path, p11Path, p12Path
    ));

    private Integer timeOut = 3600000;

    public void Run() throws Exception {
        ArrayList<ExcellReporter> reporters = new ArrayList<>();
        for (String filePath : paths) {
            Context ctx = new Context();
            Solver solver = ctx.mkSolver();
            Params timeOutParam = ctx.mkParams();
            timeOutParam.add("timeout", timeOut);
            solver.setParameters(timeOutParam);
            reporters.add(Verify(filePath, ctx, solver));
            solver.reset();
            ctx.close();
        }
        DebugHelper.print("Export to excell");
        ExportToExcell.Export(reporters);
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.Run();
    }

    public static ExcellReporter Verify(String filePath, Context ctx, Solver solver) throws Exception {
        ASTFactory ast = new ASTFactory(filePath);
        if (ast.getMain() == null) {
            DebugHelper.print("Main function of " + filePath + "is not detected. Abort.");
            return null;
        }
        IASTFunctionDefinition func = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(func, ast, true);
        //cfg.printGraph();

        EventOrderGraph eog = EventOrderGraphBuilder.Build(cfg, new HashMap<>(), false);
        //eog.printEOG();

        long startBuildConstraints = System.currentTimeMillis();
        ConstraintManager constraintManager = new ConstraintManager(ctx, solver);
        constraintManager.BuildConstraints(eog, ast);
        long endBuildConstraints = System.currentTimeMillis();

        long statSolveConstraints = System.currentTimeMillis();
        String verificationResult = "UNKNOWN";
        System.out.println("Start solve");
        if (solver.check() == Status.SATISFIABLE) {
            verificationResult = "SATISFIABLE";
            Model model = solver.getModel();
            System.out.println(model);
        } else {
            verificationResult = "UNSATISFIABLE";
            System.out.println("UNSATISFIABLE");
        }
        System.out.println("End solve");
        long endSolveConstraints = System.currentTimeMillis();

        ExcellReporter reporter = new ExcellReporter(filePath.substring(filePath.lastIndexOf("\\") + 1), LocalDateTime.now(), new ArrayList<String>(Files.readAllLines(Paths.get(filePath))),
                verificationResult, solver.getAssertions().length, endBuildConstraints - startBuildConstraints, endSolveConstraints - statSolveConstraints);
        return reporter;
    }
}
