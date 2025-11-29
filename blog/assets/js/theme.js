/**
 * Blog Theme JavaScript
 * Handles search, filtering, navigation, template switching, and interactive features
 */

(function() {
  'use strict';

  // Track current template for scoped queries
  var currentTemplateId = '1';
  
  // AbortController for managing event listeners
  var mobileMenuAbortController = null;

  /**
   * Get the currently visible template content element
   */
  function getVisibleTemplate() {
    return document.querySelector('.template-content[data-template="' + currentTemplateId + '"]');
  }

  /**
   * Query selector scoped to visible template
   */
  function scopedQuery(selector) {
    var template = getVisibleTemplate();
    return template ? template.querySelector(selector) : null;
  }

  /**
   * Query selector all scoped to visible template
   */
  function scopedQueryAll(selector) {
    var template = getVisibleTemplate();
    return template ? template.querySelectorAll(selector) : [];
  }

  // DOM Ready
  document.addEventListener('DOMContentLoaded', function() {
    initTemplateSwitcher();
    initSearch();
    initCategoryFilter();
    initMobileMenu();
    initSmoothScroll();
    initTimelineAnimations();
  });

  /**
   * Template Switcher functionality
   * Allows users to switch between templates dynamically
   */
  function initTemplateSwitcher() {
    var templateSelect = document.getElementById('template-select');
    if (!templateSelect) return;

    // Load saved template from localStorage or use default
    var savedTemplate = localStorage.getItem('selectedTemplate') || '1';
    currentTemplateId = savedTemplate;
    
    // Apply saved template on load
    applyTemplate(savedTemplate);
    templateSelect.value = savedTemplate;
    
    // Sync all mobile template selects
    syncMobileTemplateSelects(savedTemplate);

    // Listen for template changes on main select
    templateSelect.addEventListener('change', function(e) {
      var selectedTemplate = e.target.value;
      currentTemplateId = selectedTemplate;
      applyTemplate(selectedTemplate);
      localStorage.setItem('selectedTemplate', selectedTemplate);
      syncMobileTemplateSelects(selectedTemplate);
    });
    
    // Listen for template changes on mobile selects (using event delegation)
    document.addEventListener('change', function(e) {
      if (e.target.classList.contains('mobile-template-select')) {
        var selectedTemplate = e.target.value;
        currentTemplateId = selectedTemplate;
        applyTemplate(selectedTemplate);
        localStorage.setItem('selectedTemplate', selectedTemplate);
        templateSelect.value = selectedTemplate;
        syncMobileTemplateSelects(selectedTemplate);
      }
    });
  }
  
  /**
   * Sync all mobile template selects to the same value
   */
  function syncMobileTemplateSelects(templateId) {
    var mobileSelects = document.querySelectorAll('.mobile-template-select');
    mobileSelects.forEach(function(select) {
      select.value = templateId;
    });
  }

  /**
   * Apply the selected template
   * @param {string} templateId - The template number ('1', '2', or '3')
   */
  function applyTemplate(templateId) {
    // Update current template tracking
    currentTemplateId = templateId;

    // Switch CSS stylesheets
    var cssLinks = document.querySelectorAll('link[data-template]');
    cssLinks.forEach(function(link) {
      if (link.dataset.template === templateId) {
        link.removeAttribute('disabled');
      } else {
        link.setAttribute('disabled', 'disabled');
      }
    });

    // Switch template content (HTML)
    var templateContents = document.querySelectorAll('.template-content');
    templateContents.forEach(function(content) {
      if (content.dataset.template === templateId) {
        content.style.display = '';
      } else {
        content.style.display = 'none';
      }
    });

    // Re-initialize features for the new template using requestAnimationFrame
    // for more reliable timing than setTimeout
    requestAnimationFrame(function() {
      requestAnimationFrame(function() {
        initSearch();
        initCategoryFilter();
        initMobileMenu();
        initTimelineAnimations();
      });
    });
  }

  /**
   * Search functionality
   * Filters posts based on search input
   */
  function initSearch() {
    const searchInput = scopedQuery('.search-input, .header-search input, .search-box input');
    if (!searchInput) return;

    const posts = scopedQueryAll('.post-item, .post-card, .timeline-post, .post-list-item');
    
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
    let noResults = scopedQuery('.no-results');
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
    const categoryBtns = scopedQueryAll('.category-btn, .category-badge, .category-pill');
    if (!categoryBtns.length) return;

    const posts = scopedQueryAll('.post-item, .post-card, .timeline-post, .post-list-item');
    
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
          return;
        }
        
        // Filter posts
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
    
    const menuToggle = scopedQuery('.menu-toggle, .mobile-menu-toggle, .mobile-menu-btn');
    const nav = scopedQuery('.site-nav, .sidebar');
    const overlay = scopedQuery('.sidebar-overlay');
    
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
    const timelinePosts = scopedQueryAll('.timeline-post');
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
