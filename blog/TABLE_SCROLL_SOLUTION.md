# Table Horizontal Scrolling Solution

## Problem
Wide tables in blog posts overflow on mobile/narrow screens, causing layout issues and making content unreadable.

## Solution
We've implemented a CSS-based horizontal scrolling solution that wraps tables in a scrollable container. This solution:
- ✅ Works with standard Markdown tables
- ✅ Is compatible with Jekyll and GitHub Pages
- ✅ Requires minimal changes to existing content
- ✅ Maintains the dark theme styling

## Implementation

### CSS Changes
The following CSS has been added to `blog/assets/css/template1-minimal-dark.css`:

```css
/* Wrapper for horizontal scrolling on mobile */
.table-wrapper {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  margin: var(--space-lg) 0;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
}

.table-wrapper table {
  margin: 0;
  border: none;
  border-radius: 0;
}
```

Additionally, `white-space: nowrap` was added to `th, td` styles to prevent cell content from wrapping.

### How to Use in Blog Posts

To make a table scrollable on mobile, wrap it with a `<div class="table-wrapper">` element in your Markdown:

```markdown
<div class="table-wrapper" markdown="block">

| Column 1 | Column 2 | Column 3 | Column 4 |
|----------|----------|----------|----------|
| Data 1   | Data 2   | Data 3   | Data 4   |

</div>
```

**Important:** The `markdown="block"` attribute is required for Jekyll to properly parse the Markdown table inside the HTML div.

## Example Usage

See the implementation in:
- `blog/_posts/2025-11-29-collection-factory-methods-and-stream-basics.md`

Three tables in this post have been wrapped:
1. "Key Characteristics" table (4 columns)
2. "Stream.toList() vs Collectors.toList()" table (4 columns)
3. "Summary: Feature Comparison" table (5 columns)

## Testing

The solution has been tested on:
- Desktop view (wide screens)
- Mobile view (375px width)
- The wrapped tables maintain proper styling and enable horizontal scrolling when content is too wide

## Screenshots

Desktop View:
![Desktop View](https://github.com/user-attachments/assets/9192eb4f-2b37-473c-87e8-d1fded9e5cfe)

Mobile View (showing scrollable table):
![Mobile View](https://github.com/user-attachments/assets/0e47a304-d147-4b88-9244-25d6054d1f3a)

## When to Use

Use this wrapper for tables that:
- Have 4 or more columns
- Contain long code snippets or method names
- Are comparison tables with multiple languages/frameworks
- Would be difficult to read if content wraps

## Browser Support

This solution uses standard CSS properties supported by all modern browsers:
- `overflow-x: auto` - enables horizontal scrolling
- `-webkit-overflow-scrolling: touch` - smooth scrolling on iOS devices
- `white-space: nowrap` - prevents cell content from wrapping

## Future Considerations

If you want to apply this to ALL tables automatically (without manual wrapping), you could:
1. Use JavaScript to automatically wrap tables on page load
2. Create a Jekyll plugin to process tables during build
3. Modify the Markdown renderer configuration

However, the current manual approach gives more control over which tables need scrolling.
