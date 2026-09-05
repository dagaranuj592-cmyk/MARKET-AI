package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView screen = new TextView(this);

        screen.setText("MARKET AI\n\nBTC  •  GOLD\n\nMarket Analysis Engine");
        screen.setTextColor(Color.WHITE);
        screen.setTextSize(24);
        screen.setGravity(Gravity.CENTER);
        screen.setBackgroundColor(Color.rgb(11, 15, 20));

        setContentView(screen);
    }
}
