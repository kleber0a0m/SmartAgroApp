package br.com.smartagro.fragments

import ClimaFragment
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBindings
import br.com.smartagro.clima.Conexao
import br.com.smartagro.clima.ConsumirXML
import br.com.smartagro.clima.Previsao
import br.com.smartagro.PrincipalActivity
import br.com.smartagro.R
import br.com.smartagro.clima.SiglaDescricao
import br.com.smartagro.databinding.FragmentHomeBinding
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

class HomeFragment : Fragment() {

    private var cidadeId: String? = null
    private var cidadeNomeUF: String? = null
    private val previsoesList = ArrayList<Previsao>()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val nome = extrairPrimeiroNome(FirebaseAuth.getInstance().currentUser?.displayName)
        binding.txtBemVindo.text = "Olá, $nome!"

        binding.cardClima.setOnClickListener {
            val climaFragment = ClimaFragment()
            (activity as PrincipalActivity).makeCurrentFragment(climaFragment)
            //TODO: Fazer com que o BottomNavigationView fique selecionado na opção "Clima" quando o usuário clicar no cardClima
        }

        previsao()
        buscarPrecoDolar()
        buscarPrecoCafeB3()
        buscarPrecoCafeCEPEA()
        buscarPrecoCafeNYSE()

        return view
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

    fun previsao(){
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val db = FirebaseFirestore.getInstance()

            val cidadesRef: CollectionReference = db.collection("usuarios").document(userId).collection("cidade")

            cidadesRef.limit(1).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        cidadeNomeUF = document.getString("nome") + " - " + document.getString("uf")
                        cidadeId = document.getString("id")
                        var cidade = ViewBindings.findChildViewById<TextView>(view, R.id.txtCidade)
                        cidade?.text = cidadeNomeUF

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
//          val url = "https://gist.githubusercontent.com/kleber0a0m0/738376b6d7616702448ace751425e05a/raw/2cec2d51ea07cfea40adb576f0460e397bf85d58/inpe.xml"
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
            var txtTempMax = ViewBindings.findChildViewById<TextView>(view, R.id.txtTempMax)
            var txtTempMin = ViewBindings.findChildViewById<TextView>(view, R.id.txtTempMin)
            var txtData = ViewBindings.findChildViewById<TextView>(view, R.id.txtData)
            var txtPrevisao = ViewBindings.findChildViewById<TextView>(view, R.id.txtPrevisao)
            var imgTempoHome = ViewBindings.findChildViewById<ImageView>(view, R.id.imgTempoHome2)
            if (isAdded) {
                previsoesList.clear()
                previsoesList.addAll(ConsumirXML.getPrevisao(s))
                txtTempMax?.text = previsoesList[0].maxima + "°C"
                txtTempMin?.text = previsoesList[0].minima + "°C"
                txtData?.text = converterData(previsoesList[0].dia)
                txtPrevisao?.text = SiglaDescricao.converterSiglaParaDescricao(previsoesList[0].tempo)

                val resourceIdTempoCard = resources.getIdentifier(previsoesList[0].tempo, "drawable", context?.packageName)
                imgTempoHome?.setImageResource(resourceIdTempoCard)
            }
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

    fun buscarPrecoDolar() {
        val db = FirebaseFirestore.getInstance()
        val usdRef = db.collection("usd_prices")

        usdRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                var dolar = ViewBindings.findChildViewById<TextView>(view, R.id.txtDolar)
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        dolar?.text = valorFormatado

                    } else {
                        dolar?.text = "N/A"
                    }
                } else {
                    dolar?.text = "N/A"
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
                var b3 = ViewBindings.findChildViewById<TextView>(view, R.id.txtValorB3)
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        b3?.text = valorFormatado
                    } else {
                        b3?.text = "N/A"
                    }
                } else {
                    b3?.text = "N/A"
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
                var cepea = ViewBindings.findChildViewById<TextView>(view, R.id.txtValorCEPEA)
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        cepea?.text = "R$"+valorFormatado
                    } else {
                        cepea?.text = "N/A"
                    }
                } else {
                    cepea?.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do café CEPEA: $exception")
            }
    }

    fun buscarPrecoCafeNYSE() {
        val db = FirebaseFirestore.getInstance()
        val nyseRef = db.collection("coffee_prices_rt").document("NYSE")
        val pricesRef = nyseRef.collection("prices")

        pricesRef
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                var nyse = ViewBindings.findChildViewById<TextView>(view, R.id.txtValorNYSE)
                if (!documents.isEmpty) {
                    val latestDocument = documents.documents[0]
                    val latestValue = latestDocument.get("value")
                    if (latestValue != null) {
                        val latestValueString = latestValue.toString()
                        val valorString = latestValueString.replace(",", ".")
                        val valorDouble: Double = valorString.toDouble()
                        val df = DecimalFormat("#.00")
                        val valorFormatado = df.format(valorDouble)

                        nyse?.text = "$"+valorFormatado
                    } else {
                        nyse?.text = "N/A"
                    }
                } else {
                    nyse?.text = "N/A"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Erro ao buscar preço do café NYSE: $exception")
            }
    }
}