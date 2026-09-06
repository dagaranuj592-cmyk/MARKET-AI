package com.marketai;

import java.util.List;

public class CalibrationEngine {

    private static final int MIN_HISTORY = 220;
    private static final int FORWARD_DAYS = 5;

    // A signal is considered successful only if its
    // net 5-day return is positive after estimated costs.
    private static final double FEE_PER_SIDE = 0.10;
    private static final double SLIPPAGE_PER_SIDE = 0.05;

    private static final double TOTAL_COST =
            (FEE_PER_SIDE * 2.0)
                    +
            (SLIPPAGE_PER_SIDE * 2.0);


    // =========================================================
    // CALIBRATION BUCKET
    // =========================================================

    public static class Bucket {

        public String range;

        public int signals;
        public int correct;
        public int wrong;

        public double accuracy;
        public double averageReturn;

        public Bucket(
                String range,
                int signals,
                int correct,
                int wrong,
                double accuracy,
                double averageReturn
        ) {

            this.range = range;
            this.signals = signals;
            this.correct = correct;
            this.wrong = wrong;
            this.accuracy = accuracy;
            this.averageReturn = averageReturn;
        }
    }


    // =========================================================
    // RESULT
    // =========================================================

    public static class Result {

        public int totalSignals;
        public int testedSignals;

        public int correctSignals;
        public int wrongSignals;

        public double overallAccuracy;
        public double averageReturn;

        public Bucket bucket0_40;
        public Bucket bucket40_50;
        public Bucket bucket50_60;
        public Bucket bucket60_70;
        public Bucket bucket70_80;
        public Bucket bucket80_100;

        public Result(
                int totalSignals,
                int testedSignals,
                int correctSignals,
                int wrongSignals,
                double overallAccuracy,
                double averageReturn,
                Bucket bucket0_40,
                Bucket bucket40_50,
                Bucket bucket50_60,
                Bucket bucket60_70,
                Bucket bucket70_80,
                Bucket bucket80_100
        ) {

            this.totalSignals = totalSignals;
            this.testedSignals = testedSignals;

            this.correctSignals = correctSignals;
            this.wrongSignals = wrongSignals;

            this.overallAccuracy = overallAccuracy;
            this.averageReturn = averageReturn;

            this.bucket0_40 = bucket0_40;
            this.bucket40_50 = bucket40_50;
            this.bucket50_60 = bucket50_60;
            this.bucket60_70 = bucket60_70;
            this.bucket70_80 = bucket70_80;
            this.bucket80_100 = bucket80_100;
        }
    }


    // =========================================================
    // INTERNAL BUCKET DATA
    // =========================================================

    private static class BucketData {

        int signals;
        int correct;
        int wrong;

        double returnTotal;

        void add(
                boolean correctResult,
                double returnPercent
        ) {

            signals++;

            if (correctResult) {
                correct++;
            } else {
                wrong++;
            }

            returnTotal += returnPercent;
        }

        Bucket build(String range) {

            double accuracy =
                    signals == 0
                            ? 0.0
                            :
                            (
                                    (double) correct
                                            /
                                    signals
                            )
                                    * 100.0;

            double averageReturn =
                    signals == 0
                            ? 0.0
                            :
                            returnTotal /
                            signals;

            return new Bucket(
                    range,
                    signals,
                    correct,
                    wrong,
                    round(accuracy),
                    round(averageReturn)
            );
        }
    }


    // =========================================================
    // MAIN CALIBRATION
    // =========================================================

    public static Result run(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        if (
                close == null
                        ||
                high == null
                        ||
                low == null
                        ||
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


        if (
                size <
                        MIN_HISTORY +
                        FORWARD_DAYS
        ) {

            return emptyResult();
        }


        // =====================================================
        // BUCKETS
        // =====================================================

        BucketData b0_40 =
                new BucketData();

        BucketData b40_50 =
                new BucketData();

        BucketData b50_60 =
                new BucketData();

        BucketData b60_70 =
                new BucketData();

        BucketData b70_80 =
                new BucketData();

        BucketData b80_100 =
                new BucketData();


        int totalSignals = 0;
        int testedSignals = 0;

        int correctSignals = 0;
        int wrongSignals = 0;

        double returnTotal = 0.0;


        // =====================================================
        // WALK FORWARD THROUGH HISTORY
        // =====================================================

        for (
                int i = MIN_HISTORY;
                i + FORWARD_DAYS < size;
                i++
        ) {

            List<Double> historicalClose =
                    close.subList(
                            0,
                            i + 1
                    );

            List<Double> historicalHigh =
                    high.subList(
                            0,
                            i + 1
                    );

            List<Double> historicalLow =
                    low.subList(
                            0,
                            i + 1
                    );

            List<Double> historicalVolume =
                    volume.subList(
                            0,
                            i + 1
                    );


            // =================================================
            // COMBINED ENGINE
            // =================================================

            CombinedEngine.CombinedResult signal =
                    CombinedEngine.analyze(
                            historicalClose,
                            historicalHigh,
                            historicalLow,
                            historicalVolume
                    );


            totalSignals++;


            String direction =
                    signal.direction;


            // =================================================
            // ONLY TEST REAL DIRECTIONS
            // =================================================

            if (
                    "NEUTRAL".equals(
                            direction
                    )
            ) {

                continue;
            }


            double entryPrice =
                    close.get(i);

            double futurePrice =
                    close.get(
                            i + FORWARD_DAYS
                    );


            if (
                    entryPrice <= 0.0
                            ||
                    futurePrice <= 0.0
            ) {

                continue;
            }


            // =================================================
            // RAW RETURN
            // =================================================

            double rawReturn;


            if (
                    "BUY".equals(
                            direction
                    )
            ) {

                rawReturn =
                        (
                                (
                                        futurePrice -
                                        entryPrice
                                )
                                        /
                                entryPrice
                        )
                                *
                        100.0;

            } else {

                rawReturn =
                        (
                                (
                                        entryPrice -
                                        futurePrice
                                )
                                        /
                                entryPrice
                        )
                                *
                        100.0;
            }


            // =================================================
            // NET RETURN
            // =================================================

            double netReturn =
                    rawReturn -
                    TOTAL_COST;


            boolean correct =
                    netReturn > 0.0;


            testedSignals++;


            if (correct) {

                correctSignals++;

            } else {

                wrongSignals++;
            }


            returnTotal +=
                    netReturn;


            // =================================================
            // USE HIGHEST FINAL PROBABILITY
            // =================================================

            double confidence =
                    signal.confidence;


            if (
                    Double.isNaN(
                            confidence
                    )
                            ||
                    Double.isInfinite(
                            confidence
                    )
            ) {

                confidence = 33.33;
            }


            confidence =
                    Math.max(
                            0.0,
                            Math.min(
                                    100.0,
                                    confidence
                            )
                    );


            // =================================================
            // PUT RESULT INTO CALIBRATION BUCKET
            // =================================================

            if (
                    confidence < 40.0
            ) {

                b0_40.add(
                        correct,
                        netReturn
                );

            } else if (
                    confidence < 50.0
            ) {

                b40_50.add(
                        correct,
                        netReturn
                );

            } else if (
                    confidence < 60.0
            ) {

                b50_60.add(
                        correct,
                        netReturn
                );

            } else if (
                    confidence < 70.0
            ) {

                b60_70.add(
                        correct,
                        netReturn
                );

            } else if (
                    confidence < 80.0
            ) {

                b70_80.add(
                        correct,
                        netReturn
                );

            } else {

                b80_100.add(
                        correct,
                        netReturn
                );
            }
        }


        // =====================================================
        // FINAL STATS
        // =====================================================

        double accuracy =
                testedSignals == 0
                        ? 0.0
                        :
                        (
                                (double)
                                        correctSignals
                                        /
                                testedSignals
                        )
                                *
                        100.0;


        double averageReturn =
                testedSignals == 0
                        ? 0.0
                        :
                        returnTotal /
                        testedSignals;


        // =====================================================
        // RETURN
        // =====================================================

        return new Result(

                totalSignals,

                testedSignals,

                correctSignals,

                wrongSignals,

                round(accuracy),

                round(averageReturn),

                b0_40.build(
                        "0-40%"
                ),

                b40_50.build(
                        "40-50%"
                ),

                b50_60.build(
                        "50-60%"
                ),

                b60_70.build(
                        "60-70%"
                ),

                b70_80.build(
                        "70-80%"
                ),

                b80_100.build(
                        "80-100%"
                )
        );
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static Result emptyResult() {

        Bucket empty0 =
                new Bucket(
                        "0-40%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );

        Bucket empty1 =
                new Bucket(
                        "40-50%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );

        Bucket empty2 =
                new Bucket(
                        "50-60%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );

        Bucket empty3 =
                new Bucket(
                        "60-70%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );

        Bucket empty4 =
                new Bucket(
                        "70-80%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );

        Bucket empty5 =
                new Bucket(
                        "80-100%",
                        0,
                        0,
                        0,
                        0.0,
                        0.0
                );


        return new Result(
                0,
                0,
                0,
                0,
                0.0,
                0.0,
                empty0,
                empty1,
                empty2,
                empty3,
                empty4,
                empty5
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
        )
                /
                100.0;
    }
              }
