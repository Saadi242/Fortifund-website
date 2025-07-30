document.addEventListener('DOMContentLoaded', () => {
    const API_BASE_URL = 'http://localhost:8080/api'; // Your backend API base URL

    // Get elements
    const loginSection = document.getElementById('loginSection');
    const adminLoginForm = document.getElementById('adminLoginForm');
    const loginMessage = document.getElementById('loginMessage');
    const dashboardSection = document.getElementById('dashboardSection');
    const logoutBtn = document.getElementById('logoutBtn');

    const adminTabs = document.querySelectorAll('.admin-tabs .tab-button');
    const contentManagementTab = document.getElementById('contentManagement');
    const assetManagementTab = document.getElementById('assetManagement');
    const faqManagementTab = document.getElementById('faqManagement');
    const navbarManagementTab = document.getElementById('navbarManagement');
    const formSubmissionsTab = document.getElementById('formSubmissions');

    // Text Content elements
    const contentEditor = document.getElementById('contentEditor');
    const saveContentBtn = document.getElementById('saveContentBtn');
    const contentMessage = document.getElementById('contentMessage');
    let textContentData = {}; // To store fetched text content for editing

    // Asset Management elements
    const assetTabs = document.querySelectorAll('.asset-tabs .asset-tab-button');
    const imageAssetsList = document.getElementById('imageAssetsList');
    const videoAssetsList = document.getElementById('videoAssetsList');
    const addImageBtn = document.getElementById('addImageBtn');
    const addVideoBtn = document.getElementById('addVideoBtn');
    const assetModal = document.getElementById('assetModal');
    const assetModalTitle = document.getElementById('assetModalTitle');
    const assetForm = document.getElementById('assetForm');
    const assetId = document.getElementById('assetId');
    const assetTypeInput = document.getElementById('assetType');
    const assetSectionName = document.getElementById('assetSectionName');
    const assetKey = document.getElementById('assetKey');
    const assetUrl = document.getElementById('assetUrl');
    const assetAltText = document.getElementById('assetAltText');
    const posterUrlGroup = document.getElementById('posterUrlGroup');
    const assetPosterUrl = document.getElementById('assetPosterUrl');
    const assetMessage = document.getElementById('assetMessage');

    // FAQ Management elements
    const faqList = document.getElementById('faqList');
    const addFaqBtn = document.getElementById('addFaqBtn');
    const faqModal = document.getElementById('faqModal');
    const faqModalTitle = document.getElementById('faqModalTitle');
    const faqForm = document.getElementById('faqForm');
    const faqId = document.getElementById('faqId');
    const faqQuestion = document.getElementById('faqQuestion');
    const faqAnswer = document.getElementById('faqAnswer');
    const faqDisplayOrder = document.getElementById('faqDisplayOrder');
    const faqMessage = document.getElementById('faqMessage');

    // Navbar Management elements
    const navbarList = document.getElementById('navbarList');
    const saveNavbarBtn = document.getElementById('saveNavbarBtn');
    const navbarMessage = document.getElementById('navbarMessage');
    let navbarItemsData = []; // To store fetched navbar items

    // Form Submissions elements (existing)
    const formTabs = document.querySelectorAll('.form-tabs .form-tab-button');
    const contactMessagesList = document.getElementById('contactMessagesList');
    const demoRequestsList = document.getElementById('demoRequestsList');

    // Close buttons for modals
    document.querySelectorAll('.modal .close-button').forEach(button => {
        button.addEventListener('click', (e) => {
            e.target.closest('.modal').style.display = 'none';
        });
    });

    // Close modal if clicked outside content
    window.addEventListener('click', (event) => {
        if (event.target === assetModal) {
            assetModal.style.display = 'none';
        }
        if (event.target === faqModal) {
            faqModal.style.display = 'none';
        }
    });

    // --- Authentication Logic ---
    const isAuthenticated = () => {
        return localStorage.getItem('adminLoggedIn') === 'true';
    };

    const setAuthenticated = (status) => {
        localStorage.setItem('adminLoggedIn', status);
        updateUIForAuthStatus();
    };

    const updateUIForAuthStatus = () => {
        if (isAuthenticated()) {
            loginSection.style.display = 'none';
            dashboardSection.style.display = 'block';
            logoutBtn.style.display = 'inline-block';
            // Trigger initial content load for the default active tab
            document.querySelector('.admin-tabs .tab-button.active').click();
        } else {
            loginSection.style.display = 'block';
            dashboardSection.style.display = 'none';
            logoutBtn.style.display = 'none';
            adminLoginForm.reset(); // Clear login form
            loginMessage.style.display = 'none'; // Hide any previous login messages
        }
    };

    adminLoginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        loginMessage.style.display = 'none';
        loginMessage.classList.remove('success', 'error');

        const username = adminLoginForm.username.value;
        const password = adminLoginForm.password.value;

        try {
            const response = await fetch(`${API_BASE_URL}/admin/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const result = await response.json();

            if (response.ok && result.success) {
                loginMessage.textContent = result.message;
                loginMessage.classList.add('success');
                setAuthenticated(true);
            } else {
                loginMessage.textContent = result.message || 'Login failed.';
                loginMessage.classList.add('error');
            }
        } catch (error) {
            console.error('Login error:', error);
            loginMessage.textContent = 'An error occurred during login. Please try again.';
            loginMessage.classList.add('error');
        } finally {
            loginMessage.style.display = 'block';
        }
    });

    logoutBtn.addEventListener('click', () => {
        setAuthenticated(false);
    });

    // Initial UI update on page load
    updateUIForAuthStatus();

    // --- Main Tab Switching Logic ---
    adminTabs.forEach(button => {
        button.addEventListener('click', () => {
            adminTabs.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            const tabId = button.dataset.tab;
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            document.getElementById(tabId).classList.add('active');

            // Fetch data relevant to the activated tab
            if (tabId === 'contentManagement') {
                fetchAdminContent();
            } else if (tabId === 'assetManagement') {
                // Activate default asset tab (Images) and fetch its data
                document.querySelector('.asset-tab-button[data-asset-tab="images"]').click();
            } else if (tabId === 'faqManagement') {
                fetchFaqs();
            } else if (tabId === 'navbarManagement') {
                fetchNavbarItems();
            } else if (tabId === 'formSubmissions') {
                // Activate default form tab (Contact Messages) and fetch its data
                document.querySelector('.form-tab-button[data-form-tab="contact"]').click();
            }
        });
    });

    // --- Form Sub-Tab Switching Logic (Contact vs. Demo) ---
    formTabs.forEach(button => {
        button.addEventListener('click', () => {
            formTabs.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            const formTabId = button.dataset.formTab;
            document.querySelectorAll('.form-tab-content').forEach(content => {
                content.classList.remove('active');
            });

            if (formTabId === 'contact') {
                document.getElementById('contactMessages').classList.add('active');
                fetchContactMessages();
            } else if (formTabId === 'demo') {
                document.getElementById('demoRequests').classList.add('active');
                fetchDemoRequests();
            }
        });
    });

    // --- Asset Sub-Tab Switching Logic (Images vs. Videos) ---
    assetTabs.forEach(button => {
        button.addEventListener('click', () => {
            assetTabs.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            const assetTabId = button.dataset.assetTab;
            document.querySelectorAll('.asset-tab-content').forEach(content => {
                content.classList.remove('active');
            });

            if (assetTabId === 'images') {
                document.getElementById('imageAssets').classList.add('active');
                fetchAssets('image');
            } else if (assetTabId === 'videos') {
                document.getElementById('videoAssets').classList.add('active');
                fetchAssets('video');
            }
        });
    });


    // --- Content Management Logic (Existing, but improved rendering) ---
    async function fetchAdminContent() {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/content`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            textContentData = {}; // Clear previous data
            data.forEach(item => {
                if (!textContentData[item.sectionName]) {
                    textContentData[item.sectionName] = {};
                }
                textContentData[item.sectionName][item.contentKey] = item.contentValue;
            });
            renderContentEditor();
        } catch (error) {
            console.error('Error fetching admin text content:', error);
            contentEditor.innerHTML = '<p class="form-message error">Failed to load text content for editing.</p>';
        }
    }

    function renderContentEditor() {
        contentEditor.innerHTML = ''; // Clear existing content
        const sectionsHtml = {}; // Object to group HTML by sectionName

        for (const sectionName in textContentData) {
            let sectionContentHtml = `<h4 class="mini-heading">${formatSectionName(sectionName)}</h4>`;
            for (const contentKey in textContentData[sectionName]) {
                const value = textContentData[sectionName][contentKey];
                const inputType = (contentKey.includes('description') || contentKey.includes('comments') || contentKey.includes('message') || contentKey.includes('text') || contentKey.includes('answer')) ? 'textarea' : 'input';

                sectionContentHtml += `
                    <div class="content-item">
                        <label for="${sectionName}-${contentKey}">${formatContentKey(contentKey)}</label>
                        <${inputType} id="${sectionName}-${contentKey}" name="${sectionName}-${contentKey}" data-section="${sectionName}" data-key="${contentKey}" ${inputType === 'textarea' ? 'rows="3"' : 'type="text"'}>${value}</${inputType}>
                    </div>
                `;
            }
            sectionsHtml[sectionName] = `<div class="content-section-group">${sectionContentHtml}</div>`;
        }

        // Sort sections alphabetically by sectionName for consistent display
        const sortedSectionNames = Object.keys(sectionsHtml).sort();
        sortedSectionNames.forEach(sectionName => {
            contentEditor.innerHTML += sectionsHtml[sectionName];
        });
    }

    saveContentBtn.addEventListener('click', async () => {
        contentMessage.style.display = 'none';
        contentMessage.classList.remove('success', 'error');

        const inputs = contentEditor.querySelectorAll('input, textarea');
        const allContentUpdates = []; // Collect ALL content from the form

        inputs.forEach(input => {
            const sectionName = input.dataset.section;
            const contentKey = input.dataset.key;
            const contentValue = input.value;

            allContentUpdates.push({
                sectionName: sectionName,
                contentKey: contentKey,
                contentValue: contentValue
            });
        });

        if (allContentUpdates.length === 0) {
            contentMessage.textContent = 'No content fields found to save.';
            contentMessage.classList.add('error'); // Changed to error as this indicates an issue
            contentMessage.style.display = 'block';
            return;
        }

        try {
            // Send all updates in a single batch request
            const response = await fetch(`${API_BASE_URL}/admin/content`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(allContentUpdates) // Send the entire array
            });

            const result = await response.json();

            if (response.ok) {
                contentMessage.textContent = result.message;
                contentMessage.classList.add('success');
                fetchAdminContent(); // Re-fetch to ensure local data is in sync
            } else {
                contentMessage.textContent = result.message || `Failed to save all text content.`;
                contentMessage.classList.add('error');
            }
        } catch (error) {
            console.error('Error saving text content:', error);
            contentMessage.textContent = `An error occurred while saving text content: ${error.message}`;
            contentMessage.classList.add('error');
        } finally {
            contentMessage.style.display = 'block';
        }
    });

    // Helper functions for formatting keys (already present, just for context)
    function formatSectionName(name) {
        return name.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
    }

    function formatContentKey(key) {
        return key.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
    }

    // --- Asset Management Logic ---
    async function fetchAssets(type) {
        const listElement = type === 'image' ? imageAssetsList : videoAssetsList;
        listElement.innerHTML = `<p>Loading ${type} assets...</p>`;
        try {
            const response = await fetch(`${API_BASE_URL}/admin/assets/${type}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const assets = await response.json();
            renderAssets(assets, type);
        } catch (error) {
            console.error(`Error fetching ${type} assets:`, error);
            listElement.innerHTML = `<p class="form-message error">Failed to load ${type} assets.</p>`;
        }
    }

    function renderAssets(assets, type) {
        const listElement = type === 'image' ? imageAssetsList : videoAssetsList;
        listElement.innerHTML = ''; // Clear existing
        if (assets.length === 0) {
            listElement.innerHTML = `<p>No ${type} assets found. Click "Add New ${type === 'image' ? 'Image' : 'Video'}" to add one.</p>`;
            return;
        }

        assets.forEach(asset => {
            const itemDiv = document.createElement('div');
            itemDiv.classList.add('asset-item');
            itemDiv.innerHTML = `
                <p><strong>Section:</strong> ${asset.sectionName}</p>
                <p><strong>Key:</strong> ${asset.assetKey}</p>
                <p><strong>URL:</strong> <a href="${asset.url}" target="_blank">${asset.url}</a></p>
                ${type === 'image' ? `<p><strong>Alt Text:</strong> ${asset.altText || 'N/A'}</p><img src="${asset.url}" alt="${asset.altText || ''}" loading="lazy">` : ''}
                ${type === 'video' ? `<p><strong>Poster URL:</strong> ${asset.posterUrl || 'N/A'}</p><video src="${asset.url}" poster="${asset.posterUrl || ''}" controls width="100%"></video>` : ''}
                <div class="asset-actions">
                    <button class="btn btn-secondary btn-small edit-asset-btn" data-id="${asset.id}" data-type="${type}">Edit</button>
                    <button class="btn btn-secondary btn-small delete-asset-btn" data-id="${asset.id}" data-type="${type}">Delete</button>
                </div>
            `;
            listElement.appendChild(itemDiv);
        });

        // Attach event listeners for edit/delete buttons
        listElement.querySelectorAll('.edit-asset-btn').forEach(button => {
            button.addEventListener('click', (e) => openAssetModalForEdit(e.target.dataset.id, e.target.dataset.type));
        });
        listElement.querySelectorAll('.delete-asset-btn').forEach(button => {
            button.addEventListener('click', (e) => deleteAsset(e.target.dataset.id, e.target.dataset.type));
        });
    }

    addImageBtn.addEventListener('click', () => openAssetModalForAdd('image'));
    addVideoBtn.addEventListener('click', () => openAssetModalForAdd('video'));

    function openAssetModalForAdd(type) {
        assetModalTitle.textContent = `Add New ${type === 'image' ? 'Image' : 'Video'} Asset`;
        assetForm.reset();
        assetId.value = '';
        assetTypeInput.value = type;
        posterUrlGroup.style.display = type === 'video' ? 'block' : 'none';
        assetAltText.closest('.form-group').style.display = type === 'image' ? 'block' : 'none';
        assetMessage.style.display = 'none';
        assetMessage.classList.remove('success', 'error');
        assetModal.style.display = 'flex'; // Use flex to center
    }

    async function openAssetModalForEdit(id, type) {
        assetModalTitle.textContent = `Edit ${type === 'image' ? 'Image' : 'Video'} Asset`;
        assetForm.reset();
        assetId.value = id;
        assetTypeInput.value = type;
        posterUrlGroup.style.display = type === 'video' ? 'block' : 'none';
        assetAltText.closest('.form-group').style.display = type === 'image' ? 'block' : 'none';
        assetMessage.style.display = 'none';
        assetMessage.classList.remove('success', 'error');

        try {
            const response = await fetch(`${API_BASE_URL}/admin/assets/${type}`);
            const assets = await response.json();
            const asset = assets.find(a => a.id == id); // Find by ID
            if (asset) {
                assetSectionName.value = asset.sectionName;
                assetKey.value = asset.assetKey;
                assetUrl.value = asset.url;
                if (type === 'image') {
                    assetAltText.value = asset.altText || '';
                } else {
                    assetPosterUrl.value = asset.posterUrl || '';
                }
                assetModal.style.display = 'flex';
            } else {
                assetMessage.textContent = 'Asset not found.';
                assetMessage.classList.add('error');
                assetMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Error fetching asset for edit:', error);
            assetMessage.textContent = 'Failed to load asset data for editing.';
            assetMessage.classList.add('error');
            assetMessage.style.display = 'block';
        }
    }

    assetForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        assetMessage.style.display = 'none';
        assetMessage.classList.remove('success', 'error');

        const type = assetTypeInput.value;
        const method = assetId.value ? 'PUT' : 'POST';
        const url = `${API_BASE_URL}/admin/assets/${type}`;

        const data = {
            id: assetId.value ? parseInt(assetId.value) : undefined,
            sectionName: assetSectionName.value,
            assetKey: assetKey.value,
            url: assetUrl.value,
            altText: type === 'image' ? assetAltText.value : undefined,
            posterUrl: type === 'video' ? assetPosterUrl.value : undefined
        };

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                assetMessage.textContent = result.message;
                assetMessage.classList.add('success');
                fetchAssets(type); // Re-fetch list
                setTimeout(() => assetModal.style.display = 'none', 1500); // Close after success
            } else {
                assetMessage.textContent = result.message || `Failed to ${method === 'POST' ? 'add' : 'update'} asset.`;
                assetMessage.classList.add('error');
            }
        } catch (error) {
            console.error('Asset form submission error:', error);
            assetMessage.textContent = `An error occurred during asset ${method === 'POST' ? 'add' : 'update'}.`;
            assetMessage.classList.add('error');
        } finally {
            assetMessage.style.display = 'block';
        }
    });

    async function deleteAsset(id, type) {
        if (!confirm(`Are you sure you want to delete this ${type} asset?`)) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/admin/assets/${type}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: parseInt(id) })
            });

            const result = await response.json();

            if (response.ok) {
                alert(result.message); // Use alert for simplicity here, consider custom modal
                fetchAssets(type); // Re-fetch list
            } else {
                alert(result.message || `Failed to delete ${type} asset.`);
            }
        } catch (error) {
            console.error('Delete asset error:', error);
            alert('An error occurred during asset deletion.');
        }
    }


    // --- FAQ Management Logic ---
    async function fetchFaqs() {
        faqList.innerHTML = '<p>Loading FAQs...</p>';
        try {
            const response = await fetch(`${API_BASE_URL}/admin/faqs`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const faqs = await response.json();
            renderFaqs(faqs);
        } catch (error) {
            console.error('Error fetching FAQs:', error);
            faqList.innerHTML = '<p class="form-message error">Failed to load FAQs.</p>';
        }
    }

    function renderFaqs(faqs) {
        faqList.innerHTML = ''; // Clear existing
        if (faqs.length === 0) {
            faqList.innerHTML = '<p>No FAQ items found. Click "Add New FAQ" to add one.</p>';
            return;
        }

        faqs.forEach(faq => {
            const itemDiv = document.createElement('div');
            itemDiv.classList.add('data-item');
            itemDiv.innerHTML = `
                <p><strong>Order:</strong> ${faq.displayOrder}</p>
                <p><strong>Question:</strong> ${faq.question}</p>
                <p><strong>Answer:</strong> ${faq.answer}</p>
                <div class="item-actions">
                    <button class="btn btn-secondary btn-small edit-faq-btn" data-id="${faq.id}">Edit</button>
                    <button class="btn btn-secondary btn-small delete-faq-btn" data-id="${faq.id}">Delete</button>
                </div>
            `;
            faqList.appendChild(itemDiv);
        });

        // Attach event listeners for edit/delete buttons
        faqList.querySelectorAll('.edit-faq-btn').forEach(button => {
            button.addEventListener('click', (e) => openFaqModalForEdit(e.target.dataset.id));
        });
        faqList.querySelectorAll('.delete-faq-btn').forEach(button => {
            button.addEventListener('click', (e) => deleteFaq(e.target.dataset.id));
        });
    }

    addFaqBtn.addEventListener('click', () => openFaqModalForAdd());

    function openFaqModalForAdd() {
        faqModalTitle.textContent = 'Add New FAQ';
        faqForm.reset();
        faqId.value = '';
        faqMessage.style.display = 'none';
        faqMessage.classList.remove('success', 'error');
        faqModal.style.display = 'flex';
    }

    async function openFaqModalForEdit(id) {
        faqModalTitle.textContent = 'Edit FAQ';
        faqForm.reset();
        faqId.value = id;
        faqMessage.style.display = 'none';
        faqMessage.classList.remove('success', 'error');

        try {
            const response = await fetch(`${API_BASE_URL}/admin/faqs`);
            const faqs = await response.json();
            const faq = faqs.find(f => f.id == id);
            if (faq) {
                faqQuestion.value = faq.question;
                faqAnswer.value = faq.answer;
                faqDisplayOrder.value = faq.displayOrder;
                faqModal.style.display = 'flex';
            } else {
                faqMessage.textContent = 'FAQ not found.';
                faqMessage.classList.add('error');
                faqMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Error fetching FAQ for edit:', error);
            faqMessage.textContent = 'Failed to load FAQ data for editing.';
            faqMessage.classList.add('error');
            faqMessage.style.display = 'block';
        }
    }

    faqForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        faqMessage.style.display = 'none';
        faqMessage.classList.remove('success', 'error');

        const method = faqId.value ? 'PUT' : 'POST';
        const url = `${API_BASE_URL}/admin/faqs`;

        const data = {
            id: faqId.value ? parseInt(faqId.value) : undefined,
            question: faqQuestion.value,
            answer: faqAnswer.value,
            displayOrder: parseInt(faqDisplayOrder.value)
        };

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok) {
                faqMessage.textContent = result.message;
                faqMessage.classList.add('success');
                fetchFaqs(); // Re-fetch list
                setTimeout(() => faqModal.style.display = 'none', 1500); // Close after success
            } else {
                faqMessage.textContent = result.message || `Failed to ${method === 'POST' ? 'add' : 'update'} FAQ.`;
                faqMessage.classList.add('error');
            }
        } catch (error) {
            console.error('FAQ form submission error:', error);
            faqMessage.textContent = `An error occurred during FAQ ${method === 'POST' ? 'add' : 'update'}.`;
            faqMessage.classList.add('error');
        } finally {
            faqMessage.style.display = 'block';
        }
    });

    async function deleteFaq(id) {
        if (!confirm(`Are you sure you want to delete this FAQ?`)) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/admin/faqs`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: parseInt(id) })
            });

            const result = await response.json();

            if (response.ok) {
                alert(result.message);
                fetchFaqs(); // Re-fetch list
            } else {
                alert(result.message || `Failed to delete FAQ.`);
            }
        } catch (error) {
            console.error('Delete FAQ error:', error);
            alert('An error occurred during FAQ deletion.');
        }
    }

    // --- Navbar Management Logic ---
    async function fetchNavbarItems() {
        navbarList.innerHTML = '<p>Loading Navbar Items...</p>';
        try {
            const response = await fetch(`${API_BASE_URL}/admin/navbar`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            navbarItemsData = await response.json(); // Store fetched data
            renderNavbarItems();
        } catch (error) {
            console.error('Error fetching navbar items:', error);
            navbarList.innerHTML = '<p class="form-message error">Failed to load navbar items.</p>';
        }
    }

    function renderNavbarItems() {
        navbarList.innerHTML = ''; // Clear existing
        if (navbarItemsData.length === 0) {
            navbarList.innerHTML = '<p>No navbar items found. Add items via SQL or ensure data is present.</p>';
            return;
        }

        // Create a map for quick lookup and to build hierarchy
        const itemMap = new Map();
        navbarItemsData.forEach(item => {
            itemMap.set(item.id, { ...item, children: [] });
        });

        const rootItems = [];
        itemMap.forEach(item => {
            if (item.parentId === null || item.parentId === undefined) {
                rootItems.push(item);
            } else {
                const parent = itemMap.get(item.parentId);
                if (parent) {
                    parent.children.push(item);
                }
            }
        });

        // Sort root items and their children by displayOrder
        rootItems.sort((a, b) => a.displayOrder - b.displayOrder);
        rootItems.forEach(item => {
            item.children.sort((a, b) => a.displayOrder - b.displayOrder);
        });

        rootItems.forEach(item => {
            const itemDiv = document.createElement('div');
            itemDiv.classList.add('data-item', 'navbar-item', item.isDropdown ? 'dropdown-parent' : '');
            itemDiv.innerHTML = `
                <p><strong>Text:</strong> <input type="text" data-id="${item.id}" data-field="itemText" value="${item.itemText}"></p>
                <p><strong>Href:</strong> <input type="text" data-id="${item.id}" data-field="itemHref" value="${item.itemHref}"></p>
                <p><strong>Order:</strong> <input type="number" data-id="${item.id}" data-field="displayOrder" value="${item.displayOrder}" min="1"></p>
                <p><strong>Dropdown:</strong> <input type="checkbox" data-id="${item.id}" data-field="isDropdown" ${item.isDropdown ? 'checked' : ''}></p>
                <p><strong>Parent ID:</strong> <input type="number" data-id="${item.id}" data-field="parentId" value="${item.parentId !== null ? item.parentId : ''}" ${item.isDropdown ? 'disabled' : ''}></p>
                <div class="item-actions">
                    <button class="btn btn-secondary btn-small delete-navbar-btn" data-id="${item.id}">Delete</button>
                </div>
            `;
            navbarList.appendChild(itemDiv);

            if (item.children.length > 0) {
                const childrenDiv = document.createElement('div');
                childrenDiv.classList.add('dropdown-children');
                item.children.forEach(child => {
                    const childDiv = document.createElement('div');
                    childDiv.classList.add('data-item', 'navbar-child-item');
                    childDiv.innerHTML = `
                        <p><strong>Text:</strong> <input type="text" data-id="${child.id}" data-field="itemText" value="${child.itemText}"></p>
                        <p><strong>Href:</strong> <input type="text" data-id="${child.id}" data-field="itemHref" value="${child.itemHref}"></p>
                        <p><strong>Order:</strong> <input type="number" data-id="${child.id}" data-field="displayOrder" value="${child.displayOrder}" min="1"></p>
                        <p><strong>Dropdown:</strong> <input type="checkbox" data-id="${child.id}" data-field="isDropdown" ${child.isDropdown ? 'checked' : ''} disabled></p>
                        <p><strong>Parent ID:</strong> <input type="number" data-id="${child.id}" data-field="parentId" value="${child.parentId}" disabled></p>
                        <div class="item-actions">
                            <button class="btn btn-secondary btn-small delete-navbar-btn" data-id="${child.id}">Delete</button>
                        </div>
                    `;
                    childrenDiv.appendChild(childDiv);
                });
                navbarList.appendChild(childrenDiv);
            }
        });

        // Attach event listeners for input changes and delete buttons
        navbarList.querySelectorAll('input[type="text"], input[type="number"], input[type="checkbox"]').forEach(input => {
            input.addEventListener('change', (e) => {
                const id = parseInt(e.target.dataset.id);
                const field = e.target.dataset.field;
                let value;
                if (e.target.type === 'checkbox') {
                    value = e.target.checked;
                } else if (e.target.type === 'number') {
                    value = parseInt(e.target.value);
                } else {
                    value = e.target.value;
                }

                // Update the local data model
                const itemToUpdate = navbarItemsData.find(item => item.id === id);
                if (itemToUpdate) {
                    itemToUpdate[field] = value;
                }
            });
        });

        navbarList.querySelectorAll('.delete-navbar-btn').forEach(button => {
            button.addEventListener('click', (e) => deleteNavbarItem(e.target.dataset.id));
        });
    }

    saveNavbarBtn.addEventListener('click', async () => {
        navbarMessage.style.display = 'none';
        navbarMessage.classList.remove('success', 'error');

        try {
            // Send the entire updated navbarItemsData array
            const response = await fetch(`${API_BASE_URL}/admin/navbar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(navbarItemsData)
            });

            const result = await response.json();

            if (response.ok) {
                navbarMessage.textContent = result.message;
                navbarMessage.classList.add('success');
                fetchNavbarItems(); // Re-fetch to ensure local data is in sync
            } else {
                navbarMessage.textContent = result.message || `Failed to save navbar items.`;
                navbarMessage.classList.add('error');
            }
        } catch (error) {
            console.error('Error saving navbar items:', error);
            navbarMessage.textContent = `An error occurred while saving navbar items: ${error.message}`;
            navbarMessage.classList.add('error');
        } finally {
            navbarMessage.style.display = 'block';
        }
    });

    async function deleteNavbarItem(id) {
        if (!confirm(`Are you sure you want to delete this navbar item? This will also delete its children.`)) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/admin/navbar`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: parseInt(id) })
            });

            const result = await response.json();

            if (response.ok) {
                alert(result.message);
                fetchNavbarItems(); // Re-fetch list
            } else {
                alert(result.message || `Failed to delete navbar item.`);
            }
        } catch (error) {
                console.error('Delete navbar item error:', error);
                alert('An error occurred during navbar item deletion.');
        }
    }


    // --- Form Submissions Logic ---
    async function fetchContactMessages() {
        contactMessagesList.innerHTML = '<p>Loading contact messages...</p>';
        try {
            const response = await fetch(`${API_BASE_URL}/admin/submissions/contact`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const messages = await response.json();
            renderContactMessages(messages);
        } catch (error) {
            console.error('Error fetching contact messages:', error);
            contactMessagesList.innerHTML = '<p class="form-message error">Failed to load contact messages.</p>';
        }
    }

    function renderContactMessages(messages) {
        contactMessagesList.innerHTML = '';
        if (messages.length === 0) {
            contactMessagesList.innerHTML = '<p>No contact messages found.</p>';
            return;
        }
        messages.forEach(msg => {
            const itemDiv = document.createElement('div');
            itemDiv.classList.add('data-item');
            itemDiv.innerHTML = `
                <p><strong>Name:</strong> ${msg.name}</p>
                <p><strong>Email:</strong> ${msg.email}</p>
                <p><strong>Message:</strong> ${msg.message}</p>
                <p><strong>Submitted:</strong> ${new Date(msg.submissionTime).toLocaleString()}</p>
            `;
            contactMessagesList.appendChild(itemDiv);
        });
    }

    async function fetchDemoRequests() {
        demoRequestsList.innerHTML = '<p>Loading demo requests...</p>';
        try {
            const response = await fetch(`${API_BASE_URL}/admin/submissions/demo`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const requests = await response.json();
            renderDemoRequests(requests);
        } catch (error) {
            console.error('Error fetching demo requests:', error);
            demoRequestsList.innerHTML = '<p class="form-message error">Failed to load demo requests.</p>';
        }
    }

    function renderDemoRequests(requests) {
        demoRequestsList.innerHTML = '';
        if (requests.length === 0) {
            demoRequestsList.innerHTML = '<p>No demo requests found.</p>';
            return;
        }
        requests.forEach(req => {
            const itemDiv = document.createElement('div');
            itemDiv.classList.add('data-item');
            itemDiv.innerHTML = `
                <p><strong>Full Name:</strong> ${req.fullName}</p>
                <p><strong>Phone:</strong> ${req.phoneNumber || 'N/A'}</p>
                <p><strong>Company:</strong> ${req.company}</p>
                <p><strong>Email:</strong> ${req.email}</p>
                <p><strong>Comments:</strong> ${req.comments || 'N/A'}</p>
                <p><strong>Demo Date:</strong> ${req.demoDate || 'N/A'}</p>
                <p><strong>Demo Time:</strong> ${req.demoTime || 'N/A'}</p>
                <p><strong>Submitted:</strong> ${new Date(req.submissionTime).toLocaleString()}</p>
            `;
            demoRequestsList.appendChild(itemDiv);
        });
    }
});
