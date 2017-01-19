package de.htw.project.cotime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.lucasr.twowayview.TwoWayView;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  OnItemSelectedListener{

    public final static String KEY_EXTRA_ID = "KEY_EXTRA_ID";

    private ListView listView1, listView2;
    DBHelper dbHelper;

    SessionManager session;
    Button btnNeuesZiel;

    public static String log_user;

    GraphView graph;
    TextView angemeldet, noGraph;
    TwoWayView slider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---begin-------------session things-----------------------------------------------------
        // Session class instance
        session = new SessionManager(getApplicationContext());

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(SessionManager.KEY_NAME);
        log_user = name;

        // email
        String email = user.get(SessionManager.KEY_EMAIL);

        session.checkLogin();
        //---end-------------session things-----------------------------------------------------
        
        angemeldet = (TextView) findViewById(R.id.main_angemeldet);
        angemeldet.setText(name);

        btnNeuesZiel = (Button) findViewById(R.id.ziel);
        btnNeuesZiel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(KEY_EXTRA_ID, 0);
                startActivity(intent);
            }
        });

        dbHelper = new DBHelper(this);

        graph = (GraphView) findViewById(R.id.graphView);
        graph.setVisibility(View.GONE);

        noGraph = (TextView)findViewById(R.id.nograph);
        noGraph.setVisibility(View.VISIBLE);

        slider = (TwoWayView) findViewById(R.id.main_slider);
        loadSliderData();
        loadViewListData(name);
    }

    //-begin-----------------load slider date-----------------------------------------
    private void loadSliderData() {

        //DBHelper db = new DBHelper(getApplicationContext());

        List<String> lables = dbHelper.getAllLabels();

        ArrayAdapter<String> aItems = new ArrayAdapter<String>(this, R.layout.list_slider, lables);
        slider.setAdapter(aItems);
        slider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = parent.getItemAtPosition(position).toString();
                int limit = dbHelper.getLimit(label, fetchUser());
                graph = (GraphView) findViewById(R.id.graphView);
                noGraph = (TextView)findViewById(R.id.nograph);
                if (label == "ABMELDEN")
                {
                    session.logoutUser();
                    finish();
                }
                else if (label == "NEUE WOCHE STARTEN")
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.startnewweek)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    DBHelper db = new DBHelper(getApplicationContext());
                                    db.startNewWeek();
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
                    d.setTitle("Zurücksetzen");
                    d.show();
                    return;
                }
                else
                {
                    Toast.makeText(parent.getContext(), "Tätigkeit: " + label, Toast.LENGTH_LONG).show();
                    createGraph(label, limit);
                }
            }
        });
    }
    //-end-----------------load slider date-----------------------------------------

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    //method to calculate percentage from task done and target
    //gives result for yaxis of graph
    public int getGraphY(String tat, int limit, int day)
    {
        int anzahl = dbHelper.getAnzahlValue(fetchUser(), tat, day);
        int target = dbHelper.getTargetValue(fetchUser(), tat);
        int percentage;
        if (limit == 1)
        {
            if (target == 0)
            {percentage = 0;}
            else
            {percentage = (anzahl*100)/target;}
        }
        else
        {
            // calculation for lower limit graph is not possible for the time being
            percentage = 1;
        }
        return percentage;
    }

    //method to create graph
    public void createGraph(String label, int limit)
    {
        graph.setVisibility(View.VISIBLE);
        noGraph.setVisibility(View.GONE);

        //remove any series from previous select
        graph.removeAllSeries();
        //read new series from db
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, getGraphY(label, limit, 1)),
                new DataPoint(2, getGraphY(label, limit, 2)),
                new DataPoint(3, getGraphY(label, limit, 3)),
                new DataPoint(4, getGraphY(label, limit, 4)),
                new DataPoint(5, getGraphY(label, limit, 5)),
                new DataPoint(6, getGraphY(label, limit, 6)),
                new DataPoint(7, getGraphY(label, limit, 7))
        });

        //create graph
        graph.addSeries(series);

        // set manual X and Y bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(7);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(120);

        // use static labels for horizontal and vertical labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{"So", "Mo", "Di", "Mi", "Do", "Fr", "Sa"});
        staticLabelsFormatter.setVerticalLabels(new String[]{0 + "%", 20 + "%", 40 + "%", 60 + "%", 80 + "%", 100 + "%", 120 + "%"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        // styling series
        series.setColor(Color.parseColor("#FF9800"));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
    }

    //get logged in user
    public static String fetchUser() {return log_user;}

    //---begin-------------method to show the tables--------------------------------------------------
    public void loadViewListData(String user)
    {
        final Cursor cursorUpper = dbHelper.getAllUpper(user, 1);

        String [] columnsUpper = new String[] {
                DBHelper.ZIEL_COLUMN_TAETIGKEIT,
                DBHelper.NOTE_COLUMN_ANZAHL,
                DBHelper.ZIEL_COLUMN_EINHEIT,
                DBHelper.ZIEL_COLUMN_TARGETVALUE,
        };
        int [] widgetsUpper = new int[] {
                R.id.layout_taetigkeit1,
                R.id.layout_anzahl1,
                R.id.layout_einheit1,
                R.id.layout_targetValue1,
        };

        SimpleCursorAdapter cursorAdapterUpper = new SimpleCursorAdapter(this, R.layout.list_ziel_upper,
                cursorUpper, columnsUpper, widgetsUpper, 0);
        listView1 = (ListView)findViewById(R.id.listView1);
        listView1.setAdapter(cursorAdapterUpper);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                Cursor itemCursor = (Cursor) MainActivity.this.listView1.getItemAtPosition(position);
                int zielID = itemCursor.getInt(itemCursor.getColumnIndex(DBHelper.NOTE_COLUMN_NOTETAT_ID));
                Intent intent = new Intent(getApplicationContext(), InputActivity.class);
                intent.putExtra(KEY_EXTRA_ID, zielID);
                startActivity(intent);
            }
        });

        final Cursor cursorLower = dbHelper.getAllUpper(user, 0);

        String [] columnsLower = new String[] {
                DBHelper.ZIEL_COLUMN_TAETIGKEIT,
                DBHelper.NOTE_COLUMN_ANZAHL,
                DBHelper.ZIEL_COLUMN_EINHEIT,
                DBHelper.ZIEL_COLUMN_TARGETVALUE,
        };
        int [] widgetsLower = new int[] {
                R.id.layout_taetigkeit2,
                R.id.layout_anzahl2,
                R.id.layout_einheit2,
                R.id.layout_targetValue2,
        };

        SimpleCursorAdapter cursorAdapterLower = new SimpleCursorAdapter(this, R.layout.list_ziel_lower,
                cursorLower, columnsLower, widgetsLower, 0);
        listView2 = (ListView)findViewById(R.id.listView2);
        listView2.setAdapter(cursorAdapterLower);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                Cursor itemCursor = (Cursor) MainActivity.this.listView2.getItemAtPosition(position);
                int zielID = itemCursor.getInt(itemCursor.getColumnIndex(DBHelper.NOTE_COLUMN_NOTETAT_ID));
                Intent intent = new Intent(getApplicationContext(), InputActivity.class);
                intent.putExtra(KEY_EXTRA_ID, zielID);
                startActivity(intent);
            }
        });
    }
    //---end-------------method to show the tables--------------------------------------------------
}



