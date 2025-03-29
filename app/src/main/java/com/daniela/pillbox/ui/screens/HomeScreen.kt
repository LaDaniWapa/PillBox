import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen

data class HomeScreen(val modifier: Modifier = Modifier.fillMaxSize()) : Screen {
    @Composable
    override fun Content() {
        Column() {
            Text("HOME")
        }
    }

}