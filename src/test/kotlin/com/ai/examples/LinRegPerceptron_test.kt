package com.ai.examples

import com.ai.exceptions.CalculationFailure
import com.ai.perceptrons.LinRegPerceptron

fun main() {
    println(
        """Commands:
            |   exit: exit program
            |   train <r> <g> <b> <lightness>: store given parameters in training dataset (no space in lightness)
            |   trainall [n]: train based on answer key (n is the size of selection. all by default.)
            |   guess <r> <g> <b>: guess lightness
            |   accuracy: guess all and print accuracy
        """.trimMargin()
    )
    val perceptron = LinRegPerceptron<String, String>(3, yValues = listOf("very_dark" to 0, "dark" to 1, "light" to 2, "very_light" to 3), xEncAlg = {
        try {
            it.toInt()
        } catch(_: NumberFormatException) {
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
        cmd = readln()
        try {
            if (cmd == "exit") {
                break
            } else if(cmd.length >= 8 && cmd.substring(0, 8) == "trainall") {
                val selection: List<Pair<String, String>>
                if(cmd != "trainall") {
                    val n = cmd.substring(9).toInt()
                    selection = answerKey.shuffled().subList(0, n)
                } else {
                    selection = answerKey
                }
                selection.forEach {
                    perceptron.train(it.first.getColorComponents(), it.second)
                }
            } else if (cmd.length > 5 && cmd.substring(0, 5) == "train") {
                val (r, g, b, l) = cmd.substring(6).split(" ")
                perceptron.train(listOf(r, g, b), l)
            } else if (cmd.length > 5 && cmd.substring(0, 5) == "guess") {
                try {
                    val (r, g, b) = cmd.substring(6).split(" ")
                    println(perceptron.guess(listOf(r, g, b)))
                } catch (_: CalculationFailure) {
                    println("Not enough information to calculate")
                }
            } else if(cmd == "accuracy") {
                var correct = 0
                answerKey.forEach {
                    if(perceptron.guess(it.first.getColorComponents()) == it.second) correct++
                }
                println("" + (correct.toDouble() / answerKey.size.toDouble()) * 100 + "%")
            } else {
                println("Not a command.")
            }
        } catch(_: NumberFormatException) {
            println("That\'s not a number.")
        }
    }
}