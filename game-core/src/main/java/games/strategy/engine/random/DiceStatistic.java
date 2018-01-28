package games.strategy.engine.random;

import java.io.Serializable;

public class DiceStatistic implements Serializable {
  private static final long serialVersionUID = -1422839840110240480L;
  private final double m_average;
  private final int m_total;
  private final double m_median;
  private final double m_stdDeviation;
  private final double m_variance;

  DiceStatistic(final double average, final int total, final double median, final double stdDeviation,
      final double variance) {
    m_average = average;
    m_total = total;
    m_median = median;
    m_stdDeviation = stdDeviation;
    m_variance = variance;
  }

  public double getAverage() {
    return m_average;
  }

  public int getTotal() {
    return m_total;
  }

  public double getMedian() {
    return m_median;
  }

  public double getStdDeviation() {
    return m_stdDeviation;
  }

  public double getVariance() {
    return m_variance;
  }
}
