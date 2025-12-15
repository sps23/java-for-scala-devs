# Configuration and Deployment Guide

This document explains the configuration strategy for the Java for Scala Developers blog, which supports multiple deployment environments.

## Configuration Files

### 1. `_config.yml` (Production)
- **Purpose**: Default configuration for production deployment
- **Settings**:
  - `baseurl: ""` - Works on root domain (javaforscaladevs.com)
  - `url: "https://javaforscaladevs.com"`
- **Used by**: GitHub Actions workflow, production builds
- **When to edit**: When changing global settings (title, description, plugins, etc.)

### 2. `_config.local.yml` (Local Development)
- **Purpose**: Override settings for local testing
- **Settings**:
  - `baseurl: "/java-for-scala-devs"` - Matches GitHub Pages path structure for testing
  - `url: "http://localhost:4000"`
- **Used by**: Local development only (must be explicitly specified)
- **When to use**: Never commit changes here; only for local testing

## Building & Deployment

### Local Development
Use `_config.local.yml` to test with a baseurl path (like GitHub Pages):

```bash
cd blog
bundle exec jekyll serve --config _config.yml,_config.local.yml
```

Visit: `http://localhost:4000/java-for-scala-devs/`

### Production Build (Manual)
Use only `_config.yml` for your domain:

```bash
cd blog
bundle exec jekyll build
```

### GitHub Actions (Automatic)
The workflow in `.github/workflows/build-deploy.yml`:
- Automatically triggers on pushes to `main`
- Uses default `_config.yml` (baseurl: "")
- Builds the site for production domain
- Uploads artifacts to GitHub Pages (or your CDN)

## Why This Structure?

**Problem**: Your site lives at `https://javaforscaladevs.com/` (root) but you also want to test locally with GitHub Pages structure (`/java-for-scala-devs/`).

**Solution**: 
- Main config (`_config.yml`) targets production (empty baseurl)
- Local config (`_config.local.yml`) simulates GitHub Pages path for testing
- Just specify `--config _config.yml,_config.local.yml` when testing locally
- GitHub Actions uses default production config

## Jekyll Configuration Layering

When you use multiple config files with Jekyll:
```bash
--config file1.yml,file2.yml,file3.yml
```

Later files **override** earlier ones. So in local development:
```bash
--config _config.yml,_config.local.yml
```

This loads `_config.yml` first, then `_config.local.yml` overrides just the `baseurl` and `url` settings.

## All Links Use `{{ site.baseurl }}`

All internal links in the blog use Jekyll's `{{ site.baseurl }}` variable:
```markdown
[Link Text]({{ site.baseurl }}/blog/2025/11/26/immutable-data-with-java-records/)
```

This automatically adapts to whichever `baseurl` is active, making links work in all environments.

## Quick Reference

| Environment | Command | Baseurl | URL |
|-----------|---------|---------|-----|
| Local Dev | `bundle exec jekyll serve --config _config.yml,_config.local.yml` | `/java-for-scala-devs` | http://localhost:4000 |
| Local Prod Test | `bundle exec jekyll build` | `` (empty) | https://javaforscaladevs.com |
| GitHub Actions | Automatic | `` (empty) | https://javaforscaladevs.com |

## Switching Domains

**To switch from GitHub Pages to your domain:**
- ✅ Already done! `_config.yml` now has `baseurl: ""` and `url: "https://javaforscaladevs.com"`

**To switch back to GitHub Pages:**
- Change `_config.yml`:
  - `baseurl: "/java-for-scala-devs"`
  - `url: "https://sps23.github.io"`
- Update GitHub Actions if needed

**To add a staging domain:**
- Create `_config.staging.yml` with staging URL
- Use in GitHub Actions for staging environment

