package com.ai.encryptors

import com.ai.exceptions.NonexistentValueException
import kotlin.math.abs

class Encryptor<in XType, YType>(ex: ((XType) -> Number)?, ey: ((YType) -> Number)?, d: ((Number) -> YType)?) {
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