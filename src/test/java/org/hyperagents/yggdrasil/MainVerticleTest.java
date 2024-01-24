package org.hyperagents.yggdrasil;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hyperagents.yggdrasil.MainVerticle;

@ExtendWith(VertxExtension.class)
class MainVerticleTest {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(MainVerticle.class.getCanonicalName(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Check that the server has started")
  void checkServerHasStarted(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertEquals(200, response.statusCode());
        assertTrue(response.body().length() > 0);
        assertTrue(response.body().contains("@prefix hmas: <https://purl.org/hmas/> .\n" +
          "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
          "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
          "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
          "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
          "@prefix dct: <http://purl.org/dc/terms/> .\n" +
          "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
          "@prefix saref: <https://w3id.org/saref#> .\n" +
          "\n" +
          "<http://localhost:8080/> a td:Thing, hmas:HypermediaMASPlatform;\n" +
          "  td:title \"yggdrasil\";\n" +
          "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme\n" +
          "    ];\n" +
          "  td:hasActionAffordance [ a td:ActionAffordance;\n" +
          "      td:name \"createWorkspace\";\n" +
          "      td:hasForm [\n" +
          "          htv:methodName \"POST\";\n" +
          "          hctl:hasTarget <http://localhost:8080/workspaces/>;\n" +
          "          hctl:forContentType \"application/json\";\n" +
          "          hctl:hasOperationType td:invokeAction\n" +
          "        ]\n" +
          "    ] ."));
        testContext.completeNow();
      })));
  }
}
