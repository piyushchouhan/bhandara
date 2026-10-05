package com.example.bhandara.ui.reportfeast

import com.example.bhandara.photos.PickedPhoto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

// The backend's limits for a feast (FeastRequestDTO)
const val MAX_FEAST_PHOTOS = 10
const val MAX_FEAST_MENU_ITEMS = 50
const val MAX_FEAST_DESCRIPTION = 1000
const val MAX_FEAST_ADDRESS = 500
const val MAX_FEAST_LANDMARK = 255
const val MAX_FEAST_CAPACITY = 100_000

/** The three steps of posting a feast */
enum class FeastStep { FOOD, WHEN, WHERE }

/** What the user has filled in so far */
data class FeastForm(
    val photos: List<PickedPhoto> = emptyList(),
    val menuItems: List<String> = emptyList(),
    /** An item being typed, not added yet; counts as added when the user goes on */
    val menuInput: String = "",
    val foodType: String = "",
    val description: String = "",
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val organizerName: String = "",
    val contactPhone: String = "",
    val address: String = "",
    val landmark: String = "",
    val capacity: String = "",
) {
    /** The menu with the item still being typed */
    val allMenuItems: List<String>
        get() = (menuItems + menuInput.trim()).filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
}

/** The first thing to fix before the feast can be posted, and the step it's on */
data class FormProblem(val step: FeastStep, val message: String)

/** "98765 43210" or "+91-98765-43210" as the digits the backend accepts, or null if it isn't a phone number */
fun normalizePhone(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return null
    val digits = trimmed.filter { it.isDigit() }
    if (trimmed.any { !it.isDigit() && it !in " -()+" } || digits.length !in 10..15) return null
    return (if (trimmed.startsWith("+")) "+" else "") + digits
}

/**
 * The first problem with the form, checking the given steps in order; null when they're fine. [now] is the phone's
 * local time: a feast can't be in the past, nor already over today. A feast whose end is before its start runs past
 * midnight, so it ends the next day.
 */
fun FeastForm.problem(now: LocalDateTime, steps: Collection<FeastStep> = FeastStep.entries): FormProblem? {
    if (FeastStep.FOOD in steps) {
        if (photos.isEmpty()) return FormProblem(FeastStep.FOOD, "Add at least one photo of the food or the place")
        if (allMenuItems.isEmpty()) return FormProblem(FeastStep.FOOD, "Add what's being served")
        if (allMenuItems.size > MAX_FEAST_MENU_ITEMS) return FormProblem(FeastStep.FOOD, "A feast can list up to $MAX_FEAST_MENU_ITEMS items")
    }
    if (FeastStep.WHEN in steps) {
        val date = date ?: return FormProblem(FeastStep.WHEN, "Choose the date")
        val start = startTime ?: return FormProblem(FeastStep.WHEN, "Choose when it starts")
        val end = endTime ?: return FormProblem(FeastStep.WHEN, "Choose when it ends")
        if (date < now.date) return FormProblem(FeastStep.WHEN, "The date can't be in the past")
        if (start == end) return FormProblem(FeastStep.WHEN, "It can't end when it starts")
        val endsToday = end > start
        if (date == now.date && endsToday && end <= now.time) return FormProblem(FeastStep.WHEN, "This feast would already be over")
        if (contactPhone.isNotBlank() && normalizePhone(contactPhone) == null) {
            return FormProblem(FeastStep.WHEN, "Enter a phone number with 10 to 15 digits")
        }
    }
    if (FeastStep.WHERE in steps && capacity.isNotBlank()) {
        val people = capacity.trim().toIntOrNull()
        if (people == null || people !in 1..MAX_FEAST_CAPACITY) {
            return FormProblem(FeastStep.WHERE, "Enter how many people, as a number from 1 to $MAX_FEAST_CAPACITY")
        }
    }
    return null
}
