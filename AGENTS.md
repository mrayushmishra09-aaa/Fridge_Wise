# FridgeWise Developer Agents

This file documents the specialized systems and architectural decisions made for FridgeWise.

## Reminder System
The reminder system is the "heart" of the application, designed for industry-level resilience and user intimacy.
- **Testing Strategy:** See [docs/testing.md](docs/testing.md)

## Intelligent Engines
- **NotificationPersonalityEngine:** Provides adaptive, human-like messaging based on user interaction history.
- **WorkManager Integration:** Guarantees data consistency for background actions.
