# Local Feast

## Discover the food around you

Local Feast brings nearby food shops, moving vendors, and community feasts into one location-based experience. People can explore what is available, inspect menus and reviews, follow a live cart, or find a bhandara happening nearby. Shop owners and organizers can contribute information directly, while community verification and reporting help surface inaccurate listings.

The product is developed in the **Bhandara** repository. Its interface uses the **Local Feast** name.

## One product, three discovery needs

| Experience | What people need | Product response |
| --- | --- | --- |
| Local shops | Find food nearby and decide where to go | Searchable map, shop details, menus, prices, reviews, photos, and crowd indicators |
| Moving vendors | Find a vendor whose location changes | Cart profiles, usual stops, and live location viewing; Android vendor mode publishes location |
| Community feasts | Discover food that is available at a particular place and time | Nearby map and list, event timings, menus, organizer details, and feast lifecycle controls |

## Who the product serves

**People looking for food** can browse nearby options without first completing an account signup, save places, and use directions or contact actions where available.

**Shop owners and mobile vendors** can create listings, describe their offering, add menus, and establish ownership through a claim flow. Moving-cart profiles include practical details such as usual stops, payment methods, and how to recognize the cart.

**Community organizers** can publish feasts with photos, location, timings, and food details, then edit, end, remove, or repost them as circumstances change.

**Community contributors** can review shops, share photos, verify nearby places, report problems, and follow the status of their reports.

## Product foundations

- Shared Kotlin Multiplatform and Compose code supports Android and iOS experiences.
- Google Maps provides map presentation on both platforms through native integrations.
- Firebase supports authentication and photo storage; Android also integrates push notifications.
- REST APIs provide product data, and STOMP over WebSockets delivers live cart positions.
- English, Hindi, and Marathi resources support a locally relevant interface.
- Saved places have local persistence and account synchronization; privacy screens include data export and account deletion requests.

## Current status

The repository contains implemented client flows across discovery, contribution, ownership, and personal account management. Some owner and profile controls remain placeholders. Android supports vendor location publishing and push notification handling; equivalent iOS services are not implemented in the inspected source.

This documentation describes the mobile implementation reviewed on **14 October 2026**. Backend behavior is described through the client contract, since server source and deployment evidence are outside this repository. Public availability, user adoption, revenue, and operating performance require separate evidence.

## Continue reading

Start with the [investor brief](investor-brief.md) for the product thesis and validation priorities, the [feature catalogue](features.md) for implementation scope, or the [roadmap and readiness assessment](roadmap.md) for remaining work. Technical readers can continue to the [architecture](architecture.md) and [setup guide](getting-started.md).
