package br.com.smartagro

import ClimaFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import br.com.smartagro.fragments.HomeFragment
import br.com.smartagro.fragments.NoticiasFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class PrincipalActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val noticiasFragment = NoticiasFragment()
    private val climaFragment = ClimaFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        makeCurrentFragment(homeFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> makeCurrentFragment(homeFragment)
                R.id.nav_news -> makeCurrentFragment(noticiasFragment)
                R.id.nav_clima -> makeCurrentFragment(climaFragment)
            }
            true
        }
    }

     fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }


    companion object {
        fun makeCurrentFragment(fragment: Fragment) {
            PrincipalActivity.makeCurrentFragment(fragment)
        }
    }
}
