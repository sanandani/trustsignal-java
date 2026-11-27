package io.github.sanandani.trustsignal;
import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.score.AceUScorer;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ScorerEdgesTest {
 private Map<Dimension,Double> weights(){return Map.of(Dimension.AUTHENTICITY,1d,Dimension.CREDIBILITY,0d,Dimension.EMPATHY,1d,Dimension.UNIQUENESS,1d);}
 @Test void rejectsInvalidWeightsAndMissingDimensions(){
  assertThrows(IllegalArgumentException.class,()->new AceUScorer(Map.of(Dimension.AUTHENTICITY,-1d)));
  assertThrows(IllegalArgumentException.class,()->new AceUScorer(Map.of(Dimension.AUTHENTICITY,1d,Dimension.CREDIBILITY,1d,Dimension.EMPATHY,1d)));
 }
 @Test void inferredScoresAreDiscountedAndZeroWeightHasZeroConfidence(){
  var scorer=new AceUScorer(weights());
  var item=new AceUScorer.SignalEvidence("i",new SignalOutcome.Inferred("basis",.5),Map.of(Dimension.AUTHENTICITY,1d,Dimension.CREDIBILITY,1d),"synthetic");
  var result=scorer.score(List.of(item)).profile().dimensions();
  assertEquals(1,result.get(Dimension.AUTHENTICITY).value());
  assertEquals(.5,result.get(Dimension.AUTHENTICITY).confidence());
  assertEquals(0,result.get(Dimension.CREDIBILITY).confidence());
 }
 @Test void profileAndScoresAreImmutableAndRequireAllDimensions(){
  var map=new EnumMap<Dimension,DimensionScore>(Dimension.class);
  for(var d:Dimension.values())map.put(d,new DimensionScore(d,0,0,List.of()));
  var profile=new AceUProfile(map); map.clear(); assertEquals(4,profile.dimensions().size());
  assertThrows(UnsupportedOperationException.class,()->profile.dimensions().clear());
  assertThrows(IllegalArgumentException.class,()->new DimensionScore(Dimension.EMPATHY,2,0,List.of()));
 }
 @Test void constructorsValidateConfidenceAndContribution(){
  assertThrows(IllegalArgumentException.class,()->new SignalOutcome.Inferred("x",2));
  assertThrows(IllegalArgumentException.class,()->new AceUScorer.SignalEvidence("x",new SignalOutcome.Verified(),Map.of(Dimension.EMPATHY,2d),"y"));
 }
}
