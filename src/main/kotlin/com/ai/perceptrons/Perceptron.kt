package com.ai.perceptrons

interface Perceptron<in XType, YType> {
    fun train(x: Collection<XType>, y: YType)
    fun guess(x: Collection<XType>): YType
}