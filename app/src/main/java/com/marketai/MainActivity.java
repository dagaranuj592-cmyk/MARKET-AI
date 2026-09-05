package com.marketai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private TextView btcCard;
    private TextView goldCard;
    private TextView supportCard;
    private TextView resistanceCard;
    private TextView trendCard;
    private TextView volumeCard;

    private TextView rsiCard;
    private TextView emaCard;
    private TextView macdCard;
    private TextView atrCard;
    private TextView signalCard;
    private TextView status;

    private PriceChart chartView;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int dp(float value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
                + 0.5f
        );
    }

    private TextView makeText(
            String value,
            float size,
            boolean bold
    ) {

        TextView tv =
                new TextView(this);

        tv.setText(value);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(size);
        tv.setGravity(Gravity.CENTER);

        if (bold) {
            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        tv.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        return tv;
    }

    private TextView makeCard(
            String title,
            String value
    ) {

        TextView tv =
                makeText(
                        title + "\n" + value,
                        14,
                        true
                );

        tv.setBackgroundColor(
                Color.rgb(24, 31, 40)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(90),
                        1
                );

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        tv.setLayoutParams(params);

        return tv;
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        /*
         * SCROLL VIEW
         * This makes the complete screen scrollable.
         */

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(12),
                dp(18),
                dp(12),
                dp(20)
        );

        root.setBackgroundColor(
                Color.rgb(8, 12, 18)
        );

        scrollView.addView(root);

        /*
         * TITLE
         */

        TextView title =
                makeText(
                        "MARKET AI",
                        28,
                        true
                );

        title.setPadding(
                0,
                dp(5),
                0,
                dp(18)
        );

        root.addView(title);

        /*
         * BTC + GOLD
         */

        LinearLayout assets =
                new LinearLayout(this);

        assets.setOrientation(
                LinearLayout.HORIZONTAL
        );

        btcCard =
                makeCard(
                        "₿ BTC",
                        "Loading..."
                );

        goldCard =
                makeCard(
                        "GOLD FUTURES",
                        "Loading..."
                );

        assets.addView(btcCard);
        assets.addView(goldCard);

        root.addView(assets);

        /*
         * MARKET ANALYSIS
         */

        TextView heading =
                makeText(
                        "MARKET ANALYSIS",
                        20,
                        true
                );

        heading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        heading.setPadding(
                dp(5),
                dp(22),
                dp(5),
                dp(8)
        );

        root.addView(heading);

        /*
         * SUPPORT + RESISTANCE
         */

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        supportCard =
                makeCard(
                        "SUPPORT",
                        "Calculating..."
                );

        resistanceCard =
                makeCard(
                        "RESISTANCE",
                        "Calculating..."
                );

        row1.addView(supportCard);
        row1.addView(resistanceCard);

        root.addView(row1);

        /*
         * TREND + VOLUME
         */

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        trendCard =
                makeCard(
                        "TREND",
                        "Loading..."
                );

        volumeCard =
                makeCard(
                        "VOLUME",
                        "Loading..."
                );

        row2.addView(trendCard);
        row2.addView(volumeCard);

        root.addView(row2);

        /*
         * TECHNICAL ANALYSIS
         */

        TextView technicalHeading =
                makeText(
                        "TECHNICAL ANALYSIS",
                        20,
                        true
                );

        technicalHeading.setGravity(
                Gravity.CENTER_VERTICAL
        );

        technicalHeading.setPadding(
                dp(5),
                dp(22),
                dp(5),
                dp(8)
        );

        root.addView(technicalHeading);

        /*
         * RSI + EMA
         */

        LinearLayout technicalRow1 =
                new LinearLayout(this);

        technicalRow1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        rsiCard =
                makeCard(
                        "RSI",
                        "Calculating..."
                );

        emaCard =
                makeCard(
                        "EMA 20",
                        "Calculating..."
                );

        technicalRow1.addView(rsiCard);
        technicalRow1.addView(emaCard);

        root.addView(technicalRow1);

        /*
         * MACD + ATR
         */

        LinearLayout technicalRow2 =
                new LinearLayout(this);

        technicalRow2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        macdCard =
                makeCard(
                        "MACD",
                        "Calculating..."
                );

        atrCard =
                makeCard(
                        "ATR",
                        "Calculating..."
                );

        technicalRow2.addView(macdCard);
        technicalRow2.addView(atrCard);

        root.addView(technicalRow2);

        /*
         * TECHNICAL SIGNAL
         */

        signalCard =
                makeCard(
                        "TECHNICAL SIGNAL",
                        "Calculating..."
                );

        LinearLayout.LayoutParams signalParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(85)
                );

        signalParams.setMargins(
                dp(4),
                dp(8),
                dp(4),
                dp(4)
        );

        signalCard.setLayoutParams(
                signalParams
        );

        signalCard.setBackgroundColor(
                Color.rgb(30, 40, 50)
        );

        root.addView(signalCard);

        /*
         * PRICE CHART
         */

        TextView chartTitle =
                makeText(
                        "PRICE CHART",
                        17,
                        true
                );

        chartTitle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        chartTitle.setPadding(
                0,
                dp(18),
                0,
                dp(4)
        );

        root.addView(chartTitle);

        chartView =
                new PriceChart(this);

        chartView.setBackgroundColor(
                Color.rgb(17, 23, 31)
        );

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(260)
                );

        chartParams.setMargins(
                0,
                dp(4),
                0,
                dp(15)
        );

        chartView.setLayoutParams(
                chartParams
        );

        root.addView(chartView);

        /*
         * RUN BUTTON
         */

        TextView button =
                makeText(
                        "🔍  RUN FULL ANALYSIS",
                        17,
                        true
                );

        button.setBackgroundColor(
                Color.rgb(0, 150, 80)
        );

        button.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        loadAllData();
                    }
                }
        );

        root.addView(button);

        /*
         * STATUS
         */

        status =
                makeText(
                        "ENGINE STATUS: READY\n"
                        + "Waiting for market data...",
                        14,
                        false
                );

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                0,
                dp(18),
                0,
                dp(10)
        );

        root.addView(status);

        /*
         * IMPORTANT:
         * Set ScrollView, NOT root.
         */

        setContentView(scrollView);

        loadAllData();
    }

    /*
     * LOAD ALL DATA
     */

    private void loadAllData() {

        status.setText(
                "ENGINE STATUS: RUNNING\n"
                + "Downloading market data..."
        );

        loadBTC();
        loadGold();
    }

    /*
     * BTC DATA
     */

    private void loadBTC() {

        executor.execute(
                new Runnable() {

                    @Override
                    public void run() {

                        try {

                            String json =
                                    getUrl(
                                        "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart"
                                        + "?vs_currency=usd"
                                        + "&days=180"
                                        + "&interval=daily"
                                    );

                            final BTCResult result =
                                    analyzeBTC(json);

                            handler.post(
                                    new Runnable() {

                                        @Override
                                        public void run() {

                                            btcCard.setText(
                                                    "₿ BTC\n$ "
                                                    + format(
                                                        result.price
                                                    )
                                            );

                                            supportCard.setText(
                                                    "SUPPORT\n$ "
                                                    + format(
                                                        result.support
                                                    )
                                            );

                                            resistanceCard.setText(
                                                    "RESISTANCE\n$ "
                                                    + format(
                                                        result.resistance
                                                    )
                                            );

                                            trendCard.setText(
                                                    "TREND\n"
                                                    + result.trend
                                            );

                                            volumeCard.setText(
                                                    "VOLUME\n"
                                                    + formatVolume(
                                                        result.volume
                                                    )
                                            );

                                            rsiCard.setText(
                                                    "RSI\n"
                                                    + String.format(
                                                        Locale.US,
                                                        "%.2f",
                                                        result.rsi
                                                    )
                                            );

                                            emaCard.setText(
                                                    "EMA 20\n$ "
                                                    + format(
                                                        result.ema20
                                                    )
                                            );

                                            macdCard.setText(
                                                    "MACD\n"
                                                    + String.format(
                                                        Locale.US,
                                                        "%.2f",
                                                        result.macd
                                                    )
                                            );

                                            atrCard.setText(
                                                    "ATR\n$ "
                                                    + format(
                                                        result.atr
                                                    )
                                            );

                                            signalCard.setText(
                                                    "TECHNICAL SIGNAL\n"
                                                    + result.signal
                                                    + "\nScore: "
                                                    + result.score
                                                    + "/4"
                                            );

                                            chartView.setPrices(
                                                    result.prices
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: READY\n"
                                                    + "BTC technical analysis loaded"
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            handler.post(
                                    new Runnable() {

                                        @Override
                                        public void run() {

                                            btcCard.setText(
                                                    "₿ BTC\n"
                                                    + "Connection failed"
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: WARNING\n"
                                                    + "BTC data unavailable"
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    /*
     * GOLD DATA
     */

    private void loadGold() {

        executor.execute(
                new Runnable() {

                    @Override
                    public void run() {

                        try {

                            String json =
                                    getUrl(
                                        "https://query1.finance.yahoo.com/v8/finance/chart/GC%3DF"
                                        + "?range=6mo"
                                        + "&interval=1d"
                                    );

                            final GoldResult result =
                                    analyzeGold(json);

                            handler.post(
                                    new Runnable() {

                                        @Override
                                        public void run() {

                                            goldCard.setText(
                                                    "GOLD FUTURES\n$ "
                                                    + format(
                                                        result.price
                                                    )
                                            );
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            handler.post(
                                    new Runnable() {

                                        @Override
                                        public void run() {

                                            goldCard.setText(
                                                    "GOLD FUTURES\n"
                                                    + "Connection failed"
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    /*
     * HTTP REQUEST
     */

    private String getUrl(
            String address
    ) throws Exception {

        URL url =
                new URL(address);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                "GET"
        );

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                15000
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode < 200 ||
                responseCode >= 300) {

            throw new Exception(
                    "HTTP " + responseCode
            );
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );

        StringBuilder response =
                new StringBuilder();

        String line;

        while (
                (line = reader.readLine())
                        != null
        ) {

            response.append(line);
        }

        reader.close();

        connection.disconnect();

        return response.toString();
    }

    /*
     * BTC ANALYSIS
     */

    private BTCResult analyzeBTC(
            String json
    ) throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONArray prices =
                root.getJSONArray(
                        "prices"
                );

        JSONArray volumes =
                root.getJSONArray(
                        "total_volumes"
                );

        ArrayList<Double> priceList =
                new ArrayList<>();

        ArrayList<Double> volumeList =
                new ArrayList<>();

        ArrayList<Double> highList =
                new ArrayList<>();

        ArrayList<Double> lowList =
                new ArrayList<>();

        for (int i = 0;
             i < prices.length();
             i++) {

            double close =
                    prices
                            .getJSONArray(i)
                            .getDouble(1);

            priceList.add(close);

            if (i < volumes.length()) {

                volumeList.add(
                        volumes
                                .getJSONArray(i)
                                .getDouble(1)
                );

            } else {

                volumeList.add(0.0);
            }

            /*
             * CoinGecko market_chart
             * does not provide daily OHLC here.
             *
             * We create conservative
             * high/low proxies so ATR
             * can still operate.
             */

            double high =
                    close;

            double low =
                    close;

            if (i > 0) {

                double previous =
                        priceList.get(i - 1);

                high =
                        Math.max(
                                close,
                                previous
                        );

                low =
                        Math.min(
                                close,
                                previous
                        );
            }

            highList.add(high);
            lowList.add(low);
        }

        if (priceList.size() < 30) {

            throw new Exception(
                    "Not enough market data"
            );
        }

        double currentPrice =
                priceList.get(
                        priceList.size() - 1
                );

        double support =
                calculateSupport(
                        priceList,
                        currentPrice
                );

        double resistance =
                calculateResistance(
                        priceList,
                        currentPrice
                );

        String trend =
                calculateTrend(
                        priceList
                );

        double averageVolume =
                calculateAverageVolume(
                        volumeList
                );

        /*
         * IMPORTANT:
         * TechnicalResult is a separate
         * package-level class.
         *
         * So use:
         * TechnicalResult
         *
         * NOT:
         * TechnicalAnalyzer.TechnicalResult
         */

        TechnicalResult technical =
                TechnicalAnalyzer.analyze(
                        priceList,
                        highList,
                        lowList
                );

        int score =
                calculateTechnicalScore(
                        currentPrice,
                        trend,
                        technical
                );

        String signal =
                calculateSignal(
                        score
                );

        BTCResult result =
                new BTCResult();

        result.price =
                currentPrice;

        result.support =
                support;

        result.resistance =
                resistance;

        result.trend =
                trend;

        result.volume =
                averageVolume;

        result.rsi =
                technical.rsi;

        result.ema20 =
                technical.ema20;

        result.macd =
                technical.macd;

        result.atr =
                technical.atr;

        result.score =
                score;

        result.signal =
                signal;

        result.prices =
                priceList;

        return result;
    }

    /*
     * SUPPORT
     */

    private double calculateSupport(
            ArrayList<Double> prices,
            double currentPrice
    ) {

        int size =
                prices.size();

        int start =
                Math.max(
                        2,
                        size - 90
                );

        double nearest = 0;

        double distance =
                Double.MAX_VALUE;

        for (int i = start;
             i < size - 2;
             i++) {

            double p =
                    prices.get(i);

            boolean swingLow =
                    p < prices.get(i - 1) &&
                    p < prices.get(i - 2) &&
                    p < prices.get(i + 1) &&
                    p < prices.get(i + 2);

            if (swingLow &&
                    p < currentPrice) {

                double d =
                        currentPrice - p;

                if (d < distance) {

                    distance = d;
                    nearest = p;
                }
            }
        }

        if (nearest <= 0 ||
                nearest >= currentPrice) {

            nearest =
                    currentPrice * 0.97;
        }

        return nearest;
    }

    /*
     * RESISTANCE
     */

    private double calculateResistance(
            ArrayList<Double> prices,
            double currentPrice
    ) {

        int size =
                prices.size();

        int start =
                Math.max(
                        2,
                        size - 90
                );

        double nearest = 0;

        double distance =
                Double.MAX_VALUE;

        for (int i = start;
             i < size - 2;
             i++) {

            double p =
                    prices.get(i);

            boolean swingHigh =
                    p > prices.get(i - 1) &&
                    p > prices.get(i - 2) &&
                    p > prices.get(i + 1) &&
                    p > prices.get(i + 2);

            if (swingHigh &&
                    p > currentPrice) {

                double d =
                        p - currentPrice;

                if (d < distance) {

                    distance = d;
                    nearest = p;
                }
            }
        }

        if (nearest <= currentPrice) {

            nearest =
                    currentPrice * 1.03;
        }

        return nearest;
    }

    /*
     * TREND
     */

    private String calculateTrend(
            ArrayList<Double> prices
    ) {

        int size =
                prices.size();

        int recentPeriod =
                Math.min(
                        20,
                        size
                );

        int previousPeriod =
                Math.min(
                        50,
                        size
                );

        double recentSum = 0;
        double previousSum = 0;

        for (int i =
                size - recentPeriod;
                i < size;
                i++) {

            recentSum +=
                    prices.get(i);
        }

        int previousStart =
                Math.max(
                        0,
                        size - previousPeriod
                );

        int previousEnd =
                size - recentPeriod;

        for (int i =
                previousStart;
                i < previousEnd;
                i++) {

            previousSum +=
                    prices.get(i);
        }

        double recentSMA =
                recentSum /
                recentPeriod;

        int count =
                previousEnd -
                previousStart;

        if (count <= 0) {

            return "NEUTRAL";
        }

        double previousSMA =
                previousSum /
                count;

        if (recentSMA >
                previousSMA * 1.002) {

            return "UP";

        } else if (
                recentSMA <
                previousSMA * 0.998
        ) {

            return "DOWN";
        }

        return "NEUTRAL";
    }

    /*
     * AVERAGE VOLUME
     */

    private double calculateAverageVolume(
            ArrayList<Double> volumes
    ) {

        int start =
                Math.max(
                        0,
                        volumes.size() - 30
                );

        double sum = 0;

        int count = 0;

        for (int i = start;
             i < volumes.size();
             i++) {

            double value =
                    volumes.get(i);

            if (value > 0) {

                sum += value;
                count++;
            }
        }

        if (count == 0) {

            return 0;
        }

        return sum / count;
    }

    /*
     * TECHNICAL SCORE
     */

    private int calculateTechnicalScore(
            double currentPrice,
            String trend,
            TechnicalResult technical
    ) {

        int score = 0;

        /*
         * RSI
         */

        if (technical.rsi >= 50 &&
                technical.rsi <= 70) {

            score++;

        } else if (
                technical.rsi < 30
        ) {

            score++;

        } else if (
                technical.rsi > 70
        ) {

            score--;
        }

        /*
         * EMA
         */

        if (currentPrice >
                technical.ema20) {

            score++;

        } else if (
                currentPrice <
                technical.ema20
        ) {

            score--;
        }

        /*
         * MACD
         */

        if (technical.macd > 0) {

            score++;

        } else if (
                technical.macd < 0
        ) {

            score--;
        }

        /*
         * TREND
         */

        if (trend.equals("UP")) {

            score++;

        } else if (
                trend.equals("DOWN")
        ) {

            score--;
        }

        return score;
    }

    /*
     * SIGNAL
     */

    private String calculateSignal(
            int score
    ) {

        if (score >= 3) {

            return "BUY BIAS";

        } else if (
                score <= -3
        ) {

            return "SELL BIAS";
        }

        return "NEUTRAL";
    }

    /*
     * GOLD ANALYSIS
     */

    private GoldResult analyzeGold(
            String json
    ) throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONObject chart =
                root.getJSONObject(
                        "chart"
                );

        JSONArray results =
                chart.getJSONArray(
                        "result"
                );

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

        JSONObject q =
                quote.getJSONObject(0);

        JSONArray close =
                q.getJSONArray("close");

        double latest = 0;

        for (int i =
                close.length() - 1;
                i >= 0;
                i--) {

            if (!close.isNull(i)) {

                latest =
                        close.getDouble(i);

                break;
            }
        }

        if (latest <= 0) {

            throw new Exception(
                    "Gold price unavailable"
            );
        }

        GoldResult data =
                new GoldResult();

        data.price =
                latest;

        return data;
    }

    /*
     * NUMBER FORMAT
     */

    private String format(
            double value
    ) {

        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }

    /*
     * VOLUME FORMAT
     */

    private String formatVolume(
            double value
    ) {

        if (value >= 1000000000) {

            return String.format(
                    Locale.US,
                    "%.2fB",
                    value /
                    1000000000.0
            );

        } else if (
                value >= 1000000
        ) {

            return String.format(
                    Locale.US,
                    "%.2fM",
                    value /
                    1000000.0
            );

        } else if (
                value >= 1000
        ) {

            return String.format(
                    Locale.US,
                    "%.2fK",
                    value /
                    1000.0
            );
        }

        return String.format(
                Locale.US,
                "%.0f",
                value
        );
    }

    /*
     * BTC RESULT
     */

    private static class BTCResult {

        double price;
        double support;
        double resistance;
        double volume;

        double rsi;
        double ema20;
        double macd;
        double atr;

        int score;

        String trend;
        String signal;

        ArrayList<Double> prices;
    }

    /*
     * GOLD RESULT
     */

    private static class GoldResult {

        double price;
    }

    /*
     * PRICE CHART
     */

    private class PriceChart
            extends View {

        private final Paint linePaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private final Paint gridPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private final Paint textPaint =
                new Paint(
                        Paint.ANTI_ALIAS_FLAG
                );

        private ArrayList<Double> prices =
                new ArrayList<>();

        PriceChart(
                android.content.Context context
        ) {

            super(context);

            linePaint.setColor(
                    Color.rgb(
                            0,
                            220,
                            120
                    )
            );

            linePaint.setStrokeWidth(
                    dp(2)
            );

            linePaint.setStyle(
                    Paint.Style.STROKE
            );

            gridPaint.setColor(
                    Color.rgb(
                            45,
                            55,
                            65
                    )
            );

            gridPaint.setStrokeWidth(
                    dp(1)
            );

            textPaint.setColor(
                    Color.LTGRAY
            );

            textPaint.setTextSize(
                    dp(10)
            );
        }

        void setPrices(
                ArrayList<Double> data
        ) {

            prices =
                    new ArrayList<>(
                            data
                    );

            invalidate();
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            float width =
                    getWidth();

            float height =
                    getHeight();

            float left =
                    dp(10);

            float right =
                    width - dp(10);

            float top =
                    dp(15);

            float bottom =
                    height - dp(15);

            /*
             * GRID
             */

            for (int i = 0;
                 i <= 4;
                 i++) {

                float y =
                        top +
                        (
                            (bottom - top)
                            * i / 4f
                        );

                canvas.drawLine(
                        left,
                        y,
                        right,
                        y,
                        gridPaint
                );
            }

            /*
             * NO DATA
             */

            if (prices.size() < 2) {

                canvas.drawText(
                        "Loading chart...",
                        left,
                        height / 2,
                        textPaint
                );

                return;
            }

            /*
             * MIN + MAX
             */

            double min =
                    Double.MAX_VALUE;

            double max =
                    -Double.MAX_VALUE;

            for (double value :
                    prices) {

                if (value < min) {

                    min = value;
                }

                if (value > max) {

                    max = value;
                }
            }

            double range =
                    max - min;

            if (range <= 0) {

                range = 1;
            }

            /*
             * DRAW LINE
             */

            float previousX = 0;
            float previousY = 0;

            for (int i = 0;
                 i < prices.size();
                 i++) {

                double value =
                        prices.get(i);

                float x =
                        left +
                        (
                            (right - left)
                            * i /
                            (prices.size() - 1)
                        );

                float y =
                        bottom -
                        (float)
                        (
                            (value - min)
                            / range
                        )
                        * (bottom - top);

                if (i > 0) {

                    canvas.drawLine(
                            previousX,
                            previousY,
                            x,
                            y,
                            linePaint
                    );
                }

                previousX = x;
                previousY = y;
            }

            /*
             * CURRENT PRICE
             */

            double latest =
                    prices.get(
                            prices.size() - 1
                    );

            canvas.drawText(
                    "$ " + format(latest),
                    left,
                    top + dp(12),
                    textPaint
            );

            /*
             * LOW
             */

            canvas.drawText(
                    "Low " + format(min),
                    left,
                    bottom,
                    textPaint
            );

            /*
             * HIGH
             */

            String highText =
                    "High " + format(max);

            float textWidth =
                    textPaint.measureText(
                            highText
                    );

            canvas.drawText(
                    highText,
                    right - textWidth,
                    bottom,
                    textPaint
            );
        }
    }

    /*
     * DESTROY
     */

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
            }
