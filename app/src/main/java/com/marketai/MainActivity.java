package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private LinearLayout root;

    private TextView btcPriceText;
    private TextView goldPriceText;
    private TextView supportText;
    private TextView resistanceText;
    private TextView trendText;
    private TextView volumeText;

    private TextView rsiText;
    private TextView emaText;
    private TextView macdText;
    private TextView atrText;

    private TextView signalText;
    private TextView scoreText;

    private TextView buyProbabilityText;
    private TextView sellProbabilityText;
    private TextView neutralProbabilityText;
    private TextView directionText;
    private TextView confidenceText;
    private TextView sampleText;

    private PriceChartView chartView;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<Double> btcPrices =
            new ArrayList<>();

    private final List<Double> btcHighs =
            new ArrayList<>();

    private final List<Double> btcLows =
            new ArrayList<>();

    private final List<Double> btcVolumes =
            new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUI();

        loadMarketData();
    }


    // =========================================================
    // UI
    // =========================================================

    private void buildUI() {

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(24)
        );

        scrollView.addView(root);

        setContentView(scrollView);


        TextView title =
                createText(
                        "MARKET AI",
                        28,
                        Color.WHITE
                );

        title.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        root.addView(title);


        TextView subtitle =
                createText(
                        "BTC + GOLD Market Analysis",
                        14,
                        Color.LTGRAY
                );

        root.addView(
                subtitle,
                marginParams(
                        0,
                        4,
                        0,
                        18
                )
        );


        // =====================================================
        // MARKET
        // =====================================================

        root.addView(
                sectionTitle("MARKET")
        );


        btcPriceText =
                createText(
                        "BTC: Loading...",
                        20,
                        Color.WHITE
                );

        root.addView(
                btcPriceText,
                marginParams(
                        0,
                        8,
                        0,
                        8
                )
        );


        goldPriceText =
                createText(
                        "Gold: Loading...",
                        20,
                        Color.WHITE
                );

        root.addView(
                goldPriceText,
                marginParams(
                        0,
                        0,
                        0,
                        16
                )
        );


        // =====================================================
        // PRICE LEVELS
        // =====================================================

        root.addView(
                sectionTitle("PRICE LEVELS")
        );


        supportText =
                createText(
                        "Support: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                supportText,
                marginParams(
                        0,
                        8,
                        0,
                        6
                )
        );


        resistanceText =
                createText(
                        "Resistance: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                resistanceText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        trendText =
                createText(
                        "Trend: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                trendText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        volumeText =
                createText(
                        "Volume: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                volumeText,
                marginParams(
                        0,
                        0,
                        0,
                        16
                )
        );


        // =====================================================
        // CHART
        // =====================================================

        root.addView(
                sectionTitle("BTC CHART")
        );


        chartView =
                new PriceChartView(this);


        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(260)
                );


        chartParams.setMargins(
                0,
                dp(10),
                0,
                dp(20)
        );


        root.addView(
                chartView,
                chartParams
        );


        // =====================================================
        // TECHNICAL ANALYSIS
        // =====================================================

        root.addView(
                sectionTitle("TECHNICAL ANALYSIS")
        );


        rsiText =
                createText(
                        "RSI: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                rsiText,
                marginParams(
                        0,
                        8,
                        0,
                        6
                )
        );


        emaText =
                createText(
                        "EMA20: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                emaText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        macdText =
                createText(
                        "MACD: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                macdText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        atrText =
                createText(
                        "ATR: --",
                        17,
                        Color.WHITE
                );

        root.addView(
                atrText,
                marginParams(
                        0,
                        0,
                        0,
                        16
                )
        );


        // =====================================================
        // TECHNICAL SIGNAL
        // =====================================================

        root.addView(
                sectionTitle("AI SIGNAL")
        );


        signalText =
                createText(
                        "Signal: Loading...",
                        22,
                        Color.WHITE
                );

        signalText.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        root.addView(
                signalText,
                marginParams(
                        0,
                        10,
                        0,
                        8
                )
        );


        scoreText =
                createText(
                        "Technical Score: --",
                        18,
                        Color.WHITE
                );


        root.addView(
                scoreText,
                marginParams(
                        0,
                        0,
                        0,
                        18
                )
        );


        // =====================================================
        // PROBABILITY ENGINE
        // =====================================================

        root.addView(
                sectionTitle("AI PROBABILITY")
        );


        buyProbabilityText =
                createText(
                        "BUY: --",
                        20,
                        Color.WHITE
                );

        buyProbabilityText.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        root.addView(
                buyProbabilityText,
                marginParams(
                        0,
                        10,
                        0,
                        6
                )
        );


        sellProbabilityText =
                createText(
                        "SELL: --",
                        20,
                        Color.WHITE
                );

        sellProbabilityText.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        root.addView(
                sellProbabilityText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        neutralProbabilityText =
                createText(
                        "NEUTRAL: --",
                        20,
                        Color.WHITE
                );

        neutralProbabilityText.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        root.addView(
                neutralProbabilityText,
                marginParams(
                        0,
                        0,
                        0,
                        12
                )
        );


        directionText =
                createText(
                        "Direction: --",
                        17,
                        Color.WHITE
                );


        root.addView(
                directionText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        confidenceText =
                createText(
                        "Confidence: --",
                        17,
                        Color.WHITE
                );


        root.addView(
                confidenceText,
                marginParams(
                        0,
                        0,
                        0,
                        6
                )
        );


        sampleText =
                createText(
                        "Historical Samples: --",
                        15,
                        Color.LTGRAY
                );


        root.addView(
                sampleText,
                marginParams(
                        0,
                        0,
                        0,
                        18
                )
        );


        TextView note =
                createText(
                        "Probability is a historical/model estimate and is not a guaranteed prediction.",
                        13,
                        Color.GRAY
                );


        root.addView(note);
    }


    private TextView sectionTitle(
            String text
    ) {

        TextView view =
                createText(
                        text,
                        16,
                        Color.WHITE
                );


        view.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        view.setPadding(
                0,
                dp(8),
                0,
                dp(4)
        );


        return view;
    }


    private TextView createText(
            String text,
            int size,
            int color
    ) {

        TextView view =
                new TextView(this);


        view.setText(text);

        view.setTextSize(size);

        view.setTextColor(color);


        return view;
    }


    private LinearLayout.LayoutParams marginParams(
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );


        return params;
    }


    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return (int)
                (
                        value * density
                                + 0.5f
                );
    }


    // =========================================================
    // LOAD MARKET DATA
    // =========================================================

    private void loadMarketData() {

        Toast.makeText(
                this,
                "Loading market data...",
                Toast.LENGTH_SHORT
        ).show();


        executor.execute(() -> {

            try {

                String btcJson =
                        download(
                                "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart?vs_currency=usd&days=180&interval=daily"
                        );


                String goldJson =
                        download(
                                "https://query1.finance.yahoo.com/v8/finance/chart/GC=F?range=6mo&interval=1d"
                        );


                parseBTC(
                        btcJson
                );


                double gold =
                        parseGold(
                                goldJson
                        );


                runOnUiThread(() -> {

                    goldPriceText.setText(
                            "Gold: $" +
                            formatPrice(gold)
                    );


                    analyzeBTC();
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Data loading failed",
                            Toast.LENGTH_LONG
                    ).show();


                    btcPriceText.setText(
                            "BTC: Data unavailable"
                    );


                    goldPriceText.setText(
                            "Gold: Data unavailable"
                    );
                });
            }
        });
    }


    // =========================================================
    // DOWNLOAD
    // =========================================================

    private String download(
            String urlString
    ) throws Exception {

        URL url =
                new URL(urlString);


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();


        connection.setRequestMethod(
                "GET"
        );


        connection.setConnectTimeout(
                15000
        );


        connection.setReadTimeout(
                15000
        );


        connection.setRequestProperty(
                "User-Agent",
                "MarketAI/1.0"
        );


        int responseCode =
                connection.getResponseCode();


        InputStream stream;


        if (
                responseCode >= 200 &&
                responseCode < 300
        ) {

            stream =
                    connection.getInputStream();

        } else {

            stream =
                    connection.getErrorStream();
        }


        if (stream == null) {

            connection.disconnect();

            throw new Exception(
                    "Empty response"
            );
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream
                        )
                );


        StringBuilder result =
                new StringBuilder();


        String line;


        while (
                (line = reader.readLine())
                        != null
        ) {

            result.append(line);
        }


        reader.close();

        connection.disconnect();


        return result.toString();
    }


    // =========================================================
    // PARSE BTC
    // =========================================================

    private void parseBTC(
            String json
    ) throws Exception {

        JSONObject object =
                new JSONObject(json);


        JSONArray prices =
                object.getJSONArray(
                        "prices"
                );


        JSONArray totalVolumes =
                object.getJSONArray(
                        "total_volumes"
                );


        btcPrices.clear();

        btcHighs.clear();

        btcLows.clear();

        btcVolumes.clear();


        for (
                int i = 0;
                i < prices.length();
                i++
        ) {

            JSONArray row =
                    prices.getJSONArray(i);


            double close =
                    row.getDouble(1);


            btcPrices.add(
                    close
            );


            /*
             * Temporary OHLC proxy.
             *
             * This will later be replaced
             * with proper historical OHLC data.
             */

            btcHighs.add(
                    close * 1.01
            );


            btcLows.add(
                    close * 0.99
            );


            JSONArray volumeRow =
                    totalVolumes.getJSONArray(i);


            double volume =
                    volumeRow.getDouble(1);


            btcVolumes.add(
                    volume
            );
        }
    }


    // =========================================================
    // PARSE GOLD
    // =========================================================

    private double parseGold(
            String json
    ) throws Exception {

        JSONObject rootObject =
                new JSONObject(json);


        JSONObject chart =
                rootObject
                        .getJSONObject("chart");


        JSONArray results =
                chart.getJSONArray("result");


        JSONObject result =
                results.getJSONObject(0);


        JSONObject indicators =
                result.getJSONObject(
                        "indicators"
                );


        JSONArray quote =
                indicators.getJSONArray(
                        "quote"
                );


        JSONObject quoteObject =
                quote.getJSONObject(0);


        JSONArray closes =
                quoteObject.getJSONArray(
                        "close"
                );


        double latest = 0.0;


        for (
                int i = closes.length() - 1;
                i >= 0;
                i--
        ) {

            if (!closes.isNull(i)) {

                latest =
                        closes.getDouble(i);

                break;
            }
        }


        return latest;
    }


    // =========================================================
    // BTC ANALYSIS
    // =========================================================

    private void analyzeBTC() {

        if (
                btcPrices == null ||
                btcPrices.size() < 30
        ) {

            btcPriceText.setText(
                    "BTC: Not enough data"
            );


            return;
        }


        double currentPrice =
                btcPrices.get(
                        btcPrices.size() - 1
                );


        btcPriceText.setText(
                "BTC: $" +
                formatPrice(currentPrice)
        );


        double support =
                calculateSupport(
                        btcPrices
                );


        double resistance =
                calculateResistance(
                        btcPrices
                );


        String trend =
                calculateTrend(
                        btcPrices
                );


        double volume =
                calculateAverageVolume(
                        btcVolumes
                );


        supportText.setText(
                "Support: $" +
                formatPrice(support)
        );


        resistanceText.setText(
                "Resistance: $" +
                formatPrice(resistance)
        );


        trendText.setText(
                "Trend: " +
                trend
        );


        volumeText.setText(
                "Average Volume: " +
                formatLargeNumber(volume)
        );


        // =====================================================
        // TECHNICAL ENGINE
        // =====================================================

        TechnicalAnalyzer.TechnicalResult technical =
                TechnicalAnalyzer.analyze(
                        btcPrices,
                        btcHighs,
                        btcLows
                );


        rsiText.setText(
                "RSI: " +
                format2(
                        technical.rsi
                )
        );


        emaText.setText(
                "EMA20: $" +
                formatPrice(
                        technical.ema20
                )
        );


        macdText.setText(
                "MACD: " +
                format2(
                        technical.macd
                )
        );


        atrText.setText(
                "ATR: $" +
                formatPrice(
                        technical.atr
                )
        );


        int score =
                calculateTechnicalScore(
                        currentPrice,
                        trend,
                        technical
                );


        String signal;


        if (score >= 3) {

            signal =
                    "BUY BIAS";

        } else if (score <= -3) {

            signal =
                    "SELL BIAS";

        } else {

            signal =
                    "NEUTRAL";
        }


        signalText.setText(
                "Technical Signal: " +
                signal
        );


        scoreText.setText(
                "Technical Score: " +
                score +
                " / 4"
        );


        // =====================================================
        // PROBABILITY ENGINE
        // =====================================================

        ProbabilityEngine.ProbabilityResult probability =
                ProbabilityEngine.calculate(
                        btcPrices
                );


        buyProbabilityText.setText(
                "BUY: " +
                format2(
                        probability.buyProbability
                ) +
                "%"
        );


        sellProbabilityText.setText(
                "SELL: " +
                format2(
                        probability.sellProbability
                ) +
                "%"
        );


        neutralProbabilityText.setText(
                "NEUTRAL: " +
                format2(
                        probability.neutralProbability
                ) +
                "%"
        );


        directionText.setText(
                "Direction: " +
                probability.direction
        );


        confidenceText.setText(
                "Confidence: " +
                probability.confidence
        );


        sampleText.setText(
                "Historical Samples: " +
                probability.sampleSize
        );


        // =====================================================
        // CHART
        // =====================================================

        chartView.setPrices(
                btcPrices
        );
    }


    // =========================================================
    // SUPPORT
    // =========================================================

    private double calculateSupport(
            List<Double> prices
    ) {

        int start =
                Math.max(
                        0,
                        prices.size() - 30
                );


        double lowest =
                Double.MAX_VALUE;


        for (
                int i = start;
                i < prices.size();
                i++
        ) {

            double value =
                    prices.get(i);


            if (
                    value < lowest
            ) {

                lowest = value;
            }
        }


        return lowest;
    }


    // =========================================================
    // RESISTANCE
    // =========================================================

    private double calculateResistance(
            List<Double> prices
    ) {

        int start =
                Math.max(
                        0,
                        prices.size() - 30
                );


        double highest =
                -Double.MAX_VALUE;


        for (
                int i = start;
                i < prices.size();
                i++
        ) {

            double value =
                    prices.get(i);


            if (
                    value > highest
            ) {

                highest = value;
            }
        }


        return highest;
    }


    // =========================================================
    // TREND
    // =========================================================

    private String calculateTrend(
            List<Double> prices
    ) {

        if (
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
    // VOLUME
    // =========================================================

    private double calculateAverageVolume(
            List<Double> volumes
    ) {

        if (
                volumes == null ||
                volumes.isEmpty()
        ) {

            return 0.0;
        }


        int count =
                Math.min(
                        20,
                        volumes.size()
                );


        double sum = 0.0;


        for (
                int i = volumes.size() - count;
                i < volumes.size();
                i++
        ) {

            sum +=
                    volumes.get(i);
        }


        return sum / count;
    }


    // =========================================================
    // TECHNICAL SCORE
    // =========================================================

    private int calculateTechnicalScore(
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
    // AVERAGE HELPERS
    // =========================================================

    private double averageLast(
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
                int i = values.size() - count;
                i < values.size();
                i++
        ) {

            sum +=
                    values.get(i);
        }


        return sum / count;
    }


    private double averageRange(
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
    // FORMAT
    // =========================================================

    private String formatPrice(
            double value
    ) {

        if (
                value == 0
        ) {

            return "--";
        }


        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }


    private String format2(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }


    private String formatLargeNumber(
            double value
    ) {

        if (
                value >=
                1_000_000_000_000.0
        ) {

            return String.format(
                    Locale.US,
                    "%.2fT",
                    value /
                    1_000_000_000_000.0
            );
        }


        if (
                value >=
                1_000_000_000.0
        ) {

            return String.format(
                    Locale.US,
                    "%.2fB",
                    value /
                    1_000_000_000.0
            );
        }


        if (
                value >=
                1_000_000.0
        ) {

            return String.format(
                    Locale.US,
                    "%.2fM",
                    value /
                    1_000_000.0
            );
        }


        if (
                value >=
                1_000.0
        ) {

            return String.format(
                    Locale.US,
                    "%.2fK",
                    value /
                    1_000.0
            );
        }


        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }


    // =========================================================
    // CHART
    // =========================================================

    private class PriceChartView
            extends View {

        private final Paint linePaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );


        private final Paint textPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );


        private List<Double> prices =
                new ArrayList<>();


        public PriceChartView(
                android.content.Context context
        ) {

            super(context);


            setBackgroundColor(
                    Color.rgb(
                            18,
                            22,
                            28
                    )
            );


            linePaint.setColor(
                    Color.rgb(
                            0,
                            200,
                            83
                    )
            );


            linePaint.setStrokeWidth(
                    dp(2)
            );


            linePaint.setStyle(
                    Paint.Style.STROKE
            );


            textPaint.setColor(
                    Color.GRAY
            );


            textPaint.setTextSize(
                    dp(11)
            );
        }


        public void setPrices(
                List<Double> values
        ) {

            prices =
                    new ArrayList<>(
                            values
                    );


            invalidate();
        }


        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);


            if (
                    prices == null ||
                    prices.size() < 2
            ) {

                canvas.drawText(
                        "Waiting for chart data...",
                        dp(12),
                        dp(30),
                        textPaint
                );


                return;
            }


            int width =
                    getWidth();


            int height =
                    getHeight();


            double min =
                    Double.MAX_VALUE;


            double max =
                    -Double.MAX_VALUE;


            int start =
                    Math.max(
                            0,
                            prices.size() - 90
                    );


            for (
                    int i = start;
                    i < prices.size();
                    i++
            ) {

                double value =
                        prices.get(i);


                if (
                        value < min
                ) {

                    min = value;
                }


                if (
                        value > max
                ) {

                    max = value;
                }
            }


            if (
                    max <= min
            ) {

                return;
            }


            float left =
                    dp(12);


            float right =
                    width -
                    dp(12);


            float top =
                    dp(20);


            float bottom =
                    height -
                    dp(20);


            Path path =
                    new Path();


            int visibleCount =
                    prices.size() -
                    start;


            for (
                    int i = 0;
                    i < visibleCount;
                    i++
            ) {

                double value =
                        prices.get(
                                start + i
                        );


                float x =
                        left +
                        (
                                (right - left)
                                *
                                i /
                                Math.max(
                                        1,
                                        visibleCount - 1
                                )
                        );


                float normalized =
                        (float)
                        (
                                (value - min)
                                /
                                (max - min)
                        );


                float y =
                        bottom -
                        (
                                normalized *
                                (bottom - top)
                        );


                if (
                        i == 0
                ) {

                    path.moveTo(
                            x,
                            y
                    );

                } else {

                    path.lineTo(
                            x,
                            y
                    );
                }
            }


            canvas.drawPath(
                    path,
                    linePaint
            );


            canvas.drawText(
                    "$" +
                    formatPrice(max),
                    left,
                    top,
                    textPaint
            );


            canvas.drawText(
                    "$" +
                    formatPrice(min),
                    left,
                    bottom,
                    textPaint
            );
        }
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdownNow();
    }
                    }
