/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.transformIfNeeded
import org.jetbrains.kotlin.ir.visitors.*

abstract class IrFunction :
    IrDeclarationBase(),
    IrPossiblyExternalDeclaration, IrDeclarationWithVisibility, IrTypeParametersContainer, IrSymbolOwner, IrDeclarationParent, IrReturnTarget,
    IrMemberWithContainerSource,
    IrMetadataSourceOwner {

    @ObsoleteDescriptorBasedAPI
    abstract override val descriptor: FunctionDescriptor
    abstract override val symbol: IrFunctionSymbol

    abstract val isInline: Boolean // NB: there's an inline constructor for Array and each primitive array class
    abstract val isExpect: Boolean

    abstract var returnType: IrType

    abstract var dispatchReceiverParameter: IrValueParameter?
    abstract var extensionReceiverParameter: IrValueParameter?
    abstract var valueParameters: List<IrValueParameter>

    /**
     * The first `contextReceiverParametersCount` value parameters are context receivers
     */
    abstract var contextReceiverParametersCount: Int

    abstract var body: IrBody?

    @Suppress("DuplicatedCode")
    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        typeParameters.forEach { it.accept(visitor, data) }

        dispatchReceiverParameter?.accept(visitor, data)
        extensionReceiverParameter?.accept(visitor, data)
        valueParameters.forEach { it.accept(visitor, data) }

        body?.accept(visitor, data)
    }

    @Suppress("DuplicatedCode")
    override fun <D> acceptChildren(visitor: IrThinVisitor<Unit, D>, data: D) {
        typeParameters.forEach { it.accept(visitor, data) }

        dispatchReceiverParameter?.accept(visitor, data)
        extensionReceiverParameter?.accept(visitor, data)
        valueParameters.forEach { it.accept(visitor, data) }

        body?.accept(visitor, data)
    }

    override fun acceptChildren(consumer: IrElementConsumer) {
        typeParameters.acceptEach(consumer)
        dispatchReceiverParameter?.let { consumer.visitElement(it) }
        extensionReceiverParameter?.let { consumer.visitElement(it) }
        valueParameters.acceptEach(consumer)
        body?.let { consumer.visitElement(it) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        typeParameters = typeParameters.transformIfNeeded(transformer, data)

        dispatchReceiverParameter = dispatchReceiverParameter?.transform(transformer, data)
        extensionReceiverParameter = extensionReceiverParameter?.transform(transformer, data)
        valueParameters = valueParameters.transformIfNeeded(transformer, data)

        body = body?.transform(transformer, data)
    }
}
