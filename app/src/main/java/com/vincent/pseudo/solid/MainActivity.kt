package com.vincent.pseudo.solid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vincent.pseudo.solid.ui.theme.Pseudo3DTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pseudo3DTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Rotating3DPlanesDemo(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Coordinate3DSystemPreview() {
    Pseudo3DTheme {
        Rotating3DPlanesDemo(Modifier)
    }
}