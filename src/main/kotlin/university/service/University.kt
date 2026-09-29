package university.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import university.model.Course
import university.model.EnrollmentIds
import university.model.EnrollmentResult
import university.model.Student
import university.util.department
import university.util.section

class University(
    val name: String,
    private val courses: MutableList<Course>,
    private val students: MutableList<Student>
) {
    private val enrollments: MutableMap<String, MutableSet<Int>> = mutableMapOf()
    private val mutex = Mutex()

    fun findCourses(predicate: (Course) -> Boolean): List<Course> = courses.filter(predicate)

    // Default parameter
    fun lowCapacityCourses(threshold: Int = 2): List<Course> = findCourses { it.capacity <= threshold }

    suspend fun enroll(student: Student, courseCode: String): EnrollmentResult {
        // Random latency; concurrent requests may now finish in any order
        delay(Random.nextLong(100, 1000))

        // Mutex protects shared state when enrolling concurrently
        return mutex.withLock {
            val course = courses.find { it.code == courseCode }
                ?: return@withLock EnrollmentResult.Failed("Course $courseCode was not found.")

            val enrolled = enrollments.getOrPut(course.code) { mutableSetOf() }

            when {
                students.none { it.id == student.id } ->
                    EnrollmentResult.Failed("${student.name} is not registered.")

                student.id in enrolled ->
                    EnrollmentResult.Failed("${student.name} is already enrolled in ${course.code}.")

                enrolled.size >= course.capacity ->
                    EnrollmentResult.Failed("${course.code} is full.")

                else -> {
                    enrolled.add(student.id)
                    EnrollmentResult.Success(course)
                }
            }
        }
    }

    fun printCourseStatistics() {
        section("Course Statistics")

        // reduce needs a non-empty list, so an empty catalog falls back to 0
        val totalCredits = courses.map { it.credits }.takeIf { it.isNotEmpty() }?.reduce { total, c -> total + c } ?: 0
        val advanced = courses.filter { it.credits >= 4 }
        val (large, small) = courses.partition { it.capacity >= 3 }
        val byLevel = courses.groupBy { it.level }
        val byCode = courses.associateBy { it.code }
        val biggest = courses.maxByOrNull { it.capacity }
        val smallest = courses.minByOrNull { it.capacity }
        val departments: Set<String> = courses.map { it.code.department() }.toSet()

        println("Number of courses: ${courses.size}")
        println("Total credits: $totalCredits")
        println("Courses with 4+ credits: ${advanced.map { it.code }}")
        println("Capacity >= 3: ${large.map { it.code }}, others: ${small.map { it.code }}")
        byLevel.forEach { (level, list) -> println("${level.label}: ${list.map { it.code }}") }
        println("Codes indexed: ${byCode.keys}")
        println("Largest: ${biggest?.code}, smallest: ${smallest?.code}")
        println("Departments: $departments")
        println("Titles: ${courses.flatMap { it.title.split(" ") }.size} words in total")
    }

    fun printEnrollments() {
        section("Enrollments")

        if (enrollments.isEmpty()) {
            println("No students are enrolled.")
            return
        }

        for ((code, ids) in enrollments) {
            val names = ids.mapNotNull { id -> students.find { it.id == id }?.name }
            println("$code -> $names")
        }
    }
}
