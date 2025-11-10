# Versioning Strategy

This document describes the automatic versioning and release management strategy for the demo-authentication project.

## Overview

This project uses [Semantic Versioning 2.0.0](https://semver.org/) with automated version bumps, tagging, and release creation through GitHub Actions.

## Semantic Versioning Format

Versions follow the format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes or major new features that may require migration
- **MINOR**: New features that are backward compatible
- **PATCH**: Bug fixes, dependency updates, and minor improvements

## Automatic Version Bumping

The versioning system automatically determines the appropriate version bump based on the branch name or PR metadata when merging to `master` or `main`.

### Version Bump Rules

#### Major Version Bump (X.0.0)

Triggered by:
- Branch names starting with `major/`
  - Example: `major/api-v2`, `major/breaking-changes`
- PR titles containing `[major]`
  - Example: `[major] Redesign authentication API`
- PR body or commit message containing `BREAKING CHANGE`
  - Example: `BREAKING CHANGE: Remove deprecated endpoints`

**Effect**: `1.2.3` → `2.0.0` (resets minor and patch to 0)

#### Minor Version Bump (x.Y.0)

Triggered by:
- Branch names starting with `feature/`
  - Example: `feature/oauth-support`, `feature/add-mfa`
- PR titles containing `[feature]`
  - Example: `[feature] Add OAuth2 authentication`
- Commit messages starting with `feat:`
  - Example: `feat: add support for social login`

**Effect**: `1.2.3` → `1.3.0` (resets patch to 0)

#### Patch Version Bump (x.y.Z)

Triggered by:
- Branch names starting with `fix/` or `bugfix/`
  - Example: `fix/login-redirect`, `bugfix/token-expiry`
- PRs from Dependabot
  - Example: `dependabot/maven/spring-boot-3.5.7`
- PR titles containing `[fix]` or `[patch]`
  - Example: `[fix] Correct password validation`
- Commit messages starting with `fix:`
  - Example: `fix: resolve token refresh issue`
- Dependabot commit messages
  - Example: `chore(maven): bump spring-boot from 3.5.6 to 3.5.7`

**Effect**: `1.2.3` → `1.2.4`

## Workflow Behavior

### Trigger

The release workflow triggers on:
```yaml
on:
  push:
    branches:
      - master
      - main
```

### Automated Steps

1. **Fetch Latest Version**: Retrieves the latest git tag (or defaults to `v0.0.0`)

2. **Analyze PR/Commit**: 
   - Extracts PR number from merge commit message
   - Fetches PR details via GitHub API
   - Examines branch name, PR title, and PR body

3. **Determine Version Bump**: Applies rules to decide major/minor/patch bump

4. **Calculate New Version**: Increments the appropriate version component

5. **Update Version Files**:
   - `pom.xml`: Updates `<revision>` property
   - `frontend/package.json`: Updates `version` field

6. **Generate Changelog**: 
   - Extracts commits since last release
   - Prepends new version section to `CHANGELOG.md`
   - Includes date and commit list

7. **Commit Changes**: 
   - Commits updated files with message: `chore: bump version to X.Y.Z [skip ci]`
   - The `[skip ci]` prevents infinite workflow loops

8. **Create Git Tag**: 
   - Creates annotated tag: `vX.Y.Z`
   - Pushes tag to repository

9. **Create GitHub Release**: 
   - Creates release with tag
   - Includes release notes and changelog link

## Special Cases

### Direct Commits (No PR)

For direct commits to master/main (not recommended), the workflow:
- Analyzes commit message instead of PR metadata
- Uses conventional commit prefixes (`feat:`, `fix:`, `BREAKING CHANGE`)
- Defaults to patch bump if no pattern matches

### First Release

If no tags exist in the repository:
- Starts from `v0.0.0`
- First release will be `v0.0.1` (patch), `v0.1.0` (minor), or `v1.0.0` (major)
- Includes up to 20 most recent commits in changelog

### Skip CI

Commits containing `[skip ci]` in the message are ignored by the workflow to prevent recursion when the workflow itself commits version updates.

## Examples

### Example 1: Feature Branch

```bash
# Create feature branch
git checkout -b feature/add-oauth
# Make changes and commit
git commit -m "Add OAuth2 support"
# Push and create PR
git push origin feature/add-oauth
# Merge PR to main
```

**Result**: Version bumps from `1.2.3` to `1.3.0`

### Example 2: Bug Fix

```bash
# Create fix branch
git checkout -b fix/session-timeout
# Make changes and commit
git commit -m "Fix session timeout issue"
# Push and create PR
git push origin fix/session-timeout
# Merge PR to main
```

**Result**: Version bumps from `1.3.0` to `1.3.1`

### Example 3: Dependabot Update

Dependabot automatically creates PR with branch like:
```
dependabot/maven/org.springframework.boot-spring-boot-starter-parent-3.5.7
```

**Result**: Version bumps from `1.3.1` to `1.3.2` when merged

### Example 4: Breaking Change

```bash
# Create major version branch
git checkout -b major/remove-legacy-api
# Make changes and commit
git commit -m "Remove deprecated v1 API endpoints

BREAKING CHANGE: v1 API endpoints have been removed"
# Push and create PR
git push origin major/remove-legacy-api
# Merge PR to main
```

**Result**: Version bumps from `1.3.2` to `2.0.0`

## Manual Override

You can override the automatic detection by including markers in your PR title or commit message:

- `[major]` - Force major version bump
- `[feature]` or `[minor]` - Force minor version bump
- `[fix]` or `[patch]` - Force patch version bump

**Example**: A PR from branch `refactor/improve-code` with title `[feature] Improve authentication flow` will trigger a minor version bump.

## Viewing Releases

All releases are available at:
```
https://github.com/markp07/demo-authentication/releases
```

Each release includes:
- Version tag (e.g., `v1.2.3`)
- Release notes with version bump type
- Link to full changelog
- Commit history since last release

## Troubleshooting

### Workflow Not Running

- Ensure the commit message doesn't contain `[skip ci]`
- Check that you're pushing to `master` or `main` branch
- Verify GitHub Actions are enabled for the repository

### Wrong Version Bump

- Check the branch name matches the expected pattern
- Verify PR title if branch name doesn't match patterns
- Review the workflow logs to see how the version was determined

### Changelog Not Updating

- Ensure `CHANGELOG.md` exists in the repository root
- Check that the file has the expected format with headers
- Review workflow logs for any errors during changelog generation

## Best Practices

1. **Use Descriptive Branch Names**: Follow the naming conventions to ensure correct version bumps

2. **Write Clear PR Titles**: Include relevant markers if branch name doesn't convey the change type

3. **Document Breaking Changes**: Always include `BREAKING CHANGE` in commit messages for major version bumps

4. **Review Before Merge**: Check that the correct version bump will occur before merging to main

5. **Keep CHANGELOG Updated**: The workflow handles this automatically, but review the format periodically

6. **Use Conventional Commits**: Following conventional commit format improves changelog readability

## Disabling Automatic Releases

To temporarily disable automatic releases, you can:

1. Add `[skip ci]` to your merge commit message
2. Disable the workflow in `.github/workflows/release.yml`
3. Use a different branch name that doesn't trigger the workflow

## Future Enhancements

Potential improvements to consider:

- Pre-release versions (alpha, beta, rc)
- Changelog categorization (Added, Changed, Fixed, Deprecated, Removed, Security)
- Release notes from PR descriptions
- Automated GitHub release assets (build artifacts, Docker images)
- Integration with project management tools
- Automated dependency update notifications
