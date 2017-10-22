package moca.clockdraw;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TasksMenue extends AppCompatActivity {

    Button b_tremoranalyse1, b_tremoranalyse2, b_task1, b_task2, b_task3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_menue);

        b_tremoranalyse1 = (Button)findViewById(R.id.tremoranalyse1_b);
        b_tremoranalyse2 = (Button)findViewById(R.id.tremoranalyse2_b);
        b_task1 = (Button)findViewById(R.id.task1_b);
        b_task2 = (Button)findViewById(R.id.task2_b);
        b_task3 = (Button)findViewById(R.id.task3_b);


        b_tremoranalyse1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TasksMenue.this, tremoranalyse1.class);
                startActivity(intent);
            }
        });

        b_tremoranalyse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TasksMenue.this, tremoranalyse2.class);
                startActivity(intent);
            }
        });

        b_task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TasksMenue.this, task1.class);
                startActivity(intent);
            }
        });

        b_task2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TasksMenue.this, task2.class);
                startActivity(intent);
            }
        });

        b_task3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TasksMenue.this, task3.class);
                startActivity(intent);
            }
        });


    }
}
