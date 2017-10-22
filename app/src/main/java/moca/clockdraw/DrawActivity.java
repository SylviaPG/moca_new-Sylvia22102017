package moca.clockdraw;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenHoverListener;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.engine.SpenTouchListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DrawActivity extends Activity {

    private Context context;
    private SpenSurfaceView sView;
    private SpenNoteDoc noteDoc;
    private SpenPageDoc pageDoc;

    private List<Pair> CoordinateFloatList = new ArrayList<Pair>();
    private Long timeInAir;
    private Long timeOnSurface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        context = this;

        Spen pen = new Spen();
        try {
            pen.initialize(this);
        } catch (SsdkUnsupportedException e) {
            Toast.makeText(context, "Fehler bei Init", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        // Create SPen Surface:
        RelativeLayout currLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);
        sView = new SpenSurfaceView(context);

        //Get Coordinates and Pressure values:
        final TextView xCoord = (TextView) findViewById(R.id.xCoord);
        final TextView yCoord = (TextView) findViewById(R.id.yCoord);
        final TextView pressure = (TextView) findViewById(R.id.pressure);

        final TextView vOnAir = (TextView) findViewById(R.id.viewInAir);
        final TextView vHover = (TextView) findViewById(R.id.viewHover);
        final TextView vOnScreen = (TextView) findViewById(R.id.viewOnScreen);


        sView.setTouchListener(new SpenTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
                    return false;
                }

                xCoord.setText("X: " + Float.toString(motionEvent.getX()));
                yCoord.setText("Y: " + Float.toString(motionEvent.getY()));
                pressure.setText("Druck: " + Float.toString(Math.round(motionEvent.getPressure() * 1000)));
                vHover.setBackgroundColor(getResources().getColor(R.color.simpleGrey));
                vOnAir.setBackgroundColor(getResources().getColor(R.color.simpleGrey));
                vOnScreen.setBackgroundColor(getResources().getColor(R.color.activeGreen));


                //Save Coordinates in String Array
                CoordinateFloatList.add(Pair.create(motionEvent.getX(), motionEvent.getY()));

                //Updating time on surface and Time in Air
                long startTouchTime = 0;
                long startAirTime = 0;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        startAirTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        startTouchTime = System.currentTimeMillis();
                        timeInAir += (System.currentTimeMillis() - startAirTime);
                        Toast.makeText(DrawActivity.this, "Time in Air: " + timeInAir.toString(), Toast.LENGTH_LONG).show();
                        break;
                    case MotionEvent.ACTION_UP:
                        startAirTime = System.currentTimeMillis();
                        timeOnSurface += (System.currentTimeMillis() - startTouchTime);
                        Log.d("Time on Surface: ", timeOnSurface.toString());
                        Toast.makeText(DrawActivity.this, "Time on Surface: " + timeOnSurface.toString(), Toast.LENGTH_LONG).show();
                        break;
                }

                return true;
            }
        });

        sView.setHoverListener(new SpenHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                vHover.setBackgroundColor(getResources().getColor(R.color.activeGreen));
                vOnAir.setBackgroundColor(getResources().getColor(R.color.simpleGrey));
                vOnScreen.setBackgroundColor(getResources().getColor(R.color.simpleGrey));
                xCoord.setText("X: " + Float.toString(motionEvent.getX()));
                yCoord.setText("Y: " + Float.toString(motionEvent.getY()));
                pressure.setText("Druck: " + Float.toString(Math.round(motionEvent.getPressure() * 1000)));

                return true;
            }
        });


        /*
        Add Replay Stuff:
         */
        Button replayButton = (Button) findViewById(R.id.replayButton);
        replayButton.setClickable(true);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pageDoc.stopRecord();
                sView.startReplay();
            }
        });


        /*
        Clear Button stuff:
        */
        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setClickable(true);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageDoc.undoAll();
                pageDoc.clearHistory();
                sView.startReplay(); // resets the pagedoc screen, because all strokes have been undone
            }
        });

        /*
        Save Button
         */
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setClickable(true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //File fPath = new File(Environment.getDataDirectory() + "/ClockDraw/");
                File fPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MOCA");

                if(!fPath.exists()) {
                    if(!fPath.mkdirs()) {
                        Toast.makeText(context, "Fehler im Dateipfad!", Toast.LENGTH_LONG).show();
                    }
                }
                String saveFilePath = fPath.getPath() + "/testFile.spd";
                try {
                    noteDoc.save(saveFilePath, false);
                    Toast.makeText(context, "Datei gespeichert nach" + saveFilePath, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(context, "Fehler beim Speichern!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });


        //currLayout.addView(replayButton);
        //currLayout.addView(resetButton);
        currLayout.addView(sView);

        Display currDisplay = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        currDisplay.getRectSize(rect);

        // Create SPen Doc to save drawing data:
        try {
            noteDoc = new SpenNoteDoc(context, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(context, "Fehler beim Doc", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
        pageDoc = noteDoc.appendPage();
        pageDoc.setBackgroundColor(0xFFFFFFFF);
        pageDoc.clearHistory();

        sView.setPageDoc(pageDoc, true);
        sView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);
        // Log.d("Jannik", "Bis hier her alles gut!");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sView != null) {
            sView.close();
            sView = null;
        }
        if (noteDoc != null) {
            try {
                noteDoc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            noteDoc = null;
        }
    }


    //SYLVIA: SAVING METHODS---------------------------------------------------------------------------------------------
    //SAVING METHODS

    //Sichert String als txt Datei im internen Speicher
    private void saveStringToInternal(String filename, String dataString) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(dataString.getBytes());
            fos.close();
            Toast toast = Toast.makeText(this, dataString + " saved on Internal Storage in: " + getFilesDir().toString(), Toast.LENGTH_LONG);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Speichert String auf externer Festplatte
    public void saveStringToExternal(String string, String name) {
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(Environment.getExternalStorageDirectory(), name + ".txt");
            //file = new File(Environment.DIRECTORY_DOCUMENTS, name + ".txt");
            Log.d("Sylvia:", " File " + name + " saved in:" + Environment.getExternalStorageDirectory().toString());

            outputStream = new FileOutputStream(file);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveArrayAsJSON(Array array, String path) throws IOException {

        // 1. Java object (here Array) to JSON, and save into a file
        Gson gson = new Gson();
        gson.toJson(array, new FileWriter(path));

    }

    public String JSONfromPathtoString(String path) throws FileNotFoundException {

        // 1. JSON to Java object(here String), read it from a file.
        // path e.g. "D:\\" + filename + ".json")
        Gson gson = new Gson();
        String string = gson.fromJson(new FileReader(path), String.class);
        return string;

    }

    public Array JSONfromStringtoArray(String jsonString){
        // 2. JSON to Java object(here Array), read it from a Json String
        Gson gson = new Gson();
        Array array = gson.fromJson(jsonString, Array.class);
        return array;

    }


    //Liest File vom Internen Speicher als String aus
    public String saveFileFromInternaltoString(String filename) {
        File myDir = getFilesDir();
        String string = "";

        try {
            File secondInputFile = new File(myDir + "/text/", filename);
            InputStream secondInputStream = new BufferedInputStream(new FileInputStream(secondInputFile));
            BufferedReader r = new BufferedReader(new InputStreamReader(secondInputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            r.close();
            secondInputStream.close();
            string = total.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;

    }


}
