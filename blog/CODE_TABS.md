# Code Tabs Feature Documentation

> **See Also**: [THEME_AND_SYNTAX_HIGHLIGHTING.md](THEME_AND_SYNTAX_HIGHLIGHTING.md) for complete documentation on Dracula theme, syntax highlighting with Rouge, and overall implementation details.

## Overview

The blog uses a **button-based code tabs system** that allows readers to switch between Java, Scala, and Kotlin code examples seamlessly. This solution is:

✅ **GitHub Pages Compatible** - No plugins required  
✅ **Universal Browser Support** - Works in all modern browsers  
✅ **Simple & Maintainable** - Clean HTML, CSS, and JavaScript  
✅ **Accessible** - Uses semantic button elements  
✅ **Production Ready** - Tested and deployed

## Quick Start

To add code tabs to a blog post, use this template:

```html
<div class="code-tabs" data-tabs-id="example-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java">Java 21</button>
<button class="tab-button" data-tab="scala">Scala 3</button>
<button class="tab-button" data-tab="kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>public class Example {
    public static void main(String[] args) {
        System.out.println("Hello from Java!");
    }
}
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>object Example:
  @main def run(): Unit =
    println("Hello from Scala!")
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>fun main() {
    println("Hello from Kotlin!")
}
</code></pre></div></div>
</div>
</div>
```

### Important Guidelines

1. **Unique IDs**: Each code tabs section must have a unique `data-tabs-id`
2. **Active Class**: First button and first content must have `class="active"`
3. **Matching data-tab**: The `data-tab` attribute must match between buttons and content
4. **No Blank Lines**: Don't add blank lines inside `<div>` tags (Jekyll requirement)
5. **HTML Structure**: Use the exact HTML structure shown - don't use Markdown code fences

## How It Works

### Architecture

The code tabs feature consists of three components:

1. **HTML Structure** - Semantic markup with data attributes
2. **CSS Styling** - Visual appearance and tab switching behavior
3. **JavaScript Logic** - Event handling and state management

### Flow

1. **Page Load**: JavaScript finds all `.code-tabs` containers and attaches click handlers
2. **User Interaction**: When a tab button is clicked:
   - JavaScript removes `.active` class from all buttons and content
   - Adds `.active` to the clicked button and matching content
3. **Visual Update**: CSS shows only the active content (`display: block`)

### Visual Design

- **Active Tab Indicators**: Colored bottom border (2px)
- **Language Colors**:
  - Java: Blue (`#58a6ff`)
  - Scala: Red (`#ff5555`)
  - Kotlin: Purple (`#bd93f9`)
- **Hover Effects**: Subtle background color change
- **Smooth Transitions**: 0.2s ease animation

## Implementation Details

### HTML Structure

```html
<div class="code-tabs" data-tabs-id="unique-id">
  <!-- Tab Buttons -->
  <div class="tab-buttons">
    <button class="tab-button active" data-tab="java">Java 21</button>
    <button class="tab-button" data-tab="scala">Scala 3</button>
    <button class="tab-button" data-tab="kotlin">Kotlin</button>
  </div>
  
  <!-- Tab Content Panels -->
  <div class="tab-content active" data-tab="java">
    <!-- Jekyll's syntax highlighter structure -->
    <div class="language-java highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code>// Java code here</code></pre>
      </div>
    </div>
  </div>
  
  <div class="tab-content" data-tab="scala">
    <div class="language-scala highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code>// Scala code here</code></pre>
      </div>
    </div>
  </div>
  
  <div class="tab-content" data-tab="kotlin">
    <div class="language-kotlin highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code>// Kotlin code here</code></pre>
      </div>
    </div>
  </div>
</div>
```

### CSS Implementation

File: `blog/assets/css/template1-minimal-dark.css`

```css
/* Container */
.code-tabs {
  margin: var(--space-lg) 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
}

/* Tab Buttons Container */
.code-tabs .tab-buttons {
  display: flex;
  background-color: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
  gap: 0;
}

/* Individual Tab Button */
.code-tabs .tab-button {
  padding: var(--space-sm) var(--space-md);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  white-space: nowrap;
  user-select: none;
}

/* Hover State */
.code-tabs .tab-button:hover {
  color: var(--text-primary);
  background-color: rgba(255, 255, 255, 0.05);
}

/* Active Button - Generic */
.code-tabs .tab-button.active {
  color: var(--accent-color);
  border-bottom-color: var(--accent-color);
  background-color: rgba(88, 166, 255, 0.1);
}

/* Active Button - Java */
.code-tabs .tab-button[data-tab="java"].active {
  color: #58a6ff;
  border-bottom-color: #58a6ff;
  background-color: rgba(88, 166, 255, 0.1);
}

/* Active Button - Scala */
.code-tabs .tab-button[data-tab="scala"].active {
  color: #ff5555;
  border-bottom-color: #ff5555;
  background-color: rgba(255, 85, 85, 0.1);
}

/* Active Button - Kotlin */
.code-tabs .tab-button[data-tab="kotlin"].active {
  color: #bd93f9;
  border-bottom-color: #bd93f9;
  background-color: rgba(189, 147, 249, 0.1);
}

/* Tab Content - Hidden by Default */
.code-tabs .tab-content {
  display: none;
}

/* Tab Content - Active */
.code-tabs .tab-content.active {
  display: block;
}

/* Code Block Inside Tabs */
.code-tabs .tab-content pre {
  margin: 0;
  border-radius: 0;
  border: none;
}
```

### JavaScript Implementation

File: `blog/assets/js/theme.js`

```javascript
/**
 * Initialize code tabs functionality
 * Handles tab switching for code examples
 */
function initCodeTabs() {
  const codeTabsContainers = document.querySelectorAll('.code-tabs');
  if (!codeTabsContainers.length) return;

  codeTabsContainers.forEach(function(container) {
    const buttons = container.querySelectorAll('.tab-button');
    const contents = container.querySelectorAll('.tab-content');

    buttons.forEach(function(button) {
      button.addEventListener('click', function() {
        const targetTab = button.getAttribute('data-tab');

        // Remove active class from all buttons and contents in this container
        buttons.forEach(btn => btn.classList.remove('active'));
        contents.forEach(content => content.classList.remove('active'));

        // Add active class to clicked button
        button.classList.add('active');

        // Show corresponding content
        const targetContent = container.querySelector('.tab-content[data-tab="' + targetTab + '"]');
        if (targetContent) {
          targetContent.classList.add('active');
        }
      });
    });
  });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
  initCodeTabs();
  // ... other initializations
});
```

## Adding New Languages

To support additional languages (e.g., Python, Rust, Go):

### 1. Add Button

```html
<button class="tab-button" data-tab="python">Python</button>
```

### 2. Add Content

```html
<div class="tab-content" data-tab="python">
<div class="language-python highlighter-rouge"><div class="highlight"><pre class="highlight"><code>def main():
    print("Hello from Python!")
</code></pre></div></div>
</div>
```

### 3. Add CSS Styling (Optional)

Add language-specific color in `template1-minimal-dark.css`:

```css
.code-tabs .tab-button[data-tab="python"].active {
  color: #3776ab;
  border-bottom-color: #3776ab;
  background-color: rgba(55, 118, 171, 0.1);
}
```

## Examples in Production

### Current Usage

The code tabs feature is actively used in these blog posts:

- `2025-11-28-string-manipulation-with-modern-apis.md`
- `2025-12-01-comparing-jvm-test-frameworks.md`

### Test Page

A standalone test page is available at:
```
blog/code-tabs-test.html
```

Open it in your browser to see the feature in action without running Jekyll.

## Browser Compatibility

✅ **Supported Browsers:**

| Browser | Version |
|---------|---------|
| Chrome | 90+ |
| Firefox | 88+ |
| Safari | 14+ |
| Edge | 90+ |
| iOS Safari | 14+ |
| Chrome Mobile | 90+ |

✅ **GitHub Pages:** Fully compatible, no plugins or special build steps required

## Why This Approach?

### ❌ Previous Attempts That Failed

1. **Radio Inputs + CSS `:has()` Selector**
   - Limited browser support (Chrome 105+, Firefox 121+)
   - Doesn't work in Safari < 15.4
   - Complex CSS that was hard to debug
   - Example: `input[type="radio"]:checked:has(+ .tab-content)`

2. **Markdown Inside Divs (`markdown="1"` attribute)**
   - Jekyll/Kramdown wraps content in extra `<p>` tags
   - Breaks CSS selector hierarchy
   - Unpredictable rendering
   - Syntax highlighting gets lost

3. **Jekyll-Tabs Plugin**
   - Requires plugin installation
   - May not work on GitHub Pages (restricted plugins)
   - Adds dependencies and complexity
   - Less control over styling

### ✅ Current Approach Benefits

1. **Universal Browser Support** - Works in all modern browsers
2. **GitHub Pages Compatible** - No plugins needed
3. **Simple and Maintainable** - Clear HTML structure, straightforward JavaScript
4. **Reliable** - JavaScript handles state management explicitly
5. **Accessible** - Uses semantic `<button>` elements
6. **Syntax Highlighting Preserved** - Uses Jekyll's standard code block structure
7. **Mobile Friendly** - Touch-optimized buttons with proper sizing
8. **Performant** - Minimal JavaScript, CSS-based transitions

## Troubleshooting

### Problem: Tabs not switching when clicked

**Symptoms:** Clicking tab buttons does nothing, no errors in console

**Solutions:**
1. Verify JavaScript is loaded: Check browser console for errors
2. Ensure `initCodeTabs()` is called in the DOMContentLoaded event
3. Check `data-tab` attributes match between buttons and content
4. Verify the buttons have the `tab-button` class

### Problem: All code blocks showing at once

**Symptoms:** All three code examples visible simultaneously

**Solutions:**
1. Check CSS is loaded: `.tab-content { display: none; }`
2. Verify JavaScript is running: Open browser console and type `initCodeTabs()`
3. Inspect elements: Only one `.tab-content` should have `.active` class
4. Clear browser cache and reload

### Problem: Syntax highlighting not working

**Symptoms:** Code appears as plain text without colors

**Solutions:**
1. Use proper Jekyll code block structure (see HTML Structure section)
2. Don't mix Markdown code fences (` ``` `) with HTML structure
3. Ensure Rouge syntax highlighter is enabled in `_config.yml`
4. Check that language class is correct: `language-java`, `language-scala`, etc.

### Problem: Tabs look unstyled

**Symptoms:** Buttons appear as plain text, no borders or colors

**Solutions:**
1. Verify CSS file is loaded: Check browser Network tab
2. Check CSS selector specificity isn't being overridden
3. Ensure the container has the `code-tabs` class
4. Look for CSS syntax errors in `template1-minimal-dark.css`

### Problem: Mobile display issues

**Symptoms:** Tabs overflow or don't fit on mobile screens

**Solutions:**
1. Verify `overflow-x: auto` is set on `.tab-buttons`
2. Check button `white-space: nowrap` is set
3. Test with browser DevTools mobile emulation
4. Consider shorter tab labels for mobile

## Best Practices

### 1. Keep Code Examples Concise

Each tab should show focused, relevant code:
- ✅ 10-30 lines per example
- ❌ Entire file dumps (100+ lines)

### 2. Consistent Formatting

Use consistent indentation and style across all languages:
```java
// Good - 4 spaces, clear structure
public class Example {
    public void method() {
        // code
    }
}
```

### 3. Add Comments

Help readers understand what's different:
```scala
// Scala uses 'val' for immutable variables
val message = "Hello"
```

### 4. Test All Tabs

Before publishing, click through every tab to ensure:
- Code renders correctly
- Syntax highlighting works
- No layout breaking

### 5. Accessibility

- Use descriptive button text: "Java 21" not just "Java"
- Don't rely solely on color to indicate active tab
- Consider adding ARIA labels for screen readers

## Files Reference

### Modified Files

1. **`blog/_posts/2025-11-28-string-manipulation-with-modern-apis.md`**
   - First blog post using button-based tabs

2. **`blog/_posts/2025-12-01-comparing-jvm-test-frameworks.md`**
   - Extensive use of code tabs for test framework comparisons

3. **`blog/assets/css/template1-minimal-dark.css`**
   - All code tabs styling (lines 525-620)

4. **`blog/assets/js/theme.js`**
   - `initCodeTabs()` function (lines 225-250)

### Documentation Files

1. **`blog/CODE_TABS.md`** (this file)
   - Comprehensive documentation

2. **`blog/code-tabs-test.html`**
   - Standalone test page

### Removed/Deprecated Files

1. ~~`blog/CODE_TABS_FIX_SUMMARY.md`~~ (merged into this document)
2. ~~`blog/CODE_TABS_IMPLEMENTATION.md`~~ (merged into this document)

## Migration Guide

### From Old Radio Button Implementation

If you have old code tabs using radio buttons, convert them:

**Old Format:**
```html
<input type="radio" name="tabs-1" id="tab-java-1" checked>
<div class="tab-labels">
<label for="tab-java-1">Java 21</label>
```

**New Format:**
```html
<div class="tab-buttons">
<button class="tab-button active" data-tab="java">Java 21</button>
```

**Steps:**
1. Replace `<input>` and `<label>` with `<button>` elements
2. Change `<div class="tab-labels">` to `<div class="tab-buttons">`
3. Add `data-tab` attributes instead of `for` attributes
4. Replace `checked` with `class="active"`
5. Remove `markdown="1"` from content divs
6. Use proper HTML code block structure (not Markdown fences)

### From Markdown Code Fences

**Old Format:**
````markdown
```java
public class Example {
    // code
}
```
````

**New Format:**
```html
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>public class Example {
    // code
}
</code></pre></div></div>
```

## Version History

| Date | Version | Changes |
|------|---------|---------|
| Dec 1, 2025 | 2.0 | Button-based implementation, production ready |
| Nov 28, 2025 | 1.5 | Added to string manipulation post |
| Nov 25, 2025 | 1.0 | Initial radio button implementation (deprecated) |

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review examples in existing blog posts
3. Test with `blog/code-tabs-test.html`
4. Open an issue in the repository

---

**Status:** ✅ Production Ready  
**Last Updated:** December 11, 2025  
**GitHub Pages Compatible:** Yes  
**Browser Support:** All modern browsers  
**Plugin Required:** None

