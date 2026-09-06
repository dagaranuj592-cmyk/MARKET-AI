package com.marketai;

import java.util.ArrayList;
import java.util.List;

public class LearningEngine {

    // =========================================================
    // SETTINGS
    // =========================================================

    private static final int MIN_HISTORY = 220;

    // Future outcome horizon
    private static final int FORWARD_DAYS = 5;

    // Minimum movement for directional classification
    private static final double TARGET_PERCENT = 1.0;

    // Similarity threshold
    private static final double MAX_DISTANCE = 4.0;


    // =========================================================
    // RESULT
    // =========================================================

    public static class LearningResult {

        public double buyProbability;
        public double sellProbability;
        public double neutralProbability;

        public String direction;

        public int matchedSamples;

        public int buySamples;
        public int sellSamples;
        public int neutralSamples;

        public double averageFutureReturn;

        public double trainingAccuracy;
        public int trainingSamples;


        public LearningResult(
                double buyProbability,
                double sellProbability,
                double neutralProbability,
                String direction,
                int matchedSamples,
                int buySamples,
                int sellSamples,
                int neutralSamples,
                double averageFutureReturn,
                double trainingAccuracy,
                int trainingSamples
        ) {

            this.buyProbability =
                    buyProbability;

            this.sellProbability =
                    sellProbability;

            this.neutralProbability =
                    neutralProbability;

            this.direction =
                    direction;

            this.matchedSamples =
                    matchedSamples;

            this.buySamples =
                    buySamples;

            this.sellSamples =
                    sellSamples;

            this.neutralSamples =
                    neutralSamples;

            this.averageFutureReturn =
                    averageFutureReturn;

            this.trainingAccuracy =
                    trainingAccuracy;

            this.trainingSamples =
                    trainingSamples;
        }
    }


    // =========================================================
    // MAIN LEARNING FUNCTION
    // =========================================================

    public static LearningResult learn(
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
                MIN_HISTORY +
                        FORWARD_DAYS) {

            return emptyResult();
        }


        // =====================================================
        // 80% TRAINING
        // 20% UNSEEN TEST
        // =====================================================

        int trainingEnd =
                (int)
                        (size * 0.80);


        if (trainingEnd <
                MIN_HISTORY +
                        FORWARD_DAYS) {

            return emptyResult();
        }


        // =====================================================
        // CURRENT FEATURE SET
        // =====================================================

        TechnicalAnalyzer.TechnicalResult current =
                TechnicalAnalyzer.analyze(
                        prices,
                        highs,
                        lows,
                        volumes
                );


        double currentPrice =
                prices.get(
                        size - 1
                );


        // =====================================================
        // FIND SIMILAR HISTORICAL SETUPS
        // =====================================================

        int buySamples = 0;
        int sellSamples = 0;
        int neutralSamples = 0;

        double returnSum = 0.0;

        int matchedSamples = 0;


        for (int i = MIN_HISTORY;
             i < trainingEnd - FORWARD_DAYS;
             i++) {


            List<Double> hp =
                    prices.subList(
                            0,
                            i + 1
                    );

            List<Double> hh =
                    highs.subList(
                            0,
                            i + 1
                    );

            List<Double> hl =
                    lows.subList(
                            0,
                            i + 1
                    );


            List<Double> hv = null;

            if (volumes != null) {

                hv =
                        volumes.subList(
                                0,
                                i + 1
                        );
            }


            TechnicalAnalyzer.TechnicalResult historical =
                    TechnicalAnalyzer.analyze(
                            hp,
                            hh,
                            hl,
                            hv
                    );


            double historicalPrice =
                    hp.get(
                            hp.size() - 1
                    );


            double distance =
                    calculateDistance(
                            current,
                            historical,
                            currentPrice,
                            historicalPrice
                    );


            if (distance >
                    MAX_DISTANCE) {

                continue;
            }


            // =================================================
            // FUTURE OUTCOME
            // =================================================

            double futurePrice =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double futureReturn =
                    (
                            futurePrice
                                    - historicalPrice
                    )
                            / historicalPrice
                            * 100.0;


            returnSum +=
                    futureReturn;


            matchedSamples++;


            if (futureReturn >=
                    TARGET_PERCENT) {

                buySamples++;

            } else if (futureReturn <=
                    -TARGET_PERCENT) {

                sellSamples++;

            } else {

                neutralSamples++;
            }
        }


        // =====================================================
        // NOT ENOUGH MATCHES
        // =====================================================

        if (matchedSamples < 5) {

            return emptyResult();
        }


        // =====================================================
        // PROBABILITIES
        // =====================================================

        double buyProbability =
                buySamples
                        / (double)
                        matchedSamples
                        * 100.0;


        double sellProbability =
                sellSamples
                        / (double)
                        matchedSamples
                        * 100.0;


        double neutralProbability =
                neutralSamples
                        / (double)
                        matchedSamples
                        * 100.0;


        // =====================================================
        // DIRECTION
        // =====================================================

        String direction;


        if (buyProbability >
                sellProbability &&
                buyProbability >
                        neutralProbability) {

            direction = "BUY";

        } else if (
                sellProbability >
                        buyProbability &&
                        sellProbability >
                                neutralProbability
        ) {

            direction = "SELL";

        } else {

            direction = "NEUTRAL";
        }


        // =====================================================
        // TRAINING ACCURACY
        // =====================================================

        int directionalSamples =
                buySamples +
                        sellSamples;


        int dominantCorrect =
                Math.max(
                        buySamples,
                        sellSamples
                );


        double trainingAccuracy =
                0.0;


        if (directionalSamples > 0) {

            trainingAccuracy =
                    dominantCorrect
                            / (double)
                            directionalSamples
                            * 100.0;
        }


        double averageFutureReturn =
                returnSum
                        / matchedSamples;


        // =====================================================
        // UNSEEN TEST
        // =====================================================

        int testSamples = 0;
        int testCorrect = 0;


        for (int i =
                     trainingEnd;
             i <
                     size - FORWARD_DAYS;
             i++) {


            List<Double> testPrices =
                    prices.subList(
                            0,
                            i + 1
                    );

            List<Double> testHighs =
                    highs.subList(
                            0,
                            i + 1
                    );

            List<Double> testLows =
                    lows.subList(
                            0,
                            i + 1
                    );


            List<Double> testVolumes =
                    null;


            if (volumes != null) {

                testVolumes =
                        volumes.subList(
                                0,
                                i + 1
                        );
            }


            TechnicalAnalyzer.TechnicalResult testTechnical =
                    TechnicalAnalyzer.analyze(
                            testPrices,
                            testHighs,
                            testLows,
                            testVolumes
                    );


            double testPrice =
                    testPrices.get(
                            testPrices.size() - 1
                    );


            String predicted =
                    getTechnicalDirection(
                            testTechnical,
                            testPrice
                    );


            double futurePrice =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double movement =
                    (
                            futurePrice
                                    - testPrice
                    )
                            / testPrice
                            * 100.0;


            String actual;


            if (movement >=
                    TARGET_PERCENT) {

                actual = "BUY";

            } else if (
                    movement <=
                            -TARGET_PERCENT
            ) {

                actual = "SELL";

            } else {

                actual = "NEUTRAL";
            }


            if (predicted.equals(
                    actual
            )) {

                testCorrect++;
            }


            testSamples++;
        }


        double unseenAccuracy =
                0.0;


        if (testSamples > 0) {

            unseenAccuracy =
                    testCorrect
                            / (double)
                            testSamples
                            * 100.0;
        }


        return new LearningResult(

                round(buyProbability),

                round(sellProbability),

                round(neutralProbability),

                direction,

                matchedSamples,

                buySamples,

                sellSamples,

                neutralSamples,

                round(averageFutureReturn),

                round(unseenAccuracy),

                testSamples
        );
    }


    // =========================================================
    // FEATURE DISTANCE
    // =========================================================

    private static double calculateDistance(

            TechnicalAnalyzer.TechnicalResult current,

            TechnicalAnalyzer.TechnicalResult historical,

            double currentPrice,

            double historicalPrice
    ) {

        double distance = 0.0;


        // =====================================================
        // RSI
        // =====================================================

        double rsiDifference =
                Math.abs(
                        current.rsi
                                - historical.rsi
                );


        if (rsiDifference > 10.0) {

            distance += 1.0;

        } else if (
                rsiDifference > 5.0
        ) {

            distance += 0.5;
        }


        // =====================================================
        // EMA50 RELATION
        // =====================================================

        boolean currentAbove50 =
                currentPrice >
                        current.ema50;


        boolean historicalAbove50 =
                historicalPrice >
                        historical.ema50;


        if (currentAbove50 !=
                historicalAbove50) {

            distance += 1.0;
        }


        // =====================================================
        // EMA200 RELATION
        // =====================================================

        boolean currentAbove200 =
                currentPrice >
                        current.ema200;


        boolean historicalAbove200 =
                historicalPrice >
                        historical.ema200;


        if (currentAbove200 !=
                historicalAbove200) {

            distance += 1.0;
        }


        // =====================================================
        // MACD
        // =====================================================

        boolean currentMacdPositive =
                current.macd > 0;


        boolean historicalMacdPositive =
                historical.macd > 0;


        if (currentMacdPositive !=
                historicalMacdPositive) {

            distance += 1.0;
        }


        // =====================================================
        // MOMENTUM
        // =====================================================

        boolean currentMomentumPositive =
                current.momentum > 0;


        boolean historicalMomentumPositive =
                historical.momentum > 0;


        if (currentMomentumPositive !=
                historicalMomentumPositive) {

            distance += 1.0;
        }


        // =====================================================
        // VOLUME
        // =====================================================

        boolean currentHighVolume =
                current.volumeRatio >=
                        1.20;


        boolean historicalHighVolume =
                historical.volumeRatio >=
                        1.20;


        if (currentHighVolume !=
                historicalHighVolume) {

            distance += 1.0;
        }


        return distance;
    }


    // =========================================================
    // DIRECTION FOR UNSEEN DATA
    // =========================================================

    private static String getTechnicalDirection(

            TechnicalAnalyzer.TechnicalResult result,

            double price
    ) {

        int bullish = 0;
        int bearish = 0;


        if (result.rsi >= 55) {

            bullish++;

        } else if (
                result.rsi <= 45
        ) {

            bearish++;
        }


        if (result.macd > 0) {

            bullish++;

        } else {

            bearish++;
        }


        if (result.momentum > 0) {

            bullish++;

        } else if (
                result.momentum < 0
        ) {

            bearish++;
        }


        if (result.ema50 > 0) {

            if (price >
                    result.ema50) {

                bullish++;

            } else {

                bearish++;
            }
        }


        if (result.ema200 > 0) {

            if (price >
                    result.ema200) {

                bullish++;

            } else {

                bearish++;
            }
        }


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

    private static LearningResult emptyResult() {

        return new LearningResult(

                33.33,
                33.33,
                33.34,

                "NEUTRAL",

                0,

                0,
                0,
                0,

                0.0,

                0.0,

                0
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
