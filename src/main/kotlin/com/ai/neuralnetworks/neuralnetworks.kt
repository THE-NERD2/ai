package com.ai.neuralnetworks
import com.ai.Encryptor as DefaultEncryptor
import com.ai.get
import kotlin.math.cbrt
import kotlin.math.tanh
interface NeuralNetwork<in XType, YType> {
    fun train(x: XType, y: YType)
    fun guess(x: XType): YType
}
class LinNN<in XType, YType>( // XType should be a data class containing the data types of all the intended parameters
    learningMultiplier: LinNN.LearningRate,
    architecture: LinNN.ArchitectureType,
    activator: LinNN.Activator,
    yEncAlg: ((YType) -> Number)? = null,
    yDecAlg: ((Number) -> YType)? = null,
    customArchitecture: (LinNN<XType, YType>.Architecture<YType>.() -> Unit)? = null
): NeuralNetwork<XType, YType> {
    interface Input<in XType, out YType> { // Now XType is a regular data type since this corresponds to single perceptrons
        fun getValue(x: Collection<XType>): YType
    }
    enum class LearningRate(internal val multiplier: Double) {
        CAUTIOUS(0.1),
        AGGRESSIVE(10.0),
        NORMAL(1.0)
    }
    enum class ArchitectureType {
        DEFAULT,
        SOFTMAX_ALL,
        SOFTMAX_IMMEDIATE,
        CUSTOM
    }
    enum class Activator(internal val function: (x: Double) -> Double) {
        BINARY({ x: Double ->
            if(x >= 0) {
                1.0
            } else {
                0.0
            }
        }),
        CUBE_ROOT({ x: Double ->
            cbrt(x)
        }),
        HYPERBOLIC_TANGENT({ x: Double ->
            tanh(x)
        }),
        NONE({ x: Double ->
            x
        })
    }
    internal data class Node(val id: Int, val input: Input<*, *>, val flowsTo: ArrayList<Int>, val flowsFrom: ArrayList<Int>) {
        companion object {
            var greatestUsedId = 0
        }
    }
    class XEncryptor<in XType>(ex: ((XType) -> Number)? = null): DefaultEncryptor<XType, Unit>(ex, null, null), Input<XType, Number> {
        override fun getValue(x: Collection<XType>) = encryptX(x[0])
    }
    inner class Perceptron<YType>: Input<Double, YType> {
        private val inputs = arrayListOf<Input<Double, Double>>()
        private var w = arrayListOf(1.0)
        fun input(input: Input<Double, Double>) {
            inputs.add(input)
            w.add(Math.random())
        }
        fun backpropagate(error: Double) {
            // Continue
        }

    }
    inner class Architecture<YType> {
        internal val nodes = arrayListOf<Node>()
        fun input(input: LinNN.XEncryptor<*>) {

        }
        fun perceptron(final: Boolean = false, inputs: Perceptron<*>.() -> Unit): Perceptron<*> {
            val perceptron = if(final) {
                Perceptron<YType>()
            } else {
                Perceptron<Double>()
            }
            perceptron.inputs()
            return perceptron
        }
    }
}