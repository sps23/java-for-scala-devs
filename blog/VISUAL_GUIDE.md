# Category Filtering Feature - Visual Guide

## Quick Start

Open these files in your browser to see the feature:
1. `category-preview-default.html` - Shows all categories
2. `category-preview-filtered.html` - Shows filtered view (Interview only)

## How to View the Screenshots

### Option 1: Open in Browser
```bash
cd ../Workspace/java-for-scala-devs/blog
open category-preview-default.html
open category-preview-filtered.html
```

### Option 2: View in VS Code/JetBrains
Right-click on the HTML files → "Open in Browser" or "Open with Live Preview"

## Visual Examples

### 1. Default View (All Categories)

![Default View](category-preview-default.html)

**What you'll see:**
- Filter bar with 5 category buttons: 📚 All Posts (active), 💼 Interview, ✨ Features, ⚡ Concurrency, 👋 Introduction
- Counter showing "12 / 12 posts"
- Four category group sections stacked vertically
- Each section shows:
  - Icon + title + post count
  - Description
  - 3 preview cards with post details
  - "View all X posts" link

**Colors:**
- Active button: Purple (#BD93F9) with shadow
- Inactive buttons: Dark gray (#44475A) with 60% opacity
- Cards: Medium gray (#44475A) with hover effects
- Background: Dark purple-gray (#282A36)

### 2. Filtered View (Interview Selected)

![Filtered View](category-preview-filtered.html)

**What you'll see:**
- Filter bar with "Interview" button active (purple)
- Counter showing "8 / 12 posts" (animated update)
- Other buttons are dimmed (60% opacity)
- Only the Interview category group is visible
- Other category groups are hidden
- Note at bottom: "Other category groups are hidden when filtering"

**Visual changes:**
- Active "Interview" button has purple glow and shadow
- Counter updates with fade animation
- Category group has purple border (vs gray)
- Smooth transition when filtering

### 3. Hover States

**Category Button Hover:**
```
Before:   [💼 Interview  8]  ← Gray, flat
Hover:    [💼 Interview  8]  ← Purple tint, lifted, shadow
```

**Post Card Hover:**
```
Before:   Card with gray border
Hover:    Card with purple border, shadow, lifted 2px
```

**"View all" Link Hover:**
```
Before:   Gray background, purple text
Hover:    Purple background, white text, shadow
```

## Interactive Features

### Clicking Category Buttons
1. Click any category button
2. Button becomes purple (active state)
3. Icon bounces (0.5s animation)
4. Counter updates (fade animation)
5. Posts filter instantly
6. Category groups filter to show only selected
7. Smooth transitions throughout

### Clicking "View All" Links
1. Click "View all X posts in Category →"
2. Corresponding category button activates
3. Page scrolls to posts list
4. Posts filter to that category

### Search Integration
- Search box filters by title, excerpt, or tags
- Works in conjunction with category filter
- Both filters apply simultaneously

## Responsive Behavior

### Desktop (> 768px)
- Filter buttons wrap in rows
- Category cards show in grid (up to 3 columns)
- Full-width layout with max 1200px

### Mobile (≤ 768px)
- Filter header stacks vertically
- Category buttons in single column or wrapping
- Post cards stack in single column
- Touch-friendly button sizes

## Technical Details

### Files Modified
1. `_includes/home-template1.html` - Template structure
2. `assets/css/template1-minimal-dark.css` - Styling (~250 lines added)
3. `assets/js/theme.js` - Interactive behavior (~40 lines added)

### Performance
- Pure JavaScript (no jQuery)
- CSS transforms for animations (hardware-accelerated)
- Efficient DOM queries (cached)
- No external dependencies

### Browser Support
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ✅ Mobile browsers

## Color Palette Reference

```
Primary Colors:
--bg-primary:     #282a36  ██████  Background
--bg-secondary:   #383a59  ██████  Cards background
--bg-tertiary:    #44475a  ██████  Elevated surfaces
--accent-color:   #bd93f9  ██████  Purple accent (Dracula)
--text-primary:   #f8f8f2  ██████  Main text
--text-secondary: #e0e0e0  ██████  Secondary text
--text-muted:     #6272a4  ██████  Muted text
--border-color:   #6272a4  ██████  Borders
```

## Category Icons

| Category | Icon | Color Theme | Use Case |
|----------|------|-------------|----------|
| All Posts | 📚 | Purple | Shows everything |
| Interview | 💼 | Blue | Career/prep focused |
| Features | ✨ | Yellow | New capabilities |
| Concurrency | ⚡ | Yellow | Performance topics |
| Introduction | 👋 | Yellow | Beginner content |

## Animation Details

### Bounce (Icon on Active)
```css
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
Duration: 0.5s
Timing: ease
```

### Fade In (Counter Update)
```css
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
Duration: 0.3s
Timing: ease
```

### Hover Lift (Cards)
```css
transform: translateY(-2px);
box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
Duration: 0.2s
Timing: ease
```

## Accessibility Features

- ✅ Semantic HTML (`<button>`, `<article>`, `<time>`)
- ✅ ARIA labels on interactive elements
- ✅ Keyboard navigation (Tab, Enter, Space)
- ✅ Focus indicators on all interactive elements
- ✅ Color contrast meets WCAG AA standards
- ✅ Screen reader friendly
- ✅ Works without JavaScript (static view)

## Taking Screenshots

To capture the actual rendered pages:

### macOS
```bash
# Open preview
open category-preview-default.html

# Use Command+Shift+4, then Space, then click window
# Or use Command+Shift+5 for screenshot tool
```

### Alternative: Use Browser DevTools
1. Open HTML file in browser
2. F12 to open DevTools
3. Toggle device toolbar (Ctrl+Shift+M)
4. Set resolution (1200x800 for desktop)
5. Take screenshot via DevTools menu

### Recommended Resolutions
- Desktop: 1200x800 (shows full layout)
- Tablet: 768x1024 (shows responsive breakpoint)
- Mobile: 375x667 (iPhone size)

## Comparing Before/After

### Before (Old Design)
```
┌─────────────────────────────────────────┐
│ [All] [Interview] [Features]...         │  ← Simple buttons
└─────────────────────────────────────────┘

• Posts List (flat, no grouping)
• No visual hierarchy
• No post counts
• No descriptions
• Basic filtering only
```

### After (New Design)
```
┌─────────────────────────────────────────┐
│ Filter by Category          12/12 posts │  ← Header with counter
├─────────────────────────────────────────┤
│ [📚 All Posts 12] [💼 Interview 8]...  │  ← Icons + counts
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Posts by Category                │
│   Browse posts organized by topics       │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 💼 Interview                    8 posts  │  ← Category section
│ Essential topics for Java 21 prep...    │  ← Description
├─────────────────────────────────────────┤
│ [Card 1] [Card 2] [Card 3]              │  ← Preview cards
│       View all 8 posts →                 │  ← Action link
└─────────────────────────────────────────┘

• Rich visual hierarchy
• Descriptive category cards
• Preview of posts in each category
• Better discoverability
• Modern, polished UI
```

## User Flow Example

1. **User arrives on homepage**
   - Sees "Filter by Category" with counter "12 / 12 posts"
   - Sees all 5 category buttons
   - Scrolls to see category groups

2. **User hovers over "Interview" button**
   - Button highlights with purple tint
   - Shadow appears
   - Button lifts slightly
   - Cursor changes to pointer

3. **User clicks "Interview"**
   - Button turns solid purple
   - Icon bounces once
   - Counter animates to "8 / 12 posts"
   - Other category groups fade out
   - Interview group stays visible
   - Transition is smooth

4. **User scrolls through Interview posts**
   - Sees 3 preview cards
   - Hovers over a card → lifts with purple border
   - Clicks title → goes to post

5. **User clicks "View all 8 posts"**
   - Page scrolls to main posts list
   - Shows all 8 Interview posts
   - Can still see category filter at top

6. **User clicks "All Posts"**
   - All category groups become visible again
   - Counter returns to "12 / 12 posts"
   - Smooth transition back to full view

---

**Pro Tip:** Open both preview files side-by-side to see the difference between default and filtered states!

