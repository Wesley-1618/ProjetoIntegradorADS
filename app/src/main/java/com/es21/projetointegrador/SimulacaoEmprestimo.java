package com.es21.projetointegrador;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.es21.projetointegrador.http.HttpHelperLoja;
import com.es21.projetointegrador.http.HttpHelperSimulacao;
import com.es21.projetointegrador.http.HttpHelperUsuarioLoja;
import com.es21.projetointegrador.http.JsonParse;
import com.es21.projetointegrador.model.Loja;
import com.es21.projetointegrador.model.Simulacao;
import com.es21.projetointegrador.model.UsuarioLoja;
import com.example.projetointegrador.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SimulacaoEmprestimo extends AppCompatActivity {
    final Double tarifa = 0.017;
    final Double iofFixo = 0.0038;
    final Double iofRotativo = 0.0025;
    Button btSimulTSP, btVoltarTSP, btConfirmaTSP;
    ImageButton btDataTSE;
    TextInputEditText edtValorTSP, edtTarifaTSP, edtCetTSP, edtIofTSP, edtRendaTSP, edtValorTotalTSP, edtValorParcTSP;
    static TextInputLayout edtDataTSP;
    static String edtDataTSPAux = "";
    Spinner spnFinanTSP, spnParcelasTSP;
    Double cet, cetPrint, iofFinal, iofPrint, tarifaPrint, valorParcela, valorFinal, valorInicial;
    int qtdParcelas;

    String[] parcelas = new String[]{"Selecione as Parcelas", "12", "24", "36", "48", "60"};
    private String auxTarifaPrint, auxCetPrint, auxIofPrint, auxValorInicial, auxValorParcela, auxValorFinal;

    private static ArrayList<String> preencherDados(String s) {

        ArrayList<String> novaLista = new ArrayList<>();

        List<Loja> listaLoja;
        listaLoja = JsonParse.JsonToListLoja(s);
        for (int x = 0; x < listaLoja.size(); x++) {

            novaLista.add(listaLoja.get(x).getRazao_social());

        }
        return novaLista;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulacao_emprestimo);
        setTitle("Simulação Emprestimo");

        inicializaComponentes();
        escolhaPar();

        SimulacaoEmprestimo.TarefaLojaAll tarefaLojaAll = new SimulacaoEmprestimo.TarefaLojaAll();
        tarefaLojaAll.execute();

        btSimulTSP.setOnClickListener((view -> {
            if (!validaSimula()) {
                definirParcela();
            }
        }));

        btDataTSE.setOnClickListener(v -> {
            DialogFragment dialogFragment = new DatePicker();
            dialogFragment.show(getSupportFragmentManager(), "DataInicial");
        });

        btConfirmaTSP.setOnClickListener((view -> {
            if (!validaDados()) {
                TarefaPostSimulacao tarefa = new TarefaPostSimulacao();
                tarefa.execute();
            }
        }));

        btVoltarTSP.setOnClickListener(view -> finish());

    }

    @SuppressLint("StaticFieldLeak")
    private class TarefaLojaAll extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            HttpHelperLoja controleUsuario = new HttpHelperLoja();
            return controleUsuario.getLojaAll();
        }

        @Override
        protected void onPostExecute(String s) {

            ArrayList<String> preenche = preencherDados(s);

            ArrayAdapter<String> arrayAdapter;
            arrayAdapter = new ArrayAdapter<>(SimulacaoEmprestimo.this, android.R.layout.simple_list_item_1, preenche);
            spnFinanTSP.setAdapter(arrayAdapter);

            btSimulTSP.setOnClickListener((view -> {
                if (!validaSimula()) {
                    definirParcela();
                }
            }));

            btDataTSE.setOnClickListener(v -> {
                DialogFragment dialogFragment = new DatePicker();
                dialogFragment.show(getSupportFragmentManager(), "DataInicial");
            });

            btConfirmaTSP.setOnClickListener((view -> {
                if (!validaDados()) {
                    TarefaPostSimulacao tarefa = new TarefaPostSimulacao();
                    tarefa.execute();
                }
            }));

            btVoltarTSP.setOnClickListener(view -> finish());

        }
    }

    private class TarefaPostSimulacao extends AsyncTask<String, String, String> {
        String cpf = getIntent().getStringExtra("cpf");

        @Override
        protected String doInBackground(String... strings) {
            try {
                HttpHelperSimulacao helperSimulacao = new HttpHelperSimulacao();
                return helperSimulacao.postSimulacao(new Simulacao(
                        cpf,
                        spnFinanTSP.getSelectedItem().toString(),
                        Double.parseDouble(edtRendaTSP.getText().toString()),
                        Double.parseDouble(auxValorInicial),
                        tarifaPrint,
                        qtdParcelas,
                        cetPrint,
                        iofPrint,
                        valorFinal,
                        edtDataTSPAux
                ));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            AlertDialog.Builder alerta = new AlertDialog.Builder(SimulacaoEmprestimo.this);
            alerta.setTitle("Simulação");
            alerta.setCancelable(false);

            try {
                if (s != null) {
                    new TarefaPostUsuarioLoja().execute();
                } else {
                    alerta.setMessage("Error ao enviar simulação, tente novamente").setNeutralButton("ok", null).show();
                }
            } catch (Exception e) {
                alerta.setMessage("Error ao enviar simulação").setNeutralButton("ok", null).show();
            }
        }
    }

    private class TarefaPostUsuarioLoja extends  AsyncTask<String,String,String>{
        String cpf = getIntent().getStringExtra("cpf");

        @Override
        protected String doInBackground(String... strings) {
            try {
                HttpHelperLoja helperLoja = new HttpHelperLoja();
                String loja = helperLoja.getLoja(spnFinanTSP.getSelectedItem().toString());
                Loja lojaObjt = JsonParse.JsonToLoja(loja);
                Date hora = new Date();

                HttpHelperUsuarioLoja helperUsuarioLoja = new HttpHelperUsuarioLoja();
                return helperUsuarioLoja.postUsuarioLoja(new UsuarioLoja(
                        cpf,
                        lojaObjt.getCnpj_loja(),
                        "Simulacao",
                        "2021-12-01:00:00"
                ));
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            AlertDialog.Builder alerta = new AlertDialog.Builder(SimulacaoEmprestimo.this);
            alerta.setTitle("Simulação");
            alerta.setCancelable(false);

            Intent menu = new Intent(SimulacaoEmprestimo.this, Menu.class);
            menu.putExtra("cpf", cpf);

            try {
                alerta.setMessage("Simulação enviada com sucesso!")
                        .setPositiveButton("ok", (dialogInterface, i) -> startActivity(menu))
                        .create()
                        .show();
                limpaCampos();
            } catch (Exception e) {
                alerta.setMessage("Error ao enviar simulação").setNeutralButton("ok", null).show();
            }
        }
    }

    private void inicializaComponentes() {

        btSimulTSP = findViewById(R.id.btSimulTSP);
        btVoltarTSP = findViewById(R.id.btVoltarTSP);
        btConfirmaTSP = findViewById(R.id.btConfirmaTSP);
        btDataTSE = findViewById(R.id.btDataTSE);

        edtValorTSP = findViewById(R.id.edtValorTSP);
        edtTarifaTSP = findViewById(R.id.edtTarifaTSP);
        edtCetTSP = findViewById(R.id.edtCetTSP);
        edtDataTSP = findViewById(R.id.edtDataTSP);
        edtIofTSP = findViewById(R.id.edtIofTSP);
        edtRendaTSP = findViewById(R.id.edtRendaTSP);
        edtValorTotalTSP = findViewById(R.id.edtValorTotalTSP);
        edtValorParcTSP = findViewById(R.id.edtValorParcTSP);

        spnFinanTSP = findViewById(R.id.spnFinanTSP);
        spnParcelasTSP = findViewById(R.id.spnParcelasTSP);

    }

    private void escolhaPar() {

        spnParcelasTSP.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.textview_spinner, parcelas));

    }

    private void limpaCampos() {

        edtValorTSP.setText("");
        edtTarifaTSP.setText("");
        edtCetTSP.setText("");
        edtIofTSP.setText("");

        spnFinanTSP.setSelection(0);
        spnParcelasTSP.setSelection(0);

    }

    private void definirParcela() {
        auxValorInicial = edtValorTSP.getText().toString();
        valorInicial = Double.parseDouble(auxValorInicial);
        if (spnParcelasTSP.getSelectedItem().toString().equals("12")) {

            qtdParcelas = 12;

            tarifaPrint = tarifa * 100;

            iofFinal = iofFixo + (iofRotativo * qtdParcelas);
            iofPrint = iofFinal * 100;

            cet = iofFinal + tarifa;
            cetPrint = cet * 100;

            valorFinal = valorInicial + (valorInicial * cet);
            valorParcela = valorFinal / qtdParcelas;

            auxTarifaPrint = String.format("%.2f", tarifaPrint);
            auxIofPrint = String.format("%.2f", iofPrint);
            auxCetPrint = String.format("%.2f", cetPrint);
            auxValorParcela = String.format("%.2f", valorParcela);
            auxValorFinal = String.format("%.2f", valorFinal);

            edtTarifaTSP.setText(auxTarifaPrint);
            edtIofTSP.setText(auxIofPrint);
            edtCetTSP.setText(auxCetPrint);
            edtValorParcTSP.setText(auxValorParcela);
            edtValorTotalTSP.setText(auxValorFinal);

        } else if (spnParcelasTSP.getSelectedItem().toString().equals("24")) {

            qtdParcelas = 24;

            tarifaPrint = tarifa * 100;

            iofFinal = iofFixo + (iofRotativo * qtdParcelas);
            iofPrint = iofFinal * 100;

            cet = iofFinal + tarifa;
            cetPrint = cet * 100;

            valorFinal = valorInicial + (valorInicial * cet);
            valorParcela = valorFinal / qtdParcelas;

            auxTarifaPrint = String.format("%.2f", tarifaPrint);
            auxIofPrint = String.format("%.2f", iofPrint);
            auxCetPrint = String.format("%.2f", cetPrint);
            auxValorParcela = String.format("%.2f", valorParcela);
            auxValorFinal = String.format("%.2f", valorFinal);

            edtTarifaTSP.setText(auxTarifaPrint);
            edtIofTSP.setText(auxIofPrint);
            edtCetTSP.setText(auxCetPrint);
            edtValorParcTSP.setText(auxValorParcela);
            edtValorTotalTSP.setText(auxValorFinal);

        } else if (spnParcelasTSP.getSelectedItem().toString().equals("36")) {

            qtdParcelas = 36;

            tarifaPrint = tarifa * 100;

            iofFinal = iofFixo + (iofRotativo * qtdParcelas);
            iofPrint = iofFinal * 100;

            cet = iofFinal + tarifa;
            cetPrint = cet * 100;

            valorFinal = valorInicial + (valorInicial * cet);
            valorParcela = valorFinal / qtdParcelas;

            auxTarifaPrint = String.format("%.2f", tarifaPrint);
            auxIofPrint = String.format("%.2f", iofPrint);
            auxCetPrint = String.format("%.2f", cetPrint);
            auxValorParcela = String.format("%.2f", valorParcela);
            auxValorFinal = String.format("%.2f", valorFinal);

            edtTarifaTSP.setText(auxTarifaPrint);
            edtIofTSP.setText(auxIofPrint);
            edtCetTSP.setText(auxCetPrint);
            edtValorParcTSP.setText(auxValorParcela);
            edtValorTotalTSP.setText(auxValorFinal);

        } else if (spnParcelasTSP.getSelectedItem().toString().equals("48")) {

            qtdParcelas = 48;

            tarifaPrint = tarifa * 100;

            iofFinal = iofFixo + (iofRotativo * qtdParcelas);
            iofPrint = iofFinal * 100;

            cet = iofFinal + tarifa;
            cetPrint = cet * 100;

            valorFinal = valorInicial + (valorInicial * cet);
            valorParcela = valorFinal / qtdParcelas;

            auxTarifaPrint = String.format("%.2f", tarifaPrint);
            auxIofPrint = String.format("%.2f", iofPrint);
            auxCetPrint = String.format("%.2f", cetPrint);
            auxValorParcela = String.format("%.2f", valorParcela);
            auxValorFinal = String.format("%.2f", valorFinal);

            edtTarifaTSP.setText(auxTarifaPrint);
            edtIofTSP.setText(auxIofPrint);
            edtCetTSP.setText(auxCetPrint);
            edtValorParcTSP.setText(auxValorParcela);
            edtValorTotalTSP.setText(auxValorFinal);

        } else if (spnParcelasTSP.getSelectedItem().toString().equals("60")) {

            qtdParcelas = 60;

            tarifaPrint = tarifa * 100;

            iofFinal = iofFixo + (iofRotativo * qtdParcelas);
            iofPrint = iofFinal * 100;

            cet = iofFinal + tarifa;
            cetPrint = cet * 100;

            valorFinal = valorInicial + (valorInicial * cet);
            valorParcela = valorFinal / qtdParcelas;

            auxTarifaPrint = String.format("%.2f", tarifaPrint);
            auxIofPrint = String.format("%.2f", iofPrint);
            auxCetPrint = String.format("%.2f", cetPrint);
            auxValorParcela = String.format("%.2f", valorParcela);
            auxValorFinal = String.format("%.2f", valorFinal);

            edtTarifaTSP.setText(auxTarifaPrint);
            edtIofTSP.setText(auxIofPrint);
            edtCetTSP.setText(auxCetPrint);
            edtValorParcTSP.setText(auxValorParcela);
            edtValorTotalTSP.setText(auxValorFinal);

        }
    }

    private boolean validaDados() {

        boolean existeErros = false;

        if (edtRendaTSP.getText().toString().isEmpty()) {

            edtRendaTSP.setError("Campo Obrigatório");
            edtRendaTSP.requestFocus();
            existeErros = true;

        } else if (edtValorTSP.getText().toString().isEmpty()) {

            edtValorTSP.setError("Campo Obrigatório");
            edtValorTSP.requestFocus();
            existeErros = true;

        } else if (spnParcelasTSP.getSelectedItem().toString().equals(parcelas[0])) {

            ((TextView) spnParcelasTSP.getSelectedView()).setError("Campo Obrigatório");
            existeErros = true;

        } else if (edtTarifaTSP.getText().toString().isEmpty()) {

            edtTarifaTSP.setError("Campo Obrigatório");
            edtTarifaTSP.requestFocus();
            existeErros = true;

        } else if (edtIofTSP.getText().toString().isEmpty()) {

            edtIofTSP.setError("Campo Obrigatório");
            edtIofTSP.requestFocus();
            existeErros = true;

        } else if (edtCetTSP.getText().toString().isEmpty()) {

            edtCetTSP.setError("Campo Obrigatório");
            edtCetTSP.requestFocus();
            existeErros = true;

        } else if (edtDataTSPAux == "") {
            edtDataTSP.setError("Campo Obrigatório");
            edtDataTSP.requestFocus();
            existeErros = true;

        } else if (edtValorParcTSP.getText().toString().isEmpty()) {

            edtValorParcTSP.setError("Campo Obrigatório");
            edtValorParcTSP.requestFocus();
            existeErros = true;

        } else if (edtValorTotalTSP.getText().toString().isEmpty()) {

            edtValorTotalTSP.setError("Campo Obrigatório");
            edtValorTotalTSP.requestFocus();
            existeErros = true;

        }

        return existeErros;
    }

    private boolean validaSimula() {
        boolean existeErros = false;

        if (edtRendaTSP.getText().toString().isEmpty()) {

            edtRendaTSP.setError("Campo Obrigatório");
            edtRendaTSP.requestFocus();
            existeErros = true;

        } else if (edtValorTSP.getText().toString().isEmpty()) {

            edtValorTSP.setError("Campo Obrigatório");
            edtValorTSP.requestFocus();
            existeErros = true;

        } else if (spnParcelasTSP.getSelectedItem().toString().equals(parcelas[0])) {

            ((TextView) spnParcelasTSP.getSelectedView()).setError("Campo Obrigatório");
            existeErros = true;

        }

        return existeErros;

    }

    public static class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            Integer dia = calendar.get(Calendar.DAY_OF_MONTH);
            Integer mes = calendar.get(Calendar.MONTH);
            Integer ano = calendar.get(Calendar.YEAR);

            DatePickerDialog dtp = new DatePickerDialog(getActivity(), android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, ano, mes, dia);
            return dtp;
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
            String dayCorrect = String.valueOf(dayOfMonth);
            String monthCorrect;
            if (dayOfMonth < 10) {
                dayCorrect = "0" + dayOfMonth;
            }
            if (month < 10) {
                monthCorrect = "0" + (month + 1);
            } else {
                monthCorrect = String.valueOf(month + 1);
            }

            String tagSelect = getTag();
            if (tagSelect.equals("DataInicial")) {

                edtDataTSPAux = dayCorrect + "/" + monthCorrect + "/" + year;
                edtDataTSP.getEditText().setText(edtDataTSPAux);
                edtDataTSPAux = year + "-" + monthCorrect + "-" + dayCorrect + ":00:00";

            } else {
                Toast.makeText(getActivity(), "ERRO", Toast.LENGTH_SHORT).show();
            }
        }
    }


}