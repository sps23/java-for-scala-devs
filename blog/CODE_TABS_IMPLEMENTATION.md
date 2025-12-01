# Code Tabs Implementation

## Overview

The blog uses a **button-based code tabs system** that works in all browsers and on GitHub Pages without any plugins.

## How It Works

### 1. HTML Structure

```html
<div class="code-tabs" data-tabs-id="tabs-1">
  <div class="tab-buttons">
    <button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
    <button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
    <button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
  </div>
  
  <div class="tab-content active" data-tab="java">
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
      <div class="highlight"><pre class="highlight"><code>// Kotlin code here</code></pre>
      </div>
    </div>
  </div>
</div>
```

### 2. CSS (in template1-minimal-dark.css)

- `.tab-content` is `display: none` by default
- `.tab-content.active` is `display: block`
- `.tab-button.active` gets special styling (colored border and background)
- Language-specific colors:
  - Java: Blue (#58a6ff)
  - Scala: Red (#ff5555)
  - Kotlin: Purple (#bd93f9)

### 3. JavaScript (in theme.js)

The `initCodeTabs()` function:
1. Finds all `.code-tabs` containers
2. Attaches click handlers to all `.tab-button` elements
3. When clicked:
   - Removes `.active` from all buttons and content panels
   - Adds `.active` to the clicked button
   - Shows the corresponding content panel

## Why This Approach?

### ❌ Previous Attempts That Failed

1. **Radio inputs + `:has()` selector**
   - Limited browser support (only Chrome 105+, Firefox 121+)
   - Doesn't work in older Safari versions
   - Complex CSS

2. **`markdown="1"` attribute in divs**
   - Jekyll/Kramdown wraps content in extra `<p>` tags
   - Breaks CSS selector hierarchy
   - Unpredictable rendering

### ✅ Current Approach Benefits

1. **Universal browser support** - Works in all modern browsers
2. **GitHub Pages compatible** - No plugins needed
3. **Simple and maintainable** - Clear HTML structure
4. **Reliable** - JavaScript handles state management
5. **Accessible** - Uses semantic button elements
6. **Syntax highlighting preserved** - Uses Jekyll's standard code block structure

## Usage in Blog Posts

To add code tabs in a blog post:

```markdown
<div class="code-tabs" data-tabs-id="unique-id">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Your Java code
public static void main(String[] args) {
    System.out.println("Hello");
}
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Your Scala code
@main def hello(): Unit =
  println("Hello")
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Your Kotlin code
fun main() {
    println("Hello")
}
</code></pre></div></div>
</div>
</div>
```

### Important Notes:

1. **Give each tabs section a unique `data-tabs-id`**
2. **First button and content must have `class="active"`**
3. **`data-tab` values must match** between buttons and content
4. **No blank lines** inside the `<div>` tags (Jekyll requirement)
5. **Use proper HTML structure** for syntax highlighting

## Supported Languages

Currently styled:
- **Java** (blue theme)
- **Scala** (red theme)
- **Kotlin** (purple theme)

To add more languages, update the CSS in `template1-minimal-dark.css`:

```css
.code-tabs .tab-button[data-tab="python"].active {
  color: #3776ab;
  border-bottom-color: #3776ab;
  background-color: rgba(55, 118, 171, 0.1);
}
```

## Browser Compatibility

✅ **Tested and working:**
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+
- Mobile browsers (iOS Safari, Chrome Mobile)

✅ **GitHub Pages:** Fully compatible, no plugins required

## Troubleshooting

### Tabs not switching?
- Check browser console for JavaScript errors
- Verify `initCodeTabs()` is being called in `theme.js`
- Ensure `data-tab` attributes match between buttons and content

### All content showing at once?
- Check CSS is loaded: `.tab-content { display: none; }`
- Verify JavaScript is running: `initCodeTabs()` in DOMContentLoaded
- Inspect elements: only one `.tab-content` should have `.active` class

### Syntax highlighting not working?
- Use proper Jekyll code block structure: `<div class="language-X highlighter-rouge">...`
- Don't mix Markdown code fences with HTML structure
- Ensure Rouge syntax highlighter is enabled in Jekyll config

## Files Modified

1. **blog/_posts/2025-11-28-string-manipulation-with-modern-apis.md**
   - Updated to use button-based tabs

2. **blog/assets/css/template1-minimal-dark.css**
   - Replaced `:has()` selectors with simple class-based styles

3. **blog/assets/js/theme.js**
   - Added `initCodeTabs()` function

---

**Last Updated:** December 1, 2025  
**Status:** ✅ Production Ready  
**GitHub Pages Compatible:** Yes

