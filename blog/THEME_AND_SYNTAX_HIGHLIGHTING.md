# Dracula Theme & Syntax Highlighting Implementation

This document describes the complete implementation of the **Dracula theme** for code syntax highlighting and the custom code tabs feature in the Java for Scala Devs blog.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Color Palette](#color-palette)
3. [Implementation Details](#implementation-details)
4. [Code Tabs Feature](#code-tabs-feature)
5. [Rouge Token Reference](#rouge-token-reference)
6. [How to Use](#how-to-use)
7. [Testing](#testing)

> **Note**: For detailed code tabs usage instructions, see [CODE_TABS.md](CODE_TABS.md)

---

## Overview

The blog uses the **Dracula theme** for syntax highlighting, matching the popular color scheme used in IntelliJ IDEA, VS Code, and GitHub. This provides:

- **Consistent visual experience** across your development tools and blog
- **High contrast colors** optimized for dark backgrounds
- **Professional appearance** that developers recognize and appreciate
- **Excellent accessibility** with proper color contrast ratios

### Technologies Used

- **Jekyll**: Static site generator
- **Rouge**: Syntax highlighter (Jekyll's default)
- **Dracula Theme**: Color palette for code highlighting
- **Custom CSS**: For code tabs and enhanced styling

---

## Color Palette

The Dracula theme uses the following color scheme:

| Element | Color Name | Hex Code | Usage |
|---------|-----------|----------|-------|
| Background | Dark Purple-Gray | `#282a36` | Code block backgrounds |
| Current Line | Darker Gray | `#44475a` | Inline code, hover states |
| Foreground | White | `#f8f8f2` | Default text color |
| Comment | Blue-Gray | `#6272a4` | Comments (italic) |
| Cyan | Bright Cyan | `#8be9fd` | Class names, types |
| Green | Bright Green | `#50fa7b` | Functions, strings (alternate) |
| Orange | Bright Orange | `#ffb86c` | Numbers |
| Pink | Bright Pink | `#ff79c6` | Keywords, operators |
| Purple | Bright Purple | `#bd93f9` | Constants, numbers |
| Red | Bright Red | `#ff5555` | Errors, deletions |
| Yellow | Bright Yellow | `#f1fa8c` | Strings |

### CSS Variables

Defined in `/blog/assets/css/syntax.css`:

```css
:root {
  --dracula-bg: #282a36;
  --dracula-current-line: #44475a;
  --dracula-foreground: #f8f8f2;
  --dracula-comment: #6272a4;
  --dracula-cyan: #8be9fd;
  --dracula-green: #50fa7b;
  --dracula-orange: #ffb86c;
  --dracula-pink: #ff79c6;
  --dracula-purple: #bd93f9;
  --dracula-red: #ff5555;
  --dracula-yellow: #f1fa8c;
}
```

---

## Implementation Details

### File Structure

```
blog/assets/css/
├── syntax.css                    # Rouge syntax highlighting with Dracula theme
└── template1-minimal-dark.css    # Main theme (includes code tabs)
```

### 1. Syntax Highlighting (`syntax.css`)

#### Features:

✅ **Complete Rouge token support** - All Rouge syntax classes mapped to Dracula colors  
✅ **Inline code styling** - Distinct appearance for inline `code` elements  
✅ **Custom scrollbars** - Styled scrollbars for code blocks (WebKit browsers)  
✅ **Language labels** - Optional labels showing the programming language  
✅ **Mobile responsive** - Optimized for smaller screens  
✅ **Table support** - Code blocks in tables properly styled  

#### Key CSS Classes:

```css
/* Keywords (public, static, if, def, fun) */
.highlight .k, .highlight .kd, .highlight .kt { color: var(--dracula-pink); }

/* Strings */
.highlight .s, .highlight .s1, .highlight .s2 { color: var(--dracula-yellow); }

/* Function names */
.highlight .nf { color: var(--dracula-green); }

/* Class names */
.highlight .nc { color: var(--dracula-cyan); }

/* Numbers */
.highlight .m, .highlight .mi { color: var(--dracula-purple); }

/* Comments */
.highlight .c, .highlight .c1, .highlight .cm { color: var(--dracula-comment); font-style: italic; }

/* Operators */
.highlight .o { color: var(--dracula-pink); }

/* Inline code (not in code blocks) */
code:not([class]) {
  background-color: var(--dracula-current-line);
  color: var(--dracula-pink);
  padding: 0.2em 0.4em;
  border-radius: 4px;
}
```

#### Typography:

- **Font stack**: `'Fira Code', 'JetBrains Mono', 'Consolas', 'Monaco', monospace`
- **Font size**: `0.9rem` for code blocks, `0.85rem` on mobile
- **Line height**: `1.5` for better readability

### 2. Main Theme (`template1-minimal-dark.css`)

#### Code-Related Styles:

```css
/* Inline code */
code {
  background-color: #44475a; /* Dracula current line */
  padding: 0.2em 0.4em;
  border-radius: 4px;
  color: #ff79c6; /* Dracula pink */
}

/* Code blocks */
pre {
  background-color: var(--code-bg); /* #282a36 */
  padding: var(--space-md);
  border-radius: 8px;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

pre code {
  background: none;
  padding: 0;
  color: #f8f8f2; /* Dracula foreground */
}
```

---

## Code Tabs Feature

### Overview

Interactive code tabs allow side-by-side comparison of Java, Scala, and Kotlin code without overwhelming the reader.

### Implementation

#### HTML Structure:

```html
<div class="code-tabs" data-tabs-id="tabs-1">
  <div class="tab-buttons">
    <button class="tab-button active" data-tab="java">Java 21</button>
    <button class="tab-button" data-tab="scala">Scala 3</button>
    <button class="tab-button" data-tab="kotlin">Kotlin</button>
  </div>
  
  <div class="tab-content active" data-tab="java">
    <div class="language-java highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code><!-- Java code with Rouge tokens --></code></pre>
      </div>
    </div>
  </div>
  
  <div class="tab-content" data-tab="scala">
    <!-- Scala code -->
  </div>
  
  <div class="tab-content" data-tab="kotlin">
    <!-- Kotlin code -->
  </div>
</div>
```

#### JavaScript Behavior:

Located in `/blog/assets/js/theme.js`:

```javascript
// Code tabs functionality
function initializeCodeTabs() {
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
```

#### CSS Styling:

```css
.code-tabs {
  margin: var(--space-lg) 0;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
}

.code-tabs .tab-button {
  padding: var(--space-sm) var(--space-md);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.code-tabs .tab-button.active {
  color: var(--accent-color);
  border-bottom-color: var(--accent-color);
  background-color: rgba(88, 166, 255, 0.1);
}

/* Language-specific active colors */
.code-tabs .tab-button[data-tab="java"].active {
  color: #58a6ff;
  border-bottom-color: #58a6ff;
}

.code-tabs .tab-button[data-tab="scala"].active {
  color: #ff5555;
  border-bottom-color: #ff5555;
}

.code-tabs .tab-button[data-tab="kotlin"].active {
  color: #bd93f9;
  border-bottom-color: #bd93f9;
}
```

### Posts Using Code Tabs

The following blog posts use the code tabs feature:

- `2025-11-29-string-templates-preview.md` (4 tabs)
- `2025-11-29-collection-factory-methods-and-stream-basics.md` (7 tabs)
- `2025-11-28-sealed-classes-and-exhaustive-pattern-matching.md` (3 tabs)
- `2025-11-29-null-safe-programming-with-optional.md` (6+ tabs)

---

## Rouge Token Reference

Rouge (Jekyll's syntax highlighter) generates HTML with CSS classes for each token type. Here's a complete reference:

### Java Tokens

| Token Class | Description | Dracula Color | Example |
|------------|-------------|---------------|---------|
| `.kd` | Keyword declaration | Pink | `public`, `static`, `private` |
| `.kt` | Keyword type | Cyan (italic) | `void`, `int`, `boolean` |
| `.nc` | Name class | Cyan | `String`, `Object`, `List` |
| `.nf` | Name function | Green | Method names |
| `.n` | Name | Foreground | Variable names |
| `.o` | Operator | Pink | `=`, `==`, `.`, `::`, `->` |
| `.na` | Name attribute | Green | Method calls |
| `.s`, `.s1`, `.s2` | String | Yellow | `"text"` |
| `.k` | Keyword | Pink | `if`, `return`, `for` |
| `.kc` | Keyword constant | Pink | `null`, `true`, `false` |
| `.m`, `.mi` | Number | Purple | `123`, `1.5` |
| `.c`, `.c1`, `.cm` | Comment | Blue-gray (italic) | `// comment` |

### Scala Tokens

| Token Class | Description | Dracula Color | Example |
|------------|-------------|---------------|---------|
| `.k` | Keyword | Pink | `def`, `val`, `var` |
| `.kt` | Type | Cyan | `String`, `Int` |
| `.nf` | Function name | Green | Method names |
| `.o` | Operator | Pink | `=>`, `->` |
| `.py` | Property | Cyan | Method calls |
| `.nc` | Class name | Cyan | `Option`, `List` |

### Kotlin Tokens

| Token Class | Description | Dracula Color | Example |
|------------|-------------|---------------|---------|
| `.k` | Keyword | Pink | `fun`, `val`, `var` |
| `.kd` | Keyword declaration | Pink | `class`, `data` |
| `.nf` | Function name | Green | Method names |
| `.nc` | Class name | Cyan | Type names |
| `.p` | Punctuation | Foreground | `(`, `)`, `,` |

### Common Tokens (All Languages)

| Token Class | Description | Dracula Color |
|------------|-------------|---------------|
| `.err` | Error | Red |
| `.gd` | Deleted (diff) | Red with background |
| `.gi` | Inserted (diff) | Green with background |
| `.gh` | Heading | Cyan (bold) |
| `.lineno` | Line numbers | Blue-gray |
| `.nd` | Decorator/Annotation | Green |

---

## How to Use

### 1. Regular Code Blocks (Markdown)

Simply use standard markdown code fences with language tags:

````markdown
```java
public static void main(String[] args) {
    System.out.println("Hello, World!");
}
```
````

Jekyll + Rouge will automatically apply syntax highlighting with Dracula colors.

### 2. Creating Code Tabs

#### Option A: Manual HTML with Rouge Tokens (Current Method)

1. Write your code in separate language sections
2. Generate Rouge tokens for each language
3. Wrap in the code tabs HTML structure

**Example:**

```html
<div class="code-tabs" data-tabs-id="tabs-1">
  <div class="tab-buttons">
    <button class="tab-button active" data-tab="java">Java 21</button>
    <button class="tab-button" data-tab="scala">Scala 3</button>
  </div>
  
  <div class="tab-content active" data-tab="java">
    <div class="language-java highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="kt">void</span> <span class="nf">main</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Hello"</span><span class="o">);</span>
<span class="o">}</span></code></pre>
      </div>
    </div>
  </div>
  
  <div class="tab-content" data-tab="scala">
    <div class="language-scala highlighter-rouge">
      <div class="highlight">
        <pre class="highlight"><code><span class="k">def</span> <span class="nf">main</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span>
  <span class="nf">println</span><span class="o">(</span><span class="s">"Hello"</span><span class="o">)</span></code></pre>
      </div>
    </div>
  </div>
</div>
```

#### Option B: Generate Rouge Tokens (Development Helper)

Create a Ruby script to generate highlighted HTML:

```ruby
# generate_highlight.rb
require 'rouge'

code = <<~JAVA
  public static void main() {
      System.out.println("Hello");
  }
JAVA

formatter = Rouge::Formatters::HTML.new
lexer = Rouge::Lexers::Java.new
puts formatter.format(lexer.lex(code))
```

Run: `ruby generate_highlight.rb`

Copy the output and paste into your tab content.

#### Option C: Use Jekyll Build (Recommended for Development)

1. Create a temporary markdown file with your code:
````markdown
```java
public static void main() {}
```
````

2. Build the site: `bundle exec jekyll build`
3. Open the generated HTML in `_site/`
4. View source and copy the highlighted HTML
5. Paste into your code tabs

### 3. Inline Code

Use backticks for inline code:

```markdown
Use the `Optional.of()` method to create an Optional.
```

Result: Dracula pink text on dark gray background

---

## Testing

### Test Files

Two test files are available:

1. **`/blog/code-tabs-test.html`** - Tests code tabs with syntax highlighting
2. **`/blog/dracula-preview.html`** - Preview of Dracula theme with various examples

### Testing Methods

#### Option 1: Open Test Files Directly

```bash
open /Users/sylwesterstocki/Workspace/java-for-scala-devs/blog/code-tabs-test.html
open /Users/sylwesterstocki/Workspace/java-for-scala-devs/blog/dracula-preview.html
```

#### Option 2: Use Local Web Server

```bash
cd /Users/sylwesterstocki/Workspace/java-for-scala-devs/blog
python3 -m http.server 8000
```

Then visit:
- http://localhost:8000/code-tabs-test.html
- http://localhost:8000/dracula-preview.html

#### Option 3: Build with Jekyll

If Jekyll dependencies are working:

```bash
cd /Users/sylwesterstocki/Workspace/java-for-scala-devs/blog
bundle exec jekyll serve
```

Visit: http://localhost:4000

### What to Check

✅ **Keywords highlighted in pink** (`public`, `static`, `if`, `def`, `fun`)  
✅ **Strings highlighted in yellow** (`"text"`)  
✅ **Class names highlighted in cyan** (`String`, `List`, `Option`)  
✅ **Function names highlighted in green** (method names)  
✅ **Numbers highlighted in purple** (`123`, `1.5`)  
✅ **Comments highlighted in blue-gray** and italic  
✅ **Operators in pink** (`.`, `::`, `->`, `=>`)  
✅ **Inline code has pink text on dark background**  
✅ **Code tabs switch correctly**  
✅ **Tab buttons change color when active**  

---

## Comparison: Before vs After

### Before Implementation

- **Theme**: Generic GitHub Dark
- **Background**: `#161b22` (GitHub dark blue)
- **Keywords**: `#ff7b72` (Red)
- **Strings**: `#a5d6ff` (Light blue)
- **Functions**: `#d2a8ff` (Purple)
- **Classes**: `#ffa657` (Orange)
- **Code tabs**: No syntax highlighting

### After Implementation

- **Theme**: Dracula
- **Background**: `#282a36` (Dark purple-gray)
- **Keywords**: `#ff79c6` (Pink) ✨
- **Strings**: `#f1fa8c` (Yellow) ✨
- **Functions**: `#50fa7b` (Green) ✨
- **Classes**: `#8be9fd` (Cyan) ✨
- **Code tabs**: Full syntax highlighting with Rouge tokens ✨

---

## Benefits

### 1. **Visual Consistency**
- Matches IntelliJ IDEA, VS Code, and GitHub Dracula theme
- Readers see familiar colors from their IDEs

### 2. **Enhanced Readability**
- High contrast colors optimized for dark backgrounds
- Each token type has a distinct, meaningful color
- Italic comments make them visually distinct

### 3. **Professional Appearance**
- Widely recognized and loved by developers
- Modern, polished look
- Attention to detail (scrollbars, hover states, etc.)

### 4. **Accessibility**
- Excellent color contrast ratios
- Readable on various screen types
- Works well in different lighting conditions

### 5. **Interactive Learning**
- Code tabs enable easy comparison
- Reduces cognitive load by showing one language at a time
- Encourages exploration

---

## Maintenance & Updates

### Adding Support for New Languages

1. Add language-specific Rouge token mappings to `syntax.css`:

```css
/* Your-Language-specific enhancements */
.highlight .your-token { 
  color: var(--dracula-color);
}
```

2. Add tab button style in `template1-minimal-dark.css`:

```css
.code-tabs .tab-button[data-tab="yourlang"].active {
  color: #yourcolor;
  border-bottom-color: #yourcolor;
  background-color: rgba(your, rgb, values, 0.1);
}
```

### Modifying Colors

All colors are centralized in CSS variables:

```css
/* In syntax.css */
:root {
  --dracula-pink: #ff79c6;  /* Change this to update all pink elements */
}
```

### Mobile Optimization

Current breakpoint: `768px`

To adjust mobile styles, modify the media query in `syntax.css`:

```css
@media (max-width: 768px) {
  .highlight pre {
    padding: 1rem;
    font-size: 0.85rem;
  }
}
```

---

## Troubleshooting

### Issue: Syntax highlighting not working

**Solution**: Ensure Rouge is installed and Jekyll is using it:

```yaml
# _config.yml
highlighter: rouge
markdown: kramdown
```

### Issue: Code tabs not switching

**Solution**: Check that `theme.js` is loaded:

```html
<!-- In your layout file -->
<script src="{{ '/assets/js/theme.js' | relative_url }}"></script>
```

The code tabs functionality is part of the main theme.js file.

### Issue: Colors look different in browser

**Solution**: Clear browser cache or use hard refresh (Cmd+Shift+R on Mac)

### Issue: Inline code doesn't have Dracula colors

**Solution**: Check CSS specificity. Inline code styling should come after other code styles:

```css
/* This should be near the end of syntax.css */
code:not([class]) {
  background-color: var(--dracula-current-line);
  color: var(--dracula-pink);
}
```

---

## Future Enhancements

### Potential Improvements:

- [ ] **Line numbers**: Add optional line numbers to code blocks
- [ ] **Copy button**: Add "Copy to clipboard" button for code blocks
- [ ] **Diff view**: Support for showing code changes with +/- indicators
- [ ] **Collapsible sections**: Long code blocks that can be collapsed
- [ ] **Language icons**: Add language logos to tab buttons
- [ ] **Keyboard navigation**: Arrow keys to switch between tabs
- [ ] **Permalink to tabs**: URL hash to link to specific tab (#tabs-1-java)
- [ ] **Syntax highlighting generator**: Automated tool to generate Rouge HTML

---

## Credits

- **Dracula Theme**: https://draculatheme.com
- **Rouge**: https://github.com/rouge-ruby/rouge
- **Jekyll**: https://jekyllrb.com
- **Fira Code Font**: https://github.com/tonsky/FiraCode
- **JetBrains Mono Font**: https://www.jetbrains.com/lp/mono/

---

## Summary

✅ **Dracula theme** fully implemented with consistent colors across all code  
✅ **Rouge syntax highlighting** with comprehensive token support  
✅ **Interactive code tabs** for Java, Scala, and Kotlin comparison  
✅ **Mobile responsive** with optimized layouts for small screens  
✅ **Professional appearance** matching popular IDEs  
✅ **Excellent accessibility** with high contrast colors  

**Status**: ✅ **PRODUCTION READY**

**Last Updated**: December 11, 2025  
**Version**: 1.0

