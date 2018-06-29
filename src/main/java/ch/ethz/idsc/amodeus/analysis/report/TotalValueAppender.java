package ch.ethz.idsc.amodeus.analysis.report;

import java.util.Map;

public interface TotalValueAppender {
	public Map<TotalValueIdentifier, String> getTotalValues();
}
