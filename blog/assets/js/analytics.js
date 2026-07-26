/**
 * Blog Analytics JavaScript
 * Handles view count display and reading progress tracking
 */

(function() {
  'use strict';

  // DOM Ready
  document.addEventListener('DOMContentLoaded', function() {
    initViewCounters();
    initReadingProgress();
  });

  /**
   * Get GoatCounter site name from the script tag
   * @returns {string|null} The site name or null if not found
   */
  function getGoatCounterSiteName() {
    var gcScript = document.querySelector('script[data-goatcounter]');
    if (!gcScript) {
      return null;
    }

    var gcUrl = gcScript.getAttribute('data-goatcounter');
    // Extract site name from URL (e.g., "https://site.goatcounter.com/count" -> "site")
    var match = gcUrl.match(/https:\/\/([^.]+)\.goatcounter\.com/);
    return match ? match[1] : null;
  }

  /**
   * Fetch view count from GoatCounter API
   * @param {string} siteName - The GoatCounter site name
   * @param {string} path - The page path to fetch count for
   * @param {HTMLElement} element - The element to update with the count
   */
  function fetchGoatCounterData(siteName, path, element) {
    // Use GoatCounter's public API to get page count
    // Note: This requires the site to have public stats enabled
    var apiUrl = 'https://' + siteName + '.goatcounter.com/counter/' + encodeURIComponent(path) + '.json';

    fetch(apiUrl)
      .then(function(response) {
        if (!response.ok) {
          throw new Error('Not found');
        }
        return response.json();
      })
      .then(function(data) {
        if (data && data.count !== undefined) {
          element.textContent = formatNumber(data.count);
          element.classList.add('loaded');
        } else {
          element.textContent = '0';
        }
      })
      .catch(function() {
        // If we can't fetch, show a placeholder
        element.textContent = '—';
      });
  }

  /**
   * Initialize view counters
   * Displays view counts from GoatCounter
   */
  function initViewCounters() {
    var siteName = getGoatCounterSiteName();
    
    var viewCountElements = document.querySelectorAll('.view-count');
    viewCountElements.forEach(function(element) {
      if (!siteName) {
        element.textContent = '—';
        return;
      }
      var path = element.dataset.path || window.location.pathname;
      fetchGoatCounterData(siteName, path, element);
    });

    // Also update total views if present
    var totalViewsElement = document.querySelector('.total-blog-views');
    if (totalViewsElement) {
      if (!siteName) {
        totalViewsElement.textContent = '—';
        return;
      }
      // Fetch total site visits using the root path '/'
      // GoatCounter aggregates all visits when querying the root
      fetchGoatCounterData(siteName, '/', totalViewsElement);
    }
  }

  /**
   * Format large numbers with K/M suffix
   */
  function formatNumber(num) {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  /**
   * Initialize reading progress tracking
   * Tracks when users read 80% or more of an article
   */
  function initReadingProgress() {
    const article = document.querySelector('.article-content');
    if (!article) return;

    // Create progress bar
    const progressBar = createProgressBar();
    
    // Track reading progress
    var maxProgress = 0;
    var hasReported80Percent = false;

    // Intersection Observer to track how much content has been viewed
    if ('IntersectionObserver' in window) {
      var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
          if (entry.isIntersecting) {
            var rect = entry.target.getBoundingClientRect();
            var windowHeight = window.innerHeight;
            var articleHeight = article.scrollHeight;
            var articleTop = article.getBoundingClientRect().top;
            var scrolledInArticle = Math.max(0, windowHeight - articleTop);
            var progress = Math.min(100, (scrolledInArticle / articleHeight) * 100);
            
            if (progress > maxProgress) {
              maxProgress = progress;
              updateProgressBar(progressBar, maxProgress);
            }
          }
        });
      }, {
        // Use fewer thresholds for better performance
        // The scroll handler provides granular updates
        threshold: [0, 0.1, 0.25, 0.5, 0.75, 0.9, 1.0]
      });

      observer.observe(article);
    }

    // Also track scroll progress
    var scrollHandler = debounce(function() {
      var progress = calculateReadProgress(article);
      if (progress > maxProgress) {
        maxProgress = progress;
        updateProgressBar(progressBar, maxProgress);
        
        // Report 80% completion to GoatCounter
        if (!hasReported80Percent && maxProgress >= 80) {
          hasReported80Percent = true;
          reportReadCompletion();
        }
      }
    }, 100);

    window.addEventListener('scroll', scrollHandler, { passive: true });
  }

  /**
   * Create reading progress bar
   */
  function createProgressBar() {
    const existingBar = document.querySelector('.reading-progress-bar');
    if (existingBar) return existingBar;

    const bar = document.createElement('div');
    bar.className = 'reading-progress-bar';
    bar.innerHTML = '<div class="reading-progress-fill"></div>';
    document.body.appendChild(bar);
    return bar;
  }

  /**
   * Update progress bar width
   */
  function updateProgressBar(bar, progress) {
    const fill = bar.querySelector('.reading-progress-fill');
    if (fill) {
      fill.style.width = progress + '%';
      
      // Add completed class when 80% is reached
      if (progress >= 80) {
        fill.classList.add('completed');
      }
    }
  }

  /**
   * Calculate reading progress based on scroll position
   */
  function calculateReadProgress(article) {
    var rect = article.getBoundingClientRect();
    var windowHeight = window.innerHeight;
    var docHeight = document.documentElement.scrollHeight;
    var scrollTop = window.scrollY || document.documentElement.scrollTop;
    
    // Calculate how much of the article has been scrolled past
    var articleStart = rect.top + scrollTop;
    var articleEnd = articleStart + article.offsetHeight;
    var viewportBottom = scrollTop + windowHeight;
    
    // Progress is based on how much of the article we've scrolled through
    var readPortion = Math.max(0, viewportBottom - articleStart);
    var progress = (readPortion / article.offsetHeight) * 100;
    
    return Math.min(100, Math.max(0, progress));
  }

  /**
   * Report 80% read completion to GoatCounter
   */
  function reportReadCompletion() {
    if (typeof window.goatcounter !== 'undefined' && window.goatcounter.count) {
      // Send a custom event for read completion
      window.goatcounter.count({
        path: window.location.pathname + '/read-80-percent',
        title: document.title + ' (80% Read)',
        event: true
      });
    }
  }

  /**
   * Debounce utility function
   */
  function debounce(func, wait) {
    var timeout;
    return function executedFunction() {
      var context = this;
      var args = arguments;
      var later = function() {
        timeout = null;
        func.apply(context, args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

})();
