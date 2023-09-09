package br.com.smartagro

import android.content.Intent
import br.com.smartagro.noticias.cafepoint.RssParserCafePoint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.smartagro.databinding.ActivityNoticiasBinding
import br.com.smartagro.noticias.cafepoint.NewsAdapterCCCMG
import br.com.smartagro.noticias.cafepoint.RssParserCCCMG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import br.com.smartagro.noticias.cafepoint.NewsAdapterCafePoint
import com.google.android.material.bottomnavigation.BottomNavigationView


class Noticias : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasBinding
    private lateinit var newsAdapterCafePoint: NewsAdapterCafePoint
    private lateinit var newsAdapterCCCMG: NewsAdapterCCCMG
    private lateinit var rssParserCafePoint: RssParserCafePoint
    private lateinit var rssParserCCCMG: RssParserCCCMG
    private var noticiasCarregadas = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRssFeeds()
        setupChipGroupRss()
        setupBottomNavigation(binding.bottomNavigation)

        // TODO: melhorar esse trecho de código para evitar repetição para cada activity
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_news).isChecked = true

        binding.imgVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRssFeeds() {
        rssParserCafePoint = RssParserCafePoint("https://www.cafepoint.com.br/rss/")
        rssParserCCCMG = RssParserCCCMG("https://cccmg.com.br/category/noticias/feed/")

        showLoadingIndicator(true)

        MainScope().launch(Dispatchers.Main) {
            try {
                val rssItemsCCCMG = rssParserCCCMG.fetchRssItems()
                val rssItemsCafePoint = rssParserCafePoint.fetchRssItems()

                newsAdapterCafePoint = NewsAdapterCafePoint(rssItemsCafePoint)
                newsAdapterCCCMG = NewsAdapterCCCMG(rssItemsCCCMG)

                binding.recyclerView.adapter = newsAdapterCafePoint
                binding.recyclerView.layoutManager = LinearLayoutManager(this@Noticias)
            } catch (e: Exception) {
                Toast.makeText(this@Noticias, "Erro ao carregar os feeds RSS", Toast.LENGTH_SHORT).show()
            } finally {
                showLoadingIndicator(false)
                noticiasCarregadas = true
                binding.chipGroupRss.check(R.id.chipCafePoint)
            }
        }
    }

    private fun setupChipGroupRss() {
        binding.chipGroupRss.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipCafePoint -> {
                    if (noticiasCarregadas) {
                        binding.recyclerView.adapter = newsAdapterCafePoint
                        newsAdapterCafePoint.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@Noticias, "Carregando notícias...", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.chipCCCMG -> {
                    if (noticiasCarregadas) {
                        binding.recyclerView.adapter = newsAdapterCCCMG
                        newsAdapterCCCMG.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@Noticias, "Carregando notícias...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun showLoadingIndicator(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    // TODO: melhorar esse trecho de código para evitar repetição para cada activity
    protected fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
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

                R.id.nav_clima -> {
                    val intent = Intent(this, PrevisaoTempoActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
}

