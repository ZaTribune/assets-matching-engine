
<div align="center">
    <img src="docs/logo.svg" height="250" alt="logo">
</div>

## Design Overview
  <img src="docs/engine.svg" alt="logo">

- The `MatchingEngine` is the central component—consists of a set of `OrderBook`s.
- Each `OrderBook` is responsible for a specific asset's booking information—via `2` Queues.
  - One Queue for `BUY` orders.
  - Another for `SELL` orders.
- Queues are implemented with `PriorityBlockingQueue` as it's thread-safe FIFO data structure that can prioritize elements based on a custom `Comparator`.
- Each order placed is assigned an id from an `AtomicLong`—Another thread safe element used in applications such as atomically incremented sequence numbers.
  - `AtomicLong` is also managed by the `MatchingEngine`.
- The `Archive` is a centralized registry that tracks all orders submitted to the system—Also managed by the `MatchingEngine`.
- An `EventBus` is included to separate the logic of archiving from the typical booking process.
- In other words, all booking events are propagated from `OrderBook`s to the `MatchingEngine` via the `EventBus`.

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

![Instruction Coverage](https://img.shields.io/badge/Instruction-97.38%25-brightgreen)
![Line Coverage](https://img.shields.io/badge/Line-97.85%25-brightgreen)
![Branch Coverage](https://img.shields.io/badge/Branch-83.33%25-yellow)
![Complexity Coverage](https://img.shields.io/badge/Complexity-85.45%25-yellow)
![Method Coverage](https://img.shields.io/badge/Method-97.06%25-brightgreen)
![Class Coverage](https://img.shields.io/badge/Class-100.0%25-brightgreen)
![Overall Coverage](https://img.shields.io/badge/Overall-96.32%25-brightgreen)

  </div>
</div>