package com.ai.encryptors

import kotlin.math.cbrt
import kotlin.math.round
import kotlin.math.tanh

enum class ActivationFunction(func: (Double) -> Double): (Double) -> Double by func {
    NONE({ it }),
    BINARY({ round(it).coerceIn(0.0, 1.0) }),
    CBRT({ cbrt(it) }),
    TANH({ tanh(it) }),
    CBRT_01({ (cbrt(it) + 1.0) / 2.0 }),
    TANH_01({ (tanh(it) + 1.0) / 2.0 })
}