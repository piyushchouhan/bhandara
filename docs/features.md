# Feature Catalogue

This catalogue describes client capabilities present in the repository as reviewed on **14 October 2026**. “Implemented” means that a client flow exists; it does not establish that its backend is currently deployed or that the feature has completed release acceptance testing.

## Food discovery

| Capability | Implementation |
| --- | --- |
| Nearby shops | Location-based feed with map pins, selection cards, distance information, and detail pages |
| Search | Matches shop names, cuisines, shop types, and listed menu items |
| Moving-vendor browsing | Separate map mode for carts returned in the feed |
| Food-market zones | Backend-provided groups of fixed shops appear as outlines and labels; zooming reveals individual shops |
| Crowd indicators | Shop responses support quiet, moderate, and busy badges; foreground proximity pings contribute to the service |
| Search radius | User-selectable 500 m, 2 km, 5 km, or 10 km; default 5 km |
| External actions | Directions, calling, sharing, or address actions are offered where the screen and available details support them |

## Community feasts

People can browse nearby feasts on a map or in time-grouped lists. Feast details include food, photos, timing, location, and organizer information. Ended events are excluded from the nearby discovery flow.

Organizers can:

- Publish a feast with menu, date, start/end times, location, photos, and optional descriptive/contact information.
- Edit a feast before it ends, end it early, or take it down.
- Repost a past feast with a new date.
- View upcoming, past, and taken-down feasts in a personal list.
- Fix and resubmit an eligible feast removed after reports; the client represents pending and rejected outcomes.

People can save a feast or report incorrect location, timing, non-occurrence, requests for money, or inappropriate content. Reports can include notes and photos.

## Shops and moving-cart listings

Listing creation is a multi-step flow with local draft persistence. Adding a shop requires account sign-in. Standard owners confirm ownership; configured platform administrators can list on behalf of owners through the corresponding interface.

Fixed-shop fields include name, type, menu, photos, address, contact details, and amenities. Moving carts have a dedicated flow for cart type, goods sold, categories and units, usual stops, days out, payment options, seasonal items, and visual identification hints.

Detailed menu items can be entered manually or extracted from menu photos by a backend AI service. Extracted items are editable before saving. The client handles scan failures and rate limits; it does not establish extraction accuracy or operating cost.

## Live carts

Customers can open a cart's tracking sheet to view its current position, connection state, and speed when moving. The subscription reconnects after connection loss and closes when the sheet is dismissed.

Android vendor mode publishes location through a foreground service with an ongoing notification. Location handling distinguishes movement from being parked and sends presence updates to keep a stationary cart visible. iOS can display live positions but has no equivalent vendor publishing service in this repository.

## Ownership and owner tools

Owners can claim eligible platform-listed shops using photos, current location, and optional phone information. The client displays approved, in-review, and rejected outcomes from the backend.

Owner flows include viewing claimed shops, opening the public listing, adding menu items, inspecting reviews, and deactivating an active shop. Manual open/closed control, opening-time editing, permanent shop deletion, reactivation, and owner analytics are not complete; see [roadmap and readiness](roadmap.md).

## Reviews, photos, and trust

- Shop reviews support star ratings, text, photos, editing, and deletion.
- Helpful votes can be added and removed; self-voting is excluded by the client rules.
- Personal review lists distinguish published, in-review, and removed reviews, with filters and sorting.
- Personal photo management supports selection and removal of review photos.
- Nearby verification prompts ask whether a place is real, including the user's current position with a vote.
- Shop, review, and feast reports support follow-up through **My reports**, including additional evidence and withdrawal where eligible.

These are contribution and status-management interfaces. Moderator administration and enforcement are backend responsibilities outside this repository.

## Accounts, saved places, and privacy

Anonymous authentication supports entry without an account signup. Google sign-in supports account-based actions, with explicit sign-in gates for adding and claiming shops.

Saved shops and feasts persist locally, support filtering and sorting, and synchronize with an account through a queued change mechanism. Guest saves can carry into a signed-in account; switching between established accounts avoids copying one account's saves to another. This is targeted offline support, not a fully offline product.

Privacy screens include location information, selective JSON data export, and an account deletion request. The client contract describes deletion of personal account data while retaining contributed shops and feasts without the person's name; actual server retention and deletion need backend verification.

## Preferences and assistance

English, Hindi, and Marathi resource sets are present. Settings cover theme, text size, dietary preferences, starting tab, search radius, and directions app. Notification categories include nearby feasts, report updates, shop reviews, claim status, and quiet hours.

Support pages include product guidance, terms, issue reporting, and feature suggestions. Feedback can include contextual information and an optional screenshot.

## Platform coverage

| Area | Android | iOS |
| --- | --- | --- |
| Shared screens, models, ViewModels, and REST APIs | Implemented | Implemented |
| Native Google Maps integration | Implemented | Implemented through Swift bridge |
| Anonymous and Google authentication | Implemented | Implemented through Swift bridges |
| Camera/gallery and Firebase photo storage | Implemented | Implemented |
| Customer live-cart viewing | Implemented | Implemented |
| Vendor location publishing | Foreground service | Not implemented |
| Push notification delivery and tap routing | FCM service and routes | No delivery integration in inspected source |
| Periodic user-location synchronization | WorkManager scheduling | No equivalent scheduler in inspected source |

Platform support describes source coverage. Device, OS-version, accessibility, and production acceptance checks remain separate release activities.
