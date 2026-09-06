package com.marketai;

import java.util.List;

public class CombinedEngine {

    // =========================================================
    // WEIGHTS
    // =========================================================

    private static final double TECHNICAL_WEIGHT = 0.25;
    private static final double PROBABILITY_WEIGHT = 0.35;
    private static final double LEARNING_WEIGHT = 0.40;


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
                double learningAccuracy
        ) {

            this.buyProbability = buyProbability;
            this.sellProbability = sellProbability;
            this.neutralProbability = neutralProbability;

            this.direction = direction;

            this.confidence = confidence;

            this.technicalBuy = technicalBuy;
            this.technicalSell = technicalSell;
            this.technicalNeutral = technicalNeutral;

            this.probabilityBuy = probabilityBuy;
            this.probabilitySell = probabilitySell;
            this.probabilityNeutral = probabilityNeutral;

            this.learningBuy = learningBuy;
            this.learningSell = learningSell;
            this.learningNeutral = learningNeutral;

            this.learningMatchedSamples =
                    learningMatchedSamples;

            this.learningAccuracy =
                    learningAccuracy;
        }
    }


    // =========================================================
    // MAIN COMBINE METHOD
    // =========================================================

    public static CombinedResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        // -----------------------------------------------------
        // TECHNICAL ENGINE
        // -----------------------------------------------------

        TechnicalAnalyzer.TechnicalResult technical =
                TechnicalAnalyzer.analyze(
                        close,
                        high,
                        low,
                        volume
                );


        // -----------------------------------------------------
        // PROBABILITY ENGINE
        // -----------------------------------------------------

        ProbabilityEngine.ProbabilityResult probability =
                ProbabilityEngine.calculate(
                        close,
                        high,
                        low,
                        volume
                );


        // -----------------------------------------------------
        // LEARNING ENGINE
        // -----------------------------------------------------

        LearningEngine.LearningResult learning =
                LearningEngine.learn(
                        close,
                        high,
                        low,
                        volume
                );


        // -----------------------------------------------------
        // TECHNICAL SCORE
        // -----------------------------------------------------

        double technicalBuy = 0.0;
        double technicalSell = 0.0;
        double technicalNeutral = 0.0;


        // RSI
        if (technical.rsi >= 55.0 &&
                technical.rsi <= 70.0) {

            technicalBuy += 1.0;

        } else if (technical.rsi >= 30.0 &&
                technical.rsi <= 45.0) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // EMA 20 / 50
        if (technical.ema20 > 0 &&
                technical.ema50 > 0) {

            if (technical.ema20 > technical.ema50) {

                technicalBuy += 1.0;

            } else if (technical.ema20 < technical.ema50) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }
        }


        // EMA 50 / 200
        if (technical.ema50 > 0 &&
                technical.ema200 > 0) {

            if (technical.ema50 > technical.ema200) {

                technicalBuy += 1.0;

            } else if (technical.ema50 < technical.ema200) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }
        }


        // MACD
        if (technical.macd > 0) {

            technicalBuy += 1.0;

        } else if (technical.macd < 0) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // Momentum
        if (technical.momentum >= 1.0) {

            technicalBuy += 1.0;

        } else if (technical.momentum <= -1.0) {

            technicalSell += 1.0;

        } else {

            technicalNeutral += 1.0;
        }


        // Volume confirmation
        if (technical.volumeRatio >= 1.20) {

            if (technical.momentum > 0) {

                technicalBuy += 1.0;

            } else if (technical.momentum < 0) {

                technicalSell += 1.0;

            } else {

                technicalNeutral += 1.0;
            }

        } else {

            technicalNeutral += 1.0;
        }


        // -----------------------------------------------------
        // NORMALIZE TECHNICAL SCORE
        // -----------------------------------------------------

        double technicalTotal =
                technicalBuy
                        + technicalSell
                        + technicalNeutral;


        if (technicalTotal <= 0.0) {

            technicalBuy = 33.33;
            technicalSell = 33.33;
            technicalNeutral = 33.34;

        } else {

            technicalBuy =
                    (technicalBuy / technicalTotal) * 100.0;

            technicalSell =
                    (technicalSell / technicalTotal) * 100.0;

            technicalNeutral =
                    (technicalNeutral / technicalTotal) * 100.0;
        }


        // -----------------------------------------------------
        // GET PROBABILITY ENGINE VALUES
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // GET LEARNING ENGINE VALUES
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // COMBINE ALL THREE
        // -----------------------------------------------------

        double combinedBuy =
                (technicalBuy * TECHNICAL_WEIGHT)
                        +
                        (probabilityBuy * PROBABILITY_WEIGHT)
                        +
                        (learningBuy * LEARNING_WEIGHT);


        double combinedSell =
                (technicalSell * TECHNICAL_WEIGHT)
                        +
                        (probabilitySell * PROBABILITY_WEIGHT)
                        +
                        (learningSell * LEARNING_WEIGHT);


        double combinedNeutral =
                (technicalNeutral * TECHNICAL_WEIGHT)
                        +
                        (probabilityNeutral * PROBABILITY_WEIGHT)
                        +
                        (learningNeutral * LEARNING_WEIGHT);


        // -----------------------------------------------------
        // NORMALIZE FINAL RESULT
        // -----------------------------------------------------

        double total =
                combinedBuy
                        + combinedSell
                        + combinedNeutral;


        if (total <= 0.0) {

            combinedBuy = 33.33;
            combinedSell = 33.33;
            combinedNeutral = 33.34;

        } else {

            combinedBuy =
                    (combinedBuy / total) * 100.0;

            combinedSell =
                    (combinedSell / total) * 100.0;

            combinedNeutral =
                    (combinedNeutral / total) * 100.0;
        }


        // -----------------------------------------------------
        // FINAL DIRECTION
        // -----------------------------------------------------

        String direction;

        double highest =
                Math.max(
                        combinedBuy,
                        Math.max(
                                combinedSell,
                                combinedNeutral
                        )
                );


        if (highest == combinedBuy) {

            direction = "BUY";

        } else if (highest == combinedSell) {

            direction = "SELL";

        } else {

            direction = "NEUTRAL";
        }


        // -----------------------------------------------------
        // CONFIDENCE
        // -----------------------------------------------------

        double confidence =
                highest;


        // -----------------------------------------------------
        // RETURN
        // -----------------------------------------------------

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

                round(learning.trainingAccuracy)
        );
    }


    // =========================================================
    // SAFE PROBABILITY
    // =========================================================

    private static double safeProbability(
            double value
    ) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return 33.33;
        }


        if (value < 0.0) {

            return 0.0;
        }


        if (value > 100.0) {

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
