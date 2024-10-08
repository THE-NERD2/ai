package com.ai.perceptrons

import com.ai.encryptors.Encryptor
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.rand
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.operations.minusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.times

class GradDescPerceptron<in XType, YType> private constructor(private val vars: Int, private val learningRate: Double): Perceptron<XType, YType> {
    private lateinit var encryptor: Encryptor<XType, YType>
    private var w = mk.rand<Double>(vars + 1)
    private val X = arrayListOf<ArrayList<Double>>()
    private val Y = arrayListOf<Double>()
    constructor(vars: Int,
                learningRate: Double,
                xValues: Collection<Pair<XType, Number>>? = null,
                yValues: Collection<Pair<YType, Number>>? = null,
                xEncAlg: ((XType) -> Number)? = null,
                yEncAlg: ((YType) -> Number)? = null,
                yDecAlg: ((Number) -> YType)? = null): this(vars, learningRate) {
        encryptor = Encryptor(xValues, yValues, xEncAlg, yEncAlg, yDecAlg)
    }
    private fun updateWeights() {
        val dw = mk.zeros<Double>(vars + 1)
        for(i in 0..X.size - 1) {
            val xvi = mk.ndarray(X[i])
            val rawError = Y[i] - (w dot xvi)
            dw += -2 * rawError * xvi
        }
        w -= dw * (learningRate / X.size) // Fixes operator problems
    }
    fun reiterate(times: Int) {
        for(i in 0..times) {
            updateWeights()
        }
    }
    override fun train(x: Collection<XType>, y: YType) {
        x.forEach(encryptor::addX)
        encryptor.addY(y)
        val xa = arrayListOf(1.0)
        for(i in x) {
            xa.add(encryptor.encryptX(i).toDouble())
        }
        val yn = encryptor.encryptY(y).toDouble()
        X.add(xa)
        Y.add(yn)
        updateWeights()
    }
    override fun guess(x: Collection<XType>): YType {
        val encryptedX = arrayListOf(1.0)
        for(i in x) {
            encryptedX.add(encryptor.encryptX(i).toDouble())
        }
        val xv = mk.ndarray(encryptedX)
        return encryptor.decrypt(w dot xv)
    }
}