package de.htw.project.cotime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener {

    EditText editTextemail, editTextpassword, editTextpasswordConfirm;
    Button btnLogin, btnRegister, btnSave, btnCancel;
    LinearLayout layoutHiddenButtons;

    SessionManager session;

    DBHelper dbHelper;

    public static String s_email;
    public static String s_password;
    public static String s_passwordConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        dbHelper = new DBHelper(this);

        editTextemail = (EditText) findViewById(R.id.login_email);
        editTextpassword = (EditText) findViewById(R.id.login_password);
        editTextpasswordConfirm = (EditText) findViewById(R.id.login_passwordConfirm);

        btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
        btnRegister = (Button) findViewById(R.id.login_reg);
        btnRegister.setOnClickListener(this);
        btnSave = (Button) findViewById(R.id.login_hiddenSave);
        btnSave.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.login_hiddenCancel);
        btnCancel.setOnClickListener(this);

        layoutHiddenButtons = (LinearLayout) findViewById(R.id.login_hiddenButtons);

        //Abbrechen, Speichern buttons and confirmPass box are always hidden
        editTextpasswordConfirm.setVisibility(View.GONE);
        layoutHiddenButtons.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view)
    {
        String s_dbEmail;
        String s_dbPassword;

        switch (view.getId()) {

            //when register button clicked
            case R.id.login_reg:
                //show and hide some buttons
                editTextpasswordConfirm.setVisibility(View.VISIBLE);
                layoutHiddenButtons.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                btnRegister.setVisibility(View.GONE);
                return;

            //when cancel button clicked
            case R.id.login_hiddenCancel:
                //back to previous layout
                editTextpasswordConfirm.setVisibility(View.GONE);
                layoutHiddenButtons.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.VISIBLE);
                return;

            //when login button clicked
            case R.id.login_button:
                s_email = editTextemail.getText().toString();
                s_password = editTextpassword.getText().toString();
                s_dbEmail = dbHelper.checkEmail(s_email).toString();
                s_dbPassword = dbHelper.checkPassword(s_email).toString();

                // Check if email and password are filled
                if(s_email.trim().length() > 0 && s_password.trim().length() > 0)
                {
                    // Check if email already in database
                    if(s_email.equals(s_dbEmail))
                    {
                        // Check if password correct. If all correct, log in success
                        if(s_password.equals(s_dbPassword))
                        {
                            Toast.makeText(getApplicationContext(), "Anmeldung erfolgreich", Toast.LENGTH_SHORT).show();
                            session.createLoginSession(s_email, "domain."+s_email);
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        // if password incorrect give error
                        else
                        {Toast.makeText(getApplicationContext(), "Password stimmt nicht", Toast.LENGTH_SHORT).show();}
                    }
                    // if email not in database, must register
                    else
                    {Toast.makeText(getApplicationContext(), "Email nicht gefunden. Bitte registrieren", Toast.LENGTH_SHORT).show();}
                }
                // if email or password not filled, give error
                else
                {Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angabe", Toast.LENGTH_SHORT).show();}
                return;

            //when save button clicked
            case R.id.login_hiddenSave:
                s_email = editTextemail.getText().toString();
                s_password = editTextpassword.getText().toString();
                s_passwordConfirm = editTextpasswordConfirm.getText().toString();
                s_dbEmail = dbHelper.checkEmail(s_email).toString();

                // Check if email already registered
                if (!s_email.equals(s_dbEmail))
                {
                    // Check if all fields are filled
                    if(s_email.trim().length() > 0 && s_password.trim().length() > 0 && s_passwordConfirm.trim().length() > 0 )
                    {
                        //check if both password are same
                        if(s_passwordConfirm.equals(s_password))
                        {
                            // if same, save in database
                            if(dbHelper.insertToAcc(s_email, s_password))
                            {
                                Toast.makeText(getApplicationContext(), "Benutzer Eingefügt", Toast.LENGTH_SHORT).show();
                                session.createLoginSession(s_email, "domain."+s_email);
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                            // if problem in saving
                            else
                            {Toast.makeText(getApplicationContext(), "Benutzer nicht gespeichert", Toast.LENGTH_SHORT).show();}
                        }
                        //if not same
                        else
                        {Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angabe", Toast.LENGTH_SHORT).show();}
                    }
                    // if email or password not filled, give error
                    else
                    {Toast.makeText(getApplicationContext(), "Bitte prüfen Sie Ihre Angabe", Toast.LENGTH_SHORT).show();}
                }
                // if already registered
                else
                {Toast.makeText(getApplicationContext(), "Benutzer bereits registriert. Bitte züruck zur Anmeldung", Toast.LENGTH_SHORT).show();}
                return;
        }
    }
}

