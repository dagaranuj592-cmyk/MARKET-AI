package com.marketai;

import java.util.ArrayList;
import java.util.List;

public class ProbabilityEngine {

    // =========================================================
    // RESULT
    // =========================================================

    public static class ProbabilityResult {

        public double buyProbability;
        public double sellProbability;
        public double neutralProbability;

        public String direction;
        public String confidence;

        public int samples;

        public double validationAccuracy;
        public int validationSamples;

        public ProbabilityResult(
                double buyProbability,
                double sellProbability,
                double neutralProbability,
                String direction,
                String confidence,
                int samples,
                double validationAccuracy,
                int validationSamples
        ) {

            this.buyProbability = buyProbability;
            this.sellProbability = sellProbability;
            this.neutralProbability = neutralProbability;

            this.direction = direction;
            this.confidence = confidence;

            this.samples = samples;

            this.validationAccuracy = validationAccuracy;
            this.validationSamples = validationSamples;
        }
    }

    // =========================================================
    // 3-ARGUMENT VERSION
    // =========================================================
    // This keeps compatibility with an older MainActivity.

    public static ProbabilityResult calculate(
            List<Double> prices,
            List<Double> highs,
            List<Double> lows
    ) {

        return calculate(
                prices,
                highs,
                lows,
                null
        );
    }

    // =========================================================
    // 4-ARGUMENT VERSION
    // =========================================================

    public static ProbabilityResult calculate(
            List<Double> prices,
            List<Double> highs,
            List<Double> lows,
            List<Double> volumes
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

        if (size < 60) {

            return defaultResult();
        }

        // -----------------------------------------------------
        // Prepare common OHLC data
        // -----------------------------------------------------

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

        List<Double> v = null;

        if (volumes != null &&
                volumes.size() >= size) {

            v =
                    new ArrayList<>(
                            volumes.subList(
                                    0,
                                    size
                            )
                    );
        }

        // -----------------------------------------------------
        // 80% training / 20% validation
        // -----------------------------------------------------

        int trainingEnd =
                (int) (
                        size * 0.80
                );

        if (trainingEnd < 45) {

            return defaultResult();
        }

        // -----------------------------------------------------
        // Current market state
        // -----------------------------------------------------

        TechnicalAnalyzer.TechnicalResult current =
                TechnicalAnalyzer.analyze(
                        p,
                        h,
                        l,
                        v
                );

        double currentPrice =
                p.get(
                        p.size() - 1
                );

        // Laplace smoothing
        double buyScore = 2.0;
        double sellScore = 2.0;
        double neutralScore = 2.0;

        int matchedSamples = 0;

        // =====================================================
        // HISTORICAL PATTERN MATCHING
        // =====================================================

        for (int i = 40;
             i < trainingEnd - 5;
             i++) {

            List<Double> hp =
                    new ArrayList<>(
                            p.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> hh =
                    new ArrayList<>(
                            h.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> hl =
                    new ArrayList<>(
                            l.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> hv = null;

            if (v != null) {

                hv =
                        new ArrayList<>(
                                v.subList(
                                        0,
                                        i + 1
                                )
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

            // -------------------------------------------------
            // Historical match
            // -------------------------------------------------

            if (distance <= 2.0) {

                double futurePrice =
                        p.get(
                                i + 5
                        );

                double movement =
                        (
                                futurePrice
                                        - historicalPrice
                        )
                                / historicalPrice
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

        // =====================================================
        // WIDER MATCH IF TOO FEW SAMPLES
        // =====================================================

        if (matchedSamples < 10) {

            buyScore = 2.0;
            sellScore = 2.0;
            neutralScore = 2.0;

            matchedSamples = 0;

            for (int i = 40;
                 i < trainingEnd - 5;
                 i++) {

                List<Double> hp =
                        new ArrayList<>(
                                p.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> hh =
                        new ArrayList<>(
                                h.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> hl =
                        new ArrayList<>(
                                l.subList(
                                        0,
                                        i + 1
                                )
                        );

                List<Double> hv = null;

                if (v != null) {

                    hv =
                            new ArrayList<>(
                                    v.subList(
                                            0,
                                            i + 1
                                    )
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

                if (distance <= 3.0) {

                    double futurePrice =
                            p.get(
                                    i + 5
                            );

                    double movement =
                            (
                                    futurePrice
                                            - historicalPrice
                            )
                                    / historicalPrice
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

        // =====================================================
        // WALK-FORWARD VALIDATION
        // =====================================================

        int validationStart =
                trainingEnd;

        int validationEnd =
                size - 5;

        int validationSamples = 0;
        int validationCorrect = 0;

        for (int i = validationStart;
             i < validationEnd;
             i++) {

            List<Double> vp =
                    new ArrayList<>(
                            p.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> vh =
                    new ArrayList<>(
                            h.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> vl =
                    new ArrayList<>(
                            l.subList(
                                    0,
                                    i + 1
                            )
                    );

            List<Double> vv = null;

            if (v != null) {

                vv =
                        new ArrayList<>(
                                v.subList(
                                        0,
                                        i + 1
                                )
                        );
            }

            TechnicalAnalyzer.TechnicalResult validation =
                    TechnicalAnalyzer.analyze(
                            vp,
                            vh,
                            vl,
                            vv
                    );

            double validationPrice =
                    vp.get(
                            vp.size() - 1
                    );

            String predicted =
                    technicalDirection(
                            validation,
                            validationPrice
                    );

            double oldPrice =
                    p.get(i);

            double futurePrice =
                    p.get(
                            i + 5
                    );

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

        // =====================================================
        // VALIDATION ACCURACY
        // =====================================================

        double validationAccuracy = 0.0;

        if (validationSamples > 0) {

            validationAccuracy =
                    validationCorrect
                            / (double)
                            validationSamples
                            * 100.0;
        }

        // =====================================================
        // PROBABILITY
        // =====================================================

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
                        validationAccuracy,
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
                matchedSamples,
                round(validationAccuracy),
                validationSamples
        );
    }

    // =========================================================
    // DISTANCE CALCULATION
    // =========================================================

    private static double calculateDistance(
            TechnicalAnalyzer.TechnicalResult current,
            TechnicalAnalyzer.TechnicalResult historical,
            double currentPrice,
            double historicalPrice
    ) {

        double distance = 0.0;

        // RSI
        if (Math.abs(
                current.rsi
                        - historical.rsi
        ) > 15.0) {

            distance += 1.0;
        }

        // EMA 50
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

        // MACD
        boolean currentMacdPositive =
                current.macd > 0;

        boolean historicalMacdPositive =
                historical.macd > 0;

        if (currentMacdPositive !=
                historicalMacdPositive) {

            distance += 1.0;
        }

        // Momentum
        boolean currentMomentumPositive =
                current.momentum > 0;

        boolean historicalMomentumPositive =
                historical.momentum > 0;

        if (currentMomentumPositive !=
                historicalMomentumPositive) {

            distance += 1.0;
        }

        // Volume regime
        boolean currentHighVolume =
                current.volumeRatio > 1.20;

        boolean historicalHighVolume =
                historical.volumeRatio > 1.20;

        if (currentHighVolume !=
                historicalHighVolume) {

            distance += 1.0;
        }

        return distance;
    }

    // =========================================================
    // TECHNICAL DIRECTION
    // =========================================================

    private static String technicalDirection(
            TechnicalAnalyzer.TechnicalResult result,
            double price
    ) {

        int bullish = 0;
        int bearish = 0;

        // RSI
        if (result.rsi >= 52) {

            bullish++;

        } else if (result.rsi <= 48) {

            bearish++;
        }

        // MACD
        if (result.macd > 0) {

            bullish++;

        } else {

            bearish++;
        }

        // Momentum
        if (result.momentum > 0) {

            bullish++;

        } else if (result.momentum < 0) {

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
    // CONFIDENCE
    // =========================================================

    private static String calculateConfidence(
            int samples,
            double validationAccuracy,
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

        if (samples >= 20 &&
                validationAccuracy >= 55.0 &&
                edge >= 8.0) {

            return "HIGH";
        }

        if (samples >= 10 &&
                validationAccuracy >= 45.0 &&
                edge >= 5.0) {

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
                0,
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
