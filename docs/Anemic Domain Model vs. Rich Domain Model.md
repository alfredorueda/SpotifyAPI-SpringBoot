Anemic Domain Model vs. Rich Domain Model (DDD)

1. Anemic Domain Model

An anemic domain model is a design style where your domain entities are essentially just data containers — they have attributes (fields) and getters/setters, but they contain little to no business logic.
Instead, all the business rules and behavior live in separate service classes (often called “domain services” or sometimes just “service layer”).

Characteristics:
•	Entities: Mostly getters/setters, sometimes constructors. No significant methods encapsulating business behavior.
•	Business logic: Centralized in service classes, often procedural in style.
•	Design smell: While this separation might look clean at first, it often violates encapsulation and object-oriented design principles, especially the “Tell, Don’t Ask” principle.
•	Maintenance risk:
•	Changes to the domain often require changes in multiple services.
•	Entities become passive, losing the ability to enforce invariants themselves.
•	Increased risk of spreading business rules across multiple unrelated classes.

Example:

// Anemic entity
public class BankAccount {
private String id;
private BigDecimal balance;

    // getters and setters only
}

// Business logic in service
public class BankAccountService {
public void withdraw(BankAccount account, BigDecimal amount) {
if (account.getBalance().compareTo(amount) >= 0) {
account.setBalance(account.getBalance().subtract(amount));
} else {
throw new InsufficientFundsException();
}
}
}

Here, BankAccount is just a dumb data holder; all behavior is external.

⸻

2. Rich Domain Model (Domain-Driven Design)

In a proper DDD approach, domain entities encapsulate both state and behavior. They own their invariants, rules, and lifecycle changes.
Instead of letting services manipulate them freely, you send commands/messages to the entities, and they decide how to update themselves based on the domain rules.

Characteristics:
•	Entities: Contain both data and business logic directly related to their own responsibilities.
•	Domain services: Still exist, but are reserved for operations that don’t naturally belong to a single entity or value object (e.g., cross-aggregate logic).
•	Encapsulation: Entities guard their own invariants — rules cannot be bypassed by simply setting a field.
•	Ubiquitous Language: Entities and methods are named after the domain concepts understood by business experts.
•	Benefits:
•	Better alignment with business rules.
•	Easier to reason about the domain.
•	Reduced duplication of logic.
•	More resistant to accidental corruption of state.

Example:

// Rich domain entity in DDD
public class BankAccount {
private final String id;
private BigDecimal balance;

    public BankAccount(String id, BigDecimal openingBalance) {
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Opening balance must be positive");
        }
        this.id = id;
        this.balance = openingBalance;
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }
}

Here, the withdrawal rules are enforced inside the entity itself, preventing external code from violating the business constraints.

⸻

3. Key Differences

Aspect	Anemic Domain Model	Rich Domain Model (DDD)
Where is the logic?	In service classes, outside the entity	Inside the entity, close to the data
Encapsulation	Often broken — data can be modified directly	Strong — entities protect their state
Alignment with OOP	Procedural style, “data + functions” separated	True OOP: data and behavior together
Maintainability	Logic spread across multiple services	Logic localized to the entity
Business Rules Enforcement	External enforcement, risk of inconsistency	Self-enforced by the entity
Testability	Requires service testing for most rules	Entities can be tested in isolation
Industry Demand	Older enterprise style, common in legacy code	Increasingly preferred, matches job postings at companies like Citibank


⸻

4. Why Industry (Including Citibank) Prefers DDD
   •	Better communication with business stakeholders through ubiquitous language.
   •	Reduced accidental complexity by keeping the model close to the business problem.
   •	Improved maintainability and extensibility for complex domains.
   •	Stronger model integrity, especially important in financial systems, where invariants like “a withdrawal cannot exceed the balance” must never be bypassed.
   •	Supports microservices and modular architectures, as aggregates can be managed independently.

