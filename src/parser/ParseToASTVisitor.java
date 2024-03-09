package parser;

import ast.*;
import libs.Node;

import java.util.ArrayList;
import java.util.List;

public class ParseToASTVisitor extends SimpleFilesParserBaseVisitor<Node> {
    @Override
    public Program visitProgram(SimpleFilesParser.ProgramContext cxt) {
        List<Statement> statements = new ArrayList<>();

        for (SimpleFilesParser.BodyContext b : cxt.body()) {
            statements.add((Statement) b.accept(this));
        }
        return new Program(statements);
    }

    @Override
    public Node visitBody(SimpleFilesParser.BodyContext cxt) {
        return super.visitBody(cxt);
    }

    @Override
    public Instruction visitInstruction(SimpleFilesParser.InstructionContext cxt) {
        String instructionName = cxt.TEXT().getText();
        Action action = (Action) cxt.set_action().accept(this);
        Instruction instruction = new Instruction(instructionName, action);

        for (SimpleFilesParser.Set_paramContext p : cxt.set_param()) {
            instruction.addParameter((Parameter) p.accept(this));
        }
        return instruction;
    }

    @Override
    public Action visitSet_action(SimpleFilesParser.Set_actionContext cxt) {
        return new Action(cxt.ACTION().getText());
    }

    @Override
    public Parameter visitSet_param(SimpleFilesParser.Set_paramContext cxt) {
        int last_index = cxt.PARAM_VAL().getText().length() - 1;
        String trimmed_string = cxt.PARAM_VAL().getText().substring(1, last_index);
        return new Parameter(cxt.PARAM_KEY().getText(), trimmed_string);
    }

    @Override
    public Condition visitCondition(SimpleFilesParser.ConditionContext cxt) {
        String name = cxt.TEXT().getText();
        Action action = (Action) cxt.set_action().accept(this);
        Condition condition = new Condition(name, action);

        for (SimpleFilesParser.Set_paramContext p : cxt.set_param()) {
            condition.addParameter((Parameter) p.accept(this));
        }
        return condition;
    }

    @Override
    public Node visitExecution(SimpleFilesParser.ExecutionContext cxt) {
        return super.visitExecution(cxt);
    }

    @Override
    public ExecuteInstruction visitExec_inst(SimpleFilesParser.Exec_instContext cxt) {
        String instructionName = cxt.TEXT().getText();
        return new ExecuteInstruction(instructionName);
    }

    @Override
    public Instruction visitExec_map(SimpleFilesParser.Exec_mapContext cxt) {
        return new Instruction("");
    }

    @Override
    public ExecuteCondMap visitExec_cond_map(SimpleFilesParser.Exec_cond_mapContext cxt) {
        String instructionName = cxt.TEXT().get(0).getText();
        String conditionName = cxt.TEXT().get(1).getText();
        return new ExecuteCondMap(instructionName, conditionName);
    }

    @Override
    public Variable visitVariable(SimpleFilesParser.VariableContext cxt) {
        String key = cxt.PARAM_KEY().getText();
        String value = cxt.PARAM_VAL().getText();
        return new Variable(key, value.substring(1, value.length()-1));
    }

}
