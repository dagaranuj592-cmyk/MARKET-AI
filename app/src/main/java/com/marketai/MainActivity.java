package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
    private ScrollView scrollView;

    private boolean isRefreshing = false;

    private final List<Double> btcOpen = new ArrayList<>();
    private final List<Double> btcHigh = new ArrayList<>();
    private final List<Double> btcLow = new ArrayList<>();
    private final List<Double> btcPrices = new ArrayList<>();
    private final List<Double> btcVolume = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // =====================================================
        // SCROLL VIEW
        // =====================================================

        scrollView = new ScrollView(this);

        container = new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                32,
                32,
                32,
                40
        );

        container.setBackgroundColor(
                Color.rgb(11, 15, 20)
        );

        scrollView.addView(container);

        setContentView(scrollView);


        // =====================================================
        // PULL TO REFRESH
        // =====================================================

        scrollView.setOnTouchListener(
                (view, event) -> {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            view.setTag(
                                    event.getY()
                            );

                            break;


                        case MotionEvent.ACTION_UP:

                            Object tag =
                                    view.getTag();

                            if (tag instanceof Float) {

                                float startY =
                                        (Float) tag;

                                float endY =
                                        event.getY();

                                float distance =
                                        endY - startY;


                                if (
                                        scrollView.getScrollY() == 0
                                                &&
                                        distance > 150
                                                &&
                                        !isRefreshing
                                ) {

                                    refreshMarketData();
                                }
                            }

                            break;
                    }

                    return false;
                }
        );


        // =====================================================
        // INITIAL SCREEN
        // =====================================================

        showTitle(
                "MARKET AI"
        );

        addText(
                "Loading market data...",
                18,
                Color.LTGRAY
        );

        addSpace();

        loadMarketData();
    }


    // =========================================================
    // REFRESH BUTTON ACTION
    // =========================================================

    private void refreshMarketData() {

        if (isRefreshing) {
            return;
        }

        isRefreshing = true;

        container.removeAllViews();

        showTitle(
                "MARKET AI"
        );

        addText(
                "Refreshing market data...",
                17,
                Color.LTGRAY
        );

        addSpace();

        loadMarketData();
    }


    // =========================================================
    // LOAD MARKET DATA
    // =========================================================

    private void loadMarketData() {

        new Thread(
                () -> {

                    try {

                        // =================================================
                        // FETCH BTC
                        // =================================================

                        fetchBTC();


                        // =================================================
                        // FETCH GOLD
                        // =================================================

                        double goldPrice =
                                fetchGold();


                        // =================================================
                        // TECHNICAL
                        // =================================================

                        TechnicalAnalyzer.TechnicalResult technical =
                                TechnicalAnalyzer.analyze(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // PROBABILITY
                        // =================================================

                        ProbabilityEngine.ProbabilityResult probability =
                                ProbabilityEngine.calculate(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // LEARNING
                        // =================================================

                        LearningEngine.LearningResult learning =
                                LearningEngine.learn(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // COMBINED ENGINE
                        // =================================================

                        CombinedEngine.CombinedResult combined =
                                CombinedEngine.analyze(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // ORIGINAL BACKTEST V3
                        // =================================================

                        BacktestEngine.BacktestResult backtest =
                                BacktestEngine.run(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // COMBINED BACKTEST
                        // =================================================

                        CombinedBacktestEngine.Result combinedBacktest =
                                CombinedBacktestEngine.run(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // MARKET VALUES
                        // =================================================

                        double currentPrice =
                                btcPrices.get(
                                        btcPrices.size() - 1
                                );


                        double support =
                                calculateSupport();


                        double resistance =
                                calculateResistance();


                        String trend =
                                getTrend(
                                        currentPrice,
                                        technical
                                );


                        double averageVolume =
                                calculateAverageVolume();


                        // =================================================
                        // MAIN THREAD
                        // =================================================

                        new Handler(
                                Looper.getMainLooper()
                        ).post(
                                () -> {

                                    isRefreshing =
                                            false;

                                    showCompleteScreen(
                                            currentPrice,
                                            goldPrice,
                                            support,
                                            resistance,
                                            trend,
                                            averageVolume,
                                            technical,
                                            probability,
                                            learning,
                                            combined,
                                            backtest,
                                            combinedBacktest
                                    );
                                }
                        );

                    } catch (Exception e) {

                        new Handler(
                                Looper.getMainLooper()
                        ).post(
                                () -> {

                                    isRefreshing =
                                            false;

                                    showError(
                                            e
                                    );
                                }
                        );
                    }

                }
        ).start();
    }


    // =========================================================
    // COMPLETE SCREEN
    // =========================================================

    private void showCompleteScreen(
            double currentPrice,
            double goldPrice,
            double support,
            double resistance,
            String trend,
            double averageVolume,
            TechnicalAnalyzer.TechnicalResult technical,
            ProbabilityEngine.ProbabilityResult probability,
            LearningEngine.LearningResult learning,
            CombinedEngine.CombinedResult combined,
            BacktestEngine.BacktestResult backtest,
            CombinedBacktestEngine.Result combinedBacktest
    ) {

        container.removeAllViews();


        // =====================================================
        // HEADER
        // =====================================================

        showTitle(
                "MARKET AI"
        );

        addText(
                "BTC / GOLD ANALYTICAL ENGINE",
                13,
                Color.GRAY
        );

        addSpace();


        // =====================================================
        // REFRESH BUTTON
        // =====================================================

        TextView refreshButton =
                new TextView(this);

        refreshButton.setText(
                "↻  REFRESH MARKET DATA"
        );

        refreshButton.setTextSize(
                16
        );

        refreshButton.setTextColor(
                Color.WHITE
        );

        refreshButton.setGravity(
                Gravity.CENTER
        );

        refreshButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        refreshButton.setPadding(
                10,
                20,
                10,
                20
        );

        refreshButton.setBackgroundColor(
                Color.rgb(35, 55, 65)
        );

        refreshButton.setOnClickListener(
                view -> refreshMarketData()
        );

        container.addView(
                refreshButton
        );

        addSpace();


        // =====================================================
        // FINAL SIGNAL
        // =====================================================

        addSection(
                "FINAL SIGNAL"
        );


        String finalSignal =
                combined.direction;


        // WAIT / NEUTRAL
        if (
                "NEUTRAL".equals(
                        finalSignal
                )
        ) {

            addBigSignal(
                    "WAIT",
                    Color.YELLOW
            );

        } else if (
                "BUY".equals(
                        finalSignal
                )
        ) {

            addBigSignal(
                    "BUY",
                    Color.GREEN
            );

        } else {

            addBigSignal(
                    "SELL",
                    Color.RED
            );
        }


        addMetric(
                "Calibrated Confidence",
                format(
                        combined.confidence
                ) + "%"
        );


        addMetric(
                "Signal Strength",
                combined.signalStrength
        );


        addMetric(
                "Engine Agreement",
                combined.agreement
        );


        if (
                "NEUTRAL".equals(
                        combined.direction
                )
        ) {

            addText(
                    "No clear edge. The system is not forcing BUY or SELL.",
                    13,
                    Color.GRAY
            );

        } else {

            addText(
                    "Final direction is based on combined analytical engines.",
                    13,
                    Color.GRAY
            );
        }


        addSpace();


        // =====================================================
        // ENTRY / EXIT
        // =====================================================

        addSection(
                "TRADE REFERENCE"
        );


        addMetric(
                "Entry Price",
                "$" + format(
                        currentPrice
                )
        );


        addMetric(
                "Live Exit Price",
                "NOT AVAILABLE"
        );


        addText(
                "Future exit price cannot be known from live market data.",
                12,
                Color.GRAY
        );


        addSpace();


        // =====================================================
        // MARKET
        // =====================================================

        addSection(
                "MARKET"
        );


        addMetric(
                "Bitcoin",
                "$" + format(
                        currentPrice
                )
        );


        addMetric(
                "Gold",
                "$" + format(
                        goldPrice
                )
        );


        addMetric(
                "Support",
                "$" + format(
                        support
                )
        );


        addMetric(
                "Resistance",
                "$" + format(
                        resistance
                )
        );


        addMetric(
                "Trend",
                trend
        );


        addMetric(
                "Average Volume",
                format(
                        averageVolume
                )
        );


        addSpace();


        // =====================================================
        // FINAL PROBABILITIES
        // =====================================================

        addSection(
                "FINAL PROBABILITIES"
        );


        addMetric(
                "BUY",
                format(
                        combined.buyProbability
                ) + "%"
        );


        addMetric(
                "SELL",
                format(
                        combined.sellProbability
                ) + "%"
        );


        addMetric(
                "WAIT / NEUTRAL",
                format(
                        combined.neutralProbability
                ) + "%"
        );


        addSpace();


        // =====================================================
        // ENGINE AGREEMENT
        // =====================================================

        addSection(
                "ENGINE AGREEMENT"
        );


        addMetric(
                "Technical",
                getTechnicalDirection(
                        combined.technicalBuy,
                        combined.technicalSell,
                        combined.technicalNeutral
                )
        );


        addMetric(
                "Probability",
                probability.direction
        );


        addMetric(
                "Learning",
                learning.direction
        );


        addMetric(
                "Agreement",
                combined.agreement
        );


        addSpace();


        // =====================================================
        // COMBINED BACKTEST
        // =====================================================

        addSection(
                "COMBINED BACKTEST"
        );


        addMetric(
                "Total Signals",
                String.valueOf(
                        combinedBacktest.totalSignals
                )
        );


        addMetric(
                "Correct Signals",
                String.valueOf(
                        combinedBacktest.correctSignals
                )
        );


        addMetric(
                "Wrong Signals",
                String.valueOf(
                        combinedBacktest.wrongSignals
                )
        );


        addMetric(
                "Neutral Signals",
                String.valueOf(
                        combinedBacktest.neutralSignals
                )
        );


        addMetric(
                "BUY Signals",
                String.valueOf(
                        combinedBacktest.buySignals
                )
        );


        addMetric(
                "SELL Signals",
                String.valueOf(
                        combinedBacktest.sellSignals
                )
        );


        addMetric(
                "Overall Accuracy",
                format(
                        combinedBacktest.accuracy
                ) + "%"
        );


        addMetric(
                "BUY Accuracy",
                format(
                        combinedBacktest.buyAccuracy
                ) + "%"
        );


        addMetric(
                "SELL Accuracy",
                format(
                        combinedBacktest.sellAccuracy
                ) + "%"
        );


        addMetric(
                "Average Return",
                format(
                        combinedBacktest.averageReturn
                ) + "%"
        );


        addMetric(
                "Total Return",
                format(
                        combinedBacktest.totalReturn
                ) + "%"
        );


        addMetric(
                "Profit Factor",
                format(
                        combinedBacktest.profitFactor
                )
        );


        addMetric(
                "Maximum Drawdown",
                format(
                        combinedBacktest.maxDrawdown
                ) + "%"
        );


        addMetric(
                "BUY Agreement Signals",
                String.valueOf(
                        combinedBacktest.buyAgreementSignals
                )
        );


        addMetric(
                "SELL Agreement Signals",
                String.valueOf(
                        combinedBacktest.sellAgreementSignals
                )
        );


        addMetric(
                "STRONG Signals",
                String.valueOf(
                        combinedBacktest.strongSignals
                )
        );


        addMetric(
                "MODERATE Signals",
                String.valueOf(
                        combinedBacktest.moderateSignals
                )
        );


        addMetric(
                "WEAK Signals",
                String.valueOf(
                        combinedBacktest.weakSignals
                )
        );


        addSpace();


        // =====================================================
        // TECHNICAL DETAILS
        // =====================================================

        addSection(
                "TECHNICAL DETAILS"
        );


        addMetric(
                "RSI",
                format(
                        technical.rsi
                )
        );


        addMetric(
                "EMA 20",
                "$" + format(
                        technical.ema20
                )
        );


        addMetric(
                "EMA 50",
                "$" + format(
                        technical.ema50
                )
        );


        addMetric(
                "EMA 200",
                "$" + format(
                        technical.ema200
                )
        );


        addMetric(
                "MACD",
                format(
                        technical.macd
                )
        );


        addMetric(
                "ATR",
                "$" + format(
                        technical.atr
                )
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


        addMetric(
                "Technical Signal",
                getTechnicalSignal(
                        technical,
                        currentPrice
                )
        );


        addSpace();


        // =====================================================
        // AI DETAILS
        // =====================================================

        addSection(
                "AI ENGINE DETAILS"
        );


        addMetric(
                "Probability BUY",
                format(
                        probability.buyProbability
                ) + "%"
        );


        addMetric(
                "Probability SELL",
                format(
                        probability.sellProbability
                ) + "%"
        );


        addMetric(
                "Probability WAIT",
                format(
                        probability.neutralProbability
                ) + "%"
        );


        addMetric(
                "Probability Direction",
                probability.direction
        );


        addMetric(
                "Probability Confidence",
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


        addMetric(
                "Learning Direction",
                learning.direction
        );


        addMetric(
                "Learning BUY",
                format(
                        learning.buyProbability
                ) + "%"
        );


        addMetric(
                "Learning SELL",
                format(
                        learning.sellProbability
                ) + "%"
        );


        addMetric(
                "Learning WAIT",
                format(
                        learning.neutralProbability
                ) + "%"
        );


        addMetric(
                "Learning Matched Samples",
                String.valueOf(
                        learning.matchedSamples
                )
        );


        addMetric(
                "Learning Test Accuracy",
                format(
                        learning.trainingAccuracy
                ) + "%"
        );


        addSpace();


        // =====================================================
        // ORIGINAL BACKTEST V3
        // =====================================================

        addSection(
                "BACKTEST V3"
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


        addSpace();


        // =====================================================
        // V3 RISK
        // =====================================================

        addSection(
                "V3 RISK ANALYSIS"
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


        // =====================================================
        // INFORMATION
        // =====================================================

        addText(
                "Historical Data: ~2 Years BTC Daily Candles",
                12,
                Color.GRAY
        );


        addText(
                "Combined Backtest uses the Technical + Probability + Learning engines.",
                12,
                Color.GRAY
        );


        addText(
                "Historical backtests and model probabilities are research results and do not guarantee future results.",
                12,
                Color.GRAY
        );
    }


    // =========================================================
    // ERROR SCREEN
    // =========================================================

    private void showError(
            Exception e
    ) {

        container.removeAllViews();

        showTitle(
                "MARKET AI"
        );


        addText(
                "Market data loading failed.",
                18,
                Color.RED
        );


        addSpace();


        String message =
                e.getMessage();


        if (
                message == null
                        ||
                message.trim().isEmpty()
        ) {

            message =
                    "Unknown error";
        }


        addText(
                message,
                14,
                Color.LTGRAY
        );


        addSpace();


        TextView retry =
                new TextView(this);


        retry.setText(
                "↻  TRY AGAIN"
        );


        retry.setTextSize(
                16
        );


        retry.setTextColor(
                Color.WHITE
        );


        retry.setGravity(
                Gravity.CENTER
        );


        retry.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        retry.setPadding(
                10,
                20,
                10,
                20
        );


        retry.setBackgroundColor(
                Color.rgb(35, 55, 65)
        );


        retry.setOnClickListener(
                view -> refreshMarketData()
        );


        container.addView(
                retry
        );
    }


    // =========================================================
    // BTC DATA
    // =========================================================

    private void fetchBTC()
            throws Exception {

        String urlString =
                "https://api.binance.com/api/v3/klines"
                        + "?symbol=BTCUSDT"
                        + "&interval=1d"
                        + "&limit=730";


        URL url =
                new URL(
                        urlString
                );


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();


        connection.setRequestMethod(
                "GET"
        );


        connection.setConnectTimeout(
                15000
        );


        connection.setReadTimeout(
                15000
        );


        int responseCode =
                connection.getResponseCode();


        if (
                responseCode != 200
        ) {

            throw new Exception(
                    "BTC server error: "
                            +
                    responseCode
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

            response.append(
                    line
            );
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


        for (
                int i = 0;
                i < candles.length();
                i++
        ) {

            JSONArray candle =
                    candles.getJSONArray(
                            i
                    );


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


        if (
                btcPrices.size() < 700
        ) {

            throw new Exception(
                    "Not enough BTC historical data. Received: "
                            +
                    btcPrices.size()
                            +
                    " candles"
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
                new URL(
                        urlString
                );


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();


        connection.setRequestMethod(
                "GET"
        );


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


        if (
                responseCode != 200
        ) {

            throw new Exception(
                    "Gold server error: "
                            +
                    responseCode
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

            response.append(
                    line
            );
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


        if (
                results.length() == 0
        ) {

            throw new Exception(
                    "Gold data unavailable"
            );
        }


        JSONObject result =
                results.getJSONObject(
                        0
                );


        JSONObject indicators =
                result.getJSONObject(
                        "indicators"
                );


        JSONArray quote =
                indicators.getJSONArray(
                        "quote"
                );


        JSONObject quoteData =
                quote.getJSONObject(
                        0
                );


        JSONArray closes =
                quoteData.getJSONArray(
                        "close"
                );


        double latest =
                0.0;


        for (
                int i =
                        closes.length() - 1;
                i >= 0;
                i--
        ) {

            if (
                    !closes.isNull(i)
            ) {

                latest =
                        closes.getDouble(
                                i
                        );

                break;
            }
        }


        if (
                latest <= 0
        ) {

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


        for (
                int i = start;
                i < size;
                i++
        ) {

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


        for (
                int i = start;
                i < size;
                i++
        ) {

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


        if (
                result.ema20 > 0
        ) {

            if (
                    price > result.ema20
            ) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (
                result.ema50 > 0
        ) {

            if (
                    price > result.ema50
            ) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (
                result.ema200 > 0
        ) {

            if (
                    price > result.ema200
            ) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (
                result.momentum > 0
        ) {

            bullish++;

        } else if (
                result.momentum < 0
        ) {

            bearish++;
        }


        if (
                bullish >= 3
        ) {

            return "UP";
        }


        if (
                bearish >= 3
        ) {

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


        if (
                result.rsi > 50
        ) {

            bullish++;

        } else if (
                result.rsi < 50
        ) {

            bearish++;
        }


        if (
                result.macd > 0
        ) {

            bullish++;

        } else {

            bearish++;
        }


        if (
                result.momentum > 0
        ) {

            bullish++;

        } else if (
                result.momentum < 0
        ) {

            bearish++;
        }


        if (
                result.ema50 > 0
        ) {

            if (
                    price > result.ema50
            ) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (
                result.ema200 > 0
        ) {

            if (
                    price > result.ema200
            ) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (
                bullish >= 4
        ) {

            return "BUY BIAS";
        }


        if (
                bearish >= 4
        ) {

            return "SELL BIAS";
        }


        return "NEUTRAL";
    }


    // =========================================================
    // TECHNICAL DIRECTION
    // =========================================================

    private String getTechnicalDirection(
            double buy,
            double sell,
            double neutral
    ) {

        if (
                buy > sell
                        &&
                buy > neutral
        ) {

            return "BUY";
        }


        if (
                sell > buy
                        &&
                sell > neutral
        ) {

            return "SELL";
        }


        return "NEUTRAL";
    }


    // =========================================================
    // AVERAGE VOLUME
    // =========================================================

    private double calculateAverageVolume() {

        if (
                btcVolume.isEmpty()
        ) {

            return 0.0;
        }


        int count =
                Math.min(
                        20,
                        btcVolume.size()
                );


        int start =
                btcVolume.size()
                        -
                count;


        double sum =
                0.0;


        for (
                int i = start;
                i < btcVolume.size();
                i++
        ) {

            sum +=
                    btcVolume.get(i);
        }


        return sum / count;
    }


    // =========================================================
    // TITLE
    // =========================================================

    private void showTitle(
            String text
    ) {

        TextView title =
                new TextView(
                        this
                );


        title.setText(
                text
        );


        title.setTextSize(
                28
        );


        title.setTextColor(
                Color.WHITE
        );


        title.setGravity(
                Gravity.CENTER
        );


        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        title.setPadding(
                0,
                10,
                0,
                10
        );


        container.addView(
                title
        );
    }


    // =========================================================
    // BIG FINAL SIGNAL
    // =========================================================

    private void addBigSignal(
            String signal,
            int color
    ) {

        TextView view =
                new TextView(
                        this
                );


        view.setText(
                signal
        );


        view.setTextSize(
                40
        );


        view.setTextColor(
                color
        );


        view.setGravity(
                Gravity.CENTER
        );


        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        view.setPadding(
                0,
                15,
                0,
                20
        );


        container.addView(
                view
        );
    }


    // =========================================================
    // SECTION
    // =========================================================

    private void addSection(
            String text
    ) {

        TextView section =
                new TextView(
                        this
                );


        section.setText(
                text
        );


        section.setTextSize(
                17
        );


        section.setTextColor(
                Color.WHITE
        );


        section.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        section.setPadding(
                0,
                20,
                0,
                10
        );


        container.addView(
                section
        );
    }


    // =========================================================
    // METRIC
    // =========================================================

    private void addMetric(
            String name,
            String value
    ) {

        TextView metric =
                new TextView(
                        this
                );


        metric.setText(
                name
                        +
                "    "
                        +
                value
        );


        metric.setTextSize(
                16
        );


        metric.setTextColor(
                Color.LTGRAY
        );


        metric.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                metric
        );
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
                new TextView(
                        this
                );


        view.setText(
                text
        );


        view.setTextSize(
                size
        );


        view.setTextColor(
                color
        );


        view.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                view
        );


        return view;
    }


    // =========================================================
    // SPACE
    // =========================================================

    private void addSpace() {

        TextView space =
                new TextView(
                        this
                );


        space.setText(
                ""
        );


        space.setPadding(
                0,
                10,
                0,
                10
        );


        container.addView(
                space
        );
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
