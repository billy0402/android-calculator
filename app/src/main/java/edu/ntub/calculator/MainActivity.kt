package edu.ntub.calculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    var numberArray = ArrayList<BigDecimal>()
    var symbolArray = ArrayList<Symbol>()
    private var textLimitLength = 0

    private lateinit var inputLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputLabel = findViewById(R.id.textViewInput)
        textLimitLength = inputLabel.getMaxTextLength()

        buttonOne.setOnClickListener(tapDigit)
        buttonTwo.setOnClickListener(tapDigit)
        buttonThree.setOnClickListener(tapDigit)
        buttonFour.setOnClickListener(tapDigit)
        buttonFive.setOnClickListener(tapDigit)
        buttonSix.setOnClickListener(tapDigit)
        buttonSeven.setOnClickListener(tapDigit)
        buttonEight.setOnClickListener(tapDigit)
        buttonNine.setOnClickListener(tapDigit)
        buttonZero.setOnClickListener(tapDigit)

        buttonDot.setOnClickListener(tapDot)
        buttonPlus.setOnClickListener(tapSymbol)
        buttonMinus.setOnClickListener(tapSymbol)
        buttonMultiplied.setOnClickListener(tapSymbol)
        buttonDivided.setOnClickListener(tapSymbol)
        buttonEqual.setOnClickListener(tapEqual)
        buttonPlusAndMinus.setOnClickListener(tapSymbol)
        buttonPercent.setOnClickListener(tapSymbol)
        buttonDetele.setOnClickListener(tapDelete)
        buttonClear.setOnClickListener(tapClear)
    }

    // 所有按鍵
    private fun tapAny(view: View) {
        val button = view as Button
        Log.d("Tapping:", button.text.toString())
        val originLabelText = inputLabel.text.toString()

        // 限制最大字數
        if (originLabelText.length > textLimitLength * 2) {
            textLimitAlert()
        }
    }

    // 運算元
    private val tapDigit = View.OnClickListener { view ->
        tapAny(view)
        val digit = (view as Button).text
        val originLabelText = inputLabel.text.toString()

        // 消除 Label 的 ０
        if (originLabelText == "0") {
            inputLabel.text = digit
        } else {
            inputLabel.append(digit)
        }
    }

    // 小數點
    private val tapDot = View.OnClickListener { view ->
        tapAny(view)
        val dot = (view as Button).text
        val originLabelText = inputLabel.text.toString()

        // 防止小數點連點
        if (originLabelText.last() != '.') {
            inputLabel.append(dot)
        }
    }

    // 運算子
    private val tapSymbol = View.OnClickListener { view ->
        tapAny(view)
        val symbol = (view as Button).text
        val originLabelText = inputLabel.text.toString()

        // +/- % 運算
        if (symbol == "+/-" || symbol == "%") {
            if (!(originLabelText.isNumber() || originLabelText.first() == '-')) {
                return@OnClickListener
            }

            val number = BigDecimal(originLabelText)
            val result = if (symbol == "+/-") {
                number * BigDecimal(-1)
            } else {
                number.divide(BigDecimal(100), 12, BigDecimal.ROUND_HALF_UP)
            }
            inputLabel.text = resultFormatter(result)
            return@OnClickListener
        }

        // 防止符號連點
        if (!originLabelText.last().isDigit()) {
            inputLabel.text = originLabelText.dropLast(1)
        }

        inputLabel.append(symbol)
    }

    // 等號
    private val tapEqual = View.OnClickListener { view ->
        tapAny(view)
        val originLabelText = inputLabel.text.toString()

        // 特殊情況 URL
        if (originLabelText == "11+22") {
            specialUrlAlert()
            return@OnClickListener
        }

        // 防止符號句尾
        if (!originLabelText.last().isDigit()) {
            return@OnClickListener
        }

        numberArray.clear()
        symbolArray.clear()

        divideNumberSymbol()

        val result = getResult()
        inputLabel.text = resultFormatter(result)
    }

    // 刪除
    private val tapDelete = View.OnClickListener { view ->
        tapAny(view)
        val originLabelText = inputLabel.text.toString()
        inputLabel.text = originLabelText.dropLast(1)

        // 防止完全清空畫面
        if (inputLabel.text.isEmpty()) {
            inputLabel.text = "0"
        }
    }

    // 清除
    private val tapClear = View.OnClickListener { view ->
        tapAny(view)
        inputLabel.text = "0"
    }

    // 將輸入框的 數字 與 符號 分開
    private fun divideNumberSymbol() {
        var originLabelText = inputLabel.text.toString()

        // 防止負號開頭
        if (originLabelText.first() == '-') {
            originLabelText = "0$originLabelText"
        }

        for (text in originLabelText) {
            val symbol = Symbol.charToSymbol(text)
            if (symbol != null) {
                val index = originLabelText.indexOf(text)
                val nextIndex = index + 1

                val front = originLabelText.substring(0, index) // 前面數字
                val back = originLabelText.substring(nextIndex) // 後面數字

                if (front.isNumber()) {
                    numberArray.add(BigDecimal(front))
                }
                if (back.isNumber()) {
                    numberArray.add(BigDecimal(back))
                }
                symbolArray.add(symbol)

                originLabelText = originLabelText.drop(nextIndex)
            }
        }
    }

    private fun getResult(): BigDecimal? {
        val originLabelText = inputLabel.text.toString()
        var result = if (numberArray.isEmpty()) BigDecimal(originLabelText) else BigDecimal.ZERO

        while (symbolArray.isNotEmpty()) {
            val sortedSymbolArray = getSortedSymbolArray()

            val frontIndex = sortedSymbolArray.first()
            val backIndex = frontIndex + 1
            val frontNumber = numberArray[frontIndex]
            val backNumber = numberArray[backIndex]

            try {
                result = when (symbolArray[frontIndex]) {
                    Symbol.MULTIPLIED -> frontNumber * backNumber
                    Symbol.DIVIDED -> frontNumber.divide(backNumber, 12, BigDecimal.ROUND_HALF_UP)
                    Symbol.PLUS -> frontNumber + backNumber
                    Symbol.MINUS -> frontNumber - backNumber
                }
            } catch (e: ArithmeticException) {
                return null
            }

            numberArray[backIndex] = result
            numberArray.removeAt(frontIndex)
            symbolArray.removeAt(frontIndex)
        }

        return result
    }

    private fun getSortedSymbolArray(): ArrayList<Int> {
        val prioritySymbolArray = symbolArray.indexes(Symbol.MULTIPLIED, Symbol.DIVIDED)
        val normalSymbolArray = symbolArray.indexes(Symbol.PLUS, Symbol.MINUS)

        val sortedSymbplArray = ArrayList<Int>()
        sortedSymbplArray.addAll(prioritySymbolArray)
        sortedSymbplArray.addAll(normalSymbolArray)

        return sortedSymbplArray
    }

    private fun resultFormatter(result: BigDecimal?): String {
        // 防止 ÷0 和 超出上限
        if (result == null) {
            return "error"
        }

        var resultString = result.toDouble().toString()
        val resultIntString = result.toBigInteger().toString()

        if (resultIntString.toDouble().toString() == resultString) {
            resultString = if (resultString.contains('E')) resultString.pointFormatter() else resultIntString
        } else {
            val intLength = resultIntString.length
            val pointLength = textLimitLength - intLength - 1
            val decimalFormat = DecimalFormat()
            decimalFormat.maximumIntegerDigits = intLength
            decimalFormat.maximumFractionDigits = pointLength
            resultString = decimalFormat.format(result)
        }

        return resultString
    }

    private fun textLimitAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("⚠️ 警告")
        builder.setMessage("超過字數上限")
        builder.setPositiveButton("OK") { _, _ ->
            inputLabel.text = inputLabel.text.dropLast(1)
        }
        builder.show()
    }

    private fun specialUrlAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("外部連結")
        builder.setMessage("Ｇoogle")
        builder.setPositiveButton("前往") { _, _ ->
            val urlIntent = Intent(Intent.ACTION_VIEW)
            urlIntent.data = Uri.parse("https://www.google.com.tw/")
            startActivity(urlIntent)
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }
}

enum class Symbol(private val symbol: Char) {
    PLUS('+'),
    MINUS('-'),
    MULTIPLIED('×'),
    DIVIDED('÷');

    companion object {
        fun charToSymbol(text: Char): Symbol? {
            return try {
                values().single { it.symbol == text }
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }
}
