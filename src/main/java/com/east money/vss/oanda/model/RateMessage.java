package com.eastmoney.vss.oanda.model;

import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.FXTick;

/**
 * Created by keyu on 2016/1/9.
 */
public class RateMessage  implements   FXTick{

    private  FXPair FXPair;
    private double ask;
    private double bid;
    private double mean;
    private long  timestamp;


    public RateMessage(){

    }

    public RateMessage(com.oanda.fxtrade.api.FXPair FXPair, double ask, double bid, double mean, long timestamp) {
        this.FXPair = FXPair;
        this.ask = ask;
        this.bid = bid;
        this.mean = mean;
        this.timestamp = timestamp;
    }

    @Override
    public FXPair getPair() {
        return FXPair;
    }

    public void setPair(FXPair FXPair) {
        this.FXPair = FXPair;
    }



    @Override
    public FXTick getInverse() {
        return null;
    }

    public com.oanda.fxtrade.api.FXPair getFXPair() {

        return FXPair;
    }

    public void setFXPair(com.oanda.fxtrade.api.FXPair FXPair) {
        this.FXPair = FXPair;
    }

    @Override
    public double getAsk() {
        return ask;
    }

    @Override
    public void setAsk(double ask) {
        this.ask = ask;
    }

    @Override
    public double getBid() {
        return bid;
    }

    @Override
    public void setBid(double bid) {
        this.bid = bid;
    }

    @Override
    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getMaxUnits() {
        return 0;
    }

    @Override
    public void setMaxUnits(long l) {

    }

    @Override
    public  Object clone() {
        return null;
    }
}
