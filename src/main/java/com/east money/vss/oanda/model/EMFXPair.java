package com.eastmoney.vss.oanda.model;

import com.oanda.fxtrade.api.FXPair;

/**
 * Created by keyu on 2016/1/11.
 */
public class EMFXPair implements FXPair {
   private  String pair;
   private  boolean isHalted;

    public EMFXPair(){

    }
    public EMFXPair(String pair, boolean isHalted) {
        this.pair = pair;
        this.isHalted = isHalted;
    }

    @Override
    public int compareTo(FXPair fxPair) {
        return 0;
    }

    @Override
    public String getBase() {
        return null;
    }

    @Override
    public FXPair getInverse() {
        return null;
    }



    @Override
    public String getQuote() {
        return null;
    }

    @Override
    public boolean isHalted() {
        return isHalted;
    }

    public void setIsHalted(boolean isHalted) {
        this.isHalted = isHalted;
    }

    @Override
    public void setBase(String s) {

    }

    @Override
    public String getPair() {
        return pair;
    }

    @Override
    public void setPair(String pair) {
        this.pair = pair;
    }

    @Override
    public void setQuote(String s) {

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
    @Override
    public  Object clone() {
        return null;
    }
}
