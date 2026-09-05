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
            Executors.newFixedThreadPool(2);

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int dp(float value) {
        return (int) (
                value *
                getResources().getDisplayMetrics().density
                + 0.5f
        );
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
            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
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
    protected void onCreate(
            Bundle savedInstanceState
    ) {

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
                makeText(
                        "MARKET AI",
                        28,
                        true
                );

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
                makeCard(
                        "₿ BTC",
                        "Loading..."
                );

        goldCard =
                makeCard(
                        "GOLD",
                        "Loading..."
                );

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
                        loadAllData();
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

        loadAllData();
    }

    private void loadAllData() {

        status.setText(
                "ENGINE STATUS: RUNNING\n"
                + "Connecting to market data..."
        );

        loadBTC();
        loadGold();
    }

    private void loadBTC() {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String json =
                                    getUrl(
                                        "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart"
                                        + "?vs_currency=usd"
                                        + "&days=180"
                                        + "&interval=daily"
                                    );

                            final BTCResult result =
                                    analyzeBTC(json);

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            btcCard.setText(
                                                    "₿ BTC\n$ "
                                                    + format(
                                                        result.price
                                                    )
                                            );

                                            supportCard.setText(
                                                    "SUPPORT\n"
                                                    + format(
                                                        result.support
                                                    )
                                            );

                                            resistanceCard.setText(
                                                    "RESISTANCE\n"
                                                    + format(
                                                        result.resistance
                                                    )
                                            );

                                            trendCard.setText(
                                                    "TREND\n"
                                                    + result.trend
                                            );

                                            volumeCard.setText(
                                                    "VOLUME\n"
                                                    + formatVolume(
                                                        result.volume
                                                    )
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: READY\n"
                                                    + "BTC live data loaded"
                                            );
                                        }
                                    }
                            );

                        } catch (final Exception e) {

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            btcCard.setText(
                                                    "₿ BTC\n"
                                                    + "Connection failed"
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: WARNING\n"
                                                    + "BTC data unavailable"
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private void loadGold() {

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String json =
                                    getUrl(
                                        "https://query1.finance.yahoo.com/v8/finance/chart/GC%3DF"
                                        + "?range=6mo"
                                        + "&interval=1d"
                                    );

                            final GoldResult result =
                                    analyzeGold(json);

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            goldCard.setText(
                                                    "GOLD FUTURES\n$ "
                                                    + format(
                                                        result.price
                                                    )
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            goldCard.setText(
                                                    "GOLD FUTURES\n"
                                                    + "Connection failed"
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private String getUrl(
            String address
    ) throws Exception {

        URL url =
                new URL(address);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode < 200
                || responseCode >= 300) {

            throw new Exception(
                    "HTTP " + responseCode
            );
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection
                                        .getInputStream()
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while (
                (line = reader.readLine())
                        != null
        ) {
            response.append(line);
        }

        reader.close();

        connection.disconnect();

        return response.toString();
    }

    private BTCResult analyzeBTC(
            String json
    ) throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONArray prices =
                root.getJSONArray("prices");

        JSONArray volumes =
                root.getJSONArray("total_volumes");

        int size =
                prices.length();

        double latest =
                prices
                    .getJSONArray(size - 1)
                    .getDouble(1);

        double lowest =
                Double.MAX_VALUE;

        double highest =
                -Double.MAX_VALUE;

        double volumeSum = 0;

        int volumeCount = 0;

        double recentSum = 0;

        double previousSum = 0;

        int recentStart =
                Math.max(
                        0,
                        size - 20
                );

        int previousStart =
                Math.max(
                        0,
                        size - 50
                );

        int previousEnd =
                Math.max(
                        0,
                        size - 20
                );

        for (int i = 0;
             i < size;
             i++) {

            double price =
                    prices
                        .getJSONArray(i)
                        .getDouble(1);

            if (price < lowest) {
                lowest = price;
            }

            if (price > highest) {
                highest = price;
            }

            if (i >= recentStart) {
                recentSum += price;
            }

            if (i >= previousStart
                    && i < previousEnd) {

                previousSum += price;
            }

            if (i < volumes.length()) {

                double volume =
                        volumes
                            .getJSONArray(i)
                            .getDouble(1);

                volumeSum += volume;
                volumeCount++;
            }
        }

        double recentSMA =
                recentSum /
                Math.max(
                        1,
                        size - recentStart
                );

        double previousSMA =
                previousEnd > previousStart
                        ? previousSum /
                          (previousEnd
                          - previousStart)
                        : recentSMA;

        String trend =
                "NEUTRAL";

        if (recentSMA > previousSMA) {
            trend = "UP";
        } else if (recentSMA < previousSMA) {
            trend = "DOWN";
        }

        double averageVolume =
                volumeCount > 0
                        ? volumeSum /
                          volumeCount
                        : 0;

        BTCResult result =
                new BTCResult();

        result.price = latest;
        result.support = lowest;
        result.resistance = highest;
        result.trend = trend;
        result.volume = averageVolume;

        return result;
    }

    private GoldResult analyzeGold(
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
                result.getJSONObject(
                        "indicators"
                );

        JSONArray quote =
                indicators.getJSONArray(
                        "quote"
                );

        JSONObject q =
                quote.getJSONObject(0);

        JSONArray close =
                q.getJSONArray("close");

        double latest = 0;

        for (int i = close.length() - 1;
             i >= 0;
             i--) {

            if (!close.isNull(i)) {

                latest =
                        close.getDouble(i);

                break;
            }
        }

        GoldResult resultData =
                new GoldResult();

        resultData.price = latest;

        return resultData;
    }

    private String format(
            double value
    ) {

        if (value >= 1000) {

            return String.format(
                    "%,.2f",
                    value
            );
        }

        return String.format(
                "%.2f",
                value
        );
    }

    private String formatVolume(
            double value
    ) {

        if (value >= 1000000000) {

            return String.format(
                    "%.2fB",
                    value / 1000000000.0
            );

        } else if (value >= 1000000) {

            return String.format(
                    "%.2fM",
                    value / 1000000.0
            );

        } else if (value >= 1000) {

            return String.format(
                    "%.2fK",
                    value / 1000.0
            );
        }

        return String.format(
                "%.0f",
                value
        );
    }

    private static class BTCResult {

        double price;
        double support;
        double resistance;
        double volume;
        String trend;
    }

    private static class GoldResult {

        double price;
    }

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
                                }
