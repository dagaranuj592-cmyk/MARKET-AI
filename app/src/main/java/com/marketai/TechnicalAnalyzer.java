package com.marketai;

import java.util.List;

public class TechnicalAnalyzer {

    public static class TechnicalResult {

        public double rsi;
        public double ema20;
        public double ema50;
        public double ema200;
        public double macd;
        public double atr;

        public double bollingerUpper;
        public double bollingerLower;

        public double momentum;
        public double volumeRatio;

        public TechnicalResult(
                double rsi,
                double ema20,
                double ema50,
                double ema200,
                double macd,
                double atr,
                double bollingerUpper,
                double bollingerLower,
                double momentum,
                double volumeRatio
        ) {

            this.rsi = rsi;
            this.ema20 = ema20;
            this.ema50 = ema50;
            this.ema200 = ema200;
            this.macd = macd;
            this.atr = atr;
            this.bollingerUpper = bollingerUpper;
            this.bollingerLower = bollingerLower;
            this.momentum = momentum;
            this.volumeRatio = volumeRatio;
        }
    }

    // =========================================================
    // MAIN ANALYZER
    // =========================================================

    public static TechnicalResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low
    ) {

        return analyze(
                close,
                high,
                low,
                null
        );
    }

    // =========================================================
    // OHLCV ANALYZER
    // =========================================================

    public static TechnicalResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        if (close == null ||
                close.size() < 30) {

            return new TechnicalResult(
                    50.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    1.0
            );
        }

        double ema20 =
                calculateEMA(close, 20);

        double ema50 =
                calculateEMA(close, 50);

        double ema200 =
                calculateEMA(close, 200);

        double rsi =
                calculateRSI(close, 14);

        double macd =
                calculateMACD(close);

        double atr =
                calculateATR(
                        close,
                        high,
                        low,
                        14
                );

        double[] bollinger =
                calculateBollinger(close, 20);

        double momentum =
                calculateMomentum(close, 10);

        double volumeRatio =
                calculateVolumeRatio(
                        volume,
                        20
                );

        return new TechnicalResult(
                rsi,
                ema20,
                ema50,
                ema200,
                macd,
                atr,
                bollinger[0],
                bollinger[1],
                momentum,
                volumeRatio
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
                prices.isEmpty()) {

            return 0.0;
        }

        if (prices.size() < period) {

            return prices.get(
                    prices.size() - 1
            );
        }

        double sum = 0.0;

        for (int i = 0;
             i < period;
             i++) {

            sum += prices.get(i);
        }

        double ema =
                sum / period;

        double multiplier =
                2.0 /
                (period + 1.0);

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

        int start =
                prices.size() - period;

        double gain = 0.0;
        double loss = 0.0;

        for (int i = start;
             i < prices.size();
             i++) {

            double change =
                    prices.get(i)
                            - prices.get(i - 1);

            if (change > 0) {

                gain += change;

            } else {

                loss += Math.abs(change);
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

        return 100.0 -
                (
                        100.0 /
                                (1.0 + rs)
                );
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

        double total = 0.0;

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
                    currentHigh - currentLow;

            double range2 =
                    Math.abs(
                            currentHigh
                                    - previousClose
                    );

            double range3 =
                    Math.abs(
                            currentLow
                                    - previousClose
                    );

            double trueRange =
                    Math.max(
                            range1,
                            Math.max(
                                    range2,
                                    range3
                            )
                    );

            total += trueRange;
        }

        return total / period;
    }

    // =========================================================
    // BOLLINGER BANDS
    // =========================================================

    private static double[] calculateBollinger(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() < period) {

            return new double[]{
                    0.0,
                    0.0
            };
        }

        int start =
                prices.size() - period;

        double sum = 0.0;

        for (int i = start;
             i < prices.size();
             i++) {

            sum += prices.get(i);
        }

        double mean =
                sum / period;

        double variance = 0.0;

        for (int i = start;
             i < prices.size();
             i++) {

            double difference =
                    prices.get(i) - mean;

            variance +=
                    difference * difference;
        }

        variance =
                variance / period;

        double standardDeviation =
                Math.sqrt(variance);

        double upper =
                mean
                        + (2.0 * standardDeviation);

        double lower =
                mean
                        - (2.0 * standardDeviation);

        return new double[]{
                upper,
                lower
        };
    }

    // =========================================================
    // MOMENTUM
    // =========================================================

    private static double calculateMomentum(
            List<Double> prices,
            int period
    ) {

        if (prices == null ||
                prices.size() <= period) {

            return 0.0;
        }

        int currentIndex =
                prices.size() - 1;

        int oldIndex =
                prices.size() - 1 - period;

        double oldPrice =
                prices.get(oldIndex);

        if (oldPrice == 0.0) {
            return 0.0;
        }

        return (
                (
                        prices.get(currentIndex)
                                - oldPrice
                )
                        / oldPrice
        ) * 100.0;
    }

    // =========================================================
    // VOLUME RATIO
    // =========================================================

    private static double calculateVolumeRatio(
            List<Double> volume,
            int period
    ) {

        if (volume == null ||
                volume.size() < 2) {

            return 1.0;
        }

        int size =
                volume.size();

        int start =
                Math.max(
                        0,
                        size - period
                );

        double sum = 0.0;
        int count = 0;

        for (int i = start;
             i < size;
             i++) {

            sum += volume.get(i);
            count++;
        }

        if (count == 0 ||
                sum <= 0) {

            return 1.0;
        }

        double average =
                sum / count;

        double current =
                volume.get(size - 1);

        return current / average;
    }
    }
