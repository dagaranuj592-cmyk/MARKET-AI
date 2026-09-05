package com.example.marketai;

import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    LinearLayout root;
    TextView btcPrice, goldPrice;
    TextView supportText, resistanceText;
    TextView trendText, volumeText;
    TextView statusText;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUI();
        loadMarketData();
    }

    private void buildUI() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 35, 24, 25);
        root.setBackgroundColor(Color.rgb(12, 18, 30));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);

        TextView title = text(
                "MARKET AI",
                30,
                Color.WHITE,
                Gravity.CENTER
        );

        root.addView(title);

        TextView subtitle = text(
                "BTC  •  GOLD\nMarket Analysis Engine",
                20,
                Color.LTGRAY,
                Gravity.CENTER
        );

        root.addView(subtitle);

        LinearLayout assets = new LinearLayout(this);
        assets.setOrientation(LinearLayout.HORIZONTAL);

        btcPrice = card("₿ BTC\nLoading...");
        goldPrice = card("GOLD\nLoading...");

        assets.addView(btcPrice,
                new LinearLayout.LayoutParams(0, 180, 1));

        assets.addView(goldPrice,
                new LinearLayout.LayoutParams(0, 180, 1));

        root.addView(assets);

        TextView heading = text(
                "\nMARKET ANALYSIS",
                22,
                Color.WHITE,
                Gravity.LEFT
        );

        root.addView(heading);

        LinearLayout analysis = new LinearLayout(this);
        analysis.setOrientation(LinearLayout.VERTICAL);

        supportText = card("SUPPORT\nCalculating...");
        resistanceText = card("RESISTANCE\nCalculating...");
        trendText = card("TREND\nWaiting...");
        volumeText = card("VOLUME\nWaiting...");

        analysis.addView(supportText);
        analysis.addView(resistanceText);
        analysis.addView(trendText);
        analysis.addView(volumeText);

        root.addView(analysis);

        TextView chart = text(
                "\nPRICE DATA\n\nHistorical + Live Market Data\n\n" +
                "BTC and Gold analysis engine",
                20,
                Color.WHITE,
                Gravity.CENTER
        );

        chart.setPadding(10, 50, 10, 50);
        root.addView(chart);

        Button analyse = new Button(this);
        analyse.setText("🔍  RUN FULL ANALYSIS");
        analyse.setTextSize(18);

        analyse.setOnClickListener(v -> loadMarketData());

        root.addView(analyse);

        statusText = text(
                "\nENGINE STATUS: READY\nConnecting to market data...",
                16,
                Color.LTGRAY,
                Gravity.CENTER
        );

        root.addView(statusText);
    }

    private TextView text(
            String value,
            float size,
            int color,
            int gravity
    ) {
        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(gravity);
        t.setPadding(10, 15, 10, 15);

        return t;
    }

    private TextView card(String value) {

        TextView t = text(
                value,
                18,
                Color.WHITE,
                Gravity.CENTER
        );

        t.setBackgroundColor(Color.rgb(30, 43, 65));
        t.setPadding(15, 35, 15, 35);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        170
                );

        p.setMargins(5, 8, 5, 8);

        t.setLayoutParams(p);

        return t;
    }

    private void loadMarketData() {

        statusText.setText(
                "ENGINE STATUS: RUNNING\nFetching live market data..."
        );

        executor.execute(() -> {

            try {

                MarketData btc =
                        getMarketData("BTC-USD");

                MarketData gold =
                        getMarketData("GC%3DF");

                runOnUiThread(() -> {

                    if (btc != null) {

                        btcPrice.setText(
                                "₿ BTC\n$" +
                                format(btc.price)
                        );

                        supportText.setText(
                                "SUPPORT\n$" +
                                format(btc.support)
                        );

                        resistanceText.setText(
                                "RESISTANCE\n$" +
                                format(btc.resistance)
                        );

                        trendText.setText(
                                "TREND\n" + btc.trend
                        );

                        volumeText.setText(
                                "VOLUME\n" +
                                formatVolume(btc.volume)
                        );
                    }

                    if (gold != null) {

                        goldPrice.setText(
                                "GOLD\n$" +
                                format(gold.price)
                        );
                    }

                    statusText.setText(
                            "ENGINE STATUS: READY\n" +
                            "Live market data connected"
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        statusText.setText(
                                "ENGINE STATUS: ERROR\n" +
                                "Could not load market data"
                        )
                );
            }
        });
    }

    private MarketData getMarketData(String symbol)
            throws Exception {

        long now = System.currentTimeMillis() / 1000;
        long weekAgo = now - (7 * 24 * 60 * 60);

        String api =
                "https://query1.finance.yahoo.com/v8/finance/chart/"
                        + symbol
                        + "?period1="
                        + weekAgo
                        + "&period2="
                        + now
                        + "&interval=1h";

        URL url = new URL(api);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );

        StringBuilder response = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        JSONObject json =
                new JSONObject(response.toString());

        JSONObject result =
                json.getJSONObject("chart")
                        .getJSONArray("result")
                        .getJSONObject(0);

        JSONObject indicators =
                result.getJSONObject("indicators");

        JSONArray quote =
                indicators
                        .getJSONArray("quote")
                        .getJSONObject(0)
                        .getJSONArray("close");

        JSONArray volumes =
                indicators
                        .getJSONArray("quote")
                        .getJSONObject(0)
                        .getJSONArray("volume");

        List<Double> prices =
                new ArrayList<>();

        List<Double> volumeList =
                new ArrayList<>();

        for (int i = 0; i < quote.length(); i++) {

            if (!quote.isNull(i)) {
                prices.add(quote.getDouble(i));
            }

            if (!volumes.isNull(i)) {
                volumeList.add(
                        volumes.getDouble(i)
                );
            }
        }

        if (prices.size() < 5) {
            return null;
        }

        double current =
                prices.get(prices.size() - 1);

        double support =
                findSupport(prices);

        double resistance =
                findResistance(prices);

        String trend =
                calculateTrend(prices);

        double averageVolume =
                average(volumeList);

        MarketData data = new MarketData();

        data.price = current;
        data.support = support;
        data.resistance = resistance;
        data.trend = trend;
        data.volume = averageVolume;

        return data;
    }

    private double findSupport(List<Double> prices) {

        int start =
                Math.max(0, prices.size() - 50);

        double lowest =
                Double.MAX_VALUE;

        for (int i = start; i < prices.size(); i++) {

            if (prices.get(i) < lowest) {
                lowest = prices.get(i);
            }
        }

        return lowest;
    }

    private double findResistance(List<Double> prices) {

        int start =
                Math.max(0, prices.size() - 50);

        double highest =
                Double.MIN_VALUE;

        for (int i = start; i < prices.size(); i++) {

            if (prices.get(i) > highest) {
                highest = prices.get(i);
            }
        }

        return highest;
    }

    private String calculateTrend(List<Double> prices) {

        int shortPeriod =
                Math.min(20, prices.size());

        int longPeriod =
                Math.min(50, prices.size());

        double shortAverage = 0;
        double longAverage = 0;

        for (int i =
             prices.size() - shortPeriod;
             i < prices.size();
             i++) {

            shortAverage += prices.get(i);
        }

        for (int i =
             prices.size() - longPeriod;
             i < prices.size();
             i++) {

            longAverage += prices.get(i);
        }

        shortAverage /= shortPeriod;
        longAverage /= longPeriod;

        if (shortAverage > longAverage) {
            return "BULLISH";
        }

        if (shortAverage < longAverage) {
            return "BEARISH";
        }

        return "SIDEWAYS";
    }

    private double average(List<Double> values) {

        if (values.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (double value : values) {
            total += value;
        }

        return total / values.size();
    }

    private String format(double value) {

        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }

    private String formatVolume(double value) {

        if (value >= 1_000_000_000) {
            return String.format(
                    Locale.US,
                    "%.2f B",
                    value / 1_000_000_000
            );
        }

        if (value >= 1_000_000) {
            return String.format(
                    Locale.US,
                    "%.2f M",
                    value / 1_000_000
            );
        }

        if (value >= 1_000) {
            return String.format(
                    Locale.US,
                    "%.2f K",
                    value / 1_000
            );
        }

        return format(value);
    }

    static class MarketData {

        double price;
        double support;
        double resistance;
        double volume;
        String trend;
    }

    @Override
    protected void onDestroy() {

        executor.shutdown();

        super.onDestroy();
    }
            }
