# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Chess Server Design Diagram
[Server Design Diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaDAAFGQ+ymqqAJSBAO4AFkhgYoiopAC0AHzklDRQAFwwANoACgDyZAAqALowAPQ+BlAAOmgA3gBEg5TBALYoE-UTMBMANCu46rnQHIvLaysos8BICHsrAL6YwnUwVazsXJSNk9NQcwtLK+sTm6rbUF2XwOEyOJzOwKubE43Fg9xuokaUCiMWKUGSkWisUokQAjj41GAstdaqI7tVZPIlCp1I17CgwABVIbJN4fYmUxQZdTkow6eoAMSQnBgzMonJgOjCbOA80wnOpmTu8NJKkaaB8CAQJJEKl5Cu5qnqIGRchQYvRb05HO0ip592M9QUHA4oqGnJ1VDJ9wNNKNJpQZoUPjA+WSwBD+Wt8ttht5judruDoY9CL15Xu0KeDQiKOxUEiqk1WCzsOV1RuzxgryGH2WjX2Pwjoc6EAA1uh6ytLp7KOX4MhzI0AExOJyjGszWULGAN74rZv5VsdtBd-ZXdAcUxeXz+ALQdj0mAAGQg0SSqXSfpyAQKRRKg4qFdqVdaHR6-QM6gSaAnU1r07nCCfwAkC669nC1SllWk7vIBwI-CBOxAVCjxliquooI0CBnsKySnueeIErExJpoYPqxn6dIMharIAfMNpUnGDr8jAQoihaEpSjAMpysY-a+pkMZMX6-ZkY0nHaJ63rPphjQgPkKAgG2MBIAAZjA0Q0EkSGAjAxxgApajSemFKUZk8mmsUyZhou0aCfa1SOjAiYuZGqaquRmZoVWBHCoWxaYNBcIYXULwrLxnyNgukbLp2wIwFclYhdUpRDjAo5ODAE4RfRUXzhMi5xauCUbpw27eH4gReCg6AnmevjMLehTFJgaVPjU1CvtIACix49Z0PW9H0X6qD+oxFe26AQbywWNJNK5BT5kGdV6aowDh9iNfhDUhkRhKkZ5+rmbSmkMjZxXhrFU1oIxXKiSxgrCkm10rqpSQ2R6-EZmZIlCQ5qhiZ5jQXTdbG+OVZHHX9p0cCg3DWa96BXS2N13XagOPTI8MMoYi7QEgABe8SJDAkCLVDP0PDCvm7WAAUICWy1A11OZjElL4pQOZRgCOY7ZUk7NmOVniVXuyKuseqIwAA4tOgPNfebWPswoWvjLA3DfY04TUjaAzd5NM5gt03Bby4kbaicvzKo+FW-L+0kSZ5G-fdFlnWANko0uaPCW7jl8o07Evajb3Cm5KZSd9FEwxoAMs2tWERz7b1qRDW6U67GPUWA1soJyyTo8xTmsTLDIwMAWqy9OHrx1TFtS7EMAQBpecShAOgAFZKWAGiU4b2aNI3ufTgzTNGwnzxjCs2s24szSTLPKAAJLSPPACMw4AMwACw-HexQWnWCErDoCCgG2R-wdFExLwActOkIwN0HOs-27W8xl-OjNWM-y-PTRF7TlXhvbee8VgH3NHlICPwz4XyvvMGBf95gP3mE-F+wstyi13IEbAPgoDYG4PAKyhg855Bag+Hm5tOaNDfF0LWOtgh6wnPfacr9bgD1hPNZhgtkEoFQflZYqEJ6hURDAAMZo87JDgCQvOjsiTO2hv7I0HsvYm1un7DG8ZWLB2TsVd6yda4nUxqI9aoNU7p0UTHZRllAzFCkUveyxjtFOhdNXeYHlML9jmsQuxKA5FqECmbeuNDf632AWvRom9d6JQNvcD+fMso-2nuE+YIColgNiZuCqOCAiWHhjhXIMAABSEBw5kICHA5SysqFqxzK0RkH4+hL11qHdAE5CHAHyVAOAEAcJQHWEvVe7C+ycKrOonKvxz7dN6f0vYAB1Fgy9Bp9AAELHgUHAAA0iCIZkSYDRJ3rE4JslE6NE7mUtAUjSnCgCfiA6Vis6GhzmovWRcHolyDs9PRYNw6fSknXUxSd-nyEeTIYxOcHE120O8pUWNdFt20JKMIedV6aOYkCxoiLQWZ1WmIuo6kwjFFlOIvp0BVKAxANAZE4AwUW2AG4t4MA1LeFmIYpFkBNLTjBQDRofgtD2OnMkRxML0UfMDmxb52LgDIvcSvaQFcRQQDqLkKASt+5QWWo0G5VzR6BMZktERdT6hC2Su-FWiSBbViuJgnJVUAheC6QOQMsAFDYEIYQEmSRFatQ-tQ1mtDer9UGsNYwBtNVGxeMI7M-qzlGBxsUZI0gE0oHkYdLx1js7xoRigZIkl5CwoDs5OGOayaKR4kMIwHLdDcB5RC7NuNkjSsLZjT5DbihlsMEvKt8gyY1rELi3l7bc0guAC2lxw7O0V0jD2mV8gz4DqOlTHxyac1j0NTGkJAbrVxNShar+STxhlQ4EAA)