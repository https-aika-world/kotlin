/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.scopeSessionKey
import org.jetbrains.kotlin.fir.scopes.impl.*
import org.jetbrains.kotlin.fir.symbols.ensureResolved

private val ALL_IMPORTS = scopeSessionKey<FirFile, ListStorageFirScope>()
private val INVISIBLE_DEFAULT_STAR_IMPORT = scopeSessionKey<DefaultImportPriority, FirDefaultStarImportingScope>()
private val VISIBLE_DEFAULT_STAR_IMPORT = scopeSessionKey<DefaultImportPriority, FirDefaultStarImportingScope>()
private val DEFAULT_SIMPLE_IMPORT = scopeSessionKey<DefaultImportPriority, FirDefaultSimpleImportingScope>()

fun createImportingScopes(
    file: FirFile,
    session: FirSession,
    scopeSession: ScopeSession,
    useCaching: Boolean = true
): List<FirScope> = if (useCaching) {
    scopeSession.getOrBuild(file, ALL_IMPORTS) {
        ListStorageFirScope(doCreateImportingScopes(file, session, scopeSession))
    }.result
} else {
    doCreateImportingScopes(file, session, scopeSession)
}

private fun doCreateImportingScopes(
    file: FirFile,
    session: FirSession,
    scopeSession: ScopeSession
): List<FirScope> {
    file.ensureResolved(FirResolvePhase.IMPORTS)
    return listOf(
        // from low priority to high priority
        scopeSession.getOrBuild(DefaultImportPriority.LOW, INVISIBLE_DEFAULT_STAR_IMPORT) {
            FirDefaultStarImportingScope(session, scopeSession, FirImportingScopeFilter.INVISIBLE_CLASSES, DefaultImportPriority.LOW)
        },
        scopeSession.getOrBuild(DefaultImportPriority.HIGH, INVISIBLE_DEFAULT_STAR_IMPORT) {
            FirDefaultStarImportingScope(session, scopeSession, FirImportingScopeFilter.INVISIBLE_CLASSES, DefaultImportPriority.HIGH)
        },
        FirExplicitStarImportingScope(file.imports, session, scopeSession, FirImportingScopeFilter.INVISIBLE_CLASSES),
        // TODO: invisible classes from current package should go before this point
        scopeSession.getOrBuild(DefaultImportPriority.LOW, VISIBLE_DEFAULT_STAR_IMPORT) {
            FirDefaultStarImportingScope(session, scopeSession, FirImportingScopeFilter.MEMBERS_AND_VISIBLE_CLASSES, DefaultImportPriority.LOW)
        },
        scopeSession.getOrBuild(DefaultImportPriority.HIGH, VISIBLE_DEFAULT_STAR_IMPORT) {
            FirDefaultStarImportingScope(session, scopeSession, FirImportingScopeFilter.MEMBERS_AND_VISIBLE_CLASSES, DefaultImportPriority.HIGH)
        },
        FirExplicitStarImportingScope(file.imports, session, scopeSession, FirImportingScopeFilter.MEMBERS_AND_VISIBLE_CLASSES),

        scopeSession.getOrBuild(DefaultImportPriority.LOW, DEFAULT_SIMPLE_IMPORT) {
            FirDefaultSimpleImportingScope(session, scopeSession, priority = DefaultImportPriority.LOW)
        },
        scopeSession.getOrBuild(DefaultImportPriority.HIGH, DEFAULT_SIMPLE_IMPORT) {
            FirDefaultSimpleImportingScope(session, scopeSession, priority = DefaultImportPriority.HIGH)
        },
        scopeSession.getOrBuild(file.packageFqName, PACKAGE_MEMBER) {
            FirPackageMemberScope(file.packageFqName, session)
        },
        // TODO: explicit simple importing scope should have highest priority (higher than inner scopes added in process)
        FirExplicitSimpleImportingScope(file.imports, session, scopeSession)
    )
}

private class ListStorageFirScope(val result: List<FirScope>) : FirScope()
