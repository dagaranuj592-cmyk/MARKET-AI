package com.marketai;

import java.util.List;

public class BacktestEngine {

    // =========================================================
    // V3 SETTINGS
    // =========================================================

    private static final int LOOKBACK = 220;
    private static final int MAX_HOLD_DAYS = 5;

    private static final double MIN_STOP_PERCENT = 1.0;
    private static final double REWARD_RISK_RATIO = 1.5;

    // Approximate round-trip cost assumption.
    private static final double FEE_PER_SIDE = 0.10;
    private static final double SLIPPAGE_PER_SIDE = 0.05;


    // =========================================================
    // RESULT
    // =========================================================

    public static class BacktestResult {

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


        double totalReturn = 0.0;

        double buyTotalReturn = 0.0;
        double sellTotalReturn = 0.0;

        double grossProfit = 0.0;
        double grossLoss = 0.0;


        double equity = 100.0;
        double peakEquity = 100.0;
        double maxDrawdown = 0.0;


        // =====================================================
        // WALK FORWARD
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
                    getV3Signal(
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
            // ATR STOP
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


            double tradeReturn = 0.0;

            boolean stopHit = false;
            boolean targetHit = false;

            int exitIndex =
                    i + MAX_HOLD_DAYS;


            // =================================================
            // TRADE SIMULATION
            // =================================================

            for (int j = i + 1;
                 j <= i + MAX_HOLD_DAYS;
                 j++) {

                double high =
                        highs.get(j);

                double low =
                        lows.get(j);


                // =================================================
                // BUY
                // =================================================

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


                    boolean hitStop =
                            low <= stopPrice;

                    boolean hitTarget =
                            high >= targetPrice;


                    // Conservative assumption:
                    // if both are touched in same candle,
                    // count STOP first.
                    if (hitStop && hitTarget) {

                        tradeReturn =
                                -stopPercent;

                        stopHit = true;
                        exitIndex = j;

                        break;
                    }


                    if (hitStop) {

                        tradeReturn =
                                -stopPercent;

                        stopHit = true;
                        exitIndex = j;

                        break;
                    }


                    if (hitTarget) {

                        tradeReturn =
                                targetPercent;

                        targetHit = true;
                        exitIndex = j;

                        break;
                    }
                }


                // =================================================
                // SELL
                // =================================================

                else {

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


                    boolean hitStop =
                            high >= stopPrice;

                    boolean hitTarget =
                            low <= targetPrice;


                    if (hitStop && hitTarget) {

                        tradeReturn =
                                -stopPercent;

                        stopHit = true;
                        exitIndex = j;

                        break;
                    }


                    if (hitStop) {

                        tradeReturn =
                                -stopPercent;

                        stopHit = true;
                        exitIndex = j;

                        break;
                    }


                    if (hitTarget) {

                        tradeReturn =
                                targetPercent;

                        targetHit = true;
                        exitIndex = j;

                        break;
                    }
                }
            }


            // =================================================
            // TIME EXIT
            // =================================================

            if (!stopHit &&
                    !targetHit) {

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


                timeExitTrades++;
            }


            // =================================================
            // COSTS
            // =================================================

            double transactionCost =
                    (
                            FEE_PER_SIDE * 2.0
                    )
                            +
                            (
                                    SLIPPAGE_PER_SIDE * 2.0
                            );


            tradeReturn -=
                    transactionCost;


            // =================================================
            // COUNTERS
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


            if (stopHit) {

                stopLossTrades++;

            } else if (targetHit) {

                takeProfitTrades++;
            }


            // =================================================
            // RESULT
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
                    1.0
                            + tradeReturn / 100.0;


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
            // NO OVERLAPPING POSITIONS
            // =================================================

            i =
                    exitIndex + 1;
        }


        // =====================================================
        // METRICS
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


        double averageReturn = 0.0;

        if (totalTrades > 0) {

            averageReturn =
                    totalReturn
                            / totalTrades;
        }


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
        // BUY & HOLD
        // =====================================================

        double buyHoldReturn = 0.0;

        if (prices.get(0) > 0.0) {

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
    // V3 SIGNAL ENGINE
    // =========================================================

    private static String getV3Signal(
            TechnicalAnalyzer.TechnicalResult result,
            double price
    ) {

        if (result == null ||
                price <= 0.0) {

            return "NEUTRAL";
        }


        int bullishScore = 0;
        int bearishScore = 0;


        // =====================================================
        // 1. EMA TREND STRUCTURE
        // =====================================================

        boolean above20 =
                result.ema20 > 0 &&
                        price > result.ema20;

        boolean above50 =
                result.ema50 > 0 &&
                        price > result.ema50;

        boolean above200 =
                result.ema200 > 0 &&
                        price > result.ema200;


        boolean bullAlignment =
                result.ema20 > result.ema50 &&
                        result.ema50 > result.ema200;


        boolean bearAlignment =
                result.ema20 < result.ema50 &&
                        result.ema50 < result.ema200;


        if (bullAlignment) {

            bullishScore += 3;

        } else if (bearAlignment) {

            bearishScore += 3;
        }


        // Price relative to long-term trend
        if (above200) {

            bullishScore += 2;

        } else if (result.ema200 > 0) {

            bearishScore += 2;
        }


        // Price relative to medium trend
        if (above50) {

            bullishScore++;

        } else if (result.ema50 > 0) {

            bearishScore++;
        }


        // =====================================================
        // 2. RSI
        // =====================================================

        if (result.rsi >= 55.0 &&
                result.rsi <= 70.0) {

            bullishScore += 2;

        } else if (result.rsi <= 45.0 &&
                result.rsi >= 30.0) {

            bearishScore += 2;
        }


        // Avoid blindly buying extreme RSI
        if (result.rsi > 75.0) {

            bullishScore -= 2;
        }


        // Avoid blindly selling extreme RSI
        if (result.rsi < 25.0) {

            bearishScore -= 2;
        }


        // =====================================================
        // 3. MACD
        // =====================================================

        if (result.macd > 0.0) {

            bullishScore += 2;

        } else if (result.macd < 0.0) {

            bearishScore += 2;
        }


        // =====================================================
        // 4. MOMENTUM
        // =====================================================

        if (result.momentum >= 1.0) {

            bullishScore += 2;

        } else if (result.momentum <= -1.0) {

            bearishScore += 2;

        } else if (result.momentum > 0.0) {

            bullishScore++;

        } else if (result.momentum < 0.0) {

            bearishScore++;
        }


        // =====================================================
        // 5. BOLLINGER POSITION
        // =====================================================

        if (result.bollingerUpper > 0.0 &&
                result.bollingerLower > 0.0) {

            double middle =
                    (
                            result.bollingerUpper
                                    + result.bollingerLower
                    )
                            / 2.0;


            double range =
                    result.bollingerUpper
                            - result.bollingerLower;


            if (range > 0.0) {

                double position =
                        (
                                price
                                        - result.bollingerLower
                        )
                                / range;


                if (position > 0.55 &&
                        position < 0.90) {

                    bullishScore++;

                } else if (position < 0.45 &&
                        position > 0.10) {

                    bearishScore++;
                }
            }
        }


        // =====================================================
        // 6. VOLUME CONFIRMATION
        // =====================================================

        if (result.volumeRatio >= 1.20) {

            if (bullishScore > bearishScore) {

                bullishScore++;

            } else if (bearishScore > bullishScore) {

                bearishScore++;
            }
        }


        // =====================================================
        // 7. MARKET REGIME
        // =====================================================

        int trendStrength =
                Math.abs(
                        bullishScore
                                - bearishScore
                );


        // Strong directional trend
        if (bullishScore >= 9 &&
                bullishScore > bearishScore &&
                trendStrength >= 3) {

            return "BUY";
        }


        if (bearishScore >= 9 &&
                bearishScore > bullishScore &&
                trendStrength >= 3) {

            return "SELL";
        }


        // Medium trend:
        // require stronger confirmation
        if (bullishScore >= 11 &&
                bullishScore > bearishScore) {

            return "BUY";
        }


        if (bearishScore >= 11 &&
                bearishScore > bullishScore) {

            return "SELL";
        }


        // =====================================================
        // SIDEWAYS / CONFLICTING MARKET
        // =====================================================

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
