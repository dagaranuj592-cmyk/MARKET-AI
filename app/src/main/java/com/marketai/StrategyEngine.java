package com.marketai;

import java.util.List;

public class StrategyEngine {

    // =========================================================
    // RESULT
    // =========================================================

    public static class StrategyResult {

        public String setup;
        public String strategy;
        public String direction;

        public double qualityScore;

        public double entry;
        public double stopLoss;
        public double target1;
        public double target2;

        public double risk;
        public double reward1;
        public double reward2;

        public double riskReward1;
        public double riskReward2;

        public boolean emaAligned;
        public boolean trendAligned;
        public boolean momentumConfirmed;
        public boolean volumeConfirmed;
        public boolean bollingerBreakout;
        public boolean rsiConfirmed;
        public boolean supportResistanceConfirmed;
        public boolean multiTimeframeConfirmed;

        public String evidence;

        public StrategyResult() {

            setup = "NO SETUP";
            strategy = "NONE";
            direction = "NEUTRAL";

            qualityScore = 0;

            entry = 0;
            stopLoss = 0;
            target1 = 0;
            target2 = 0;

            risk = 0;
            reward1 = 0;
            reward2 = 0;

            riskReward1 = 0;
            riskReward2 = 0;

            emaAligned = false;
            trendAligned = false;
            momentumConfirmed = false;
            volumeConfirmed = false;
            bollingerBreakout = false;
            rsiConfirmed = false;
            supportResistanceConfirmed = false;
            multiTimeframeConfirmed = false;

            evidence = "";
        }
    }


    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final int MIN_HISTORY = 60;

    private static final double MIN_QUALITY = 60.0;

    private static final double MIN_RR = 1.30;


    // =========================================================
    // MAIN ANALYZER
    // =========================================================

    public static StrategyResult analyze(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        StrategyResult result =
                new StrategyResult();


        if (
                close == null ||
                high == null ||
                low == null ||
                volume == null
        ) {

            result.evidence =
                    "Market data unavailable.";

            return result;
        }


        int size =
                close.size();


        if (
                size < MIN_HISTORY ||
                high.size() < size ||
                low.size() < size ||
                volume.size() < size
        ) {

            result.evidence =
                    "Not enough market history.";

            return result;
        }


        // =====================================================
        // CURRENT VALUES
        // =====================================================

        double price =
                close.get(size - 1);


        double ema5 =
                ema(
                        close,
                        5
                );


        double ema13 =
                ema(
                        close,
                        13
                );


        double ema20 =
                ema(
                        close,
                        20
                );


        double ema50 =
                ema(
                        close,
                        50
                );


        double rsi =
                rsi(
                        close,
                        14
                );


        double momentum =
                momentum(
                        close,
                        10
                );


        double volumeRatio =
                volumeRatio(
                        volume,
                        20
                );


        double upperBand =
                bollingerUpper(
                        close,
                        20,
                        2.0
                );


        double lowerBand =
                bollingerLower(
                        close,
                        20,
                        2.0
                );


        double atr =
                atr(
                        close,
                        high,
                        low,
                        14
                );


        // =====================================================
        // PREVIOUS VALUES
        // =====================================================

        double previousClose =
                close.get(size - 2);


        double previousUpper =
                bollingerUpper(
                        close.subList(
                                0,
                                size - 1
                        ),
                        20,
                        2.0
                );


        double previousLower =
                bollingerLower(
                        close.subList(
                                0,
                                size - 1
                        ),
                        20,
                        2.0
                );


        // =====================================================
        // CONDITIONS
        // =====================================================

        boolean bullishEMA =
                ema5 > ema13;


        boolean bearishEMA =
                ema5 < ema13;


        boolean bullishTrend =
                price > ema20 &&
                ema20 > ema50;


        boolean bearishTrend =
                price < ema20 &&
                ema20 < ema50;


        boolean bullishMomentum =
                momentum > 0;


        boolean bearishMomentum =
                momentum < 0;


        boolean bullishVolume =
                volumeRatio >= 1.15;


        boolean bearishVolume =
                volumeRatio >= 1.15;


        boolean bullishRSI =
                rsi >= 52 &&
                rsi <= 70;


        boolean bearishRSI =
                rsi <= 48 &&
                rsi >= 30;


        boolean bullishBreakout =
                price > upperBand &&
                previousClose <= previousUpper;


        boolean bearishBreakout =
                price < lowerBand &&
                previousClose >= previousLower;


        // =====================================================
        // SUPPORT / RESISTANCE
        // =====================================================

        double support =
                recentLow(
                        low,
                        30
                );


        double resistance =
                recentHigh(
                        high,
                        30
                );


        boolean supportOK =
                price > support;


        boolean resistanceOK =
                price < resistance;


        // =====================================================
        // LONG SCORE
        // =====================================================

        double longScore = 0;


        if (
                bullishEMA
        ) {

            longScore += 18;
        }


        if (
                bullishTrend
        ) {

            longScore += 18;
        }


        if (
                bullishMomentum
        ) {

            longScore += 14;
        }


        if (
                bullishVolume
        ) {

            longScore += 10;
        }


        if (
                bullishRSI
        ) {

            longScore += 10;
        }


        if (
                bullishBreakout
        ) {

            longScore += 20;
        }


        if (
                supportOK
        ) {

            longScore += 5;
        }


        if (
                price > ema50
        ) {

            longScore += 5;
        }


        // =====================================================
        // SHORT SCORE
        // =====================================================

        double shortScore = 0;


        if (
                bearishEMA
        ) {

            shortScore += 18;
        }


        if (
                bearishTrend
        ) {

            shortScore += 18;
        }


        if (
                bearishMomentum
        ) {

            shortScore += 14;
        }


        if (
                bearishVolume
        ) {

            shortScore += 10;
        }


        if (
                bearishRSI
        ) {

            shortScore += 10;
        }


        if (
                bearishBreakout
        ) {

            shortScore += 20;
        }


        if (
                resistanceOK
        ) {

            shortScore += 5;
        }


        if (
                price < ema50
        ) {

            shortScore += 5;
        }


        // =====================================================
        // SELECT DIRECTION
        // =====================================================

        boolean longCandidate =
                longScore >= MIN_QUALITY;


        boolean shortCandidate =
                shortScore >= MIN_QUALITY;


        // =====================================================
        // NO SETUP
        // =====================================================

        if (
                !longCandidate &&
                !shortCandidate
        ) {

            result.setup =
                    "NO SETUP";

            result.strategy =
                    "NONE";

            result.direction =
                    "NEUTRAL";

            result.qualityScore =
                    Math.max(
                            longScore,
                            shortScore
                    );


            result.entry =
                    price;


            result.evidence =
                    buildEvidence(
                            longScore,
                            shortScore,
                            bullishEMA,
                            bearishEMA,
                            bullishTrend,
                            bearishTrend,
                            bullishMomentum,
                            bearishMomentum,
                            bullishVolume,
                            bullishBreakout,
                            bearishBreakout
                    );


            return result;
        }


        // =====================================================
        // CONFLICT FILTER
        // =====================================================

        if (
                longCandidate &&
                shortCandidate
        ) {

            double difference =
                    Math.abs(
                            longScore -
                            shortScore
                    );


            if (
                    difference < 15
            ) {

                result.setup =
                        "NO SETUP";

                result.strategy =
                        "CONFLICT";

                result.direction =
                        "NEUTRAL";

                result.qualityScore =
                        Math.max(
                                longScore,
                                shortScore
                        );


                result.evidence =
                        "Bullish and bearish conditions are too closely balanced.";

                return result;
            }
        }


        // =====================================================
        // LONG SETUP
        // =====================================================

        if (
                longScore > shortScore
        ) {

            double stop =
                    Math.min(
                            support,
                            price - atr * 1.20
                    );


            if (
                    stop <= 0 ||
                    stop >= price
            ) {

                stop =
                        price -
                        atr * 1.20;
            }


            double risk =
                    price - stop;


            double target1 =
                    price +
                    risk * 1.50;


            double target2 =
                    price +
                    risk * 2.20;


            double rr1 =
                    risk > 0
                            ? (target1 - price) / risk
                            : 0;


            double rr2 =
                    risk > 0
                            ? (target2 - price) / risk
                            : 0;


            if (
                    rr1 < MIN_RR
            ) {

                result.setup =
                        "NO SETUP";

                result.strategy =
                        "LONG FILTER";

                result.direction =
                        "NEUTRAL";

                result.qualityScore =
                        longScore;

                result.evidence =
                        "Long conditions exist but estimated reward/risk is too weak.";

                return result;
            }


            result.setup =
                    "LONG SETUP";

            result.strategy =
                    bullishBreakout
                            ? "BOLLINGER BREAKOUT"
                            : "EMA 5/13 TREND";

            result.direction =
                    "LONG";

            result.qualityScore =
                    longScore;

            result.entry =
                    price;

            result.stopLoss =
                    stop;

            result.target1 =
                    target1;

            result.target2 =
                    target2;

            result.risk =
                    risk;

            result.reward1 =
                    target1 - price;

            result.reward2 =
                    target2 - price;

            result.riskReward1 =
                    rr1;

            result.riskReward2 =
                    rr2;

            result.emaAligned =
                    bullishEMA;

            result.trendAligned =
                    bullishTrend;

            result.momentumConfirmed =
                    bullishMomentum;

            result.volumeConfirmed =
                    bullishVolume;

            result.bollingerBreakout =
                    bullishBreakout;

            result.rsiConfirmed =
                    bullishRSI;

            result.supportResistanceConfirmed =
                    supportOK;

            result.evidence =
                    buildEvidence(
                            longScore,
                            shortScore,
                            bullishEMA,
                            bearishEMA,
                            bullishTrend,
                            bearishTrend,
                            bullishMomentum,
                            bearishMomentum,
                            bullishVolume,
                            bullishBreakout,
                            bearishBreakout
                    );


            return result;
        }


        // =====================================================
        // SHORT SETUP
        // =====================================================

        double stop =
                Math.max(
                        resistance,
                        price + atr * 1.20
                );


        if (
                stop <= price
        ) {

            stop =
                    price +
                    atr * 1.20;
        }


        double risk =
                stop - price;


        double target1 =
                price -
                risk * 1.50;


        double target2 =
                price -
                risk * 2.20;


        double rr1 =
                risk > 0
                        ? (price - target1) / risk
                        : 0;


        double rr2 =
                risk > 0
                        ? (price - target2) / risk
                        : 0;


        if (
                rr1 < MIN_RR
        ) {

            result.setup =
                    "NO SETUP";

            result.strategy =
                    "SHORT FILTER";

            result.direction =
                    "NEUTRAL";

            result.qualityScore =
                    shortScore;

            result.evidence =
                    "Short conditions exist but estimated reward/risk is too weak.";

            return result;
        }


        result.setup =
                "SHORT SETUP";

        result.strategy =
                bearishBreakout
                        ? "BOLLINGER BREAKOUT"
                        : "EMA 5/13 TREND";

        result.direction =
                "SHORT";

        result.qualityScore =
                shortScore;

        result.entry =
                price;

        result.stopLoss =
                stop;

        result.target1 =
                target1;

        result.target2 =
                target2;

        result.risk =
                risk;

        result.reward1 =
                price - target1;

        result.reward2 =
                price - target2;

        result.riskReward1 =
                rr1;

        result.riskReward2 =
                rr2;

        result.emaAligned =
                bearishEMA;

        result.trendAligned =
                bearishTrend;

        result.momentumConfirmed =
                bearishMomentum;

        result.volumeConfirmed =
                bearishVolume;

        result.bollingerBreakout =
                bearishBreakout;

        result.rsiConfirmed =
                bearishRSI;

        result.supportResistanceConfirmed =
                resistanceOK;

        result.evidence =
                buildEvidence(
                        longScore,
                        shortScore,
                        bullishEMA,
                        bearishEMA,
                        bullishTrend,
                        bearishTrend,
                        bullishMomentum,
                        bearishMomentum,
                        bearishVolume,
                        bullishBreakout,
                        bearishBreakout
                );


        return result;
    }


    // =========================================================
    // EMA
    // =========================================================

    private static double ema(
            List<Double> data,
            int period
    ) {

        if (
                data.size() < period
        ) {

            return 0;
        }


        double multiplier =
                2.0 /
                (period + 1.0);


        double value =
                0;


        int start =
                data.size() - period;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            if (
                    value == 0
            ) {

                value =
                        data.get(i);

            } else {

                value =
                        (
                                data.get(i) -
                                value
                        )
                                *
                                multiplier
                                +
                                value;
            }
        }


        return value;
    }


    // =========================================================
    // RSI
    // =========================================================

    private static double rsi(
            List<Double> data,
            int period
    ) {

        if (
                data.size() <= period
        ) {

            return 50;
        }


        double gain = 0;
        double loss = 0;


        int start =
                data.size() - period;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            double change =
                    data.get(i)
                            -
                    data.get(i - 1);


            if (
                    change > 0
            ) {

                gain += change;

            } else {

                loss -= change;
            }
        }


        double averageGain =
                gain / period;


        double averageLoss =
                loss / period;


        if (
                averageLoss == 0
        ) {

            return 100;
        }


        double rs =
                averageGain /
                averageLoss;


        return
                100 -
                (
                        100 /
                        (1 + rs)
                );
    }


    // =========================================================
    // MOMENTUM
    // =========================================================

    private static double momentum(
            List<Double> data,
            int period
    ) {

        if (
                data.size() <= period
        ) {

            return 0;
        }


        double current =
                data.get(
                        data.size() - 1
                );


        double previous =
                data.get(
                        data.size() - 1 - period
                );


        if (
                previous == 0
        ) {

            return 0;
        }


        return
                (
                        (
                                current -
                                previous
                        )
                                /
                                previous
                )
                        *
                        100.0;
    }


    // =========================================================
    // VOLUME RATIO
    // =========================================================

    private static double volumeRatio(
            List<Double> volume,
            int period
    ) {

        if (
                volume.size() <= period
        ) {

            return 1;
        }


        int last =
                volume.size() - 1;


        double current =
                volume.get(last);


        double sum = 0;


        int start =
                Math.max(
                        0,
                        last - period
                );


        int count = 0;


        for (
                int i = start;
                i < last;
                i++
        ) {

            sum +=
                    volume.get(i);

            count++;
        }


        if (
                count == 0 ||
                sum == 0
        ) {

            return 1;
        }


        double average =
                sum / count;


        return current / average;
    }


    // =========================================================
    // BOLLINGER UPPER
    // =========================================================

    private static double bollingerUpper(
            List<Double> data,
            int period,
            double multiplier
    ) {

        double mean =
                sma(
                        data,
                        period
                );


        double deviation =
                standardDeviation(
                        data,
                        period
                );


        return
                mean +
                deviation * multiplier;
    }


    // =========================================================
    // BOLLINGER LOWER
    // =========================================================

    private static double bollingerLower(
            List<Double> data,
            int period,
            double multiplier
    ) {

        double mean =
                sma(
                        data,
                        period
                );


        double deviation =
                standardDeviation(
                        data,
                        period
                );


        return
                mean -
                deviation * multiplier;
    }


    // =========================================================
    // SMA
    // =========================================================

    private static double sma(
            List<Double> data,
            int period
    ) {

        if (
                data.size() < period
        ) {

            return 0;
        }


        double sum = 0;


        int start =
                data.size() - period;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            sum +=
                    data.get(i);
        }


        return sum / period;
    }


    // =========================================================
    // STANDARD DEVIATION
    // =========================================================

    private static double standardDeviation(
            List<Double> data,
            int period
    ) {

        if (
                data.size() < period
        ) {

            return 0;
        }


        double mean =
                sma(
                        data,
                        period
                );


        double sum = 0;


        int start =
                data.size() - period;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            double difference =
                    data.get(i) - mean;


            sum +=
                    difference *
                    difference;
        }


        return Math.sqrt(
                sum / period
        );
    }


    // =========================================================
    // ATR
    // =========================================================

    private static double atr(

            List<Double> close,
            List<Double> high,
            List<Double> low,

            int period

    ) {

        if (
                close.size() <= period
        ) {

            return 0;
        }


        int start =
                close.size() - period;


        double sum = 0;


        for (
                int i = start;
                i < close.size();
                i++
        ) {

            double previousClose =
                    close.get(i - 1);


            double range1 =
                    high.get(i) -
                    low.get(i);


            double range2 =
                    Math.abs(
                            high.get(i) -
                            previousClose
                    );


            double range3 =
                    Math.abs(
                            low.get(i) -
                            previousClose
                    );


            double trueRange =
                    Math.max(
                            range1,
                            Math.max(
                                    range2,
                                    range3
                            )
                    );


            sum +=
                    trueRange;
        }


        return sum / period;
    }


    // =========================================================
    // RECENT LOW
    // =========================================================

    private static double recentLow(
            List<Double> data,
            int period
    ) {

        int start =
                Math.max(
                        0,
                        data.size() - period
                );


        double value =
                Double.MAX_VALUE;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            value =
                    Math.min(
                            value,
                            data.get(i)
                    );
        }


        return value;
    }


    // =========================================================
    // RECENT HIGH
    // =========================================================

    private static double recentHigh(
            List<Double> data,
            int period
    ) {

        int start =
                Math.max(
                        0,
                        data.size() - period
                );


        double value =
                Double.MIN_VALUE;


        for (
                int i = start;
                i < data.size();
                i++
        ) {

            value =
                    Math.max(
                            value,
                            data.get(i)
                    );
        }


        return value;
    }


    // =========================================================
    // EVIDENCE
    // =========================================================

    private static String buildEvidence(

            double longScore,
            double shortScore,

            boolean bullishEMA,
            boolean bearishEMA,

            boolean bullishTrend,
            boolean bearishTrend,

            boolean bullishMomentum,
            boolean bearishMomentum,

            boolean volumeConfirmed,

            boolean bullishBreakout,
            boolean bearishBreakout

    ) {

        StringBuilder text =
                new StringBuilder();


        if (
                bullishEMA
        ) {

            text.append(
                    "EMA bullish; "
            );
        }


        if (
                bearishEMA
        ) {

            text.append(
                    "EMA bearish; "
            );
        }


        if (
                bullishTrend
        ) {

            text.append(
                    "trend bullish; "
            );
        }


        if (
                bearishTrend
        ) {

            text.append(
                    "trend bearish; "
            );
        }


        if (
                bullishMomentum
        ) {

            text.append(
                    "positive momentum; "
            );
        }


        if (
                bearishMomentum
        ) {

            text.append(
                    "negative momentum; "
            );
        }


        if (
                volumeConfirmed
        ) {

            text.append(
                    "volume expansion; "
            );
        }


        if (
                bullishBreakout
        ) {

            text.append(
                    "upper Bollinger breakout; "
            );
        }


        if (
                bearishBreakout
        ) {

            text.append(
                    "lower Bollinger breakout; "
            );
        }


        if (
                text.length() == 0
        ) {

            text.append(
                    "No strong confluence."
            );
        }


        text.append(
                "Scores L="
        );

        text.append(
                round(longScore)
        );

        text.append(
                " / S="
        );

        text.append(
                round(shortScore)
        );


        return text.toString();
    }


    // =========================================================
    // ROUND
    // =========================================================

    private static String round(
            double value
    ) {

        return String.format(
                java.util.Locale.US,
                "%.1f",
                value
        );
    }
              }
