/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.visitors.*

abstract class IrTry : IrExpression() {
    abstract var tryResult: IrExpression

    abstract val catches: MutableList<IrCatch>

    abstract var finallyExpression: IrExpression?

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitTry(this, data)

    override fun <R, D> accept(visitor: IrThinVisitor<R, D>, data: D): R =
        visitor.visitTry(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        tryResult.accept(visitor, data)
        catches.forEach { it.accept(visitor, data) }
        finallyExpression?.accept(visitor, data)
    }

    override fun <D> acceptChildren(visitor: IrThinVisitor<Unit, D>, data: D) {
        tryResult.accept(visitor, data)
        catches.forEach { it.accept(visitor, data) }
        finallyExpression?.accept(visitor, data)
    }

    override fun acceptChildren(consumer: IrElementConsumer) {
        consumer.visitElement(tryResult)
        catches.acceptEach(consumer)
        finallyExpression?.let { consumer.visitElement(it) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        tryResult = tryResult.transform(transformer, data)
        catches.forEachIndexed { i, irCatch ->
            catches[i] = irCatch.transform(transformer, data)
        }
        finallyExpression = finallyExpression?.transform(transformer, data)
    }
}
