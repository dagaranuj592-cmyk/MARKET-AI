package com.marketai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout container;

    private final List<Double> btcOpen = new ArrayList<>();
    private final List<Double> btcHigh = new ArrayList<>();
    private final List<Double> btcLow = new ArrayList<>();
    private final List<Double> btcPrices = new ArrayList<>();
    private final List<Double> btcVolume = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 32, 32, 32);
        container.setBackgroundColor(Color.rgb(11, 15, 20));

        scrollView.addView(container);
        setContentView(scrollView);

        showTitle("MARKET AI");

        TextView loading = addText(
                "Loading market data...",
                18,
                Color.LTGRAY
        );

        loadMarketData(loading);
    }

    private void loadMarketData(TextView loading) {

        new Thread(() -> {

            try {

                fetchBTC();

                double goldPrice = fetchGold();

                TechnicalAnalyzer.TechnicalResult technical =
                        TechnicalAnalyzer.analyze(
                                btcPrices,
                                btcHigh,
                                btcLow
                        );

                ProbabilityEngine.ProbabilityResult probability =
                        ProbabilityEngine.calculate(
                                btcPrices,
                                btcHigh,
                                btcLow
                        );

                double support = calculateSupport();
                double resistance = calculateResistance();

                String trend = getTrend(
                        btcPrices,
                        technical.ema20
                );

                double averageVolume =
                        calculateAverageVolume();

                new Handler(Looper.getMainLooper()).post(() -> {

                    container.removeAllViews();

                    showTitle("MARKET AI");

                    addText(
                            "BTC / GOLD MARKET ANALYSIS",
                            14,
                            Color.GRAY
                    );

                    addSpace();

                    addSection("MARKET");

                    addMetric(
                            "Bitcoin",
                            "$" + format(btcPrices.get(
                                    btcPrices.size() - 1
                            ))
                    );

                    addMetric(
                            "Gold",
                            "$" + format(goldPrice)
                    );

                    addMetric(
                            "Support",
                            "$" + format(support)
                    );

                    addMetric(
                            "Resistance",
                            "$" + format(resistance)
                    );

                    addMetric(
                            "Trend",
                            trend
                    );

                    addMetric(
                            "Average Volume",
                            format(averageVolume)
                    );

                    addSpace();

                    addSection("TECHNICAL ANALYSIS");

                    addMetric(
                            "RSI",
                            format(technical.rsi)
                    );

                    addMetric(
                            "EMA 20",
                            "$" + format(technical.ema20)
                    );

                    addMetric(
                            "MACD",
                            format(technical.macd)
                    );

                    addMetric(
                            "ATR",
                            "$" + format(technical.atr)
                    );

                    String signal =
                            getTechnicalSignal(
                                    technical,
                                    trend
                            );

                    addMetric(
                            "Technical Signal",
                            signal
                    );

                    addSpace();

                    addSection("AI PROBABILITY");

                    addMetric(
                            "BUY Probability",
                            format(probability.buyProbability) + "%"
                    );

                    addMetric(
                            "SELL Probability",
                            format(probability.sellProbability) + "%"
                    );

                    addMetric(
                            "NEUTRAL Probability",
                            format(probability.neutralProbability) + "%"
                    );

                    addMetric(
                            "Direction",
                            probability.direction
                    );

                    addMetric(
                            "Confidence",
                            probability.confidence
                    );

                    addMetric(
                            "Historical Samples",
                            String.valueOf(
                                    probability.samples
                            )
                    );

                    addSpace();

                    addText(
                            "Historical/model estimate only — not a guarantee of future price movement.",
                            12,
                            Color.GRAY
                    );

                });

            } catch (Exception e) {

                new Handler(Looper.getMainLooper()).post(() -> {

                    container.removeAllViews();

                    showTitle("MARKET AI");

                    addText(
                            "Market data loading failed.",
                            18,
                            Color.RED
                    );

                    addSpace();

                    addText(
                            e.getMessage() == null
                                    ? "Unknown error"
                                    : e.getMessage(),
                            14,
                            Color.LTGRAY
                    );
                });
            }

        }).start();
    }

    // =========================================================
    // BTC DATA - BINANCE DAILY OHLCV
    // =========================================================

    private void fetchBTC() throws Exception {

        String urlString =
                "https://api.binance.com/api/v3/klines"
                        + "?symbol=BTCUSDT"
                        + "&interval=1d"
                        + "&limit=180";

        URL url = new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        int responseCode =
                connection.getResponseCode();

        if (responseCode != 200) {

            throw new Exception(
                    "BTC data server error: "
                            + responseCode
            );
        }

        InputStream inputStream =
                connection.getInputStream();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        inputStream.close();
        connection.disconnect();

        JSONArray candles =
                new JSONArray(
                        response.toString()
                );

        btcOpen.clear();
        btcHigh.clear();
        btcLow.clear();
        btcPrices.clear();
        btcVolume.clear();

        for (int i = 0; i < candles.length(); i++) {

            JSONArray candle =
                    candles.getJSONArray(i);

            double open =
                    Double.parseDouble(
                            candle.getString(1)
                    );

            double high =
                    Double.parseDouble(
                            candle.getString(2)
                    );

            double low =
                    Double.parseDouble(
                            candle.getString(3)
                    );

            double close =
                    Double.parseDouble(
                            candle.getString(4)
                    );

            double volume =
                    Double.parseDouble(
                            candle.getString(5)
                    );

            btcOpen.add(open);
            btcHigh.add(high);
            btcLow.add(low);
            btcPrices.add(close);
            btcVolume.add(volume);
        }

        if (btcPrices.size() < 30) {

            throw new Exception(
                    "Not enough BTC historical data"
            );
        }
    }

    // =========================================================
    // GOLD DATA
    // =========================================================

    private double fetchGold() throws Exception {

        String urlString =
                "https://query1.finance.yahoo.com/v8/finance/chart/GC=F"
                        + "?range=6mo"
                        + "&interval=1d";

        URL url = new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode != 200) {

            throw new Exception(
                    "Gold data server error: "
                            + responseCode
            );
        }

        InputStream inputStream =
                connection.getInputStream();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        inputStream.close();
        connection.disconnect();

        org.json.JSONObject root =
                new org.json.JSONObject(
                        response.toString()
                );

        org.json.JSONObject chart =
                root.getJSONObject("chart");

        JSONArray results =
                chart.getJSONArray("result");

        if (results.length() == 0) {

            throw new Exception(
                    "Gold data unavailable"
            );
        }

        org.json.JSONObject result =
                results.getJSONObject(0);

        org.json.JSONObject indicators =
                result.getJSONObject(
                        "indicators"
                );

        JSONArray quote =
                indicators.getJSONArray("quote");

        org.json.JSONObject quoteData =
                quote.getJSONObject(0);

        JSONArray closeArray =
                quoteData.getJSONArray("close");

        double latestGold = 0.0;

        for (int i = closeArray.length() - 1;
             i >= 0;
             i--) {

            if (!closeArray.isNull(i)) {

                latestGold =
                        closeArray.getDouble(i);

                break;
            }
        }

        if (latestGold <= 0) {

            throw new Exception(
                    "Gold price unavailable"
            );
        }

        return latestGold;
    }

    // =========================================================
    // SUPPORT
    // =========================================================

    private double calculateSupport() {

        int size = btcLow.size();

        int start =
                Math.max(
                        0,
                        size - 30
                );

        double support =
                Double.MAX_VALUE;

        for (int i = start; i < size; i++) {

            if (btcLow.get(i) < support) {

                support = btcLow.get(i);
            }
        }

        return support;
    }

    // =========================================================
    // RESISTANCE
    // =========================================================

    private double calculateResistance() {

        int size = btcHigh.size();

        int start =
                Math.max(
                        0,
                        size - 30
                );

        double resistance =
                Double.MIN_VALUE;

        for (int i = start; i < size; i++) {

            if (btcHigh.get(i) > resistance) {

                resistance = btcHigh.get(i);
            }
        }

        return resistance;
    }

    // =========================================================
    // TREND
    // =========================================================

    private String getTrend(
            List<Double> prices,
            double ema20
    ) {

        if (prices == null ||
                prices.size() < 5) {

            return "NEUTRAL";
        }

        double current =
                prices.get(
                        prices.size() - 1
                );

        double previous =
                prices.get(
                        prices.size() - 5
                );

        if (current > ema20 &&
                current > previous) {

            return "UP";
        }

        if (current < ema20 &&
                current < previous) {

            return "DOWN";
        }

        return "NEUTRAL";
    }

    // =========================================================
    // TECHNICAL SIGNAL
    // =========================================================

    private String getTechnicalSignal(
            TechnicalAnalyzer.TechnicalResult result,
            String trend
    ) {

        int score = 0;

        if (result.rsi > 50) {
            score++;
        }

        if (result.ema20 > 0 &&
                btcPrices.get(
                        btcPrices.size() - 1
                ) > result.ema20) {

            score++;
        }

        if (result.macd > 0) {
            score++;
        }

        if (trend.equals("UP")) {
            score++;
        }

        if (score >= 3) {

            return "BUY BIAS";

        } else if (score <= 1) {

            return "SELL BIAS";

        } else {

            return "NEUTRAL";
        }
    }

    // =========================================================
    // AVERAGE VOLUME
    // =========================================================

    private double calculateAverageVolume() {

        if (btcVolume.isEmpty()) {
            return 0.0;
        }

        int count =
                Math.min(
                        20,
                        btcVolume.size()
                );

        int start =
                btcVolume.size() - count;

        double sum = 0.0;

        for (int i = start;
             i < btcVolume.size();
             i++) {

            sum += btcVolume.get(i);
        }

        return sum / count;
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private void showTitle(String text) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 10);

        container.addView(title);
    }

    private void addSection(String text) {

        TextView section =
                new TextView(this);

        section.setText(text);
        section.setTextSize(17);
        section.setTextColor(Color.WHITE);
        section.setPadding(0, 20, 0, 10);

        container.addView(section);
    }

    private void addMetric(
            String name,
            String value
    ) {

        TextView metric =
                new TextView(this);

        metric.setText(
                name + "    " + value
        );

        metric.setTextSize(16);
        metric.setTextColor(Color.LTGRAY);
        metric.setPadding(0, 8, 0, 8);

        container.addView(metric);
    }

    private TextView addText(
            String text,
            int size,
            int color
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, 8, 0, 8);

        container.addView(view);

        return view;
    }

    private void addSpace() {

        TextView space =
                new TextView(this);

        space.setText("");
        space.setPadding(0, 10, 0, 10);

        container.addView(space);
    }

    private String format(double value) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }
             }
