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
import android.widget.Button;
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
    private float downY = 0f;

    private final List<Double> btcOpen = new ArrayList<>();
    private final List<Double> btcHigh = new ArrayList<>();
    private final List<Double> btcLow = new ArrayList<>();
    private final List<Double> btcPrices = new ArrayList<>();
    private final List<Double> btcVolume = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        createUI();

        loadInitialData();
    }


    // =========================================================
    // CREATE UI
    // =========================================================

    private void createUI() {

        scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        container =
                new LinearLayout(this);

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

        scrollView.addView(
                container
        );

        setContentView(
                scrollView
        );


        // =====================================================
        // FIXED PULL TO REFRESH
        // =====================================================

        scrollView.setOnTouchListener(
                (view, event) -> {

                    switch (
                            event.getActionMasked()
                    ) {

                        case MotionEvent.ACTION_DOWN:

                            downY =
                                    event.getY();

                            return false;


                        case MotionEvent.ACTION_UP:

                            float upY =
                                    event.getY();

                            float distance =
                                    upY - downY;


                            if (
                                    scrollView.getScrollY() <= 0
                                            &&
                                    distance >= 120
                                            &&
                                    !isRefreshing
                            ) {

                                startRefresh();
                            }

                            return false;


                        case MotionEvent.ACTION_CANCEL:

                            downY = 0f;

                            return false;
                    }

                    return false;
                }
        );
    }


    // =========================================================
    // INITIAL LOAD
    // =========================================================

    private void loadInitialData() {

        showLoadingScreen(
                "Loading market data..."
        );

        loadMarketData();
    }


    // =========================================================
    // REFRESH
    // =========================================================

    private void startRefresh() {

        if (isRefreshing) {
            return;
        }

        isRefreshing = true;

        showLoadingScreen(
                "Refreshing market data..."
        );

        loadMarketData();
    }


    // =========================================================
    // LOAD MARKET DATA
    // =========================================================

    private void loadMarketData() {

        new Thread(
                () -> {

                    try {

                        fetchBTC();

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
                        // COMBINED
                        // =================================================

                        CombinedEngine.CombinedResult combined =
                                CombinedEngine.analyze(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // ORIGINAL BACKTEST
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
                        // CALIBRATION
                        // =================================================

                        CalibrationEngine.Result calibration =
                                CalibrationEngine.run(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        // =================================================
                        // MARKET
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
                        // DISPLAY ON MAIN THREAD
                        // =================================================

                        new Handler(
                                Looper.getMainLooper()
                        ).post(
                                () -> {

                                    isRefreshing =
                                            false;

                                    showResults(
                                            goldPrice,
                                            currentPrice,
                                            support,
                                            resistance,
                                            trend,
                                            averageVolume,
                                            technical,
                                            probability,
                                            learning,
                                            combined,
                                            backtest,
                                            combinedBacktest,
                                            calibration
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
                                            e.getMessage()
                                                    ==
                                            null
                                                    ?
                                            "Unknown error"
                                                    :
                                            e.getMessage()
                                    );
                                }
                        );
                    }

                }
        ).start();
    }


    // =========================================================
    // RESULTS SCREEN
    // =========================================================

    private void showResults(
            double goldPrice,
            double currentPrice,
            double support,
            double resistance,
            String trend,
            double averageVolume,
            TechnicalAnalyzer.TechnicalResult technical,
            ProbabilityEngine.ProbabilityResult probability,
            LearningEngine.LearningResult learning,
            CombinedEngine.CombinedResult combined,
            BacktestEngine.BacktestResult backtest,
            CombinedBacktestEngine.Result combinedBacktest,
            CalibrationEngine.Result calibration
    ) {

        container.removeAllViews();


        // =====================================================
        // TITLE
        // =====================================================

        showTitle(
                "MARKET AI"
        );

        addText(
                "BTC / GOLD ANALYTICAL ENGINE",
                13,
                Color.GRAY
        );


        // =====================================================
        // REFRESH BUTTON
        // =====================================================

        Button refreshButton =
                new Button(this);

        refreshButton.setText(
                "↻  REFRESH MARKET DATA"
        );

        refreshButton.setTextSize(
                14
        );

        refreshButton.setTextColor(
                Color.WHITE
        );

        refreshButton.setOnClickListener(
                v -> startRefresh()
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


        String finalSignal;


        if (
                "BUY".equals(
                        combined.direction
                )
        ) {

            finalSignal =
                    "BUY";

        } else if (
                "SELL".equals(
                        combined.direction
                )
        ) {

            finalSignal =
                    "SELL";

        } else {

            finalSignal =
                    "WAIT";
        }


        TextView signalView =
                new TextView(this);


        signalView.setText(
                finalSignal
        );

        signalView.setTextSize(
                38
        );

        signalView.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        signalView.setGravity(
                Gravity.CENTER
        );

        signalView.setPadding(
                0,
                25,
                0,
                25
        );


        if (
                "BUY".equals(
                        finalSignal
                )
        ) {

            signalView.setTextColor(
                    Color.rgb(
                            0,
                            220,
                            100
                    )
            );

        } else if (
                "SELL".equals(
                        finalSignal
                )
        ) {

            signalView.setTextColor(
                    Color.rgb(
                            255,
                            80,
                            80
                    )
            );

        } else {

            signalView.setTextColor(
                    Color.LTGRAY
            );
        }


        container.addView(
                signalView
        );


        addMetric(
                "Confidence",
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
                combined.agreement != null
                        &&
                !"MIXED".equals(
                        combined.agreement
                )
        ) {

            addText(
                    "Multiple analytical engines currently agree on this direction.",
                    12,
                    Color.GRAY
            );

        } else {

            addText(
                    "Engines are not sufficiently aligned. WAIT is safer than forcing a direction.",
                    12,
                    Color.GRAY
            );
        }


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
                        combined
                )
        );

        addMetric(
                "Historical Probability",
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

        addMetric(
                "Agreement Count",
                String.valueOf(
                        combined.agreementCount
                ) + "/3"
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
        // HISTORICAL PROBABILITY
        // =====================================================

        addSection(
                "HISTORICAL PROBABILITY"
        );

        addMetric(
                "BUY",
                format(
                        probability.buyProbability
                ) + "%"
        );

        addMetric(
                "SELL",
                format(
                        probability.sellProbability
                ) + "%"
        );

        addMetric(
                "NEUTRAL",
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
                "Samples",
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


        // =====================================================
        // LEARNING DETAILS
        // =====================================================

        addSection(
                "LEARNING DETAILS"
        );

        addMetric(
                "Direction",
                learning.direction
        );

        addMetric(
                "BUY",
                format(
                        learning.buyProbability
                ) + "%"
        );

        addMetric(
                "SELL",
                format(
                        learning.sellProbability
                ) + "%"
        );

        addMetric(
                "NEUTRAL",
                format(
                        learning.neutralProbability
                ) + "%"
        );

        addMetric(
                "Matched Samples",
                String.valueOf(
                        learning.matchedSamples
                )
        );

        addMetric(
                "BUY Samples",
                String.valueOf(
                        learning.buySamples
                )
        );

        addMetric(
                "SELL Samples",
                String.valueOf(
                        learning.sellSamples
                )
        );

        addMetric(
                "NEUTRAL Samples",
                String.valueOf(
                        learning.neutralSamples
                )
        );

        addMetric(
                "Average Future Return",
                format(
                        learning.averageFutureReturn
                ) + "%"
        );

        addMetric(
                "Unseen Test Accuracy",
                format(
                        learning.trainingAccuracy
                ) + "%"
        );

        addMetric(
                "Training Samples",
                String.valueOf(
                        learning.trainingSamples
                )
        );


        addSpace();


        // =====================================================
        // COMBINED BACKTEST
        // =====================================================

        addSection(
                "COMBINED SYSTEM BACKTEST"
        );

        addMetric(
                "Test Signals",
                String.valueOf(
                        combinedBacktest.totalSignals
                )
        );

        addMetric(
                "Correct",
                String.valueOf(
                        combinedBacktest.correctSignals
                )
        );

        addMetric(
                "Wrong",
                String.valueOf(
                        combinedBacktest.wrongSignals
                )
        );

        addMetric(
                "Neutral",
                String.valueOf(
                        combinedBacktest.neutralSignals
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
                "Strong Signals",
                String.valueOf(
                        combinedBacktest.strongSignals
                )
        );

        addMetric(
                "Moderate Signals",
                String.valueOf(
                        combinedBacktest.moderateSignals
                )
        );

        addMetric(
                "Weak Signals",
                String.valueOf(
                        combinedBacktest.weakSignals
                )
        );

        addMetric(
                "BTC Buy & Hold",
                format(
                        combinedBacktest.buyHoldReturn
                ) + "%"
        );


        addSpace();


        // =====================================================
        // CALIBRATION
        // =====================================================

        addSection(
                "CONFIDENCE CALIBRATION"
        );

        addText(
                "Historical accuracy of signals grouped by model confidence.",
                12,
                Color.GRAY
        );

        addCalibrationBucket(
                calibration.bucket0_40
        );

        addCalibrationBucket(
                calibration.bucket40_50
        );

        addCalibrationBucket(
                calibration.bucket50_60
        );

        addCalibrationBucket(
                calibration.bucket60_70
        );

        addCalibrationBucket(
                calibration.bucket70_80
        );

        addCalibrationBucket(
                calibration.bucket80_100
        );

        addMetric(
                "Calibration Accuracy",
                format(
                        calibration.overallAccuracy
                ) + "%"
        );

        addMetric(
                "Calibration Avg Return",
                format(
                        calibration.averageReturn
                ) + "%"
        );


        addSpace();


        // =====================================================
        // V3 BACKTEST
        // =====================================================

        addSection(
                "V3 BACKTEST"
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

        addMetric(
                "Profit Factor",
                format(
                        backtest.profitFactor
                )
        );

        addMetric(
                "Maximum Drawdown",
                format(
                        backtest.maxDrawdown
                ) + "%"
        );


        addSpace();


        // =====================================================
        // RISK
        // =====================================================

        addSection(
                "RISK ANALYSIS"
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
        // FOOTER
        // =====================================================

        addText(
                "Historical data: approximately 2 years of BTC daily candles.",
                12,
                Color.GRAY
        );

        addText(
                "FINAL SIGNAL is a model-based analytical result, not a guaranteed outcome.",
                12,
                Color.GRAY
        );

        addText(
                "Probabilities and backtests are historical research metrics.",
                12,
                Color.GRAY
        );
    }


    // =========================================================
    // CALIBRATION BUCKET
    // =========================================================

    private void addCalibrationBucket(
            CalibrationEngine.Bucket bucket
    ) {

        if (
                bucket == null
        ) {
            return;
        }

        addMetric(
                bucket.range,
                "Accuracy "
                        +
                format(
                        bucket.accuracy
                )
                        +
                "% | "
                        +
                bucket.signals
                        +
                " signals"
        );
    }


    // =========================================================
    // TECHNICAL DIRECTION
    // =========================================================

    private String getTechnicalDirection(
            CombinedEngine.CombinedResult combined
    ) {

        if (
                combined.technicalBuy >
                        combined.technicalSell
                        &&
                combined.technicalBuy >
                        combined.technicalNeutral
        ) {

            return "BUY";
        }

        if (
                combined.technicalSell >
                        combined.technicalBuy
                        &&
                combined.technicalSell >
                        combined.technicalNeutral
        ) {

            return "SELL";
        }

        return "NEUTRAL";
    }


    // =========================================================
    // LOADING SCREEN
    // =========================================================

    private void showLoadingScreen(
            String message
    ) {

        container.removeAllViews();

        showTitle(
                "MARKET AI"
        );

        addText(
                message,
                17,
                Color.LTGRAY
        );

        addSpace();

        addText(
                "Fetching fresh BTC and Gold market data...",
                13,
                Color.GRAY
        );
    }


    // =========================================================
    // ERROR SCREEN
    // =========================================================

    private void showError(
            String message
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

        addText(
                message,
                14,
                Color.LTGRAY
        );

        addSpace();

        Button retry =
                new Button(this);

        retry.setText(
                "↻  TRY AGAIN"
        );

        retry.setOnClickListener(
                v -> startRefresh()
        );

        container.addView(
                retry
        );

        addSpace();

        addText(
                "You can also pull down from the top to refresh.",
                13,
                Color.GRAY
        );
    }


    // =========================================================
    // BTC DATA
    // =========================================================

    private void fetchBTC()
            throws Exception {

        String urlString =
                "https://api.binance.com/api/v3/klines"
                        +
                "?symbol=BTCUSDT"
                        +
                "&interval=1d"
                        +
                "&limit=730";


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


            btcOpen.add(
                    Double.parseDouble(
                            candle.getString(1)
                    )
            );

            btcHigh.add(
                    Double.parseDouble(
                            candle.getString(2)
                    )
            );

            btcLow.add(
                    Double.parseDouble(
                            candle.getString(3)
                    )
            );

            btcPrices.add(
                    Double.parseDouble(
                            candle.getString(4)
                    )
            );

            btcVolume.add(
                    Double.parseDouble(
                            candle.getString(5)
                    )
            );
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
                        +
                "?range=6mo"
                        +
                "&interval=1d";


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
                latest <= 0.0
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
                new TextView(this);

        title.setText(
                text
        );

        title.setTextSize(
                28
        );

        title.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        title.setTextColor(
                Color.WHITE
        );

        title.setGravity(
                Gravity.CENTER
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
    // SECTION
    // =========================================================

    private void addSection(
            String text
    ) {

        TextView section =
                new TextView(this);

        section.setText(
                text
        );

        section.setTextSize(
                17
        );

        section.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        section.setTextColor(
                Color.WHITE
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
                new TextView(this);

        metric.setText(
                name
                        +
                "    "
                        +
                value
        );

        metric.setTextSize(
                15
        );

        metric.setTextColor(
                Color.LTGRAY
        );

        metric.setPadding(
                0,
                7,
                0,
                7
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
                new TextView(this);

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
                7,
                0,
                7
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
                new TextView(this);

        space.setText(
                ""
        );

        space.setPadding(
                0,
                8,
                0,
                8
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
