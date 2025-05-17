# 🛒 Functional Rule-Based Discount Engine (Scala - ITI 45 Project)

## 📋 Problem Statement

A large retail store needs a **rule-based engine** that processes transaction records, applies **functional discount logic**, and stores the calculated results in a PostgreSQL database.

This engine reads raw CSV data, applies qualifying and calculation rules using **pure functional programming**, logs key events, and pushes final results to a database.

---

## 🧠 Discount Rules Summary

### ✅ Qualifying Rules & Discounts:

| Rule Description                                      | Discount Logic                                      |
|------------------------------------------------------|-----------------------------------------------------|
| Product expires in < 30 days                         | (30 - daysLeft) × 1% per day                        |
| Product is "Cheese"                                  | 10%                                                 |
| Product is "Wine"                                    | 5%                                                  |
| Transaction date is March 23                         | 50%                                                 |
| Quantity-based discount                              | 6–9 units: 5%<br>10–14: 7%<br>>=15: 10%             |
| Channel is **App** (from MOM update)                 | `ceil(quantity / 5) × 5 × 0.01`                     |
| Payment method is **Visa** (from MOM update)         | 5%                                                  |

---

## 🔁 Discount Calculation Rule

- If **no rules** apply → discount = `0%`
- If **one rule** applies → use that discount
- If **multiple rules** apply → **take top 2 discounts**, average them

---

## 💡 Technologies Used

- Language: **Scala 3**
- Build Tool: **SBT**
- Database: **PostgreSQL**
- Logging: Custom logger to `logs/application.log`
- CSV: Read using `scala.io.Source`

---

## 📁 File Structure

```
project-root/
│
├── src/
│   └── Main.scala              # Main logic and functional rules
│
├── logs/
│   └── application.log         # Timestamped logs of engine events
│
├── TRX1000.csv                # Sample input CSV file
├── build.sbt                  # Scala project configuration
└── README.md                  # This file
```

---

## 📥 Input Format

CSV Columns:
```
timestamp,product_name,expiry_date,quantity,unit_price,channel,payment_method
```

Example:
```
2023-04-09T02:56:43Z,External Supplier,2023-05-23,14,9.92,App,Visa
```

---

## 🧮 Output

Stored in a PostgreSQL table: `product_discounts`

| Field           | Type              |
|----------------|-------------------|
| id             | SERIAL PRIMARY KEY|
| product_name   | VARCHAR(255)      |
| original_price | DOUBLE PRECISION  |
| discount       | DOUBLE PRECISION  |
| final_price    | DOUBLE PRECISION  |

---

## ⚙️ How to Run the Project

### 🔧 1. Set up PostgreSQL

- Create a database called `postgres`
- Set username/password in code (default: `postgres` / `123`)
- Table will be auto-created

### ▶ 2. Run the Scala project

Make sure you have [SBT](https://www.scala-sbt.org/) installed, then:

```bash
sbt run
```

---

## 🔒 Functional Programming Constraints (Applied)

- ✅ Only **`val`** used (no `var`)
- ✅ All logic is **pure**: No side-effects in business rules
- ✅ No loops: Only `map`, `filter`, `zip`, etc.
- ✅ Logging and DB writing handled as impure I/O wrappers

---

## ✅ Example: Logging Format

```
2025-05-16 15:23:44     INFO     The Data Has Been Calculated Successfully
2025-05-16 15:23:47     INFO     The Data Has Been Written to DB Successfully
```

---

## 📦 How to Build & Push to GitHub

```bash
git init
git add .
git commit -m "Initial functional discount engine"
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```
## Screens : 

![Screenshot 2025-05-17 125918](https://github.com/user-attachments/assets/58e23756-18b4-4c2b-a71d-de1c095cb636)



---
## Comment:
- This Code is moduled and commented with the help of LLM
- If you want to see my version, go to a first-version branch
---

## 👨‍💻 Author

- **Name**: Mahmoud Osama
- **Cohort**: ITI 45
- **Contact**: [osamamahmod657@gmail.com](mailto:your.email@example.com)

---

## 📹 Reference

> Project Reference Video: [YouTube](https://youtu.be/6uwRajbkaqI?si=6OJW_oCXE8Fcq36I)
