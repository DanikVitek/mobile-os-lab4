package me.danikvitek.lab4.util

sealed class BoundaryException : Exception() {
    class Break : BoundaryException()
    class Continue : BoundaryException()
}

inline fun loop(crossinline body: Boundary.() -> Unit) {
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

suspend inline fun loopSuspending(crossinline body: suspend Boundary.() -> Unit) {
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
