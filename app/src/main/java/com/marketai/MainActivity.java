package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout container;

    private final List<Double> btcOpen =
            new ArrayList<>();

    private final List<Double> btcHigh =
            new ArrayList<>();

    private final List<Double> btcLow =
            new ArrayList<>();

    private final List<Double> btcPrices =
            new ArrayList<>();

    private final List<Double> btcVolume =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ScrollView scrollView =
                new ScrollView(this);

        container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                32,
                32,
                32,
                32
        );

        container.setBackgroundColor(
                Color.rgb(11, 15, 20)
        );

        scrollView.addView(container);

        setContentView(scrollView);

        showTitle("MARKET AI");

        TextView loading =
                addText(
                        "Loading market data...",
                        18,
                        Color.LTGRAY
                );

        loadMarketData(loading);
    }

    // =========================================================
    // LOAD MARKET DATA
    // =========================================================

    private void loadMarketData(
            TextView loading
    ) {

        new Thread(() -> {

            try {

                fetchBTC();

                double goldPrice =
                        fetchGold();

                TechnicalAnalyzer.TechnicalResult technical =
                        TechnicalAnalyzer.analyze(
                                btcPrices,
                                btcHigh,
                                btcLow,
                                btcVolume
                        );

                ProbabilityEngine.ProbabilityResult probability =
                        ProbabilityEngine.calculate(
                                btcPrices,
                                btcHigh,
                                btcLow,
                                btcVolume
                        );

                BacktestEngine.BacktestResult backtest =
                        BacktestEngine.run(
                                btcPrices,
                                btcHigh,
                                btcLow,
                                btcVolume
                        );

                double support =
                        calculateSupport();

                double resistance =
                        calculateResistance();

                double currentPrice =
                        btcPrices.get(
                                btcPrices.size() - 1
                        );

                String trend =
                        getTrend(
                                currentPrice,
                                technical
                        );

                double averageVolume =
                        calculateAverageVolume();

                new Handler(
                        Looper.getMainLooper()
                ).post(() -> {

                    container.removeAllViews();

                    // =================================================
                    // TITLE
                    // =================================================

                    showTitle("MARKET AI");

                    addText(
                            "BTC / GOLD MARKET ANALYSIS",
                            14,
                            Color.GRAY
                    );

                    addSpace();

                    // =================================================
                    // MARKET
                    // =================================================

                    addSection("MARKET");

                    addMetric(
                            "Bitcoin",
                            "$" + format(currentPrice)
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

                    // =================================================
                    // TECHNICAL ANALYSIS
                    // =================================================

                    addSection(
                            "TECHNICAL ANALYSIS"
                    );

                    addMetric(
                            "RSI",
                            format(technical.rsi)
                    );

                    addMetric(
                            "EMA 20",
                            "$" + format(technical.ema20)
                    );

                    addMetric(
                            "EMA 50",
                            "$" + format(technical.ema50)
                    );

                    addMetric(
                            "EMA 200",
                            "$" + format(technical.ema200)
                    );

                    addMetric(
                            "MACD",
                            format(technical.macd)
                    );

                    addMetric(
                            "ATR",
                            "$" + format(technical.atr)
                    );

                    addMetric(
                            "Bollinger Upper",
                            "$" + format(
                                    technical.bollingerUpper
                            )
                    );

                    addMetric(
                            "Bollinger Lower",
                            "$" + format(
                                    technical.bollingerLower
                            )
                    );

                    addMetric(
                            "Momentum",
                            format(
                                    technical.momentum
                            ) + "%"
                    );

                    addMetric(
                            "Volume Ratio",
                            format(
                                    technical.volumeRatio
                            ) + "x"
                    );

                    String signal =
                            getTechnicalSignal(
                                    technical,
                                    currentPrice
                            );

                    addMetric(
                            "Technical Signal",
                            signal
                    );

                    addSpace();

                    // =================================================
                    // AI PROBABILITY
                    // =================================================

                    addSection(
                            "AI PROBABILITY"
                    );

                    addMetric(
                            "BUY Probability",
                            format(
                                    probability.buyProbability
                            ) + "%"
                    );

                    addMetric(
                            "SELL Probability",
                            format(
                                    probability.sellProbability
                            ) + "%"
                    );

                    addMetric(
                            "NEUTRAL Probability",
                            format(
                                    probability.neutralProbability
                            ) + "%"
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

                    addMetric(
                            "Validation Accuracy",
                            format(
                                    probability.validationAccuracy
                            ) + "%"
                    );

                    addMetric(
                            "Validation Samples",
                            String.valueOf(
                                    probability.validationSamples
                            )
                    );

                    addSpace();

                    // =================================================
                    // BACKTEST
                    // =================================================

                    addSection(
                            "BACKTEST V2"
                    );

                    addMetric(
                            "Total Trades",
                            String.valueOf(
                                    backtest.totalTrades
                            )
                    );

                    addMetric(
                            "Correct Trades",
                            String.valueOf(
                                    backtest.correctTrades
                            )
                    );

                    addMetric(
                            "Wrong Trades",
                            String.valueOf(
                                    backtest.wrongTrades
                            )
                    );

                    addMetric(
                            "Neutral Signals",
                            String.valueOf(
                                    backtest.neutralTrades
                            )
                    );

                    addMetric(
                            "BUY Signals",
                            String.valueOf(
                                    backtest.buySignals
                            )
                    );

                    addMetric(
                            "BUY Correct",
                            String.valueOf(
                                    backtest.buyCorrect
                            )
                    );

                    addMetric(
                            "BUY Accuracy",
                            format(
                                    backtest.buyAccuracy
                            ) + "%"
                    );

                    addMetric(
                            "SELL Signals",
                            String.valueOf(
                                    backtest.sellSignals
                            )
                    );

                    addMetric(
                            "SELL Correct",
                            String.valueOf(
                                    backtest.sellCorrect
                            )
                    );

                    addMetric(
                            "SELL Accuracy",
                            format(
                                    backtest.sellAccuracy
                            ) + "%"
                    );

                    addMetric(
                            "Overall Accuracy",
                            format(
                                    backtest.accuracy
                            ) + "%"
                    );

                    addMetric(
                            "Average Return",
                            format(
                                    backtest.averageReturn
                            ) + "%"
                    );

                    addMetric(
                            "Total Return",
                            format(
                                    backtest.totalReturn
                            ) + "%"
                    );

                    // =================================================
                    // V2 RISK METRICS
                    // =================================================

                    addSpace();

                    addSection(
                            "V2 RISK ANALYSIS"
                    );

                    addMetric(
                            "Stop Loss Trades",
                            String.valueOf(
                                    backtest.stopLossTrades
                            )
                    );

                    addMetric(
                            "Take Profit Trades",
                            String.valueOf(
                                    backtest.takeProfitTrades
                            )
                    );

                    addMetric(
                            "Time Exit Trades",
                            String.valueOf(
                                    backtest.timeExitTrades
                            )
                    );

                    addMetric(
                            "Profit Factor",
                            format(
                                    backtest.profitFactor
                            )
                    );

                    addMetric(
                            "Gross Profit",
                            format(
                                    backtest.grossProfit
                            ) + "%"
                    );

                    addMetric(
                            "Gross Loss",
                            format(
                                    backtest.grossLoss
                            ) + "%"
                    );

                    addMetric(
                            "Maximum Drawdown",
                            format(
                                    backtest.maxDrawdown
                            ) + "%"
                    );

                    addMetric(
                            "BUY Average Return",
                            format(
                                    backtest.buyAverageReturn
                            ) + "%"
                    );

                    addMetric(
                            "SELL Average Return",
                            format(
                                    backtest.sellAverageReturn
                            ) + "%"
                    );

                    addMetric(
                            "BUY Total Return",
                            format(
                                    backtest.buyTotalReturn
                            ) + "%"
                    );

                    addMetric(
                            "SELL Total Return",
                            format(
                                    backtest.sellTotalReturn
                            ) + "%"
                    );

                    addMetric(
                            "BTC Buy & Hold",
                            format(
                                    backtest.buyHoldReturn
                            ) + "%"
                    );

                    addSpace();

                    addText(
                            "Backtest uses approximately 2 years of BTC daily historical data.",
                            12,
                            Color.GRAY
                    );

                    addText(
                            "Backtest is a historical simulation. It does not guarantee future results.",
                            12,
                            Color.GRAY
                    );

                });

            } catch (Exception e) {

                new Handler(
                        Looper.getMainLooper()
                ).post(() -> {

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
    // BTC DATA - 2 YEARS
    // =========================================================

    private void fetchBTC()
            throws Exception {

        String urlString =
                "https://api.binance.com/api/v3/klines"
                        + "?symbol=BTCUSDT"
                        + "&interval=1d"
                        + "&limit=730";

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode != 200) {

            throw new Exception(
                    "BTC server error: "
                            + responseCode
            );
        }

        InputStream input =
                connection.getInputStream();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input
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
        input.close();

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

        for (int i = 0;
             i < candles.length();
             i++) {

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

        // Require approximately 2 years of data
        if (btcPrices.size() < 700) {

            throw new Exception(
                    "Not enough BTC historical data. "
                            + "Received: "
                            + btcPrices.size()
                            + " candles"
            );
        }
    }

    // =========================================================
    // GOLD DATA
    // =========================================================

    private double fetchGold()
            throws Exception {

        String urlString =
                "https://query1.finance.yahoo.com/v8/finance/chart/GC=F"
                        + "?range=6mo"
                        + "&interval=1d";

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode != 200) {

            throw new Exception(
                    "Gold server error: "
                            + responseCode
            );
        }

        InputStream input =
                connection.getInputStream();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input
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
        input.close();

        connection.disconnect();

        JSONObject root =
                new JSONObject(
                        response.toString()
                );

        JSONObject chart =
                root.getJSONObject(
                        "chart"
                );

        JSONArray results =
                chart.getJSONArray(
                        "result"
                );

        if (results.length() == 0) {

            throw new Exception(
                    "Gold data unavailable"
            );
        }

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

        JSONObject quoteData =
                quote.getJSONObject(0);

        JSONArray closes =
                quoteData.getJSONArray(
                        "close"
                );

        double latest = 0.0;

        for (int i =
                     closes.length() - 1;
             i >= 0;
             i--) {

            if (!closes.isNull(i)) {

                latest =
                        closes.getDouble(i);

                break;
            }
        }

        if (latest <= 0) {

            throw new Exception(
                    "Gold price unavailable"
            );
        }

        return latest;
    }

    // =========================================================
    // SUPPORT
    // =========================================================

    private double calculateSupport() {

        int size =
                btcLow.size();

        int start =
                Math.max(
                        0,
                        size - 30
                );

        double support =
                Double.MAX_VALUE;

        for (int i = start;
             i < size;
             i++) {

            support =
                    Math.min(
                            support,
                            btcLow.get(i)
                    );
        }

        return support;
    }

    // =========================================================
    // RESISTANCE
    // =========================================================

    private double calculateResistance() {

        int size =
                btcHigh.size();

        int start =
                Math.max(
                        0,
                        size - 30
                );

        double resistance =
                Double.MIN_VALUE;

        for (int i = start;
             i < size;
             i++) {

            resistance =
                    Math.max(
                            resistance,
                            btcHigh.get(i)
                    );
        }

        return resistance;
    }

    // =========================================================
    // TREND
    // =========================================================

    private String getTrend(
            double price,
            TechnicalAnalyzer.TechnicalResult result
    ) {

        int bullish = 0;
        int bearish = 0;

        if (result.ema20 > 0) {

            if (price > result.ema20) {
                bullish++;
            } else {
                bearish++;
            }
        }

        if (result.ema50 > 0) {

            if (price > result.ema50) {
                bullish++;
            } else {
                bearish++;
            }
        }

        if (result.ema200 > 0) {

            if (price > result.ema200) {
                bullish++;
            } else {
                bearish++;
            }
        }

        if (result.momentum > 0) {

            bullish++;

        } else if (result.momentum < 0) {

            bearish++;
        }

        if (bullish >= 3) {
            return "UP";
        }

        if (bearish >= 3) {
            return "DOWN";
        }

        return "NEUTRAL";
    }

    // =========================================================
    // TECHNICAL SIGNAL
    // =========================================================

    private String getTechnicalSignal(
            TechnicalAnalyzer.TechnicalResult result,
            double price
    ) {

        int bullish = 0;
        int bearish = 0;

        if (result.rsi > 50) {

            bullish++;

        } else if (result.rsi < 50) {

            bearish++;
        }

        if (result.macd > 0) {

            bullish++;

        } else {

            bearish++;
        }

        if (result.momentum > 0) {

            bullish++;

        } else if (result.momentum < 0) {

            bearish++;
        }

        if (result.ema50 > 0) {

            if (price > result.ema50) {
                bullish++;
            } else {
                bearish++;
            }
        }

        if (result.ema200 > 0) {

            if (price > result.ema200) {
                bullish++;
            } else {
                bearish++;
            }
        }

        if (bullish >= 4) {
            return "BUY BIAS";
        }

        if (bearish >= 4) {
            return "SELL BIAS";
        }

        return "NEUTRAL";
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
    // UI TITLE
    // =========================================================

    private void showTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        title.setPadding(
                0,
                10,
                0,
                10
        );

        container.addView(title);
    }

    // =========================================================
    // SECTION
    // =========================================================

    private void addSection(
            String text
    ) {

        TextView section =
                new TextView(this);

        section.setText(text);
        section.setTextSize(17);
        section.setTextColor(Color.WHITE);

        section.setPadding(
                0,
                20,
                0,
                10
        );

        container.addView(section);
    }

    // =========================================================
    // METRIC
    // =========================================================

    private void addMetric(
            String name,
            String value
    ) {

        TextView metric =
                new TextView(this);

        metric.setText(
                name
                        + "    "
                        + value
        );

        metric.setTextSize(16);
        metric.setTextColor(Color.LTGRAY);

        metric.setPadding(
                0,
                8,
                0,
                8
        );

        container.addView(metric);
    }

    // =========================================================
    // TEXT
    // =========================================================

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

        view.setPadding(
                0,
                8,
                0,
                8
        );

        container.addView(view);

        return view;
    }

    // =========================================================
    // SPACE
    // =========================================================

    private void addSpace() {

        TextView space =
                new TextView(this);

        space.setText("");

        space.setPadding(
                0,
                10,
                0,
                10
        );

        container.addView(space);
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private String format(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }
                        }
