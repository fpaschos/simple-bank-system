### Simple Bank Accounts System
Implementation of a simple bank account management system using [akka.io](https://akka.io/docs/).

The purpose of this repository is to be familiarized with the new [akka-typed](https://doc.akka.io/docs/akka/current/typed/index.html) API.

### Final implementation
[Capture](https://github.com/fpaschos/simple-bank-system/blob/react-ui/docs/images/capture-1.gif)


### How to read the code
The code is split and organized in several **tags** in order to reflect 
the progression of the implementation.
The domain is intentionally left extremely simple in order to display as many akka features as possible.

The implementation progress is as following: 

##### Part 1 - A simple actor with tests
- [Part 1.1](https://github.com/fpaschos/simple-bank-system/tree/part-1.1) 
Create a simple `AccountHolder` actor.

- [Part 1.2](https://github.com/fpaschos/simple-bank-system/tree/part-1.2)
Use akka ask pattern to interact with the `AccountHolder` from outside the system.

- [Part 1.3](https://github.com/fpaschos/simple-bank-system/tree/part-1.3) 
Create `AccountHolderSpec` tests.

##### Part 2 - Create a supervisor cache group actor
- [Part 2.1](https://github.com/fpaschos/simple-bank-system/tree/part-2.1)
Create an `AccountGroup` in memory cache and supervisor actor.
 
- [Part 2.2](https://github.com/fpaschos/simple-bank-system/tree/part-2.2)
Create `AccountGroupSpec` tests.

##### Part 3 - Implement an REST API using `akka-http`
- [Part 3.1](https://github.com/fpaschos/simple-bank-system/tree/part-3.1)
Create an application `MainSupervisor` actor.
Create an `HttpServer` server actor with account endpoints.

- [Part 3.2](https://github.com/fpaschos/simple-bank-system/tree/part-3.2)
Implement `HttpServer` endpoints and integrate the existing account actors.

- [Part 3.3](https://github.com/fpaschos/simple-bank-system/tree/part-3.3)
Simulate some account operations traffic.
Create a `transaction-client` project that uses `akka-stream` API to post continuously random requests to the backend.

- [Part 3.4]() 
Create a react-ui project to utilize the existing API
