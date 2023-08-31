package br.com.smartagro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: melhorar esse trecho de código para evitar repetição para cada activity
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true

    }
    fun onClickNoticias(view: View) {
        val intent = Intent(this, Noticias::class.java)
        startActivity(intent)
    }

    // TODO: melhorar esse trecho de código para evitar repetição para cada activity
    protected fun setupBottomNavigation(bottomNavigationView: BottomNavigationView,) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_news -> {
                    val intent = Intent(this, Noticias::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

}