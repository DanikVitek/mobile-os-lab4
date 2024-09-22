package me.danikvitek.lab4.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class BoundaryException : Exception() {
    class Break : BoundaryException()
    class Continue : BoundaryException()
}

@OptIn(ExperimentalContracts::class)
inline fun loop(crossinline body: Boundary.() -> Unit) {
    contract {
        callsInPlace(body, InvocationKind.AT_LEAST_ONCE)
    }
    while (true) {
        try {
            Boundary.body()
        } catch (e: BoundaryException) {
            when (e) {
                is BoundaryException.Break -> break
                is BoundaryException.Continue -> continue
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
suspend inline fun loopSuspending(crossinline body: suspend Boundary.() -> Unit) {
    contract {
        callsInPlace(body, InvocationKind.AT_LEAST_ONCE)
    }
    while (true) {
        try {
            Boundary.body()
        } catch (e: BoundaryException) {
            when (e) {
                is BoundaryException.Break -> break
                is BoundaryException.Continue -> continue
            }
        }
    }
}

object Boundary {
    inline val breakLoop: Nothing get() = throw BoundaryException.Break()
    inline val continueLoop: Nothing get() = throw BoundaryException.Continue()
}
