package br.com.smartagro

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.smartagro.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityMainBinding
    private var cidadeId: String? = null
    private var cidadeNomeUF: String? = null
    private val previsoesList = ArrayList<Previsao>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val nome = extrairPrimeiroNome(FirebaseAuth.getInstance().currentUser?.displayName)
        binding.txtBemVindo.text = "Olá, $nome!"


        // TODO: melhorar esse trecho de código para evitar repetição para cada activity
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setupBottomNavigation(bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.nav_home).isChecked = true

        previsao()
        buscarPrecoDolar()
        buscarPrecoCafeB3()
        buscarPrecoCafeCEPEA()
        buscarPrecoCafeNYSE()
    }

    fun onClickNoticias(view: View) {
        val intent = Intent(this, Noticias::class.java)
        startActivity(intent)
    }

    fun onClickClima(view: View) {
        val intent = Intent(this, PrevisaoTempoActivity::class.java)
        startActivity(intent)
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

    fun extrairPrimeiroNome(nomeCompleto: String?): String {
        val partesDoNome = nomeCompleto?.trim()?.split(" ")
        if (partesDoNome != null) {
            return if (partesDoNome.isNotEmpty()) {
                partesDoNome[0]
            } else {
                "Cafeicultor"
            }
        }
        return "Cafeicultor"
    }

    //Previsão do tempo

    fun previsao(){
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Inicializar o Firestore
            val db = FirebaseFirestore.getInstance()

            // Refira a coleção de cidades do usuário atual
            val cidadesRef: CollectionReference = db.collection("usuarios").document(userId).collection("cidades")

            // Consultar a primeira cidade da coleção
            cidadesRef.limit(1).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        cidadeNomeUF = document.getString("nome") + " - " + document.getString("uf")
                        cidadeId = document.getString("id")
                        binding.txtCidade.text = cidadeNomeUF
                        previsaoDoDia()
                        break
                    }
                } else {
                    Log.d("Cidade", "Falha ao recuperar os dados da cidade", task.exception)
                }
            }
        }
    }

    fun previsaoDoDia(){
        try {
            val url = "http://servicos.cptec.inpe.br/XML/cidade/7dias/$cidadeId/previsao.xml"
            TarefaPrevisao().execute(url)
        } catch (e: Exception) {
            e.message?.let { Log.e("Erro", it) }
        }
    }

    private inner class TarefaPrevisao : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg strings: String): String {
            val dados = Conexao.getDados(strings[0])
            println("Dados: $dados")
            return dados
        }

        override fun onPostExecute(s: String) {
            previsoesList.clear()
            previsoesList.addAll(ConsumirXML.getPrevisao(s))
            binding.txtTempMax.text = previsoesList[0].maxima + "°C"
            binding.txtTempMin.text = previsoesList[0].minima + "°C"
            binding.txtData.text = converterData(previsoesList[0].dia)
            binding.txtPrevisao.text = SiglaDescricao.converterSiglaParaDescricao(previsoesList[0].tempo)

            val resourceIdTempoCard = resources.getIdentifier(previsoesList[0].tempo, "drawable", packageName)
            binding.imgTempoHome.setImageResource(resourceIdTempoCard)

        }
    }

    fun converterData(dataNoFormatoOriginal: String?): String? {
        return try {
            val formatoEntrada =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSaida =
                SimpleDateFormat("dd/MM", Locale.getDefault())
            val data: Date = formatoEntrada.parse(dataNoFormatoOriginal)
            formatoSaida.format(data)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    fun buscarPrecoDolar() {val db = FirebaseFirestore.getInstance()
        val usdRef = db.collection("usd_prices")

        usdRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        binding.txtDolar.text = valorFormatado
                    } else {
                        binding.txtDolar.text = "N/A"
                    }
                } else {
                    binding.txtDolar.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do dólar: $exception")
            }
    }

    fun buscarPrecoCafeB3() {
        val db = FirebaseFirestore.getInstance()
        val b3Ref = db.collection("coffee_prices_rt").document("B3")
        val b3PricesRef = b3Ref.collection("prices")

        b3PricesRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        binding.txtValorB3.text = "$"+valorFormatado
                    } else {
                        binding.txtValorB3.text = "N/A"
                    }
                } else {
                    binding.txtValorB3.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do café: $exception")
            }
    }

    fun buscarPrecoCafeCEPEA() {
        val db = FirebaseFirestore.getInstance()
        val cepeaRef = db.collection("coffee_prices_rt").document("CEPEA")
        val pricesRef = cepeaRef.collection("prices")

        pricesRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        binding.txtValorCEPEA.text = "R$"+valorFormatado
                    } else {
                        binding.txtValorCEPEA.text = "N/A"
                    }
                } else {
                    binding.txtValorCEPEA.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do café CEPEA: $exception")
            }
    }

    fun buscarPrecoCafeNYSE() {
        val db = FirebaseFirestore.getInstance()
        val nyseRef = db.collection("coffee_prices_rt").document("NYSE")
        val pricesRef = nyseRef.collection("prices") // Referência à subcoleção "prices" dentro de "NYSE"

        pricesRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        binding.txtValorNYSE.text = "$"+valorFormatado
                    } else {
                        binding.txtValorNYSE.text = "N/A"
                    }
                } else {
                    binding.txtValorNYSE.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do café NYSE: $exception")
            }
    }

}


