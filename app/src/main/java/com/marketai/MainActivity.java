package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
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

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // =========================================================
    // LIVE BTC PRICE
    // =========================================================

    private LiveMarketEngine liveMarketEngine;

    private TextView liveBTCPriceView;
    private TextView liveBTCStatusView;

    private double latestLiveBTC = 0.0;


    // =========================================================
    // LIVE CANDLE ENGINE
    // =========================================================

    private LiveCandleEngine liveCandleEngine;

    private TextView live5mStatusView;
    private TextView live15mStatusView;

    private TextView live5mAnalysisView;
    private TextView live15mAnalysisView;

    private boolean live5mConnected = false;
    private boolean live15mConnected = false;

    private long last5mCandleTime = -1;
    private long last15mCandleTime = -1;


    // =========================================================
    // DAILY BTC DATA
    // =========================================================

    private final List<Double> btcOpen = new ArrayList<>();
    private final List<Double> btcHigh = new ArrayList<>();
    private final List<Double> btcLow = new ArrayList<>();
    private final List<Double> btcPrices = new ArrayList<>();
    private final List<Double> btcVolume = new ArrayList<>();


    // =========================================================
    // 15M BTC DATA
    // =========================================================

    private final List<Double> btc15Open = new ArrayList<>();
    private final List<Double> btc15High = new ArrayList<>();
    private final List<Double> btc15Low = new ArrayList<>();
    private final List<Double> btc15Prices = new ArrayList<>();
    private final List<Double> btc15Volume = new ArrayList<>();


    // =========================================================
    // 5M BTC DATA
    // =========================================================

    private final List<Double> btc5Open = new ArrayList<>();
    private final List<Double> btc5High = new ArrayList<>();
    private final List<Double> btc5Low = new ArrayList<>();
    private final List<Double> btc5Prices = new ArrayList<>();
    private final List<Double> btc5Volume = new ArrayList<>();


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
    // ON START
    // =========================================================

    @Override
    protected void onStart() {

        super.onStart();

        startLiveMarket();

        startLiveCandles();
    }


    // =========================================================
    // LIVE PRICE
    // =========================================================

    private void startLiveMarket() {

        if (liveMarketEngine == null) {

            liveMarketEngine =
                    new LiveMarketEngine(
                            new LiveMarketEngine.LiveMarketListener() {

                                @Override
                                public void onPriceUpdate(
                                        String symbol,
                                        double price
                                ) {

                                    latestLiveBTC =
                                            price;

                                    mainHandler.post(
                                            () -> {

                                                if (
                                                        liveBTCPriceView
                                                                != null
                                                ) {

                                                    liveBTCPriceView.setText(
                                                            "$"
                                                                    +
                                                            format(
                                                                    price
                                                            )
                                                    );
                                                }

                                                if (
                                                        liveBTCStatusView
                                                                != null
                                                ) {

                                                    liveBTCStatusView.setText(
                                                            "● LIVE  •  "
                                                                    +
                                                            symbol
                                                    );

                                                    liveBTCStatusView.setTextColor(
                                                            Color.GREEN
                                                    );
                                                }
                                            }
                                    );
                                }


                                @Override
                                public void onConnectionChanged(
                                        boolean connected
                                ) {

                                    mainHandler.post(
                                            () -> {

                                                if (
                                                        liveBTCStatusView
                                                                != null
                                                ) {

                                                    if (connected) {

                                                        liveBTCStatusView.setText(
                                                                "● LIVE  •  BTCUSDT"
                                                        );

                                                        liveBTCStatusView.setTextColor(
                                                                Color.GREEN
                                                        );

                                                    } else {

                                                        liveBTCStatusView.setText(
                                                                "○ RECONNECTING..."
                                                        );

                                                        liveBTCStatusView.setTextColor(
                                                                Color.YELLOW
                                                        );
                                                    }
                                                }
                                            }
                                    );
                                }


                                @Override
                                public void onError(
                                        String message
                                ) {

                                    mainHandler.post(
                                            () -> {

                                                if (
                                                        liveBTCStatusView
                                                                != null
                                                ) {

                                                    liveBTCStatusView.setText(
                                                            "○ LIVE CONNECTION ERROR"
                                                    );

                                                    liveBTCStatusView.setTextColor(
                                                            Color.RED
                                                    );
                                                }
                                            }
                                    );
                                }
                            }
                    );
        }

        liveMarketEngine.startBTC();
    }


    // =========================================================
    // LIVE CANDLES
    // =========================================================

    private void startLiveCandles() {

        if (liveCandleEngine == null) {

            liveCandleEngine =
                    new LiveCandleEngine(
                            new LiveCandleEngine.Listener() {

                                @Override
                                public void onCandleUpdate(
                                        String interval,
                                        LiveCandleEngine.Candle candle,
                                        List<LiveCandleEngine.Candle> candles
                                ) {

                                    handleLiveCandle(
                                            interval,
                                            candle
                                    );
                                }


                                @Override
                                public void onConnectionChanged(
                                        String interval,
                                        boolean connected
                                ) {

                                    if (
                                            "5m".equals(interval)
                                    ) {

                                        live5mConnected =
                                                connected;

                                    } else if (
                                            "15m".equals(interval)
                                    ) {

                                        live15mConnected =
                                                connected;
                                    }

                                    mainHandler.post(
                                            () -> updateLiveConnectionUI()
                                    );
                                }


                                @Override
                                public void onError(
                                        String interval,
                                        String message
                                ) {

                                    mainHandler.post(
                                            () -> {

                                                if (
                                                        "5m".equals(
                                                                interval
                                                        )
                                                ) {

                                                    if (
                                                            live5mStatusView
                                                                    != null
                                                    ) {

                                                        live5mStatusView.setText(
                                                                "○ 5M ERROR"
                                                        );

                                                        live5mStatusView.setTextColor(
                                                                Color.RED
                                                        );
                                                    }

                                                } else {

                                                    if (
                                                            live15mStatusView
                                                                    != null
                                                    ) {

                                                        live15mStatusView.setText(
                                                                "○ 15M ERROR"
                                                        );

                                                        live15mStatusView.setTextColor(
                                                                Color.RED
                                                        );
                                                    }
                                                }
                                            }
                                    );
                                }
                            }
                    );
        }

        liveCandleEngine.start();
    }


    // =========================================================
    // HANDLE LIVE CANDLE
    // =========================================================

    private void handleLiveCandle(
            String interval,
            LiveCandleEngine.Candle candle
    ) {

        if (
                candle == null
        ) {
            return;
        }


        if (
                "5m".equals(interval)
        ) {

            updateLocalCandle(
                    candle,
                    btc5Open,
                    btc5High,
                    btc5Low,
                    btc5Prices,
                    btc5Volume
            );


            boolean newCandle =
                    last5mCandleTime != candle.openTime;

            last5mCandleTime =
                    candle.openTime;


            if (
                    newCandle ||
                    candle.closed
            ) {

                calculateLive5mAnalysis();
            }


        } else if (
                "15m".equals(interval)
        ) {

            updateLocalCandle(
                    candle,
                    btc15Open,
                    btc15High,
                    btc15Low,
                    btc15Prices,
                    btc15Volume
            );


            boolean newCandle =
                    last15mCandleTime != candle.openTime;

            last15mCandleTime =
                    candle.openTime;


            if (
                    newCandle ||
                    candle.closed
            ) {

                calculateLive15mAnalysis();
            }
        }
    }


    // =========================================================
    // UPDATE LOCAL CANDLE
    // =========================================================

    private synchronized void updateLocalCandle(

            LiveCandleEngine.Candle candle,

            List<Double> openList,
            List<Double> highList,
            List<Double> lowList,
            List<Double> closeList,
            List<Double> volumeList

    ) {

        if (
                openList.isEmpty()
                        ||
                closeList.isEmpty()
        ) {

            openList.add(candle.open);
            highList.add(candle.high);
            lowList.add(candle.low);
            closeList.add(candle.close);
            volumeList.add(candle.volume);

            return;
        }


        int last =
                closeList.size() - 1;


        /*
         * WebSocket candle belongs to the current
         * candle. If its open time matches the last
         * locally stored candle, replace it.
         *
         * Otherwise append it.
         */

        double previousClose =
                closeList.get(last);


        if (
                Math.abs(
                        previousClose -
                                candle.close
                ) < 0
                        &&
                openList.get(last) == candle.open
        ) {

            openList.set(
                    last,
                    candle.open
            );

            highList.set(
                    last,
                    candle.high
            );

            lowList.set(
                    last,
                    candle.low
            );

            closeList.set(
                    last,
                    candle.close
            );

            volumeList.set(
                    last,
                    candle.volume
            );

        } else {

            openList.add(candle.open);

            highList.add(candle.high);

            lowList.add(candle.low);

            closeList.add(candle.close);

            volumeList.add(candle.volume);
        }


        while (
                closeList.size() > 1200
        ) {

            openList.remove(0);
            highList.remove(0);
            lowList.remove(0);
            closeList.remove(0);
            volumeList.remove(0);
        }
    }


    // =========================================================
    // LIVE 5M ANALYSIS
    // =========================================================

    private void calculateLive5mAnalysis() {

        if (
                btc5Prices.size() < 40
        ) {
            return;
        }


        new Thread(
                () -> {

                    try {

                        TechnicalAnalyzer.TechnicalResult technical =
                                TechnicalAnalyzer.analyze(
                                        btc5Prices,
                                        btc5High,
                                        btc5Low,
                                        btc5Volume
                                );


                        MoveDetector.MoveResult move =
                                MoveDetector.analyze(
                                        btc5Prices,
                                        btc5High,
                                        btc5Low,
                                        btc5Volume,
                                        5
                                );


                        double price =
                                btc5Prices.get(
                                        btc5Prices.size() - 1
                                );


                        String trend =
                                getTrend(
                                        price,
                                        technical
                                );


                        mainHandler.post(
                                () -> {

                                    if (
                                            live5mAnalysisView
                                                    == null
                                    ) {
                                        return;
                                    }


                                    String text =
                                            "PRICE  $"
                                                    +
                                            format(
                                                    price
                                            )
                                                    +
                                            "\n\n"
                                                    +
                                            "TREND  "
                                                    +
                                            trend
                                                    +
                                            "\n"
                                                    +
                                            "RSI  "
                                                    +
                                            format(
                                                    technical.rsi
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "EMA 20  $"
                                                    +
                                            format(
                                                    technical.ema20
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "EMA 50  $"
                                                    +
                                            format(
                                                    technical.ema50
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "MACD  "
                                                    +
                                            format(
                                                    technical.macd
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "ATR  $"
                                                    +
                                            format(
                                                    technical.atr
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "VOLUME  "
                                                    +
                                            format(
                                                    technical.volumeRatio
                                            )
                                                    +
                                            "x"
                                                    +
                                            "\n\n"
                                                    +
                                            "MOVE RISK  "
                                                    +
                                            format(
                                                    move.moveRisk
                                            )
                                                    +
                                            "%"
                                                    +
                                            "\n"
                                                    +
                                            "RISK LEVEL  "
                                                    +
                                            move.riskLevel
                                                    +
                                            "\n"
                                                    +
                                            "DIRECTION  "
                                                    +
                                            move.direction
                                                    +
                                            "\n"
                                                    +
                                            "UPSIDE  "
                                                    +
                                            format(
                                                    move.upsideProbability
                                            )
                                                    +
                                            "%"
                                                    +
                                            "\n"
                                                    +
                                            "DOWNSIDE  "
                                                    +
                                            format(
                                                    move.downsideProbability
                                            )
                                                    +
                                            "%";


                                    live5mAnalysisView.setText(
                                            text
                                    );


                                    if (
                                            move.direction.equals(
                                                    "UP"
                                            )
                                    ) {

                                        live5mAnalysisView.setTextColor(
                                                Color.GREEN
                                        );

                                    } else if (
                                            move.direction.equals(
                                                    "DOWN"
                                            )
                                    ) {

                                        live5mAnalysisView.setTextColor(
                                                Color.RED
                                        );

                                    } else {

                                        live5mAnalysisView.setTextColor(
                                                Color.LTGRAY
                                        );
                                    }
                                }
                        );

                    } catch (
                            Exception ignored
                    ) {
                    }
                }
        ).start();
    }


    // =========================================================
    // LIVE 15M ANALYSIS
    // =========================================================

    private void calculateLive15mAnalysis() {

        if (
                btc15Prices.size() < 40
        ) {
            return;
        }


        new Thread(
                () -> {

                    try {

                        TechnicalAnalyzer.TechnicalResult technical =
                                TechnicalAnalyzer.analyze(
                                        btc15Prices,
                                        btc15High,
                                        btc15Low,
                                        btc15Volume
                                );


                        MoveDetector.MoveResult move =
                                MoveDetector.analyze(
                                        btc15Prices,
                                        btc15High,
                                        btc15Low,
                                        btc15Volume,
                                        15
                                );


                        double price =
                                btc15Prices.get(
                                        btc15Prices.size() - 1
                                );


                        String trend =
                                getTrend(
                                        price,
                                        technical
                                );


                        mainHandler.post(
                                () -> {

                                    if (
                                            live15mAnalysisView
                                                    == null
                                    ) {
                                        return;
                                    }


                                    String text =
                                            "PRICE  $"
                                                    +
                                            format(
                                                    price
                                            )
                                                    +
                                            "\n\n"
                                                    +
                                            "TREND  "
                                                    +
                                            trend
                                                    +
                                            "\n"
                                                    +
                                            "RSI  "
                                                    +
                                            format(
                                                    technical.rsi
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "EMA 20  $"
                                                    +
                                            format(
                                                    technical.ema20
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "EMA 50  $"
                                                    +
                                            format(
                                                    technical.ema50
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "MACD  "
                                                    +
                                            format(
                                                    technical.macd
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "ATR  $"
                                                    +
                                            format(
                                                    technical.atr
                                            )
                                                    +
                                            "\n"
                                                    +
                                            "VOLUME  "
                                                    +
                                            format(
                                                    technical.volumeRatio
                                            )
                                                    +
                                            "x"
                                                    +
                                            "\n\n"
                                                    +
                                            "MOVE RISK  "
                                                    +
                                            format(
                                                    move.moveRisk
                                            )
                                                    +
                                            "%"
                                                    +
                                            "\n"
                                                    +
                                            "RISK LEVEL  "
                                                    +
                                            move.riskLevel
                                                    +
                                            "\n"
                                                    +
                                            "DIRECTION  "
                                                    +
                                            move.direction
                                                    +
                                            "\n"
                                                    +
                                            "UPSIDE  "
                                                    +
                                            format(
                                                    move.upsideProbability
                                            )
                                                    +
                                            "%"
                                                    +
                                            "\n"
                                                    +
                                            "DOWNSIDE  "
                                                    +
                                            format(
                                                    move.downsideProbability
                                            )
                                                    +
                                            "%";


                                    live15mAnalysisView.setText(
                                            text
                                    );


                                    if (
                                            move.direction.equals(
                                                    "UP"
                                            )
                                    ) {

                                        live15mAnalysisView.setTextColor(
                                                Color.GREEN
                                        );

                                    } else if (
                                            move.direction.equals(
                                                    "DOWN"
                                            )
                                    ) {

                                        live15mAnalysisView.setTextColor(
                                                Color.RED
                                        );

                                    } else {

                                        live15mAnalysisView.setTextColor(
                                                Color.LTGRAY
                                        );
                                    }
                                }
                        );

                    } catch (
                            Exception ignored
                    ) {
                    }
                }
        ).start();
    }


    // =========================================================
    // LIVE CONNECTION UI
    // =========================================================

    private void updateLiveConnectionUI() {

        if (
                live5mStatusView != null
        ) {

            if (live5mConnected) {

                live5mStatusView.setText(
                        "● LIVE  •  5M CANDLES"
                );

                live5mStatusView.setTextColor(
                        Color.GREEN
                );

            } else {

                live5mStatusView.setText(
                        "○ RECONNECTING  •  5M"
                );

                live5mStatusView.setTextColor(
                        Color.YELLOW
                );
            }
        }


        if (
                live15mStatusView != null
        ) {

            if (live15mConnected) {

                live15mStatusView.setText(
                        "● LIVE  •  15M CANDLES"
                );

                live15mStatusView.setTextColor(
                        Color.GREEN
                );

            } else {

                live15mStatusView.setText(
                        "○ RECONNECTING  •  15M"
                );

                live15mStatusView.setTextColor(
                        Color.YELLOW
                );
            }
        }
    }


    // =========================================================
    // ON STOP
    // =========================================================

    @Override
    protected void onStop() {

        if (
                liveMarketEngine != null
        ) {

            liveMarketEngine.stop();
        }


        if (
                liveCandleEngine != null
        ) {

            liveCandleEngine.stop();
        }


        super.onStop();
    }


    // =========================================================
    // REFRESH
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
                "Refreshing BTC 1D / 15M / 5M market data...",
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

                        fetchBTC();


                        fetchIntradayBTC(
                                "15m",
                                1000,
                                btc15Open,
                                btc15High,
                                btc15Low,
                                btc15Prices,
                                btc15Volume
                        );


                        fetchIntradayBTC(
                                "5m",
                                1000,
                                btc5Open,
                                btc5High,
                                btc5Low,
                                btc5Prices,
                                btc5Volume
                        );


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


                        LearningEngine.LearningResult learning =
                                LearningEngine.learn(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        CombinedEngine.CombinedResult combined =
                                CombinedEngine.analyze(
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


                        CombinedBacktestEngine.Result combinedBacktest =
                                CombinedBacktestEngine.run(
                                        btcPrices,
                                        btcHigh,
                                        btcLow,
                                        btcVolume
                                );


                        MoveDetector.MoveResult move15 =
                                MoveDetector.analyze(
                                        btc15Prices,
                                        btc15High,
                                        btc15Low,
                                        btc15Volume,
                                        15
                                );


                        MoveDetector.MoveResult move5 =
                                MoveDetector.analyze(
                                        btc5Prices,
                                        btc5High,
                                        btc5Low,
                                        btc5Volume,
                                        5
                                );


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


                        mainHandler.post(
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
                                            combinedBacktest,
                                            move15,
                                            move5
                                    );
                                }
                        );

                    } catch (
                            Exception e
                    ) {

                        mainHandler.post(
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

            CombinedBacktestEngine.Result combinedBacktest,

            MoveDetector.MoveResult move15,

            MoveDetector.MoveResult move5

    ) {

        container.removeAllViews();


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
        // BTC LIVE PRICE
        // =====================================================

        addSection(
                "BTC LIVE MARKET"
        );


        liveBTCPriceView =
                new TextView(this);


        liveBTCPriceView.setTextSize(
                30
        );


        liveBTCPriceView.setTextColor(
                Color.GREEN
        );


        liveBTCPriceView.setGravity(
                Gravity.CENTER
        );


        liveBTCPriceView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        if (
                latestLiveBTC > 0
        ) {

            liveBTCPriceView.setText(
                    "$"
                            +
                    format(
                            latestLiveBTC
                    )
            );

        } else {

            liveBTCPriceView.setText(
                    "$"
                            +
                    format(
                            currentPrice
                    )
            );
        }


        liveBTCPriceView.setPadding(
                0,
                10,
                0,
                5
        );


        container.addView(
                liveBTCPriceView
        );


        liveBTCStatusView =
                new TextView(this);


        liveBTCStatusView.setText(
                "○ CONNECTING..."
        );


        liveBTCStatusView.setTextSize(
                13
        );


        liveBTCStatusView.setTextColor(
                Color.YELLOW
        );


        liveBTCStatusView.setGravity(
                Gravity.CENTER
        );


        liveBTCStatusView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        container.addView(
                liveBTCStatusView
        );


        addSpace();


        // =====================================================
        // LIVE CANDLE STATUS
        // =====================================================

        addSection(
                "LIVE CANDLE ENGINE"
        );


        live5mStatusView =
                new TextView(this);


        live5mStatusView.setTextSize(
                14
        );


        live5mStatusView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        live5mStatusView.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                live5mStatusView
        );


        live15mStatusView =
                new TextView(this);


        live15mStatusView.setTextSize(
                14
        );


        live15mStatusView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        live15mStatusView.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                live15mStatusView
        );


        updateLiveConnectionUI();


        addSpace();


        // =====================================================
        // LIVE 5M ANALYSIS
        // =====================================================

        addSection(
                "LIVE 5M ANALYSIS"
        );


        live5mAnalysisView =
                new TextView(this);


        live5mAnalysisView.setText(
                "Waiting for live 5M candle data..."
        );


        live5mAnalysisView.setTextSize(
                15
        );


        live5mAnalysisView.setTextColor(
                Color.LTGRAY
        );


        live5mAnalysisView.setTypeface(
                Typeface.MONOSPACE,
                Typeface.NORMAL
        );


        live5mAnalysisView.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                live5mAnalysisView
        );


        addSpace();


        // =====================================================
        // LIVE 15M ANALYSIS
        // =====================================================

        addSection(
                "LIVE 15M ANALYSIS"
        );


        live15mAnalysisView =
                new TextView(this);


        live15mAnalysisView.setText(
                "Waiting for live 15M candle data..."
        );


        live15mAnalysisView.setTextSize(
                15
        );


        live15mAnalysisView.setTextColor(
                Color.LTGRAY
        );


        live15mAnalysisView.setTypeface(
                Typeface.MONOSPACE,
                Typeface.NORMAL
        );


        live15mAnalysisView.setPadding(
                0,
                8,
                0,
                8
        );


        container.addView(
                live15mAnalysisView
        );


        addSpace();


        // =====================================================
        // REFRESH
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
        // LARGE MOVE
        // =====================================================

        addSection(
                "LARGE MOVE DETECTOR"
        );


        addText(
                "Detects abnormal volatility / volume / compression conditions.",
                12,
                Color.GRAY
        );


        addSpace();


        // =====================================================
        // 15M MOVE
        // =====================================================

        addSection(
                "15 MINUTE MOVE DETECTOR"
        );


        addMetric(
                "Move Risk",
                format(move15.moveRisk) + "%"
        );


        addMetric(
                "Risk Level",
                move15.riskLevel
        );


        addMetric(
                "Expected Direction",
                move15.direction
        );


        addMetric(
                "Upside Probability",
                format(move15.upsideProbability) + "%"
        );


        addMetric(
                "Downside Probability",
                format(move15.downsideProbability) + "%"
        );


        addMetric(
                "Volatility Score",
                format(move15.volatilityScore)
        );


        addMetric(
                "Volume Score",
                format(move15.volumeScore)
        );


        addMetric(
                "Compression Score",
                format(move15.compressionScore)
        );


        addMetric(
                "Momentum Score",
                format(move15.momentumScore)
        );


        addMetric(
                "Volatility Expansion",
                yesNo(move15.volatilityExpansion)
        );


        addMetric(
                "Volume Expansion",
                yesNo(move15.volumeExpansion)
        );


        addMetric(
                "Volatility Compression",
                yesNo(move15.volatilityCompression)
        );


        addText(
                "Evidence: " + move15.evidence,
                12,
                Color.GRAY
        );


        addSpace();


        // =====================================================
        // 5M MOVE
        // =====================================================

        addSection(
                "5 MINUTE MOVE DETECTOR"
        );


        addMetric(
                "Move Risk",
                format(move5.moveRisk) + "%"
        );


        addMetric(
                "Risk Level",
                move5.riskLevel
        );


        addMetric(
                "Expected Direction",
                move5.direction
        );


        addMetric(
                "Upside Probability",
                format(move5.upsideProbability) + "%"
        );


        addMetric(
                "Downside Probability",
                format(move5.downsideProbability) + "%"
        );


        addMetric(
                "Volatility Score",
                format(move5.volatilityScore)
        );


        addMetric(
                "Volume Score",
                format(move5.volumeScore)
        );


        addMetric(
                "Compression Score",
                format(move5.compressionScore)
        );


        addMetric(
                "Momentum Score",
                format(move5.momentumScore)
        );


        addMetric(
                "Volatility Expansion",
                yesNo(move5.volatilityExpansion)
        );


        addMetric(
                "Volume Expansion",
                yesNo(move5.volumeExpansion)
        );


        addMetric(
                "Volatility Compression",
                yesNo(move5.volatilityCompression)
        );


        addText(
                "Evidence: " + move5.evidence,
                12,
                Color.GRAY
        );


        addSpace();


        // =====================================================
        // MULTI TIMEFRAME
        // =====================================================

        addSection(
                "MULTI-TIMEFRAME MOVE VIEW"
        );


        String moveAgreement =
                getMoveAgreement(
                        move5,
                        move15
                );


        addBigSignal(
                moveAgreement,
                getMoveAgreementColor(
                        moveAgreement
                )
        );


        addMetric(
                "5M Direction",
                move5.direction
        );


        addMetric(
                "15M Direction",
                move15.direction
        );


        addMetric(
                "5M Risk",
                format(move5.moveRisk) + "%"
        );


        addMetric(
                "15M Risk",
                format(move15.moveRisk) + "%"
        );


        addText(
                getMoveExplanation(
                        move5,
                        move15
                ),
                13,
                Color.GRAY
        );


        addSpace();


        // =====================================================
        // TRADE REFERENCE
        // =====================================================

        addSection(
                "TRADE REFERENCE"
        );


        addMetric(
                "Reference Price",
                "$" + format(currentPrice)
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
        // TECHNICAL
        // =====================================================

        addSection(
                "TECHNICAL DETAILS"
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
                "$" + format(technical.bollingerUpper)
        );


        addMetric(
                "Bollinger Lower",
                "$" + format(technical.bollingerLower)
        );


        addMetric(
                "Momentum",
                format(technical.momentum) + "%"
        );


        addMetric(
                "Volume Ratio",
                format(technical.volumeRatio) + "x"
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
                format(probability.buyProbability) + "%"
        );


        addMetric(
                "Probability SELL",
                format(probability.sellProbability) + "%"
        );


        addMetric(
                "Probability WAIT",
                format(probability.neutralProbability) + "%"
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
        // BACKTEST V3
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
        // RISK
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
        // DATA
        // =====================================================

        addText(
                "BTC Daily Data: ~2 Years",
                12,
                Color.GRAY
        );


        addText(
                "BTC 15M Data: 1000 candles + live candle stream",
                12,
                Color.GRAY
        );


        addText(
                "BTC 5M Data: 1000 candles + live candle stream",
                12,
                Color.GRAY
        );


        addText(
                "BTC live price and candles are received through WebSocket streams.",
                12,
                Color.GRAY
        );


        addText(
                "Live 5M / 15M indicators update as the current candle changes.",
                12,
                Color.GRAY
        );


        addText(
                "Move probabilities are analytical estimates, not guarantees.",
                12,
                Color.GRAY
        );


        addText(
                "Historical backtests do not guarantee future results.",
                12,
                Color.GRAY
        );
    }


    // =========================================================
    // MOVE AGREEMENT
    // =========================================================

    private String getMoveAgreement(
            MoveDetector.MoveResult move5,
            MoveDetector.MoveResult move15
    ) {

        if (
                move5 == null ||
                move15 == null
        ) {

            return "UNCERTAIN";
        }


        boolean highRisk =
                move5.moveRisk >= 55.0
                        ||
                move15.moveRisk >= 55.0;


        if (
                move5.direction.equals("UP")
                        &&
                move15.direction.equals("UP")
                        &&
                highRisk
        ) {

            return "LARGE MOVE → UP";
        }


        if (
                move5.direction.equals("DOWN")
                        &&
                move15.direction.equals("DOWN")
                        &&
                highRisk
        ) {

            return "LARGE MOVE → DOWN";
        }


        if (
                move5.direction.equals("UP")
                        &&
                move15.direction.equals("UP")
        ) {

            return "UP BIAS";
        }


        if (
                move5.direction.equals("DOWN")
                        &&
                move15.direction.equals("DOWN")
        ) {

            return "DOWN BIAS";
        }


        return "UNCERTAIN";
    }


    // =========================================================
    // MOVE COLOR
    // =========================================================

    private int getMoveAgreementColor(
            String value
    ) {

        if (
                value.contains("UP")
        ) {

            return Color.GREEN;
        }


        if (
                value.contains("DOWN")
        ) {

            return Color.RED;
        }


        return Color.YELLOW;
    }


    // =========================================================
    // MOVE EXPLANATION
    // =========================================================

    private String getMoveExplanation(
            MoveDetector.MoveResult move5,
            MoveDetector.MoveResult move15
    ) {

        if (
                move5 == null ||
                move15 == null
        ) {

            return "Move detector data unavailable.";
        }


        if (
                move5.direction.equals("UP")
                        &&
                move15.direction.equals("UP")
                        &&
                (
                                move5.moveRisk >= 55.0
                                        ||
                                move15.moveRisk >= 55.0
                        )
        ) {

            return "Both short-term timeframes point UP while move-risk is elevated. This is an early-warning condition, not a guarantee.";
        }


        if (
                move5.direction.equals("DOWN")
                        &&
                move15.direction.equals("DOWN")
                        &&
                (
                                move5.moveRisk >= 55.0
                                        ||
                                move15.moveRisk >= 55.0
                        )
        ) {

            return "Both short-term timeframes point DOWN while move-risk is elevated. This is an early-warning condition, not a guarantee.";
        }


        if (
                move5.direction.equals("UNCERTAIN")
                        ||
                move15.direction.equals("UNCERTAIN")
        ) {

            return "Short-term direction is not sufficiently aligned yet.";
        }


        return "5M and 15M signals are not strongly aligned.";
    }


    // =========================================================
    // YES NO
    // =========================================================

    private String yesNo(
            boolean value
    ) {

        return value
                ? "YES"
                : "NO";
    }


    // =========================================================
    // ERROR
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
    // FETCH DAILY BTC
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


        fetchCandleData(
                urlString,
                btcOpen,
                btcHigh,
                btcLow,
                btcPrices,
                btcVolume
        );


        if (
                btcPrices.size() < 700
        ) {

            throw new Exception(
                    "Not enough BTC daily data. Received: "
                            +
                    btcPrices.size()
                            +
                    " candles"
            );
        }
    }


    // =========================================================
    // FETCH INTRADAY BTC
    // =========================================================

    private void fetchIntradayBTC(
            String interval,
            int limit,

            List<Double> openList,
            List<Double> highList,
            List<Double> lowList,
            List<Double> closeList,
            List<Double> volumeList

    ) throws Exception {

        String urlString =
                "https://api.binance.com/api/v3/klines"
                        +
                "?symbol=BTCUSDT"
                        +
                "&interval="
                        +
                interval
                        +
                "&limit="
                        +
                limit;


        fetchCandleData(
                urlString,
                openList,
                highList,
                lowList,
                closeList,
                volumeList
        );


        if (
                closeList.size() < 1000
        ) {

            throw new Exception(
                    "Not enough BTC "
                            +
                    interval
                            +
                    " data. Received: "
                            +
                    closeList.size()
                            +
                    " candles"
            );
        }
    }


    // =========================================================
    // GENERIC CANDLE FETCH
    // =========================================================

    private void fetchCandleData(

            String urlString,

            List<Double> openList,
            List<Double> highList,
            List<Double> lowList,
            List<Double> closeList,
            List<Double> volumeList

    ) throws Exception {

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
                "MarketAI/1.0"
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


        openList.clear();
        highList.clear();
        lowList.clear();
        closeList.clear();
        volumeList.clear();


        for (
                int i = 0;
                i < candles.length();
                i++
        ) {

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


            openList.add(open);

            highList.add(high);

            lowList.add(low);

            closeList.add(close);

            volumeList.add(volume);
        }
    }


    // =========================================================
    // GOLD
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


        if (
                results.length() == 0
        ) {

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
                        closes.getDouble(i);

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
                new TextView(this);


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
    // BIG SIGNAL
    // =========================================================

    private void addBigSignal(
            String signal,
            int color
    ) {

        TextView view =
                new TextView(this);


        view.setText(
                signal
        );


        view.setTextSize(
                32
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
                new TextView(this);


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
                new TextView(this);


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
                new TextView(this);


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
