package com.marketai;

import java.util.ArrayList;
import java.util.List;

public class TechnicalAnalyzer {

    public static class Analysis {

        public double ema20;
        public double ema50;
        public double ema200;

        public double rsi;

        public double macd;
        public double macdSignal;
        public double macdHistogram;

        public double atr;

        public String trend;
        public String momentum;

        public Analysis() {
            ema20 = 0;
            ema50 = 0;
            ema200 = 0;

            rsi = 50;

            macd = 0;
            macdSignal = 0;
            macdHistogram = 0;

            atr = 0;

            trend = "NEUTRAL";
            momentum = "NEUTRAL";
        }
    }

    public static Analysis analyze(
            List<Double> closes,
            List<Double> highs,
            List<Double> lows
    ) {

        Analysis result = new Analysis();

        if (closes == null ||
                closes.size() < 50) {

            return result;
        }

        result.ema20 =
                calculateEMA(
                        closes,
                        20
                );

        result.ema50 =
                calculateEMA(
                        closes,
                        50
                );

        result.ema200 =
                calculateEMA(
                        closes,
                        Math.min(
                                200,
                                closes.size()
                        )
                );

        result.rsi =
                calculateRSI(
                        closes,
                        14
                );

        double[] macd =
                calculateMACD(
                        closes
                );

        result.macd =
                macd[0];

        result.macdSignal =
                macd[1];

        result.macdHistogram =
                macd[2];

        if (highs != null &&
                lows != null &&
                highs.size() == closes.size() &&
                lows.size() == closes.size()) {

            result.atr =
                    calculateATR(
                            highs,
                            lows,
                            closes,
                            14
                    );
        }

        /*
         * TREND
         */

        double current =
                closes.get(
                        closes.size() - 1
                );

        if (current > result.ema20 &&
                result.ema20 > result.ema50 &&
                result.ema50 > result.ema200) {

            result.trend =
                    "STRONG UP";

        } else if (current > result.ema50 &&
                result.ema50 > result.ema200) {

            result.trend =
                    "UP";

        } else if (current < result.ema20 &&
                result.ema20 < result.ema50 &&
                result.ema50 < result.ema200) {

            result.trend =
                    "STRONG DOWN";

        } else if (current < result.ema50 &&
                result.ema50 < result.ema200) {

            result.trend =
                    "DOWN";

        } else {

            result.trend =
                    "NEUTRAL";
        }

        /*
         * MOMENTUM
         */

        if (result.rsi >= 60 &&
                result.macdHistogram > 0) {

            result.momentum =
                    "BULLISH";

        } else if (result.rsi <= 40 &&
                result.macdHistogram < 0) {

            result.momentum =
                    "BEARISH";

        } else {

            result.momentum =
                    "NEUTRAL";
        }

        return result;
    }

    /*
     * =====================================================
     * EMA
     * =====================================================
     */

    public static double calculateEMA(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() == 0) {

            return 0;
        }

        period =
                Math.min(
                        period,
                        prices.size()
                );

        double multiplier =
                2.0 /
                (period + 1);

        double ema = 0;

        /*
         * Initial SMA
         */

        for (int i = 0;
             i < period;
             i++) {

            ema +=
                    prices.get(i);
        }

        ema =
                ema / period;

        /*
         * EMA calculation
         */

        for (int i = period;
             i < prices.size();
             i++) {

            double price =
                    prices.get(i);

            ema =
                    (
                        price - ema
                    )
                    * multiplier
                    + ema;
        }

        return ema;
    }

    /*
     * =====================================================
     * RSI
     * =====================================================
     */

    public static double calculateRSI(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() <= period) {

            return 50;
        }

        double gain = 0;
        double loss = 0;

        for (int i = 1;
             i <= period;
             i++) {

            double change =
                    prices.get(i)
                    -
                    prices.get(i - 1);

            if (change > 0) {

                gain += change;

            } else {

                loss -= change;
            }
        }

        double averageGain =
                gain / period;

        double averageLoss =
                loss / period;

        for (int i = period + 1;
             i < prices.size();
             i++) {

            double change =
                    prices.get(i)
                    -
                    prices.get(i - 1);

            double currentGain =
                    Math.max(
                            change,
                            0
                    );

            double currentLoss =
                    Math.max(
                            -change,
                            0
                    );

            averageGain =
                    (
                        averageGain *
                        (period - 1)
                        + currentGain
                    ) / period;

            averageLoss =
                    (
                        averageLoss *
                        (period - 1)
                        + currentLoss
                    ) / period;
        }

        if (averageLoss == 0) {

            return 100;
        }

        double relativeStrength =
                averageGain /
                averageLoss;

        return 100 -
                (
                    100 /
                    (
                        1 +
                        relativeStrength
                    )
                );
    }

    /*
     * =====================================================
     * MACD
     * =====================================================
     *
     * MACD = EMA12 - EMA26
     *
     * Signal = EMA9 of MACD values
     */

    public static double[] calculateMACD(
            List<Double> prices
    ) {

        double[] output =
                new double[]{
                        0,
                        0,
                        0
                };

        if (prices == null ||
                prices.size() < 35) {

            return output;
        }

        ArrayList<Double> macdValues =
                new ArrayList<>();

        double ema12 =
                calculateInitialEMA(
                        prices,
                        12
                );

        double ema26 =
                calculateInitialEMA(
                        prices,
                        26
                );

        double multiplier12 =
                2.0 / 13.0;

        double multiplier26 =
                2.0 / 27.0;

        /*
         * Build EMA values from beginning.
         */

        double ema12Current = 0;
        double ema26Current = 0;

        for (int i = 0;
             i < prices.size();
             i++) {

            double price =
                    prices.get(i);

            if (i == 11) {

                ema12Current =
                        calculateSMAAt(
                                prices,
                                i,
                                12
                        );

            } else if (i > 11) {

                ema12Current =
                        (
                            price -
                            ema12Current
                        )
                        * multiplier12
                        +
                        ema12Current;
            }

            if (i == 25) {

                ema26Current =
                        calculateSMAAt(
                                prices,
                                i,
                                26
                        );

            } else if (i > 25) {

                ema26Current =
                        (
                            price -
                            ema26Current
                        )
                        * multiplier26
                        +
                        ema26Current;
            }

            if (i >= 25) {

                double macdValue =
                        ema12Current -
                        ema26Current;

                macdValues.add(
                        macdValue
                );
            }
        }

        if (macdValues.size() == 0) {
            return output;
        }

        double signal =
                calculateEMA(
                        macdValues,
                        Math.min(
                                9,
                                macdValues.size()
                        )
                );

        double macd =
                macdValues.get(
                        macdValues.size() - 1
                );

        output[0] =
                macd;

        output[1] =
                signal;

        output[2] =
                macd - signal;

        return output;
    }

    private static double calculateInitialEMA(
            List<Double> prices,
            int period
    ) {

        if (prices.size() < period) {
            return 0;
        }

        return calculateSMAAt(
                prices,
                period - 1,
                period
        );
    }

    private static double calculateSMAAt(
            List<Double> prices,
            int endIndex,
            int period
    ) {

        int start =
                endIndex -
                period +
                1;

        double sum = 0;

        for (int i = start;
             i <= endIndex;
             i++) {

            sum +=
                    prices.get(i);
        }

        return sum / period;
    }

    /*
     * =====================================================
     * ATR
     * =====================================================
     */

    public static double calculateATR(
            List<Double> highs,
            List<Double> lows,
            List<Double> closes,
            int period
    ) {

        if (highs == null ||
                lows == null ||
                closes == null) {

            return 0;
        }

        if (closes.size() < period + 1) {
            return 0;
        }

        ArrayList<Double> trueRanges =
                new ArrayList<>();

        for (int i = 1;
             i < closes.size();
             i++) {

            double high =
                    highs.get(i);

            double low =
                    lows.get(i);

            double previousClose =
                    closes.get(i - 1);

            double range1 =
                    high - low;

            double range2 =
                    Math.abs(
                            high -
                            previousClose
                    );

            double range3 =
                    Math.abs(
                            low -
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

            trueRanges.add(
                    trueRange
            );
        }

        if (trueRanges.size() < period) {
            return 0;
        }

        double atr = 0;

        int start =
                trueRanges.size() -
                period;

        for (int i = start;
             i < trueRanges.size();
             i++) {

            atr +=
                    trueRanges.get(i);
        }

        return atr / period;
    }
          }
