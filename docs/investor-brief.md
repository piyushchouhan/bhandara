# Investor Brief

## Product thesis

Local Feast aims to make neighborhood food easier to discover, including food that moves or is available only for a limited time. Its product combines permanent shops, mobile vendors, and community feasts within a shared map-based experience.

The opportunity being tested is whether accurate local information can create recurring value for both people looking for food and the people providing it. The mobile implementation provides a foundation for testing that thesis; commercial demand and operating performance still need to be demonstrated.

## The problem the product addresses

A fixed address is only part of food discovery. A moving cart changes location, a community feast has a start and end time, and a neighborhood shop needs current information about what it serves. Local Feast is designed around those differences rather than treating every food provider as a static listing.

The product also gives contributors ways to challenge inaccurate information. Reviews, nearby verification prompts, reports with evidence, ownership claims, and organizer corrections are part of the client experience.

## Value for each participant

| Participant | Intended value | Existing product capability |
| --- | --- | --- |
| Customer | Find a suitable nearby food option | Shop and feast maps, search, menus, reviews, saved places, and live cart viewing |
| Shop owner | Make a local business discoverable | Listing creation, ownership claims, menu entry, and review visibility |
| Moving vendor | Be found as the business moves | Cart profiles, usual stops, and Android live location sharing |
| Feast organizer | Help nearby people find an event and receive accurate details | Posting, editing, ending early, reposting, and correction after takedown |
| Community contributor | Improve the quality of local information | Reviews, photos, helpful votes, verification, and report follow-up |

## What has been built

The repository includes shared Android and iOS screens, feature ViewModels, typed API integrations, native maps, authentication, photo uploads, local preferences, and automated tests. The product extends beyond feast reporting into shop discovery, moving-cart profiles, AI-assisted menu entry, saved-place synchronization, and user data controls.

Android has additional operational integrations for FCM notifications and foreground vendor tracking. Customer cart viewing is shared across platforms. See the [feature catalogue](features.md) for scope and the [readiness assessment](roadmap.md) for incomplete controls and release dependencies.

## Differentiation to validate

The intended differentiation is the combination of **fixed, moving, and time-limited food availability** in one product. Supporting features include food-market zones on the map, cart-specific profiles, regional languages, and community contributions.

These are product design choices, not evidence of a competitive moat. Defensibility would need to emerge from reliable local coverage, returning users, participating vendors, and effective trust operations.

## Potential business models

The following are commercial hypotheses, not implemented revenue streams or pricing commitments:

- **Merchant subscriptions:** paid tools that help owners maintain listings and understand customer interest, once useful owner analytics exist.
- **Clearly identified promoted discovery:** optional paid visibility for relevant local businesses, subject to maintaining useful organic discovery.
- **Local partnerships:** collaborations with markets, merchant groups, or community organizations to improve area coverage and onboarding.

Each model needs customer interviews, willingness-to-pay testing, delivery-cost analysis, and evidence that it improves participant outcomes. The current repository does not establish payments, paid subscriptions, advertising infrastructure, or transactional delivery operations.

## Proposed path to validation

Begin with a concentrated geographic pilot where listing quality can be checked and vendors can be supported directly. Measure whether people find useful options and return, whether vendors keep information current, and whether community contributions improve accuracy.

| Question | Suggested evidence |
| --- | --- |
| Is coverage useful? | Verified active listings, geographic density, and successful discovery sessions |
| Do people return? | Retention cohorts and repeat discovery or saved-place use |
| Do vendors receive value? | Active participating vendors, sharing sessions, and owner feedback |
| Is the information trustworthy? | Listing freshness, report rates, resolution times, and claim outcomes |
| Can the service operate economically? | Maps, storage, AI, infrastructure, onboarding, and moderation cost per active participant |
| Will merchants pay? | Structured interviews and controlled paid pilots after the core experience is reliable |

These metrics are proposed measurement targets. No values are asserted here.

## Investment discussion

The immediate case for further development is the breadth of the implemented product and its shared mobile foundation. The next evidence should come from completing essential owner controls, validating the service in real operating conditions, and demonstrating a repeatable local adoption model.

A fundraising data room should supplement these docs with a live product demonstration, release status, traction cohorts, vendor evidence, operating costs, team background, funding requirement, and use of funds. Those materials are not established by this codebase and should be supplied from verified business records.

*Prepared from the mobile repository reviewed on 14 October 2026. This brief presents the product thesis and validation plan; it does not claim market size, revenue, adoption, or investment returns.*
