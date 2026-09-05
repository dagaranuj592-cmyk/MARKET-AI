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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private TextView btcCard;
    private TextView goldCard;
    private TextView supportCard;
    private TextView resistanceCard;
    private TextView trendCard;
    private TextView volumeCard;
    private TextView status;

    private PriceChart chartView;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private int dp(float value) {
        return (int) (
                value *
                getResources().getDisplayMetrics().density
                + 0.5f
        );
    }

    private TextView makeText(
            String value,
            float size,
            boolean bold
    ) {
        TextView tv = new TextView(this);

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
                        15,
                        true
                );

        tv.setBackgroundColor(
                Color.rgb(24, 31, 40)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(95),
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

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(12),
                dp(18),
                dp(12),
                dp(12)
        );

        root.setBackgroundColor(
                Color.rgb(8, 12, 18)
        );

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
                        "GOLD",
                        "Loading..."
                );

        assets.addView(btcCard);
        assets.addView(goldCard);

        root.addView(assets);

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

        TextView chartTitle =
                makeText(
                        "PRICE CHART",
                        17,
                        true
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

        status =
                makeText(
                        "ENGINE STATUS: READY\n"
                        + "Waiting for market data...",
                        14,
                        false
                );

        status.setPadding(
                0,
                dp(18),
                0,
                0
        );

        root.addView(status);

        setContentView(root);

        loadAllData();
    }

    private void loadAllData() {

        status.setText(
                "ENGINE STATUS: RUNNING\n"
                + "Downloading market data..."
        );

        loadBTC();
        loadGold();
    }

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
                                                    "SUPPORT\n"
                                                    + format(
                                                        result.support
                                                    )
                                            );

                                            resistanceCard.setText(
                                                    "RESISTANCE\n"
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

                                            chartView.setPrices(
                                                    result.prices
                                            );

                                            status.setText(
                                                    "ENGINE STATUS: READY\n"
                                                    + "BTC live + chart data loaded"
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
     * =====================================================
     * BTC ANALYSIS ENGINE
     * =====================================================
     */

    private BTCResult analyzeBTC(
            String json
    ) throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONArray prices =
                root.getJSONArray("prices");

        JSONArray volumes =
                root.getJSONArray(
                        "total_volumes"
                );

        ArrayList<Double> priceList =
                new ArrayList<>();

        ArrayList<Double> volumeList =
                new ArrayList<>();

        for (int i = 0;
             i < prices.length();
             i++) {

            double price =
                    prices
                            .getJSONArray(i)
                            .getDouble(1);

            priceList.add(price);

            if (i < volumes.length()) {

                volumeList.add(
                        volumes
                                .getJSONArray(i)
                                .getDouble(1)
                );

            } else {

                volumeList.add(0.0);
            }
        }

        int size =
                priceList.size();

        if (size < 20) {

            throw new Exception(
                    "Not enough market data"
            );
        }

        double currentPrice =
                priceList.get(size - 1);

        /*
         * -------------------------------------------------
         * RECENT SWING LEVELS
         * -------------------------------------------------
         */

        int lookback =
                Math.min(
                        90,
                        size
                );

        int start =
                size - lookback;

        ArrayList<Double>
                supportCandidates =
                new ArrayList<>();

        ArrayList<Double>
                resistanceCandidates =
                new ArrayList<>();

        for (int i = start + 2;
             i < size - 2;
             i++) {

            double p =
                    priceList.get(i);

            double left1 =
                    priceList.get(i - 1);

            double left2 =
                    priceList.get(i - 2);

            double right1 =
                    priceList.get(i + 1);

            double right2 =
                    priceList.get(i + 2);

            boolean swingLow =
                    p < left1 &&
                    p < left2 &&
                    p < right1 &&
                    p < right2;

            boolean swingHigh =
                    p > left1 &&
                    p > left2 &&
                    p > right1 &&
                    p > right2;

            double distance =
                    Math.abs(
                            p - currentPrice
                    ) / currentPrice;

            /*
             * Ignore extremely distant levels.
             */

            if (distance <= 0.15) {

                if (swingLow &&
                        p < currentPrice) {

                    supportCandidates.add(p);
                }

                if (swingHigh &&
                        p > currentPrice) {

                    resistanceCandidates.add(p);
                }
            }
        }

        /*
         * -------------------------------------------------
         * NEAREST SUPPORT / RESISTANCE
         * -------------------------------------------------
         */

        double support =
                findNearestLevel(
                        supportCandidates,
                        currentPrice,
                        true
                );

        double resistance =
                findNearestLevel(
                        resistanceCandidates,
                        currentPrice,
                        false
                );

        /*
         * -------------------------------------------------
         * FALLBACK
         * -------------------------------------------------
         */

        if (support <= 0 ||
                support >= currentPrice) {

            support =
                    currentPrice * 0.97;
        }

        if (resistance <= currentPrice) {

            resistance =
                    currentPrice * 1.03;
        }

        /*
         * -------------------------------------------------
         * TREND
         * -------------------------------------------------
         */

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
                    priceList.get(i);
        }

        int previousStart =
                Math.max(
                        0,
                        size - previousPeriod
                );

        int previousEnd =
                Math.max(
                        0,
                        size - recentPeriod
                );

        for (int i =
                previousStart;
                i < previousEnd;
                i++) {

            previousSum +=
                    priceList.get(i);
        }

        double sma20 =
                recentSum /
                recentPeriod;

        int previousCount =
                previousEnd -
                previousStart;

        double previousSMA;

        if (previousCount > 0) {

            previousSMA =
                    previousSum /
                    previousCount;

        } else {

            previousSMA =
                    sma20;
        }

        String trend =
                "NEUTRAL";

        if (sma20 > previousSMA) {

            trend = "UP";

        } else if (sma20 < previousSMA) {

            trend = "DOWN";
        }

        /*
         * -------------------------------------------------
         * VOLUME
         * -------------------------------------------------
         */

        double volumeSum = 0;

        int volumeCount = 0;

        int volumeStart =
                Math.max(
                        0,
                        size - 30
                );

        for (int i =
                volumeStart;
                i < volumeList.size();
                i++) {

            double volume =
                    volumeList.get(i);

            if (volume > 0) {

                volumeSum += volume;
                volumeCount++;
            }
        }

        double averageVolume =
                volumeCount > 0
                        ? volumeSum /
                          volumeCount
                        : 0;

        /*
         * -------------------------------------------------
         * RESULT
         * -------------------------------------------------
         */

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

        result.prices =
                priceList;

        return result;
    }

    /*
     * Find the closest valid level to current price.
     */

    private double findNearestLevel(
            ArrayList<Double> candidates,
            double currentPrice,
            boolean support
    ) {

        if (candidates == null ||
                candidates.size() == 0) {

            return 0;
        }

        double best =
                0;

        double bestDistance =
                Double.MAX_VALUE;

        for (double level :
                candidates) {

            if (support) {

                if (level >= currentPrice) {
                    continue;
                }

            } else {

                if (level <= currentPrice) {
                    continue;
                }
            }

            double distance =
                    Math.abs(
                            currentPrice -
                            level
                    );

            if (distance <
                    bestDistance) {

                bestDistance =
                        distance;

                best =
                        level;
            }
        }

        return best;
    }

    /*
     * =====================================================
     * GOLD
     * =====================================================
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
                q.getJSONArray(
                        "close"
                );

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

        GoldResult data =
                new GoldResult();

        data.price =
                latest;

        return data;
    }

    /*
     * =====================================================
     * FORMATTERS
     * =====================================================
     */

    private String format(
            double value
    ) {

        return String.format(
                "%,.2f",
                value
        );
    }

    private String formatVolume(
            double value
    ) {

        if (value >= 1000000000) {

            return String.format(
                    "%.2fB",
                    value /
                    1000000000.0
            );

        } else if (value >= 1000000) {

            return String.format(
                    "%.2fM",
                    value /
                    1000000.0
            );

        } else if (value >= 1000) {

            return String.format(
                    "%.2fK",
                    value /
                    1000.0
            );
        }

        return String.format(
                "%.0f",
                value
        );
    }

    /*
     * =====================================================
     * DATA CLASSES
     * =====================================================
     */

    private static class BTCResult {

        double price;
        double support;
        double resistance;
        double volume;

        String trend;

        ArrayList<Double> prices;
    }

    private static class GoldResult {

        double price;
    }

    /*
     * =====================================================
     * PRICE CHART
     * =====================================================
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
             * Grid
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

            if (prices.size() < 2) {

                canvas.drawText(
                        "Loading chart...",
                        left,
                        height / 2,
                        textPaint
                );

                return;
            }

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
             * Latest price
             */

            double latest =
                    prices.get(
                            prices.size() - 1
                    );

            canvas.drawText(
                    "$ "
                    + format(latest),
                    left,
                    top + dp(12),
                    textPaint
            );

            /*
             * Low
             */

            canvas.drawText(
                    "Low "
                    + format(min),
                    left,
                    bottom,
                    textPaint
            );

            /*
             * High
             */

            String highText =
                    "High "
                    + format(max);

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

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
            }
