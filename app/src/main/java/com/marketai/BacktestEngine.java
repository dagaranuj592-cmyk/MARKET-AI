package com.marketai;

import java.util.List;

public class BacktestEngine {

    private static final int LOOKBACK = 220;
    private static final int FORWARD_DAYS = 5;
    private static final double TARGET_PERCENT = 1.0;

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
                double totalReturn
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
        }
    }

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
                LOOKBACK + FORWARD_DAYS) {

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

        double totalReturn = 0.0;

        /*
         * WALK-FORWARD BACKTEST
         *
         * Signal uses ONLY data available
         * at that historical point.
         *
         * Future candles are used only
         * to evaluate the result.
         */

        for (int i = LOOKBACK;
             i < size - FORWARD_DAYS;
             i++) {

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

            double currentPrice =
                    prices.get(i);

            String signal =
                    getSignal(
                            technical,
                            currentPrice
                    );

            double futurePrice =
                    prices.get(
                            i + FORWARD_DAYS
                    );

            double futureReturn =
                    (
                            futurePrice
                                    - currentPrice
                    )
                            / currentPrice
                            * 100.0;

            if (signal.equals("NEUTRAL")) {

                neutralTrades++;
                continue;
            }

            totalTrades++;

            if (signal.equals("BUY")) {

                buySignals++;

                totalReturn += futureReturn;

                if (futureReturn >=
                        TARGET_PERCENT) {

                    correctTrades++;
                    buyCorrect++;

                } else {

                    wrongTrades++;
                }

            } else if (signal.equals("SELL")) {

                sellSignals++;

                totalReturn -= futureReturn;

                if (futureReturn <=
                        -TARGET_PERCENT) {

                    correctTrades++;
                    sellCorrect++;

                } else {

                    wrongTrades++;
                }
            }
        }

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
                round(totalReturn)
        );
    }

    private static String getSignal(
            TechnicalAnalyzer.TechnicalResult result,
            double price
    ) {

        int bullish = 0;
        int bearish = 0;

        // RSI
        if (result.rsi >= 55) {

            bullish++;

        } else if (result.rsi <= 45) {

            bearish++;
        }

        // MACD
        if (result.macd > 0) {

            bullish++;

        } else if (result.macd < 0) {

            bearish++;
        }

        // EMA 50
        if (result.ema50 > 0) {

            if (price > result.ema50) {

                bullish++;

            } else {

                bearish++;
            }
        }

        // EMA 200
        if (result.ema200 > 0) {

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
        if (result.bollingerUpper > 0 &&
                result.bollingerLower > 0) {

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

        // Final signal
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
                0.0
        );
    }

    private static double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
          }
