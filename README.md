# Clearance Core

`clearance-core` is a specialized financial middleware service responsible for the **Clearing** phase of the payment lifecycle. It aggregates authorized transactions, calculates network fees, and prepares batches for final settlement.

## 🚀 Features

* **Automated Clearing Worker**: A scheduled engine that captures `AUTHORIZED` payments.
* **Fee Engine**: Calculates bank/network fees (default 1.5%) per transaction.
* **Batch Management**: Groups individual payments into a `ClearingBatch` for auditability.
* **Persistence**: Built with Spring Data JPA and H2 (In-memory) for rapid processing.
* **CI/CD Ready**: Includes Docker and GitHub Actions configurations.

## 🏗 Architecture

This project is part of a split-core architecture:
1.  **Clearance Core**: (This service) Handles Bank Network capture and fee deduction.
2.  **Settlement Core**: Handles merchant payouts and net fund movement.

## 🛠 Tech Stack

* **Java 17** (Eclipse Temurin)
* **Spring Boot 3.2.0**
* **Spring Data JPA**
* **H2 Database** (Development/Testing)
* **Maven** (Build Tool)

## 🚦 Getting Started

### Prerequisites
* JDK 17 or higher
* Maven 3.8+

### Build and Test
To compile the project and run the integration tests:
```bash
mvn clean install
mvn spring-boot:run
```

### 🐳 Docker (Optional)
If Docker is available in your deployment environment:

```bash
docker build -t clearance-core .
docker run -p 8083:8083 clearance-core
```

## 📝 Usage Note
The ClearingWorker is configured to run automatically every 60 seconds (see SchedulingConfig). It looks for any records in the payments table with the status AUTHORIZED and transitions them to CLEARED.

## 🧪 Verification Flow
1. Start the application on port 8083.

2. Access the H2 Console and insert payments with status AUTHORIZED.

3. Observe the ClearingWorker logs:
    3.1. Starting clearing process for X payments.
    3.2. Batch 1 [USD] completed successfully.

4. Query the clearing_batches table to see the aggregated totals and 1.5% fee deduction.


---

### Is the project 100% complete?

For **clearance-core** as a standalone module: **Yes.**