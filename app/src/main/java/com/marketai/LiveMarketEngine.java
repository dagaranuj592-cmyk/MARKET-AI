package com.marketai;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;

public class LiveMarketEngine {

    public interface LiveMarketListener {

        void onPriceUpdate(String symbol, double price);

        void onConnectionChanged(boolean connected);

        void onError(String message);
    }

    private final OkHttpClient client;
    private final LiveMarketListener listener;

    private okhttp3.WebSocket webSocket;
    private boolean manuallyStopped = false;

    public LiveMarketEngine(LiveMarketListener listener) {

        this.listener = listener;

        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void startBTC() {

        manuallyStopped = false;

        String url =
                "wss://stream.binance.com:9443/ws/btcusdt@trade";

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(
                request,
                new okhttp3.WebSocketListener() {

                    @Override
                    public void onOpen(
                            okhttp3.WebSocket webSocket,
                            okhttp3.Response response) {

                        if (listener != null) {
                            listener.onConnectionChanged(true);
                        }
                    }

                    @Override
                    public void onMessage(
                            okhttp3.WebSocket webSocket,
                            String text) {

                        try {

                            org.json.JSONObject json =
                                    new org.json.JSONObject(text);

                            String symbol =
                                    json.optString("s", "BTCUSDT");

                            String priceText =
                                    json.optString("p", "0");

                            double price =
                                    Double.parseDouble(priceText);

                            if (listener != null) {
                                listener.onPriceUpdate(
                                        symbol,
                                        price
                                );
                            }

                        } catch (Exception e) {

                            if (listener != null) {
                                listener.onError(
                                        "Live data parse error"
                                );
                            }
                        }
                    }

                    @Override
                    public void onClosed(
                            okhttp3.WebSocket webSocket,
                            int code,
                            String reason) {

                        if (listener != null) {
                            listener.onConnectionChanged(false);
                        }

                        reconnect();
                    }

                    @Override
                    public void onFailure(
                            okhttp3.WebSocket webSocket,
                            Throwable t,
                            okhttp3.Response response) {

                        if (listener != null) {
                            listener.onConnectionChanged(false);
                            listener.onError(
                                    "Live connection lost"
                            );
                        }

                        reconnect();
                    }
                }
        );
    }

    private void reconnect() {

        if (manuallyStopped) {
            return;
        }

        new Thread(() -> {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }

            if (!manuallyStopped) {
                startBTC();
            }

        }).start();
    }

    public void stop() {

        manuallyStopped = true;

        if (webSocket != null) {

            webSocket.close(
                    1000,
                    "Stopped by app"
            );

            webSocket = null;
        }

        if (listener != null) {
            listener.onConnectionChanged(false);
        }
    }
}
