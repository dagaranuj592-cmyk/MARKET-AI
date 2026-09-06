package com.marketai;

import java.util.List;

public class BacktestEngine {

    // =========================================================
    // BACKTEST SETTINGS
    // =========================================================

    private static final int LOOKBACK = 220;

    // Maximum number of candles a trade can remain open
    private static final int MAX_HOLD_DAYS = 5;

    // Minimum movement required for a signal to count as a
    // directional success when TP/SL are not hit.
    private static final double MIN_TARGET_PERCENT = 1.0;

    // Risk settings
    private static final double MIN_STOP_PERCENT = 1.0;

    // Risk : Reward = 1 : 1.5
    private static final double REWARD_RISK_RATIO = 1.5;

    // Approximate round-trip trading cost.
    // This is a configurable assumption, NOT a claim about
    // any particular exchange's current fee.
    private static final double FEE_PER_SIDE = 0.10;

    // Slippage assumption per side.
    private static final double SLIPPAGE_PER_SIDE = 0.05;


    // =========================================================
    // RESULT
    // =========================================================

    public static class BacktestResult {

        // Old fields - kept for MainActivity compatibility
        public int totalTrades;
        public int correctTrades;
        public int wrongTrades;
        public int neutralTrades;

        public int buySignals;
        public int sellSignals;

        public int buyCorrect;
        public int sellCorrect;

        public double accuracy;
        public double buyAccuracy;
        public double sellAccuracy;

        public double averageReturn;
        public double totalReturn;


        // New V2 metrics
        public int stopLossTrades;
        public int takeProfitTrades;
        public int timeExitTrades;

        public double profitFactor;
        public double grossProfit;
        public double grossLoss;

        public double maxDrawdown;

        public double buyAverageReturn;
        public double sellAverageReturn;

        public double buyTotalReturn;
        public double sellTotalReturn;

        public double buyHoldReturn;


        public BacktestResult(
                int totalTrades,
                int correctTrades,
                int wrongTrades,
                int neutralTrades,
                int buySignals,
                int sellSignals,
                int buyCorrect,
                int sellCorrect,
                double accuracy,
                double buyAccuracy,
                double sellAccuracy,
                double averageReturn,
                double totalReturn,

                int stopLossTrades,
                int takeProfitTrades,
                int timeExitTrades,

                double profitFactor,
                double grossProfit,
                double grossLoss,
                double maxDrawdown,

                double buyAverageReturn,
                double sellAverageReturn,

                double buyTotalReturn,
                double sellTotalReturn,

                double buyHoldReturn
        ) {

            this.totalTrades = totalTrades;
            this.correctTrades = correctTrades;
            this.wrongTrades = wrongTrades;
            this.neutralTrades = neutralTrades;

            this.buySignals = buySignals;
            this.sellSignals = sellSignals;

            this.buyCorrect = buyCorrect;
            this.sellCorrect = sellCorrect;

            this.accuracy = accuracy;
            this.buyAccuracy = buyAccuracy;
            this.sellAccuracy = sellAccuracy;

            this.averageReturn = averageReturn;
            this.totalReturn = totalReturn;


            this.stopLossTrades = stopLossTrades;
            this.takeProfitTrades = takeProfitTrades;
            this.timeExitTrades = timeExitTrades;

            this.profitFactor = profitFactor;
            this.grossProfit = grossProfit;
            this.grossLoss = grossLoss;

            this.maxDrawdown = maxDrawdown;

            this.buyAverageReturn = buyAverageReturn;
            this.sellAverageReturn = sellAverageReturn;

            this.buyTotalReturn = buyTotalReturn;
            this.sellTotalReturn = sellTotalReturn;

            this.buyHoldReturn = buyHoldReturn;
        }
    }


    // =========================================================
    // MAIN BACKTEST
    // =========================================================

    public static BacktestResult run(
            List<Double> prices,
            List<Double> highs,
            List<Double> lows,
            List<Double> volumes
    ) {

        if (prices == null ||
                highs == null ||
                lows == null) {

            return emptyResult();
        }


        int size =
                Math.min(
                        prices.size(),
                        Math.min(
                                highs.size(),
                                lows.size()
                        )
                );


        if (volumes != null) {

            size =
                    Math.min(
                            size,
                            volumes.size()
                    );
        }


        if (size <
                LOOKBACK + MAX_HOLD_DAYS + 1) {

            return emptyResult();
        }


        // =====================================================
        // COUNTERS
        // =====================================================

        int totalTrades = 0;

        int correctTrades = 0;
        int wrongTrades = 0;
        int neutralTrades = 0;

        int buySignals = 0;
        int sellSignals = 0;

        int buyCorrect = 0;
        int sellCorrect = 0;

        int stopLossTrades = 0;
        int takeProfitTrades = 0;
        int timeExitTrades = 0;


        // =====================================================
        // RETURN TRACKING
        // =====================================================

        double totalReturn = 0.0;

        double buyTotalReturn = 0.0;
        double sellTotalReturn = 0.0;

        double grossProfit = 0.0;
        double grossLoss = 0.0;


        // =====================================================
        // EQUITY TRACKING
        // =====================================================

        double equity = 100.0;
        double peakEquity = 100.0;

        double maxDrawdown = 0.0;


        // =====================================================
        // NON-OVERLAPPING WALK-FORWARD BACKTEST
        // =====================================================

        int i = LOOKBACK;


        while (i < size - MAX_HOLD_DAYS) {

            List<Double> historicalPrices =
                    prices.subList(
                            0,
                            i + 1
                    );

            List<Double> historicalHighs =
                    highs.subList(
                            0,
                            i + 1
                    );

            List<Double> historicalLows =
                    lows.subList(
                            0,
                            i + 1
                    );


            List<Double> historicalVolumes = null;

            if (volumes != null) {

                historicalVolumes =
                        volumes.subList(
                                0,
                                i + 1
                        );
            }


            TechnicalAnalyzer.TechnicalResult technical =
                    TechnicalAnalyzer.analyze(
                            historicalPrices,
                            historicalHighs,
                            historicalLows,
                            historicalVolumes
                    );


            double entryPrice =
                    prices.get(i);


            String signal =
                    getSignal(
                            technical,
                            entryPrice
                    );


            // =================================================
            // NEUTRAL
            // =================================================

            if (signal.equals("NEUTRAL")) {

                neutralTrades++;

                i++;

                continue;
            }


            // =================================================
            // ATR BASED STOP
            // =================================================

            double atrPercent = 0.0;

            if (technical.atr > 0.0 &&
                    entryPrice > 0.0) {

                atrPercent =
                        technical.atr
                                / entryPrice
                                * 100.0;
            }


            double stopPercent =
                    Math.max(
                            MIN_STOP_PERCENT,
                            atrPercent
                    );


            double targetPercent =
                    stopPercent
                            * REWARD_RISK_RATIO;


            // =================================================
            // FUTURE TRADE SIMULATION
            // =================================================

            double tradeReturn =
                    0.0;

            boolean closedByStop = false;
            boolean closedByTarget = false;
            boolean closedByTime = false;


            int exitIndex =
                    i + MAX_HOLD_DAYS;


            for (int j = i + 1;
                 j <= i + MAX_HOLD_DAYS;
                 j++) {

                double high =
                        highs.get(j);

                double low =
                        lows.get(j);


                // =============================================
                // BUY
                // =============================================

                if (signal.equals("BUY")) {

                    double stopPrice =
                            entryPrice
                                    * (
                                    1.0
                                            - stopPercent
                                            / 100.0
                            );


                    double targetPrice =
                            entryPrice
                                    * (
                                    1.0
                                            + targetPercent
                                            / 100.0
                            );


                    boolean stopHit =
                            low <= stopPrice;

                    boolean targetHit =
                            high >= targetPrice;


                    // If both happen on the same candle,
                    // assume STOP happened first.
                    // This is conservative.
                    if (stopHit && targetHit) {

                        tradeReturn =
                                -stopPercent;

                        exitIndex = j;

                        closedByStop = true;

                        break;
                    }


                    if (stopHit) {

                        tradeReturn =
                                -stopPercent;

                        exitIndex = j;

                        closedByStop = true;

                        break;
                    }


                    if (targetHit) {

                        tradeReturn =
                                targetPercent;

                        exitIndex = j;

                        closedByTarget = true;

                        break;
                    }
                }


                // =============================================
                // SELL
                // =============================================

                else if (signal.equals("SELL")) {

                    double stopPrice =
                            entryPrice
                                    * (
                                    1.0
                                            + stopPercent
                                            / 100.0
                            );


                    double targetPrice =
                            entryPrice
                                    * (
                                    1.0
                                            - targetPercent
                                            / 100.0
                            );


                    boolean stopHit =
                            high >= stopPrice;

                    boolean targetHit =
                            low <= targetPrice;


                    // Conservative assumption
                    if (stopHit && targetHit) {

                        tradeReturn =
                                -stopPercent;

                        exitIndex = j;

                        closedByStop = true;

                        break;
                    }


                    if (stopHit) {

                        tradeReturn =
                                -stopPercent;

                        exitIndex = j;

                        closedByStop = true;

                        break;
                    }


                    if (targetHit) {

                        tradeReturn =
                                targetPercent;

                        exitIndex = j;

                        closedByTarget = true;

                        break;
                    }
                }
            }


            // =================================================
            // TIME EXIT
            // =================================================

            if (!closedByStop &&
                    !closedByTarget) {

                double exitPrice =
                        prices.get(exitIndex);


                if (signal.equals("BUY")) {

                    tradeReturn =
                            (
                                    exitPrice
                                            - entryPrice
                            )
                                    / entryPrice
                                    * 100.0;

                } else {

                    tradeReturn =
                            (
                                    entryPrice
                                            - exitPrice
                            )
                                    / entryPrice
                                    * 100.0;
                }


                closedByTime = true;
            }


            // =================================================
            // TRANSACTION COST
            // =================================================

            double transactionCost =
                    (
                            FEE_PER_SIDE
                                    * 2.0
                    )
                            +
                            (
                                    SLIPPAGE_PER_SIDE
                                            * 2.0
                            );


            tradeReturn -= transactionCost;


            // =================================================
            // COUNT TRADE
            // =================================================

            totalTrades++;


            if (signal.equals("BUY")) {

                buySignals++;

                buyTotalReturn +=
                        tradeReturn;

            } else {

                sellSignals++;

                sellTotalReturn +=
                        tradeReturn;
            }


            totalReturn +=
                    tradeReturn;


            // =================================================
            // EXIT TYPE
            // =================================================

            if (closedByStop) {

                stopLossTrades++;

            } else if (closedByTarget) {

                takeProfitTrades++;

            } else if (closedByTime) {

                timeExitTrades++;
            }


            // =================================================
            // WIN / LOSS
            // =================================================

            if (tradeReturn > 0.0) {

                correctTrades++;

                if (signal.equals("BUY")) {

                    buyCorrect++;

                } else {

                    sellCorrect++;
                }


                grossProfit +=
                        tradeReturn;

            } else {

                wrongTrades++;

                grossLoss +=
                        Math.abs(tradeReturn);
            }


            // =================================================
            // EQUITY
            // =================================================

            equity *=
                    (
                            1.0
                                    + tradeReturn
                                    / 100.0
                    );


            if (equity > peakEquity) {

                peakEquity =
                        equity;
            }


            double drawdown =
                    (
                            peakEquity
                                    - equity
                    )
                            / peakEquity
                            * 100.0;


            if (drawdown > maxDrawdown) {

                maxDrawdown =
                        drawdown;
            }


            // =================================================
            // IMPORTANT:
            // Move directly after trade exit.
            // This prevents overlapping trades.
            // =================================================

            i =
                    exitIndex + 1;
        }


        // =====================================================
        // ACCURACY
        // =====================================================

        double accuracy = 0.0;

        if (totalTrades > 0) {

            accuracy =
                    correctTrades
                            / (double) totalTrades
                            * 100.0;
        }


        double buyAccuracy = 0.0;

        if (buySignals > 0) {

            buyAccuracy =
                    buyCorrect
                            / (double) buySignals
                            * 100.0;
        }


        double sellAccuracy = 0.0;

        if (sellSignals > 0) {

            sellAccuracy =
                    sellCorrect
                            / (double) sellSignals
                            * 100.0;
        }


        // =====================================================
        // AVERAGE RETURN
        // =====================================================

        double averageReturn = 0.0;

        if (totalTrades > 0) {

            averageReturn =
                    totalReturn
                            / totalTrades;
        }


        // =====================================================
        // BUY / SELL AVERAGE
        // =====================================================

        double buyAverageReturn = 0.0;

        if (buySignals > 0) {

            buyAverageReturn =
                    buyTotalReturn
                            / buySignals;
        }


        double sellAverageReturn = 0.0;

        if (sellSignals > 0) {

            sellAverageReturn =
                    sellTotalReturn
                            / sellSignals;
        }


        // =====================================================
        // PROFIT FACTOR
        // =====================================================

        double profitFactor = 0.0;

        if (grossLoss > 0.0) {

            profitFactor =
                    grossProfit
                            / grossLoss;

        } else if (grossProfit > 0.0) {

            profitFactor =
                    999.0;
        }


        // =====================================================
        // BUY & HOLD BENCHMARK
        // =====================================================

        double buyHoldReturn = 0.0;

        if (size > 1 &&
                prices.get(0) > 0.0) {

            buyHoldReturn =
                    (
                            prices.get(size - 1)
                                    - prices.get(0)
                    )
                            / prices.get(0)
                            * 100.0;
        }


        return new BacktestResult(

                totalTrades,

                correctTrades,

                wrongTrades,

                neutralTrades,

                buySignals,

                sellSignals,

                buyCorrect,

                sellCorrect,

                round(accuracy),

                round(buyAccuracy),

                round(sellAccuracy),

                round(averageReturn),

                round(totalReturn),


                stopLossTrades,

                takeProfitTrades,

                timeExitTrades,

                round(profitFactor),

                round(grossProfit),

                round(grossLoss),

                round(maxDrawdown),

                round(buyAverageReturn),

                round(sellAverageReturn),

                round(buyTotalReturn),

                round(sellTotalReturn),

                round(buyHoldReturn)
        );
    }


    // =========================================================
    // SIGNAL ENGINE
    // =========================================================

    private static String getSignal(
            TechnicalAnalyzer.TechnicalResult result,
            double price
    ) {

        int bullish = 0;
        int bearish = 0;


        // RSI
        if (result.rsi >= 55.0) {

            bullish++;

        } else if (result.rsi <= 45.0) {

            bearish++;
        }


        // MACD
        if (result.macd > 0.0) {

            bullish++;

        } else if (result.macd < 0.0) {

            bearish++;
        }


        // EMA 50
        if (result.ema50 > 0.0) {

            if (price > result.ema50) {

                bullish++;

            } else {

                bearish++;
            }
        }


        // EMA 200
        if (result.ema200 > 0.0) {

            if (price > result.ema200) {

                bullish++;

            } else {

                bearish++;
            }
        }


        // Momentum
        if (result.momentum > 0.50) {

            bullish++;

        } else if (result.momentum < -0.50) {

            bearish++;
        }


        // Bollinger middle
        if (result.bollingerUpper > 0.0 &&
                result.bollingerLower > 0.0) {

            double middle =
                    (
                            result.bollingerUpper
                                    + result.bollingerLower
                    )
                            / 2.0;


            if (price > middle) {

                bullish++;

            } else {

                bearish++;
            }
        }


        // =====================================================
        // STRONG SIGNAL ONLY
        // =====================================================

        if (bullish >= 4 &&
                bullish > bearish) {

            return "BUY";
        }


        if (bearish >= 4 &&
                bearish > bullish) {

            return "SELL";
        }


        return "NEUTRAL";
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static BacktestResult emptyResult() {

        return new BacktestResult(

                0,
                0,
                0,
                0,

                0,
                0,

                0,
                0,

                0.0,
                0.0,
                0.0,

                0.0,
                0.0,


                0,
                0,
                0,

                0.0,
                0.0,
                0.0,

                0.0,

                0.0,
                0.0,

                0.0,
                0.0,

                0.0
        );
    }


    // =========================================================
    // ROUND
    // =========================================================

    private static double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
                }
