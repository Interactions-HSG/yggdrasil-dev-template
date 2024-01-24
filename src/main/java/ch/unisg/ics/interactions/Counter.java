package ch.unisg.ics.interactions;

import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;

import cartago.OPERATION;
import cartago.ObsProperty;

public class Counter extends HypermediaArtifact {

  public void init(int initValue) {
    defineObsProperty("count", initValue);
  }

  @OPERATION
  public void inc() {
    ObsProperty prop = getObsProperty("count");
    prop.updateValue(prop.intValue() + 1);
  }

  @Override
  protected void registerInteractionAffordances() {
    // Register one action affordance with an input schema
    registerActionAffordance("http://example.org/Increment", "inc", "/increment");
  }
}
