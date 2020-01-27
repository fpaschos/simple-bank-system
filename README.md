### [Simple Bank Accounting System](#readme)
Implementation of a simple bank accounting system using [akka.io](https://akka.io/docs/).

Some articles hopefully will be produced by this code base explaining the whole route of thoughts...

The purpose of this repository is to be familiarized with the new [akka-typed](https://doc.akka.io/docs/akka/current/typed/index.html) API.

### [Final implementation](#final-implementation)
![Capture](https://github.com/fpaschos/simple-bank-system/blob/master/docs/images/capture-2.gif)

### [How to run the code locally](#run-locally)
You should checkout to the lastest **master** branch.
From the project root directory

- For persistence to work (after part-4 ...)
```
cd deploy/local
docker-compose up
cd ../..
```

- Start the backend server (akka)
```sbt backend/run```

- Start the transaction-client to generate random traffic
```sbt transactions-cliens/run```

- Start the react ui client
```
cd react-ui
npm start
```

### [How to read the code](#how-to-read-code)
The code is split and organized in several **tags** in order to reflect 
the progression of the implementation.
The domain is intentionally left extremely simple in order to display as many akka features as possible.

The implementation progress is as following: 
#### In memory implementation 

##### Section 1 - A simple actor with tests
- [Part 1.1](https://github.com/fpaschos/simple-bank-system/tree/part-1.1) 
Create a simple `AccountHolder` actor.

- [Part 1.2](https://github.com/fpaschos/simple-bank-system/tree/part-1.2)
Use akka ask pattern to interact with the `AccountHolder` from outside the system.

- [Part 1.3](https://github.com/fpaschos/simple-bank-system/tree/part-1.3) 
Create `AccountHolderSpec` tests.

##### Section 2 - Create a supervisor cache group actor
- [Part 2.1](https://github.com/fpaschos/simple-bank-system/tree/part-2.1)
Create an `AccountGroup` in memory cache and supervisor actor.
 
- [Part 2.2](https://github.com/fpaschos/simple-bank-system/tree/part-2.2)
Create `AccountGroupSpec` tests.

##### Section 3 - Implement an REST API using `akka-http`
- [Part 3.1](https://github.com/fpaschos/simple-bank-system/tree/part-3.1)
Create an application `MainSupervisor` actor.
Create an `HttpServer` server actor with account endpoints.

- [Part 3.2](https://github.com/fpaschos/simple-bank-system/tree/part-3.2)
Implement `HttpServer` endpoints and integrate the existing account actors.

- [Part 3.3](https://github.com/fpaschos/simple-bank-system/tree/part-3.3)
Simulate some account operations traffic.
Create a `transaction-client` project that uses `akka-stream` API to post continuously random requests to the backend.

- [Part 3.4](https://github.com/fpaschos/simple-bank-system/tree/part-3.4)
Create a react-ui project to utilize the existing API.

- [Part 3.5](https://github.com/fpaschos/simple-bank-system/tree/part-3.5)
Change the logic of **AccountHolder** to accept only withdraws that 'fit' to the current balance.

#### Persistence and Event Sourcing
From now on a database is required.

##### Section 4 - Event Sourcing using JDBC
- [Part 4.1](https://github.com/fpaschos/simple-bank-system/tree/part-4.1)
Set up docker and postgresql.
Add persistence jdbc dependencies and configuration.
Make the **AccountHolder** a persisted actor.

- [Part 4.2](https://github.com/fpaschos/simple-bank-system/tree/part-4.2)
Implement **AccountHistoryService** that fetches account history from journal.
Updated the ```react-ui``` to display the persisted account history with  polling.

- [Part 4.3](https://github.com/fpaschos/simple-bank-system/tree/part-4.3)
Fix the existing unit tests to use in memory persistence journal.

### [Resources](#resources)
Of course this project is heavily influenced and inspired by the work of others.

- [Akka documentation](https://doc.akka.io/docs/akka/current/index.html) 
Practically everything is available here.

- [Akka IoT example](https://doc.akka.io/docs/akka/current/typed/guide/tutorial.html)
 An excellent starting point. The similarities with the project structure are obvious.
 
- [Tour of Akka Typed](https://manuel.bernhardt.io/articles/)
An in depth guide and example of akka-typed features. (see also the other blog articles)

- [Colin Breck Blog](https://blog.colinbreck.com/)
IMHO the best resource about actors - akka streams integration and articles about practical realtime scaling architecture topics.

- [SoftwareMill Blog](https://blog.softwaremill.com/)
Another great place of scala related material.

- [Scala Times](https://scalatimes.com/) 
The feed of the average scala developer.

Also, if you follow the code in various places you will find comments with more references.
