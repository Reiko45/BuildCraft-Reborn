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
import buildcraftreborn.lib.expression.api.INodeFunc.INodeFuncObject;
import buildcraftreborn.lib.expression.api.INodeStack;
import buildcraftreborn.lib.expression.api.InvalidExpressionException;
import buildcraftreborn.lib.expression.api.NodeTypes;
import buildcraftreborn.lib.expression.node.func.StringFunctionBi;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase;
import buildcraftreborn.lib.expression.node.func.NodeFuncBase.IFunctionNode;
import buildcraftreborn.lib.expression.node.value.NodeConstantObject;

// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!
public class NodeFuncLongToObject<R> extends NodeFuncBase implements INodeFuncObject<R> {

    public final IFuncLongToObject<R> function;
    private final StringFunctionBi stringFunction;
    private final Class<R> returnType;

    public NodeFuncLongToObject(String name, Class<R> returnType, IFuncLongToObject<R> function) {
        this(returnType, function, (a) -> "[ long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a +  ")");
    }

    public NodeFuncLongToObject(Class<R> returnType, IFuncLongToObject<R> function, StringFunctionBi stringFunction) {
        this.returnType = returnType;

        this.function = function;
        this.stringFunction = stringFunction;
    }

    @Override
    public Class<R> getType() {
        return returnType;
    }

    @Override
    public String toString() {
        return stringFunction.apply("{A}");
    }

    @Override
    public NodeFuncLongToObject<R> setNeverInline() {
        super.setNeverInline();
        return this;
    }

    @Override
    public INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {

        INodeLong a = stack.popLong();

        return create(a);
    }

    /** Shortcut to create a new {@link FuncLongToObject} without needing to create
     *  and populate an {@link INodeStack} to pass to {@link #getNode(INodeStack)}. */
    public FuncLongToObject create(INodeLong argA) {
        return new FuncLongToObject(argA); 
    }

    public class FuncLongToObject implements INodeObject<R>, IDependantNode, IFunctionNode {
        public final INodeLong argA;

        public FuncLongToObject(INodeLong argA) {
            this.argA = argA;

        }

        @Override
        public Class<R> getType() {
            return returnType;
        }

        @Override
        public R evaluate() {
            return function.apply(argA.evaluate());
        }

        @Override
        public INodeObject<R> inline() {
            if (!canInline) {
                // Note that we can still inline the arguments, just not *this* function
                return NodeInliningHelper.tryInline(this, argA,
                    (a) -> new FuncLongToObject(a),
                    (a) -> new FuncLongToObject(a)
                );
            }
            return NodeInliningHelper.tryInline(this, argA,
                (a) -> new FuncLongToObject(a),
                (a) -> new NodeConstantObject<>(returnType, function.apply(a.evaluate()))
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
            return NodeFuncLongToObject.this;
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
            FuncLongToObject other = (FuncLongToObject) obj;
            return Objects.equals(argA, other.argA);
        }
    }

    @FunctionalInterface
    public interface IFuncLongToObject<R> {
        R apply(long a);
    }
}
