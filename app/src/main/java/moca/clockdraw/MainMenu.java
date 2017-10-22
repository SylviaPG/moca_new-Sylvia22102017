package moca.clockdraw;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    Button b_startTest, b_startDataAnalyse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        b_startTest = (Button)findViewById(R.id.startTest_b);
        b_startDataAnalyse = (Button)findViewById(R.id.startDataAnalyse_b);

        b_startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainMenu.this, TasksMenue.class);
                startActivity(intent);
            }
        });

        b_startDataAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainMenu.this, DrawActivity.class);
                startActivity(intent);
            }
        });


    }
}
