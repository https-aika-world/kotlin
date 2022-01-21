/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.visitors.*

abstract class IrWhen : IrExpression() {
    abstract val origin: IrStatementOrigin?

    abstract val branches: MutableList<IrBranch>

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitWhen(this, data)

    override fun <R, D> accept(visitor: IrThinVisitor<R, D>, data: D): R =
        visitor.visitWhen(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        branches.forEach { it.accept(visitor, data) }
    }

    override fun <D> acceptChildren(visitor: IrThinVisitor<Unit, D>, data: D) {
        branches.forEach { it.accept(visitor, data) }
    }

    override fun acceptChildren(consumer: IrElementConsumer) {
        branches.acceptEach(consumer)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        branches.forEachIndexed { i, irBranch ->
            branches[i] = irBranch.transform(transformer, data)
        }
    }
}
