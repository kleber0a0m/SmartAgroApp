package br.com.smartagro

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


class Noticias : AppCompatActivity() {
    private lateinit var binding: ActivityNoticiasBinding
    private lateinit var newsAdapterCafePoint: NewsAdapterCafePoint
    private lateinit var newsAdapterCCCMG: NewsAdapterCCCMG
    private lateinit var rssParserCafePoint: RssParserCafePoint
    private lateinit var rssParserCCCMG: RssParserCCCMG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticiasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rssParserCafePoint = RssParserCafePoint("https://www.cafepoint.com.br/rss/")
        rssParserCCCMG = RssParserCCCMG("https://cccmg.com.br/category/noticias/feed/")

        showLoadingIndicator(true)

        var noticiasCarregadas = false;

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

        binding.chipGroupRss.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipCafePoint -> {
                    if(noticiasCarregadas){
                        binding.recyclerView.adapter = newsAdapterCafePoint
                        newsAdapterCafePoint.notifyDataSetChanged()
                    }else {
                        Toast.makeText(this@Noticias, "Carregando notícias...", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.chipCCCMG -> {
                    if(noticiasCarregadas){
                        binding.recyclerView.adapter = newsAdapterCCCMG
                        newsAdapterCCCMG.notifyDataSetChanged()
                    }else {
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
}

