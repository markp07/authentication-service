# Release Workflow Testing Guide

This document provides instructions for testing the automatic versioning and release workflow.

## Overview

The release workflow (`.github/workflows/release.yml`) automatically creates versions, tags, and releases when PRs are merged to `master` or `main`.

## Testing Scenarios

### Scenario 1: Test Patch Version Bump

**Purpose**: Verify that bug fixes trigger a patch version bump.

**Steps**:
1. Create a branch: `git checkout -b fix/test-patch-bump`
2. Make a small change (e.g., update a comment in README.md)
3. Commit: `git commit -am "Fix typo in documentation"`
4. Push and create PR: `git push origin fix/test-patch-bump`
5. Merge PR to main

**Expected Result**: 
- Version bumps from `1.0.0` to `1.0.1`
- New tag `v1.0.1` is created
- GitHub release is published
- CHANGELOG.md is updated

### Scenario 2: Test Minor Version Bump

**Purpose**: Verify that features trigger a minor version bump.

**Steps**:
1. Create a branch: `git checkout -b feature/test-minor-bump`
2. Make a change that represents a new feature
3. Commit: `git commit -am "Add new configuration option"`
4. Push and create PR: `git push origin feature/test-minor-bump`
5. Merge PR to main

**Expected Result**:
- Version bumps from `1.0.1` to `1.1.0`
- New tag `v1.1.0` is created
- GitHub release is published
- CHANGELOG.md is updated

### Scenario 3: Test Major Version Bump

**Purpose**: Verify that breaking changes trigger a major version bump.

**Steps**:
1. Create a branch: `git checkout -b major/test-major-bump`
2. Make a significant change
3. Commit: `git commit -am "BREAKING CHANGE: Update API structure"`
4. Push and create PR: `git push origin major/test-major-bump`
5. Merge PR to main

**Expected Result**:
- Version bumps from `1.1.0` to `2.0.0`
- New tag `v2.0.0` is created
- GitHub release is published
- CHANGELOG.md is updated

### Scenario 4: Test Dependabot PR

**Purpose**: Verify that Dependabot PRs trigger a patch version bump.

**Prerequisites**: Dependabot must be enabled

**Steps**:
1. Wait for Dependabot to create a dependency update PR
2. Review and merge the PR

**Expected Result**:
- Version bumps patch number (e.g., `2.0.0` to `2.0.1`)
- New tag is created
- GitHub release is published
- CHANGELOG.md is updated with dependency update

### Scenario 5: Test [skip ci] Behavior

**Purpose**: Verify that the workflow doesn't run in a loop.

**Steps**:
1. After any of the above scenarios complete
2. Check that the version bump commit includes `[skip ci]`
3. Verify that the workflow did not run again for the version bump commit

**Expected Result**:
- Version bump commit is made with message: `chore: bump version to X.Y.Z [skip ci]`
- Workflow does not trigger on this commit
- No infinite loop occurs

## Manual Testing with GitHub API

You can also simulate the workflow behavior locally:

```bash
# Get latest tag
LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
echo "Latest tag: $LATEST_TAG"

# Extract version
VERSION=${LATEST_TAG#v}
echo "Current version: $VERSION"

# Calculate next patch version
IFS='.' read -ra VERSION_PARTS <<< "$VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]}
PATCH=$((PATCH + 1))
NEW_VERSION="$MAJOR.$MINOR.$PATCH"
echo "New patch version would be: $NEW_VERSION"
```

## Verifying Results

After each test, verify:

1. **Git Tags**: Check that the tag was created
   ```bash
   git fetch --tags
   git tag -l
   ```

2. **GitHub Releases**: Visit `https://github.com/markp07/demo-authentication/releases`
   - Verify the release exists
   - Check the release notes

3. **Version Files**: Check that versions were updated
   ```bash
   # Check pom.xml
   grep "<revision>" pom.xml
   
   # Check package.json
   grep "\"version\"" frontend/package.json
   ```

4. **CHANGELOG**: Verify CHANGELOG.md was updated
   ```bash
   head -20 CHANGELOG.md
   ```

5. **Workflow Logs**: Check GitHub Actions logs
   - Go to Actions tab in GitHub
   - Find the "Release" workflow run
   - Review each step's output

## Troubleshooting Test Issues

### Workflow Didn't Run

**Check**:
- Ensure PR was merged to `master` or `main` (not another branch)
- Verify commit message doesn't contain `[skip ci]`
- Check if GitHub Actions are enabled for the repository

**Fix**:
- Push another commit without `[skip ci]`
- Enable GitHub Actions in repository settings

### Wrong Version Bump

**Check**:
- Review branch name pattern
- Check PR title for version markers
- Review workflow logs to see how version was determined

**Fix**:
- Ensure branch names follow the convention (major/*, feature/*, fix/*, bugfix/*)
- Add version marker to PR title if needed ([major], [feature], [fix])

### Version Files Not Updated

**Check**:
- Review workflow logs for errors
- Check if files were committed

**Fix**:
- Verify file paths in workflow are correct
- Check sed commands completed successfully

### Release Not Created

**Check**:
- Verify GITHUB_TOKEN has correct permissions
- Check if tag was created
- Review gh CLI output in logs

**Fix**:
- Ensure `contents: write` permission is set in workflow
- Manually create release if needed: `gh release create vX.Y.Z`

## Dry Run Testing

To test the workflow logic without actually creating releases:

1. Create a test branch from your feature branch
2. Modify the workflow to add `--dry-run` flags (where applicable)
3. Push to trigger the workflow
4. Review logs without side effects

## Best Practices for Testing

1. **Test in Order**: Run patch → minor → major to see version progression
2. **Keep Track**: Document the version at each step
3. **Review Logs**: Always check workflow logs for unexpected behavior
4. **Clean Up**: After testing, you may want to delete test tags/releases
5. **Backup**: Consider testing on a fork first

## Cleanup After Testing

If you need to remove test versions:

```bash
# Delete local tag
git tag -d v1.0.1

# Delete remote tag
git push origin :refs/tags/v1.0.1

# Delete release via GitHub UI or CLI
gh release delete v1.0.1
```

## Success Criteria

The workflow is working correctly when:

✅ Version bumps follow semantic versioning rules  
✅ Tags are created automatically  
✅ GitHub releases are published  
✅ CHANGELOG.md is updated  
✅ Version files (pom.xml, package.json) are updated  
✅ Workflow doesn't run in infinite loops  
✅ All tests pass for different bump types  

## Notes

- First release will start from `v0.0.1` if no tags exist
- The workflow analyzes PR metadata, so direct commits may not work as expected
- Use PR merge commits for best results
- Branch naming is the primary way to control version bumps
