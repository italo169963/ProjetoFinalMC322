package com.stockmarket.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PriceUpdater implements Runnable {
    private StockMarket market;
    private ScheduledExecutorSerivce scheduler;
    private boolean running;

    public PriceUpdater(StockMarket market) {
        this.market = market;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.running = false;
    }

    @Override
    public void run() {
        if (running) {
            market.uptadePrices();
        }
    }

    public void start() {
        if(!running) {
            running = true;
            scheduler.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }
}