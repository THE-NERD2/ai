package com.ai.perceptrons

import com.ai.encryptors.Encryptor
import com.ai.exceptions.CalculationFailure
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.get

class LinRegPerceptron<in XType, YType> private constructor(vars: Int): Perceptron<XType, YType> {
    private lateinit var encryptor: Encryptor<XType, YType>
    private var w = mk.zeros<Double>(vars + 1, 1)
    private var xv = arrayListOf<ArrayList<Double>>()
    private var yv = arrayListOf<ArrayList<Double>>()
    private var isUpToDate = false
    constructor(vars: Int,
                xEncAlg: ((XType) -> Number)? = null,
                yEncAlg: ((YType) -> Number)? = null,
                yDecAlg: ((Number) -> YType)? = null): this(vars) {
        encryptor = Encryptor(xEncAlg, yEncAlg, yDecAlg)
    }
    constructor(vars: Int,
                xValues: Collection<Pair<XType, Number>>? = null,
                yValues: Collection<Pair<YType, Number>>? = null,
                xEncAlg: ((XType) -> Number)? = null,
                yEncAlg: ((YType) -> Number)? = null,
                yDecAlg: ((Number) -> YType)? = null): this(vars) {
        encryptor = Encryptor(xValues, yValues, xEncAlg, yEncAlg, yDecAlg)
    }
    private fun finalizeTraining() {
        try {
            val X = mk.ndarray(xv)
            val Y = mk.ndarray(yv)
            val Xt = X.transpose()
            w = mk.linalg.inv(Xt dot X) dot Xt dot Y
        } catch(_: Exception) {
            throw CalculationFailure("Not enough information to train")
        }
    }
    override fun train(x: Collection<XType>, y: YType) {
        x.forEach(encryptor::addX)
        encryptor.addY(y)
        val doubleX = arrayListOf(1.0)
        for(i in x) {
            doubleX.add(encryptor.encryptX(i).toDouble())
        }
        xv.add(doubleX)
        yv.add(arrayListOf(encryptor.encryptY(y).toDouble()))
        isUpToDate = false
    }
    override fun guess(x: Collection<XType>): YType {
        if(!isUpToDate) {
            finalizeTraining()
            isUpToDate = true
        }
        try {
            val encryptedX = arrayListOf(1.0)
            for(i in x) {
                encryptedX.add(encryptor.encryptX(i).toDouble())
            }
            val X = mk.ndarray(listOf(encryptedX))
            val unencrypted = X dot w
            return encryptor.decrypt(unencrypted[0, 0])
        } catch(e: Exception) {
            e.printStackTrace()
            throw CalculationFailure("Not enough information to calculate")
        }
    }
}