package com.bigbigwork.vo;

import java.io.Serializable;

public class Configure implements Serializable {
    private String startTime;
    private String stopTime;

    private Integer period;
    private Integer errorPeriod;
    private Integer sleepTime;
    private Integer requests;

    private String startAPPScript;
    private String stopAPPScript;
    private String findAPPScript;
    private String findProxyScript;

    public Integer getErrorPeriod() {
        return errorPeriod;
    }

    public void setErrorPeriod(Integer errorPeriod) {
        this.errorPeriod = errorPeriod;
    }

    public String getStartHour(){
        return startTime.split(":")[0];
    }
    public String getStartMinute(){
        return startTime.split(":")[1];
    }

    public String getStopHour(){
        return stopTime.split(":")[0];
    }

    public String getStopMinute(){
        return stopTime.split(":")[1];
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public Integer getRequests() {
        return requests;
    }

    public void setRequests(Integer requests) {
        this.requests = requests;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    public String getStartAPPScript() {
        return startAPPScript;
    }

    public void setStartAPPScript(String startAPPScript) {
        this.startAPPScript = startAPPScript;
    }

    public String getStopAPPScript() {
        return stopAPPScript;
    }

    public void setStopAPPScript(String stopAPPScript) {
        this.stopAPPScript = stopAPPScript;
    }

    public String getFindAPPScript() {
        return findAPPScript;
    }

    public void setFindAPPScript(String findAPPScript) {
        this.findAPPScript = findAPPScript;
    }

    public String getFindProxyScript() {
        return findProxyScript;
    }

    public void setFindProxyScript(String findProxyScript) {
        this.findProxyScript = findProxyScript;
    }

    @Override
    public String toString() {
        return "Configure{" +
                "startTime='" + startTime + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", period=" + period +
                ", errorPeriod=" + errorPeriod +
                ", sleepTime=" + sleepTime +
                ", requests=" + requests +
                ", startAPPScript='" + startAPPScript + '\'' +
                ", stopAPPScript='" + stopAPPScript + '\'' +
                ", findAPPScript='" + findAPPScript + '\'' +
                ", findProxyScript='" + findProxyScript + '\'' +
                '}';
    }
}
