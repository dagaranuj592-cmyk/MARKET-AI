package com.marketai;

import java.util.List;

public class MoveDetector {

    public static class MoveResult {

        public double moveRisk;
        public String riskLevel;

        public String direction;
        public double upsideProbability;
        public double downsideProbability;

        public double volatilityScore;
        public double volumeScore;
        public double compressionScore;
        public double momentumScore;

        public boolean volatilityExpansion;
        public boolean volumeExpansion;
        public boolean volatilityCompression;

        public String evidence;

        public MoveResult(
                double moveRisk,
                String riskLevel,
                String direction,
                double upsideProbability,
                double downsideProbability,
                double volatilityScore,
                double volumeScore,
                double compressionScore,
                double momentumScore,
                boolean volatilityExpansion,
                boolean volumeExpansion,
                boolean volatilityCompression,
                String evidence
        ) {
            this.moveRisk = moveRisk;
            this.riskLevel = riskLevel;
            this.direction = direction;
            this.upsideProbability = upsideProbability;
            this.downsideProbability = downsideProbability;
            this.volatilityScore = volatilityScore;
            this.volumeScore = volumeScore;
            this.compressionScore = compressionScore;
            this.momentumScore = momentumScore;
            this.volatilityExpansion = volatilityExpansion;
            this.volumeExpansion = volumeExpansion;
            this.volatilityCompression = volatilityCompression;
            this.evidence = evidence;
        }
    }

    /*
     * candleMinutes:
     *
     * 5  = 5 minute candles
     * 15 = 15 minute candles
     * 30 = 30 minute candles
     * 60 = 1 hour candles
     *
     * For a 1-2 hour move detector,
     * 5m or 15m data is preferred.
     */
    public static MoveResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume,
            int candleMinutes
    ) {

        if (
                close == null ||
                high == null ||
                low == null ||
                volume == null
        ) {
            return emptyResult();
        }

        int size =
                Math.min(
                        close.size(),
                        Math.min(
                                high.size(),
                                Math.min(
                                        low.size(),
                                        volume.size()
                                )
                        )
                );

        if (size < 40) {
            return emptyResult();
        }

        double currentPrice =
                close.get(size - 1);

        if (currentPrice <= 0.0) {
            return emptyResult();
        }

        /*
         * -------------------------------------------------
         * 1. RECENT VOLATILITY
         * -------------------------------------------------
         */

        double recentRange =
                averageRange(
                        high,
                        low,
                        size,
                        8
                );

        double previousRange =
                averageRange(
                        high,
                        low,
                        size - 8,
                        8
                );

        double volatilityRatio;

        if (previousRange <= 0.0) {
            volatilityRatio = 1.0;
        } else {
            volatilityRatio =
                    recentRange /
                    previousRange;
        }

        double volatilityScore =
                clamp(
                        (volatilityRatio - 0.80)
                                /
                        0.80
                                *
                        100.0,
                        0.0,
                        100.0
                );

        boolean volatilityExpansion =
                volatilityRatio >= 1.35;


        /*
         * -------------------------------------------------
         * 2. VOLUME EXPANSION
         * -------------------------------------------------
         */

        double recentVolume =
                averageVolume(
                        volume,
                        size,
                        5
                );

        double baselineVolume =
                averageVolume(
                        volume,
                        size - 5,
                        20
                );

        double volumeRatio;

        if (baselineVolume <= 0.0) {
            volumeRatio = 1.0;
        } else {
            volumeRatio =
                    recentVolume /
                    baselineVolume;
        }

        double volumeScore =
                clamp(
                        (volumeRatio - 0.80)
                                /
                        1.20
                                *
                        100.0,
                        0.0,
                        100.0
                );

        boolean volumeExpansion =
                volumeRatio >= 1.35;


        /*
         * -------------------------------------------------
         * 3. VOLATILITY COMPRESSION
         *
         * Large moves often follow periods where
         * short-term ranges become unusually small.
         * This is an early-warning component,
         * NOT a guarantee of a breakout.
         * -------------------------------------------------
         */

        double shortRange =
                averageRange(
                        high,
                        low,
                        size,
                        8
                );

        double longRange =
                averageRange(
                        high,
                        low,
                        size - 8,
                        24
                );

        double compressionRatio;

        if (longRange <= 0.0) {
            compressionRatio = 1.0;
        } else {
            compressionRatio =
                    shortRange /
                    longRange;
        }

        boolean volatilityCompression =
                compressionRatio <= 0.70;

        double compressionScore;

        if (compressionRatio <= 0.70) {
            compressionScore = 100.0;
        } else if (compressionRatio <= 0.85) {
            compressionScore = 75.0;
        } else if (compressionRatio <= 1.00) {
            compressionScore = 45.0;
        } else {
            compressionScore = 20.0;
        }


        /*
         * -------------------------------------------------
         * 4. SHORT-TERM MOMENTUM
         * -------------------------------------------------
         */

        int momentumLookback =
                Math.max(
                        3,
                        Math.min(
                                12,
                                60 / Math.max(
                                        1,
                                        candleMinutes
                                )
                        )
                );

        if (size <= momentumLookback) {
            return emptyResult();
        }

        double oldPrice =
                close.get(
                        size -
                        1 -
                        momentumLookback
                );

        double momentumPercent;

        if (oldPrice <= 0.0) {
            momentumPercent = 0.0;
        } else {
            momentumPercent =
                    (
                            (
                                    currentPrice -
                                    oldPrice
                            )
                                    /
                            oldPrice
                    )
                            *
                    100.0;
        }

        double momentumScore =
                clamp(
                        50.0
                                +
                        momentumPercent *
                        25.0,
                        0.0,
                        100.0
                );


        /*
         * -------------------------------------------------
         * 5. PRICE RANGE / BREAKOUT PRESSURE
         * -------------------------------------------------
         */

        double highest =
                highest(
                        high,
                        size,
                        20
                );

        double lowest =
                lowest(
                        low,
                        size,
                        20
                );

        double range;

        if (highest <= lowest) {
            range = 0.0;
        } else {
            range =
                    highest -
                    lowest;
        }

        double positionInRange = 0.5;

        if (range > 0.0) {
            positionInRange =
                    (
                            currentPrice -
                            lowest
                    )
                            /
                    range;
        }

        /*
         * -------------------------------------------------
         * 6. DIRECTION ESTIMATE
         * -------------------------------------------------
         *
         * Direction is deliberately separated from
         * move-risk detection.
         */

        double upsideScore = 50.0;
        double downsideScore = 50.0;

        if (momentumPercent > 0.0) {
            upsideScore +=
                    Math.min(
                            30.0,
                            momentumPercent * 20.0
                    );
        } else if (momentumPercent < 0.0) {
            downsideScore +=
                    Math.min(
                            30.0,
                            Math.abs(momentumPercent) * 20.0
                    );
        }

        if (positionInRange >= 0.75) {
            upsideScore += 15.0;
        }

        if (positionInRange <= 0.25) {
            downsideScore += 15.0;
        }

        if (volumeRatio >= 1.35) {

            if (momentumPercent > 0.0) {
                upsideScore += 10.0;
            } else if (momentumPercent < 0.0) {
                downsideScore += 10.0;
            }
        }

        upsideScore =
                clamp(
                        upsideScore,
                        0.0,
                        100.0
                );

        downsideScore =
                clamp(
                        downsideScore,
                        0.0,
                        100.0
                );

        double directionTotal =
                upsideScore +
                downsideScore;

        double upsideProbability =
                directionTotal <= 0.0
                        ? 50.0
                        :
                        upsideScore /
                        directionTotal *
                        100.0;

        double downsideProbability =
                directionTotal <= 0.0
                        ? 50.0
                        :
                        downsideScore /
                        directionTotal *
                        100.0;

        String direction;

        if (
                Math.abs(
                        upsideProbability -
                        downsideProbability
                ) < 10.0
        ) {
            direction = "UNCERTAIN";
        } else if (
                upsideProbability >
                downsideProbability
        ) {
            direction = "UP";
        } else {
            direction = "DOWN";
        }


        /*
         * -------------------------------------------------
         * 7. COMBINE MOVE-RISK FACTORS
         * -------------------------------------------------
         */

        double moveRisk =
                (
                        volatilityScore * 0.30
                                +
                        volumeScore * 0.25
                                +
                        compressionScore * 0.25
                                +
                        Math.abs(
                                momentumScore -
                                50.0
                        ) * 2.0 * 0.20
                );

        /*
         * If volatility is already expanding,
         * increase the move-risk score.
         */

        if (volatilityExpansion) {
            moveRisk += 10.0;
        }

        if (volumeExpansion) {
            moveRisk += 8.0;
        }

        if (volatilityCompression) {
            moveRisk += 8.0;
        }

        moveRisk =
                clamp(
                        moveRisk,
                        0.0,
                        100.0
                );


        /*
         * -------------------------------------------------
         * 8. RISK LEVEL
         * -------------------------------------------------
         */

        String riskLevel;

        if (moveRisk >= 75.0) {
            riskLevel = "HIGH";
        } else if (moveRisk >= 55.0) {
            riskLevel = "ELEVATED";
        } else if (moveRisk >= 35.0) {
            riskLevel = "NORMAL";
        } else {
            riskLevel = "LOW";
        }


        /*
         * -------------------------------------------------
         * 9. EVIDENCE
         * -------------------------------------------------
         */

        StringBuilder evidence =
                new StringBuilder();

        if (volatilityExpansion) {
            evidence.append(
                    "Volatility expanding. "
            );
        }

        if (volumeExpansion) {
            evidence.append(
                    "Volume above baseline. "
            );
        }

        if (volatilityCompression) {
            evidence.append(
                    "Recent volatility compressed. "
            );
        }

        if (Math.abs(momentumPercent) >= 0.30) {
            evidence.append(
                    "Short-term momentum active. "
            );
        }

        if (evidence.length() == 0) {
            evidence.append(
                    "No strong abnormal-move conditions detected."
            );
        }

        /*
         * Safety against invalid probabilities.
         */

        upsideProbability =
                round(
                        upsideProbability
                );

        downsideProbability =
                round(
                        downsideProbability
                );

        moveRisk =
                round(
                        moveRisk
                );

        return new MoveResult(
                moveRisk,
                riskLevel,
                direction,
                upsideProbability,
                downsideProbability,
                round(volatilityScore),
                round(volumeScore),
                round(compressionScore),
                round(momentumScore),
                volatilityExpansion,
                volumeExpansion,
                volatilityCompression,
                evidence.toString()
        );
    }


    private static double averageRange(
            List<Double> high,
            List<Double> low,
            int endExclusive,
            int period
    ) {

        if (
                high == null ||
                low == null ||
                endExclusive <= 0
        ) {
            return 0.0;
        }

        int end =
                Math.min(
                        endExclusive,
                        Math.min(
                                high.size(),
                                low.size()
                        )
                );

        int start =
                Math.max(
                        0,
                        end - period
                );

        if (end <= start) {
            return 0.0;
        }

        double total = 0.0;

        int count = 0;

        for (
                int i = start;
                i < end;
                i++
        ) {

            double h =
                    high.get(i);

            double l =
                    low.get(i);

            if (h >= l) {
                total +=
                        h - l;

                count++;
            }
        }

        return count == 0
                ? 0.0
                :
                total / count;
    }


    private static double averageVolume(
            List<Double> volume,
            int endExclusive,
            int period
    ) {

        if (
                volume == null ||
                endExclusive <= 0
        ) {
            return 0.0;
        }

        int end =
                Math.min(
                        endExclusive,
                        volume.size()
                );

        int start =
                Math.max(
                        0,
                        end - period
                );

        if (end <= start) {
            return 0.0;
        }

        double total = 0.0;

        int count = 0;

        for (
                int i = start;
                i < end;
                i++
        ) {

            double value =
                    volume.get(i);

            if (value >= 0.0) {
                total += value;
                count++;
            }
        }

        return count == 0
                ? 0.0
                :
                total / count;
    }


    private static double highest(
            List<Double> values,
            int endExclusive,
            int period
    ) {

        int end =
                Math.min(
                        endExclusive,
                        values.size()
                );

        int start =
                Math.max(
                        0,
                        end - period
                );

        double result =
                Double.NEGATIVE_INFINITY;

        for (
                int i = start;
                i < end;
                i++
        ) {
            result =
                    Math.max(
                            result,
                            values.get(i)
                    );
        }

        return result ==
                Double.NEGATIVE_INFINITY
                ? 0.0
                :
                result;
    }


    private static double lowest(
            List<Double> values,
            int endExclusive,
            int period
    ) {

        int end =
                Math.min(
                        endExclusive,
                        values.size()
                );

        int start =
                Math.max(
                        0,
                        end - period
                );

        double result =
                Double.POSITIVE_INFINITY;

        for (
                int i = start;
                i < end;
                i++
        ) {
            result =
                    Math.min(
                            result,
                            values.get(i)
                    );
        }

        return result ==
                Double.POSITIVE_INFINITY
                ? 0.0
                :
                result;
    }


    private static double clamp(
            double value,
            double min,
            double max
    ) {

        if (
                Double.isNaN(value) ||
                Double.isInfinite(value)
        ) {
            return min;
        }

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }


    private static double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }


    private static MoveResult emptyResult() {

        return new MoveResult(
                0.0,
                "LOW",
                "UNCERTAIN",
                50.0,
                50.0,
                0.0,
                0.0,
                0.0,
                50.0,
                false,
                false,
                false,
                "Insufficient intraday market data."
        );
    }
                      }
