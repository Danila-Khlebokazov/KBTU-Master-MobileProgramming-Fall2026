# Task 1. Kotlin Fundamentals and OOP

> [!TIP]
> To see all works in this repository, please check the [master branch](../../tree/master).

Small Kotlin/JVM console application (no Android components). It simulates a university:
courses with limited capacity, students, an instructor, and concurrent course enrollment
with coroutines.

## How to run
Requires JDK 18+ (Gradle downloads everything else, including `kotlinx-coroutines-core`).

- IntelliJ IDEA: open the folder, wait for the Gradle import, run `main()` in `src/main/kotlin/university/Main.kt`.
- Terminal: `./gradlew run` (macOS/Linux) or `gradlew.bat run` (Windows).

## Structure
```
src/main/kotlin/university/
├── Main.kt                 entry point, whole scenario
├── model/Models.kt         interfaces, enum, data class, sealed class, inheritance, object
├── service/University.kt   business logic, higher-order functions, suspend function
└── util/Format.kt          const, extension functions, output helper
```

## Where the requirements are demonstrated
| Requirement | Where |
|---|---|
| Variables, data types, conditions, loops | `Main.kt`: `val`/`var`, `Int`/`String`/`Boolean`, `if/else`, `when`, `for`, `while`, `withIndex()` |
| List, Set, Map | `University.kt`: `MutableList` of courses/students, `Set` of departments and enrolled IDs, `Map` course code -> enrolled IDs (`groupBy`, `associateBy`) |
| map, filter, reduce | `University.printCourseStatistics()`: `map`, `filter`, `reduce` (plus `partition`, `flatMap`, `sumOf`, `minByOrNull`/`maxByOrNull`) |
| Functions, higher-order, lambdas | `University.findCourses(predicate: (Course) -> Boolean)` called with lambdas in `Main.kt`; default parameter in `lowCapacityCourses`; extension functions in `Format.kt` |
| Classes and objects | `University`, `Student`, `Instructor`, `Course`; `object EnrollmentIds` (singleton); `enum class Level` |
| Inheritance | `abstract class Person` -> `Student`, `Instructor` |
| Interfaces and polymorphism | `Displayable`, `Gradable` (default method); `List<Displayable>` in `Main.kt` calls `displayInfo()` on different types |
| Data class | `Course` (`copy()`, equality, destructuring in `Main.kt`) |
| Sealed class | `EnrollmentResult` (`Success` / `Failed`), handled with `when` in `Main.kt` |
| suspend + coroutine | `University.enroll()` is `suspend` (uses `delay`, `Mutex`); `Main.kt` launches requests concurrently with `runBlocking`, `async`, `awaitAll` |
