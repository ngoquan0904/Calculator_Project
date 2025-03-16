package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var workingsTV: TextView
    private lateinit var resultsTV: TextView
    private var workings: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workingsTV = findViewById(R.id.workingsTV)
        resultsTV = findViewById(R.id.resultsTV)
    }

    fun numberAction(view: android.view.View) {
        val button = view as Button
        workings += button.text
        updateWorkings()
    }

    fun operationAction(view: android.view.View) {
        val button = view as Button
        val operator = button.text.toString()
        val lastChar = workings.takeLast(1)

        if (workings.isEmpty() && operator == "-") {
            // Cho phép nhập số âm ở đầu
            workings += "-"
        } else if (lastChar.isNotEmpty() && !isOperator(lastChar) || (operator == "-" && isOperator(lastChar))) {
            // Cho phép dấu '-' sau toán tử để nhập số âm
            workings += operator
        }

        updateWorkings()
    }

    fun allClearAction(view: android.view.View) {
        workings = ""
        resultsTV.text = ""
        updateWorkings()
    }

    fun backSpaceAction(view: android.view.View) {
        if (workings.isNotEmpty()) {
            workings = workings.dropLast(1)
            updateWorkings()
        }
    }

    fun equalsAction(view: android.view.View) {
        try {
            val result = eval(workings)
            resultsTV.text = result.toString()
        } catch (e: Exception) {
            resultsTV.text = "Error"
        }
    }

    private fun updateWorkings() {
        workingsTV.text = workings
    }

    private fun isOperator(char: String): Boolean {
        return char in listOf("+", "-", "x", "/")
    }

    private fun eval(expression: String): Double {
        val formattedExpression = expression.replace("x", "*")
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < formattedExpression.length) formattedExpression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < formattedExpression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor() // Hỗ trợ số âm
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = formattedExpression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }
}
