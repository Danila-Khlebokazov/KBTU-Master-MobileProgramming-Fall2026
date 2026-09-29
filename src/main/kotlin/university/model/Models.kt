package university.model

interface Displayable {
    fun displayInfo(): String
}

interface Gradable {
    val passingScore: Int
        get() = 50

    fun isPassed(score: Int): Boolean = score >= passingScore
}

enum class Level(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class Course(
    val code: String,
    val title: String,
    val credits: Int,
    val capacity: Int,
    val level: Level = Level.BEGINNER
) : Displayable {
    override fun displayInfo() = "$code: $title ($credits credits, ${level.label})"
}

sealed class EnrollmentResult {
    data class Success(val course: Course) : EnrollmentResult()
    data class Failed(val reason: String) : EnrollmentResult()
}

abstract class Person(val id: Int, val name: String) : Displayable {
    abstract val role: String

    override fun displayInfo() = "$name ($role)"
}

class Student(id: Int, name: String, val year: Int) : Person(id, name), Gradable {
    override val role = "Student"
}

class Instructor(id: Int, name: String, val department: String) : Person(id, name), Gradable {
    override val role = "Instructor"
    override val passingScore = 70

    override fun displayInfo() = "${super.displayInfo()}, $department"
}

object EnrollmentIds {
    private var counter = 0

    fun next(): Int = ++counter
}
