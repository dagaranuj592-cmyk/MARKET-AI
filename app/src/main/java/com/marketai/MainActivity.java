package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private LinearLayout root;
    private TextView btcCard;
    private TextView goldCard;
    private TextView supportCard;
    private TextView resistanceCard;
    private TextView trendCard;
    private TextView volumeCard;
    private TextView status;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView makeText(
            String value,
            float size,
            boolean bold
    ) {
        TextView tv = new TextView(this);

        tv.setText(value);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(size);
        tv.setGravity(Gravity.CENTER);

        if (bold) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        tv.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        return tv;
    }

    private TextView makeCard(
            String title,
            String value
    ) {

        TextView tv = makeText(
                title + "\n" + value,
                15,
                true
        );

        tv.setBackgroundColor(
                Color.rgb(24, 31, 40)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(95),
                        1
                );

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        tv.setLayoutParams(params);

        return tv;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(12),
                dp(18),
                dp(12),
                dp(12)
        );

        root.setBackgroundColor(
                Color.rgb(8, 12, 18)
        );

        TextView title =
                makeText("MARKET AI", 28, true);

        title.setPadding(
                0,
                dp(5),
                0,
                dp(18)
        );

        root.addView(title);

        LinearLayout assets =
                new LinearLayout(this);

        assets.setOrientation(
                LinearLayout.HORIZONTAL
        );

        btcCard =
                makeCard("₿ BTC", "Loading...");

        goldCard =
                makeCard("GOLD", "Loading...");

        assets.addView(btcCard);
        assets.addView(goldCard);

        root.addView(assets);

        TextView heading =
                makeText(
                        "MARKET ANALYSIS",
                        20,
                        true
                );

        heading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        heading.setPadding(
                dp(5),
                dp(22),
                dp(5),
                dp(8)
        );

        root.addView(heading);

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        supportCard =
                makeCard(
                        "SUPPORT",
                        "Calculating..."
                );

        resistanceCard =
                makeCard(
                        "RESISTANCE",
                        "Calculating..."
                );

        row1.addView(supportCard);
        row1.addView(resistanceCard);

        root.addView(row1);

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        trendCard =
                makeCard(
                        "TREND",
                        "Loading..."
                );

        volumeCard =
                makeCard(
                        "VOLUME",
                        "Loading..."
                );

        row2.addView(trendCard);
        row2.addView(volumeCard);

        root.addView(row2);

        TextView chart =
                makeText(
                        "PRICE CHART\n\n"
                        + "Historical + Live Market Data",
                        17,
                        true
                );

        chart.setBackgroundColor(
                Color.rgb(17, 23, 31)
        );

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(210)
                );

        chartParams.setMargins(
                0,
                dp(15),
                0,
                dp(15)
        );

        chart.setLayoutParams(chartParams);

        root.addView(chart);

        TextView button =
                makeText(
                        "🔍  RUN FULL ANALYSIS",
                        17,
                        true
                );

        button.setBackgroundColor(
                Color.rgb(0, 150, 80)
        );

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMarketData();
                    }
                }
        );

        root.addView(button);

        status =
                makeText(
                        "ENGINE STATUS: READY\n"
                        + "Live data engine: READY",
                        14,
                        false
                );

        status.setPadding(
                0,
                dp(18),
                0,
                0
        );

        root.addView(status);

        setContentView(root);

        loadMarketData();
    }

    private void loadMarketData() {

        status.setText(
                "ENGINE STATUS: RUNNING\n"
                + "Downloading market data..."
        );

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String btc =
                                    getMarketData("BTC-USD");

                            String gold =
                                    getMarketData("GC=F");

                            final String btcResult =
                                    analyze(btc);

                            final String goldResult =
                                    analyze(gold);

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            updateBTC(
                                                    btcResult
                                            );

                                            updateGold(
                                                    goldResult
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: READY\n"
                                                    + "Live analysis completed"
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            status.setText(
                                                    "ENGINE STATUS: ERROR\n"
                                                    + "Could not load market data"
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private String getMarketData(
            String symbol
    ) throws Exception {

        String encoded =
                symbol.replace("=", "%3D");

        String urlString =
                "https://query1.finance.yahoo.com/v8/finance/chart/"
                + encoded
                + "?range=6mo&interval=1d";

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        connection.disconnect();

        return response.toString();
    }

    private String analyze(
            String json
    ) throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONObject chart =
                root.getJSONObject("chart");

        JSONArray results =
                chart.getJSONArray("result");

        JSONObject result =
                results.getJSONObject(0);

        JSONObject indicators =
                result.getJSONObject("indicators");

        JSONArray quote =
                indicators
                        .getJSONArray("quote");

        JSONObject q =
                quote.getJSONObject(0);

        JSONArray close =
                q.getJSONArray("close");

        JSONArray volume =
                q.optJSONArray("volume");

        double lowest =
                Double.MAX_VALUE;

        double highest =
                -Double.MAX_VALUE;

        double sumVolume = 0;

        int count = 0;

        for (int i = 0;
             i < close.length();
             i++) {

            if (close.isNull(i)) {
                continue;
            }

            double price =
                    close.getDouble(i);

            if (price < lowest) {
                lowest = price;
            }

            if (price > highest) {
                highest = price;
            }

            if (volume != null
                    && !volume.isNull(i)) {

                sumVolume +=
                        volume.getDouble(i);
            }

            count++;
        }

        double averageVolume =
                count > 0
                        ? sumVolume / count
                        : 0;

        double last =
                close.getDouble(
                        close.length() - 1
                );

        String trend =
                "NEUTRAL";

        if (close.length() >= 20) {

            double recentSum = 0;
            double oldSum = 0;

            int start =
                    Math.max(
                            0,
                            close.length() - 20
                    );

            for (int i = start;
                 i < close.length();
                 i++) {

                if (!close.isNull(i)) {
                    recentSum +=
                            close.getDouble(i);
                }
            }

            int recentCount =
                    close.length() - start;

            double sma20 =
                    recentSum /
                    Math.max(
                            recentCount,
                            1
                    );

            int oldStart =
                    Math.max(
                            0,
                            close.length() - 50
                    );

            int oldEnd =
                    Math.max(
                            0,
                            close.length() - 20
                    );

            for (int i = oldStart;
                 i < oldEnd;
                 i++) {

                if (!close.isNull(i)) {
                    oldSum +=
                            close.getDouble(i);
                }
            }

            int oldCount =
                    oldEnd - oldStart;

            if (oldCount > 0) {

                double smaOld =
                        oldSum / oldCount;

                if (sma20 > smaOld) {
                    trend = "UP";
                } else if (sma20 < smaOld) {
                    trend = "DOWN";
                }
            }
        }

        return String.format(
                "%.2f|%.2f|%.2f|%s|%.0f",
                last,
                lowest,
                highest,
                trend,
                averageVolume
        );
    }

    private void updateBTC(
            String result
    ) {

        String[] data =
                result.split("\\|");

        if (data.length < 5) {
            return;
        }

        btcCard.setText(
                "₿ BTC\n$ "
                + data[0]
        );

        supportCard.setText(
                "SUPPORT\n"
                + data[1]
        );

        resistanceCard.setText(
                "RESISTANCE\n"
                + data[2]
        );

        trendCard.setText(
                "TREND\n"
                + data[3]
        );

        volumeCard.setText(
                "VOLUME\n"
                + data[4]
        );
    }

    private void updateGold(
            String result
    ) {

        String[] data =
                result.split("\\|");

        if (data.length < 5) {
            return;
        }

        goldCard.setText(
                "GOLD FUTURES\n$ "
                + data[0]
        );
    }

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
            }
