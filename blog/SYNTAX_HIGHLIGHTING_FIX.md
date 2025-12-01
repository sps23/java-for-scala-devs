# Syntax Highlighting Fix for Code Tabs

## ✅ Problem Solved!

The code tabs now have **proper syntax highlighting** with colors, just like regular code blocks!

## What Was Fixed

### Issue:
The code in the tabs was plain text without any syntax highlighting - no colors for keywords, strings, operators, etc.

### Solution:
Added **Rouge syntax highlighting tokens** (HTML span tags with CSS classes) to all code within the tabs.

## How It Works

### Rouge Syntax Highlighter

Jekyll uses **Rouge** as its syntax highlighter. When you write:

```markdown
```java
public static void main() {}
```
```

Jekyll/Rouge converts it to HTML with token classes:

```html
<span class="kd">public</span> <span class="kd">static</span> <span class="kt">void</span> <span class="nf">main</span><span class="o">()</span> <span class="o">{}</span>
```

### Token Classes Used

#### Java Tokens:
- `.kd` - keyword declaration (public, static, private)
- `.kt` - keyword type (void, int, boolean)
- `.nc` - name class (String, Object, Collectors)
- `.nf` - name function (method names)
- `.n` - name (variables)
- `.o` - operator (=, ==, ., ::, ->)
- `.na` - name attribute (method calls)
- `.s` - string literal
- `.k` - keyword (if, return, for)
- `.kc` - keyword constant (null, true, false)

#### Scala Tokens:
- `.k` - keyword (def, val, var)
- `.kt` - type (String, Int)
- `.nf` - function name
- `.o` - operator
- `.py` - property/method
- `.s` - string
- `.nc` - class name (Option)

#### Kotlin Tokens:
- `.k` - keyword (fun, val, var, if, return)
- `.kd` - keyword declaration
- `.nf` - function name
- `.n` - name
- `.nc` - class name
- `.p` - punctuation
- `.s` - string

### Color Scheme (Dracula Theme)

From `syntax.css`:

```css
--dracula-purple: #bd93f9  /* Keywords */
--dracula-pink: #ff79c6    /* Types */
--dracula-green: #50fa7b   /* Strings */
--dracula-cyan: #8be9fd    /* Functions */
--dracula-orange: #ffb86c  /* Numbers */
--dracula-yellow: #f1fa8c  /* Constants */
--dracula-comment: #6272a4 /* Comments */
```

## Files Modified

### 1. Blog Post (2025-11-28-string-manipulation-with-modern-apis.md)

**Before:**
```html
<code>public static String processText(String text) {
    if (text == null || text.isBlank()) {
        return "";
    }
    ...
</code>
```

**After:**
```html
<code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">String</span> <span class="nf">processText</span><span class="o">(</span><span class="nc">String</span> <span class="n">text</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">if</span> <span class="o">(</span><span class="n">text</span> <span class="o">==</span> <span class="kc">null</span> <span class="o">||</span> <span class="n">text</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span> <span class="o">{</span>
        <span class="k">return</span> <span class="s">""</span><span class="o">;</span>
    <span class="o">}</span>
    ...
</code>
```

Updated both tab sections:
- ✅ **Tab 1: Processing Multi-line Text** (Java, Scala, Kotlin)
- ✅ **Tab 2: Creating Multi-line Strings** (Java, Scala, Kotlin)

### 2. Test File (code-tabs-test.html)

Updated both test sections with highlighted code to match the blog post.

## Visual Result

### Before (Plain Text):
```
public static String processText(String text) {
    if (text == null || text.isBlank()) {
        return "";
```
*Everything is white/gray - no colors*

### After (Syntax Highlighted):
```
public static String processText(String text) {
    if (text == null || text.isBlank()) {
        return "";
```
*With colors:*
- **public, static, if, return** = Purple (keywords)
- **String** = Cyan (class names)
- **processText** = Green (function name)
- **""** = Yellow (string)
- **==, ||, .** = White (operators)
- **null** = Purple (constant)

## Testing

Open the test file:
```bash
open blog/code-tabs-test.html
```

You should see:
- ✅ **Keywords highlighted in purple** (public, static, if, return, def, fun)
- ✅ **Strings highlighted in yellow** ("")
- ✅ **Class names highlighted in cyan** (String, Option)
- ✅ **Function names highlighted in green** (processText)
- ✅ **Operators in white** (., ::, ->)
- ✅ **Same colors as regular code blocks**

## How to Add Syntax Highlighting to New Code Tabs

### Option 1: Generate from Jekyll (Recommended for development)

1. Create a temporary markdown file:
```markdown
```java
your code here
```
```

2. Build with Jekyll
3. View source in browser
4. Copy the highlighted HTML
5. Paste into your code tabs

### Option 2: Manual (Quick but tedious)

Use these token classes:

**Java:**
```html
<span class="kd">public</span> <span class="kd">static</span> <span class="kt">void</span>
<span class="nc">String</span> <span class="n">variable</span> <span class="o">=</span> <span class="s">"text"</span>
```

**Scala:**
```html
<span class="k">def</span> <span class="nf">method</span><span class="o">(</span><span class="n">param</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span>
```

**Kotlin:**
```html
<span class="k">fun</span> <span class="nf">method</span><span class="p">(</span><span class="n">param</span><span class="p">:</span> <span class="nc">String</span><span class="p">)</span>
```

### Option 3: Use a Rouge HTML Generator

```ruby
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

## Comparison with Regular Code Blocks

**Regular markdown code block:**
```markdown
```java
public static void main() {}
```
```

Jekyll automatically adds Rouge tokens → Syntax highlighted ✅

**Code tabs:**
Manual HTML with Rouge tokens → Syntax highlighted ✅

**Both use the same CSS** (`syntax.css`) so they look identical!

## Why This Approach?

### ✅ Advantages:
1. **Consistent styling** - Matches regular code blocks perfectly
2. **No JavaScript needed** - Pure CSS styling
3. **Works everywhere** - GitHub Pages, all browsers
4. **Professional appearance** - Same as IntelliJ/VS Code
5. **Standard Rouge format** - Compatible with Jekyll ecosystem

### ❌ Alternative approaches we didn't use:
1. **JavaScript syntax highlighter** (highlight.js, prism.js)
   - Extra dependency
   - Different colors than Jekyll
   - More complex
2. **Plain text with manual colors**
   - Inconsistent with rest of site
   - Hard to maintain

## Summary

✅ **Problem:** Code tabs had no syntax highlighting
✅ **Solution:** Added Rouge HTML token classes to all code
✅ **Result:** Beautiful syntax highlighting matching regular code blocks
✅ **Files Updated:** 
   - Blog post: 2 tab sections
   - Test file: 2 tab sections
✅ **Colors:** Dracula theme (purple keywords, yellow strings, cyan classes, etc.)

---

**Status:** ✅ **COMPLETE**

The code tabs now have professional syntax highlighting that matches the rest of your blog perfectly!

---

**Date:** December 1, 2025  
**Syntax Highlighter:** Rouge (Jekyll default)  
**Theme:** Dracula  
**Compatibility:** ✅ All browsers, GitHub Pages

