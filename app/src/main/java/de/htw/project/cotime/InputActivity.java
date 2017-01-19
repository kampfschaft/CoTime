package de.htw.project.cotime;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;


public class InputActivity extends ActionBarActivity implements View.OnClickListener {

    private DBHelper dbHelper ;
    EditText taetigkeit;
    EditText targetValue;
    EditText einheit;
    EditText anzahl;

    Button NeuZiel;

    LinearLayout buttonlayout1;
    Button anpassen1, aktualisieren1;

    LinearLayout buttonlayout2;
    Button speichern2, loeschen2;

    RadioGroup grenzeGroup;
    RadioButton grenzeUpper, grenzeLower;

    int zielID;
    int grenze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        zielID = getIntent().getIntExtra(MainActivity.KEY_EXTRA_ID, 0);

        setContentView(R.layout.activity_input);

        taetigkeit = (EditText) findViewById(R.id.anpassen_taetigkeit);
        einheit = (EditText) findViewById(R.id.anpassen_einheit);
        anzahl = (EditText) findViewById(R.id.anpassen_anzahl);
        targetValue = (EditText) findViewById(R.id.anpassen_targetValue);

        NeuZiel = (Button) findViewById(R.id.ziel_speichern_orig);
        NeuZiel.setOnClickListener(this);

        buttonlayout1 = (LinearLayout) findViewById(R.id.buttons1);
        anpassen1 = (Button) findViewById(R.id.ziel_anpassen1);
        anpassen1.setOnClickListener(this);
        aktualisieren1 = (Button) findViewById(R.id.ziel_aktualisieren1);
        aktualisieren1.setOnClickListener(this);

        buttonlayout2 = (LinearLayout) findViewById(R.id.buttons2);
        speichern2 = (Button) findViewById(R.id.ziel_speichern);
        speichern2.setOnClickListener(this);
        loeschen2 = (Button) findViewById(R.id.ziel_loeschen);
        loeschen2.setOnClickListener(this);

        grenzeGroup = (RadioGroup) findViewById(R.id.radio_group);
        grenzeUpper = (RadioButton) findViewById(R.id.radio_upper);
        grenzeLower = (RadioButton) findViewById(R.id.radio_lower);

        dbHelper = new DBHelper(this);

        if(zielID > 0) {
            NeuZiel.setVisibility(View.GONE);
            buttonlayout2.setVisibility(View.GONE);
            buttonlayout1.setVisibility(View.VISIBLE);


            Cursor rs = dbHelper.getZiel(zielID);
            rs.moveToFirst();
            String column_taetigkeit = rs.getString(rs.getColumnIndex(DBHelper.ZIEL_COLUMN_TAETIGKEIT));
            String column_einheit = rs.getString(rs.getColumnIndex(DBHelper.ZIEL_COLUMN_EINHEIT));
            int column_targetValue = rs.getInt(rs.getColumnIndex(DBHelper.ZIEL_COLUMN_TARGETVALUE));
            int column_grenze = rs.getInt(rs.getColumnIndex(DBHelper.ZIEL_COLUMN_GRENZE));

            Cursor rsNote = dbHelper.getNote(zielID);
            rsNote.moveToFirst();
            int column_anzahl = rsNote.getInt(rsNote.getColumnIndex(DBHelper.NOTE_COLUMN_ANZAHL));


            if (!rs.isClosed()) {
                rs.close();
            }

            if (!rsNote.isClosed()) {
                rsNote.close();
            }

            taetigkeit.setText(column_taetigkeit);
            taetigkeit.setFocusable(false);
            taetigkeit.setClickable(false);
            taetigkeit.setBackgroundResource(R.drawable.typingbox_disabled);

            einheit.setText((CharSequence) column_einheit);
            einheit.setFocusable(false);
            einheit.setClickable(false);
            einheit.setBackgroundResource(R.drawable.typingbox_disabled);

            targetValue.setText((CharSequence) (column_targetValue + ""));
            targetValue.setFocusable(false);
            targetValue.setClickable(false);
            targetValue.setBackgroundResource(R.drawable.typingbox_disabled);

            anzahl.setText((CharSequence) (column_anzahl + ""));
            anzahl.setFocusable(true);
            anzahl.setClickable(true);

            if (column_grenze == 1)
            {
                grenzeUpper.setChecked(true);
                grenze = 1;
            }
            else if (column_grenze == 0)
            {
                grenzeLower.setChecked(true);
                grenze = 0;
            }
        }
    }

    // method for radiobutton
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_upper:
                if (checked)
                    grenze = 1;
                break;
            case R.id.radio_lower:
                if (checked)
                    grenze = 0;
                break;
        }
    }


    //Method for every possible buttons
    @Override
    public void onClick(View view) {
        String s_taetigkeit = taetigkeit.getText().toString();
        String s_einheit = einheit.getText().toString();
        String s_targetValue = targetValue.getText().toString();
        String s_anzahl = anzahl.getText().toString();

        switch (view.getId()) {
            //Ziel speichern button gedruckt
            case R.id.ziel_speichern_orig:
                if (s_taetigkeit.trim().length() > 0 && s_einheit.trim().length() > 0
                        && s_targetValue.trim().length() > 0 && s_anzahl.trim().length() > 0)
                {
                    persistZiel();
                    return;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angaben", Toast.LENGTH_SHORT).show();
                }

                //Aktualisieren button gedruckt
            case R.id.ziel_aktualisieren1:
                if (s_taetigkeit.trim().length() > 0 && s_einheit.trim().length() > 0
                        && s_targetValue.trim().length() > 0 && s_anzahl.trim().length() > 0)
                {
                    persistZiel();
                    return;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angaben", Toast.LENGTH_SHORT).show();
                }
                return;

            //Anpassen button gedruckt
            case R.id.ziel_anpassen1:
                NeuZiel.setVisibility(View.GONE);
                buttonlayout2.setVisibility(View.VISIBLE);
                buttonlayout1.setVisibility(View.GONE);

                einheit.setEnabled(true);
                einheit.setFocusableInTouchMode(true);
                einheit.setClickable(true);
                einheit.setBackgroundResource(R.drawable.typingbox);

                targetValue.setEnabled(true);
                targetValue.setFocusableInTouchMode(true);
                targetValue.setClickable(true);
                targetValue.setBackgroundResource(R.drawable.typingbox);

                anzahl.setEnabled(true);
                anzahl.setFocusableInTouchMode(true);
                anzahl.setClickable(true);

                taetigkeit.setEnabled(true);
                taetigkeit.setFocusableInTouchMode(true);
                taetigkeit.setClickable(true);
                taetigkeit.setBackgroundResource(R.drawable.typingbox);
                return;

            //Speichern nach der Anpassung
            case R.id.ziel_speichern:
                if (s_taetigkeit.trim().length() > 0 && s_einheit.trim().length() > 0
                        && s_targetValue.trim().length() > 0 && s_anzahl.trim().length() > 0)
                {
                    persistZiel();
                    return;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angaben", Toast.LENGTH_SHORT).show();
                }
                return;

            //Löschen button gedruckt
            case R.id.ziel_loeschen:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.zielloeschen)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dbHelper.deleteZiel(zielID);
                                Toast.makeText(getApplicationContext(), "Gelöscht", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                AlertDialog d = builder.create();
                d.setTitle("Löschen");
                d.show();
                return;
        }
    }



    //Methode zum Speichern und aktualisieren
    public void persistZiel() {
        if(zielID > 0)
        {
            if(dbHelper.updateZiel(zielID, taetigkeit.getText().toString(),
                    Integer.parseInt(targetValue.getText().toString()),
                    einheit.getText().toString(),
                    Integer.parseInt(anzahl.getText().toString()),
                    grenze
            ))
            {
                Toast.makeText(getApplicationContext(), "Ziel aktualisiert", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if(dbHelper.insertZiel(taetigkeit.getText().toString(),
                    Integer.parseInt(targetValue.getText().toString()),
                    einheit.getText().toString(),
                    Integer.parseInt(anzahl.getText().toString()),
                    grenze))
            {
                Toast.makeText(getApplicationContext(), "Ziel Eingefügt", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Ziel nicht gespeichert", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
