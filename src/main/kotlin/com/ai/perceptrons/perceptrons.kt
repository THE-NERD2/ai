package com.ai.perceptrons
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.get
import kotlin.math.abs

private class Encryptor<in XType, YType>(ex: ((XType) -> Number)?, ey: ((YType) -> Number)?, d: ((Number) -> YType)?) {
    private class NonexistentValueException(msg: String): Exception(msg)
    private val encXAlg: (XType) -> Number
    private val encYAlg: (YType) -> Number
    private val decAlg: (Number) -> YType
    private val xndict = arrayListOf<Pair<XType, Number>>()
    private val yndict = arrayListOf<Pair<YType, Number>>()
    private val registeredXValues = arrayListOf<XType>()
    private val registeredYValues = arrayListOf<YType>()
    private var lastX = 0
    private var lastY = 0
    init {
        if(ex is (XType) -> Number) {
            encXAlg = ex
        } else {
            encXAlg = lambda@ { input: XType ->
                var ret: Number? = null
                xndict.forEach {
                    if(it.first == input) {
                        ret = it.second
                    }
                }
                return@lambda ret ?: throw NonexistentValueException("Unrecognized value")
            }
        }
        if(ey is (YType) -> Number) {
            encYAlg = ey
        } else {
            encYAlg = lambda@ { input: YType ->
                var ret: Number? = null
                yndict.forEach {
                    if(it.first == input) {
                        ret = it.second
                    }
                }
                return@lambda ret ?: throw NonexistentValueException("Unrecognized value")
            }
        }
        if(d is (Number) -> YType) {
            decAlg = d
        } else {
            decAlg = lambda@ { input: Number ->
                val dists = arrayListOf<Double>()
                yndict.forEach {
                    dists.add(abs(it.second.toDouble() - input.toDouble()))
                }
                val i = dists.indexOf(dists.min())
                return@lambda yndict[i].first
            }
        }
    }
    fun encryptX(x: XType) = encXAlg(x)
    fun encryptY(y: YType) = encYAlg(y)
    fun decrypt(x: Number) = decAlg(x)
    fun addX(x: XType) {
        if(x !in registeredXValues) {
            registeredXValues.add(x)
            xndict.add(x to lastX++)
        }
    }
    fun addY(y: YType) {
        if(y !in registeredYValues) {
            registeredYValues.add(y)
            yndict.add(y to lastY++)
        }
    }
}
class LinRegPerceptron<in XType, YType>(vars: Int, xEncAlg: ((XType) -> Number)? = null, yEncAlg: ((YType) -> Number)? = null, yDecAlg: ((Number) -> YType)? = null) {
    class CalculationFailure(msg: String): Exception(msg)
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