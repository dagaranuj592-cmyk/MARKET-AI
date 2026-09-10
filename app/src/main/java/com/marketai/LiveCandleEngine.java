package com.marketai;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LiveCandleEngine {

    public static class Candle {

        public long openTime;
        public long closeTime;

        public double open;
        public double high;
        public double low;
        public double close;
        public double volume;

        public boolean closed;

        public Candle(
                long openTime,
                long closeTime,
                double open,
                double high,
                double low,
                double close,
                double volume,
                boolean closed
        ) {
            this.openTime = openTime;
            this.closeTime = closeTime;

            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;

            this.closed = closed;
        }
    }

    public interface Listener {

        void onCandleUpdate(
                String interval,
                Candle candle,
                List<Candle> candles
        );

        void onConnectionChanged(
                String interval,
                boolean connected
        );

        void onError(
                String interval,
                String message
        );
    }

    private final OkHttpClient client;
    private final Listener listener;

    private WebSocket socket5m;
    private WebSocket socket15m;

    private boolean manuallyStopped = false;

    private final List<Candle> candles5m =
            new ArrayList<>();

    private final List<Candle> candles15m =
            new ArrayList<>();

    private static final int MAX_CANDLES = 500;

    public LiveCandleEngine(Listener listener) {

        this.listener = listener;

        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    // =========================================================
    // START
    // =========================================================

    public void start() {

        manuallyStopped = false;

        startInterval("5m");
        startInterval("15m");
    }

    // =========================================================
    // START SINGLE INTERVAL
    // =========================================================

    private void startInterval(String interval) {

        String stream;

        if (interval.equals("5m")) {

            stream =
                    "wss://stream.binance.com:9443/ws/btcusdt@kline_5m";

        } else {

            stream =
                    "wss://stream.binance.com:9443/ws/btcusdt@kline_15m";
        }

        Request request =
                new Request.Builder()
                        .url(stream)
                        .build();

        WebSocket socket =
                client.newWebSocket(
                        request,
                        createListener(interval)
                );

        if (interval.equals("5m")) {

            socket5m = socket;

        } else {

            socket15m = socket;
        }
    }

    // =========================================================
    // WEBSOCKET LISTENER
    // =========================================================

    private WebSocketListener createListener(
            final String interval
    ) {

        return new WebSocketListener() {

            @Override
            public void onOpen(
                    WebSocket webSocket,
                    okhttp3.Response response
            ) {

                if (listener != null) {

                    listener.onConnectionChanged(
                            interval,
                            true
                    );
                }
            }

            @Override
            public void onMessage(
                    WebSocket webSocket,
                    String text
            ) {

                try {

                    JSONObject root =
                            new JSONObject(text);

                    JSONObject kline =
                            root.getJSONObject("k");

                    long openTime =
                            kline.getLong("t");

                    long closeTime =
                            kline.getLong("T");

                    double open =
                            Double.parseDouble(
                                    kline.getString("o")
                            );

                    double high =
                            Double.parseDouble(
                                    kline.getString("h")
                            );

                    double low =
                            Double.parseDouble(
                                    kline.getString("l")
                            );

                    double close =
                            Double.parseDouble(
                                    kline.getString("c")
                            );

                    double volume =
                            Double.parseDouble(
                                    kline.getString("v")
                            );

                    boolean closed =
                            kline.getBoolean("x");

                    Candle candle =
                            new Candle(
                                    openTime,
                                    closeTime,
                                    open,
                                    high,
                                    low,
                                    close,
                                    volume,
                                    closed
                            );

                    updateCandle(
                            interval,
                            candle
                    );

                } catch (Exception e) {

                    if (listener != null) {

                        listener.onError(
                                interval,
                                "Candle parse error"
                        );
                    }
                }
            }

            @Override
            public void onClosed(
                    WebSocket webSocket,
                    int code,
                    String reason
            ) {

                if (listener != null) {

                    listener.onConnectionChanged(
                            interval,
                            false
                    );
                }

                reconnect(interval);
            }

            @Override
            public void onFailure(
                    WebSocket webSocket,
                    Throwable t,
                    okhttp3.Response response
            ) {

                if (listener != null) {

                    listener.onConnectionChanged(
                            interval,
                            false
                    );

                    listener.onError(
                            interval,
                            "Live candle connection lost"
                    );
                }

                reconnect(interval);
            }
        };
    }

    // =========================================================
    // UPDATE CANDLE
    // =========================================================

    private synchronized void updateCandle(
            String interval,
            Candle candle
    ) {

        List<Candle> targetList;

        if (interval.equals("5m")) {

            targetList = candles5m;

        } else {

            targetList = candles15m;
        }

        boolean replaced = false;

        if (!targetList.isEmpty()) {

            Candle last =
                    targetList.get(
                            targetList.size() - 1
                    );

            if (last.openTime == candle.openTime) {

                targetList.set(
                        targetList.size() - 1,
                        candle
                );

                replaced = true;
            }
        }

        if (!replaced) {

            targetList.add(candle);
        }

        while (
                targetList.size() >
                        MAX_CANDLES
        ) {

            targetList.remove(0);
        }

        if (listener != null) {

            listener.onCandleUpdate(
                    interval,
                    candle,
                    new ArrayList<>(targetList)
            );
        }
    }

    // =========================================================
    // RECONNECT
    // =========================================================

    private void reconnect(
            final String interval
    ) {

        if (manuallyStopped) {
            return;
        }

        new Thread(() -> {

            try {

                Thread.sleep(3000);

            } catch (InterruptedException ignored) {
            }

            if (!manuallyStopped) {

                startInterval(interval);
            }

        }).start();
    }

    // =========================================================
    // GET 5M CANDLES
    // =========================================================

    public synchronized List<Candle> get5mCandles() {

        return new ArrayList<>(
                candles5m
        );
    }

    // =========================================================
    // GET 15M CANDLES
    // =========================================================

    public synchronized List<Candle> get15mCandles() {

        return new ArrayList<>(
                candles15m
        );
    }

    // =========================================================
    // GET LATEST 5M
    // =========================================================

    public synchronized Candle getLatest5m() {

        if (candles5m.isEmpty()) {
            return null;
        }

        return candles5m.get(
                candles5m.size() - 1
        );
    }

    // =========================================================
    // GET LATEST 15M
    // =========================================================

    public synchronized Candle getLatest15m() {

        if (candles15m.isEmpty()) {
            return null;
        }

        return candles15m.get(
                candles15m.size() - 1
        );
    }

    // =========================================================
    // STOP
    // =========================================================

    public synchronized void stop() {

        manuallyStopped = true;

        if (socket5m != null) {

            socket5m.close(
                    1000,
                    "Stopped by app"
            );

            socket5m = null;
        }

        if (socket15m != null) {

            socket15m.close(
                    1000,
                    "Stopped by app"
            );

            socket15m = null;
        }

        if (listener != null) {

            listener.onConnectionChanged(
                    "5m",
                    false
            );

            listener.onConnectionChanged(
                    "15m",
                    false
            );
        }
    }

    // =========================================================
    // CLEAR DATA
    // =========================================================

    public synchronized void clear() {

        candles5m.clear();
        candles15m.clear();
    }
      }
