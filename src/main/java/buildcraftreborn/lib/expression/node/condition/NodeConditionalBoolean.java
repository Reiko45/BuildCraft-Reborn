/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraftreborn.lib.expression.node.condition;

import buildcraftreborn.lib.expression.api.IDependancyVisitor;
import buildcraftreborn.lib.expression.api.IDependantNode;
import buildcraftreborn.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraftreborn.lib.expression.node.value.NodeConstantBoolean;

public class NodeConditionalBoolean implements INodeBoolean, IDependantNode {
    private final INodeBoolean condition;
    private final INodeBoolean ifTrue, ifFalse;

    public NodeConditionalBoolean(INodeBoolean condition, INodeBoolean ifTrue, INodeBoolean ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public boolean evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeBoolean inline() {
        INodeBoolean c = condition.inline();
        INodeBoolean t = ifTrue.inline();
        INodeBoolean f = ifFalse.inline();
        if (c instanceof NodeConstantBoolean) {
            return ((NodeConstantBoolean) c).value ? t : f;
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalBoolean(c, t, f);
        } else if (c instanceof NodeConstantBoolean) {
            return ((NodeConstantBoolean) c).value ? t : f;
        } else {
            return this;
        }
    }

    @Override
    public void visitDependants(IDependancyVisitor visitor) {
        visitor.dependOn(condition, ifTrue, ifFalse);
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
