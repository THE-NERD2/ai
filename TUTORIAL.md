# AI library tutorial

### Table of contents

- [Overview](#overview)
- [Linear Regression](#linear-regression)
- [Gradient Descent](#gradient-descent)

## Overview

Currently, all AI models are in the form of classes implementing the ```Perceptron``` interface. This is the implementation for ```Perceptron```:

```kotlin
interface Perceptron<in XType, YType> {
    fun train(x: Collection<XType>, y: YType)
    fun guess(x: Collection<XType>): YType
}
```

All models, therefore, are trained with the ```train``` method. Predicting a value is done with the ```guess``` method.

You may have noticed that ```Perceptron``` is generic. This is because AI internally uses an ```Encryptor``` class to automatically convert any type to a number, which is used in the models.
For the most part, ```Encryptor``` works pretty well, but there are some cases where it needs a little more information from the user to work correctly.

### Configuring ```Encryptor```s

An ```Encryptor``` has a default algorithm for encryption and decryption, which works by assigning each value with a certain number. This is okay, but it has two problems: the first and most dangerous
problem is that the default method requires that all values (both x and y) must be given in ascending or descending order because this is the way numbers are assigned under the hood. Another problem
is quite simple: not all problems are classification problems. Fortunately, ```Encryptor``` allows you to combat both of these problems. To combat the first problem, simply pre-define the possible
values and associated numbers, like so:

```kotlin
val perceptron = LinRegPerceptron<String, String>(3, yValues = listOf("very_dark" to 0, "dark" to 1, "light" to 2, "very_light" to 3), ...
```

Don't worry about this code too much, but notice that this code assigns a list to the ```yValues``` parameter. The parameter takes a list of ```Pair<YType, Number>```. There is also an ```xValues```
parameter as well.

> __Note__: Because perceptrons tend to take a lot of constructor parameters, it's a good practice to pass ```Encryptor``` configurations by name, to keep the code easy to understand.
> This also helps if you want to configure part of the ```Encryptor``` but leave the rest as default.

To combat the second aforementioned problem, ```Encryptor``` also allows you to completely override an encrypting/decrypting algorithm, like so:

```kotlin
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
```

<sub>This code is taken from the LinRegPerceptron_test.kt file under src/main/test. If you want a great example of how to use AI models, look there.</sub>

Notice that this uses both of the configuring techniques explained above.

## Linear Regression

The Linear Regression model (represented with the ```LinRegPerceptron``` class) performs a simple linear regression on its input. Linear regressions, as the name implies, can
only learn from linear data. It works by finding the theoretical optimum for the best fit line of the input data. It is also very computationally intensive for large problems.
```LinRegPerceptron``` has no public methods except those that comefrom ```Perceptron```.

```LinRegPerceptron``` also requires one constructor parameter: the number of variables.

A general example of ```LinRegPerceptron```:

```kotlin
val perceptron = LinRegPerceptron<Any, Any>(number_of_variables)

perceptron.train(xValue1, yValue1)
//...

perceptron.guess(xValue1)
```

> __Note__: Linear regression problems cannot learn from ambiguous or otherwise insufficient information. ```LinRegPerceptron``` can usually learn from information that a human could
> logically learn from. If you attempt to guess a value from a ```LinRegPerceptron``` that hasn't received sufficient training data, it will throw a ```CalculationFailure```.

## Gradient Descent

Gradient descent (represented by ```GradDescPerceptron```) is a good alternative to linear regression. It is much less computationally intensive because it works by continually
updating its weight vector based on the given data point. ```GradDescPerceptron``` also never throws a ```CalculationFailure``` because of insufficient data. In some problems,
it learns better by reiterating existing training data, a task which is performed by the ```iterate``` method. It's constructor requires the number of variables and also a floating
point value that represents the learning rate. The learning rate is an arbitrary value that is multiplied with the error when updating weights. Tinker with this value to find
the optimal learning rate.

A general example of ```GradDescPerceptron``` is as follows:

```kotlin
val perceptron = GradDescPerceptron<Any, Any>(number_of_variables, 0.01) // Replace with your own learning rate

perceptron.train(xValue1, yValue1)
//...

// Reiterate the given training data 500 times.
// Reiteration isn't always necessary, and it
// doesn't necessarily need to be 500 times.
// Tinker with this too!
perceptron.iterate(500)

perceptron.guess(xValue1)
```