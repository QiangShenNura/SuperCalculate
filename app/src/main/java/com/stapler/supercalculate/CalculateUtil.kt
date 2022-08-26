package com.stapler.supercalculate

import androidx.compose.ui.graphics.Color
import com.stapler.supercalculate.ui.theme.DarkBlue
import com.stapler.supercalculate.ui.theme.LightBlue
import com.stapler.supercalculate.ui.theme.LightGray
import java.text.NumberFormat
import java.util.*

class CalculateUtil {
    companion object{
        val elements = arrayOf(
            arrayOf(
                ElementData("C", DarkBlue, LightBlue),
                ElementData("7", Color.Black, LightGray),
                ElementData("4", Color.Black, LightGray),
                ElementData("1", Color.Black, LightGray),
                ElementData("%", Color.Black, LightGray)
            ),
            arrayOf(
                ElementData("÷", DarkBlue, LightBlue),
                ElementData("8", Color.Black, LightGray),
                ElementData("5", Color.Black, LightGray),
                ElementData("2", Color.Black, LightGray),
                ElementData("0", Color.Black, LightGray)
            ),
            arrayOf(
                ElementData("×", DarkBlue, LightBlue),
                ElementData("9", Color.Black, LightGray),
                ElementData("6", Color.Black, LightGray),
                ElementData("3", Color.Black, LightGray),
                ElementData(".", Color.Black, LightGray)
            ),
            arrayOf(
                ElementData("←", DarkBlue, LightBlue),
                ElementData("-", DarkBlue, LightBlue),
                ElementData("+", DarkBlue, LightBlue),
                ElementData("=", Color.White, DarkBlue)
            )
        )

        private val numbers = arrayOf("0","1","2","3","4","5","6","7","8","9")
        private val operators = arrayOf("+","-","×","÷","%")

        fun verifyInput(state: CalculateState, symbol: String): Boolean {
            // TODO verify more case
            if (state.status == Status.INPUT) {
                val lastInput = state.input
                // only operator is not valid
                if (lastInput.isEmpty() && symbol !in numbers) {
                    return false
                }

                if (lastInput.isNotEmpty()){
                    val lastChar = lastInput.last().toString()
                    // continuous operator is not valid
                    if((lastChar in operators) && symbol in operators)
                        return false
                    // check last with .
                    if (lastChar == "."
                        && ((symbol == ".") or ( symbol in operators))){
                        return false
                    }
                }
            } else {
                val result= state.result
                if (result.isNotEmpty() && result.contains(",")) {
                    state.result = result.replace(",","")
                }
            }
            return true
        }

        fun calculate(state: CalculateState, symbol: String): CalculateState {
            return when (symbol) {
                "=" -> {
                    val result = NumberFormat.getInstance().format(exec(state.input))
                    state.copy(status = Status.RESULT, input = "", result = result)
                }
                "C" -> {
                    state.copy(status = Status.INPUT, input = "", result= "")
                }
                "←" -> {
                    val lastInput = state.input
                    val newInput = if (lastInput.isNotEmpty()) lastInput.substring(0,lastInput.length-1) else ""
                    state.copy(status = Status.INPUT, input = newInput, result= "")
                }
                else -> {
                    val newInput =
                        if (state.status == Status.RESULT && state.result != "0") state.result.plus(symbol)
                        else state.input.plus(symbol)
                    state.copy(status = Status.INPUT, input = newInput, result = "")
                }
            }
        }

        private fun exec(_exp: String): Double {
            try {// 有括号
                // -8*(((-2+4)+3)/((-1-5)*-2)-5)
                var exp = _exp
                val leftIndex = exp.lastIndexOf('(') // 16
                return if (leftIndex == -1) {
                    // 没有括号
                    // System.out.println("calc" + exp);
                    calc(exp)
                } else {
                    // 如果有括弧，调用exec
                    // System.out.println("exec" + exp);
                    // 先找最里面的(位置 再找对应的)位置

                    // (-1-5)*-2)-5) 21
                    val rightIndex = exp.substring(leftIndex).indexOf(')') + leftIndex

                    // 去独立的表达式，运算 calc（-1-5）
                    val res: Double = calc(exp.substring(leftIndex + 1, rightIndex))
                    // 重新组织表达式
                    exp = (exp.substring(0, leftIndex) + res
                            + exp.substring(rightIndex + 1))
                    // -8*(((-2+4)+3)/( -6 *-2)-5)
                    exec(exp)
                }
                // 如果没有括弧 直接调用calc
            } catch (e: Exception) {
                println("exec:${_exp} throw Exception:${e}")
                e.printStackTrace()
                return 0.0
            }
        }

        /**
         *
         * @param exp
         * 不带括号的四则表达式
         * @return 运算结果
         */
        private fun calc(exp: String): Double {
            // 1 . 获取所有四则运算的数字
            val numbers: ArrayList<Double> = sliptNumbers(exp)
            // 2. 获取所有四则运算的操作符号
            val ops: ArrayList<Char> = sliptOps(exp)
            // 3. 先乘车运算
            // 遍历运算符中的*和/
            var i = 0
            while (i < ops.size) {

                // * /
                // 获取运算符（不移除）
                val op = ops[i]

                // 如果是 * 或者 /， 从运算符的容器中移除，同是从数字容器中到对应该运算符位置的两个数字（移除数据，后面所有数据往前顺序移动）
                if (op == '×' || op == '÷' || op == '%') {
                    // 从运算符的容器中移除
                    ops.removeAt(i) // 移除当前位置

                    // 从数字容器中获取对应该运算符位置的两个数字（移除）
                    var d1: Double = numbers.removeAt(i)
                    val d2: Double = numbers.removeAt(i)

                    // 运算
                    d1 = when (op) {
                        '×' -> {
                            d1 * d2
                        }
                        '÷' -> {
                            d1 / d2
                        }
                        else -> d1 % d2
                    }

                    // 把运算结果插入到数字容器中的i位置
                    numbers.add(i, d1) // 插入到i的位置 原来从i位置一直到最后的数据，都要往后瞬移一位
                    // numbers.set(i, d1);//设置i位置上的数据为d1,其余不变
                    i-- // 移除后，后面所有运算符往前移动，为了保证下一个运算符不被遗漏，所以i--
                } // end if (op == '*' || op == '/') {
                i++
            }

            // 4. 再加减运算
            while (!ops.isEmpty()) {
                // 每次去运算容器中第一个运算符
                val op: Char = ops.removeAt(0)
                // 每次从数字容器中两次取第一个数字
                var d1: Double = numbers.removeAt(0)
                val d2: Double = numbers.removeAt(0)

                // 计算
                d1 = if (op == '+') d1 + d2 else d1 - d2

                // 把结果插入到数字容器中的第一个位置
                numbers.add(0, d1)
            }

            // 5. 返回结果
            return numbers[0]
        }

        /**
         * 从表达式中分离所有的运算符
         *
         * @param exp
         */
        private fun sliptOps(exp: String): ArrayList<Char> {
            val ops = arrayListOf<Char>()
            // -8*-2+3/-1-5*-2-5
            // 把真实表达式变成下面的表达式
            val formaterExp: String = formaterExp(exp)
            // @8*@2+3/@1-5*@2-5
            val st = StringTokenizer(formaterExp, "@0123456789.")
            while (st.hasMoreTokens()) {
                val opStr: String = st.nextElement().toString() + "" // 取出分割符号之间的数据
                // System.out.println(numStr);
                // 如果前面是@ 变为负数
                ops.add(opStr[0])
            }
            return ops
        }

        /**
         * 从表达式中分离所有的数字
         *
         * @param exp
         * -8*-2+3/-1-5*-2-5 表达式
         */
        private fun sliptNumbers(exp: String): ArrayList<Double> {
            val numbers = arrayListOf<Double>()
            // -8*-2+3/-1-5*-2-5
            // 把真实表达式变成下面的表达式
            val formaterExp: String = formaterExp(exp)
            // @8*@2+3/@1-5*@2-5
            val st = StringTokenizer(formaterExp, "+-×÷%")
            while (st.hasMoreTokens()) {
                var numStr: String = st.nextElement().toString() + "" // 取出分割符号之间的数据
                // System.out.println(numStr);
                // 如果前面是@ 变为负数
                if (numStr[0] == '@') {
                    numStr = "-" + numStr.substring(1)
                }

                // 把数字型的字符串变成数字
                numbers.add(numStr.toDouble())
            }
            return numbers
        }

        private fun formaterExp(exp: String): String {
            var exp = exp
            var index = 0
            while (index < exp.length) {
                val c = exp[index]
                // 判断是否是-符号
                // -代表的是负数 第一个，前一字符*/
                if (c == '-') {
                    // 第一个，
                    if (index == 0) {
                        exp = "@" + exp.substring(1)
                    } else {
                        // 前一字符* /
                        if (exp[index - 1] == '×'
                            || exp[index - 1] == '÷' || exp[index - 1] == '%'
                        ) {
                            exp = (exp.substring(0, index) + "@"
                                    + exp.substring(index + 1))
                        }
                    }
                }
                index++
                //
            }
            return exp
        }

    }

}