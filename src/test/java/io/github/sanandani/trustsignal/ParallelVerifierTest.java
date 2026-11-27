package io.github.sanandani.trustsignal;
import io.github.sanandani.trustsignal.model.*;
import io.github.sanandani.trustsignal.verify.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ParallelVerifierTest {
 private SignedClaim claim(String id){return new SignedClaim(new Claim(id,"synthetic","statement",Instant.EPOCH,Instant.MAX),new byte[0],"key");}
 @Test void rejectsInvalidConcurrencyOptions(){
  assertThrows(IllegalArgumentException.class,()->new ParallelVerifier(c->new SignalOutcome.Verified(),0,Duration.ofSeconds(1)));
  assertThrows(IllegalArgumentException.class,()->new ParallelVerifier(c->new SignalOutcome.Verified(),1,Duration.ZERO));
 }
 @Test void resultOrderMatchesInputOrder(){
  var parallel=new ParallelVerifier(c->new SignalOutcome.Inferred(c.claim().id(),.5),2,Duration.ofSeconds(1));
  var outcomes=parallel.verifyAll(List.of(claim("first"),claim("second")));
  assertEquals("first",((SignalOutcome.Inferred)outcomes.getFirst()).basis());
  assertEquals("second",((SignalOutcome.Inferred)outcomes.getLast()).basis());
 }
 @Test void emptyBatchIsEmpty(){assertTrue(new ParallelVerifier(c->new SignalOutcome.Verified(),1,Duration.ofSeconds(1)).verifyAll(List.of()).isEmpty());}
}
