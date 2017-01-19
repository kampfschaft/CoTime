package de.htw.project.cotime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import java.util.ArrayList;

import static de.htw.project.cotime.MainActivity.fetchUser;

public class DBHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "cotime.db";
    private static final int DATABASE_VERSION = 1;

    //Tabelle Accounts
    public static final String ACC_TABLE_NAME = "acc";
    public static final String ACC_COLUMN_ID = "_id";
    public static final String ACC_COLUMN_EMAIL = "email";
    public static final String ACC_COLUMN_PASSWORD = "password";

    //Tabelle ziel
    public static final String ZIEL_TABLE_NAME = "tat";
    public static final String ZIEL_COLUMN_ID = "_id";
    public static final String ZIEL_COLUMN_TAETIGKEIT = "taetigkeit";
    public static final String ZIEL_COLUMN_TARGETVALUE = "targetValue";
    public static final String ZIEL_COLUMN_EINHEIT = "einheit";
    public static final String ZIEL_COLUMN_GRENZE = "grenze";

    //Tabelle note
    public static final String NOTE_TABLE_NAME = "note";
    public static final String NOTE_COLUMN_NOTE_DEFAULT_ID = "_id";
    public static final String NOTE_COLUMN_NOTETAT_ID = "note_id";
    public static final String NOTE_COLUMN_ANZAHL = "anzahl";
    public static final String NOTE_COLUMN_DATUM = "datum";
    public static final String NOTE_COLUMN_TAG = "tag";

    //Tabelle accTat
    public static final String ACCTAT_TABLE_NAME = "accTat";
    public static final String ACCTAT_COLUMN_DEFAULT_ID = "_id";
    public static final String ACCTAT_COLUMN_USER = "user";
    public static final String ACCTAT_COLUMN_ZIEL = "ziel";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // enable foreign key
        db.execSQL("PRAGMA foreign_keys=ON");

        // create table acc
        db.execSQL(
                "CREATE TABLE " + ACC_TABLE_NAME +
                        "(" + ACC_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        ACC_COLUMN_EMAIL + " TEXT, " +
                        ACC_COLUMN_PASSWORD + " TEXT)"
        );

        // create table tat
        db.execSQL(
                "CREATE TABLE " + ZIEL_TABLE_NAME +
                        "(" + ZIEL_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        ZIEL_COLUMN_TAETIGKEIT + " TEXT, " +
                        ZIEL_COLUMN_TARGETVALUE + " INTEGER," +
                        ZIEL_COLUMN_EINHEIT + " TEXT, " +
                        ZIEL_COLUMN_GRENZE + " INTEGER)"
        );

        // create table note
        db.execSQL(
                "CREATE TABLE " + NOTE_TABLE_NAME +
                        "(" + NOTE_COLUMN_NOTE_DEFAULT_ID +  " INTEGER PRIMARY KEY, " +
                        NOTE_COLUMN_NOTETAT_ID + " INTEGER, " +
                        NOTE_COLUMN_ANZAHL + " INTEGER, " +
                        NOTE_COLUMN_DATUM + " DATETIME, " +
                        NOTE_COLUMN_TAG + " INTEGER, FOREIGN KEY (" +
                        NOTE_COLUMN_NOTETAT_ID + ") REFERENCES " + ZIEL_TABLE_NAME + "(" +ZIEL_COLUMN_ID + ") ON UPDATE CASCADE ON DELETE CASCADE)"
        );

        // create table acctat
        db.execSQL(
                "CREATE TABLE " + ACCTAT_TABLE_NAME +
                        "(" + ACCTAT_COLUMN_DEFAULT_ID + " INTEGER PRIMARY KEY, " +
                        ACCTAT_COLUMN_USER + " INTEGER, " +
                        ACCTAT_COLUMN_ZIEL + " INTEGER, FOREIGN KEY (" +
                        ACCTAT_COLUMN_USER + ") REFERENCES " + ACC_TABLE_NAME + "(" + ACC_COLUMN_ID + ") ON DELETE CASCADE, FOREIGN KEY (" +
                        ACCTAT_COLUMN_ZIEL + ") REFERENCES " + ZIEL_TABLE_NAME + "(" + ZIEL_COLUMN_ID + ") ON DELETE CASCADE)"
        );

    }

    // when database updated, drop table, then recreate table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ACC_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ZIEL_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ACCTAT_TABLE_NAME);
        onCreate(db);
    }

    //Insert to Acc (login and register)
    public boolean insertToAcc(String email, String password)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ACC_COLUMN_EMAIL, email);
        contentValues.put(ACC_COLUMN_PASSWORD, password);

        db.insert(ACC_TABLE_NAME, null, contentValues);
        return true;
    }


    //---begin----------Insert new Ziel-------------------------------------
    public boolean insertZiel(String taetigkeit, int targetValue, String einheit, int anzahl, int grenze) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Tabelle tat
        ContentValues contentValuesTabZiel = new ContentValues();
        contentValuesTabZiel.put(ZIEL_COLUMN_TAETIGKEIT, taetigkeit);
        contentValuesTabZiel.put(ZIEL_COLUMN_TARGETVALUE, targetValue);
        contentValuesTabZiel.put(ZIEL_COLUMN_EINHEIT, einheit);
        contentValuesTabZiel.put(ZIEL_COLUMN_GRENZE, grenze);

        // insert returns a long value which is the row id of the inserted line
        long i = db.insert(ZIEL_TABLE_NAME, null, contentValuesTabZiel);

        //Tabelle note
        int count = 0;
        for (count = 1; count<8; count++)
        {
            if (count == getDayNow())
            {
                ContentValues contentValuesTabNote = new ContentValues();
                contentValuesTabNote.put(NOTE_COLUMN_NOTETAT_ID, i);
                contentValuesTabNote.put(NOTE_COLUMN_ANZAHL, anzahl);
                contentValuesTabNote.put(NOTE_COLUMN_DATUM, getDateTime());
                contentValuesTabNote.put(NOTE_COLUMN_TAG, getDayNow());

                db.insert(NOTE_TABLE_NAME, null, contentValuesTabNote);
            }
            else
            {
                String temp1 = "2017-01-0"+ count;
                String temp2 = temp1 +" 01:00:00";

                ContentValues contentValuesTabNote = new ContentValues();
                contentValuesTabNote.put(NOTE_COLUMN_NOTETAT_ID, i);
                contentValuesTabNote.put(NOTE_COLUMN_ANZAHL, 0);
                contentValuesTabNote.put(NOTE_COLUMN_DATUM, temp2);
                contentValuesTabNote.put(NOTE_COLUMN_TAG, count);

                db.insert(NOTE_TABLE_NAME, null, contentValuesTabNote);
            }
        }

        //Tabelle AccTat
        ContentValues contentValuesTabAccTat = new ContentValues();
        int refAccID = getAccRefID(fetchUser());
        contentValuesTabAccTat.put(ACCTAT_COLUMN_USER, refAccID);
        contentValuesTabAccTat.put(ACCTAT_COLUMN_ZIEL, i);

        db.insert(ACCTAT_TABLE_NAME, null, contentValuesTabAccTat);

        return true;
    }
    //---end----------Insert new Ziel-------------------------------------


    //---begin----------update Ziel-------------------------------------
    // when "Ziel aktualisieren"
    public boolean updateZiel(Integer id, String taetigkeit, int targetValue, String einheit, int anzahl, int grenze) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValuesTabZiel = new ContentValues();
        contentValuesTabZiel.put(ZIEL_COLUMN_TAETIGKEIT, taetigkeit);
        contentValuesTabZiel.put(ZIEL_COLUMN_TARGETVALUE, targetValue);
        contentValuesTabZiel.put(ZIEL_COLUMN_EINHEIT, einheit);
        contentValuesTabZiel.put(ZIEL_COLUMN_GRENZE, grenze);
        db.update(ZIEL_TABLE_NAME, contentValuesTabZiel, ZIEL_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) } );

        ContentValues contentValuesTabNote = new ContentValues();
        contentValuesTabNote.put(NOTE_COLUMN_ANZAHL, anzahl);
        contentValuesTabNote.put(NOTE_COLUMN_DATUM, getDateTime());
        db.update(NOTE_TABLE_NAME, contentValuesTabNote, NOTE_COLUMN_NOTETAT_ID + " = " + id +
                " AND " + NOTE_COLUMN_TAG + " = " + getDayNow(), null );

        return true;
    }
    //---end----------update Ziel-------------------------------------

    //get number of rows
    public int numberOfRows(String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, table_name);
        return numRows;
    }

    // when a task is deleted
    public Integer deleteZiel(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTE_TABLE_NAME, NOTE_COLUMN_NOTETAT_ID + " = ? ", new String[] { Integer.toString(id) });
        return db.delete(ZIEL_TABLE_NAME,
                ZIEL_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

    // when a task is clicked on listVIew
    public Cursor getZiel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM " + ZIEL_TABLE_NAME +
                " WHERE " + ZIEL_COLUMN_ID + "=?", new String[]{Integer.toString(id)});
        return res;
    }

    // take the latest done value
    public Cursor getNote (int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT MAX(" + NOTE_COLUMN_DATUM+ "), " + NOTE_COLUMN_ANZAHL + " FROM " + NOTE_TABLE_NAME +
                " WHERE " + NOTE_COLUMN_NOTETAT_ID + "=?", new String[]{Integer.toString(id)});
        return res;
    }



    // Nur zum testen
    public Cursor getAllZiel2() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + NOTE_TABLE_NAME , null );
        return res;
    }
    // Nur zum testen
    public Cursor getAllZiel() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME+
                " WHERE " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +  " = " + NOTE_COLUMN_NOTETAT_ID +
                " AND " + NOTE_COLUMN_TAG + " = " + getDayNow(), null );
        return res;
    }

    // Nur zum testen
    public Cursor getAllAcc() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + ACC_TABLE_NAME, null );
        return res;
    }

    // Nur zum testen
    public Cursor getAllNote() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + NOTE_TABLE_NAME, null );
        return res;
    }

    // Nur zum testen
    public Cursor getAllAccTatTest() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + ACCTAT_TABLE_NAME, null );
        return res;
    }

    // Nur zum testen
    public Cursor getAllAccTat(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID, null );
        return res;
    }

    // Show all at MainActivity
    public Cursor getAllFinal(String user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + "= '" + user + "'" , null );
        return res;
    }

    public Cursor getAllFinalTest(String user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT *, MAX(" + NOTE_COLUMN_ANZAHL + ")"+
                " FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + "= '" + user + "'" +
                " GROUP BY " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID, null );
        return res;
    }

    public Cursor getAllUpper(String user, int g){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT *, MAX(" + NOTE_COLUMN_ANZAHL + ")"+
                " FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + "= '" + user + "'" +
                " AND " + ZIEL_COLUMN_GRENZE + " = " + g +
                " GROUP BY " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID, null );
        return res;
    }


    //-begin-----------------method to compare user input with email database-----------------
    public String checkEmail (String userInput)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + ACC_COLUMN_EMAIL + " FROM " + ACC_TABLE_NAME + " WHERE " +
                ACC_COLUMN_EMAIL + " = " + "'"+userInput+"'", null);
        String temp = "";
        if(res.moveToFirst())
        {
            int emailColumn = res.getColumnIndex(ACC_COLUMN_EMAIL);
            temp = res.getString(emailColumn);
        }
        return temp;
    }
    //-end-----------------method to compare user input with email database--------------------

    //-begin-----------------method to compare user input with password database---------------
    public String checkPassword (String userInput)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + ACC_COLUMN_PASSWORD + " FROM " + ACC_TABLE_NAME + " WHERE " +
                ACC_COLUMN_EMAIL + " = " + "'"+userInput+"'", null);
        String temp = "";
        if(res.moveToFirst())
        {
            int passColumn = res.getColumnIndex(ACC_COLUMN_PASSWORD);
            temp = res.getString(passColumn);
        }
        return temp;
    }
    //-end-----------------method to compare user input with password database-----------------

    //-begin-----------------method to get email Id -----------------
    public int getAccRefID (String loggedInUser)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + ACC_COLUMN_ID + " FROM " + ACC_TABLE_NAME + " WHERE " +
                ACC_COLUMN_EMAIL + " = " + "'"+loggedInUser+"'", null);
        int temp = 0;
        if(res.moveToFirst())
        {
            int accIDColumn = res.getColumnIndex(ACC_COLUMN_ID);
            temp = res.getInt(accIDColumn);
        }
        return temp;
    }
    //-end-----------------method to get email Id -----------------

    //-begin-----------------method to get current datetime--------------------------------------------
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    //-end-------------------method to get current datetime--------------------------------------------

    //-begin-----------------method to get current day--------------------------------------------
    public int getDayNow()
    {
        Date dayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dayDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day;
    }
    //-end-----------------method to get current day--------------------------------------------

    //---begin----------------method to get done value---------------------------------------
    public int getAnzahlValue(String user, String tat, int day){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT " + NOTE_COLUMN_ANZAHL + " FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + " = '" + user + "'" +
                " AND " + ZIEL_COLUMN_TAETIGKEIT + " = '" + tat + "'" +
                " AND " + NOTE_COLUMN_TAG + " = " + day  , null );

        int temp = 0;

        if(res.moveToFirst())
        {
            int anz = res.getColumnIndex(NOTE_COLUMN_ANZAHL);
            temp = res.getInt(anz);
        }
        return temp;
    }
    //---end----------------method to get done value---------------------------------------

    //---begin----------------method to get target value---------------------------------------
    public int getTargetValue(String user, String tat){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT " + ZIEL_COLUMN_TARGETVALUE + " FROM " + ACCTAT_TABLE_NAME + ", " + ACC_TABLE_NAME + ", " + ZIEL_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + " = '" + user + "'" +
                " AND " + ZIEL_COLUMN_TAETIGKEIT + " = '" + tat + "'" , null );

        int temp = 0;

        if(res.moveToFirst())
        {
            int tarVal = res.getColumnIndex(ZIEL_COLUMN_TARGETVALUE);
            temp = res.getInt(tarVal);
        }
        return temp;
    }
    //---end----------------method to get target value---------------------------------------


    //-begin-----------------method to get label for twoWayView--------------------------------------------
    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        String selectQuery = "SELECT * FROM " + ZIEL_TABLE_NAME + "," +ACCTAT_TABLE_NAME +", " + ACC_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME +"."+ ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL+ " = " + ZIEL_TABLE_NAME +"."+ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL +" = '" + fetchUser()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // loop through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // add logout at the end
        labels.add("NEUE WOCHE STARTEN");
        // add logout at the end
        labels.add("ABMELDEN");

        // closing connection
        cursor.close();
        db.close();
        return labels;
    }
    //-end-----------------method to get label for twoWayView--------------------------------------------

    //-begin-----------------method to reset done value--------------------------------------------
    public void startNewWeek()
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTE_COLUMN_ANZAHL, 0);
        db.update(NOTE_TABLE_NAME, contentValues, null, null );



        //String query = "UPDATE " + NOTE_TABLE_NAME + " SET " +  NOTE_COLUMN_ANZAHL + " = " + 0 ;
        //db.rawQuery("UPDATE " + NOTE_TABLE_NAME + " SET " +  NOTE_COLUMN_ANZAHL + " = ?", new String[] { Integer.toString(0) });
        //db.close();
    }


    //int idServizo = 150;``
    //String updateQuery ="UPDATE myTable SET sync = 1 WHERE id_servizio = "+idServizio;
    //Cursor c= dbManager.RawQuery(updateQuery, null);

    //c.moveToFirst();
    //c.close();
    // new String[] { Integer.toString(id) }
    //-end-----------------method to reset done value--------------------------------------------

    //-begin-----------------method to get label for twoWayView--------------------------------------------
    public int startNewWeek2(){
        List<String> ids = new ArrayList<String>();

        String selectQuery = "SELECT " + NOTE_COLUMN_NOTETAT_ID +" FROM " + ZIEL_TABLE_NAME + "," +ACCTAT_TABLE_NAME +", " + ACC_TABLE_NAME + ", " + NOTE_TABLE_NAME +
                " WHERE " + ACCTAT_COLUMN_USER + " = " + ACC_TABLE_NAME + "." + ACC_COLUMN_ID +
                " AND " + ACCTAT_COLUMN_ZIEL + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + NOTE_TABLE_NAME + "." + NOTE_COLUMN_NOTETAT_ID + " = " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID +
                " AND " + ACC_COLUMN_EMAIL + "= '" + fetchUser() + "'" +
                " GROUP BY " + ZIEL_TABLE_NAME + "." + ZIEL_COLUMN_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // loop through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();

        //transfer id into array string
        String[] s_id = new String[ids.size()];
        s_id = ids.toArray(s_id);
        int k = 0;
        String updateQuery = "SELECT " + NOTE_COLUMN_NOTETAT_ID+", "+ NOTE_COLUMN_ANZAHL + " FROM " + NOTE_TABLE_NAME;
        SQLiteDatabase dbToUpdate = this.getWritableDatabase();
        Cursor cursorToUpdate = dbToUpdate.rawQuery(updateQuery, null);

        for (int i = 0; i<s_id.length; i++)
        {
            // loop through all rows and change to null
            if (cursorToUpdate.moveToFirst()) {
                do {
                    int m = cursorToUpdate.getInt(0);
                    int n = Integer.parseInt(s_id[i]);
                    if(m == n)
                    {
                        SQLiteDatabase dbUpdated = this.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(NOTE_COLUMN_ANZAHL, 0);
                        dbUpdated.update(NOTE_TABLE_NAME, contentValues, NOTE_COLUMN_NOTETAT_ID + " = " + Integer.parseInt(s_id[i]), null );
                        dbUpdated.close();
                        k = k+1;
                    }
                    else
                    {
                        k = m;
                    }
                } while (cursorToUpdate.moveToNext());
            }
        }
        // closing connection
        cursorToUpdate.close();
        dbToUpdate.close();
        return k;
    }
    //-end-----------------method to get label for twoWayView--------------------------------------------

}



