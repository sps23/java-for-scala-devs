/**
 * Blog Theme JavaScript
 * Handles search, filtering, navigation, and interactive features
 */

(function() {
  'use strict';

  // AbortController for managing event listeners
  var mobileMenuAbortController = null;

  // DOM Ready
  document.addEventListener('DOMContentLoaded', function() {
    initSearch();
    initCategoryFilter();
    initMobileMenu();
    initSmoothScroll();
    initTimelineAnimations();
    initCodeTabs();
  });

  /**
   * Search functionality
   * Filters posts based on search input
   */
  function initSearch() {
    const searchInput = document.querySelector('.search-input, .header-search input, .search-box input');
    if (!searchInput) return;

    const posts = document.querySelectorAll('.post-item, .post-card, .timeline-post, .post-list-item');
    
    searchInput.addEventListener('input', function(e) {
      const query = e.target.value.toLowerCase().trim();
      
      posts.forEach(function(post) {
        const title = post.querySelector('.post-title, .post-card-title, .post-list-title');
        const excerpt = post.querySelector('.post-excerpt, .post-card-excerpt, .post-list-excerpt');
        const tags = post.querySelector('.tags, .post-tags, .post-card-tags');
        
        const titleText = title ? title.textContent.toLowerCase() : '';
        const excerptText = excerpt ? excerpt.textContent.toLowerCase() : '';
        const tagsText = tags ? tags.textContent.toLowerCase() : '';
        
        const matches = titleText.includes(query) || 
                       excerptText.includes(query) || 
                       tagsText.includes(query);
        
        post.style.display = query === '' || matches ? '' : 'none';
      });
      
      updateNoResultsMessage(query, posts);
    });
  }

  /**
   * Show/hide no results message
   */
  function updateNoResultsMessage(query, posts) {
    let noResults = document.querySelector('.no-results');
    const visiblePosts = Array.from(posts).filter(p => p.style.display !== 'none');
    
    if (query && visiblePosts.length === 0) {
      if (!noResults && posts.length > 0) {
        noResults = document.createElement('div');
        noResults.className = 'no-results';
        noResults.innerHTML = '<p>No posts found matching your search.</p>';
        noResults.style.cssText = 'text-align: center; padding: 2rem; color: var(--text-muted);';
        
        const container = posts[0].parentElement;
        container.appendChild(noResults);
      }
      if (noResults) {
        noResults.style.display = 'block';
      }
    } else if (noResults) {
      noResults.style.display = 'none';
    }
  }

  /**
   * Category filtering
   * Filter posts by category/tag
   */
  function initCategoryFilter() {
    const filterContainer = document.querySelector('.category-filter');
    if (!filterContainer) return;

    const posts = document.querySelectorAll('.post-item, .post-card, .timeline-post, .post-list-item');
    if (!posts.length) return;

    const tagCounts = {};
    posts.forEach(function(post) {
      const rawTags = post.dataset.tags || '';
      const tags = rawTags.split(/\s+/).filter(Boolean);
      tags.forEach(function(tag) {
        const key = tag.toLowerCase();
        tagCounts[key] = (tagCounts[key] || 0) + 1;
      });
    });

    const sortedTags = Object.entries(tagCounts)
      .sort(function(a, b) {
        return b[1] - a[1] || a[0].localeCompare(b[0]);
      })
      .map(function(entry) {
        return entry[0];
      });

    const visibleTags = sortedTags.slice(0, 5);
    const moreTags = sortedTags.slice(5);

    filterContainer.innerHTML = '';

    const row = document.createElement('div');
    row.className = 'filter-main-row';

    const allBtn = document.createElement('button');
    allBtn.type = 'button';
    allBtn.className = 'category-btn active';
    allBtn.dataset.category = 'all';
    allBtn.textContent = 'All Posts';
    row.appendChild(allBtn);

    visibleTags.forEach(function(tag) {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'category-btn';
      btn.dataset.category = tag;
      btn.textContent = tag.charAt(0).toUpperCase() + tag.slice(1);
      row.appendChild(btn);
    });

    if (moreTags.length) {
      const wrap = document.createElement('div');
      wrap.className = 'filter-more-wrap';

      const moreBtn = document.createElement('button');
      moreBtn.type = 'button';
      moreBtn.className = 'filter-more-btn';
      moreBtn.setAttribute('aria-expanded', 'false');
      moreBtn.setAttribute('aria-label', 'Toggle more filters');
      moreBtn.title = 'Toggle more filters';

      const toggle = document.createElement('span');
      toggle.className = 'filter-more-toggle';
      toggle.setAttribute('aria-hidden', 'true');
      moreBtn.appendChild(toggle);

      const panel = document.createElement('div');
      panel.className = 'filter-more-panel';

      moreTags.forEach(function(tag) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'category-btn';
        btn.dataset.category = tag;
        btn.textContent = tag.charAt(0).toUpperCase() + tag.slice(1);
        panel.appendChild(btn);
      });

      moreBtn.addEventListener('click', function() {
        const expanded = moreBtn.getAttribute('aria-expanded') === 'true';
        moreBtn.setAttribute('aria-expanded', String(!expanded));
        panel.classList.toggle('visible', !expanded);
      });

      row.appendChild(moreBtn);
      wrap.appendChild(row);
      wrap.appendChild(panel);
      filterContainer.appendChild(wrap);
    } else {
      filterContainer.appendChild(row);
    }

    const categoryBtns = document.querySelectorAll('.category-btn, .category-badge, .category-pill');
    categoryBtns.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();

        categoryBtns.forEach(function(b) {
          b.classList.remove('active');
        });
        btn.classList.add('active');

        const category = btn.dataset.category || btn.textContent.trim().toLowerCase();

        if (category === 'all') {
          posts.forEach(function(post) {
            post.style.display = '';
          });
          return;
        }

        posts.forEach(function(post) {
          const postCategories = post.dataset.categories || '';
          const postTags = post.dataset.tags || '';
          const categoryEl = post.querySelector('.post-category, .post-card-category');
          const categoryText = categoryEl ? categoryEl.textContent.toLowerCase() : '';

          const matches = postCategories.toLowerCase().includes(category) ||
                         postTags.toLowerCase().includes(category) ||
                         categoryText.includes(category);

          post.style.display = matches ? '' : 'none';
        });
      });
    });
  }

  /**
   * Mobile menu toggle
   */
  function initMobileMenu() {
    // Abort previous listeners before adding new ones
    if (mobileMenuAbortController) {
      mobileMenuAbortController.abort();
    }
    mobileMenuAbortController = new AbortController();
    var signal = mobileMenuAbortController.signal;
    
    const menuToggle = document.querySelector('.menu-toggle, .mobile-menu-toggle, .mobile-menu-btn');
    const nav = document.querySelector('.site-nav, .sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    
    if (menuToggle && nav) {
      menuToggle.addEventListener('click', function() {
        nav.classList.toggle('active');
        if (overlay) overlay.classList.toggle('active');
        
        // Update aria-expanded
        const expanded = nav.classList.contains('active');
        menuToggle.setAttribute('aria-expanded', expanded);
      }, { signal: signal });
      
      // Close menu on overlay click
      if (overlay) {
        overlay.addEventListener('click', function() {
          nav.classList.remove('active');
          overlay.classList.remove('active');
          menuToggle.setAttribute('aria-expanded', 'false');
        }, { signal: signal });
      }
      
      // Close menu on escape key
      document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && nav.classList.contains('active')) {
          nav.classList.remove('active');
          if (overlay) overlay.classList.remove('active');
          menuToggle.setAttribute('aria-expanded', 'false');
        }
      }, { signal: signal });
    }
  }

  /**
   * Smooth scroll for anchor links
   */
  function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(function(anchor) {
      anchor.addEventListener('click', function(e) {
        const targetId = this.getAttribute('href');
        if (targetId === '#') return;
        
        const target = document.querySelector(targetId);
        if (target) {
          e.preventDefault();
          target.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
          });
        }
      });
    });
  }

  /**
   * Timeline scroll animations
   * Uses Intersection Observer for performance
   */
  function initTimelineAnimations() {
    const timelinePosts = document.querySelectorAll('.timeline-post');
    if (!timelinePosts.length) return;

    // Check if IntersectionObserver is supported
    if (!('IntersectionObserver' in window)) {
      timelinePosts.forEach(post => post.style.opacity = '1');
      return;
    }

    const observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          entry.target.style.animationPlayState = 'running';
          observer.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px'
    });

    timelinePosts.forEach(function(post) {
      post.style.animationPlayState = 'paused';
      observer.observe(post);
    });
  }

  /**
   * Initialize code tabs
   * Handle tab switching for code examples in multiple languages
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

  /**
   * Update active nav link based on current page
   */
  function updateActiveNavLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.site-nav a, .nav-links a');
    
    navLinks.forEach(function(link) {
      try {
        const linkPath = new URL(link.href, window.location.origin).pathname;
        if (currentPath === linkPath || currentPath.startsWith(linkPath + '/')) {
          link.classList.add('active');
        }
      } catch (e) {
        // Skip invalid URLs
      }
    });
  }

  // Initialize active nav on load
  updateActiveNavLink();

})();
