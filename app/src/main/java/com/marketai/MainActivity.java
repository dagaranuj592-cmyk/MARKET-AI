package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(size);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        if (bold) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        tv.setPadding(dp(16), dp(8), dp(16), dp(8));
        return tv;
    }

    private TextView card(String title, String value) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(90),
                        1
                );

        TextView tv = text(title + "\n" + value, 16, true);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.rgb(24, 31, 40));
        tv.setLayoutParams(params);

        return tv;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(20), dp(16), dp(16));
        root.setBackgroundColor(Color.rgb(8, 12, 18));

        // Header
        TextView title = text("MARKET AI", 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(20));
        root.addView(title);

        // Asset selector
        LinearLayout assets = new LinearLayout(this);
        assets.setOrientation(LinearLayout.HORIZONTAL);

        TextView btc = card("₿ BTC", "BITCOIN");
        TextView gold = card("GOLD", "XAU");

        assets.addView(btc);
        assets.addView(gold);

        root.addView(assets);

        // Section title
        TextView analysisTitle = text(
                "MARKET ANALYSIS",
                20,
                true
        );

        analysisTitle.setPadding(
                dp(4),
                dp(24),
                dp(4),
                dp(10)
        );

        root.addView(analysisTitle);

        // Analysis cards
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        row1.addView(card("SUPPORT", "Calculating..."));
        row1.addView(card("RESISTANCE", "Calculating..."));

        root.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row2.addView(card("TREND", "Waiting for data"));
        row2.addView(card("VOLUME", "Waiting for data"));

        root.addView(row2);

        // Chart placeholder
        TextView chart = text(
                "PRICE CHART\n\nHistorical + Live Market Data",
                18,
                true
        );

        chart.setGravity(Gravity.CENTER);
        chart.setBackgroundColor(Color.rgb(17, 23, 31));

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(220)
                );

        chartParams.setMargins(0, dp(20), 0, dp(20));
        chart.setLayoutParams(chartParams);

        root.addView(chart);

        // Analysis button
        TextView button = text(
                "🔍  RUN FULL ANALYSIS",
                18,
                true
        );

        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(Color.rgb(0, 150, 80));

        root.addView(button);

        // Status
        TextView status = text(
                "ENGINE STATUS: READY\nHistorical data engine: OFFLINE",
                14,
                false
        );

        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(20), 0, 0);

        root.addView(status);

        setContentView(root);
    }
}
