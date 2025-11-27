/**
 * Blog Theme JavaScript
 * Handles search, filtering, navigation, and interactive features
 */

(function() {
  'use strict';

  // DOM Ready
  document.addEventListener('DOMContentLoaded', function() {
    initSearch();
    initCategoryFilter();
    initMobileMenu();
    initSmoothScroll();
    initTimelineAnimations();
    initLikes();
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
    const postList = document.querySelector('.post-list');
    
    categoryBtns.forEach(function(btn) {
      btn.addEventListener('click', function(e) {
        e.preventDefault();
        
        // Update active state
        categoryBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        const category = btn.dataset.category || btn.textContent.trim().toLowerCase();
        
        // "all" shows everything in original order
        if (category === 'all') {
          posts.forEach(post => post.style.display = '');
          // Restore original order
          if (postList) {
            var postsArray = Array.from(postList.querySelectorAll('.post-item'));
            postsArray.sort(function(a, b) {
              var dateA = a.querySelector('time');
              var dateB = b.querySelector('time');
              if (dateA && dateB) {
                return new Date(dateB.getAttribute('datetime')) - new Date(dateA.getAttribute('datetime'));
              }
              return 0;
            });
            postsArray.forEach(function(post) {
              postList.appendChild(post);
            });
          }
          return;
        }
        
        // "most-liked" sorts by likes count
        if (category === 'most-liked') {
          posts.forEach(post => post.style.display = '');
          if (postList) {
            var postsArray = Array.from(postList.querySelectorAll('.post-item'));
            postsArray.sort(function(a, b) {
              var likesA = getLikesCount(a.dataset.postId);
              var likesB = getLikesCount(b.dataset.postId);
              return likesB - likesA;
            });
            postsArray.forEach(function(post) {
              postList.appendChild(post);
            });
          }
          return;
        }
        
        // Filter posts by category
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
      });
      
      // Close menu on overlay click
      if (overlay) {
        overlay.addEventListener('click', function() {
          nav.classList.remove('active');
          overlay.classList.remove('active');
          menuToggle.setAttribute('aria-expanded', 'false');
        });
      }
      
      // Close menu on escape key
      document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && nav.classList.contains('active')) {
          nav.classList.remove('active');
          if (overlay) overlay.classList.remove('active');
          menuToggle.setAttribute('aria-expanded', 'false');
        }
      });
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

  /**
   * Likes functionality
   * Stores likes in localStorage
   */
  var LIKES_STORAGE_KEY = 'blog_post_likes';
  var USER_LIKES_KEY = 'blog_user_likes';

  function getLikesData() {
    try {
      return JSON.parse(localStorage.getItem(LIKES_STORAGE_KEY)) || {};
    } catch (e) {
      return {};
    }
  }

  function getUserLikes() {
    try {
      return JSON.parse(localStorage.getItem(USER_LIKES_KEY)) || {};
    } catch (e) {
      return {};
    }
  }

  function saveLikesData(data) {
    try {
      localStorage.setItem(LIKES_STORAGE_KEY, JSON.stringify(data));
    } catch (e) {
      // localStorage not available
    }
  }

  function saveUserLikes(data) {
    try {
      localStorage.setItem(USER_LIKES_KEY, JSON.stringify(data));
    } catch (e) {
      // localStorage not available
    }
  }

  function getLikesCount(postId) {
    var likesData = getLikesData();
    return likesData[postId] || 0;
  }

  function hasUserLiked(postId) {
    var userLikes = getUserLikes();
    return userLikes[postId] === true;
  }

  function toggleLike(postId) {
    var likesData = getLikesData();
    var userLikes = getUserLikes();
    
    if (userLikes[postId]) {
      // User already liked - unlike
      likesData[postId] = Math.max(0, (likesData[postId] || 1) - 1);
      delete userLikes[postId];
    } else {
      // User hasn't liked - add like
      likesData[postId] = (likesData[postId] || 0) + 1;
      userLikes[postId] = true;
    }
    
    saveLikesData(likesData);
    saveUserLikes(userLikes);
    
    return {
      count: likesData[postId] || 0,
      liked: userLikes[postId] === true
    };
  }

  function initLikes() {
    // Initialize like buttons on post pages
    var likeButtons = document.querySelectorAll('.like-btn');
    likeButtons.forEach(function(btn) {
      var postId = btn.dataset.postId;
      if (!postId) return;
      
      var countEl = btn.querySelector('.like-count');
      var count = getLikesCount(postId);
      var liked = hasUserLiked(postId);
      
      if (countEl) {
        countEl.textContent = count;
      }
      if (liked) {
        btn.classList.add('liked');
      }
      
      btn.addEventListener('click', function() {
        var result = toggleLike(postId);
        if (countEl) {
          countEl.textContent = result.count;
        }
        if (result.liked) {
          btn.classList.add('liked');
        } else {
          btn.classList.remove('liked');
        }
      });
    });
    
    // Initialize likes display on home page
    var postLikesElements = document.querySelectorAll('.post-likes');
    postLikesElements.forEach(function(el) {
      var postId = el.dataset.postId;
      if (!postId) return;
      
      var countEl = el.querySelector('.likes-count');
      var count = getLikesCount(postId);
      
      if (count > 0) {
        el.style.display = 'inline-flex';
        if (countEl) {
          countEl.textContent = count;
        }
      }
    });
  }

})();
