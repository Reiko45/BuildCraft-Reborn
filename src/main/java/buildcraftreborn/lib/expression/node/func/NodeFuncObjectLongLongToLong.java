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
import buildcraftreborn.lib.expression.node.func.StringFunctionQuad;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import buildcraftreborn.lib.expression.node.value.NodeConstantLong;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncObjectLongLongToLong<A> extends NodeFuncBase implements INodeFuncLong {

    public final IFuncObjectLongLongToLong<A> function;
    private final StringFunctionQuad stringFunction;
    private final Class<A> argTypeA;

    public NodeFuncObjectLongLongToLong(String name, Class<A> argTypeA, IFuncObjectLongLongToLong<A> function) {
        this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", long, long -> long ] " + name + "(" + a + ", " + b + ", " + c +  ")");
    }

    public NodeFuncObjectLongLongToLong(Class<A> argTypeA, IFuncObjectLongLongToLong<A> function, StringFunctionQuad stringFunction) {
        this.argTypeA = argTypeA;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}", "{B}", "{C}");
    }

    @Override
    public NodeFuncObjectLongLongToLong<A> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeLong getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong c = stack.popLong();
        INodeLong b = stack.popLong();
        INodeObject<A> a = stack.popObject(argTypeA);

        return create(a, b, c);
    }

    /** Shortcut to create a new {@link FuncObjectLongLongToLong} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncObjectLongLongToLong create(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
        return new FuncObjectLongLongToLong(argA, argB, argC); 
    }

    public class FuncObjectLongLongToLong implements INodeLong, IDependantNode, IFunctionNode {
        public final INodeObject<A> argA;
        public final INodeLong argB;
        public final INodeLong argC;

        public FuncObjectLongLongToLong(INodeObject<A> argA, INodeLong argB, INodeLong argC) {
            this.argA = argA;
            this.argB = argB;
            this.argC = argC;

        }

        @Override
        public long evaluate() {
            return function.apply(argA.evaluate(), argB.evaluate(), argC.evaluate());
        }

        @Override
        public INodeLong inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA, argB, argC,
                    (a, b, c) -> new FuncObjectLongLongToLong(a, b, c),
                    (a, b, c) -> new FuncObjectLongLongToLong(a, b, c)
                );
            }
            return NodeInliningHelper.tryInline(this, argA, argB, argC,
                (a, b, c) -> new FuncObjectLongLongToLong(a, b, c),
                (a, b, c) -> NodeConstantLong.of(function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
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
            visitor.dependOn(argA, argB, argC);
        }

        @Override
        public String toString() {
            return stringFunction.apply(argA.toString(), argB.toString(), argC.toString());
        }

        @Override
        public NodeFuncBase getFunction() {
            return NodeFuncObjectLongLongToLong.this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(argA, argB, argC);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FuncObjectLongLongToLong other = (FuncObjectLongLongToLong) obj;
            return Objects.equals(argA, other.argA) //
            &&Objects.equals(argB, other.argB) //
            &&Objects.equals(argC, other.argC);
        }
    }

    @FunctionalInterface
    public interface IFuncObjectLongLongToLong<A> {
        long apply(A a, long b, long c);
    }
}
