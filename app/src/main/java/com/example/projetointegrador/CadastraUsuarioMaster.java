package com.example.projetointegrador;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CadastraUsuarioMaster extends AppCompatActivity {

    Button btnConfirmaTCUM, btnCancelaTCUM;
    Spinner spnUserTypeTCUM, spnCargoTCUM;
    EditText edtCpfTCUM, edtNomeTCUM, edtCelTCUM, edtTelComTCUM, edtEmailTCUM, edtSenhaTCUM, edtRptSenhaTCUM;

    String[] tipoUsuario = new String[]{"Selecione uma Opção", "Administrador", "Usuário"};
    String[] cargoUsusario = new String[]{"Selecione uma Opção", "teste 1", "teste 2"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastra_usuario_master);
        setTitle("Cadastra Usuarios");

        inicializarComponentes();
        escolhaTipoUser();
        escolhaStatusUser();

        btnConfirmaTCUM.setOnClickListener((view -> {
            if (!validaDados()) {
                String tipoUser = spnUserTypeTCUM.getSelectedItem().toString();
                String cargoUser = spnCargoTCUM.getSelectedItem().toString();
                System.out.println(tipoUser + cargoUser);
                limpaCampos();
            } else {

            }
        }));

        btnCancelaTCUM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void inicializarComponentes() {

        btnConfirmaTCUM = findViewById(R.id.btnConfirmaTCUM);
        btnCancelaTCUM = findViewById(R.id.btnCancelaTCUM);

        spnUserTypeTCUM = findViewById(R.id.spnUserTypeTCUM);
        spnCargoTCUM = findViewById(R.id.spnCargoTCUM);

        edtCpfTCUM = findViewById(R.id.edtCpfTCUM);
        edtNomeTCUM = findViewById(R.id.edtNomeTCUM);
        edtCelTCUM = findViewById(R.id.edtCelTCUM);
        edtTelComTCUM = findViewById(R.id.edtTelComTCUM);
        edtEmailTCUM = findViewById(R.id.edtEmailTCUM);
        edtSenhaTCUM = findViewById(R.id.edtSenhaTCUM);
        edtRptSenhaTCUM = findViewById(R.id.edtRptSenhaTCUM);

    }

    private void escolhaTipoUser() {
        spnUserTypeTCUM.setAdapter(new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.textview_spinner,
                tipoUsuario
        ));
    }

    private void escolhaStatusUser() {
        spnCargoTCUM.setAdapter(new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.textview_spinner,
                cargoUsusario
        ));
    }

    private void limpaCampos() {

        edtCpfTCUM.setText("");
        edtNomeTCUM.setText("");
        edtCelTCUM.setText("");
        edtTelComTCUM.setText("");
        edtEmailTCUM.setText("");
        edtSenhaTCUM.setText("");
        edtRptSenhaTCUM.setText("");

        spnUserTypeTCUM.setSelection(0);
        spnCargoTCUM.setSelection(0);
    }

    private boolean validaDados() {

        Boolean existeErros = false;

        if (edtCpfTCUM.getText().toString().isEmpty()) {

            edtCpfTCUM.setError("Campo Obrigatório");
            edtCpfTCUM.requestFocus();
            existeErros = true;

        } else if (edtNomeTCUM.getText().toString().isEmpty()) {

            edtNomeTCUM.setError("Campo Obrigatório");
            edtNomeTCUM.requestFocus();
            existeErros = true;

        } else if (edtCelTCUM.getText().toString().isEmpty()) {

            edtCelTCUM.setError("Campo Obrigatório");
            edtCelTCUM.requestFocus();
            existeErros = true;

        } else if (edtTelComTCUM.getText().toString().isEmpty()) {

            edtTelComTCUM.setError("Campo Obrigatório");
            edtTelComTCUM.requestFocus();
            existeErros = true;

        } else if (edtEmailTCUM.getText().toString().isEmpty()) {

            edtEmailTCUM.setError("Campo Obrigatório");
            edtEmailTCUM.requestFocus();
            existeErros = true;

        } else if (edtSenhaTCUM.getText().toString().isEmpty()) {

            edtSenhaTCUM.setError("Campo Obrigatório");
            edtSenhaTCUM.requestFocus();
            existeErros = true;

        } else if (edtRptSenhaTCUM.getText().toString().isEmpty()) {

            edtRptSenhaTCUM.setError("Campo Obrigatório");
            edtRptSenhaTCUM.requestFocus();
            existeErros = true;

        } else if (spnUserTypeTCUM.getSelectedItem().toString().equals("Selecione uma Opção")) {

            ((TextView) spnUserTypeTCUM.getSelectedView()).setError("Campo Obrigatório");
            existeErros = true;

        } else if (spnCargoTCUM.getSelectedItem().toString().equals("Selecione uma Opção")) {

            ((TextView) spnCargoTCUM.getSelectedView()).setError("Campo Obrigatório");
            existeErros = true;

        }
        return existeErros;
    }
}