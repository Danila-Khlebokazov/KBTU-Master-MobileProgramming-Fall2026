package university

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import university.model.*
import university.service.University
import university.util.UNIVERSITY_NAME
import university.util.credits
import university.util.section

fun main() = runBlocking {
    val courses = mutableListOf(
        Course("CS-101", "Programming Fundamentals", 4, 2),
        Course("CS-205", "Object-Oriented Programming", 5, 3, Level.INTERMEDIATE),
        Course("MATH-110", "Discrete Mathematics", 3, 4),
        Course("DB-210", "Database Systems", 4, 2, Level.ADVANCED)
    )

    val students = mutableListOf(
        Student(1, "Danila", 1),
        Student(2, "Dias", 2),
        Student(3, "Madina", 2)
    )

    val instructor = Instructor(100, "Dr. Science", "Computer Science")
    val university = University(UNIVERSITY_NAME, courses, students)

    println("================================")
    println("     ${university.name}")
    println("================================")

    section("People")
    val people: List<Displayable> = listOf(*students.toTypedArray(), instructor)
    people.withIndex().forEach { (i, p) -> println("${i + 1}. ${p.displayInfo()}") }

    section("Courses with 4+ credits")
    university.findCourses { it.credits >= 4 }.forEach {
        println("${it.displayInfo()} - ${it.credits.credits()}")
    }
    println("Low capacity: ${university.lowCapacityCourses().map { it.code }}")

    university.printCourseStatistics()

    section("Enrollment")
    val requests = listOf(
        students[0] to "CS-101",
        students[1] to "CS-101",
        students[2] to "CS-101",
        students[0] to "CS-101"
    )

    // Requests run concurrently
    val results = requests
        .map { (student, courseCode) -> async { student to university.enroll(student, courseCode) } }
        .awaitAll()

    for ((student, result) in results) {
        val id = EnrollmentIds.next()
        when (result) {
            is EnrollmentResult.Success ->
                println("#$id ${student.name} successfully enrolled in ${result.course.code}.")

            is EnrollmentResult.Failed ->
                println("#$id Enrollment failed for ${student.name}: ${result.reason}")
        }
    }

    university.printEnrollments()

    section("Student Years")
    for (student in students) {
        if (student.year >= 2) {
            println("${student.name} is a continuing student.")
        } else {
            println("${student.name} is a first-year student.")
        }
    }

    section("Grades")
    val score = 60
    println("Student passes with $score: ${students[0].isPassed(score)}")
    println("Instructor passes with $score: ${instructor.isPassed(score)}")

    println("\nApplication finished.")
}
