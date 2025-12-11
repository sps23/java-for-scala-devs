# Blog Module

This module contains the Jekyll-based GitHub Pages blog for the Java for Scala Developers project.

## 📚 Documentation

- **[Theme & Syntax Highlighting](THEME_AND_SYNTAX_HIGHLIGHTING.md)** - Complete guide to the Dracula theme implementation, syntax highlighting with Rouge, and interactive code tabs feature

## Ruby Installation on macOS M4 (Apple Silicon)

If you're experiencing OpenSSL issues or need to set up Ruby for this blog, follow these steps to install a modern Ruby version on macOS M4:

### Prerequisites

Ensure you have Xcode command line tools installed:

```bash
xcode-select --install
```

### Step 1: Install Homebrew (if not already installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

After installation, add Homebrew to your PATH (M-series Macs):

```bash
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
source ~/.zshrc
```

### Step 2: Install rbenv and Dependencies

`rbenv` is a Ruby version manager that allows you to safely manage multiple Ruby versions without conflicts.

```bash
brew install rbenv ruby-build openssl@3
```

### Step 3: Initialize rbenv

```bash
rbenv init
```

Add the following line to your shell configuration file (`~/.zshrc` for zsh):

```bash
eval "$(rbenv init - zsh)"
```

Reload your shell:

```bash
exec zsh
```

### Step 4: Install Ruby with OpenSSL 3 Support

Install Ruby 3.3.0 (or later) with explicit OpenSSL 3 configuration:

```bash
LDFLAGS="-L$(brew --prefix openssl@3)/lib" \
CPPFLAGS="-I$(brew --prefix openssl@3)/include" \
RUBY_CONFIGURE_OPTS="--with-openssl=$(brew --prefix openssl@3)" \
rbenv install 3.3.0
```

This command may take several minutes as it compiles Ruby from source.

### Step 5: Set Your Preferred Ruby Version

```bash
rbenv global 3.3.0
rbenv rehash
```

### Step 6: Verify Installation

```bash
ruby --version
gem list | grep bundler
bundle --version
```

### Why This Works

- **rbenv** - Cleanly manages multiple Ruby versions without conflicting with system Ruby
- **OpenSSL 3.x** - Modern SSL library with proper Apple Silicon (M4) support
- **RUBY_CONFIGURE_OPTS** - Explicitly links Ruby against OpenSSL 3 during compilation
- **M4 Optimization** - Homebrew packages are compiled for ARM64 architecture on Apple Silicon
- **Avoids OpenSSL errors** - Eliminates common errors like "OPENSSL_1_1_0g not found" or version mismatches

### Running the Blog Locally

Once Ruby is properly installed:

```bash
cd blog
bundle install
bundle exec jekyll serve
```

Then visit `http://localhost:4000/java-for-scala-devs/` in your browser.

### Troubleshooting

**Issue: "Could not locate Gemfile" error**

Make sure you're in the `blog` directory when running `bundle install`:

```bash
cd /Users/sylwesterstocki/Workspace/java-for-scala-devs/blog
bundle install
```

**Issue: RubyGems version warning**

If you see: "Your RubyGems version (3.0.3.1) has a bug that prevents `required_ruby_version` from working..."

Update RubyGems:

```bash
gem update --system
```

This will install the latest compatible version for your Ruby release.

---

## Theme Templates

This blog includes **three modern dark theme templates** that you can choose from. Each template features:

- ✅ Modern dark color scheme
- ✅ Navigation menu for latest blog posts
- ✅ Category/subject filtering
- ✅ Search functionality
- ✅ Responsive mobile design
- ✅ Timeline view for posts

### Template Options

| # | Template Name | Description |
|---|---------------|-------------|
| 1 | **Modern Minimal Dark** | Clean, minimalist dark theme with a timeline view and category filters. Perfect for readability and simplicity. |
| 2 | **Tech Blog Pro** | Professional layout with sidebar navigation, category panels, and card-based post display. Great for blogs with many categories. |
| 3 | **Developer Journal** | Timeline-centric design with animated post entries and date-based navigation. Ideal for chronological blog exploration. |

### Selecting a Template

To change the template, edit the `_config.yml` file and set the `theme_template` value:

```yaml
# Theme Template Selection
# Choose from:
#   1 = Modern Minimal Dark (clean, minimalist dark theme)
#   2 = Tech Blog Pro (professional with sidebar navigation)
#   3 = Developer Journal (timeline-centric with search)
theme_template: 1
```

After changing the template:
1. If running locally, restart the Jekyll server
2. For GitHub Pages, commit and push the change - the site will rebuild automatically

## Features

### Dark Color Scheme
All templates use a carefully designed dark color palette that's easy on the eyes and modern looking. The colors are defined as CSS custom properties (variables) in each template's CSS file.

### Navigation Menu
Each template includes:
- Header navigation with links to Home, About, and GitHub
- Quick access to recent posts
- Mobile-responsive hamburger menu

### Category Filtering
Click on category buttons/badges to filter posts by subject. Categories are automatically extracted from post front matter.

### Search
Use the search box to filter posts by:
- Title
- Content excerpt
- Tags

### Timeline View
Templates 1 and 3 feature a visual timeline for browsing posts chronologically.

## Structure

```
blog/
├── _config.yml          # Jekyll configuration (template selection here)
├── _layouts/
│   ├── default.html     # Base layout with template CSS loading
│   ├── home.html        # Home page layout
│   ├── post.html        # Individual post layout
│   └── page.html        # Static page layout (About, etc.)
├── _includes/
│   ├── home-template1.html   # Template 1 home content
│   ├── home-template2.html   # Template 2 home content
│   ├── home-template3.html   # Template 3 home content
│   ├── post-template1.html   # Template 1 post content
│   ├── post-template2.html   # Template 2 post content
│   └── post-template3.html   # Template 3 post content
├── _posts/              # Blog posts in Markdown format
├── assets/
│   ├── css/
│   │   ├── template1-minimal-dark.css   # Template 1 styles
│   │   ├── template2-tech-pro.css       # Template 2 styles
│   │   ├── template3-dev-journal.css    # Template 3 styles
│   │   └── syntax.css                   # Code syntax highlighting
│   └── js/
│       └── theme.js     # Search, filtering, and interactivity
├── _posts/              # Blog posts in Markdown format
├── assets/
│   ├── css/
│   │   ├── template1-minimal-dark.css   # Template 1 styles
│   │   ├── template2-tech-pro.css       # Template 2 styles
│   │   ├── template3-dev-journal.css    # Template 3 styles
│   │   └── syntax.css                   # Code syntax highlighting
│   └── js/
│       └── theme.js     # Search, filtering, and interactivity
├── about.md             # About page
├── index.md             # Home page
├── Gemfile              # Ruby dependencies
└── README.md            # This file
```

## Customizing Styles

### Color Scheme
Each template defines CSS custom properties (variables) at the top of its CSS file. To customize colors:

1. Open the relevant template CSS file in `assets/css/`
2. Modify the variables in the `:root` selector:

```css
:root {
  --bg-primary: #0d1117;      /* Main background */
  --bg-secondary: #161b22;    /* Card/section background */
  --text-primary: #e6edf3;    /* Main text color */
  --text-secondary: #8b949e;  /* Secondary text */
  --accent-color: #58a6ff;    /* Links and highlights */
  /* ... more variables */
}
```

### Typography
Font families are also defined as CSS variables:
```css
:root {
  --font-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-mono: 'JetBrains Mono', Consolas, monospace;
}
```

### Adding Custom CSS
Create a file `assets/css/custom.css` and add a link to it in `_layouts/default.html`:
```html
<link rel="stylesheet" href="{{ '/assets/css/custom.css' | relative_url }}">
```

## Local Development

To run the blog locally:

```bash
cd blog
bundle install
bundle exec jekyll serve
```

Then visit `http://localhost:4000/java-for-scala-devs` in your browser.

### Testing Different Templates

1. Change `theme_template` in `_config.yml`
2. Restart the Jekyll server (Ctrl+C then `bundle exec jekyll serve`)
3. Refresh your browser

## Writing Posts

Create new posts in the `_posts/` directory with the naming convention:
```
YYYY-MM-DD-post-title.md
```

Example front matter:
```yaml
---
layout: post
title: "Your Post Title"
date: 2024-01-15 10:00:00 +0000
categories: features
tags: java scala pattern-matching
---
```

### Code Tabs Feature

The blog supports interactive code tabs that allow readers to switch between Java, Scala, and Kotlin examples. This is perfect for comparing implementations across JVM languages.

**Quick Example:**

```html
<div class="code-tabs" data-tabs-id="example-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java">Java 21</button>
<button class="tab-button" data-tab="scala">Scala 3</button>
<button class="tab-button" data-tab="kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Java code here
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Scala code here
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>// Kotlin code here
</code></pre></div></div>
</div>
</div>
```

**For complete documentation, see:** [CODE_TABS.md](CODE_TABS.md)

Features:
- ✅ Button-based tabs with JavaScript switching
- ✅ Language-specific colors (Java=blue, Scala=red, Kotlin=purple)
- ✅ GitHub Pages compatible (no plugins)
- ✅ Works in all modern browsers
- ✅ Preserves syntax highlighting

### Categories
Use categories to group related posts. Common categories:
- `introduction` - Getting started posts
- `features` - Language feature comparisons
- `tutorials` - Step-by-step guides
- `tips` - Quick tips and tricks

### Tags
Use tags for more specific topics. They appear on post cards and can be used for filtering.

## Deployment

The blog is automatically deployed to GitHub Pages when changes are pushed to the `main` branch. The deployment is handled by the GitHub Actions workflow in `.github/workflows/build-deploy.yml`.

