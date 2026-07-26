# Blog Post Sorting - Implementation and Maintenance Guide

## Quick Reference

### Post Sorting Behavior

Blog posts on the homepage are sorted by **effective date**, which is:
- **If post has `updated` field:** `max(updated, date)`
- **If post has NO `updated` field:** `date`

This ensures the most recently active posts (either new or updated) appear at the top.

### Where Posts Are Sorted

1. **Latest Posts** section – on Template 1 (Modern Minimal Dark)
2. **Post Timeline** section – on Template 1
3. **Post Grid** – on Template 2 (Tech Blog Pro)
4. **Posts Timeline** – on Template 3 (Developer Journal)

All use the same sorting algorithm for consistency.

---

## Implementation Details

### Files Involved

| File | Purpose |
|------|---------|
| `blog/assets/js/post-sorting.js` | Sorting algorithm (executed on page load) |
| `blog/_includes/home-template1.html` | Template 1 – includes data attributes and script |
| `blog/_includes/home-template2.html` | Template 2 – includes data attributes and script |
| `blog/_includes/home-template3.html` | Template 3 – includes data attributes and script |
| `blog/SORTING_ALGORITHM.md` | Full algorithm documentation |

### How It Works

1. **Jekyll builds the page** with posts in `date` order (fallback)
2. **JavaScript loads** after DOM is ready
3. **Script reads data attributes:** `data-date` and `data-updated` from each post element
4. **Script sorts** by effective date (using `getEffectiveDate()`)
5. **Script reorders** DOM elements to match sorted order
6. **User sees** correctly sorted posts

### Key Functions in `post-sorting.js`

```javascript
// Calculate effective date for a post
getEffectiveDate(post)

// Sort array of posts by effective date
sortPostsByEffectiveDate(posts)

// Apply sorting to all post containers on page
applyPostSorting()
```

---

## Adding or Updating a Blog Post

### When Creating a New Post (No Updates Yet)

The front matter should look like:
```yaml
---
layout: post
title: "Your Post Title"
description: "SEO description"
date: 2026-07-26 13:00:00 +0000
categories: [interview]
tags: [java, java21, scala]
---
```

**No `updated` field needed yet.** The sorting algorithm will use the `date` field.

### When Updating an Existing Post

Add or update the `updated` field:
```yaml
---
layout: post
title: "Your Post Title"
description: "SEO description"
date: 2026-05-25 15:00:00 +0000
updated: 2026-07-26 14:30:00 +0000
categories: [interview]
tags: [java, java21, scala]
---
```

**The sorting algorithm will now use `max(updated, date)`**, which is `2026-07-26 14:30:00`.

---

## Troubleshooting

### Post Not Appearing at Top After Publishing

**Check 1: Verify date format**
```yaml
date: 2026-07-26 13:00:00 +0000  # ✅ Correct
date: 2026-07-26 13:00:00       # ❌ Missing timezone
```

**Check 2: Verify browser cache**
- Hard refresh: Ctrl+Shift+R (Windows) or Cmd+Shift+R (Mac)
- Or clear browser cache for the site

**Check 3: Verify script is loaded**
1. Open DevTools (F12)
2. Go to Console tab
3. Run: `typeof sortPostsByEffectiveDate` 
4. Should return: `"function"` (not `"undefined"`)

### Post Not Appearing at Top After Updating

**Check 1: Verify `updated` field**
```yaml
updated: 2026-07-26 16:00:00 +0000  # ✅ Present and recent
```

**Check 2: Verify effective date calculation**
1. Open DevTools (F12)
2. Go to Console tab
3. Run this to check a post's effective date:
```javascript
const post = document.querySelector('.post-item');
console.log('Date:', post.dataset.date);
console.log('Updated:', post.dataset.updated);
console.log('Effective:', getEffectiveDate(post));
```

### Sorting Not Working Across Templates

**Check 1: Script is included**
All templates should end with:
```html
<script src="{{ '/assets/js/post-sorting.js' | relative_url }}"></script>
```

**Check 2: Data attributes are present**
All post elements should have:
```html
data-date="2026-07-26T13:00:00Z"
data-updated="2026-07-26T14:30:00Z"
```

---

## Modifying the Sorting Algorithm

To change how posts are sorted, edit `blog/assets/js/post-sorting.js`:

### Example: Sort Newest Updates First (Ignore Creation Date)

```javascript
function getEffectiveDate(post) {
  const dateStr = post.dataset.date;
  const updatedStr = post.dataset.updated;
  
  if (!dateStr) return new Date(0);
  
  try {
    // MODIFIED: Always prefer updated date, fall back to date
    return updatedStr 
      ? new Date(updatedStr) 
      : new Date(dateStr);
  } catch (e) {
    console.error('Error parsing dates for post:', post, e);
    return new Date(0);
  }
}
```

### Example: Sort by Title (Alphabetical)

```javascript
function sortPostsByEffectiveDate(posts) {
  return Array.from(posts).sort((a, b) => {
    const titleA = (a.querySelector('.post-title')?.textContent || '').toLowerCase();
    const titleB = (b.querySelector('.post-title')?.textContent || '').toLowerCase();
    return titleA.localeCompare(titleB);
  });
}
```

**Note:** After making changes, update `blog/SORTING_ALGORITHM.md` to document the new behavior.

---

## Performance Considerations

- **Sorting runs on every page load** – sorting ~30–50 posts takes <5ms
- **No SEO impact** – JavaScript runs after initial render, doesn't affect crawlers
- **Fallback behavior** – if JavaScript fails to load, Jekyll's default sort is used

For large post counts (>200), consider moving sorting to Jekyll build time using a custom plugin.

---

## Testing the Sorting

### Manual Test: Create Test Posts

Create three test posts with known dates:
1. Old post: `date: 2020-01-01`
2. Medium post: `date: 2026-05-01`
3. New post: `date: 2026-07-26`
4. Updated post: `date: 2026-01-01, updated: 2026-07-25`

Expected sort order (top to bottom):
1. Updated post (2026-07-25)
2. New post (2026-07-26)
3. Medium post (2026-05-01)
4. Old post (2020-01-01)

### Automated Test (Node.js)

```javascript
// Requires post-sorting.js to be modified for Node.js compatibility
const { sortPostsByEffectiveDate } = require('./blog/assets/js/post-sorting.js');

const mockPosts = [
  { dataset: { date: '2020-01-01T00:00:00Z', updated: '' } },
  { dataset: { date: '2026-05-01T00:00:00Z', updated: '' } },
  { dataset: { date: '2026-07-26T00:00:00Z', updated: '' } },
  { dataset: { date: '2026-01-01T00:00:00Z', updated: '2026-07-25T00:00:00Z' } }
];

const sorted = sortPostsByEffectiveDate(mockPosts);
console.log('Sorted:', sorted.map(p => p.dataset.date));
```

---

## When to Update This Guide

Update this document when:
- You change the sorting algorithm
- You add new post containers
- You change the data attribute names
- You encounter a new troubleshooting scenario
- You optimize the sorting performance
