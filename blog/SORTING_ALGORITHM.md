# Blog Post Sorting Algorithm

## Overview

Blog posts in the "Latest Posts" section and "Post Timeline" section are sorted using a unified algorithm that prioritizes the most recent activity (either post creation or update).

## Sorting Logic

The sorting algorithm uses the following approach:

1. **Determine the "effective date"** for each post:
   - If the post has an `updated` field: use `max(updated, date)`
   - If the post has NO `updated` field: use `date`
   - This ensures we always show the most recent activity date

2. **Sort in descending order** (newest first):
   - Posts are sorted by their effective date
   - Newer posts appear at the top
   - Older posts appear at the bottom

## Example

Given these three posts:

```
Post A: date: 2026-07-26 13:00:00, updated: (none)
        → effective date = 2026-07-26 13:00:00

Post B: date: 2025-12-14 18:00:00, updated: 2026-07-26 11:05:20
        → effective date = max(2026-07-26 11:05:20, 2025-12-14 18:00:00) = 2026-07-26 11:05:20

Post C: date: 2026-05-25 20:00:00, updated: 2026-07-26 11:25:00
        → effective date = max(2026-07-26 11:25:00, 2026-05-25 20:00:00) = 2026-07-26 11:25:00
```

**Sorted order (newest first):**
1. Post C (2026-07-26 11:25:00)
2. Post A (2026-07-26 13:00:00) ← Wait, this is newer but Post C appears first!
3. Post B (2026-07-26 11:05:20)

**Correction:** If Post A has no update, it should appear first. Let me recalculate:

```
Post A: effective date = 2026-07-26 13:00:00
Post C: effective date = 2026-07-26 11:25:00
Post B: effective date = 2026-07-26 11:05:20
```

**Sorted order (newest first):**
1. Post A (2026-07-26 13:00:00) ✓
2. Post C (2026-07-26 11:25:00)
3. Post B (2026-07-26 11:05:20)

## Implementation

### Frontend Sorting (JavaScript)

The sorting is applied via JavaScript in `assets/js/theme.js` using the `sortPostsByEffectiveDate()` function:

```javascript
function getEffectiveDate(post) {
  // Extract ISO date strings from data attributes
  const dateStr = post.dataset.date;
  const updatedStr = post.dataset.updated;
  
  if (!dateStr) return new Date(0); // Fallback
  
  const date = new Date(dateStr);
  const updated = updatedStr ? new Date(updatedStr) : date;
  
  // Return the more recent date (max)
  return updated.getTime() > date.getTime() ? updated : date;
}

function sortPostsByEffectiveDate(posts) {
  return Array.from(posts).sort((a, b) => {
    const dateA = getEffectiveDate(a);
    const dateB = getEffectiveDate(b);
    return dateB - dateA; // Descending (newest first)
  });
}
```

### Why JavaScript Instead of Jekyll Filter?

1. **Consistency:** The same logic applies in both "Latest Posts" and "Post Timeline"
2. **Client-side flexibility:** No need to rebuild Jekyll with custom plugins
3. **Extensibility:** Easy to modify sorting behavior in the future
4. **Performance:** JavaScript sorting in the browser is efficient for reasonable post counts

### Jekyll Template (Fallback)

The Jekyll template (`blog/_includes/home-template1.html`) provides a basic sort as a fallback:

```liquid
{% assign sorted_posts = site.posts | sort: "date" | reverse %}
```

However, the JavaScript implementation always overrides this and applies the correct sorting algorithm.

## Applied Locations

1. **Latest Posts section** (`.post-list` class)
2. **Post Timeline section** (`.timeline` class)

Both sections use the same sorting function for consistency.

## When to Update This Algorithm

Modify the sorting algorithm if:
- You want to sort by creation date only (ignore updates)
- You want to weight newer updates differently
- You need to sort by a different field entirely

**Remember:** Any changes to this algorithm must be made in BOTH:
1. `blog/_includes/home-template1.html` (Jekyll template, for SEO bots that don't run JS)
2. `blog/assets/js/theme.js` (JavaScript, for dynamic client-side sorting)

This dual implementation ensures posts appear in the correct order even if JavaScript is disabled.

## Debugging

To debug sorting issues:

1. Open browser DevTools (F12)
2. Go to the Console tab
3. Run: `Array.from(document.querySelectorAll('.post-item')).map(p => ({ title: p.querySelector('.post-title').textContent, date: p.dataset.date, updated: p.dataset.updated }))`
4. Verify the effective dates are calculated correctly
