package com.fieldvet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fieldvet.view.FieldVetNavHost
import com.fieldvet.view.theme.FieldVetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FieldVetTheme {
                FieldVetNavHost()
            }
        }
    }
}
