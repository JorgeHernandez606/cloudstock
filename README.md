# CloudStock

**Problem statement:** Store employees lack a real-time, synchronized overview of their assigned inventory, leading to stock discrepancies and manual restock errors across shifts.

## Team & Roles
- **Jorge Hernandez** (`@JorgeHernandez606`) – Product Owner, Tech Lead, Developer, QA/DevOps *(Solo team)*

## MVP Features
- [x] **Sprint 1 (Lab 1):** User registration, login, logout, and auto-login via Firebase Authentication
- [ ] **Sprint 2 (Lab 2):** Real-time list of store products assigned to the employee
- [ ] **Sprint 2 (Lab 2):** Create, edit, and delete products with category selection and field validation
- [ ] **Sprint 2 (Lab 2):** Search, sorting, and stock summary statistics
- [ ] **Sprint 3 (Lab 3):** Automated server-side stock validation (Cloud Function) with `pending` / `confirmed` / `rejected` workflow

## Data Model (Cloud Firestore)

| Collection | Field | Type | Set by | Description |
| :--- | :--- | :--- | :--- | :--- |
| **users** | `userId` (doc id) | `string` | Auth / App | Authenticated user UID |
| | `name` | `string` | User | Employee display name |
| | `email` | `string` | User / Auth | Employee email address |
| | `createdAt` | `timestamp` | Server | Account creation timestamp |
| **categories** *(read-only)* | `id` (doc id) | `string` | Admin / Seed | Category ID (`1`, `2`, `3`) |
| | `name` | `string` | Admin / Seed | Category name (`Food`, `Beverages`, `Cleaning`) |
| **products** | `productId` (doc id) | `string` | Firestore | Auto-generated product ID |
| | `userId` | `string` | App (Auth) | Owner UID of the assigned employee |
| | `name` | `string` | User | Product name |
| | `categoryId` | `string` | User | Reference ID to `categories` |
| | `categoryName` | `string` | App | Denormalized category name for list display |
| | `stock` | `number` | User | Current units in stock |
| | `maxCapacity` | `number` | User | Maximum shelf/storage capacity |
| | `status` | `string` | App / Server | `pending`, `confirmed`, or `rejected` |
| | `rejectReason` | `string` | Server | Reason provided if rejected by Cloud Function |
| | `createdAt` | `timestamp` | Server | Server creation timestamp |
| | `updatedAt` | `timestamp` | Server | Last modification server timestamp |

## Architecture Diagram

```mermaid
flowchart LR
    A[Android App<br/>Kotlin + Jetpack Compose] -->|Auth SDK| B[Firebase Authentication]
    A -->|Firestore SDK| C[(Cloud Firestore)]
    D[Cloud Functions<br/>Lab 3] -->|Validates stock| C


## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Mobile App** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Architecture** | MVVM (`StateFlow` + `ViewModel`), Repository pattern, Pure Kotlin validators |
| **Authentication** | Firebase Authentication (Email/Password) |
| **Cloud Database** | Cloud Firestore (EU region) + Firestore Security Rules |
| **Testing** | JUnit 4, Kotlin Coroutines Test |

## Definition of Done
A user story or requirement is considered **Done** when:
1. All acceptance criteria are met and verified on an Android emulator with Google Play Services.
2. Input validation and security rules prevent invalid data or unauthorized access without crashing.
3. Code is cleanly separated into Data, Logic/Domain, and UI layers with no backend SDK calls in UI composables.
4. Changes are merged into `main` via a reviewed Pull Request linking `Closes #<issue-number>`, and the corresponding issue is moved to **Done**.