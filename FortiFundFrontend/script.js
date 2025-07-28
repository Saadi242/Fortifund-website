// script.js - Handles dynamic content loading, sticky header, dropdown, and form submissions

document.addEventListener('DOMContentLoaded', () => {
    const mainHeader = document.getElementById('main-header');
    const contactForm = document.getElementById('contactForm');
    const contactMessage = document.getElementById('contactMessage');
    const bookDemoForm = document.getElementById('bookDemoForm');
    const demoMessage = document.getElementById('demoMessage');

    // Base URL for your backend API
    const API_BASE_URL = 'http://localhost:8080/api'; // Adjust if your backend is on a different host/port

    // --- Sticky Header ---
    window.addEventListener('scroll', () => {
        if (window.scrollY > 0) {
            mainHeader.classList.add('header-sticky');
        } else {
            mainHeader.classList.remove('header-sticky');
        }
    });

    // --- Dynamic Content Loading for various sections ---
    // Select all sections that have a 'data-section' attribute
    const dynamicSections = document.querySelectorAll('[data-section]');
    dynamicSections.forEach(section => {
        const sectionName = section.dataset.section;
        fetchContentForSection(sectionName, section);
    });

    async function fetchContentForSection(sectionName, sectionElement) {
        try {
            // Fetch text content
            const textResponse = await fetch(`${API_BASE_URL}/content/text-content?section_name=${sectionName}`);
            if (!textResponse.ok) {
                console.warn(`Text content for section "${sectionName}" not found or error: ${textResponse.status}`);
                // Continue, as other asset types might still exist
            }
            const textData = await textResponse.json();

            // Fetch image assets
            const imageResponse = await fetch(`${API_BASE_URL}/content/image-assets?section_name=${sectionName}`);
            if (!imageResponse.ok) {
                console.warn(`Image assets for section "${sectionName}" not found or error: ${imageResponse.status}`);
            }
            const imageData = await imageResponse.json();

            // Fetch video assets
            const videoResponse = await fetch(`${API_BASE_URL}/content/video-assets?section_name=${sectionName}`);
            if (!videoResponse.ok) {
                console.warn(`Video assets for section "${sectionName}" not found or error: ${videoResponse.status}`);
            }
            const videoData = await videoResponse.json();

            // Combine all data for the section
            const data = {
                textContent: textData.textContent || {},
                imageAssets: imageData.imageAssets || {},
                videoAssets: videoData.videoAssets || {}
            };

            // Define how content keys map to HTML elements and how their values are applied
            // Use innerHTML for content that might contain HTML tags (like <br>)
            // Use textContent for plain text
            const applyContent = (element, value, useInnerHTML = false) => {
                if (element && value !== undefined && value !== null) { // Ensure value is not undefined/null
                    if (useInnerHTML) {
                        element.innerHTML = value;
                    } else {
                        element.textContent = value;
                    }
                }
            };

            // Apply text content
            for (const key in data.textContent) {
                const element = sectionElement.querySelector(`[data-content-key="${key}"]`);
                // Check if the key might contain HTML (e.g., hero_title has <br>)
                // Add more keys here if they contain HTML (e.g., contact_email_text)
                const useInnerHTML = (key === 'hero_title' || key === 'contact_email_text' || key === 'coming_soon_text');
                applyContent(element, data.textContent[key], useInnerHTML);
            }

            // Apply image assets
            for (const key in data.imageAssets) {
                const asset = data.imageAssets[key];
                const element = sectionElement.querySelector(`[data-asset-key="${key}"]`);
                if (element && asset && asset.url) { // Ensure asset and URL exist
                    let img = element.querySelector('img');
                    if (!img) {
                        img = document.createElement('img');
                        // Preserve existing content if it's not just a placeholder
                        // Or clear if you want to strictly replace
                        element.innerHTML = ''; // Clear existing content to replace with image
                        element.appendChild(img);
                    }
                    img.src = asset.url;
                    img.alt = asset.altText || '';
                    // Add any specific classes for styling if needed
                    // img.classList.add('dynamic-image');
                }
            }

            // Apply video assets
            for (const key in data.videoAssets) {
                const asset = data.videoAssets[key];
                const element = sectionElement.querySelector(`[data-asset-key="${key}"]`);
                if (element && asset && asset.url) { // Ensure asset and URL exist
                    let video = element.querySelector('video');
                    if (!video) {
                        video = document.createElement('video');
                        video.autoplay = true;
                        video.loop = true;
                        video.muted = true;
                        video.controls = false; // Or true, depending on design
                        // Clear existing content to replace with video
                        element.innerHTML = '';
                        element.appendChild(video);
                    }
                    video.src = asset.url;
                    video.poster = asset.posterUrl || '';
                    // Add any specific classes for styling if needed
                    // video.classList.add('dynamic-video');
                }
            }

        } catch (error) {
            console.error(`Error fetching or applying content for section "${sectionName}":`, error);
            // You might want to display a specific error for this section or fallback to default content
        }
    }

    // --- FAQ Accordion (only active on faq.html) ---
    const faqContainer = document.querySelector('.faq-container');
    if (faqContainer && window.location.pathname.includes('faq.html')) {
        fetchFaqItemsPublic(); // Call a new function for public FAQ page
    }

    async function fetchFaqItemsPublic() {
        try {
            const response = await fetch(`${API_BASE_URL}/content/faq-items`); // This is the public API
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const faqItems = await response.json();
            renderFaqItemsPublic(faqItems);
        } catch (error) {
            console.error('Error fetching public FAQ items:', error);
            if (faqContainer) {
                faqContainer.innerHTML = '<p class="form-message error">Failed to load FAQ items.</p>';
            }
        }
    }

    function renderFaqItemsPublic(faqItems) {
        const faqContainer = document.querySelector('.faq-container');
        if (!faqContainer) return;

        faqContainer.innerHTML = ''; // Clear existing static content

        faqItems.forEach(item => {
            const accordionItem = document.createElement('div');
            accordionItem.classList.add('accordion-item');

            accordionItem.innerHTML = `
                <div class="accordion-header" data-accordion="faq${item.id}">
                    <h3 class="accordion-title">${item.question}</h3>
                    <span class="accordion-arrow">&#x25BC;</span>
                </div>
                <div class="accordion-content" id="faq${item.id}-content">
                    <p>${item.answer}</p>
                </div>
            `;
            faqContainer.appendChild(accordionItem);
        });

        // Re-attach event listeners to new accordion headers
        const newAccordionHeaders = faqContainer.querySelectorAll('.accordion-header');
        newAccordionHeaders.forEach(header => {
            header.addEventListener('click', () => {
                const content = document.getElementById(header.dataset.accordion + '-content');
                if (content) { // Ensure content element exists
                    header.classList.toggle('active');
                    content.classList.toggle('open');
                    if (content.classList.contains('open')) {
                        content.style.maxHeight = content.scrollHeight + 'px';
                    } else {
                        content.style.maxHeight = '0';
                    }
                }
            });
        });
    }

    // --- Navbar Dynamic Loading ---
    const mainNavUl = document.querySelector('.main-nav ul'); // Get the <ul> element inside <nav class="main-nav">
    if (mainNavUl) {
        fetchAndRenderNavbar();
    }

    async function fetchAndRenderNavbar() {
        try {
            const response = await fetch(`${API_BASE_URL}/content/navbar-items`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const navbarItems = await response.json();
            renderNavbar(navbarItems);
        } catch (error) {
            console.error('Error fetching navbar items:', error);
            // Fallback: If dynamic loading fails, the HTML will show the static navbar (if present)
            // Or you can display an error message in the navbar area.
            if (mainNavUl) {
                mainNavUl.innerHTML = '<li><a href="#" class="nav-link">Error loading navigation</a></li>';
            }
        }
    }

    function renderNavbar(items) {
        if (!mainNavUl) return;
        mainNavUl.innerHTML = ''; // Clear existing hardcoded navbar items

        const currentPagePath = window.location.pathname.split('/').pop();

        items.forEach(item => {
            const li = document.createElement('li');

            if (item.isDropdown && item.children && item.children.length > 0) {
                li.classList.add('dropdown');
                const dropdownToggle = document.createElement('a');
                dropdownToggle.href = item.itemHref;
                dropdownToggle.classList.add('nav-link', 'dropdown-toggle');
                dropdownToggle.innerHTML = `${item.itemText} <span class="arrow-down">&#9660;</span>`;
                li.appendChild(dropdownToggle);

                const dropdownMenu = document.createElement('div');
                dropdownMenu.classList.add('dropdown-menu');
                item.children.forEach(child => {
                    const childLink = document.createElement('a');
                    childLink.href = child.itemHref;
                    childLink.textContent = child.itemText;
                    if (child.itemHref === currentPagePath) {
                        childLink.classList.add('current-page');
                        dropdownToggle.classList.add('current-page'); // Highlight parent dropdown
                    }
                    dropdownMenu.appendChild(childLink);
                });
                li.appendChild(dropdownMenu);

                // Re-attach dropdown toggle event listener
                dropdownToggle.querySelector('.arrow-down').addEventListener('click', function(event) {
                    event.preventDefault();
                    dropdownMenu.classList.toggle('show');
                });
            } else {
                const link = document.createElement('a');
                link.href = item.itemHref;
                link.textContent = item.itemText;
                link.classList.add('nav-link');
                if (item.itemHref === currentPagePath) {
                    link.classList.add('current-page');
                }
                // Special handling for BOOK DEMO button
                if (item.itemText === 'BOOK DEMO') { // Assuming "BOOK DEMO" is always a button
                    link.classList.add('btn', 'btn-primary');
                }
                li.appendChild(link);
            }
            mainNavUl.appendChild(li);
        });

        // Re-attach global click listener for dropdowns (to close when clicking outside)
        document.addEventListener('click', function(event) {
            document.querySelectorAll('.dropdown-menu.show').forEach(openDropdown => {
                if (!event.target.closest('.dropdown') || !openDropdown.contains(event.target)) {
                    openDropdown.classList.remove('show');
                }
            });
        });
    }


    // --- Highlight current page in navigation (REMOVED - now handled by renderNavbar) ---
    // The previous logic for `currentPagePath` and `navLinks.forEach` is now integrated
    // directly into `renderNavbar` to ensure dynamic links get the `current-page` class.
    // So, this block is no longer needed:
    /*
    const currentPagePath = window.location.pathname.split('/').pop();
    const navLinks = document.querySelectorAll('.main-nav .nav-link');
    navLinks.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath === currentPagePath) {
            link.classList.add('current-page');
        } else {
            const parentDropdown = link.closest('.dropdown');
            if (parentDropdown && parentDropdown.querySelector('.dropdown-menu a.current-page')) {
                parentDropdown.querySelector('.dropdown-toggle').classList.add('current-page');
            }
        }
    });
    */


    // --- Dropdown Navigation (REMOVED - now handled by renderNavbar) ---
    // The previous event listeners for `droptn` and `dropdown` are now attached
    // directly within `renderNavbar` for dynamically created elements.
    // So, this block is no longer needed:
    /*
    let droptn = document.querySelector('.arrow-down');
    let dropdown = document.querySelector('.dropdown-menu');

    if (droptn && dropdown) {
        droptn.addEventListener('click', function(event) {
            event.preventDefault();
            dropdown.classList.toggle('show');
        });

        document.addEventListener('click', function(event) {
            if (!event.target.closest('.dropdown') && dropdown.classList.contains('show')) {
                dropdown.classList.remove('show');
            }
        });
    }
    */


    // --- Contact Form Submission ---
    if (contactForm) {
        contactForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            contactMessage.style.display = 'none';
            contactMessage.classList.remove('success', 'error');

            const formData = new FormData(contactForm);
            const data = Object.fromEntries(formData.entries());

            try {
                const response = await fetch(`${API_BASE_URL}/submit/contact`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (response.ok) {
                    contactMessage.textContent = result.message;
                    contactMessage.classList.add('success');
                    contactForm.reset();
                } else {
                    contactMessage.textContent = result.message || 'Failed to send message.';
                    contactMessage.classList.add('error');
                }
            } catch (error) {
                console.error('Contact form submission error:', error);
                contactMessage.textContent = 'An error occurred. Please try again later.';
                contactMessage.classList.add('error');
            } finally {
                contactMessage.style.display = 'block';
            }
        });
    }

    // --- Book Demo Form Submission ---
    if (bookDemoForm) {
        bookDemoForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            demoMessage.style.display = 'none';
            demoMessage.classList.remove('success', 'error');

            const formData = new FormData(bookDemoForm);
            const data = Object.fromEntries(formData.entries());

            try {
                const response = await fetch(`${API_BASE_URL}/submit/demo`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (response.ok) {
                    demoMessage.textContent = result.message;
                    demoMessage.classList.add('success');
                    bookDemoForm.reset();
                } else {
                    demoMessage.textContent = result.message || 'Failed to schedule demo.';
                    demoMessage.classList.add('error');
                }
            } catch (error) {
                console.error('Book Demo form submission error:', error);
                demoMessage.textContent = 'An error occurred. Please try again later.';
                demoMessage.classList.add('error');
            } finally {
                demoMessage.style.display = 'block';
            }
        });
    }
});
