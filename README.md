# Yggdrasil Project Template

This project is a template to start your own Yggdrasil project using Gradle. The template is based
on [Vert.x Gradle Starter](https://github.com/vert-x3/vertx-gradle-starter), and includes a version
of Yggdrasil as a submodule.

## Prerequisites

* JDK 8+

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
   `git submodule add -b master git@github.com:Interactions-HSG/yggdrasil.git`.
6. Initialize all submodules recursively: `git submodule update --init --recursive`.
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

* the Gradle project using the Kotlin DSL (see [build.gradle.kts](build.gradle.kts)) with
  auto-reloading and fat-jar building
* a [main verticle file](src/main/java/org/hyperagents/yggdrasil/dev/MainVerticle.java)
* a [Counter](src/main/java/ch/unisg/ics/interactions/Counter.java) hypermedia artifact
* a [unit test](src/main/test/org/hyperagents/yggdrasil/dev/MainVerticleTest.java) for the main
  verticle

## Programming hypermedia artifacts

Yggdrasil uses [CArtAgO v2.5](https://github.com/cartago-lang/cartago) for programming and running
hypermedia artifacts and it generates a W3C WoT TD for each virtual artifact when the artifact is
instantiated. All hypermedia artifact templates have to be registered before deploying the
`CArtAgOVerticle`.

All HTTP requests that interact with hypermedia artifacts have to include an `X-Agent-WebID` header
field. Sample request for creating a counter artifact:

```shell
curl -X POST 'http://localhost:8080/environments/env1/workspaces/wksp1/artifacts/' \
-H 'X-Agent-WebID: http://andreiciortea.ro/#me' \
-H 'Content-Type: application/json' \
-d '{
    "artifactClass" : "http://example.org/Counter",
    "artifactName" : "c1"
}'
```

## Building the project

To build the project, just use:

```shell
./gradlew build
```

It generates a _fat-jar_ in the `build/libs` directory. To launch the app with a configuration file:

```shell
java -jar build/libs/[NAME_OF_YOUR_PROJECT]-fat.jar -conf conf/config.json
```
