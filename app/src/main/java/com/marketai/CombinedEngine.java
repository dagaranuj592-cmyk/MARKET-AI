package com.marketai;

import java.util.List;

public class CombinedEngine {

    // =========================================================
    // BASE WEIGHTS
    // =========================================================

    private static final double TECHNICAL_WEIGHT = 0.25;
    private static final double PROBABILITY_WEIGHT = 0.35;
    private static final double LEARNING_WEIGHT = 0.40;


    // =========================================================
    // AGREEMENT BOOST
    // =========================================================

    private static final double AGREEMENT_BOOST = 8.0;

    // If the strongest direction is only slightly ahead,
    // keep the result NEUTRAL instead of forcing a direction.
    private static final double MIN_DIRECTION_MARGIN = 4.0;


    // =========================================================
    // RESULT
    // =========================================================

    public static class CombinedResult {

        public double buyProbability;
        public double sellProbability;
        public double neutralProbability;

        public String direction;

        public double confidence;

        public double technicalBuy;
        public double technicalSell;
        public double technicalNeutral;

        public double probabilityBuy;
        public double probabilitySell;
        public double probabilityNeutral;

        public double learningBuy;
        public double learningSell;
        public double learningNeutral;

        public int learningMatchedSamples;

        public double learningAccuracy;

        // New V2 information
        public int agreementCount;

        public String agreement;

        public String signalStrength;


        public CombinedResult(
                double buyProbability,
                double sellProbability,
                double neutralProbability,
                String direction,
                double confidence,
                double technicalBuy,
                double technicalSell,
                double technicalNeutral,
                double probabilityBuy,
                double probabilitySell,
                double probabilityNeutral,
                double learningBuy,
                double learningSell,
                double learningNeutral,
                int learningMatchedSamples,
                double learningAccuracy,
                int agreementCount,
                String agreement,
                String signalStrength
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

            this.technicalBuy =
                    technicalBuy;

            this.technicalSell =
                    technicalSell;

            this.technicalNeutral =
                    technicalNeutral;

            this.probabilityBuy =
                    probabilityBuy;

            this.probabilitySell =
                    probabilitySell;

            this.probabilityNeutral =
                    probabilityNeutral;

            this.learningBuy =
                    learningBuy;

            this.learningSell =
                    learningSell;

            this.learningNeutral =
                    learningNeutral;

            this.learningMatchedSamples =
                    learningMatchedSamples;

            this.learningAccuracy =
                    learningAccuracy;

            this.agreementCount =
                    agreementCount;

            this.agreement =
                    agreement;

            this.signalStrength =
                    signalStrength;
        }
    }


    // =========================================================
    // MAIN ANALYSIS
    // =========================================================

    public static CombinedResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        // =====================================================
        // TECHNICAL ENGINE
        // =====================================================

        TechnicalAnalyzer.TechnicalResult technical =
                TechnicalAnalyzer.analyze(
                        close,
                        high,
                        low,
                        volume
                );


        // =====================================================
        // PROBABILITY ENGINE
        // =====================================================

        ProbabilityEngine.ProbabilityResult probability =
                ProbabilityEngine.calculate(
                        close,
                        high,
                        low,
                        volume
                );


        // =====================================================
        // LEARNING ENGINE
        // =====================================================

        LearningEngine.LearningResult learning =
                LearningEngine.learn(
                        close,
                        high,
                        low,
                        volume
                );


        // =====================================================
        // TECHNICAL SCORE
        // =====================================================

        double technicalBuy = 0.0;
        double technicalSell = 0.0;
        double technicalNeutral = 0.0;


        // RSI
        if (
                technical.rsi >= 55.0
                        &&
                technical.rsi <= 70.0
        ) {

            technicalBuy += 1.0;

        } else if (
                technical.rsi >= 30.0
                        &&
                technical.rsi <= 45.0
        ) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // EMA 20 / 50
        if (
                technical.ema20 > 0
                        &&
                technical.ema50 > 0
        ) {

            if (
                    technical.ema20 > technical.ema50
            ) {

                technicalBuy += 1.0;

            } else if (
                    technical.ema20 < technical.ema50
            ) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }
        }


        // EMA 50 / 200
        if (
                technical.ema50 > 0
                        &&
                technical.ema200 > 0
        ) {

            if (
                    technical.ema50 > technical.ema200
            ) {

                technicalBuy += 1.0;

            } else if (
                    technical.ema50 < technical.ema200
            ) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }
        }


        // MACD
        if (
                technical.macd > 0
        ) {

            technicalBuy += 1.0;

        } else if (
                technical.macd < 0
        ) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // Momentum
        if (
                technical.momentum >= 1.0
        ) {

            technicalBuy += 1.0;

        } else if (
                technical.momentum <= -1.0
        ) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // Volume
        if (
                technical.volumeRatio >= 1.20
        ) {

            if (
                    technical.momentum > 0
            ) {

                technicalBuy += 1.0;

            } else if (
                    technical.momentum < 0
            ) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }

        } else {

            technicalNeutral += 1.0;
        }


        // =====================================================
        // NORMALIZE TECHNICAL
        // =====================================================

        double technicalTotal =
                technicalBuy
                        +
                technicalSell
                        +
                technicalNeutral;


        if (
                technicalTotal <= 0.0
        ) {

            technicalBuy = 33.33;
            technicalSell = 33.33;
            technicalNeutral = 33.34;

        } else {

            technicalBuy =
                    technicalBuy
                            /
                    technicalTotal
                            *
                    100.0;

            technicalSell =
                    technicalSell
                            /
                    technicalTotal
                            *
                    100.0;

            technicalNeutral =
                    technicalNeutral
                            /
                    technicalTotal
                            *
                    100.0;
        }


        // =====================================================
        // PROBABILITY VALUES
        // =====================================================

        double probabilityBuy =
                safeProbability(
                        probability.buyProbability
                );

        double probabilitySell =
                safeProbability(
                        probability.sellProbability
                );

        double probabilityNeutral =
                safeProbability(
                        probability.neutralProbability
                );


        // =====================================================
        // LEARNING VALUES
        // =====================================================

        double learningBuy =
                safeProbability(
                        learning.buyProbability
                );

        double learningSell =
                safeProbability(
                        learning.sellProbability
                );

        double learningNeutral =
                safeProbability(
                        learning.neutralProbability
                );


        // =====================================================
        // BASE COMBINATION
        // =====================================================

        double combinedBuy =
                technicalBuy
                        *
                        TECHNICAL_WEIGHT
                        +
                        probabilityBuy
                        *
                        PROBABILITY_WEIGHT
                        +
                        learningBuy
                        *
                        LEARNING_WEIGHT;


        double combinedSell =
                technicalSell
                        *
                        TECHNICAL_WEIGHT
                        +
                        probabilitySell
                        *
                        PROBABILITY_WEIGHT
                        +
                        learningSell
                        *
                        LEARNING_WEIGHT;


        double combinedNeutral =
                technicalNeutral
                        *
                        TECHNICAL_WEIGHT
                        +
                        probabilityNeutral
                        *
                        PROBABILITY_WEIGHT
                        +
                        learningNeutral
                        *
                        LEARNING_WEIGHT;


        // =====================================================
        // FIND INDIVIDUAL ENGINE DIRECTIONS
        // =====================================================

        String technicalDirection =
                getTechnicalDirection(
                        technicalBuy,
                        technicalSell,
                        technicalNeutral
                );


        String probabilityDirection =
                normalizeDirection(
                        probability.direction
                );


        String learningDirection =
                normalizeDirection(
                        learning.direction
                );


        // =====================================================
        // COUNT AGREEMENT
        // =====================================================

        int buyAgreement = 0;
        int sellAgreement = 0;
        int neutralAgreement = 0;


        if (
                technicalDirection.equals("BUY")
        ) {

            buyAgreement++;

        } else if (
                technicalDirection.equals("SELL")
        ) {

            sellAgreement++;

        } else {

            neutralAgreement++;
        }


        if (
                probabilityDirection.equals("BUY")
        ) {

            buyAgreement++;

        } else if (
                probabilityDirection.equals("SELL")
        ) {

            sellAgreement++;

        } else {

            neutralAgreement++;
        }


        if (
                learningDirection.equals("BUY")
        ) {

            buyAgreement++;

        } else if (
                learningDirection.equals("SELL")
        ) {

            sellAgreement++;

        } else {

            neutralAgreement++;
        }


        // =====================================================
        // AGREEMENT BOOST
        // =====================================================

        if (
                buyAgreement >= 2
        ) {

            combinedBuy +=
                    AGREEMENT_BOOST;

        }


        if (
                sellAgreement >= 2
        ) {

            combinedSell +=
                    AGREEMENT_BOOST;

        }


        if (
                neutralAgreement >= 2
        ) {

            combinedNeutral +=
                    AGREEMENT_BOOST;
        }


        // =====================================================
        // NORMALIZE FINAL PROBABILITIES
        // =====================================================

        double total =
                combinedBuy
                        +
                combinedSell
                        +
                combinedNeutral;


        if (
                total <= 0.0
        ) {

            combinedBuy = 33.33;
            combinedSell = 33.33;
            combinedNeutral = 33.34;

        } else {

            combinedBuy =
                    combinedBuy
                            /
                    total
                            *
                    100.0;

            combinedSell =
                    combinedSell
                            /
                    total
                            *
                    100.0;

            combinedNeutral =
                    combinedNeutral
                            /
                    total
                            *
                    100.0;
        }


        // =====================================================
        // FIND STRONGEST RESULT
        // =====================================================

        double highest =
                Math.max(
                        combinedBuy,
                        Math.max(
                                combinedSell,
                                combinedNeutral
                        )
                );


        double secondHighest =
                getSecondHighest(
                        combinedBuy,
                        combinedSell,
                        combinedNeutral
                );


        double margin =
                highest - secondHighest;


        // =====================================================
        // FINAL DIRECTION
        // =====================================================

        String direction;


        if (
                margin < MIN_DIRECTION_MARGIN
        ) {

            direction =
                    "NEUTRAL";

        } else if (
                highest == combinedBuy
        ) {

            direction =
                    "BUY";

        } else if (
                highest == combinedSell
        ) {

            direction =
                    "SELL";

        } else {

            direction =
                    "NEUTRAL";
        }


        // =====================================================
        // AGREEMENT COUNT
        // =====================================================

        int agreementCount;

        String agreement;


        if (
                buyAgreement >= 2
        ) {

            agreementCount =
                    buyAgreement;

            agreement =
                    "BUY "
                            +
                    buyAgreement
                            +
                    "/3";

        } else if (
                sellAgreement >= 2
        ) {

            agreementCount =
                    sellAgreement;

            agreement =
                    "SELL "
                            +
                    sellAgreement
                            +
                    "/3";

        } else {

            agreementCount =
                    Math.max(
                            buyAgreement,
                            Math.max(
                                    sellAgreement,
                                    neutralAgreement
                            )
                    );

            agreement =
                    "MIXED";
        }


        // =====================================================
        // SIGNAL STRENGTH
        // =====================================================

        String signalStrength;


        if (
                direction.equals("NEUTRAL")
        ) {

            signalStrength =
                    "WEAK / NO CLEAR EDGE";

        } else if (
                agreementCount >= 3
                        &&
                margin >= 15.0
        ) {

            signalStrength =
                    "STRONG";

        } else if (
                agreementCount >= 2
                        &&
                margin >= 8.0
        ) {

            signalStrength =
                    "MODERATE";

        } else {

            signalStrength =
                    "WEAK";
        }


        // =====================================================
        // CONFIDENCE
        // =====================================================

        double confidence =
                highest;


        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new CombinedResult(

                round(combinedBuy),

                round(combinedSell),

                round(combinedNeutral),

                direction,

                round(confidence),

                round(technicalBuy),

                round(technicalSell),

                round(technicalNeutral),

                round(probabilityBuy),

                round(probabilitySell),

                round(probabilityNeutral),

                round(learningBuy),

                round(learningSell),

                round(learningNeutral),

                learning.matchedSamples,

                round(learning.trainingAccuracy),

                agreementCount,

                agreement,

                signalStrength
        );
    }


    // =========================================================
    // TECHNICAL DIRECTION
    // =========================================================

    private static String getTechnicalDirection(
            double buy,
            double sell,
            double neutral
    ) {

        if (
                buy > sell
                        &&
                buy > neutral
        ) {

            return "BUY";
        }


        if (
                sell > buy
                        &&
                sell > neutral
        ) {

            return "SELL";
        }


        return "NEUTRAL";
    }


    // =========================================================
    // NORMALIZE DIRECTION
    // =========================================================

    private static String normalizeDirection(
            String direction
    ) {

        if (
                direction == null
        ) {

            return "NEUTRAL";
        }


        String value =
                direction
                        .trim()
                        .toUpperCase();


        if (
                value.contains("BUY")
        ) {

            return "BUY";
        }


        if (
                value.contains("SELL")
        ) {

            return "SELL";
        }


        return "NEUTRAL";
    }


    // =========================================================
    // SECOND HIGHEST
    // =========================================================

    private static double getSecondHighest(
            double a,
            double b,
            double c
    ) {

        double highest =
                Math.max(
                        a,
                        Math.max(
                                b,
                                c
                        )
                );


        double second =
                Double.NEGATIVE_INFINITY;


        if (
                a < highest
        ) {

            second =
                    Math.max(
                            second,
                            a
                    );
        }


        if (
                b < highest
        ) {

            second =
                    Math.max(
                            second,
                            b
                    );
        }


        if (
                c < highest
        ) {

            second =
                    Math.max(
                            second,
                            c
                    );
        }


        // Handles equal highest values
        if (
                second ==
                        Double.NEGATIVE_INFINITY
        ) {

            second =
                    highest;
        }


        return second;
    }


    // =========================================================
    // SAFE PROBABILITY
    // =========================================================

    private static double safeProbability(
            double value
    ) {

        if (
                Double.isNaN(value)
                        ||
                Double.isInfinite(value)
        ) {

            return 33.33;
        }


        if (
                value < 0.0
        ) {

            return 0.0;
        }


        if (
                value > 100.0
        ) {

            return 100.0;
        }


        return value;
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
