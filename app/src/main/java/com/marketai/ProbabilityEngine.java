package com.marketai;

import java.util.ArrayList;
import java.util.List;

public class ProbabilityEngine {

    public static class ProbabilityResult {

        public double buyProbability;
        public double sellProbability;
        public double neutralProbability;

        public String direction;
        public String confidence;

        public int samples;

        public ProbabilityResult(
                double buyProbability,
                double sellProbability,
                double neutralProbability,
                String direction,
                String confidence,
                int samples
        ) {

            this.buyProbability =
                    buyProbability;

            this.sellProbability =
                    sellProbability;

            this.neutralProbability =
                    neutralProbability;

            this.direction =
                    direction;

            this.confidence =
                    confidence;

            this.samples =
                    samples;
        }
    }

    // =========================================================
    // MAIN CALCULATION
    // =========================================================

    public static ProbabilityResult calculate(
            List<Double> prices,
            List<Double> highs,
            List<Double> lows
    ) {

        if (prices == null ||
                highs == null ||
                lows == null) {

            return defaultResult();
        }

        int size =
                Math.min(
                        prices.size(),
                        Math.min(
                                highs.size(),
                                lows.size()
                        )
                );

        if (size < 40) {

            return defaultResult();
        }

        /*
         * Use only the common portion of the
         * three OHLC lists.
         */

        List<Double> p =
                new ArrayList<>(
                        prices.subList(
                                0,
                                size
                        )
                );

        List<Double> h =
                new ArrayList<>(
                        highs.subList(
                                0,
                                size
                        )
                );

        List<Double> l =
                new ArrayList<>(
                        lows.subList(
                                0,
                                size
                        )
                );

        /*
         * Keep the most recent 80% as validation.
         * Older data is used for historical matching.
         */

        int trainingEnd =
                (int) (size * 0.80);

        if (trainingEnd < 35) {

            return defaultResult();
        }

        int validationStart =
                trainingEnd;

        int validationEnd =
                size - 5;

        double buyScore = 2.0;
        double sellScore = 2.0;
        double neutralScore = 2.0;

        int matchedSamples = 0;

        /*
         * Evaluate historical situations.
         *
         * Each historical candle is compared with
         * the current market's technical state.
         */

        TechnicalAnalyzer.TechnicalResult currentTechnical =
                TechnicalAnalyzer.analyze(
                        p,
                        h,
                        l
                );

        double currentPrice =
                p.get(
                        p.size() - 1
                );

        for (int i = 30;
             i < trainingEnd - 5;
             i++) {

            List<Double> historicalPrices =
                    new ArrayList<>(
                            p.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> historicalHighs =
                    new ArrayList<>(
                            h.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> historicalLows =
                    new ArrayList<>(
                            l.subList(
                                    0,
                                    i + 1
                            )
                    );

            TechnicalAnalyzer.TechnicalResult historicalTechnical =
                    TechnicalAnalyzer.analyze(
                            historicalPrices,
                            historicalHighs,
                            historicalLows
                    );

            double distance =
                    calculateTechnicalDistance(
                            currentTechnical,
                            historicalTechnical,
                            currentPrice,
                            historicalPrices.get(
                                    historicalPrices.size() - 1
                            )
                    );

            /*
             * Strict historical match.
             */

            if (distance <= 1.0) {

                double oldPrice =
                        historicalPrices.get(
                                historicalPrices.size() - 1
                        );

                double futurePrice =
                        p.get(i + 5);

                double movement =
                        (
                                futurePrice
                                        - oldPrice
                        )
                        / oldPrice
                        * 100.0;

                if (movement >= 1.0) {

                    buyScore += 1.0;

                } else if (movement <= -1.0) {

                    sellScore += 1.0;

                } else {

                    neutralScore += 1.0;
                }

                matchedSamples++;
            }
        }

        /*
         * If strict matching is too small,
         * use a wider historical distance.
         */

        if (matchedSamples < 8) {

            buyScore = 2.0;
            sellScore = 2.0;
            neutralScore = 2.0;

            matchedSamples = 0;

            for (int i = 30;
                 i < trainingEnd - 5;
                 i++) {

                List<Double> historicalPrices =
                        new ArrayList<>(
                                p.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> historicalHighs =
                        new ArrayList<>(
                                h.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> historicalLows =
                        new ArrayList<>(
                                l.subList(
                                        0,
                                        i + 1
                                )
                        );

                TechnicalAnalyzer.TechnicalResult historicalTechnical =
                        TechnicalAnalyzer.analyze(
                                historicalPrices,
                                historicalHighs,
                                historicalLows
                        );

                double distance =
                        calculateTechnicalDistance(
                                currentTechnical,
                                historicalTechnical,
                                currentPrice,
                                historicalPrices.get(
                                        historicalPrices.size() - 1
                                )
                        );

                if (distance <= 2.0) {

                    double oldPrice =
                            historicalPrices.get(
                                    historicalPrices.size() - 1
                            );

                    double futurePrice =
                            p.get(i + 5);

                    double movement =
                            (
                                    futurePrice
                                            - oldPrice
                            )
                            / oldPrice
                            * 100.0;

                    if (movement >= 1.0) {

                        buyScore += 1.0;

                    } else if (movement <= -1.0) {

                        sellScore += 1.0;

                    } else {

                        neutralScore += 1.0;
                    }

                    matchedSamples++;
                }
            }
        }

        /*
         * Validation stage.
         *
         * This gives us a basic out-of-sample
         * quality check.
         */

        int validationSamples = 0;
        int validationCorrect = 0;

        if (validationStart < validationEnd) {

            for (int i = validationStart;
                 i < validationEnd;
                 i++) {

                if (i < 30) {
                    continue;
                }

                List<Double> validationPrices =
                        new ArrayList<>(
                                p.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> validationHighs =
                        new ArrayList<>(
                                h.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> validationLows =
                        new ArrayList<>(
                                l.subList(
                                        0,
                                        i + 1
                                )
                        );

                TechnicalAnalyzer.TechnicalResult vt =
                        TechnicalAnalyzer.analyze(
                                validationPrices,
                                validationHighs,
                                validationLows
                        );

                String predicted =
                        technicalDirection(vt);

                double oldPrice =
                        p.get(i);

                double futurePrice =
                        p.get(i + 5);

                double movement =
                        (
                                futurePrice
                                        - oldPrice
                        )
                        / oldPrice
                        * 100.0;

                String actual;

                if (movement >= 1.0) {

                    actual = "BUY";

                } else if (movement <= -1.0) {

                    actual = "SELL";

                } else {

                    actual = "NEUTRAL";
                }

                if (predicted.equals(actual)) {
                    validationCorrect++;
                }

                validationSamples++;
            }
        }

        double total =
                buyScore
                        + sellScore
                        + neutralScore;

        if (total <= 0) {
            return defaultResult();
        }

        double buy =
                buyScore
                        / total
                        * 100.0;

        double sell =
                sellScore
                        / total
                        * 100.0;

        double neutral =
                neutralScore
                        / total
                        * 100.0;

        String direction;

        if (buy > sell &&
                buy > neutral) {

            direction = "BUY";

        } else if (sell > buy &&
                sell > neutral) {

            direction = "SELL";

        } else {

            direction = "NEUTRAL";
        }

        String confidence =
                calculateConfidence(
                        matchedSamples,
                        validationSamples,
                        validationCorrect,
                        buy,
                        sell,
                        neutral
                );

        return new ProbabilityResult(
                round(buy),
                round(sell),
                round(neutral),
                direction,
                confidence,
                matchedSamples
        );
    }

    // =========================================================
    // TECHNICAL DISTANCE
    // =========================================================

    private static double calculateTechnicalDistance(
            TechnicalAnalyzer.TechnicalResult current,
            TechnicalAnalyzer.TechnicalResult historical,
            double currentPrice,
            double historicalPrice
    ) {

        double distance = 0.0;

        /*
         * RSI difference
         */

        double rsiDifference =
                Math.abs(
                        current.rsi
                                - historical.rsi
                );

        if (rsiDifference > 20) {

            distance += 1.0;
        }

        /*
         * EMA relationship
         */

        boolean currentAboveEMA =
                currentPrice > current.ema20;

        boolean historicalAboveEMA =
                historicalPrice > historical.ema20;

        if (currentAboveEMA !=
                historicalAboveEMA) {

            distance += 1.0;
        }

        /*
         * MACD direction
         */

        boolean currentMACDPositive =
                current.macd > 0;

        boolean historicalMACDPositive =
                historical.macd > 0;

        if (currentMACDPositive !=
                historicalMACDPositive) {

            distance += 1.0;
        }

        /*
         * ATR regime
         */

        if (current.atr > 0 &&
                historical.atr > 0) {

            double atrRatio =
                    current.atr
                            / historical.atr;

            if (atrRatio > 1.75 ||
                    atrRatio < 0.57) {

                distance += 1.0;
            }
        }

        return distance;
    }

    // =========================================================
    // TECHNICAL DIRECTION
    // =========================================================

    private static String technicalDirection(
            TechnicalAnalyzer.TechnicalResult result
    ) {

        int score = 0;

        if (result.rsi > 50) {
            score++;
        }

        if (result.macd > 0) {
            score++;
        }

        if (result.ema20 > 0) {
            score++;
        }

        if (score >= 2) {

            return "BUY";

        } else if (score == 0) {

            return "SELL";

        } else {

            return "NEUTRAL";
        }
    }

    // =========================================================
    // CONFIDENCE
    // =========================================================

    private static String calculateConfidence(
            int matchedSamples,
            int validationSamples,
            int validationCorrect,
            double buy,
            double sell,
            double neutral
    ) {

        double largest =
                Math.max(
                        buy,
                        Math.max(
                                sell,
                                neutral
                        )
                );

        double second;

        if (largest == buy) {

            second =
                    Math.max(
                            sell,
                            neutral
                    );

        } else if (largest == sell) {

            second =
                    Math.max(
                            buy,
                            neutral
                    );

        } else {

            second =
                    Math.max(
                            buy,
                            sell
                    );
        }

        double edge =
                largest - second;

        double validationAccuracy = 0.0;

        if (validationSamples > 0) {

            validationAccuracy =
                    (
                            validationCorrect
                                    / (double)
                                    validationSamples
                    )
                    * 100.0;
        }

        if (matchedSamples >= 20 &&
                edge >= 10 &&
                validationAccuracy >= 55) {

            return "HIGH";
        }

        if (matchedSamples >= 10 &&
                edge >= 5) {

            return "MODERATE";
        }

        return "LOW";
    }

    // =========================================================
    // DEFAULT
    // =========================================================

    private static ProbabilityResult defaultResult() {

        return new ProbabilityResult(
                33.33,
                33.33,
                33.34,
                "NEUTRAL",
                "LOW",
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
