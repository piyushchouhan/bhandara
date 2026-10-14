package com.example.bhandara.navigation

import kotlinx.serialization.Serializable

/** A screen in the app, with what it needs to open */
@Serializable
sealed interface Route {

    /** The map of shops and live carts, with the side menu; always at the bottom of the back stack */
    @Serializable
    data object Home : Route

    /** Community feasts near the user */
    @Serializable
    data object Hungry : Route

    /** Post a community feast */
    @Serializable
    data object ReportFeast : Route

    @Serializable
    data class FeastDetails(val feastId: String) : Route

    @Serializable
    data object AddShop : Route

    @Serializable
    data class ShopDetails(val shopId: String) : Route

    /** A shop's reviews, with the shop's name and first photo for the header */
    @Serializable
    data class ShopReviews(val shopId: String, val shopName: String, val photoUrl: String? = null) : Route

    /**
     * Writing a review of a shop, or editing your own, full screen
     *
     * @param rating stars already picked (e.g. tapped on "No reviews yet"); 0 for none
     */
    @Serializable
    data class WriteReview(
        val shopId: String,
        val shopName: String,
        val photoUrl: String? = null,
        val rating: Int = 0,
    ) : Route

    /** The owner claims a shop listed for them */
    @Serializable
    data class ClaimShop(val shopId: String, val shopName: String) : Route

    @Serializable
    data object Profile : Route

    /** The user's own reviews, from the profile */
    @Serializable
    data object MyReviews : Route

    /** The photos in the user's reviews, from the profile */
    @Serializable
    data object MyPhotos : Route

    /** The shops and feasts the user saved on this phone, from the profile */
    @Serializable
    data object SavedPlaces : Route

    /**
     * Privacy and data, from the profile or Settings; [startAtDelete] opens straight on deleting the account,
     * [startAtLocation] on location access
     */
    @Serializable
    data class Privacy(val startAtDelete: Boolean = false, val startAtLocation: Boolean = false) : Route

    /** The app's settings, from the side menu */
    @Serializable
    data object Settings : Route

    /** The reports the user sent, from the profile */
    @Serializable
    data object MyReports : Route

    /** The feasts the user posted, from the profile */
    @Serializable
    data object MyFeasts : Route

    /** Changing one of the user's feasts that hasn't ended (the post form, filled in) */
    @Serializable
    data class EditFeast(val feastId: String) : Route

    /** Posting a past feast again: the post form with its details and a new date */
    @Serializable
    data class RepostFeast(val feastId: String) : Route

    /** The owner manages a shop of theirs, from the profile */
    @Serializable
    data class ManageShop(val shopId: String) : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Support : Route

    @Serializable
    data object Terms : Route

    @Serializable
    data object ReportIssue : Route

    @Serializable
    data object SuggestFeature : Route

    @Serializable
    data object HowToUse : Route
}
