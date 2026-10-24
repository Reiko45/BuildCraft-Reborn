package buildcraftreborn.lib.expression.node.value;

import java.util.Locale;

import buildcraftreborn.lib.expression.api.IExpressionNode;
import buildcraftreborn.lib.expression.api.IVariableNode;

public abstract class NodeVariable implements IVariableNode {

    public final String name;
    protected boolean isConst = false;

    public NodeVariable(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }

    @Override
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    @Override
    public boolean isConstant() {
        return isConst;
    }

    public abstract void setConstantSource(IExpressionNode source);

    @Override
    public String toString() {
        return "variable: " + name + " = " + evaluateAsString();
    }
}
