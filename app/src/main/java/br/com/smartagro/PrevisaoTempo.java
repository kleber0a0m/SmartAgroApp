package br.com.smartagro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrevisaoTempo extends AppCompatActivity {

    private List<Previsao> previsoesList = new ArrayList<>();
    private  Cidade cidadeSelecionada;
    private int indexAtual = 0;
    ImageView imgTempo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previsao_tempo);
        Intent intent = getIntent();
        //TODO: Verificar
//        cidadeSelecionada = (Cidade) intent.getSerializableExtra("cidade");

//        if(cidadeSelecionada.isUsarLocalizacao()){
//            buscarPrevisaoLocalizacao();
//            TextView lblCidade = findViewById(R.id.lblCidade);
//            lblCidade.setText(cidadeSelecionada.getLatitude() + "  " + cidadeSelecionada.getLongitude());
//
//        }
//        else{
            buscarPrevisao();
            TextView lblCidade = findViewById(R.id.lblCidade);
//           lblCidade.setText(cidadeSelecionada.getNome() + " - " + cidadeSelecionada.getUf());
           lblCidade.setText("Nome cidade");
//        }
    }

    public void buscarPrevisao() {
        // EditText cidade = findViewById(R.id.editCidade);
        //TODO: Remover o id fixo da url
        try {
            String url = "http://servicos.cptec.inpe.br/XML/cidade/7dias/2701/previsao.xml";
            new Tarefa().execute(url);
        }catch (Exception e){
            Log.e("Erro", e.getMessage());
        }
    }

    public void buscarPrevisaoLocalizacao() {
        try {
            String url = cidadeSelecionada.getUrlLocalizacao();
            new Tarefa().execute(url);
        }catch (Exception e){
            Log.e("Erro", e.getMessage());
        }
    }

    public void compartilhar(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Previsão do tempo");

        String data = previsoesList.get(indexAtual).getDia();
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date dataFormatada = formatoEntrada.parse(data);
            String dataFormatadaString = formatoSaida.format(dataFormatada);

            intent.putExtra(Intent.EXTRA_TEXT, "🌤️ Previsão do tempo para: \n" + cidadeSelecionada.getNome() + " - " + cidadeSelecionada.getUf() + "\n\n" +
                    "📅 Data: " + dataFormatadaString + "\n" +
                    "⛅ Tempo: " + SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(indexAtual).getTempo()) + "\n" +
                    "🌡️ Temperatura:  " + "Max: " + previsoesList.get(indexAtual).getMaxima() + "°C" + " - Min: " + previsoesList.get(indexAtual).getMinima() + "°C\n" +
                    "☀️ Índice UV: " + obterClassificacaoUV(previsoesList.get(indexAtual).getIuv()));

            startActivity(Intent.createChooser(intent, "Compartilhar"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private class Tarefa  extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String dados = Conexao.getDados(strings[0]);
            System.out.println("Dados: " + dados);
            return dados;
        }

        @Override
        protected void onPostExecute(String s) {
            //TODO: Verificar
//            if(cidadeSelecionada.isUsarLocalizacao()){
//                List<Cidade> cidades;
//                cidades = ConsumirXML.getCidade(s);
//                TextView lblCidade = findViewById(R.id.lblCidade);
//                lblCidade.setText(cidades.get(0).getNome() + " - " + cidades.get(0).getUf());
//            }
            previsoesList = ConsumirXML.getPrevisao(s);
            TextView txtDia = findViewById(R.id.txtDia);
            TextView txtDiaMes = findViewById(R.id.txtDiaMes);
            TextView txtTempo = findViewById(R.id.txtTempo);
            TextView txtUV = findViewById(R.id.txtUV);
            TextView txtMax = findViewById(R.id.txtMax);
            TextView txtMin= findViewById(R.id.txtMin);


            txtDia.setText(obterDiaDaSemana(previsoesList.get(indexAtual).getDia()));
            txtDiaMes.setText(obterDiaMes(previsoesList.get(indexAtual).getDia()));
            txtTempo.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(indexAtual).getTempo()));

            imgTempo = findViewById(R.id.imgTempo);
            int resourceId = getResources().getIdentifier(previsoesList.get(indexAtual).getTempo(), "drawable", getPackageName());
            imgTempo.setImageResource(resourceId);

            txtUV.setText(obterClassificacaoUV(previsoesList.get(indexAtual).getIuv()));
            txtMax.setText(previsoesList.get(indexAtual).getMaxima()+" ºC");
            txtMin.setText(previsoesList.get(indexAtual).getMinima()+" ºC");

            AtualizarCards();
        }
    }

    public void AtualizarCards() {
        // Card 01
        TextView txtDiaCard_01 = findViewById(R.id.txtDiaCard_01);
        TextView txtMesCard_01 = findViewById(R.id.txtMesCard_01);
        TextView txtTempoCard_01 = findViewById(R.id.txtTempoCard_01);
        TextView txtUvCard_01 = findViewById(R.id.txtUvCard_01);
        ImageView imgTempoCard_01 = findViewById(R.id.imgTempoCard_01);

        txtDiaCard_01.setText(obterDia(previsoesList.get(0).getDia()));
        txtMesCard_01.setText(obterMes(previsoesList.get(0).getDia()));
        txtTempoCard_01.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(0).getTempo()));
        txtUvCard_01.setText(obterClassificacaoUV(previsoesList.get(0).getIuv()));

        int resourceIdTempoCard_01 = getResources().getIdentifier(previsoesList.get(0).getTempo(), "drawable", getPackageName());
        imgTempoCard_01.setImageResource(resourceIdTempoCard_01);

        // Card 02
        TextView txtDiaCard_02 = findViewById(R.id.txtDiaCard_02);
        TextView txtMesCard_02 = findViewById(R.id.txtMesCard_02);
        TextView txtTempoCard_02 = findViewById(R.id.txtTempoCard_02);
        TextView txtUvCard_02 = findViewById(R.id.txtUvCard_02);
        ImageView imgTempoCard_02 = findViewById(R.id.imgTempoCard_02);

        txtDiaCard_02.setText(obterDia(previsoesList.get(1).getDia()));
        txtMesCard_02.setText(obterMes(previsoesList.get(1).getDia()));
        txtTempoCard_02.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(1).getTempo()));
        txtUvCard_02.setText(obterClassificacaoUV(previsoesList.get(1).getIuv()));

        int resourceIdTempoCard_02 = getResources().getIdentifier(previsoesList.get(1).getTempo(), "drawable", getPackageName());
        imgTempoCard_02.setImageResource(resourceIdTempoCard_02);

        // Card 03
        TextView txtDiaCard_03 = findViewById(R.id.txtDiaCard_03);
        TextView txtMesCard_03 = findViewById(R.id.txtMesCard_03);
        TextView txtTempoCard_03 = findViewById(R.id.txtTempoCard_03);
        TextView txtUvCard_03 = findViewById(R.id.txtUvCard_03);
        ImageView imgTempoCard_03 = findViewById(R.id.imgTempoCard_03);

        txtDiaCard_03.setText(obterDia(previsoesList.get(2).getDia()));
        txtMesCard_03.setText(obterMes(previsoesList.get(2).getDia()));
        txtTempoCard_03.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(2).getTempo()));
        txtUvCard_03.setText(obterClassificacaoUV(previsoesList.get(2).getIuv()));

        int resourceIdTempoCard_03 = getResources().getIdentifier(previsoesList.get(2).getTempo(), "drawable", getPackageName());
        imgTempoCard_03.setImageResource(resourceIdTempoCard_03);

        // Card 04
        TextView txtDiaCard_04 = findViewById(R.id.txtDiaCard_04);
        TextView txtMesCard_04 = findViewById(R.id.txtMesCard_04);
        TextView txtTempoCard_04 = findViewById(R.id.txtTempoCard_04);
        TextView txtUvCard_04 = findViewById(R.id.txtUvCard_04);
        ImageView imgTempoCard_04 = findViewById(R.id.imgTempoCard_04);

        txtDiaCard_04.setText(obterDia(previsoesList.get(3).getDia()));
        txtMesCard_04.setText(obterMes(previsoesList.get(3).getDia()));
        txtTempoCard_04.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(3).getTempo()));
        txtUvCard_04.setText(obterClassificacaoUV(previsoesList.get(3).getIuv()));

        int resourceIdTempoCard_04 = getResources().getIdentifier(previsoesList.get(3).getTempo(), "drawable", getPackageName());
        imgTempoCard_04.setImageResource(resourceIdTempoCard_04);

        // Card 05
        TextView txtDiaCard_05 = findViewById(R.id.txtDiaCard_05);
        TextView txtMesCard_05 = findViewById(R.id.txtMesCard_05);
        TextView txtTempoCard_05 = findViewById(R.id.txtTempoCard_05);
        TextView txtUvCard_05 = findViewById(R.id.txtUvCard_05);
        ImageView imgTempoCard_05 = findViewById(R.id.imgTempoCard_05);

        txtDiaCard_05.setText(obterDia(previsoesList.get(4).getDia()));
        txtMesCard_05.setText(obterMes(previsoesList.get(4).getDia()));
        txtTempoCard_05.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(4).getTempo()));
        txtUvCard_05.setText(obterClassificacaoUV(previsoesList.get(4).getIuv()));

        int resourceIdTempoCard_05 = getResources().getIdentifier(previsoesList.get(4).getTempo(), "drawable", getPackageName());
        imgTempoCard_05.setImageResource(resourceIdTempoCard_05);

        // Card 06
        TextView txtDiaCard_06 = findViewById(R.id.txtDiaCard_06);
        TextView txtMesCard_06 = findViewById(R.id.txtMesCard_06);
        TextView txtTempoCard_06 = findViewById(R.id.txtTempoCard_06);
        TextView txtUvCard_06 = findViewById(R.id.txtUvCard_06);
        ImageView imgTempoCard_06 = findViewById(R.id.imgTempoCard_06);

        txtDiaCard_06.setText(obterDia(previsoesList.get(5).getDia()));
        txtMesCard_06.setText(obterMes(previsoesList.get(5).getDia()));
        txtTempoCard_06.setText(SiglaDescricao.converterSiglaParaDescricao(previsoesList.get(5).getTempo()));
        txtUvCard_06.setText(obterClassificacaoUV(previsoesList.get(5).getIuv()));

        int resourceIdTempoCard_06 = getResources().getIdentifier(previsoesList.get(5).getTempo(), "drawable", getPackageName());
        imgTempoCard_06.setImageResource(resourceIdTempoCard_06);
    }

    public static String obterDia(String data) {
        if(data.equals("null") || data.isEmpty()){
            return "";
        }
        LocalDate localDate = LocalDate.parse(data);
        int dia = localDate.getDayOfMonth();
        return String.valueOf(dia);
    }

    public static String obterMes(String data) {
        if(data.equals("null") || data.isEmpty()){
            return "";
        }
        LocalDate localDate = LocalDate.parse(data);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", new Locale("pt", "BR"));
        return localDate.format(formatter);
    }

    public static String obterDiaMes(String data) {
        if(data.equals("null") || data.isEmpty()){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = sdf.parse(data);
            calendar.setTime(date);

            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            int mes = calendar.get(Calendar.MONTH);

            DateFormatSymbols dfs = new DateFormatSymbols(new Locale("pt", "BR"));
            String[] meses = dfs.getMonths();

            String mesFormatado = meses[mes];

            return String.format("%02d %s", dia, mesFormatado);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String obterDiaDaSemana(String data) {
        if(data.equals("null") || data.isEmpty()){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = sdf.parse(data);
            calendar.setTime(date);

            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] diasDaSemana = dfs.getShortWeekdays();

            int diaDaSemana = calendar.get(Calendar.DAY_OF_WEEK);

            return diasDaSemana[diaDaSemana].toUpperCase();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String obterClassificacaoUV(String indiceUV) {
        Double indiceUVDouble = Double.parseDouble(indiceUV);
        if (indiceUVDouble < 3.0) {
            return "Baixo ("+indiceUV+")";
        } else if (indiceUVDouble < 6.0) {
            return "Moderado ("+indiceUV+")";
        } else if (indiceUVDouble < 8.0) {
            return "Alto ("+indiceUV+")";
        } else if (indiceUVDouble < 11.0) {
            return "Muito Alto ("+indiceUV+")";
        } else {
            return "Extremo ("+indiceUV+")";
        }
    }

    public void alterarIndex0(View view) {
        indexAtual = 0;
        buscarPrevisao();
    }

    public void alterarIndex1(View view) {
        indexAtual = 1;
        buscarPrevisao();
    }

    public void alterarIndex2(View view) {
        indexAtual = 2;
        buscarPrevisao();
    }

    public void alterarIndex3(View view) {
        indexAtual = 3;
        buscarPrevisao();
    }

    public void alterarIndex4(View view) {
        indexAtual = 4;
        buscarPrevisao();
    }

    public void alterarIndex5(View view) {
        indexAtual = 5;
        buscarPrevisao();
    }

    public void voltar(View view) {
        cidadeSelecionada.setUsarLocalizacao(false);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cidadeId");
        editor.remove("cidadeNome");
        editor.remove("cidadeUf");
        editor.commit();
        //Intent intent = new Intent(this, BuscaCidade.class);
        //startActivity(intent);
        //TODO: Arrumar botão
    }

}