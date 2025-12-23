---
layout: page
title: About
description: "About Java for Scala Developers - learn about the author Sylwester Stocki, the purpose of this guide, and how to contribute to the project."
permalink: /about/
---

# About Java for Scala Developers

This blog is a practical guide for Scala developers who need to work with Java again. Whether you're transitioning to a Java project, working in a polyglot environment, or just curious about modern Java, this guide is for you.

<div class="blog-statistics">
  <h3>📊 Blog Statistics</h3>
  <div class="stats-grid">
    <div class="stat-card">
      <svg class="stat-icon-large" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
        <circle cx="12" cy="12" r="3"></circle>
      </svg>
      <span class="stat-value total-blog-views">—</span>
      <span class="stat-label">Total Visits</span>
    </div>
    <div class="stat-card">
      <svg class="stat-icon-large" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
        <polyline points="14 2 14 8 20 8"></polyline>
        <line x1="16" y1="13" x2="8" y2="13"></line>
        <line x1="16" y1="17" x2="8" y2="17"></line>
        <polyline points="10 9 9 9 8 9"></polyline>
      </svg>
      <span class="stat-value">{{ site.posts | size }}</span>
      <span class="stat-label">Blog Posts</span>
    </div>
    <div class="stat-card">
      <svg class="stat-icon-large" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
        <line x1="7" y1="7" x2="7.01" y2="7"></line>
      </svg>
      <span class="stat-value">{% assign all_tags = site.posts | map: "tags" | join: "," | split: "," | uniq | size %}{{ all_tags }}</span>
      <span class="stat-label">Topics Covered</span>
    </div>
  </div>
</div>

## Why This Guide?

Many Scala developers started with Java and then moved to Scala for its functional programming features, type inference, and concise syntax. However, Java has evolved significantly, especially with recent releases (Java 17, 21, and beyond).

This guide helps you:

- **Understand modern Java features** - Records, sealed classes, pattern matching, virtual threads
- **Compare with Scala** - See how Java's new features relate to what you already know
- **Practical examples** - Real code examples you can run and experiment with

## About the Author

Hi! I'm **Sylwester Stocki**, a passionate software engineer with extensive experience in JVM technologies.

### Education

I hold a Batchelor's degree in Computer Science from Warsaw University of Technology, where I developed a strong foundation in software engineering principles and computer science fundamentals.

### Work Experience

Throughout my career, I have specialized in building scalable, distributed systems using JVM-based technologies. My professional journey includes:

- **Senior Software Engineer** roles at various technology companies, focusing on backend development
- Extensive experience with **Scala**, **Java**, and **Kotlin** in production environments
- Working with big data technologies including **Apache Spark** for large-scale data processing
- Building microservices architectures and distributed systems
- Mentoring developers and contributing to technical excellence within engineering teams

I created this guide to share my experience and help fellow developers navigate between Scala and modern Java effectively.

### Connect with Me

- **LinkedIn**: [linkedin.com/in/sylwesterstocki](https://www.linkedin.com/in/sylwesterstocki)
- **GitHub**: [github.com/sps23](https://github.com/sps23)

## Contributing

This is an open-source project! Feel free to:
- [Open an issue](https://github.com/sps23/java-for-scala-devs/issues) for questions or suggestions
- Submit a pull request with improvements
- Share your own experiences

## Repository

Find the source code at [github.com/sps23/java-for-scala-devs](https://github.com/sps23/java-for-scala-devs).
