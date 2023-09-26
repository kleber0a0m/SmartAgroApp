package br.com.smartagro.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.smartagro.R
import br.com.smartagro.databinding.FragmentNoticiasBinding
import br.com.smartagro.noticias.cafepoint.NewsAdapterCCCMG
import br.com.smartagro.noticias.cafepoint.NewsAdapterCafePoint
import br.com.smartagro.noticias.cafepoint.RssParserCCCMG
import br.com.smartagro.noticias.cafepoint.RssParserCafePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [NoticiasFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoticiasFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentNoticiasBinding
    private lateinit var newsAdapterCafePoint: NewsAdapterCafePoint
    private lateinit var newsAdapterCCCMG: NewsAdapterCCCMG
    private lateinit var rssParserCafePoint: RssParserCafePoint
    private lateinit var rssParserCCCMG: RssParserCCCMG
    private var noticiasCarregadas = false

    private fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?) {
        if (!::binding.isInitialized) {
            binding = FragmentNoticiasBinding.inflate(inflater, container, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setupRssFeeds()
        setupChipGroupRss()


//        binding.imgVoltar.setOnClickListener {
//            //mudar o fragment
//            val noticiasFragment = NoticiasFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.home, noticiasFragment)
//            transaction.commit()
//        } TODO: Arrumar
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initializeBinding(inflater, container)
        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NoticiasFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NoticiasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    private fun setupRssFeeds() {
        initializeBinding(layoutInflater, null)
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
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar os feeds RSS", Toast.LENGTH_SHORT).show()
            } finally {
                showLoadingIndicator(false)
                noticiasCarregadas = true
                binding.chipGroupRss.check(R.id.chipCafePoint)
            }
        }
    }

    private fun setupChipGroupRss() {
        initializeBinding(layoutInflater, null)
        binding.chipGroupRss.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipCafePoint -> {
                    if (noticiasCarregadas) {
                        binding.recyclerView.adapter = newsAdapterCafePoint
                        newsAdapterCafePoint.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Carregando notícias...", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.chipCCCMG -> {
                    if (noticiasCarregadas) {
                        binding.recyclerView.adapter = newsAdapterCCCMG
                        newsAdapterCCCMG.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Carregando notícias...", Toast.LENGTH_SHORT).show()
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