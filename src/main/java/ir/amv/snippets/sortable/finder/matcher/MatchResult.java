package ir.amv.snippets.sortable.finder.matcher;

/**
 * Created by AMV on 5/1/2016.
 */
public class MatchResult {

    private Double resemblanceScore;

    public MatchResult() {
    }

    public MatchResult(Double resemblanceScore) {
        this.resemblanceScore = resemblanceScore;
    }

    public Double getResemblanceScore() {
        return resemblanceScore;
    }

    public void setResemblanceScore(Double resemblanceScore) {
        this.resemblanceScore = resemblanceScore;
    }
}
