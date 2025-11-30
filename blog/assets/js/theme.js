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
    const categoryBtns = document.querySelectorAll('.category-btn, .category-badge, .category-pill');
    if (!categoryBtns.length) return;

    const posts = document.querySelectorAll('.post-item, .post-card, .timeline-post, .post-list-item');
    const categoryGroups = document.querySelectorAll('.category-group');
    const visibleCountEl = document.getElementById('visible-count');

    categoryBtns.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        
        // Update active state
        categoryBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        const category = btn.dataset.category || btn.textContent.trim().toLowerCase();
        
        // "all" shows everything
        if (category === 'all') {
          posts.forEach(post => post.style.display = '');
          categoryGroups.forEach(group => group.style.display = '');
          updateVisibleCount(posts.length, visibleCountEl);
          return;
        }
        
        // Filter posts in post list
        let visibleCount = 0;
        posts.forEach(function(post) {
          const postCategories = post.dataset.categories || '';
          const postTags = post.dataset.tags || '';
          const categoryEl = post.querySelector('.post-category, .post-card-category');
          const categoryText = categoryEl ? categoryEl.textContent.toLowerCase() : '';
          
          const matches = postCategories.toLowerCase().includes(category) ||
                         postTags.toLowerCase().includes(category) ||
                         categoryText.includes(category);
          
          post.style.display = matches ? '' : 'none';
          if (matches) visibleCount++;
        });

        // Filter category groups
        categoryGroups.forEach(function(group) {
          const groupCategory = group.dataset.category;
          group.style.display = (groupCategory === category) ? '' : 'none';
        });

        updateVisibleCount(visibleCount, visibleCountEl);
      });
    });

    // Handle "View all" links in category groups
    const viewAllLinks = document.querySelectorAll('.view-all-link');
    viewAllLinks.forEach(function(link) {
      link.addEventListener('click', function(e) {
        e.preventDefault();
        const category = link.dataset.category;

        // Find and click the corresponding category button
        const categoryBtn = document.querySelector('.category-btn[data-category="' + category + '"]');
        if (categoryBtn) {
          categoryBtn.click();

          // Scroll to posts list
          const postsList = document.querySelector('.post-list, h2');
          if (postsList) {
            postsList.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }
      });
    });
  }

  /**
   * Update visible post counter
   */
  function updateVisibleCount(count, element) {
    if (element) {
      element.textContent = count;
      element.style.animation = 'none';
      setTimeout(function() {
        element.style.animation = 'fadeIn 0.3s ease';
      }, 10);
    }
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
