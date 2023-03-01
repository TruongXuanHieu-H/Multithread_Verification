package com.mtv.graph.cfg.build;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.ast.ASTFactory;
import com.mtv.graph.ast.FunctionHelper;
import com.mtv.graph.ast.IASTVariable;
import com.mtv.graph.cfg.node.*;
import com.mtv.graph.cfg.utils.ExpressionModifier;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import java.util.ArrayList;



public class MultiFunctionCFGBuilder {
    private ASTFactory ast;

    public MultiFunctionCFGBuilder() {
    }

    public MultiFunctionCFGBuilder(ASTFactory ast) {
        this.ast = ast;

    }


    public ControlFlowGraph build(IASTFunctionDefinition func, boolean includeGlobalVars) {
        if (func == null) {
            DebugHelper.print("Function is null. Terminated.");
            return null;
        }
        // Create empty control flow graph

        ControlFlowGraph prvCfg = new ControlFlowGraph();
        // Create CPP node factory, where is used to build node for control flow graph
        CPPNodeFactory factory = new CPPNodeFactory();

        ASTFactory ast = new ASTFactory(func.getTranslationUnit());


        if (includeGlobalVars) {
            ArrayList<IASTDeclaration> declarations = ast.getGlobalVarList();
            ControlFlowGraph subCfg;
            for (IASTDeclaration declaration : declarations) {
                IASTDeclarationStatement statement = factory.newDeclarationStatement(declaration.copy());
                subCfg = (new ControlFlowGraphBuilder()).createSubGraph(statement);
                if (prvCfg == null) {
                    prvCfg = subCfg;
                } else {
                    prvCfg.concat(subCfg);
                }
            }
        }
        BeginFunctionNode beginFunc = new BeginFunctionNode(func);
        prvCfg.concat(new ControlFlowGraph(beginFunc, beginFunc));
        ControlFlowGraph mainFuncCfg = new ControlFlowGraph(func);
        prvCfg.concat(mainFuncCfg);
        EndFunctionNode endFunction = new EndFunctionNode(func);
        prvCfg.concat(new ControlFlowGraph(endFunction, endFunction));
        iterateNode(prvCfg.getStart(), func);

        return prvCfg;
    }

    //Create a list of CFGs
    private ArrayList<ControlFlowGraph> createList() {
        ArrayList<ControlFlowGraph> list = new ArrayList<>();
        ControlFlowGraph cfg;
        for (IASTFunctionDefinition func : ast.getListFunction()) {
            cfg = new ControlFlowGraph(func);
            list.add(cfg);
        }
        return list;
    }

    /**
     * @param node, end, func
     *              Ham duyet java.cfg va xu ly FunctionCallNode
     */
    private CFGNode iterateNode(CFGNode node, IASTFunctionDefinition func) {
        if (node == null) {

        } else if (node instanceof DecisionNode) {
            ((DecisionNode) node).setThenNode(iterateNode(((DecisionNode) node).getThenNode(), func));
            ((DecisionNode) node).setElseNode(iterateNode(((DecisionNode) node).getElseNode(), func));
        } else if (node instanceof BeginNode) {
            node.setNext(iterateNode(node.getNext(), func));
            ((BeginNode) node).getEndNode().setNext(iterateNode(((BeginNode) node).getEndNode().getNext(), func));
        } else if (node instanceof FunctionCallNode) {
            ControlFlowGraph functionGraph = createFuncGraph(((FunctionCallNode) node).getFunctionCall(), func);
            if (functionGraph != null) {
                CFGNode pause = node.getNext();
                node = iterateNode(functionGraph.getStart(), func);
                functionGraph.getExit().setNext(iterateNode(pause, func));
            }
        } else if (node instanceof EndNode) {

        } else if (node instanceof AbortNode) {

        } else if (node instanceof EndFunctionNode) {

        } else if (node instanceof EndConditionNode) {

        } else {
            node.setNext(iterateNode(node.getNext(), func));
        }
        return node;
    }

    private boolean isVoid(IASTFunctionCallExpression callExpression) {
        String funcName = callExpression.getFunctionNameExpression().toString();
        IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), funcName);
        String type = FunctionHelper.getFunctionType(func);
        return type.equals("void") ? true : false;
    }

    /* Create CFG for function call expression
     * Argument list:
     *  - callExpression: expression of the call, which contains information about the call such as called function's name or arguments
     *  - currentFunc: the function which callExpression is stay inside
     */
    private ControlFlowGraph createFuncGraph(IASTFunctionCallExpression callExpression, IASTFunctionDefinition currentFunc) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        // The function which is called by this expression
        String targetedFuncName = callExpression.getFunctionNameExpression().toString();
        String info = callExpression.getRawSignature();

        /*
         * Custom CFG for pthread_create function
         *
         */
        if (targetedFuncName.equals("pthread_create")) {
            /*
             * pthread_create takes 4 arguments
             */
            IASTInitializerClause[] arguments = callExpression.getArguments();
            String threadReference = arguments[0].getRawSignature().substring(1) + "_" + currentFunc.getDeclarator().getName();
            String attributeExpression = arguments[1].getRawSignature();
            String funcReference = arguments[2].getRawSignature();
            String restrictionExpression = arguments[3].getRawSignature();

            /*
             * Control flow graph of the function which pthread_t is pointed to
             */
            ControlFlowGraph funcCFG = new ControlFlowGraph(ast.getFunction(funcReference), ast, false);
            CreateThreadNode createThreadNode = new CreateThreadNode(threadReference, attributeExpression, funcReference, funcCFG, restrictionExpression);


            return new ControlFlowGraph(createThreadNode, createThreadNode);
        }
        /*
         * Custom CFG for pthread_join function
         *
         */
        else if (targetedFuncName.equals("pthread_join")) {
            /*
             * pthread_join takes 2 arguments
             */
            IASTInitializerClause[] arguments = callExpression.getArguments();
            String threadReference = arguments[0].getRawSignature() + "_" + currentFunc.getDeclarator().getName();
            String retvalExpression = arguments[1].getRawSignature();

            JoinThreadNode joinThreadNode = new JoinThreadNode(threadReference, retvalExpression);

            return new ControlFlowGraph(joinThreadNode, joinThreadNode);
        } else if (targetedFuncName.equals("abort")) {
            AbortNode abortNode = new AbortNode();
            return new ControlFlowGraph(abortNode, abortNode);
        }
        /*
         * Default CFG for other functions
         *
         */
        else {
            IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), targetedFuncName);

            if (func == null) {
                System.err.println("Not found function " + targetedFuncName);
                System.exit(1);
            }

            //add begin function node at the beginning
            BeginFunctionNode beginNode = new BeginFunctionNode(func);
            cfg.concat(new ControlFlowGraph(beginNode, beginNode));

            CPPNodeFactory factory = (CPPNodeFactory) func.getTranslationUnit().getASTNodeFactory();
            //Cho tham so = params
            ControlFlowGraph argGraph = createArguments(callExpression, currentFunc);
            if (argGraph != null) cfg.concat(argGraph);

            //Noi voi than cua ham duoc goi
            ControlFlowGraph funcGraph = new ControlFlowGraph(func);
            //funcGraph.ungoto();
            //funcGraph.unfold(1);
            //TODO Try to unfold funcGraph
            cfg.concat(funcGraph);

            //Tao ra node: ham duoc goi = return neu khong phai void
            if (!isVoid(callExpression)) {
                IASTIdExpression left = (IASTIdExpression) ExpressionModifier.changeFunctionCallExpression(callExpression, func);
                IASTName nameRight = factory.newName(("return_" + FunctionHelper.getShortenName(targetedFuncName)).toCharArray());
                IASTIdExpression right = factory.newIdExpression(nameRight);
                IASTBinaryExpression binaryExp = factory.newBinaryExpression(IASTBinaryExpression.op_assign, left, right);
                IASTExpressionStatement statement = factory.newExpressionStatement(binaryExp);

                CFGNode plainNode = new PlainNode(statement); //tao ra plainNode khong co ten ham dang sau
                cfg.concat(new ControlFlowGraph(plainNode, plainNode));
            }

            EndFunctionNode endFunction = new EndFunctionNode(func);
            cfg.concat(new ControlFlowGraph(endFunction, endFunction));

            return cfg;


        }

    }

    /**
     * @param callExpression
     * @param currentFunc    Tra ve cac Node xu ly tham so cua ham (neu co)
     */
    private ControlFlowGraph createArguments(IASTFunctionCallExpression callExpression, IASTFunctionDefinition currentFunc) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        String funcName = callExpression.getFunctionNameExpression().toString();
        CFGNode plainNode;
        IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), funcName);

        ArrayList<IASTVariable> params = FunctionHelper.getParameters(func);
        IASTInitializerClause[] arguments = callExpression.getArguments();
        IASTBinaryExpression expression;
        IASTExpressionStatement statement;
        IASTExpression right;
        IASTName leftName;
        IASTIdExpression left;
        String leftNameStr;
        String offset = "";
//		CFGNode declNode;
//		IASTDeclarationStatement declStatement;
//		IASTDeclarator declarator;
//		IASTSimpleDeclaration declaration;

        if (arguments.length == 0) return null;
        CPPNodeFactory factory = (CPPNodeFactory) func.getTranslationUnit().getASTNodeFactory();

        for (int i = 0; i < arguments.length; i++) {
            leftNameStr = params.get(i).getName().toString();
            leftNameStr += "_" + funcName;
            offset = "";
//			for (IASTNode node : arguments) {
//				offset += "_" +  node.toString();
//			}
            leftNameStr += offset;
            leftName = factory.newName(leftNameStr.toCharArray());
            left = factory.newIdExpression(leftName);
//			IASTDeclSpecifier type = params.get(i).getType().copy();
//			declarator = factory.newDeclarator(leftName);
//			declaration = factory.newSimpleDeclaration(type);
//			declaration.addDeclarator(declarator);
//			declStatement = factory.newDeclarationStatement(declaration);
//			declNode = new PlainNode(declStatement, func);
//			java.cfg.concat(new ControlFlowGraph(declNode, declNode));

//			IASTName rightName = factory.newName((arguments[i].getRawSignature().toCharArray()));
//			IASTIdExpression right = factory.newIdExpression(rightName);

            right = (IASTExpression) ExpressionModifier.changeVariableName((IASTExpression) arguments[i].copy(), currentFunc);
            expression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, left, right);
            statement = factory.newExpressionStatement(expression);
            plainNode = new PlainNode(statement);
            cfg.concat(new ControlFlowGraph(plainNode, plainNode));
        }

        return cfg;
    }


}
