package university.util

const val UNIVERSITY_NAME = "KBTU Console University"

fun String.department(): String = substringBefore("-")

fun Int.credits(): String = if (this == 1) "1 credit" else "$this credits"

fun section(title: String) = println("\n--- $title ---")
