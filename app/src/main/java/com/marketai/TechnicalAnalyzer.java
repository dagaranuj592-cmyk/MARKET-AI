package com.marketai;

import java.util.List;

public class TechnicalAnalyzer {

    // =========================================================
    // TECHNICAL RESULT
    // =========================================================

    public static class TechnicalResult {

        public double rsi;
        public double ema20;
        public double macd;
        public double atr;

        public TechnicalResult(
                double rsi,
                double ema20,
                double macd,
                double atr
        ) {

            this.rsi = rsi;
            this.ema20 = ema20;
            this.macd = macd;
            this.atr = atr;
        }
    }

    // =========================================================
    // MAIN ANALYSIS
    // =========================================================

    public static TechnicalResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low
    ) {

        if (close == null ||
                close.size() < 30) {

            return new TechnicalResult(
                    50.0,
                    0.0,
                    0.0,
                    0.0
            );
        }

        double ema20 =
                calculateEMA(
                        close,
                        20
                );

        double rsi =
                calculateRSI(
                        close,
                        14
                );

        double macd =
                calculateMACD(
                        close
                );

        double atr =
                calculateATR(
                        close,
                        high,
                        low,
                        14
                );

        return new TechnicalResult(
                rsi,
                ema20,
                macd,
                atr
        );
    }

    // =========================================================
    // EMA
    // =========================================================

    private static double calculateEMA(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() == 0) {

            return 0.0;
        }

        if (prices.size() < period) {

            return prices.get(
                    prices.size() - 1
            );
        }

        int start =
                prices.size() - period;

        double sum = 0.0;

        for (int i = start;
             i < prices.size();
             i++) {

            sum += prices.get(i);
        }

        double ema =
                sum / period;

        double multiplier =
                2.0 /
                (period + 1.0);

        /*
         * Continue calculation from
         * the first available candle.
         */
        for (int i = period;
             i < prices.size();
             i++) {

            double price =
                    prices.get(i);

            ema =
                    (
                        (price - ema)
                        * multiplier
                    )
                    + ema;
        }

        return ema;
    }

    // =========================================================
    // RSI
    // =========================================================

    private static double calculateRSI(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() <= period) {

            return 50.0;
        }

        double gain = 0.0;
        double loss = 0.0;

        int start =
                prices.size() - period;

        for (int i = start;
             i < prices.size();
             i++) {

            double change =
                    prices.get(i)
                    -
                    prices.get(i - 1);

            if (change > 0) {

                gain += change;

            } else {

                loss +=
                        Math.abs(change);
            }
        }

        double averageGain =
                gain / period;

        double averageLoss =
                loss / period;

        if (averageLoss == 0.0) {

            if (averageGain > 0.0) {
                return 100.0;
            }

            return 50.0;
        }

        double rs =
                averageGain /
                averageLoss;

        double rsi =
                100.0 -
                (
                    100.0 /
                    (1.0 + rs)
                );

        if (rsi < 0.0) {
            rsi = 0.0;
        }

        if (rsi > 100.0) {
            rsi = 100.0;
        }

        return rsi;
    }

    // =========================================================
    // MACD
    // =========================================================

    private static double calculateMACD(
            List<Double> prices
    ) {

        if (prices == null ||
                prices.size() < 26) {

            return 0.0;
        }

        double ema12 =
                calculateEMA(
                        prices,
                        12
                );

        double ema26 =
                calculateEMA(
                        prices,
                        26
                );

        return ema12 - ema26;
    }

    // =========================================================
    // ATR
    // =========================================================

    private static double calculateATR(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            int period
    ) {

        if (close == null ||
                high == null ||
                low == null) {

            return 0.0;
        }

        int size =
                Math.min(
                        close.size(),
                        Math.min(
                                high.size(),
                                low.size()
                        )
                );

        if (size < period + 1) {

            return 0.0;
        }

        int start =
                size - period;

        double trSum = 0.0;

        for (int i = start;
             i < size;
             i++) {

            double currentHigh =
                    high.get(i);

            double currentLow =
                    low.get(i);

            double previousClose =
                    close.get(i - 1);

            double range1 =
                    currentHigh -
                    currentLow;

            double range2 =
                    Math.abs(
                            currentHigh -
                            previousClose
                    );

            double range3 =
                    Math.abs(
                            currentLow -
                            previousClose
                    );

            double trueRange =
                    Math.max(
                            range1,
                            Math.max(
                                    range2,
                                    range3
                            )
                    );

            trSum += trueRange;
        }

        return trSum / period;
    }
    }
