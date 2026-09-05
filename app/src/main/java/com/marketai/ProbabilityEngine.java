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

            this.buyProbability =
                    buyProbability;

            this.sellProbability =
                    sellProbability;

            this.neutralProbability =
                    neutralProbability;

            this.sampleSize =
                    sampleSize;

            this.confidence =
                    confidence;

            this.direction =
                    direction;
        }
    }


    // =========================================================
    // HISTORICAL SETTINGS
    // =========================================================

    /*
     * We evaluate what happened approximately
     * 5 candles after a historical signal.
     *
     * Example:
     *
     * Signal at Day 100
     * Compare price with Day 105
     */

    private static final int
            FORWARD_DAYS = 5;


    /*
     * Minimum movement required to classify
     * a historical signal as directional.
     *
     * +1%  = BUY
     * -1%  = SELL
     * between = NEUTRAL
     */

    private static final double
            MOVE_THRESHOLD = 0.01;


    /*
     * Minimum historical examples before
     * we consider the result reasonably useful.
     */

    private static final int
            MIN_SAMPLE_SIZE = 10;


    // =========================================================
    // MAIN ENGINE
    // =========================================================

    public static ProbabilityResult calculate(
            List<Double> prices
    ) {

        if (
                prices == null ||
                prices.size() < 50
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


        /*
         * Get the current technical state.
         */

        TechnicalAnalyzer.TechnicalResult current =
                TechnicalAnalyzer.analyze(
                        prices,
                        createProxyHighs(prices),
                        createProxyLows(prices)
                );


        String currentTrend =
                calculateTrend(
                        prices
                );


        int currentScore =
                calculateScore(
                        prices.get(
                                prices.size() - 1
                        ),
                        currentTrend,
                        current
                );


        /*
         * Historical counters.
         */

        int buyCount = 0;

        int sellCount = 0;

        int neutralCount = 0;

        int sampleCount = 0;


        /*
         * Walk through historical data.
         *
         * IMPORTANT:
         * We only use data BEFORE the future outcome.
         *
         * This avoids using future prices to
         * calculate the historical signal itself.
         */

        int lastIndex =
                prices.size()
                        - FORWARD_DAYS
                        - 1;


        for (
                int i = 30;
                i <= lastIndex;
                i++
        ) {

            List<Double> historicalPrices =
                    new ArrayList<>(
                            prices.subList(
                                    0,
                                    i + 1
                            )
                    );


            TechnicalAnalyzer.TechnicalResult historicalTechnical =
                    TechnicalAnalyzer.analyze(
                            historicalPrices,
                            createProxyHighs(
                                    historicalPrices
                            ),
                            createProxyLows(
                                    historicalPrices
                            )
                    );


            String historicalTrend =
                    calculateTrend(
                            historicalPrices
                    );


            double historicalPrice =
                    historicalPrices.get(
                            historicalPrices.size() - 1
                    );


            int historicalScore =
                    calculateScore(
                            historicalPrice,
                            historicalTrend,
                            historicalTechnical
                    );


            /*
             * Only compare historical situations
             * that are similar to the current situation.
             *
             * Score difference <= 1
             * and same broad trend.
             */

            boolean scoreMatch =
                    Math.abs(
                            historicalScore
                                    -
                            currentScore
                    ) <= 1;


            boolean trendMatch =
                    historicalTrend.equals(
                            currentTrend
                    );


            if (
                    !scoreMatch ||
                    !trendMatch
            ) {

                continue;
            }


            /*
             * Future price.
             */

            double futurePrice =
                    prices.get(
                            i + FORWARD_DAYS
                    );


            double returnPercent =
                    (
                            futurePrice
                                    -
                            historicalPrice
                    )
                    /
                    historicalPrice;


            sampleCount++;


            if (
                    returnPercent
                            >=
                    MOVE_THRESHOLD
            ) {

                buyCount++;

            } else if (
                    returnPercent
                            <=
                    -MOVE_THRESHOLD
            ) {

                sellCount++;

            } else {

                neutralCount++;
            }
        }


        /*
         * If there are too few matching examples,
         * widen the search to all historical
         * technical states.
         */

        if (
                sampleCount < MIN_SAMPLE_SIZE
        ) {

            buyCount = 0;
            sellCount = 0;
            neutralCount = 0;
            sampleCount = 0;


            for (
                    int i = 30;
                    i <= lastIndex;
                    i++
            ) {

                List<Double> historicalPrices =
                        new ArrayList<>(
                                prices.subList(
                                        0,
                                        i + 1
                                )
                        );


                TechnicalAnalyzer.TechnicalResult historicalTechnical =
                        TechnicalAnalyzer.analyze(
                                historicalPrices,
                                createProxyHighs(
                                        historicalPrices
                                ),
                                createProxyLows(
                                        historicalPrices
                                )
                        );


                String historicalTrend =
                        calculateTrend(
                                historicalPrices
                        );


                int historicalScore =
                        calculateScore(
                                historicalPrices.get(
                                        historicalPrices.size() - 1
                                ),
                                historicalTrend,
                                historicalTechnical
                        );


                /*
                 * Use slightly broader matching.
                 */

                boolean scoreMatch =
                        Math.abs(
                                historicalScore
                                        -
                                currentScore
                        ) <= 2;


                if (!scoreMatch) {

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
                                futurePrice
                                        -
                                historicalPrice
                        )
                        /
                        historicalPrice;


                sampleCount++;


                if (
                        returnPercent
                                >=
                        MOVE_THRESHOLD
                ) {

                    buyCount++;

                } else if (
                        returnPercent
                                <=
                        -MOVE_THRESHOLD
                ) {

                    sellCount++;

                } else {

                    neutralCount++;
                }
            }
        }


        /*
         * Final fallback.
         */

        if (sampleCount == 0) {

            return new ProbabilityResult(
                    33.33,
                    33.33,
                    33.34,
                    0,
                    "LOW",
                    "NO HISTORICAL MATCH"
            );
        }


        /*
         * Convert historical counts
         * into probabilities.
         */

        double buyProbability =
                (
                        buyCount * 100.0
                )
                /
                sampleCount;


        double sellProbability =
                (
                        sellCount * 100.0
                )
                /
                sampleCount;


        double neutralProbability =
                (
                        neutralCount * 100.0
                )
                /
                sampleCount;


        /*
         * Round to two decimals.
         */

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
         * Determine strongest direction.
         */

        String direction;


        if (
                buyProbability >=
                        sellProbability
                        &&
                buyProbability >=
                        neutralProbability
        ) {

            direction =
                    "BUY";

        } else if (
                sellProbability >=
                        buyProbability
                        &&
                sellProbability >=
                        neutralProbability
        ) {

            direction =
                    "SELL";

        } else {

            direction =
                    "NEUTRAL";
        }


        /*
         * Confidence is based primarily
         * on historical sample size and
         * separation between probabilities.
         */

        double highest =
                Math.max(
                        buyProbability,
                        Math.max(
                                sellProbability,
                                neutralProbability
                        )
                );


        double secondHighest;


        if (
                highest == buyProbability
        ) {

            secondHighest =
                    Math.max(
                            sellProbability,
                            neutralProbability
                    );

        } else if (
                highest == sellProbability
        ) {

            secondHighest =
                    Math.max(
                            buyProbability,
                            neutralProbability
                    );

        } else {

            secondHighest =
                    Math.max(
                            buyProbability,
                            sellProbability
                    );
        }


        double separation =
                highest -
                secondHighest;


        String confidence;


        if (
                sampleCount >= 50 &&
                separation >= 20
        ) {

            confidence =
                    "HIGH";

        } else if (
                sampleCount >= 25 &&
                separation >= 10
        ) {

            confidence =
                    "MODERATE";

        } else {

            confidence =
                    "LOW";
        }


        return new ProbabilityResult(
                buyProbability,
                sellProbability,
                neutralProbability,
                sampleCount,
                confidence,
                direction
        );
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


        /*
         * Trend
         */

        if (
                trend.equals("UP")
        ) {

            score++;

        } else if (
                trend.equals("DOWN")
        ) {

            score--;
        }


        /*
         * Price vs EMA20
         */

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


        /*
         * RSI
         */

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


        /*
         * MACD
         */

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
    // PROXY HIGH / LOW
    // =========================================================

    private static List<Double> createProxyHighs(
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


    private static List<Double> createProxyLows(
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

        count =
                Math.min(
                        count,
                        values.size()
                );


        if (count <= 0) {

            return 0.0;
        }


        double sum = 0.0;


        for (
                int i = values.size() - count;
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
    // ROUNDING
    // =========================================================

    private static double round2(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
  }
