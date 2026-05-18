package com.example.manekelsa

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.manekelsa.ui.theme.ManeKelsaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import android.content.Intent
import android.net.Uri


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("test")

        myRef.setValue("Working")
            .addOnSuccessListener {
                android.util.Log.d("FIREBASE", "DATA SENT")
            }
            .addOnFailureListener {
                android.util.Log.e("FIREBASE", "ERROR: ${it.message}")
            }
        enableEdgeToEdge()
        var selectedLanguage by mutableStateOf("English")
        setContent {
            ManeKelsaTheme {

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen( navController = navController,
                            language = selectedLanguage,
                            onLanguageChange = {
                                selectedLanguage = it
                            }
                        )
                    }
                    composable("list") {
                        WorkerListScreen(
                            navController = navController,
                            language = selectedLanguage
                        )

                    }

                    composable("bookingSuccess") {
                        BookingSuccessScreen(navController)
                    }
                    composable("viewBookings") {
                        ViewBookingsScreen(navController)
                    }

                    composable("detail/{name}/{phone}") { backStackEntry ->

                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val phone = backStackEntry.arguments?.getString("phone") ?: ""

                        WorkerDetailScreen(name, phone, navController)
                    }
                }
            }
        }
    }
}

data class Worker(
    val name: String,
    val skill: String,
    val rate: String,
    val available: Boolean,
    val phone: String,
    val rating: Float,
    val image: Int   // ⭐ NEW
)
data class Booking(
    val workerName: String = "",
    val customerName: String = "",
    val date: String = "",
    val time: String = "",
    val paymentMethod: String = ""
)
@Composable
fun LoginScreen( navController: NavController,
                 language: String,
                 onLanguageChange: (String) -> Unit) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Mane-Kelsa",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(if (language == "Kannada")
            "ನಿಮ್ಮ ಹತ್ತಿರದ ಕೆಲಸಗಾರರನ್ನು ಹುಡುಕಿ"
        else
            "Find workers near you")
        Spacer(modifier = Modifier.height(12.dp))

        Row {

            Button(
                onClick = {
                    onLanguageChange("English")
                }
            ) {
                Text("English")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    onLanguageChange("Kannada")
                }
            ) {
                Text("ಕನ್ನಡ")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (username.isNotEmpty() && password.isNotEmpty()) {

                    auth.signInWithEmailAndPassword(username, password)

                        .addOnSuccessListener {

                            navController.navigate("list")

                        }

                        .addOnFailureListener { e ->

                            error = e.message ?: "Login Failed"

                        }

                } else {

                    error = "Please enter email and password"

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (language == "Kannada")
                    "ಲಾಗಿನ್"
                else
                    "Login"
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerListScreen(
    navController: NavController,
    language: String
) {
    var searchText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    val workers = listOf(
        Worker("Ravi", "Gardening", "₹300/day", true, "9876543210", 4.5f, R.drawable.ravi),
        Worker("Sita", "Cleaning", "₹250/day", false, "9123456780", 3.8f, R.drawable.sita),
        Worker("Ramesh", "Plumbing", "₹400/day", true, "9988776655", 4.9f, R.drawable.ramesh)
    )

    val filteredWorkers = workers.filter {
        it.name.contains(searchText, ignoreCase = true) ||
                it.skill.contains(searchText, ignoreCase = true)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,   // ✅ MOVE HERE
        topBar = {
            TopAppBar(
                title = { Text("Mane-Kelsa") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = {
                    Text(
                        if (language == "Kannada")
                            "ಕೆಲಸದ ಹೆಸರು ಹುಡುಕಿ"
                        else
                            "Search worker or skill"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            LazyColumn(){

                items(filteredWorkers) { worker ->
                    WorkerItem(
                        worker = worker,
                        language = language
                    ) {
                        navController.navigate("detail/${worker.name}/${worker.phone}")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerItem(
    worker: Worker,
    language: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < worker.rating.toInt())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = worker.rating.toString(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔹 Top Row (Icon + Name + Price)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = worker.image),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = worker.skill,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Text(
                    text = worker.rate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Availability Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                Text(
                    if (language == "Kannada")
                        "ಲಭ್ಯ"
                    else
                        "Available"
                )

                Switch(
                    checked = worker.available,
                    onCheckedChange = {}
                )
            }
        }
    }
}
@Composable
fun WorkerDetailScreen(name: String, phone: String, navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState() // Required for clickability on long screens

    var customerName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    // Use your specific database URL
    val database = FirebaseDatabase.getInstance("https://manekelsa-eeac3-default-rtdb.firebaseio.com/")
    val bookingsRef = database.getReference("bookings")
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> selectedDate = "$day/${month + 1}/$year" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> selectedTime = "$hour:${if (minute < 10) "0$minute" else minute}" },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Basic worker lookup (keep your current logic)
    val worker = when (name) {
        "Ravi" -> Worker("Ravi", "Gardening", "₹300/day", true, "9876543210", 4.5f, R.drawable.ravi)
        "Sita" -> Worker("Sita", "Cleaning", "₹250/day", false, "9123456780", 3.8f, R.drawable.sita)
        else -> Worker("Ramesh", "Plumbing", "₹400/day", true, "9988776655", 4.9f, R.drawable.ramesh)
    }

    // THE FIX: Added verticalScroll and fillMaxSize
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(worker.name, style = MaterialTheme.typography.titleLarge)
        Text("Skill: ${worker.skill}")
        Text(worker.rate)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {

                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )

                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Call Worker")
        }
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedDate.isEmpty()) "Select Date" else selectedDate)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedTime.isEmpty()) "Select Time" else selectedTime)
        }
        Text("Select Payment Method")

        Spacer(modifier = Modifier.height(10.dp))

        Row {

            Button(
                onClick = {
                    paymentMethod = "Cash"
                }
            ) {
                Text("Cash")
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {

                    paymentMethod = "UPI"

                    android.widget.Toast.makeText(
                        context,
                        "Opening UPI Payment...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Text("UPI")
            }
        }
        Text("Selected: $paymentMethod")
        Spacer(modifier = Modifier.height(30.dp))


        Button(
            onClick = {
                // Debug Log to see if button is physically working
                android.util.Log.d("APP_DEBUG", "Confirm Button Clicked")

                if (customerName.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                    val bookingId = bookingsRef.push().key ?: return@Button
                    val booking = Booking(
                        worker.name,
                        customerName,
                        selectedDate,
                        selectedTime,
                        paymentMethod
                    )

                    bookingsRef.child(bookingId).setValue(booking)
                        .addOnSuccessListener {
                            navController.navigate("bookingSuccess")
                        }
                        .addOnFailureListener { e ->
                            android.widget.Toast.makeText(context, "Firebase Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Tell the user why it didn't work
                    android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Booking")
        }
    }
}


@Composable
fun BookingSuccessScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "🎉 Booking Confirmed!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your worker will contact you soon.")
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate("viewBookings")
            }
        ) {
            Text("View All Bookings")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = {
            navController.navigate("list") {
                popUpTo("list") { inclusive = true }
            }
        }) {
            Text("Back to Home")
        }
    }
}
@Composable
fun ViewBookingsScreen(navController: NavController) {

    val database = FirebaseDatabase.getInstance(
        "https://manekelsa-eeac3-default-rtdb.firebaseio.com/"
    )

    val bookingsRef = database.getReference("bookings")

    var bookingList by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {

        bookingsRef.get().addOnSuccessListener { snapshot ->

            val tempList = mutableListOf<Booking>()

            for (bookingSnapshot in snapshot.children) {

                val booking = bookingSnapshot.getValue(Booking::class.java)

                booking?.let {
                    tempList.add(it)
                }
            }

            bookingList = tempList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "All Bookings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(bookingList) { booking ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text("Worker: ${booking.workerName}")
                        Text("Customer: ${booking.customerName}")
                        Text("Date: ${booking.date}")
                        Text("Time: ${booking.time}")
                        Text("Payment: ${booking.paymentMethod}")
                    }
                }
            }
        }
    }
}