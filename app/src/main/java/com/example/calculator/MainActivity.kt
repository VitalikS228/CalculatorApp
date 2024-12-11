package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

@Composable
fun CalculatorApp() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isResultDisplayed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Поле для вводу та результату
        Text(
            text = if (input.isNotEmpty()) input else result,
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки
        val buttonColors = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0","", ".", "=")
        )

        for (row in buttonColors) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (button in row) {
                    CalculatorButton(
                        text = button,
                        modifier = Modifier
                            .weight(if (button == "0") 1f else 1f)
                            .aspectRatio(1f),
                        onClick = {
                            when (button) {
                                "C" -> {
                                    input = ""
                                    result = ""
                                    isResultDisplayed = false
                                }
                                "=" -> {
                                    try {
                                        result = calculateExpression(input)
                                        input = ""
                                        isResultDisplayed = true
                                    } catch (e: Exception) {
                                        result = "Error"
                                        input = ""
                                    }
                                }
                                "±" -> {
                                    if (input.isNotEmpty() && !isResultDisplayed) {
                                        input = if (input.startsWith("-")) {
                                            input.removePrefix("-")
                                        } else {
                                            "-$input"
                                        }
                                    }
                                }
                                else -> {
                                    if (isResultDisplayed) {
                                        input = result
                                        isResultDisplayed = false
                                    }
                                    input += button
                                }
                            }
                        },
                        backgroundColor = when (button) {
                            "C", "±", "%" -> Color.Gray
                            "÷", "×", "-", "+", "=" -> Color(0xFFFF9500)
                            else -> Color.DarkGray
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(4.dp)
            .background(backgroundColor, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            color = if (backgroundColor == Color.Gray) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// Функція для обчислень
fun calculateExpression(expression: String): String {
    return try {
        val sanitizedExpression = expression.replace("×", "*").replace("÷", "/")
        val result = eval(sanitizedExpression)

        if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            result.toString()
        }
    } catch (e: Exception) {
        "Error"
    }
}

// Простий інтерпретатор для обчислень
fun eval(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat.code) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x += parseTerm() // addition
                else if (eat('-')) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*')) x *= parseFactor() // multiplication
                else if (eat('/')) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+')) return parseFactor() // unary plus
            if (eat('-')) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            if (eat('(')) { // parentheses
                x = parseExpression()
                eat(')')
            } else if (ch in '0'.code..'9'.code || ch == '.'.code) { // numbers
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }.parse()
}
