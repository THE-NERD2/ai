package com.ai.perceptrons

import com.ai.encryptors.Encryptor
import com.ai.exceptions.CalculationFailure
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.get

class LinRegPerceptron<in XType, YType>(vars: Int, xEncAlg: ((XType) -> Number)? = null, yEncAlg: ((YType) -> Number)? = null, yDecAlg: ((Number) -> YType)? = null) {
    private val encryptor = Encryptor(xEncAlg, yEncAlg, yDecAlg)
    private var w = mk.zeros<Double>(vars + 1, 1)
    private var xv = arrayListOf<ArrayList<Double>>()
    private var yv = arrayListOf<ArrayList<Double>>()
    fun train(x: Collection<XType>, y: YType) = runBlocking {
        val job = launch {
            launch {
                x.forEach {
                    encryptor.addX(it)
                }
            }
            launch {
                encryptor.addY(y)
            }
        }
        job.join()
        val doubleX = arrayListOf(1.0)
        for(i in x) {
            doubleX.add(encryptor.encryptX(i).toDouble())
        }
        xv.add(doubleX)
        yv.add(arrayListOf(encryptor.encryptY(y).toDouble()))
    }
    fun finalizeTraining() {
        try {
            val X = mk.ndarray(xv)
            val Y = mk.ndarray(yv)
            val Xt = X.transpose()
            w = mk.linalg.inv(Xt dot X) dot Xt dot Y
        } catch(e: Exception) {
            throw CalculationFailure("Not enough information to train")
        }
    }
    fun guess(x: Collection<XType>): YType {
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