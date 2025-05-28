
<div align="center">
    <img src="docs/logo.svg" height="250" alt="logo">
<p>A matching engine is a technology that lies at the core of any exchange.</p>
<p>From a high-level perspective, a matching engine matches people (or organizations) who want to buy an asset with people who want to sell an asset.</p>
</div>

### Problem Statement
This [file](Baraka%20Java%20Take%20Home%20Assignment%202025.pdf) includes all the information
needed to understand the problem this project is intended to solve.

### Design Overview
  <img src="docs/engine.svg" alt="logo">

- The `MatchingEngine` is the central component—consists of a set of `OrderBook`s.
- Each `OrderBook` is responsible for a specific asset's booking information—via `2` Queues.
- One Queue is for `BUY` orders, another is for `SELL` orders.
- Queues are implemented with `PriorityBlockingQueue` as it's thread-safe and can prioritize elements based on a custom `Comparator`.
- Each order placed is assigned an id from an `AtomicLong` held by the `MatchingEngine`—Also a thread safe element used for counting.
- The `Archive` is a centralized registry that tracks all orders ever submitted to the system, and is managed by the matching engine.
- An `EventBus` is included to separate the logic of archiving from the typical booking process.
- All booking events are propagated from all `OrderBook`s to the `MatchingEngine`. 

### Built with
- Java (JDK 21).
- Spring Boot (3.4).

### Steps to deploy
- Load this project on Intellij.
- Run the application in `local` profile.
- Use [generated-requests.http](docs/generated-requests.http) to test the application.

<div align="center" style="margin: 20px 0; border: 2px solid; border-radius: 10px; background-color: transparent; max-width: 600px;">
  <h3 style="margin: 0; font-size: 1.5em;">📊 Code Coverage</h3>
  <div style="display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;">

![Instruction Coverage](https://img.shields.io/badge/Instruction-98.84%25-brightgreen)
![Line Coverage](https://img.shields.io/badge/Line-100.0%25-brightgreen)
![Branch Coverage](https://img.shields.io/badge/Branch-79.41%25-yellow)
![Complexity Coverage](https://img.shields.io/badge/Complexity-82.93%25-yellow)
![Method Coverage](https://img.shields.io/badge/Method-100.0%25-brightgreen)
![Class Coverage](https://img.shields.io/badge/Class-100.0%25-brightgreen)
![Overall Coverage](https://img.shields.io/badge/Overall-97.55%25-brightgreen)

  </div>
</div>