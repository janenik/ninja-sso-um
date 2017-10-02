package models.sso.admin;

/**
 * Application statistics entry.
 */
public class ApplicationStatisticsEntry {

    private String name;
    private Double max;
    private Double min;
    private Double percentile50th;
    private Double percentile75th;
    private Double percentile95th;
    private Double percentile98th;
    private Double percentile99th;
    private Double percentile999th;
    private Double stdDev;
    private Double mean;
    private Double fifteenMinuteRate;
    private Double fiveMinuteRate;
    private Double oneMinuteRate;
    private Double meanRate;
    private String rateUnit;
    private String durationUnit;
    private Long count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getPercentile50th() {
        return percentile50th;
    }

    public void setPercentile50th(Double percentile50th) {
        this.percentile50th = percentile50th;
    }

    public Double getPercentile75th() {
        return percentile75th;
    }

    public void setPercentile75th(Double percentile75th) {
        this.percentile75th = percentile75th;
    }

    public Double getPercentile95th() {
        return percentile95th;
    }

    public void setPercentile95th(Double percentile95th) {
        this.percentile95th = percentile95th;
    }

    public Double getPercentile98th() {
        return percentile98th;
    }

    public void setPercentile98th(Double percentile98th) {
        this.percentile98th = percentile98th;
    }

    public Double getPercentile99th() {
        return percentile99th;
    }

    public void setPercentile99th(Double percentile99th) {
        this.percentile99th = percentile99th;
    }

    public Double getPercentile999th() {
        return percentile999th;
    }

    public void setPercentile999th(Double percentile999th) {
        this.percentile999th = percentile999th;
    }

    public Double getStdDev() {
        return stdDev;
    }

    public void setStdDev(Double stdDev) {
        this.stdDev = stdDev;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getFifteenMinuteRate() {
        return fifteenMinuteRate;
    }

    public void setFifteenMinuteRate(Double fifteenMinuteRate) {
        this.fifteenMinuteRate = fifteenMinuteRate;
    }

    public Double getFiveMinuteRate() {
        return fiveMinuteRate;
    }

    public void setFiveMinuteRate(Double fiveMinuteRate) {
        this.fiveMinuteRate = fiveMinuteRate;
    }

    public Double getMeanRate() {
        return meanRate;
    }

    public void setMeanRate(Double meanRate) {
        this.meanRate = meanRate;
    }

    public Double getOneMinuteRate() {
        return oneMinuteRate;
    }

    public void setOneMinuteRate(Double oneMinuteRate) {
        this.oneMinuteRate = oneMinuteRate;
    }

    public String getRateUnit() {
        return rateUnit;
    }

    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
