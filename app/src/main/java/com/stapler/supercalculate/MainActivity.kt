package com.stapler.supercalculate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stapler.supercalculate.CalculateUtil.Companion.elements
import com.stapler.supercalculate.ui.theme.DarkBlue
import com.stapler.supercalculate.ui.theme.LightBlue
import com.stapler.supercalculate.ui.theme.LightGray
import com.stapler.supercalculate.ui.theme.SuperCalculateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperCalculateTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SuperCalculator()
                }
            }
        }
    }
}


@Composable
fun SuperCalculator() {
    Column(Modifier.background(Color.White).padding(20.dp, 0.dp, 20.dp, 0.dp)) {
        var state by remember {
            mutableStateOf(CalculateState())
        }
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                if (state.status == Status.INPUT) state.input else state.result,
                fontSize = 80.sp,
                color = Color.Black
            )
        }
        Row(
            Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            elements.forEach { columns ->
                Column(Modifier.weight(1f)) {
                    columns.forEach {
                        val btnWeight = if (it.symbol == "=") 2f else 1f
                        val btnRatio = if (it.symbol == "=") 0.4f else 1f
                        CalculatorElement(
                            Modifier
                                .weight(btnWeight)
                                .aspectRatio(btnRatio)
                                .background(it.backgroundColor), it.symbol, it.textColor,
                            onClick = {
                                if(!CalculateUtil.verifyInput(state,it.symbol)) return@CalculatorElement
                                state = CalculateUtil.calculate(state, it.symbol)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorElement(
    modifier: Modifier,
    symbol: String,
    symbolColor: Color,
    onClick: () -> Unit = {}
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(modifier)
            .clickable { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 40.sp, color = symbolColor)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    SuperCalculateTheme {
        SuperCalculator()
    }
}