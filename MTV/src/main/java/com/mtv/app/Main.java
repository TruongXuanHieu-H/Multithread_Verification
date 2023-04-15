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
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    static String p1Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1b.c";
    static String p2Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-1.c";
    static String p3Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\read_write_lock-2.c";
    static String p4Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-1.c"; // Result: SATISFIABLE
    static String p5Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\stateful01-2.c"; // Result: SATISFIABLE
    static String p6Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-1.c"; // Result: UNSATISFIABLE with n = 20
    static String p7Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\nondet-loop-bound-2.c";
    static String p8Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-1.c"; // Result: UNSATISFIABLE
    static String p9Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-2.c";
    static String p10Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longer-1.c";
    static String p11Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longer-2.c";
    static String p12Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longest-1.c";
    static String p13Path = "D:\\KLTN\\Multithread_Verification\\MTV\\Benchmark\\sv_comp\\triangular-longest-2.c";


    static ArrayList<String> paths = new ArrayList<>(Arrays.asList(
            p1Path, p2Path, p3Path, p4Path, p5Path, p6Path, p7Path, p8Path, p9Path, p10Path, p11Path, p12Path, p13Path

    ));


    public static void main(String[] args) throws Exception {
        Integer multiplier = 3;
        Integer baseTimeOut = 300000;
        VerifyFiles(paths, multiplier * baseTimeOut);
    }
    public static void VerifyFiles(ArrayList<String> filePaths, Integer timeOut) throws Exception {
        for (String filePath : paths) {
            Context ctx = new Context();
            Solver solver = ctx.mkSolver();
            Params timeOutParam = ctx.mkParams();
            timeOutParam.add("timeout", timeOut);
            solver.setParameters(timeOutParam);

            ArrayList<ExcellReporter> reporters = new ArrayList<>();
            reporters.add(VerifyFile(filePath, ctx, solver));
            ExportToExcell.Export(reporters, timeOut);
            reporters.clear();
            DebugHelper.print(filePath + " is verified");

            solver.reset();
            ctx.close();
        }
    }

    public static ExcellReporter VerifyFile(String filePath, Context ctx, Solver solver) throws Exception {
        long buildConstraintsTime = BuildConstraints(filePath, ctx, solver);

        long statSolveConstraints = System.currentTimeMillis();
        String verificationResult = "UNKNOWN";
        DebugHelper.print("Start solve " + filePath.substring(filePath.lastIndexOf("\\") + 1));
        if (solver.check() == Status.SATISFIABLE) {
            verificationResult = "SATISFIABLE";
            System.out.println("SATISFIABLE. UNSAFE PROGRAM.");
        } else {
            verificationResult = "UNSATISFIABLE";
            System.out.println("UNSATISFIABLE. SAFE PROGRAM.");
        }
        DebugHelper.print("End solve " + filePath.substring(filePath.lastIndexOf("\\") + 1));
        long endSolveConstraints = System.currentTimeMillis();

        ExcellReporter reporter = new ExcellReporter(filePath.substring(filePath.lastIndexOf("\\") + 1), LocalDateTime.now(), new ArrayList<String>(Files.readAllLines(Paths.get(filePath))),
                verificationResult, solver.getAssertions().length, buildConstraintsTime, endSolveConstraints - statSolveConstraints);

        return reporter;
    }

    public static void PrintAssertions(Solver solver) {
        for (BoolExpr boolExpr: solver.getAssertions()) {
            System.out.println(boolExpr.toString());
        }
    }
    public static void PrintModel(Solver solver) {
        Model model = solver.getModel();
        System.out.println(model);
    }
    public static ASTFactory BuildAST(String filePath) throws Exception {
        ASTFactory ast = new ASTFactory(filePath);
        if (ast.getMain() == null) {
            throw new Exception("Main function 'main' of " + filePath + "is not detected.");
        }
        return ast;
    }
    public static ControlFlowGraph BuildCFG(ASTFactory ast) {
        IASTFunctionDefinition mainFunc = ast.getMain();
        ControlFlowGraph cfg = new ControlFlowGraph(mainFunc, ast, true);
        // cfg.printGraph();
        return cfg;
    }
    public static EventOrderGraph BuildEOG(ControlFlowGraph cfg) {
        EventOrderGraph eog = EventOrderGraphBuilder.Build(cfg, new HashMap<>(), false);
        // eog.printEOG();
        return eog;
    }
    public static long BuildConstraints(ASTFactory ast, EventOrderGraph eog, Context ctx, Solver solver) throws Exception {
        long startBuildConstraints = System.currentTimeMillis();
        ConstraintManager constraintManager = new ConstraintManager(ctx, solver);
        constraintManager.BuildConstraints(eog, ast);
        long endBuildConstraints = System.currentTimeMillis();
        return endBuildConstraints - startBuildConstraints;
    }
    public static ControlFlowGraph BuildCFG(String filePath) throws Exception {
        return BuildCFG(BuildAST(filePath));
    }
    public static EventOrderGraph BuildEOG(String filePath) throws Exception {
        return BuildEOG(BuildCFG(BuildAST(filePath)));
    }
    public static long BuildConstraints(String filePath, Context ctx, Solver solver) throws Exception {
        ASTFactory ast = BuildAST(filePath);

        ControlFlowGraph cfg = BuildCFG(ast);

        EventOrderGraph eog = BuildEOG(cfg);

        return BuildConstraints(ast, eog, ctx, solver);
    }

}
