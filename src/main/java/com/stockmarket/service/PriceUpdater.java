package com.stockmarket.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PriceUpdater implements Runnable {
    private StockMarket market;
    private ScheduledExecutorService scheduler;
    private AtomicBoolean running;
    private long updateInterval;
    private TimeUnit timeUnit;

    public PriceUpdater(StockMarket market) {
        this.market = market;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.running = new AtomicBoolean(false);
        this.updateInterval = 3;
        this.timeUnit = TimeUnit.SECONDS;
    }

    public PriceUpdater(StockMarket market, long updateInterval, TimeUnit timeUnit) {
        this(market);
        this.updateInterval = updateInterval;
        this.timeUnit = timeUnit;
    }

    @Override
    public void run() {
        if (running.get()) {
            try {
                market.updatePrices();
            } catch (Exception e) {
                System.err.println("Erro ao atualizar preços: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (!running.getAndSet(true)) {
            scheduler.scheduleAtFixedRate(this, 0, updateInterval, timeUnit);
            System.out.println("PriceUpdater iniciado com intervalo de " + 
                updateInterval + " " + timeUnit.name().toLowerCase());
        }
    }

    public void stop() {
        if (running.getAndSet(false)) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("PriceUpdater parado");
        }
    }

    public boolean isRunning() {
        return running.get() && !scheduler.isShutdown();
    }

    public void restart() {
        if (isRunning()) {
            stop();
        }
        // Recriar scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();
        start();
    }

    public void setUpdateInterval(long interval, TimeUnit unit) {
        if (isRunning()) {
            stop();
            this.updateInterval = interval;
            this.timeUnit = unit;
            start();
        } else {
            this.updateInterval = interval;
            this.timeUnit = unit;
        }
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}