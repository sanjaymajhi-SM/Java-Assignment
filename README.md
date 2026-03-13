# Hall Symphony Inc. — Booking System

## Project Structure

```
HallSymphony/
├── src/
│   ├── Main.java                          ← Entry point
│   └── hallsymphony/
│       ├── model/                         ← OOP model classes
│       │   ├── User.java                  ← Abstract base class (Abstraction)
│       │   ├── Customer.java              ← extends User (Inheritance)
│       │   ├── Staff.java                 ← extends User (Inheritance)
│       │   ├── Hall.java
│       │   ├── HallSchedule.java
│       │   ├── Booking.java
│       │   └── Issue.java
│       ├── data/                          ← File handling / DAO classes
│       │   ├── FileHelper.java            ← Low-level read/write for .txt files
│       │   ├── UserDAO.java               ← CRUD for users.txt
│       │   ├── HallDAO.java               ← CRUD for halls.txt
│       │   ├── ScheduleDAO.java           ← CRUD for schedules.txt
│       │   ├── BookingDAO.java            ← CRUD for bookings.txt
│       │   ├── IssueDAO.java              ← CRUD for issues.txt
│       │   └── DatabaseSeeder.java        ← Creates default data on first run
│       ├── ui/                            ← Swing GUI
│       │   ├── MainFrame.java             ← Root window + CardLayout
│       │   ├── LoginPanel.java
│       │   ├── RegisterPanel.java
│       │   ├── scheduler/
│       │   │   └── SchedulerDashboard.java
│       │   ├── customer/
│       │   │   └── CustomerDashboard.java
│       │   ├── admin/
│       │   │   └── AdminDashboard.java
│       │   └── manager/
│       │       └── ManagerDashboard.java
│       └── util/
│           └── UIHelper.java              ← Shared Swing helper methods
│
└── database/                              ← TXT files (auto-created on first run)
    ├── users.txt
    ├── halls.txt
    ├── schedules.txt
    ├── bookings.txt
    └── issues.txt
```

---

## TXT Database Format

Each file stores one record per line. Fields are separated by `|`.

### users.txt
```
STF-001|admin|admin123|admin@hallsymphony.com|0100000001|ADMIN|true|Administration
USR-001|john_doe|john123|john@email.com|0123456789|CUSTOMER|true|123 Main St|Acme Corp
```

### halls.txt
```
HLL-001|Grand Auditorium|AUDITORIUM|1000|300.00|Main auditorium
HLL-002|Crystal Banquet|BANQUET_HALL|300|100.00|Banquet hall
```

### schedules.txt
```
SCH-001|HLL-001|AVAILABILITY|2025-08-01 08:00|2025-08-31 18:00|Open for August|STF-003
```

### bookings.txt
```
BKG-001|USR-001|HLL-002|2025-08-10 09:00|2025-08-10 13:00|Wedding|CONFIRMED|400.00|Cash|PAID|2025-07-01
```

### issues.txt
```
ISS-001|USR-001|BKG-001|AC not working||STF-003|IN_PROGRESS|2025-07-01
```

---

## How to Compile & Run

### Step 1 — Compile
```bash
mkdir -p out
find src -name "*.java" | xargs javac -d out -sourcepath src
```

### Step 2 — Run
```bash
java -cp out Main
```

### Using an IDE (IntelliJ / Eclipse / NetBeans)
1. Open project
2. Set `src` as source root
3. Run `Main.java`

---

## Default Login Accounts

| Role          | Username    | Password     |
|---------------|-------------|--------------|
| Administrator | admin       | admin123     |
| Manager       | manager     | manager123   |
| Scheduler     | scheduler1  | sched123     |
| Scheduler     | scheduler2  | sched456     |
| Customer      | john_doe    | john123      |
| Customer      | jane_smith  | jane123      |

---

## OOP Concepts Used

| Concept       | Where                                                      |
|---------------|------------------------------------------------------------|
| Abstraction   | `User` is the abstract base — never instantiated directly  |
| Inheritance   | `Customer` and `Staff` both extend `User`                  |
| Encapsulation | All fields are `private`/`protected` with getters/setters  |
| Polymorphism  | `UserDAO.addUser(User)` accepts any User subclass          |

---

## File Handling

- **FileHelper.java** handles all raw read/write operations
- Each DAO class (UserDAO, HallDAO, etc.) uses FileHelper
- Records are appended on add, and the whole file is rewritten on update/delete
- On first run, `DatabaseSeeder` creates default `.txt` files automatically
