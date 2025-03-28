document.addEventListener('DOMContentLoaded', function() {
    // Find the timeline header and child panels
    const timelineHeader = document.querySelector('.timeline-header-container');
    const childPanels = document.querySelectorAll('.child-panel');

    // Add scroll event listener to timeline header
    timelineHeader.addEventListener('scroll', function() {
        // Sync scroll position of child panels
        childPanels.forEach(panel => {
            panel.scrollLeft = this.scrollLeft;
        });
    });

    const rects = document.querySelectorAll('.timeline-rectangle');
    console.log('Found rectangles:', rects.length);
    rects.forEach(rect => {
        console.log('Rectangle styles:', window.getComputedStyle(rect));
    });
});