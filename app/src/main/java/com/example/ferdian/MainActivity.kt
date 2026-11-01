package com.example.ferdian

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(247, 247, 242)
        window.navigationBarColor = android.graphics.Color.rgb(247, 247, 242)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        setContent {
            TomatoCareHome(
                onStartScan = { openScreen(CameraActivity::class.java) },
                onOpenDiseases = { openScreen(InformasiActivity::class.java) },
                onOpenAbout = { openScreen(InfoActivity::class.java) }
            )
        }
    }

    private fun openScreen(screen: Class<*>) {
        startActivity(Intent(this, screen))
    }
}

private val Forest = Color(0xFF263D33)
private val Paper = Color(0xFFF7F7F2)
private val Quiet = Color(0xFF748078)

@Composable
private fun TomatoCareHome(
    onStartScan: () -> Unit,
    onOpenDiseases: () -> Unit,
    onOpenAbout: () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            onPrimary = Paper,
            background = Paper,
            surface = Paper,
            onSurface = Forest,
            onSurfaceVariant = Quiet
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Forest,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.home_eyebrow),
                    color = Quiet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.home_headline),
                    color = Forest,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.7).sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.home_description),
                    color = Quiet,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(34.dp))
                Button(
                    onClick = onStartScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Forest,
                        contentColor = Paper
                    )
                ) {
                    Text(
                        text = stringResource(R.string.home_primary_action),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.home_scan_hint),
                    color = Quiet,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(42.dp))
                Text(
                    text = stringResource(R.string.home_explore),
                    color = Quiet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HomeLink(
                    title = stringResource(R.string.disease_title),
                    supportingText = stringResource(R.string.disease_subtitle),
                    onClick = onOpenDiseases
                )
                HomeLink(
                    title = stringResource(R.string.about_title),
                    supportingText = stringResource(R.string.info_subtitle),
                    onClick = onOpenAbout
                )
            }
        }
    }
}

@Composable
private fun HomeLink(
    title: String,
    supportingText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Forest,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = supportingText,
                color = Quiet,
                fontSize = 13.sp
            )
        }
        Text(
            text = "→",
            color = Quiet,
            fontSize = 20.sp
        )
    }
}