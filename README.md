# Yggdrasil Project Template

This project is a template to start your own Yggdrasil project using Gradle. Yggdrasil is based on
[Vert.x](https://vertx.io/) and this template is based on
[Vert.x Gradle Starter](https://github.com/vert-x3/vertx-gradle-starter).

## Prerequisites

* JDK 21+

## Start to hack

Create your project with:

```shell
git clone git@github.com:Interactions-HSG/yggdrasil-dev-template.git PROJECT_NAME
```

Replace `PROJECT_NAME` with the name of your project.

Follow the next steps to clear the git history and to set up a fresh project with Yggdrasil:

1. Delete the `.git` directory: `rm -rf .git`.
2. [Optional] Open the [build.gradle.kts](build.gradle.kts) file and customize `vertxVersion`. You
   can also change the `mainVerticleName` variable to use your own package name and verticle class.
3. Update the root project name with the name of your project in
   [settings.gradle.kts](settings.gradle.kts).
4. Initialize a new git repository: `git init`.
5. Add Yggdrasil as a submodule pointing to a given branch:
   `git submodule add -b main git@github.com:Interactions-HSG/yggdrasil.git`.
6. Initialize all submodules recursively: `git submodule update --init`.
7. Run `./gradlew test run`.

This last command compiles the project and runs the tests, then  it launches the application. Open your
browser to [http://localhost:8080](http://localhost:8080) and you should see a message with the
current version of Yggdrasil. The command relaunches Gradle and the application as soon as you change
something under `src/main`.

Note: The `run` Gradle task is configured to use the [conf/config.json](conf/config.json)
configuration file.

Note that the project contains an `.editorconfig` file for default editor settings.
[EditorConfig](https://editorconfig.org/) is supported by many editors and IDEs, others require
installing a plugin (see [Download a Plugin](http://editorconfig.org/#download)).

## Anatomy of the project

The project contains:

* a sample [Counter](src/main/java/ch/unisg/ics/interactions/Counter.java) hypermedia artifact
* a sample [unit test](src/main/test/org/hyperagents/yggdrasil/MainVerticleTest.java) for the main
  verticle (deployed by Yggdrasil)

## Programming hypermedia artifacts

Yggdrasil uses [CArtAgO v3.1](https://github.com/cartago-lang/cartago) for programming and running
virtual hypermedia artifacts. When an artifact is instantiated, Yggdrasil exposes an HTTP API for the artifact
instance and generates a [W3C WoT Thing Description (TD)](https://www.w3.org/TR/wot-thing-description/).
For instance, this is a Turtle representation of a TD generated for a virtual counter artifact:

```text
@prefix hmas: <https://purl.org/hmas/> .
@prefix td: <https://www.w3.org/2019/wot/td#> .
@prefix htv: <http://www.w3.org/2011/http#> .
@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .
@prefix wotsec: <https://www.w3.org/2019/wot/security#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix js: <https://www.w3.org/2019/wot/json-schema#> .
@prefix saref: <https://w3id.org/saref#> .

<http://localhost:8080/workspaces/main_workspace/artifacts/c0> a td:Thing, hmas:Artifact,
    <http://example.org/Counter>;
  td:title "c0";
  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme
    ];
  td:hasActionAffordance [ a td:ActionAffordance, <http://example.org/Increment>;
      td:name "inc";
      td:title "inc";
      td:hasForm [
          htv:methodName "POST";
          hctl:hasTarget <http://localhost:8080/workspaces/main_workspace/artifacts/c0/increment>;
          hctl:forContentType "application/json";
          hctl:hasOperationType td:invokeAction
        ]
    ];
  hmas:isContainedIn <http://localhost:8080/workspaces/main_workspace> .

<http://localhost:8080/workspaces/main_workspace> a hmas:Workspace .
```

The virtual counter exposes an action to increment the counter. A client can invoke this action by
issuing an `HTTP POST` request to the given endpoint. No payload is required to execute this action
(the content type `application/json` added here is just a default value given by the W3C
Recommendation for the WoT TD).

### Defining virtual hypermedia artifacts

To define a new virtual artifact, you need to extend the abstract class `HypermediaArtifact` and
to implement the abstract method `registerInteractionAffordances()`. This method is called by Yggdrasil
to generate TDs for instances of your artifact class. For instance, we can define a virtual `Counter`
artifact as follows:

```java
package ch.unisg.ics.interactions;

import org.hyperagents.yggdrasil.cartago.HypermediaArtifact;

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
```

In the current implementation, only action affordances can be exposed in the generated TDs. Other
useful methods provided by `HypermediaArtifact` are:
* `addMetadata(Model model)`: can be used to add additional metadata to generated TDs; the metadata
  is passed along as an RDF model with [RDF4J](https://rdf4j.org/).
* `setSecurityScheme(SecurityScheme scheme)`: can be used to add a
  [security scheme](https://www.w3.org/TR/wot-thing-description/#sec-security-vocabulary-definition).

`HypermediaArtifact` extends from CArtAgO's `Artifact` class. For a hands-on introduction to CArtAgO,
check out the [CArtAgO by Examples](http://cartago.sourceforge.net/?page_id=47) tutorial for the
[JaCaMo platform](https://github.com/jacamo-lang/jacamo).

### Registering hypermedia artifact templates

All hypermedia artifact templates have to be registered in the configuration file as _known artifacts_
(see [conf/config.json](conf/config.json)):

```json
  "environment-config" : {
    "known-artifacts" : [
      {
        "class" : "http://example.org/Counter",
        "template" : "ch.unisg.ics.interactions.Counter"
      }
    ],
    "enabled" : true
  }
```

An artifact is registered under a given URI, e.g. `http://example.org/Counter`. The artifact class URIs
are language-agnostic and add an indirection level: they are used by Yggdrasil to advertise supported
artifacts and by clients to specify the artifact class to be instantiated.

For instance, the following TD is for a workspace artifact and exposes an action affordance that can
be used to instantiate an artifact. One hypermedia artifact template can be used in this case, the
above-mentioned virtual counter:

```text
@prefix hmas: <https://purl.org/hmas/> .
@prefix td: <https://www.w3.org/2019/wot/td#> .
@prefix htv: <http://www.w3.org/2011/http#> .
@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .
@prefix wotsec: <https://www.w3.org/2019/wot/security#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix js: <https://www.w3.org/2019/wot/json-schema#> .
@prefix saref: <https://w3id.org/saref#> .

<http://localhost:8080/workspaces/main_workspace> a td:Thing, hmas:Workspace;
  td:hasActionAffordance [ a td:ActionAffordance;
      td:name "makeArtifact";
      td:hasForm [
          htv:methodName "POST";
          hctl:hasTarget <http://localhost:8080/workspaces/main_workspace/artifacts/>;
          hctl:forContentType "application/json";
          hctl:hasOperationType td:invokeAction
        ];
      td:hasInputSchema [ a js:ObjectSchema;
          js:properties [ a js:StringSchema;
              js:propertyName "artifactClass";
              js:enum <http://example.org/Counter>
            ], [ a js:ArraySchema;
              js:propertyName "initParams"
            ], [ a js:StringSchema;
              js:propertyName "artifactName"
            ];
          js:required "artifactClass", "artifactName"
        ]
    ], [ a td:ActionAffordance;
      td:name "joinWorkspace";
      (...)
```

Following this TD specification, a client can instantiate a counter artifact by issuing the following
`HTTP POST` request:

```shell
curl -X POST 'http://localhost:8080/environments/env1/workspaces/wksp1/artifacts/' \
-H 'X-Agent-WebID: http://andreiciortea.ro/#me' \
-H 'Content-Type: application/json' \
-d '{
    "artifactClass" : "http://example.org/Counter",
    "artifactName" : "c1",
    "initParams": [5]
}'
```

All HTTP requests have to include an `X-Agent-WebID` header field to indicate the agent on behalf
of whom the HTTP request was issued (e.g., the agent creating an artifact or performing an action).
In the current prototype implementation, this is meant as a substitute for implementing an actual
authentication protocol (e.g., the WebID authentication protocol).

*Note:* in the current implementation, the WebIDs of agents should have the form
`http://localhost:8080/agents/<agent_name>`.

## Building the project

To build the project, just use:

```shell
./gradlew build
```

It generates a _fat-jar_ in the `build/libs` directory. To launch the app with a configuration file:

```shell
java -jar build/libs/[NAME_OF_YOUR_PROJECT]-fat.jar -conf conf/config.json
```
