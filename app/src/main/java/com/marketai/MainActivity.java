package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    // =========================================================
    // COLORS
    // =========================================================

    private static final int BG =
            Color.rgb(9, 13, 18);

    private static final int CARD =
            Color.rgb(17, 23, 30);

    private static final int CARD_2 =
            Color.rgb(21, 28, 36);

    private static final int BORDER =
            Color.rgb(39, 49, 60);

    private static final int WHITE =
            Color.rgb(245, 248, 250);

    private static final int MUTED =
            Color.rgb(145, 157, 170);

    private static final int GREEN =
            Color.rgb(0, 220, 120);

    private static final int RED =
            Color.rgb(255, 75, 90);

    private static final int YELLOW =
            Color.rgb(255, 190, 60);

    private static final int BLUE =
            Color.rgb(80, 170, 255);


    // =========================================================
    // LIVE BTC
    // =========================================================

    private LiveMarketEngine liveMarketEngine;
    private LiveCandleEngine liveCandleEngine;

    private TextView liveBTCPriceView;
    private TextView liveBTCStatusView;

    private TextView live5mStatusView;
    private TextView live15mStatusView;

    private TextView live5mCandleView;
    private TextView live15mCandleView;

    private TextView live5mAnalysisView;
    private TextView live15mAnalysisView;

    private double latestLiveBTC = 0.0;


    // =========================================================
    // LIVE 5M DATA
    // =========================================================

    private final List<Double> live5Open =
            new ArrayList<>();

    private final List<Double> live5High =
            new ArrayList<>();

    private final List<Double> live5Low =
            new ArrayList<>();

    private final List<Double> live5Close =
            new ArrayList<>();

    private final List<Double> live5Volume =
            new ArrayList<>();


    // =========================================================
    // LIVE 15M DATA
    // =========================================================

    private final List<Double> live15Open =
            new ArrayList<>();

    private final List<Double> live15High =
            new ArrayList<>();

    private final List<Double> live15Low =
            new ArrayList<>();

    private final List<Double> live15Close =
            new ArrayList<>();

    private final List<Double> live15Volume =
            new ArrayList<>();


    private TechnicalAnalyzer.TechnicalResult live5Technical;
    private TechnicalAnalyzer.TechnicalResult live15Technical;

    private MoveDetector.MoveResult live5Move;
    private MoveDetector.MoveResult live15Move;


    // =========================================================
    // STRATEGY ENGINE
    // =========================================================

    private StrategyEngine.StrategyResult strategy5m;
    private StrategyEngine.StrategyResult strategy15m;


    // =========================================================
    // DAILY BTC
    // =========================================================

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


    // =========================================================
    // HISTORICAL 15M
    // =========================================================

    private final List<Double> btc15Open =
            new ArrayList<>();

    private final List<Double> btc15High =
            new ArrayList<>();

    private final List<Double> btc15Low =
            new ArrayList<>();

    private final List<Double> btc15Prices =
            new ArrayList<>();

    private final List<Double> btc15Volume =
            new ArrayList<>();


    // =========================================================
    // HISTORICAL 5M
    // =========================================================

    private final List<Double> btc5Open =
            new ArrayList<>();

    private final List<Double> btc5High =
            new ArrayList<>();

    private final List<Double> btc5Low =
            new ArrayList<>();

    private final List<Double> btc5Prices =
            new ArrayList<>();

    private final List<Double> btc5Volume =
            new ArrayList<>();


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        buildBaseUI();

        showLoading();

        loadMarketData();
    }


    // =========================================================
    // BASE UI
    // =========================================================

    private void buildBaseUI() {

        scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                20,
                22,
                20,
                35
        );

        container.setBackgroundColor(BG);

        scrollView.addView(container);

        setContentView(scrollView);


        scrollView.setOnTouchListener(
                (view, event) -> {

                    if (
                            event.getAction()
                                    ==
                            MotionEvent.ACTION_DOWN
                    ) {

                        view.setTag(event.getY());

                    } else if (
                            event.getAction()
                                    ==
                            MotionEvent.ACTION_UP
                    ) {

                        Object tag =
                                view.getTag();

                        if (
                                tag instanceof Float
                        ) {

                            float start =
                                    (Float) tag;

                            float end =
                                    event.getY();

                            if (
                                    scrollView.getScrollY() == 0
                                            &&
                                    end - start > 160
                                            &&
                                    !isRefreshing
                            ) {

                                refreshMarketData();
                            }
                        }
                    }

                    return false;
                }
        );
    }


    // =========================================================
    // START
    // =========================================================

    @Override
    protected void onStart() {

        super.onStart();

        startLiveBTC();

        startLiveCandles();
    }


    // =========================================================
    // LIVE BTC
    // =========================================================

    private void startLiveBTC() {

        if (
                liveMarketEngine == null
        ) {

            liveMarketEngine =
                    new LiveMarketEngine(
                            new LiveMarketEngine.LiveMarketListener() {

                                @Override
                                public void onPriceUpdate(
                                        String symbol,
                                        double price
                                ) {

                                    latestLiveBTC = price;

                                    runOnUiThread(
                                            () -> {

                                                if (
                                                        liveBTCPriceView != null
                                                ) {

                                                    liveBTCPriceView.setText(
                                                            "$"
                                                                    +
                                                            format(price)
                                                    );
                                                }
                                            }
                                    );
                                }


                                @Override
                                public void onConnectionChanged(
                                        boolean connected
                                ) {

                                    runOnUiThread(
                                            () -> {

                                                if (
                                                        liveBTCStatusView == null
                                                ) {

                                                    return;
                                                }

                                                if (connected) {

                                                    liveBTCStatusView.setText(
                                                            "● LIVE  •  BTCUSDT"
                                                    );

                                                    liveBTCStatusView.setTextColor(
                                                            GREEN
                                                    );

                                                } else {

                                                    liveBTCStatusView.setText(
                                                            "○ RECONNECTING"
                                                    );

                                                    liveBTCStatusView.setTextColor(
                                                            YELLOW
                                                    );
                                                }
                                            }
                                    );
                                }


                                @Override
                                public void onError(
                                        String message
                                ) {

                                    runOnUiThread(
                                            () -> {

                                                if (
                                                        liveBTCStatusView != null
                                                ) {

                                                    liveBTCStatusView.setText(
                                                            "○ LIVE CONNECTION ERROR"
                                                    );

                                                    liveBTCStatusView.setTextColor(
                                                            RED
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

        if (
                liveCandleEngine == null
        ) {

            liveCandleEngine =
                    new LiveCandleEngine(
                            new LiveCandleEngine.Listener() {

                                @Override
                                public void onCandleUpdate(
                                        String interval,
                                        LiveCandleEngine.Candle candle,
                                        List<LiveCandleEngine.Candle> candles
                                ) {

                                    processLiveCandle(
                                            interval,
                                            candle
                                    );
                                }


                                @Override
                                public void onConnectionChanged(
                                        String interval,
                                        boolean connected
                                ) {

                                    runOnUiThread(
                                            () ->
                                                    updateCandleStatus(
                                                            interval,
                                                            connected
                                                    )
                                    );
                                }


                                @Override
                                public void onError(
                                        String interval,
                                        String message
                                ) {

                                    runOnUiThread(
                                            () ->
                                                    updateCandleError(
                                                            interval
                                                    )
                                    );
                                }
                            }
                    );
        }

        liveCandleEngine.start();
    }


    // =========================================================
    // STOP
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
    // PROCESS LIVE CANDLE
    // =========================================================

    private void processLiveCandle(
            String interval,
            LiveCandleEngine.Candle candle
    ) {

        if (candle == null) {
            return;
        }


        if ("5m".equals(interval)) {

            updateLiveLists(
                    candle,
                    live5Open,
                    live5High,
                    live5Low,
                    live5Close,
                    live5Volume
            );


            if (live5Close.size() >= 40) {

                live5Technical =
                        TechnicalAnalyzer.analyze(
                                live5Close,
                                live5High,
                                live5Low,
                                live5Volume
                        );

                live5Move =
                        MoveDetector.analyze(
                                live5Close,
                                live5High,
                                live5Low,
                                live5Volume,
                                5
                        );
            }


            runOnUiThread(
                    () -> {

                        if (
                                live5mCandleView != null
                        ) {

                            live5mCandleView.setText(
                                    candleText(candle)
                            );
                        }

                        updateLive5Analysis();
                    }
            );
        }


        if ("15m".equals(interval)) {

            updateLiveLists(
                    candle,
                    live15Open,
                    live15High,
                    live15Low,
                    live15Close,
                    live15Volume
            );


            if (live15Close.size() >= 40) {

                live15Technical =
                        TechnicalAnalyzer.analyze(
                                live15Close,
                                live15High,
                                live15Low,
                                live15Volume
                        );

                live15Move =
                        MoveDetector.analyze(
                                live15Close,
                                live15High,
                                live15Low,
                                live15Volume,
                                15
                        );
            }


            runOnUiThread(
                    () -> {

                        if (
                                live15mCandleView != null
                        ) {

                            live15mCandleView.setText(
                                    candleText(candle)
                            );
                        }

                        updateLive15Analysis();
                    }
            );
        }
    }


    // =========================================================
    // LIVE LIST UPDATE
    // =========================================================

    private synchronized void updateLiveLists(

            LiveCandleEngine.Candle candle,

            List<Double> opens,
            List<Double> highs,
            List<Double> lows,
            List<Double> closes,
            List<Double> volumes

    ) {

        if (closes.isEmpty()) {

            opens.add(candle.open);
            highs.add(candle.high);
            lows.add(candle.low);
            closes.add(candle.close);
            volumes.add(candle.volume);

        } else {

            int last =
                    closes.size() - 1;


            if (
                    Math.abs(
                            candle.open - opens.get(last)
                    )
                            <=
                    Math.max(
                            0.01,
                            candle.open * 0.0005
                    )
            ) {

                opens.set(
                        last,
                        candle.open
                );

                highs.set(
                        last,
                        candle.high
                );

                lows.set(
                        last,
                        candle.low
                );

                closes.set(
                        last,
                        candle.close
                );

                volumes.set(
                        last,
                        candle.volume
                );

            } else {

                opens.add(candle.open);
                highs.add(candle.high);
                lows.add(candle.low);
                closes.add(candle.close);
                volumes.add(candle.volume);
            }
        }


        while (
                closes.size() > 500
        ) {

            opens.remove(0);
            highs.remove(0);
            lows.remove(0);
            closes.remove(0);
            volumes.remove(0);
        }
    }


    // =========================================================
    // LOADING
    // =========================================================

    private void showLoading() {

        container.removeAllViews();

        showHeader();

        addSectionTitle("MARKET DATA");

        addText(
                "Loading BTC and Gold analysis...",
                14,
                MUTED
        );
    }


    // =========================================================
    // REFRESH
    // =========================================================

    private void refreshMarketData() {

        if (isRefreshing) {
            return;
        }

        isRefreshing = true;

        showLoading();

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


                        // =================================================
                        // STRATEGY ENGINE
                        // =================================================

                        StrategyEngine.StrategyResult setup5m =
                                StrategyEngine.analyze(
                                        btc5Prices,
                                        btc5High,
                                        btc5Low,
                                        btc5Volume
                                );


                        StrategyEngine.StrategyResult setup15m =
                                StrategyEngine.analyze(
                                        btc15Prices,
                                        btc15High,
                                        btc15Low,
                                        btc15Volume
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


                        new Handler(
                                Looper.getMainLooper()
                        ).post(
                                () -> {

                                    isRefreshing = false;


                                    strategy5m =
                                            setup5m;

                                    strategy15m =
                                            setup15m;


                                    showDashboard(
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
                                            move5,
                                            setup5m,
                                            setup15m
                                    );
                                }
                        );

                    } catch (Exception e) {

                        new Handler(
                                Looper.getMainLooper()
                        ).post(
                                () -> {

                                    isRefreshing = false;

                                    showError(e);
                                }
                        );
                    }

                }
        ).start();
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard(

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

            MoveDetector.MoveResult move5,

            StrategyEngine.StrategyResult setup5m,

            StrategyEngine.StrategyResult setup15m

    ) {

        container.removeAllViews();


        showHeader();


        // =====================================================
        // LIVE BTC
        // =====================================================

        addSectionTitle("LIVE MARKET");


        LinearLayout liveCard =
                createCard();


        liveCard.addView(
                cardTitle("BTCUSDT")
        );


        liveBTCPriceView =
                new TextView(this);

        liveBTCPriceView.setTextSize(34);

        liveBTCPriceView.setTextColor(WHITE);

        liveBTCPriceView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        liveBTCPriceView.setPadding(
                0,
                5,
                0,
                2
        );


        double displayPrice =
                latestLiveBTC > 0
                        ? latestLiveBTC
                        : currentPrice;


        liveBTCPriceView.setText(
                "$" + format(displayPrice)
        );


        liveCard.addView(
                liveBTCPriceView
        );


        liveBTCStatusView =
                new TextView(this);

        liveBTCStatusView.setText(
                "● LIVE  •  BTCUSDT"
        );

        liveBTCStatusView.setTextSize(12);

        liveBTCStatusView.setTextColor(GREEN);

        liveCard.addView(
                liveBTCStatusView
        );


        container.addView(
                liveCard
        );


        // =====================================================
        // FINAL SIGNAL
        // =====================================================

        addSectionTitle("FINAL SIGNAL");


        LinearLayout signalCard =
                createCard();


        TextView signal =
                new TextView(this);


        String direction =
                combined.direction;


        signal.setText(
                "NEUTRAL".equals(direction)
                        ? "WAIT"
                        : direction
        );


        signal.setTextSize(30);

        signal.setGravity(Gravity.CENTER);

        signal.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        signal.setTextColor(
                signalColor(direction)
        );


        signalCard.addView(signal);


        addCardMetric(
                signalCard,
                "Confidence",
                format(combined.confidence) + "%"
        );


        addCardMetric(
                signalCard,
                "Signal Strength",
                combined.signalStrength
        );


        addCardMetric(
                signalCard,
                "Engine Agreement",
                combined.agreement
        );


        signalCard.addView(divider());


        addCardMetric(
                signalCard,
                "BUY",
                format(combined.buyProbability) + "%"
        );


        addCardMetric(
                signalCard,
                "SELL",
                format(combined.sellProbability) + "%"
        );


        addCardMetric(
                signalCard,
                "WAIT",
                format(combined.neutralProbability) + "%"
        );


        container.addView(signalCard);


        // =====================================================
        // STRATEGY SETUP
        // =====================================================

        addSectionTitle("STRATEGY SETUP");


        // =====================================================
        // 5M STRATEGY
        // =====================================================

        LinearLayout strategyCard5 =
                createCard();


        strategyCard5.addView(
                cardTitle("5M SETUP")
        );


        TextView setupTitle5 =
                new TextView(this);


        setupTitle5.setText(
                setup5m.setup
        );

        setupTitle5.setTextSize(25);

        setupTitle5.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        setupTitle5.setTextColor(
                strategyColor(setup5m.setup)
        );

        setupTitle5.setPadding(
                0,
                4,
                0,
                8
        );


        strategyCard5.addView(
                setupTitle5
        );


        addCardMetric(
                strategyCard5,
                "Strategy",
                setup5m.strategy
        );


        addCardMetric(
                strategyCard5,
                "Quality",
                format(setup5m.qualityScore) + "%"
        );


        addCardMetric(
                strategyCard5,
                "Direction",
                setup5m.direction
        );


        if (
                !"NO SETUP".equals(
                        setup5m.setup
                )
        ) {

            strategyCard5.addView(
                    divider()
            );


            addCardMetric(
                    strategyCard5,
                    "Entry",
                    "$" +
                    format(setup5m.entry)
            );


            addCardMetric(
                    strategyCard5,
                    "Stop Loss",
                    "$" +
                    format(setup5m.stopLoss)
            );


            addCardMetric(
                    strategyCard5,
                    "Target 1",
                    "$" +
                    format(setup5m.target1)
            );


            addCardMetric(
                    strategyCard5,
                    "Target 2",
                    "$" +
                    format(setup5m.target2)
            );


            addCardMetric(
                    strategyCard5,
                    "R:R 1",
                    format(setup5m.riskReward1)
            );


            addCardMetric(
                    strategyCard5,
                    "R:R 2",
                    format(setup5m.riskReward2)
            );
        }


        strategyCard5.addView(
                divider()
        );


        addCardMetric(
                strategyCard5,
                "EMA",
                setup5m.emaAligned
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard5,
                "Trend",
                setup5m.trendAligned
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard5,
                "Momentum",
                setup5m.momentumConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard5,
                "Volume",
                setup5m.volumeConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard5,
                "Bollinger",
                setup5m.bollingerBreakout
                        ? "BREAKOUT"
                        : "NO"
        );


        addCardMetric(
                strategyCard5,
                "RSI",
                setup5m.rsiConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        strategyCard5.addView(
                divider()
        );


        TextView evidence5 =
                new TextView(this);

        evidence5.setText(
                setup5m.evidence
        );

        evidence5.setTextSize(12);

        evidence5.setTextColor(MUTED);

        evidence5.setPadding(
                0,
                5,
                0,
                5
        );


        strategyCard5.addView(
                evidence5
        );


        container.addView(
                strategyCard5
        );


        // =====================================================
        // 15M STRATEGY
        // =====================================================

        LinearLayout strategyCard15 =
                createCard();


        strategyCard15.addView(
                cardTitle("15M SETUP")
        );


        TextView setupTitle15 =
                new TextView(this);


        setupTitle15.setText(
                setup15m.setup
        );

        setupTitle15.setTextSize(25);

        setupTitle15.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        setupTitle15.setTextColor(
                strategyColor(setup15m.setup)
        );

        setupTitle15.setPadding(
                0,
                4,
                0,
                8
        );


        strategyCard15.addView(
                setupTitle15
        );


        addCardMetric(
                strategyCard15,
                "Strategy",
                setup15m.strategy
        );


        addCardMetric(
                strategyCard15,
                "Quality",
                format(setup15m.qualityScore) + "%"
        );


        addCardMetric(
                strategyCard15,
                "Direction",
                setup15m.direction
        );


        if (
                !"NO SETUP".equals(
                        setup15m.setup
                )
        ) {

            strategyCard15.addView(
                    divider()
            );


            addCardMetric(
                    strategyCard15,
                    "Entry",
                    "$" +
                    format(setup15m.entry)
            );


            addCardMetric(
                    strategyCard15,
                    "Stop Loss",
                    "$" +
                    format(setup15m.stopLoss)
            );


            addCardMetric(
                    strategyCard15,
                    "Target 1",
                    "$" +
                    format(setup15m.target1)
            );


            addCardMetric(
                    strategyCard15,
                    "Target 2",
                    "$" +
                    format(setup15m.target2)
            );


            addCardMetric(
                    strategyCard15,
                    "R:R 1",
                    format(setup15m.riskReward1)
            );


            addCardMetric(
                    strategyCard15,
                    "R:R 2",
                    format(setup15m.riskReward2)
            );
        }


        strategyCard15.addView(
                divider()
        );


        addCardMetric(
                strategyCard15,
                "EMA",
                setup15m.emaAligned
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard15,
                "Trend",
                setup15m.trendAligned
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard15,
                "Momentum",
                setup15m.momentumConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard15,
                "Volume",
                setup15m.volumeConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        addCardMetric(
                strategyCard15,
                "Bollinger",
                setup15m.bollingerBreakout
                        ? "BREAKOUT"
                        : "NO"
        );


        addCardMetric(
                strategyCard15,
                "RSI",
                setup15m.rsiConfirmed
                        ? "CONFIRMED"
                        : "NO"
        );


        strategyCard15.addView(
                divider()
        );


        TextView evidence15 =
                new TextView(this);

        evidence15.setText(
                setup15m.evidence
        );

        evidence15.setTextSize(12);

        evidence15.setTextColor(MUTED);

        evidence15.setPadding(
                0,
                5,
                0,
                5
        );


        strategyCard15.addView(
                evidence15
        );


        container.addView(
                strategyCard15
        );


        // =====================================================
        // MULTI TIMEFRAME AGREEMENT
        // =====================================================

        addSectionTitle(
                "MULTI-TIMEFRAME SETUP"
        );


        LinearLayout mtfCard =
                createCard();


        String mtfState =
                getStrategyAgreement(
                        setup5m,
                        setup15m
                );


        TextView mtfTitle =
                new TextView(this);


        mtfTitle.setText(
                mtfState
        );

        mtfTitle.setTextSize(22);

        mtfTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        mtfTitle.setTextColor(
                strategyColor(mtfState)
        );

        mtfTitle.setGravity(
                Gravity.CENTER
        );

        mtfTitle.setPadding(
                0,
                5,
                0,
                10
        );


        mtfCard.addView(
                mtfTitle
        );


        addCardMetric(
                mtfCard,
                "5M Direction",
                setup5m.direction
        );


        addCardMetric(
                mtfCard,
                "15M Direction",
                setup15m.direction
        );


        addCardMetric(
                mtfCard,
                "5M Quality",
                format(setup5m.qualityScore) + "%"
        );


        addCardMetric(
                mtfCard,
                "15M Quality",
                format(setup15m.qualityScore) + "%"
        );


        mtfCard.addView(
                divider()
        );


        addTextToCard(
                mtfCard,
                "Both timeframes must independently confirm conditions before treating the setup as strong."
        );


        container.addView(
                mtfCard
        );


        // =====================================================
        // SHORT TERM
        // =====================================================

        addSectionTitle(
                "SHORT-TERM MARKET"
        );


        LinearLayout shortCard =
                createCard();


        addCardMetric(
                shortCard,
                "5M Direction",
                move5.direction
        );


        addCardMetric(
                shortCard,
                "5M Move Risk",
                format(move5.moveRisk) + "%"
        );


        addCardMetric(
                shortCard,
                "15M Direction",
                move15.direction
        );


        addCardMetric(
                shortCard,
                "15M Move Risk",
                format(move15.moveRisk) + "%"
        );


        String agreement =
                getMoveAgreement(
                        move5,
                        move15
                );


        shortCard.addView(
                divider()
        );


        addCardMetric(
                shortCard,
                "Market State",
                agreement
        );


        container.addView(
                shortCard
        );


        // =====================================================
        // LIVE CANDLE MONITOR
        // =====================================================

        addSectionTitle(
                "LIVE CANDLE MONITOR"
        );


        LinearLayout candle5 =
                createCard();


        candle5.addView(
                cardTitle("5 MINUTE")
        );


        live5mStatusView =
                new TextView(this);

        live5mStatusView.setText(
                "○ CONNECTING"
        );

        live5mStatusView.setTextSize(12);

        live5mStatusView.setTextColor(YELLOW);

        candle5.addView(
                live5mStatusView
        );


        live5mCandleView =
                new TextView(this);

        live5mCandleView.setText(
                "Waiting for live candle..."
        );

        live5mCandleView.setTextSize(14);

        live5mCandleView.setTextColor(WHITE);

        live5mCandleView.setPadding(
                0,
                10,
                0,
                8
        );


        candle5.addView(
                live5mCandleView
        );


        live5mAnalysisView =
                new TextView(this);

        live5mAnalysisView.setText(
                "Analysis waiting..."
        );

        live5mAnalysisView.setTextSize(13);

        live5mAnalysisView.setTextColor(MUTED);


        candle5.addView(
                live5mAnalysisView
        );


        container.addView(
                candle5
        );


        LinearLayout candle15 =
                createCard();


        candle15.addView(
                cardTitle("15 MINUTE")
        );


        live15mStatusView =
                new TextView(this);

        live15mStatusView.setText(
                "○ CONNECTING"
        );

        live15mStatusView.setTextSize(12);

        live15mStatusView.setTextColor(YELLOW);

        candle15.addView(
                live15mStatusView
        );


        live15mCandleView =
                new TextView(this);

        live15mCandleView.setText(
                "Waiting for live candle..."
        );

        live15mCandleView.setTextSize(14);

        live15mCandleView.setTextColor(WHITE);

        live15mCandleView.setPadding(
                0,
                10,
                0,
                8
        );


        candle15.addView(
                live15mCandleView
        );


        live15mAnalysisView =
                new TextView(this);

        live15mAnalysisView.setText(
                "Analysis waiting..."
        );

        live15mAnalysisView.setTextSize(13);

        live15mAnalysisView.setTextColor(MUTED);


        candle15.addView(
                live15mAnalysisView
        );


        container.addView(
                candle15
        );


        // =====================================================
        // MARKET STRUCTURE
        // =====================================================

        addSectionTitle(
                "MARKET STRUCTURE"
        );


        LinearLayout marketCard =
                createCard();


        addCardMetric(
                marketCard,
                "BTC",
                "$" + format(currentPrice)
        );


        addCardMetric(
                marketCard,
                "Gold",
                "$" + format(goldPrice)
        );


        addCardMetric(
                marketCard,
                "Trend",
                trend
        );


        addCardMetric(
                marketCard,
                "Support",
                "$" + format(support)
        );


        addCardMetric(
                marketCard,
                "Resistance",
                "$" + format(resistance)
        );


        addCardMetric(
                marketCard,
                "Average Volume",
                format(averageVolume)
        );


        container.addView(
                marketCard
        );


        // =====================================================
        // TECHNICAL
        // =====================================================

        addSectionTitle(
                "TECHNICAL ANALYSIS"
        );


        LinearLayout technicalCard =
                createCard();


        addCardMetric(
                technicalCard,
                "RSI",
                format(technical.rsi)
        );


        addCardMetric(
                technicalCard,
                "EMA 20",
                "$" + format(technical.ema20)
        );


        addCardMetric(
                technicalCard,
                "EMA 50",
                "$" + format(technical.ema50)
        );


        addCardMetric(
                technicalCard,
                "EMA 200",
                "$" + format(technical.ema200)
        );


        addCardMetric(
                technicalCard,
                "MACD",
                format(technical.macd)
        );


        addCardMetric(
                technicalCard,
                "ATR",
                "$" + format(technical.atr)
        );


        addCardMetric(
                technicalCard,
                "Momentum",
                format(technical.momentum) + "%"
        );


        addCardMetric(
                technicalCard,
                "Volume Ratio",
                format(technical.volumeRatio) + "x"
        );


        container.addView(
                technicalCard
        );


        // =====================================================
        // MOVE DETECTOR
        // =====================================================

        addSectionTitle(
                "MOVE DETECTOR"
        );


        LinearLayout moveCard =
                createCard();


        addCardMetric(
                moveCard,
                "5M Risk",
                format(move5.moveRisk) + "%"
        );


        addCardMetric(
                moveCard,
                "5M Direction",
                move5.direction
        );


        addCardMetric(
                moveCard,
                "15M Risk",
                format(move15.moveRisk) + "%"
        );


        addCardMetric(
                moveCard,
                "15M Direction",
                move15.direction
        );


        addCardMetric(
                moveCard,
                "5M Upside",
                format(move5.upsideProbability) + "%"
        );


        addCardMetric(
                moveCard,
                "5M Downside",
                format(move5.downsideProbability) + "%"
        );


        addCardMetric(
                moveCard,
                "15M Upside",
                format(move15.upsideProbability) + "%"
        );


        addCardMetric(
                moveCard,
                "15M Downside",
                format(move15.downsideProbability) + "%"
        );


        container.addView(
                moveCard
        );


        // =====================================================
        // AI
        // =====================================================

        addSectionTitle(
                "AI ENGINE"
        );


        LinearLayout aiCard =
                createCard();


        addCardMetric(
                aiCard,
                "Probability Direction",
                probability.direction
        );


        addCardMetric(
                aiCard,
                "Probability Confidence",
                probability.confidence
        );


        addCardMetric(
                aiCard,
                "Historical Samples",
                String.valueOf(
                        probability.samples
                )
        );


        addCardMetric(
                aiCard,
                "Validation Accuracy",
                format(
                        probability.validationAccuracy
                ) + "%"
        );


        addCardMetric(
                aiCard,
                "Learning Direction",
                learning.direction
        );


        addCardMetric(
                aiCard,
                "Learning Samples",
                String.valueOf(
                        learning.matchedSamples
                )
        );


        addCardMetric(
                aiCard,
                "Learning Accuracy",
                format(
                        learning.trainingAccuracy
                ) + "%"
        );


        container.addView(
                aiCard
        );


        // =====================================================
        // COMBINED BACKTEST
        // =====================================================

        addSectionTitle(
                "COMBINED BACKTEST"
        );


        LinearLayout cbCard =
                createCard();


        addCardMetric(
                cbCard,
                "Total Signals",
                String.valueOf(
                        combinedBacktest.totalSignals
                )
        );


        addCardMetric(
                cbCard,
                "Correct",
                String.valueOf(
                        combinedBacktest.correctSignals
                )
        );


        addCardMetric(
                cbCard,
                "Wrong",
                String.valueOf(
                        combinedBacktest.wrongSignals
                )
        );


        addCardMetric(
                cbCard,
                "Accuracy",
                format(
                        combinedBacktest.accuracy
                ) + "%"
        );


        addCardMetric(
                cbCard,
                "Average Return",
                format(
                        combinedBacktest.averageReturn
                ) + "%"
        );


        addCardMetric(
                cbCard,
                "Total Return",
                format(
                        combinedBacktest.totalReturn
                ) + "%"
        );


        addCardMetric(
                cbCard,
                "Profit Factor",
                format(
                        combinedBacktest.profitFactor
                )
        );


        addCardMetric(
                cbCard,
                "Max Drawdown",
                format(
                        combinedBacktest.maxDrawdown
                ) + "%"
        );


        addCardMetric(
                cbCard,
                "Strong Signals",
                String.valueOf(
                        combinedBacktest.strongSignals
                )
        );


        container.addView(
                cbCard
        );


        // =====================================================
        // ORIGINAL BACKTEST
        // =====================================================

        addSectionTitle(
                "BACKTEST V3"
        );


        LinearLayout btCard =
                createCard();


        addCardMetric(
                btCard,
                "Total Trades",
                String.valueOf(
                        backtest.totalTrades
                )
        );


        addCardMetric(
                btCard,
                "Correct",
                String.valueOf(
                        backtest.correctTrades
                )
        );


        addCardMetric(
                btCard,
                "Wrong",
                String.valueOf(
                        backtest.wrongTrades
                )
        );


        addCardMetric(
                btCard,
                "Accuracy",
                format(
                        backtest.accuracy
                ) + "%"
        );


        addCardMetric(
                btCard,
                "Average Return",
                format(
                        backtest.averageReturn
                ) + "%"
        );


        addCardMetric(
                btCard,
                "Total Return",
                format(
                        backtest.totalReturn
                ) + "%"
        );


        addCardMetric(
                btCard,
                "Profit Factor",
                format(
                        backtest.profitFactor
                )
        );


        addCardMetric(
                btCard,
                "Max Drawdown",
                format(
                        backtest.maxDrawdown
                ) + "%"
        );


        addCardMetric(
                btCard,
                "BTC Buy & Hold",
                format(
                        backtest.buyHoldReturn
                ) + "%"
        );


        container.addView(
                btCard
        );


        // =====================================================
        // REFRESH
        // =====================================================

        addSpace();


        TextView refresh =
                button(
                        "↻  REFRESH MARKET DATA"
                );


        refresh.setOnClickListener(
                v -> refreshMarketData()
        );


        container.addView(
                refresh
        );


        addSpace();


        addText(
                "Market AI is an analytical and research system. Historical results and model probabilities do not guarantee future results.",
                11,
                MUTED
        );


        addText(
                "Live market data: BTCUSDT.",
                11,
                MUTED
        );
    }


    // =========================================================
    // LIVE 5M ANALYSIS
    // =========================================================

    private void updateLive5Analysis() {

        if (
                live5mAnalysisView == null
        ) {

            return;
        }


        if (
                live5Technical == null
                        ||
                live5Move == null
        ) {

            live5mAnalysisView.setText(
                    "Collecting live candle history..."
            );

            return;
        }


        live5mAnalysisView.setText(
                "RSI  "
                        +
                format(live5Technical.rsi)
                        +
                "   •   EMA20  $"
                        +
                format(live5Technical.ema20)
                        +
                "\n"
                        +
                "MACD  "
                        +
                format(live5Technical.macd)
                        +
                "   •   Momentum  "
                        +
                format(live5Technical.momentum)
                        +
                "%\n"
                        +
                "Move Risk  "
                        +
                format(live5Move.moveRisk)
                        +
                "%   •   "
                        +
                live5Move.direction
        );


        live5mAnalysisView.setTextColor(
                signalColor(
                        live5Move.direction
                )
        );
    }


    // =========================================================
    // LIVE 15M ANALYSIS
    // =========================================================

    private void updateLive15Analysis() {

        if (
                live15mAnalysisView == null
        ) {

            return;
        }


        if (
                live15Technical == null
                        ||
                live15Move == null
        ) {

            live15mAnalysisView.setText(
                    "Collecting live candle history..."
            );

            return;
        }


        live15mAnalysisView.setText(
                "RSI  "
                        +
                format(live15Technical.rsi)
                        +
                "   •   EMA20  $"
                        +
                format(live15Technical.ema20)
                        +
                "\n"
                        +
                "MACD  "
                        +
                format(live15Technical.macd)
                        +
                "   •   Momentum  "
                        +
                format(live15Technical.momentum)
                        +
                "%\n"
                        +
                "Move Risk  "
                        +
                format(live15Move.moveRisk)
                        +
                "%   •   "
                        +
                live15Move.direction
        );


        live15mAnalysisView.setTextColor(
                signalColor(
                        live15Move.direction
                )
        );
    }


    // =========================================================
    // CANDLE STATUS
    // =========================================================

    private void updateCandleStatus(
            String interval,
            boolean connected
    ) {

        TextView target =
                "5m".equals(interval)
                        ? live5mStatusView
                        : live15mStatusView;


        if (target == null) {
            return;
        }


        if (connected) {

            target.setText(
                    "● LIVE  •  " + interval
            );

            target.setTextColor(GREEN);

        } else {

            target.setText(
                    "○ RECONNECTING  •  "
                            +
                    interval
            );

            target.setTextColor(YELLOW);
        }
    }


    // =========================================================
    // CANDLE ERROR
    // =========================================================

    private void updateCandleError(
            String interval
    ) {

        TextView target =
                "5m".equals(interval)
                        ? live5mStatusView
                        : live15mStatusView;


        if (target != null) {

            target.setText(
                    "○ CONNECTION ERROR  •  "
                            +
                    interval
            );

            target.setTextColor(RED);
        }
    }


    // =========================================================
    // CANDLE TEXT
    // =========================================================

    private String candleText(
            LiveCandleEngine.Candle candle
    ) {

        String type;


        if (
                candle.close > candle.open
        ) {

            type = "BULLISH";

        } else if (
                candle.close < candle.open
        ) {

            type = "BEARISH";

        } else {

            type = "DOJI";
        }


        String state =
                candle.closed
                        ? "CLOSED"
                        : "FORMING";


        return
                type
                        +
                "  •  "
                        +
                state
                        +
                "\n"
                        +
                "O  $"
                        +
                format(candle.open)
                        +
                "   H  $"
                        +
                format(candle.high)
                        +
                "\n"
                        +
                "L  $"
                        +
                format(candle.low)
                        +
                "   C  $"
                        +
                format(candle.close)
                        +
                "\n"
                        +
                "Volume  "
                        +
                format(candle.volume);
    }


    // =========================================================
    // HEADER
    // =========================================================

    private void showHeader() {

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );


        LinearLayout titleBox =
                new LinearLayout(this);

        titleBox.setOrientation(
                LinearLayout.VERTICAL
        );


        TextView title =
                new TextView(this);

        title.setText(
                "MARKET AI"
        );

        title.setTextSize(26);

        title.setTextColor(WHITE);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        titleBox.addView(title);


        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "BTC / GOLD ANALYTICAL ENGINE"
        );

        subtitle.setTextSize(10);

        subtitle.setTextColor(MUTED);


        titleBox.addView(subtitle);


        titleBox.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );


        header.addView(titleBox);


        TextView live =
                new TextView(this);

        live.setText("● LIVE");

        live.setTextSize(11);

        live.setTextColor(GREEN);

        live.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        header.addView(live);


        container.addView(header);


        addSpace();
    }


    // =========================================================
    // SECTION TITLE
    // =========================================================

    private void addSectionTitle(
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextSize(13);

        view.setTextColor(MUTED);

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        view.setPadding(
                2,
                18,
                2,
                8
        );


        container.addView(view);
    }


    // =========================================================
    // CARD
    // =========================================================

    private LinearLayout createCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                17,
                15,
                17,
                15
        );


        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(CARD);

        bg.setCornerRadius(20);

        bg.setStroke(
                1,
                BORDER
        );


        card.setBackground(bg);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );


        params.setMargins(
                0,
                3,
                0,
                8
        );


        card.setLayoutParams(params);


        return card;
    }


    // =========================================================
    // CARD TITLE
    // =========================================================

    private TextView cardTitle(
            String text
    ) {

        TextView title =
                new TextView(this);

        title.setText(text);

        title.setTextSize(15);

        title.setTextColor(WHITE);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setPadding(
                0,
                0,
                0,
                7
        );


        return title;
    }


    // =========================================================
    // CARD METRIC
    // =========================================================

    private void addCardMetric(

            LinearLayout card,

            String name,

            String value

    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                5,
                0,
                5
        );


        TextView left =
                new TextView(this);

        left.setText(name);

        left.setTextSize(13);

        left.setTextColor(MUTED);


        left.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );


        TextView right =
                new TextView(this);

        right.setText(value);

        right.setTextSize(13);

        right.setTextColor(
                valueColor(value)
        );

        right.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        right.setGravity(
                Gravity.RIGHT
        );


        row.addView(left);

        row.addView(right);


        card.addView(row);
    }


    // =========================================================
    // CARD TEXT
    // =========================================================

    private void addTextToCard(
            LinearLayout card,
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextSize(12);

        view.setTextColor(MUTED);

        view.setPadding(
                0,
                5,
                0,
                5
        );


        card.addView(view);
    }


    // =========================================================
    // DIVIDER
    // =========================================================

    private View divider() {

        View view =
                new View(this);

        view.setBackgroundColor(BORDER);


        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                );


        params.setMargins(
                0,
                8,
                0,
                8
        );


        view.setLayoutParams(params);


        return view;
    }


    // =========================================================
    // BUTTON
    // =========================================================

    private TextView button(
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);

        view.setTextSize(14);

        view.setTextColor(WHITE);

        view.setGravity(Gravity.CENTER);

        view.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        view.setPadding(
                10,
                17,
                10,
                17
        );


        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(CARD_2);

        bg.setCornerRadius(18);

        bg.setStroke(
                1,
                BORDER
        );


        view.setBackground(bg);


        return view;
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
                5,
                0,
                5
        );


        container.addView(view);


        return view;
    }


    // =========================================================
    // SPACE
    // =========================================================

    private void addSpace() {

        View view =
                new View(this);

        view.setLayoutParams(
                new LinearLayout.LayoutParams(
                        1,
                        8
                )
        );


        container.addView(view);
    }


    // =========================================================
    // VALUE COLOR
    // =========================================================

    private int valueColor(
            String value
    ) {

        if (value == null) {
            return WHITE;
        }


        String v =
                value.toUpperCase(
                        Locale.US
                );


        if (
                v.equals("BUY")
                        ||
                v.contains("UP")
                        ||
                v.contains("BULLISH")
                        ||
                v.contains("LONG")
                        ||
                v.contains("CONFIRMED")
                        ||
                v.contains("BREAKOUT")
        ) {

            return GREEN;
        }


        if (
                v.equals("SELL")
                        ||
                v.contains("DOWN")
                        ||
                v.contains("BEARISH")
                        ||
                v.contains("SHORT")
        ) {

            return RED;
        }


        if (
                v.equals("WAIT")
                        ||
                v.contains("NEUTRAL")
                        ||
                v.contains("UNCERTAIN")
                        ||
                v.contains("RECONNECT")
                        ||
                v.equals("NO")
                        ||
                v.contains("NO SETUP")
        ) {

            return YELLOW;
        }


        return WHITE;
    }


    // =========================================================
    // SIGNAL COLOR
    // =========================================================

    private int signalColor(
            String direction
    ) {

        if (direction == null) {
            return YELLOW;
        }


        if (
                direction.equalsIgnoreCase("BUY")
                        ||
                direction.equalsIgnoreCase("UP")
                        ||
                direction.equalsIgnoreCase("LONG")
        ) {

            return GREEN;
        }


        if (
                direction.equalsIgnoreCase("SELL")
                        ||
                direction.equalsIgnoreCase("DOWN")
                        ||
                direction.equalsIgnoreCase("SHORT")
        ) {

            return RED;
        }


        return YELLOW;
    }


    // =========================================================
    // STRATEGY COLOR
    // =========================================================

    private int strategyColor(
            String setup
    ) {

        if (setup == null) {
            return YELLOW;
        }


        String value =
                setup.toUpperCase(
                        Locale.US
                );


        if (
                value.contains("LONG")
                        ||
                value.contains("UP")
        ) {

            return GREEN;
        }


        if (
                value.contains("SHORT")
                        ||
                value.contains("DOWN")
        ) {

            return RED;
        }


        return YELLOW;
    }


    // =========================================================
    // STRATEGY AGREEMENT
    // =========================================================

    private String getStrategyAgreement(

            StrategyEngine.StrategyResult setup5,

            StrategyEngine.StrategyResult setup15

    ) {

        if (
                setup5 == null ||
                setup15 == null
        ) {

            return "WAIT";
        }


        boolean long5 =
                "LONG".equalsIgnoreCase(
                        setup5.direction
                );


        boolean long15 =
                "LONG".equalsIgnoreCase(
                        setup15.direction
                );


        boolean short5 =
                "SHORT".equalsIgnoreCase(
                        setup5.direction
                );


        boolean short15 =
                "SHORT".equalsIgnoreCase(
                        setup15.direction
                );


        boolean valid5 =
                setup5.qualityScore >= 60;


        boolean valid15 =
                setup15.qualityScore >= 60;


        if (
                long5 &&
                long15 &&
                valid5 &&
                valid15
        ) {

            return "LONG AGREEMENT";
        }


        if (
                short5 &&
                short15 &&
                valid5 &&
                valid15
        ) {

            return "SHORT AGREEMENT";
        }


        if (
                long5 &&
                long15
        ) {

            return "LONG BIAS";
        }


        if (
                short5 &&
                short15
        ) {

            return "SHORT BIAS";
        }


        if (
                setup5.setup.equals("NO SETUP")
                        &&
                setup15.setup.equals("NO SETUP")
        ) {

            return "NO SETUP";
        }


        return "TIMEFRAME CONFLICT";
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
                price > result.ema20
        ) {

            bullish++;

        } else {

            bearish++;
        }


        if (
                price > result.ema50
        ) {

            bullish++;

        } else {

            bearish++;
        }


        if (
                price > result.ema200
        ) {

            bullish++;

        } else {

            bearish++;
        }


        if (
                result.momentum > 0
        ) {

            bullish++;

        } else {

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
    // AVERAGE VOLUME
    // =========================================================

    private double calculateAverageVolume() {

        if (
                btcVolume.isEmpty()
        ) {

            return 0;
        }


        int count =
                Math.min(
                        20,
                        btcVolume.size()
                );


        int start =
                btcVolume.size() - count;


        double sum = 0;


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
    // BTC FETCH
    // =========================================================

    private void fetchBTC()
            throws Exception {

        String url =
                "https://api.binance.com/api/v3/klines"
                        +
                "?symbol=BTCUSDT"
                        +
                "&interval=1d"
                        +
                "&limit=730";


        fetchCandleData(
                url,
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
                    "Not enough BTC daily data"
            );
        }
    }


    // =========================================================
    // INTRADAY FETCH
    // =========================================================

    private void fetchIntradayBTC(

            String interval,

            int limit,

            List<Double> opens,
            List<Double> highs,
            List<Double> lows,
            List<Double> closes,
            List<Double> volumes

    ) throws Exception {

        String url =
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
                url,
                opens,
                highs,
                lows,
                closes,
                volumes
        );


        if (
                closes.size() < 1000
        ) {

            throw new Exception(
                    "Not enough BTC "
                            +
                    interval
                            +
                    " data"
            );
        }
    }


    // =========================================================
    // GENERIC CANDLE FETCH
    // =========================================================

    private void fetchCandleData(

            String urlString,

            List<Double> opens,
            List<Double> highs,
            List<Double> lows,
            List<Double> closes,
            List<Double> volumes

    ) throws Exception {

        URL url =
                new URL(urlString);


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


        int code =
                connection.getResponseCode();


        if (
                code != 200
        ) {

            throw new Exception(
                    "Market server error: "
                            +
                    code
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


        JSONArray data =
                new JSONArray(
                        response.toString()
                );


        opens.clear();
        highs.clear();
        lows.clear();
        closes.clear();
        volumes.clear();


        for (
                int i = 0;
                i < data.length();
                i++
        ) {

            JSONArray c =
                    data.getJSONArray(i);


            opens.add(
                    Double.parseDouble(
                            c.getString(1)
                    )
            );


            highs.add(
                    Double.parseDouble(
                            c.getString(2)
                    )
            );


            lows.add(
                    Double.parseDouble(
                            c.getString(3)
                    )
            );


            closes.add(
                    Double.parseDouble(
                            c.getString(4)
                    )
            );


            volumes.add(
                    Double.parseDouble(
                            c.getString(5)
                    )
            );
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
                new URL(urlString);


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


        int code =
                connection.getResponseCode();


        if (
                code != 200
        ) {

            throw new Exception(
                    "Gold server error: "
                            +
                    code
            );
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
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


        JSONObject root =
                new JSONObject(
                        response.toString()
                );


        JSONArray results =
                root.getJSONObject(
                        "chart"
                ).getJSONArray(
                        "result"
                );


        JSONObject indicators =
                results.getJSONObject(0)
                        .getJSONObject(
                                "indicators"
                        );


        JSONObject quote =
                indicators
                        .getJSONArray("quote")
                        .getJSONObject(0);


        JSONArray closes =
                quote.getJSONArray("close");


        for (
                int i =
                        closes.length() - 1;
                i >= 0;
                i--
        ) {

            if (
                    !closes.isNull(i)
            ) {

                double price =
                        closes.getDouble(i);


                if (
                        price > 0
                ) {

                    return price;
                }
            }
        }


        throw new Exception(
                "Gold price unavailable"
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
                move5.moveRisk >= 55
                        ||
                move15.moveRisk >= 55;


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
    // ERROR
    // =========================================================

    private void showError(
            Exception e
    ) {

        container.removeAllViews();

        showHeader();


        addSectionTitle(
                "DATA ERROR"
        );


        LinearLayout card =
                createCard();


        TextView error =
                new TextView(this);


        error.setText(
                e.getMessage() == null
                        ? "Unable to load market data."
                        : e.getMessage()
        );


        error.setTextSize(14);

        error.setTextColor(RED);


        card.addView(error);


        container.addView(card);


        TextView retry =
                button(
                        "↻  TRY AGAIN"
                );


        retry.setOnClickListener(
                v -> refreshMarketData()
        );


        container.addView(retry);
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
