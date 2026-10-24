/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraftreborn.lib.expression.node.func;

import java.util.Objects;

import buildcraftreborn.lib.expression.NodeInliningHelper;
import buildcraftreborn.lib.expression.api.IDependantNode;
import buildcraftreborn.lib.expression.api.IDependancyVisitor;
import buildcraftreborn.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraftreborn.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraftreborn.lib.expression.api.IExpressionNode.INodeLong;
import buildcraftreborn.lib.expression.api.IExpressionNode.INodeObject;
import buildcraftreborn.lib.expression.api.INodeFunc.INodeFuncLong;
import buildcraftreborn.lib.expression.api.INodeStack;
import buildcraftreborn.lib.expression.api.InvalidExpressionException;
import buildcraftreborn.lib.expression.api.NodeTypes;
import buildcraftreborn.lib.expression.node.func.StringFunctionBi;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import buildcraftreborn.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectToLong<A> extends NodeFuncBase implements INodeFuncLong {

    public final IFuncObjectToLong<A> function;
    private final StringFunctionBi stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectToLong(String name, Class<A> argTypeA, IFuncObjectToLong<A> function) {
        this(argTypeA, function, (a) -> "[ " + NodeTypes.getName(argTypeA) + " -> long ] " + name + "(" + a +  ")");
    }

    public NodeFuncObjectToLong(Class<A> argTypeA, IFuncObjectToLong<A> function, StringFunctionBi stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public NodeFuncObjectToLong<A> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a);
    }

    /** Shortcut to create a new {@link FuncObjectToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectToLong create(INodeObject<A> argA) {
        return new FuncObjectToLong(argA); 
    }

    public class FuncObjectToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;

        public FuncObjectToLong(INodeObject<A> argA) {
            this.argA = argA;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA,
                    (a) -> new FuncObjectToLong(a),
                    (a) -> new FuncObjectToLong(a)
                );
            }
            return NodeInliningHelper.tryInline(this, argA,
                (a) -> new FuncObjectToLong(a),
                (a) -> NodeConstantLong.of(function.apply(a.evaluate()))
            );
        }

        @Override
        public void visitDependants(IDependancyVisitor visitor) {
            if (!canInline) {
                if (function instanceof IDependantNode) {
                    visitor.dependOn((IDependantNode) function);
                } else {
                    visitor.dependOnExplictly(this);
                }
            }
            visitor.dependOn(argA);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncObjectToLong.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncObjectToLong other = (FuncObjectToLong) obj;
            return Objects.equals(argA, other.argA);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectToLong<A> {
        long apply(A a);
    }
}
