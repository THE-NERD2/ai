@file:JvmName("LinRegPerceptronExample")
package com.ai.examples
import com.ai.CalculationFailure
import com.ai.perceptrons.LinRegPerceptron
fun main() {
    println(
        """Commands:
            |   exit: exit program
            |   train <r> <g> <b> <lightness>: store given parameters in training dataset (no space in lightness)
            |   eval: train based on stored dataset
            |   guess <r> <g> <b>: guess lightness
        """.trimMargin()
    )
    val perceptron = LinRegPerceptron<String, String>(3, xEncAlg = {
        try {
            it.toInt()
        } catch(e: NumberFormatException) {
            if(it.substring(0, 2) == "0x") {
                it.substring(2).toInt(16)
            } else if(it[0] == '#') {
                it.substring(1).toInt(16)
            } else {
                it.toInt(16)
            }
        }
    })
    var cmd: String
    while(true) {
        print(">>> ")
        cmd = readLine()!!
        try {
            if (cmd == "exit") {
                break
            } else if (cmd == "eval") {
                try {
                    perceptron.finalizeTraining()
                } catch (e: CalculationFailure) {
                    println("Not enough information to train")
                }
            } else if (cmd.substring(0, 5) == "train") {
                val (r, g, b, l) = cmd.substring(6).split(" ")
                perceptron.train(listOf(r, g, b), l)
            } else if (cmd.substring(0, 5) == "guess") {
                try {
                    val (r, g, b) = cmd.substring(6).split(" ")
                    println(perceptron.guess(listOf(r, g, b)))
                } catch (e: CalculationFailure) {
                    println("Not enough information to calculate")
                }
            } else {
                println("Not a command.")
            }
        } catch(e: NumberFormatException) {
            println("That\'s not a number.")
        }
    }
}