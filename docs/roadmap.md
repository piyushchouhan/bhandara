# Roadmap and Readiness

## Current baseline

The repository contains a broad mobile implementation covering shops, carts, community feasts, reviews, saved places, ownership claims, reporting, preferences, and data controls. Most product code is shared between Android and iOS.

The priorities below are proposed from the source review on **14 October 2026**. They are not delivery commitments or evidence of a publicly launched service.

## Known implementation gaps

| Area | Current boundary |
| --- | --- |
| Owner operations | Manual open/closed switching and timing changes show “Coming soon” |
| Shop lifecycle | Deactivation exists; permanent deletion and reactivation are not completed |
| Owner analytics | Weekly statistics lack a connected analytics implementation |
| Profile editing | Name/profile editing and photo changes show “Coming soon” |
| iOS services | Vendor publishing, push delivery, and periodic location synchronization lack Android-equivalent integrations |
| Offline experience | Local saved places and listing drafts are supported; discovery still depends on network services |
| Backend verification | Server implementation, moderator tooling, infrastructure, and deployment status are outside this repository |
| Release configuration | Android points to a local development backend and uses debug signing for the release build configuration |

## Proposed development priorities

### 1. Establish a release baseline

Complete a device-based walkthrough of discovery, posting, ownership, contribution, and privacy flows against the intended backend. Verify permission behavior, notification routing, location accuracy, network recovery, account switching, and moderation outcomes.

Configure production endpoints and signing, review transport settings, and confirm Maps, Firebase, storage, and backend access controls. Document supported OS versions and a tested device matrix; the current Android minimum is API 35, and the iOS project declares iOS 16.0, subject to dependency compatibility validation.

**Evidence to collect:** acceptance results, reproducible builds, deployed service inventory, and release ownership.

### 2. Complete essential provider controls

Prioritize opening-time updates, operational status, and an appropriate reactivation flow. Replace placeholder statistics only when real measurement is available. Decide whether iOS vendor publishing and push delivery are required for the initial target audience.

**Evidence to collect:** completed owner tasks, vendor feedback, field reliability, and support workload.

### 3. Validate a concentrated local pilot

Select a manageable area and build accurate coverage with participating shops, vendors, and organizers. Establish responsibility for listing quality, claim decisions, and report resolution.

**Evidence to collect:** discovery usefulness, listing freshness, repeat use, vendor participation, and moderation response times.

### 4. Test commercial demand

Interview participating merchants, assess willingness to pay, and test a narrow commercial offering after the core discovery experience is reliable. Track Maps, storage, AI, infrastructure, onboarding, and support costs alongside engagement.

**Evidence to collect:** paid-pilot results, cost drivers, retention, and repeatable onboarding economics.

## Scope of readiness claims

Automated tests exist across shared features, but their presence alone is not a production-readiness certification. This documentation update does not represent a new app test run, security audit, legal review, backend audit, or deployment verification.

A complete readiness decision should combine mobile validation with backend evidence, operational processes, and verified business metrics. The [investor brief](investor-brief.md) identifies the business evidence needed for a funding discussion.
