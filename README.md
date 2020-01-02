### Simple Bank Accounts System
Implementation of a simple bank account management system using [akka.io](https://akka.io/docs/).

The purpose of this repository is to be familiarized with the new [akka-typed](https://doc.akka.io/docs/akka/current/typed/index.html) API.

### How to read the code
The code is split and organized to several **branches** in order to reflect 
the progression of the implementation.
The domain is intentionally left extremely simple in order to display as many akka features as possible.

The code is organized as following: 

- [Part 1.1](https://github.com/fpaschos/simple-bank-system/tree/part-1.1) 
Implement a simple  **AccountHolder** actor.

- [Part 1.2](https://github.com/fpaschos/simple-bank-system/tree/part-1.2)
 Use akka ask pattern to interact with the **AccountHolder** from outside the system.

- [Part 1.3](https://github.com/fpaschos/simple-bank-system/tree/part-1.3) 
Implement **AccountHolderSpec** tests.

- [Part 2.1](https://github.com/fpaschos/simple-bank-system/tree/part-2.1)
 Implement a **AccountGroup** in memory cache and supervisor actor.
 
- [Part 2.2](https://github.com/fpaschos/simple-bank-system/tree/part-2.2)
Implement **AccountGroupSpec** tests.
