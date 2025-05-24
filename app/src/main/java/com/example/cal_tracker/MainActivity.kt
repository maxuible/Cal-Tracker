package com.example.cal_tracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cal_tracker.ui.theme.CalTrackerTheme
import com.google.gson.Gson
import androidx.compose.runtime.LaunchedEffect
import com.google.gson.reflect.TypeToken

private const val TAG = "MyActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalTrackerTheme {
                Main(context = this@MainActivity)
            }
        }
    }


}
@Composable
fun Main(modifier: Modifier = Modifier, context: Context){
    val sharedPrefs = context.getSharedPreferences("calorie_tracker", Context.MODE_PRIVATE)
    val gson = Gson()
    Column(modifier = Modifier) {
        var goal by remember { mutableStateOf("") }
        var dailyList by remember { mutableStateOf(emptyList<Pair<String, String>>())}
        var tempName by remember { mutableStateOf("") }
        var tempCal by remember { mutableStateOf("") }
        var totalCalories by remember { mutableStateOf("")}



        LaunchedEffect(Unit) {
            goal = sharedPrefs.getString("goal", "2000").toString()
            val savedListJson = sharedPrefs.getString("daily_list", "[]")
            val type = object : TypeToken<List<Pair<String, String>>>() {}.type
            dailyList = gson.fromJson(savedListJson, type) ?: emptyList()
            totalCalories = dailyList.sumOf { pair -> pair.first.toIntOrNull() ?: 0 }.toString()
        }

        fun saveData() {
            with(sharedPrefs.edit()) {
                putString("goal", goal)
                putString("daily_list", gson.toJson(dailyList))
                apply()
            }
        }

        Column(modifier = modifier.padding(16.dp)) {
            OutlinedTextField(
                label = { Text("Enter a goal") },
                value = goal.toString(),
                onValueChange = { newGoal ->
                    if (newGoal.all { it.isDigit() } || newGoal.isEmpty()) { // Allow empty string
                        goal = newGoal
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            OutlinedTextField(
                label = { Text("Enter a name") },
                value = tempName,
                onValueChange = { newName ->
                    tempName = newName
                }
            )
            OutlinedTextField(
                label = { Text("Enter a Cal") },
                value = tempCal.toString(),
                onValueChange = { newCal ->
                    if (newCal.all { it.isDigit() } || newCal.isEmpty()) { // Allow empty string
                        tempCal = newCal
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Button(
                onClick = {
                    dailyList = dailyList + (tempCal to tempName)
                    tempCal = ""
                    tempName = ""
                    totalCalories = dailyList.sumOf { pair -> pair.first.toIntOrNull() ?: 0 }.toString()
                    saveData()
                },
            ) {
                Text("Submit")
            }
        }
        Row(modifier = Modifier) {
            Text(text = "TOTAL:$totalCalories", modifier = modifier.weight(1f))
            Text(text = "GOAL:$goal", modifier = modifier.weight(1f))
            Text(text = "LEFT:${(goal.toIntOrNull() ?: 0) - (totalCalories.toIntOrNull() ?: 0)}", modifier = modifier.weight(1f))
        }
        Row(modifier = Modifier) {
            Text(text = "CAL", modifier = Modifier.weight(1f))
            Text(text = "ITEM", modifier = Modifier.weight(1f))
        }
        dailyList.forEachIndexed { index, pair ->
            val key = pair.first
            val value = pair.second

            Row(modifier = Modifier) {
                Text(text = key.toString(), modifier = Modifier.weight(1f))
                Text(text = value, modifier = Modifier.weight(1f))
                Button(onClick = {
                    dailyList = dailyList.filterIndexed { i, _ -> i != index}
                    totalCalories = dailyList.sumOf { pair -> pair.first.toIntOrNull() ?: 0 }.toString()
                    }) {
                    Text("Remove")
                    saveData()
                }
            }
        }
    }
}


