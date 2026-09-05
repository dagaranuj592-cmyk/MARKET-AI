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

        public int sampleSize;

        public String confidence;

        public String direction;


        public ProbabilityResult(
                double buyProbability,
                double sellProbability,
                double neutralProbability,
                int sampleSize,
                String confidence,
                String direction
        ) {

            this.buyProbability = buyProbability;
            this.sellProbability = sellProbability;
            this.neutralProbability = neutralProbability;
            this.sampleSize = sampleSize;
            this.confidence = confidence;
            this.direction = direction;
        }
    }


    // =========================================================
    // SETTINGS
    // =========================================================

    /*
     * Number of future candles used to evaluate
     * a historical signal.
     */
    private static final int FORWARD_DAYS = 5;


    /*
     * Price movement required to classify
     * a historical outcome.
     *
     * >= +1%  -> BUY outcome
     * <= -1%  -> SELL outcome
     * between -> NEUTRAL outcome
     */
    private static final double MOVE_THRESHOLD = 0.01;


    /*
     * Minimum technical history required.
     */
    private static final int MIN_HISTORY = 40;


    /*
     * Percentage of history reserved for
     * validation / out-of-sample checking.
     *
     * The engine does NOT use the validation
     * period for its main probability estimate.
     */
    private static final double TRAINING_RATIO = 0.80;


    /*
     * Maximum acceptable score difference
     * for a historical match.
     */
    private static final int STRICT_SCORE_DISTANCE = 1;


    /*
     * Wider matching if strict matching
     * does not produce enough examples.
     */
    private static final int WIDE_SCORE_DISTANCE = 2;


    /*
     * Laplace smoothing prevents extreme
     * 0% / 100% probabilities from tiny samples.
     */
    private static final double SMOOTHING = 2.0;


    // =========================================================
    // MAIN ENGINE
    // =========================================================

    public static ProbabilityResult calculate(
            List<Double> prices
    ) {

        if (
                prices == null ||
                prices.size() < MIN_HISTORY
        ) {

            return new ProbabilityResult(
                    33.33,
                    33.33,
                    33.34,
                    0,
                    "LOW",
                    "INSUFFICIENT DATA"
            );
        }


        // -----------------------------------------------------
        // CURRENT MARKET STATE
        // -----------------------------------------------------

        TechnicalAnalyzer.TechnicalResult currentTechnical =
                TechnicalAnalyzer.analyze(
                        prices,
                        createProxyHighs(prices),
                        createProxyLows(prices)
                );


        String currentTrend =
                calculateTrend(prices);


        double currentPrice =
                prices.get(
                        prices.size() - 1
                );


        int currentScore =
                calculateScore(
                        currentPrice,
                        currentTrend,
                        currentTechnical
                );


        // -----------------------------------------------------
        // TRAINING / VALIDATION SPLIT
        // -----------------------------------------------------

        int totalUsable =
                prices.size()
                        -
                FORWARD_DAYS;


        int trainingEnd =
                (int)
                Math.floor(
                        totalUsable
                                *
                        TRAINING_RATIO
                );


        /*
         * Safety boundaries.
         */
        trainingEnd =
                Math.max(
                        30,
                        trainingEnd
                );


        trainingEnd =
                Math.min(
                        trainingEnd,
                        totalUsable - 1
                );


        // -----------------------------------------------------
        // STRICT HISTORICAL MATCHING
        // -----------------------------------------------------

        HistoricalStats strictStats =
                collectHistoricalStats(
                        prices,
                        currentScore,
                        currentTrend,
                        0,
                        trainingEnd,
                        STRICT_SCORE_DISTANCE
                );


        HistoricalStats stats =
                strictStats;


        // -----------------------------------------------------
        // WIDE MATCHING FALLBACK
        // -----------------------------------------------------

        if (
                stats.sampleSize < 10
        ) {

            stats =
                    collectHistoricalStats(
                            prices,
                            currentScore,
                            currentTrend,
                            0,
                            trainingEnd,
                            WIDE_SCORE_DISTANCE
                    );
        }


        // -----------------------------------------------------
        // GLOBAL FALLBACK
        // -----------------------------------------------------

        if (
                stats.sampleSize < 10
        ) {

            stats =
                    collectGlobalStats(
                            prices,
                            0,
                            trainingEnd
                    );
        }


        // -----------------------------------------------------
        // NO DATA
        // -----------------------------------------------------

        if (
                stats.sampleSize == 0
        ) {

            return new ProbabilityResult(
                    33.33,
                    33.33,
                    33.34,
                    0,
                    "LOW",
                    "NO HISTORICAL DATA"
            );
        }


        // -----------------------------------------------------
        // CALIBRATED PROBABILITY
        // -----------------------------------------------------

        double buyProbability =
                smoothedProbability(
                        stats.buyCount,
                        stats.sampleSize
                );


        double sellProbability =
                smoothedProbability(
                        stats.sellCount,
                        stats.sampleSize
                );


        double neutralProbability =
                smoothedProbability(
                        stats.neutralCount,
                        stats.sampleSize
                );


        /*
         * Normalise all three values so their total
         * is exactly 100%.
         */
        double total =
                buyProbability
                        +
                sellProbability
                        +
                neutralProbability;


        if (total > 0.0) {

            buyProbability =
                    buyProbability
                            *
                    100.0
                            /
                    total;


            sellProbability =
                    sellProbability
                            *
                    100.0
                            /
                    total;


            neutralProbability =
                    neutralProbability
                            *
                    100.0
                            /
                    total;
        }


        buyProbability =
                round2(
                        buyProbability
                );


        sellProbability =
                round2(
                        sellProbability
                );


        neutralProbability =
                round2(
                        neutralProbability
                );


        /*
         * Fix possible rounding difference so
         * displayed probabilities total 100%.
         */
        double displayedTotal =
                buyProbability
                        +
                sellProbability
                        +
                neutralProbability;


        double difference =
                round2(
                        100.0 -
                        displayedTotal
                );


        neutralProbability =
                round2(
                        neutralProbability
                                +
                        difference
                );


        // -----------------------------------------------------
        // DIRECTION
        // -----------------------------------------------------

        String direction;


        if (
                buyProbability >= sellProbability
                        &&
                buyProbability >= neutralProbability
        ) {

            direction = "BUY";

        } else if (
                sellProbability >= buyProbability
                        &&
                sellProbability >= neutralProbability
        ) {

            direction = "SELL";

        } else {

            direction = "NEUTRAL";
        }


        // -----------------------------------------------------
        // OUT-OF-SAMPLE VALIDATION
        // -----------------------------------------------------

        ValidationStats validation =
                validateOutOfSample(
                        prices,
                        currentScore,
                        currentTrend,
                        trainingEnd,
                        totalUsable,
                        STRICT_SCORE_DISTANCE
                );


        /*
         * If strict validation has very few examples,
         * use wider validation.
         */
        if (
                validation.sampleSize < 5
        ) {

            validation =
                    validateOutOfSample(
                            prices,
                            currentScore,
                            currentTrend,
                            trainingEnd,
                            totalUsable,
                            WIDE_SCORE_DISTANCE
                    );
        }


        // -----------------------------------------------------
        // CONFIDENCE
        // -----------------------------------------------------

        String confidence =
                calculateConfidence(
                        stats.sampleSize,
                        buyProbability,
                        sellProbability,
                        neutralProbability,
                        validation
                );


        return new ProbabilityResult(
                buyProbability,
                sellProbability,
                neutralProbability,
                stats.sampleSize,
                confidence,
                direction
        );
    }


    // =========================================================
    // HISTORICAL STATS
    // =========================================================

    private static HistoricalStats
    collectHistoricalStats(
            List<Double> prices,
            int currentScore,
            String currentTrend,
            int start,
            int end,
            int scoreDistance
    ) {

        HistoricalStats stats =
                new HistoricalStats();


        for (
                int i = start + 30;
                i <= end;
                i++
        ) {

            if (
                    i + FORWARD_DAYS
                            >=
                    prices.size()
            ) {

                break;
            }


            List<Double> historicalPrices =
                    new ArrayList<>(
                            prices.subList(
                                    0,
                                    i + 1
                            )
                    );


            TechnicalAnalyzer.TechnicalResult technical =
                    TechnicalAnalyzer.analyze(
                            historicalPrices,
                            createProxyHighs(
                                    historicalPrices
                            ),
                            createProxyLows(
                                    historicalPrices
                            )
                    );


            String trend =
                    calculateTrend(
                            historicalPrices
                    );


            int score =
                    calculateScore(
                            historicalPrices.get(
                                    historicalPrices.size() - 1
                            ),
                            trend,
                            technical
                    );


            boolean scoreMatch =
                    Math.abs(
                            score -
                            currentScore
                    )
                    <=
                    scoreDistance;


            boolean trendMatch =
                    trend.equals(
                            currentTrend
                    );


            if (
                    !scoreMatch ||
                    !trendMatch
            ) {

                continue;
            }


            double historicalPrice =
                    historicalPrices.get(
                            historicalPrices.size() - 1
                    );


            double futurePrice =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double returnPercent =
                    (
                            futurePrice -
                            historicalPrice
                    )
                    /
                    historicalPrice;


            stats.sampleSize++;


            if (
                    returnPercent
                            >=
                    MOVE_THRESHOLD
            ) {

                stats.buyCount++;

            } else if (
                    returnPercent
                            <=
                    -MOVE_THRESHOLD
            ) {

                stats.sellCount++;

            } else {

                stats.neutralCount++;
            }
        }


        return stats;
    }


    // =========================================================
    // GLOBAL STATS
    // =========================================================

    private static HistoricalStats
    collectGlobalStats(
            List<Double> prices,
            int start,
            int end
    ) {

        HistoricalStats stats =
                new HistoricalStats();


        for (
                int i = start + 30;
                i <= end;
                i++
        ) {

            if (
                    i + FORWARD_DAYS
                            >=
                    prices.size()
            ) {

                break;
            }


            double current =
                    prices.get(i);


            double future =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double returnPercent =
                    (
                            future -
                            current
                    )
                    /
                    current;


            stats.sampleSize++;


            if (
                    returnPercent
                            >=
                    MOVE_THRESHOLD
            ) {

                stats.buyCount++;

            } else if (
                    returnPercent
                            <=
                    -MOVE_THRESHOLD
            ) {

                stats.sellCount++;

            } else {

                stats.neutralCount++;
            }
        }


        return stats;
    }


    // =========================================================
    // OUT OF SAMPLE VALIDATION
    // =========================================================

    private static ValidationStats
    validateOutOfSample(
            List<Double> prices,
            int currentScore,
            String currentTrend,
            int start,
            int end,
            int scoreDistance
    ) {

        ValidationStats result =
                new ValidationStats();


        for (
                int i = start;
                i <= end;
                i++
        ) {

            if (
                    i < 30 ||
                    i + FORWARD_DAYS
                            >=
                    prices.size()
            ) {

                continue;
            }


            List<Double> historicalPrices =
                    new ArrayList<>(
                            prices.subList(
                                    0,
                                    i + 1
                            )
                    );


            TechnicalAnalyzer.TechnicalResult technical =
                    TechnicalAnalyzer.analyze(
                            historicalPrices,
                            createProxyHighs(
                                    historicalPrices
                            ),
                            createProxyLows(
                                    historicalPrices
                            )
                    );


            String trend =
                    calculateTrend(
                            historicalPrices
                    );


            int score =
                    calculateScore(
                            historicalPrices.get(
                                    historicalPrices.size() - 1
                            ),
                            trend,
                            technical
                    );


            boolean scoreMatch =
                    Math.abs(
                            score -
                            currentScore
                    )
                    <=
                    scoreDistance;


            boolean trendMatch =
                    trend.equals(
                            currentTrend
                    );


            if (
                    !scoreMatch ||
                    !trendMatch
            ) {

                continue;
            }


            double current =
                    prices.get(i);


            double future =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double movement =
                    (
                            future -
                            current
                    )
                    /
                    current;


            result.sampleSize++;


            /*
             * Validation asks:
             *
             * Did the historical direction
             * agree with the current direction?
             */
            if (
                    currentScore > 0
            ) {

                if (
                        movement
                                >=
                        MOVE_THRESHOLD
                ) {

                    result.correct++;
                }

            } else if (
                    currentScore < 0
            ) {

                if (
                        movement
                                <=
                        -MOVE_THRESHOLD
                ) {

                    result.correct++;
                }

            } else {

                if (
                        Math.abs(movement)
                                <
                        MOVE_THRESHOLD
                ) {

                    result.correct++;
                }
            }
        }


        return result;
    }


    // =========================================================
    // PROBABILITY SMOOTHING
    // =========================================================

    private static double
    smoothedProbability(
            int count,
            int total
    ) {

        /*
         * Three possible classes:
         *
         * BUY
         * SELL
         * NEUTRAL
         *
         * Laplace smoothing adds the same
         * small prior to every class.
         */

        return
                (
                        count +
                        SMOOTHING
                )
                /
                (
                        total +
                        (
                                SMOOTHING * 3.0
                        )
                );
    }


    // =========================================================
    // CONFIDENCE
    // =========================================================

    private static String
    calculateConfidence(
            int sampleSize,
            double buy,
            double sell,
            double neutral,
            ValidationStats validation
    ) {

        double highest =
                Math.max(
                        buy,
                        Math.max(
                                sell,
                                neutral
                        )
                );


        double secondHighest;


        if (
                highest == buy
        ) {

            secondHighest =
                    Math.max(
                            sell,
                            neutral
                    );

        } else if (
                highest == sell
        ) {

            secondHighest =
                    Math.max(
                            buy,
                            neutral
                    );

        } else {

            secondHighest =
                    Math.max(
                            buy,
                            sell
                    );
        }


        double separation =
                highest -
                secondHighest;


        /*
         * Validation quality.
         */
        double validationAccuracy = 0.0;


        if (
                validation.sampleSize > 0
        ) {

            validationAccuracy =
                    (
                            validation.correct
                                    *
                            100.0
                    )
                    /
                    validation.sampleSize;
        }


        /*
         * HIGH:
         *
         * Large historical sample
         * Strong probability separation
         * Reasonable validation sample
         * Validation accuracy above 55%
         */
        if (
                sampleSize >= 50
                        &&
                separation >= 20
                        &&
                validation.sampleSize >= 10
                        &&
                validationAccuracy >= 55
        ) {

            return "HIGH";
        }


        /*
         * MODERATE
         */
        if (
                sampleSize >= 25
                        &&
                separation >= 10
                        &&
                validation.sampleSize >= 5
                        &&
                validationAccuracy >= 50
        ) {

            return "MODERATE";
        }


        return "LOW";
    }


    // =========================================================
    // TECHNICAL SCORE
    // =========================================================

    private static int calculateScore(
            double currentPrice,
            String trend,
            TechnicalAnalyzer.TechnicalResult technical
    ) {

        int score = 0;


        // Trend

        if (
                trend.equals("UP")
        ) {

            score++;

        } else if (
                trend.equals("DOWN")
        ) {

            score--;
        }


        // EMA20

        if (
                currentPrice >
                technical.ema20
        ) {

            score++;

        } else if (
                currentPrice <
                technical.ema20
        ) {

            score--;
        }


        // RSI

        if (
                technical.rsi >= 55 &&
                technical.rsi <= 70
        ) {

            score++;

        } else if (
                technical.rsi >= 30 &&
                technical.rsi <= 45
        ) {

            score--;
        }


        // MACD

        if (
                technical.macd > 0
        ) {

            score++;

        } else if (
                technical.macd < 0
        ) {

            score--;
        }


        if (score > 4) {

            score = 4;
        }


        if (score < -4) {

            score = -4;
        }


        return score;
    }


    // =========================================================
    // TREND
    // =========================================================

    private static String calculateTrend(
            List<Double> prices
    ) {

        if (
                prices == null ||
                prices.size() < 20
        ) {

            return "UNKNOWN";
        }


        double recent =
                averageLast(
                        prices,
                        5
                );


        double previous =
                averageRange(
                        prices,
                        prices.size() - 10,
                        prices.size() - 5
                );


        if (
                recent >
                previous * 1.002
        ) {

            return "UP";

        } else if (
                recent <
                previous * 0.998
        ) {

            return "DOWN";

        } else {

            return "SIDEWAYS";
        }
    }


    // =========================================================
    // PROXY HIGH
    // =========================================================

    private static List<Double>
    createProxyHighs(
            List<Double> prices
    ) {

        List<Double> result =
                new ArrayList<>();


        for (
                Double price : prices
        ) {

            result.add(
                    price * 1.01
            );
        }


        return result;
    }


    // =========================================================
    // PROXY LOW
    // =========================================================

    private static List<Double>
    createProxyLows(
            List<Double> prices
    ) {

        List<Double> result =
                new ArrayList<>();


        for (
                Double price : prices
        ) {

            result.add(
                    price * 0.99
            );
        }


        return result;
    }


    // =========================================================
    // AVERAGES
    // =========================================================

    private static double averageLast(
            List<Double> values,
            int count
    ) {

        if (
                values == null ||
                values.isEmpty()
        ) {

            return 0.0;
        }


        count =
                Math.min(
                        count,
                        values.size()
                );


        double sum = 0.0;


        for (
                int i =
                        values.size() - count;
                i < values.size();
                i++
        ) {

            sum +=
                    values.get(i);
        }


        return sum / count;
    }


    private static double averageRange(
            List<Double> values,
            int start,
            int end
    ) {

        if (
                values == null ||
                values.isEmpty()
        ) {

            return 0.0;
        }


        start =
                Math.max(
                        0,
                        start
                );


        end =
                Math.min(
                        values.size(),
                        end
                );


        if (
                start >= end
        ) {

            return values.get(
                    values.size() - 1
            );
        }


        double sum = 0.0;


        for (
                int i = start;
                i < end;
                i++
        ) {

            sum +=
                    values.get(i);
        }


        return sum /
                (end - start);
    }


    // =========================================================
    // ROUND
    // =========================================================

    private static double round2(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }


    // =========================================================
    // HISTORICAL STATS CLASS
    // =========================================================

    private static class HistoricalStats {

        int buyCount = 0;

        int sellCount = 0;

        int neutralCount = 0;

        int sampleSize = 0;
    }


    // =========================================================
    // VALIDATION STATS CLASS
    // =========================================================

    private static class ValidationStats {

        int correct = 0;

        int sampleSize = 0;
    }
            }
