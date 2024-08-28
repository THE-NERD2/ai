package com.ai.exceptions

internal class CalculationFailure(msg: String): Exception("Calculation failed: $msg")
internal class NonexistentValueException(msg: String): Exception("Unknown value: $msg")