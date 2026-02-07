---
name: assertion-refactoring
description: Systematic workflow for refactoring test assertions to use builder patterns while ensuring error message quality through spot-checking
---

# Assertion Refactoring Skill

Use this skill when refactoring test assertions to adopt a new builder pattern or fluent API style across multiple test
files.

## When to Use This Skill

- Introducing a new assertion builder/helper pattern (e.g., Strikt extension properties)
- Migrating from one assertion style to another
- Any change to test assertion patterns that affects error messages
- Ensuring error message quality after assertion API changes

## Process

### 1. Understand the Change

First, review what's being changed:

```bash
git diff --staged
```

Look for:

- New extension properties/functions on assertion builders
- Pattern changes (old style vs new style)
- Required import changes

### 2. Find All Affected Instances

Search comprehensively for the old pattern:

```bash
# Find old-style assertions
grep -rn "OLD_PATTERN" src/test/

# Search specific file types
grep -rn "PATTERN" --include="*Contract.kt" src/test/

# Count total occurrences
grep -r "PATTERN" src/test/ | wc -l
```

Document:

- Number of files affected
- Number of instances per file
- Any variations or edge cases

### 3. Update Files Systematically

**Critical: Work incrementally, one file at a time**

For each file:

1. Make the changes
2. Add any necessary imports
3. Run tests for that specific file
4. Verify compilation and tests pass
5. Move to next file

```bash
# After editing each file
./gradlew test --tests "PackageName.TestClassName" --no-daemon
```

### 4. Verify All Tests Pass

After all files are updated:

```bash
# Run full test suite
./gradlew test --no-daemon

# Verify no old patterns remain
grep -r "OLD_PATTERN" src/test/ || echo "✓ No old-style usages found"
```

### 5. Spot Check Error Messages (Critical Step)

**This step ensures the new pattern produces helpful error messages**

#### Choose Representative Tests

Select 2-3 tests that represent:

- Simple assertions (e.g., `isEqualTo`)
- Complex assertions (e.g., nested `.all {}` or chained assertions)
- Edge cases if applicable

#### For Each Test:

**A. Test with NEW pattern:**

1. Intentionally break the test:
   ```kotlin
   expectThat(result).shrunkValues.isEqualTo(listOf(WRONG_VALUE))
   ```

2. Run the test and capture output:
   ```bash
   ./gradlew test --tests "*TestName" 2>&1 | grep -A 30 "TestName"
   ```

3. Save/note the error output

**B. Test with OLD pattern:**

1. Change to old pattern and break same test:
   ```kotlin
   expectThat(result.shrunkValues).isEqualTo(listOf(WRONG_VALUE))
   ```

2. Run the test again:
   ```bash
   ./gradlew test --tests "*TestName" 2>&1 | grep -A 30 "TestName"
   ```

3. Save/note the error output

**C. Compare:**

Evaluate:

- Does the new error provide enough context?
- Is it as clear or clearer than the old one?
- Are nested assertions still readable?
- Can you identify what failed quickly?

**D. Revert:**

Fix the test back to passing state

#### Repeat for All Selected Tests

### 6. Document Findings

Create a comparison summary:

```markdown
## Error Message Comparison

### Test 1: [Test Name]

**New pattern:**
```

[error output]

```

**Old pattern:**
```

[error output]

```

**Analysis:**
- Context: [better/worse/same]
- Readability: [better/worse/same]
- Verdict: [BETTER/WORSE/ACCEPTABLE]
```

### 7. Make Decision

**If error messages are BETTER or ACCEPTABLE:**

- ✅ Proceed with refactoring
- Document any trade-offs noted

**If error messages are WORSE:**

- ❌ Revert changes
- Reconsider assertion builder design
- Explore alternative approaches

## Example Usage

When you introduce an extension property like:

```kotlin
internal val <T> DescribeableBuilder<GenResults<T>>.shrunkValues get() = get { shrunkValues }
```

You would:

1. Search for `expectThat(.*\.shrunkValues)`
2. Update to `expectThat(result).shrunkValues`
3. Spot check 2-3 tests
4. Compare error outputs
5. Verify improvement

## Checklist

Before completing:

- [ ] All old-pattern usages found via grep
- [ ] All files updated systematically
- [ ] All tests compile
- [ ] All tests pass
- [ ] No old patterns remain (grep verified)
- [ ] At least 2 tests spot-checked
- [ ] Error messages compared and documented
- [ ] Decision: new pattern is better/acceptable
- [ ] All tests restored to passing state

## Common Pitfalls

- Forgetting to add new imports
- Testing only new pattern errors (must compare with old!)
- Not checking nested/complex assertions
- Skipping test verification between files
- Not documenting comparison results
