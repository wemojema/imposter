# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- SNS event detection and routing
- EventBridge/ScheduledEvent detection and routing
- Kinesis event detection and routing
- CloudFront event detection and routing
- `handleUnknown(InputStream input)` fallback handler
- GitHub Actions CI/CD workflows
- Maven Central publishing support
- Apache 2.0 license

### Changed
- Detection engine replaced string-substring matching with structural JSON inspection
- Routing switch replaced with type-safe class equality checks
- `java.util.Date` replaced with `java.time.Instant` in deserialization

### Fixed
- S3 event detection and deserialization

## [0.4.6] - 2024-01-01

### Changed
- Upgraded AWS Lambda libraries

## [0.4.5]

### Changed
- Removed symphonia from implementation dependencies

## [0.4.4]

### Added
- CloudFront event fixture

### Fixed
- APIGatewayProxyRequestEvent serialization issue
