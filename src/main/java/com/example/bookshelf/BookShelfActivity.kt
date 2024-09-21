package com.example.bookshelf

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BookShelfActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookshelfScreen()
        }
    }
}
@Composable
fun BookshelfScreen() {
    var books by rememberSaveable { mutableStateOf(listOf<Book>()) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }
    var booksFetched by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // Fetch books from the API if they haven't been fetched yet
    LaunchedEffect(Unit) {
        if (!booksFetched) {
            BookRetrofitInstance.api.getBooks().enqueue(object : Callback<List<Book>> {
                override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            books = it
                            booksFetched = true  // Mark books as fetched
                            // Set the default selected year to the most recent year
                            selectedYear = books.maxByOrNull { book -> book.publishedChapterDate }?.let {
                                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                                sdf.format(Date(it.publishedChapterDate * 1000L)).toInt()
                            }
                        } ?: run {
                            Toast.makeText(context, "No books found", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to fetch books", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // Group books by published year
    val booksByYear = books.groupBy { book ->
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        sdf.format(Date(book.publishedChapterDate * 1000L)).toInt()
    }

    // Sort years in descending order
    val sortedYears = booksByYear.keys.sortedDescending()

    if (sortedYears.isEmpty()) {
        // Handle the case where no books are available
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No books available")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title and Logout button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Activity name at the top center
                Text("Bookshelf", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))

                // Logout button at the top-right
                IconButton(
                    onClick = {
                        // Navigate back to MainActivity
                        (context as? ComponentActivity)?.finish()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Year tabs
            ScrollableTabRow(
                selectedTabIndex = sortedYears.indexOf(selectedYear ?: sortedYears.first()),
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                sortedYears.forEach { year ->
                    Tab(
                        selected = year == selectedYear,
                        onClick = { selectedYear = year },
                        text = { Text(year.toString()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book list for the selected year
            LazyColumn {
                val booksForYear = booksByYear[selectedYear ?: sortedYears.first()] ?: emptyList()
                items(booksForYear) { book ->
                    BookItem(book)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun BookItem(book: Book) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Load book thumbnail using Coil
        Image(
            painter = rememberImagePainter(book.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(book.title, fontWeight = FontWeight.Bold)
            Text("Score: ${book.score}")
            Text("Popularity: ${book.popularity}")
        }
    }
}