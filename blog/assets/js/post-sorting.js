/**
 * Post Sorting Algorithm
 * 
 * Sorts blog posts by their "effective date" which is:
 * - If a post has an 'updated' field: max(updated, date)
 * - If no 'updated' field: date
 * 
 * This ensures that both newly published and recently updated posts
 * appear at the top of the list in the correct order.
 * 
 * See blog/SORTING_ALGORITHM.md for detailed documentation.
 */

/**
 * Extract the effective date from a post element
 * 
 * @param {HTMLElement} post - The post element with data-date and data-updated attributes
 * @returns {Date} The effective date (max of updated and date, or just date if no update)
 */
function getEffectiveDate(post) {
  const dateStr = post.dataset.date;
  const updatedStr = post.dataset.updated;
  
  if (!dateStr) {
    console.warn('Post missing date:', post);
    return new Date(0);
  }
  
  try {
    const date = new Date(dateStr);
    const updated = updatedStr ? new Date(updatedStr) : date;
    
    // Return the more recent date (max)
    return updated.getTime() > date.getTime() ? updated : date;
  } catch (e) {
    console.error('Error parsing dates for post:', post, e);
    return new Date(0);
  }
}

/**
 * Sort an array of post elements by their effective date
 * 
 * @param {HTMLElement[]} posts - Array of post elements to sort
 * @returns {HTMLElement[]} Sorted posts (newest first)
 */
function sortPostsByEffectiveDate(posts) {
  return Array.from(posts).sort((a, b) => {
    const dateA = getEffectiveDate(a);
    const dateB = getEffectiveDate(b);
    // Descending order: newer posts first
    return dateB - dateA;
  });
}

/**
 * Apply the sorting algorithm to all post lists on the page
 * This function should be called after the DOM is ready
 */
function applyPostSorting() {
  // Sort Latest Posts (Template 1)
  const postList = document.querySelector('.post-list');
  if (postList) {
    const postItems = postList.querySelectorAll('li.post-item');
    if (postItems.length > 0) {
      const sortedPosts = sortPostsByEffectiveDate(postItems);
      
      // Reorder the DOM elements
      sortedPosts.forEach(post => {
        postList.appendChild(post);
      });
    }
  }
  
  // Sort Timeline (Template 1)
  const timeline = document.querySelector('.timeline');
  if (timeline) {
    const timelineItems = timeline.querySelectorAll('.timeline-item');
    if (timelineItems.length > 0) {
      const sortedItems = sortPostsByEffectiveDate(timelineItems);
      
      // Reorder the DOM elements
      sortedItems.forEach(item => {
        timeline.appendChild(item);
      });
    }
  }
  
  // Sort Post Grid (Template 2)
  const postGrid = document.querySelector('.post-grid');
  if (postGrid) {
    const postCards = postGrid.querySelectorAll('article.post-card');
    if (postCards.length > 0) {
      const sortedCards = sortPostsByEffectiveDate(postCards);
      
      // Reorder the DOM elements
      sortedCards.forEach(card => {
        postGrid.appendChild(card);
      });
    }
  }
  
  // Sort Posts Timeline (Template 3)
  const postsTimeline = document.querySelector('.posts-timeline');
  if (postsTimeline) {
    const timelinePosts = postsTimeline.querySelectorAll('article.timeline-post');
    if (timelinePosts.length > 0) {
      const sortedPosts = sortPostsByEffectiveDate(timelinePosts);
      
      // Reorder the DOM elements
      sortedPosts.forEach(post => {
        postsTimeline.appendChild(post);
      });
    }
  }
}

// Apply sorting when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', applyPostSorting);
} else {
  applyPostSorting();
}

// Export for testing purposes if running in Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    getEffectiveDate,
    sortPostsByEffectiveDate,
    applyPostSorting
  };
}
