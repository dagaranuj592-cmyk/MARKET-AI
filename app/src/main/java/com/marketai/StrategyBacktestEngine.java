package com.marketai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StrategyBacktestEngine {

    private static final int MIN_HISTORY = 80;
    private static final int MAX_HOLDING = 12;

    // Educational backtest assumptions
    private static final double FEE_PER_SIDE = 0.0010;
    private static final double SLIPPAGE_PER_SIDE = 0.0005;

    public static class Result {

        public int totalSignals;
        public int longSignals;
        public int shortSignals;

        public int wins;
        public int losses;
        public int timeExits;

        public double winRate;
        public double averageReturn;
        public double totalReturn;
        public double profitFactor;
        public double maxDrawdown;

        public int quality60Plus;
        public int quality70Plus;
        public int quality80Plus;

        public int confluenceSignals;
        public int confluenceWins;

        public String summary;

        public Result() {
            summary = "Not enough data";
        }
    }

    private static class Trade {

        String direction;

        double entry;
        double stop;
        double target;

        double exit;
        double returnPercent;

        int barsHeld;

        boolean win;
    }

    public static Result run(
            List<Double> close,
            List<Double> high,
            List<Double> low,
            List<Double> volume
    ) {

        Result result = new Result();

        if (close == null ||
                high == null ||
                low == null ||
                volume == null) {
            return result;
        }

        int size = Math.min(
                Math.min(close.size(), high.size()),
                Math.min(low.size(), volume.size())
        );

        if (size < MIN_HISTORY + MAX_HOLDING) {
            result.summary = "Not enough historical data";
            return result;
        }

        List<Double> returns = new ArrayList<>();

        double equity = 1.0;
        double peakEquity = 1.0;
        double maxDrawdown = 0.0;

        double grossProfit = 0.0;
        double grossLoss = 0.0;

        int i = MIN_HISTORY;

        while (i < size - 1) {

            List<Double> c = new ArrayList<>(close.subList(0, i));
            List<Double> h = new ArrayList<>(high.subList(0, i));
            List<Double> l = new ArrayList<>(low.subList(0, i));
            List<Double> v = new ArrayList<>(volume.subList(0, i));

            StrategyEngine.StrategyResult strategy =
                    StrategyEngine.analyze(c, h, l, v);

            if (strategy == null ||
                    strategy.setup == null ||
                    strategy.setup.equalsIgnoreCase("NO SETUP")) {

                i++;
                continue;
            }

            boolean isLong =
                    strategy.direction != null &&
                    strategy.direction.equalsIgnoreCase("LONG");

            boolean isShort =
                    strategy.direction != null &&
                    strategy.direction.equalsIgnoreCase("SHORT");

            if (!isLong && !isShort) {
                i++;
                continue;
            }

            if (strategy.qualityScore < 60) {
                i++;
                continue;
            }

            result.totalSignals++;

            if (isLong) {
                result.longSignals++;
            }

            if (isShort) {
                result.shortSignals++;
            }

            if (strategy.qualityScore >= 60) {
                result.quality60Plus++;
            }

            if (strategy.qualityScore >= 70) {
                result.quality70Plus++;
            }

            if (strategy.qualityScore >= 80) {
                result.quality80Plus++;
            }

            Trade trade = simulateTrade(
                    strategy,
                    close,
                    high,
                    low,
                    i,
                    size
            );

            if (trade == null) {
                i++;
                continue;
            }

            returns.add(trade.returnPercent);

            if (trade.win) {
                result.wins++;
            } else {
                result.losses++;
            }

            if (trade.barsHeld >= MAX_HOLDING) {
                result.timeExits++;
            }

            if (trade.returnPercent > 0) {
                grossProfit += trade.returnPercent;
            } else {
                grossLoss += Math.abs(trade.returnPercent);
            }

            equity *= (1.0 + trade.returnPercent / 100.0);

            if (equity > peakEquity) {
                peakEquity = equity;
            }

            double drawdown =
                    ((peakEquity - equity) / peakEquity) * 100.0;

            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }

            // Prevent overlapping trades.
            i += Math.max(1, trade.barsHeld);

        }

        result.totalReturn = (equity - 1.0) * 100.0;

        if (result.totalSignals > 0) {
            result.winRate =
                    ((double) result.wins / result.totalSignals) * 100.0;

            result.averageReturn =
                    average(returns);
        }

        if (grossLoss > 0) {
            result.profitFactor =
                    grossProfit / grossLoss;
        } else if (grossProfit > 0) {
            result.profitFactor = Double.POSITIVE_INFINITY;
        } else {
            result.profitFactor = 0.0;
        }

        result.maxDrawdown = maxDrawdown;

        result.summary = buildSummary(result);

        return result;
    }

    private static Trade simulateTrade(
            StrategyEngine.StrategyResult strategy,
            List<Double> close,
            List<Double> high,
            List<Double> low,
            int entryIndex,
            int size
    ) {

        boolean isLong =
                strategy.direction != null &&
                strategy.direction.equalsIgnoreCase("LONG");

        boolean isShort =
                strategy.direction != null &&
                strategy.direction.equalsIgnoreCase("SHORT");

        if (!isLong && !isShort) {
            return null;
        }

        double entry = close.get(entryIndex);

        if (entry <= 0) {
            return null;
        }

        double stop = strategy.stopLoss;
        double target = strategy.target1;

        if (stop <= 0 || target <= 0) {
            return null;
        }

        Trade trade = new Trade();

        trade.direction = isLong ? "LONG" : "SHORT";
        trade.entry = entry;
        trade.stop = stop;
        trade.target = target;

        int end =
                Math.min(
                        size - 1,
                        entryIndex + MAX_HOLDING
                );

        boolean exited = false;

        for (int j = entryIndex + 1; j <= end; j++) {

            double barHigh = high.get(j);
            double barLow = low.get(j);

            if (isLong) {

                boolean stopHit = barLow <= stop;
                boolean targetHit = barHigh >= target;

                /*
                 * If both are touched in the same candle,
                 * use the conservative assumption:
                 * stop is considered hit first.
                 */
                if (stopHit) {

                    trade.exit = stop;
                    trade.win = false;
                    trade.barsHeld = j - entryIndex;

                    exited = true;
                    break;

                } else if (targetHit) {

                    trade.exit = target;
                    trade.win = true;
                    trade.barsHeld = j - entryIndex;

                    exited = true;
                    break;
                }

            } else {

                boolean stopHit = barHigh >= stop;
                boolean targetHit = barLow <= target;

                if (stopHit) {

                    trade.exit = stop;
                    trade.win = false;
                    trade.barsHeld = j - entryIndex;

                    exited = true;
                    break;

                } else if (targetHit) {

                    trade.exit = target;
                    trade.win = true;
                    trade.barsHeld = j - entryIndex;

                    exited = true;
                    break;
                }
            }
        }

        // Time exit
        if (!exited) {

            trade.exit = close.get(end);
            trade.barsHeld = end - entryIndex;

            if (isLong) {
                trade.win = trade.exit > trade.entry;
            } else {
                trade.win = trade.exit < trade.entry;
            }
        }

        double rawReturn;

        if (isLong) {
            rawReturn =
                    ((trade.exit - trade.entry) / trade.entry) * 100.0;
        } else {
            rawReturn =
                    ((trade.entry - trade.exit) / trade.entry) * 100.0;
        }

        /*
         * Approximate round-trip trading friction.
         * This is for backtest realism, not live execution.
         */
        double friction =
                (FEE_PER_SIDE + SLIPPAGE_PER_SIDE) * 2.0 * 100.0;

        trade.returnPercent =
                rawReturn - friction;

        // Re-evaluate win after costs.
        trade.win = trade.returnPercent > 0;

        return trade;
    }

    private static double average(List<Double> values) {

        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private static String buildSummary(Result r) {

        return String.format(
                Locale.US,
                "Signals=%d | Win Rate=%.2f%% | Avg Return=%.3f%% | Total Return=%.2f%% | PF=%.2f | Max DD=%.2f%%",
                r.totalSignals,
                r.winRate,
                r.averageReturn,
                r.totalReturn,
                r.profitFactor,
                r.maxDrawdown
        );
    }
                }
