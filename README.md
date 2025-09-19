# 🚇 Chennai Metro Ticket Booking System

A **console-based Java application** that simulates the **Metro Ticket Booking System** for Chennai Metro Rail.  
The application supports **user registration, login, journey planning, QR code ticket booking, ticket management, and admin functionalities** like user monitoring and station/route management.  

---

## ✨ Features  

### 👤 User Management
- **Signup** → New user registration with input validations.  
- **Login** → Secure login with SHA-256 password hashing.  
- **User Dashboard** →  
  - 🎟️ Book QR Ticket  
  - 📂 View My Tickets  
  - ❌ Cancel Tickets  
  - 🚪 Logout  

---

### 👨‍💼 Admin Management
- Secure **Admin Login**.  
- View all registered users with account creation date.  
- Manage **station and route data**.  

---

### 🚉 Station & Route Management
- **StationLoader** (Singleton pattern) loads all stations from JSON at startup.  
- **Travel Planner** calculates:  
  - Stops count  
  - Fare (with discounts for Group/SVP)  
  - Estimated travel time (1.5 mins per stop + 3 mins interchange)  

---

### 🎟️ Ticket Booking
- Book multiple ticket types: **SJT, RJT, Family, Group **.  
- Each ticket includes:  
  - Ticket ID  
  - Journey details (From → To)  
  - Fare & Ticket type  
  - Booking Date & Validity  

---

### 📲 QR Code Ticket
- Generates **QR codes** using the ZXing library.  
- Creates a **ticket image (.jpg)** containing:  
  - QR code  
  - Ticket details  
  - Footer note → *“Validity starts from booking time”*.  
- Saved automatically in the **file system**.  

---

### 📂 Ticket Management
- View all booked tickets.  
- Cancel tickets with **refund/cancellation rules**.  

---

### 🛡️ Logging & Error Handling
- Uses **Log4j2** for structured logging (`info`, `debug`, `warn`, `error`).  
- Custom exceptions for input validation, login failures, and ticketing errors.  

---

### 📂 File Management
- **Users** → stored in `users.json`  
- **Stations** → stored in `stations.json`  
- **Tickets** → generated as QR-based `.jpg` files  

---

## 🛠️ Tech Stack
- **Java** (Core + Advanced)  
- **File I/O** (JSON, text, QR image files)  
- **Collections Framework**  
- **Multithreading** (for tasks like QR generation)  
- **Java 8 Features** (Lambdas, Streams, Functional Interfaces, Optional)  
- **Logging** → Log4j2  
- **Design Patterns** → Singleton (StationLoader), Factory, Builder  

---

## 🚀 How to Run
1. Clone the repo:  
   ```bash
   git clone https://github.com/ezhilrajQA/CMRLConsoleApp.git
   cd CMRLConsoleApp
