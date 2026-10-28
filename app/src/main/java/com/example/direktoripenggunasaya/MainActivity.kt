package com.example.direktoripenggunasaya

import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnCobaLagi: Button
    private lateinit var btnPost: Button
    private lateinit var btnHapus: Button
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var daftarUser: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root)
        val pad = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom)
            insets
        }

        tvStatus = findViewById(R.id.tvStatus)
        btnCobaLagi = findViewById(R.id.btnCobaLagi)
        btnPost = findViewById(R.id.btnPost)
        btnHapus = findViewById(R.id.btnHapus)

        listView = findViewById(R.id.listUser)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            muatPostingan(daftarUser[position])
        }

        btnCobaLagi.setOnClickListener { muatUsers() }
        btnPost.setOnClickListener { kirimPostingan() }
        muatUsers()
        btnHapus.setOnClickListener { hapusPostingan() }
    }

    private fun muatUsers() {
        tvStatus.text = "Memuat data..."
        btnCobaLagi.visibility = View.GONE

        // lifecycleScope: coroutine otomatis dibatalkan saat Activity ditutup
        lifecycleScope.launch {
            try {
                val daftar = RetrofitClient.api.getUsers()
                daftarUser = daftar
                adapter.clear()
                adapter.addAll(daftar.map { "${it.name} (@${it.username})" })
                tvStatus.text = "${daftar.size} pengguna dimuat"
            } catch (e: HttpException) {
                // Server menjawab, tapi dengan kode error (4xx / 5xx)
                tvStatus.text = "Server error: HTTP ${e.code()}"
                btnCobaLagi.visibility = View.VISIBLE
            } catch (e: IOException) {
                // Tidak ada jawaban: internet mati, timeout, DNS gagal
                tvStatus.text = "Tidak ada koneksi internet"
                btnCobaLagi.visibility = View.VISIBLE
            }
        }
    }
    private fun kirimPostingan() {
        lifecycleScope.launch {
            try {
                val baru = PostBaru(
                    title = "Belajar Retrofit",
                    body = "Contoh POST dari aplikasi",
                    userId = 1
                )
                val hasil = RetrofitClient.api.buatPost(baru)
                tvStatus.text = "Tersimpan dengan id ${hasil.id}"
            } catch (e: HttpException) {
                tvStatus.text = "Gagal menyimpan: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
    private fun muatPostingan(user: User) {
        tvStatus.text = "Memuat postingan ${user.name}..."

        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.api.getPostsByUser(user.id)
                tvStatus.text = "${posts.size} postingan dari ${user.name}"

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Postingan ${user.name}")
                    .setMessage(posts.joinToString("\n\n") { "• ${it.title}" })
                    .setPositiveButton("Tutup", null)
                    .show()
            } catch (e: HttpException) {
                tvStatus.text = "Gagal memuat postingan: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
    private fun hapusPostingan() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.hapusPost(1)
                if (response.code() == 200) {
                    tvStatus.text = "Postingan berhasil dihapus (HTTP 200)"
                } else {
                    tvStatus.text = "Gagal menghapus: HTTP ${response.code()}"
                }
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
}