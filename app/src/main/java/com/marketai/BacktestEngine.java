package com.marketai;

import java.util.List;

public class CombinedBacktestEngine {

    private static final int MIN_HISTORY = 220;
    private static final int FORWARD_DAYS = 5;

    private static final double FEE_PER_SIDE = 0.10;
    private static final double SLIPPAGE_PER_SIDE = 0.05;


    // =========================================================
    // RESULT
    // =========================================================

    public static class Result {

        public int totalSignals;
        public int correctSignals;
        public int wrongSignals;
        public int neutralSignals;

        public int buySignals;
        public int sellSignals;

        public int buyCorrect;
        public int sellCorrect;

        public double accuracy;
        public double buyAccuracy;
        public double sellAccuracy;

        public double averageReturn;
        public double totalReturn;

        public double profitFactor;
        public double grossProfit;
        public double grossLoss;
        public double maxDrawdown;

        public double buyAverageReturn;
        public double sellAverageReturn;

        public double buyTotalReturn;
        public double sellTotalReturn;

        public double buyHoldReturn;

        public int buyAgreementSignals;
        public int sellAgreementSignals;

        public int strongSignals;
        public int moderateSignals;
        public int weakSignals;


        public Result(
                int totalSignals,
                int correctSignals,
                int wrongSignals,
                int neutralSignals,
                int buySignals,
                int sellSignals,
                int buyCorrect,
                int sellCorrect,
                double accuracy,
                double buyAccuracy,
                double sellAccuracy,
                double averageReturn,
                double totalReturn,
                double profitFactor,
                double grossProfit,
                double grossLoss,
                double maxDrawdown,
                double buyAverageReturn,
                double sellAverageReturn,
                double buyTotalReturn,
                double sellTotalReturn,
                double buyHoldReturn,
                int buyAgreementSignals,
                int sellAgreementSignals,
                int strongSignals,
                int moderateSignals,
                int weakSignals
        ) {

            this.totalSignals = totalSignals;
            this.correctSignals = correctSignals;
            this.wrongSignals = wrongSignals;
            this.neutralSignals = neutralSignals;

            this.buySignals = buySignals;
            this.sellSignals = sellSignals;

            this.buyCorrect = buyCorrect;
            this.sellCorrect = sellCorrect;

            this.accuracy = accuracy;
            this.buyAccuracy = buyAccuracy;
            this.sellAccuracy = sellAccuracy;

            this.averageReturn = averageReturn;
            this.totalReturn = totalReturn;

            this.profitFactor = profitFactor;
            this.grossProfit = grossProfit;
            this.grossLoss = grossLoss;
            this.maxDrawdown = maxDrawdown;

            this.buyAverageReturn = buyAverageReturn;
            this.sellAverageReturn = sellAverageReturn;

            this.buyTotalReturn = buyTotalReturn;
            this.sellTotalReturn = sellTotalReturn;

            this.buyHoldReturn = buyHoldReturn;

            this.buyAgreementSignals = buyAgreementSignals;
            this.sellAgreementSignals = sellAgreementSignals;

            this.strongSignals = strongSignals;
            this.moderateSignals = moderateSignals;
            this.weakSignals = weakSignals;
        }
    }


    // =========================================================
    // MAIN BACKTEST
    // =========================================================

    public static Result run(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        if (
                close == null ||
                high == null ||
                low == null ||
                volume == null
        ) {
            return emptyResult();
        }


        int size = Math.min(
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
                size < MIN_HISTORY + FORWARD_DAYS + 1
        ) {
            return emptyResult();
        }


        int totalSignals = 0;
        int correctSignals = 0;
        int wrongSignals = 0;
        int neutralSignals = 0;

        int buySignals = 0;
        int sellSignals = 0;

        int buyCorrect = 0;
        int sellCorrect = 0;

        int buyAgreementSignals = 0;
        int sellAgreementSignals = 0;

        int strongSignals = 0;
        int moderateSignals = 0;
        int weakSignals = 0;

        double totalReturn = 0.0;
        double grossProfit = 0.0;
        double grossLoss = 0.0;

        double buyTotalReturn = 0.0;
        double sellTotalReturn = 0.0;

        int returnCount = 0;

        double equity = 100.0;
        double peakEquity = 100.0;
        double maxDrawdown = 0.0;


        // =====================================================
        // WALK FORWARD TEST
        // =====================================================

        for (
                int i = MIN_HISTORY;
                i + FORWARD_DAYS < size;
                i++
        ) {

            List<Double> pastClose =
                    close.subList(0, i + 1);

            List<Double> pastHigh =
                    high.subList(0, i + 1);

            List<Double> pastLow =
                    low.subList(0, i + 1);

            List<Double> pastVolume =
                    volume.subList(0, i + 1);


            CombinedEngine.CombinedResult signal;

            try {

                signal =
                        CombinedEngine.analyze(
                                pastClose,
                                pastHigh,
                                pastLow,
                                pastVolume
                        );

            } catch (Exception e) {

                neutralSignals++;
                continue;
            }


            if (signal == null) {

                neutralSignals++;
                continue;
            }


            // =================================================
            // SIGNAL STRENGTH
            // =================================================

            if (
                    "STRONG".equals(
                            signal.signalStrength
                    )
            ) {

                strongSignals++;

            } else if (
                    "MODERATE".equals(
                            signal.signalStrength
                    )
            ) {

                moderateSignals++;

            } else {

                weakSignals++;
            }


            // =================================================
            // ENGINE AGREEMENT
            // =================================================

            if (
                    signal.agreement != null
                            &&
                    signal.agreement.startsWith("BUY")
            ) {

                buyAgreementSignals++;

            } else if (
                    signal.agreement != null
                            &&
                    signal.agreement.startsWith("SELL")
            ) {

                sellAgreementSignals++;
            }


            String direction =
                    signal.direction;


            // =================================================
            // IGNORE NEUTRAL
            // =================================================

            if (
                    direction == null
                            ||
                    "NEUTRAL".equalsIgnoreCase(
                            direction
                    )
            ) {

                neutralSignals++;
                continue;
            }


            // =================================================
            // PRICES
            // =================================================

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

                neutralSignals++;
                continue;
            }


            // =================================================
            // RAW RETURN
            // =================================================

            double rawReturn;


            if (
                    "BUY".equalsIgnoreCase(
                            direction
                    )
            ) {

                rawReturn =
                        (
                                futurePrice
                                        -
                                entryPrice
                        )
                                /
                        entryPrice
                                *
                        100.0;

            } else if (
                    "SELL".equalsIgnoreCase(
                            direction
                    )
            ) {

                rawReturn =
                        (
                                entryPrice
                                        -
                                futurePrice
                        )
                                /
                        entryPrice
                                *
                        100.0;

            } else {

                neutralSignals++;
                continue;
            }


            // =================================================
            // COST
            // =================================================

            double totalCost =
                    (
                            FEE_PER_SIDE * 2.0
                    )
                            +
                    (
                            SLIPPAGE_PER_SIDE * 2.0
                    );


            double netReturn =
                    rawReturn - totalCost;


            // =================================================
            // COUNT
            // =================================================

            totalSignals++;

            totalReturn += netReturn;

            returnCount++;


            if (
                    netReturn > 0.0
            ) {

                correctSignals++;

                grossProfit += netReturn;

            } else {

                wrongSignals++;

                grossLoss +=
                        Math.abs(netReturn);
            }


            // =================================================
            // BUY
            // =================================================

            if (
                    "BUY".equalsIgnoreCase(
                            direction
                    )
            ) {

                buySignals++;

                buyTotalReturn +=
                        netReturn;


                if (
                        netReturn > 0.0
                ) {

                    buyCorrect++;
                }
            }


            // =================================================
            // SELL
            // =================================================

            if (
                    "SELL".equalsIgnoreCase(
                            direction
                    )
            ) {

                sellSignals++;

                sellTotalReturn +=
                        netReturn;


                if (
                        netReturn > 0.0
                ) {

                    sellCorrect++;
                }
            }


            // =================================================
            // EQUITY CURVE
            // =================================================

            equity =
                    equity
                            *
                    (
                            1.0
                                    +
                            (
                                    netReturn / 100.0
                            )
                    );


            if (
                    equity > peakEquity
            ) {

                peakEquity =
                        equity;
            }


            double drawdown =
                    (
                            peakEquity
                                    -
                            equity
                    )
                            /
                    peakEquity
                            *
                    100.0;


            if (
                    drawdown > maxDrawdown
            ) {

                maxDrawdown =
                        drawdown;
            }
        }


        // =====================================================
        // ACCURACY
        // =====================================================

        double accuracy =
                percentage(
                        correctSignals,
                        totalSignals
                );


        double buyAccuracy =
                percentage(
                        buyCorrect,
                        buySignals
                );


        double sellAccuracy =
                percentage(
                        sellCorrect,
                        sellSignals
                );


        // =====================================================
        // AVERAGE RETURN
        // =====================================================

        double averageReturn = 0.0;


        if (
                returnCount > 0
        ) {

            averageReturn =
                    totalReturn
                            /
                    returnCount;
        }


        // =====================================================
        // PROFIT FACTOR
        // =====================================================

        double profitFactor = 0.0;


        if (
                grossLoss > 0.0
        ) {

            profitFactor =
                    grossProfit
                            /
                    grossLoss;

        } else if (
                grossProfit > 0.0
        ) {

            profitFactor =
                    999.0;
        }


        // =====================================================
        // BUY AVERAGE
        // =====================================================

        double buyAverageReturn = 0.0;


        if (
                buySignals > 0
        ) {

            buyAverageReturn =
                    buyTotalReturn
                            /
                    buySignals;
        }


        // =====================================================
        // SELL AVERAGE
        // =====================================================

        double sellAverageReturn = 0.0;


        if (
                sellSignals > 0
        ) {

            sellAverageReturn =
                    sellTotalReturn
                            /
                    sellSignals;
        }


        // =====================================================
        // BUY & HOLD
        // =====================================================

        double firstPrice =
                close.get(
                        MIN_HISTORY
                );

        double lastPrice =
                close.get(
                        size - 1
                );


        double buyHoldReturn = 0.0;


        if (
                firstPrice > 0.0
        ) {

            buyHoldReturn =
                    (
                            lastPrice
                                    -
                            firstPrice
                    )
                            /
                    firstPrice
                            *
                    100.0;
        }


        // =====================================================
        // RESULT
        // =====================================================

        return new Result(

                totalSignals,

                correctSignals,

                wrongSignals,

                neutralSignals,

                buySignals,

                sellSignals,

                buyCorrect,

                sellCorrect,

                round(accuracy),

                round(buyAccuracy),

                round(sellAccuracy),

                round(averageReturn),

                round(totalReturn),

                round(profitFactor),

                round(grossProfit),

                round(grossLoss),

                round(maxDrawdown),

                round(buyAverageReturn),

                round(sellAverageReturn),

                round(buyTotalReturn),

                round(sellTotalReturn),

                round(buyHoldReturn),

                buyAgreementSignals,

                sellAgreementSignals,

                strongSignals,

                moderateSignals,

                weakSignals
        );
    }


    // =========================================================
    // EMPTY RESULT
    // =========================================================

    private static Result emptyResult() {

        return new Result(

                0,
                0,
                0,
                0,

                0,
                0,

                0,
                0,

                0.0,
                0.0,
                0.0,

                0.0,
                0.0,

                0.0,

                0.0,
                0.0,

                0.0,

                0.0,
                0.0,

                0.0,
                0.0,

                0.0,

                0,
                0,

                0,
                0,
                0
        );
    }


    // =========================================================
    // PERCENTAGE
    // =========================================================

    private static double percentage(
            int numerator,
            int denominator
    ) {

        if (
                denominator <= 0
        ) {

            return 0.0;
        }


        return (
                (
                        (double) numerator
                )
                        /
                denominator
        )
                *
                100.0;
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
