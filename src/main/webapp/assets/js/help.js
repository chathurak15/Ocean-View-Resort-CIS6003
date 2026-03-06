// help.js - Role-specific Help Guide

document.addEventListener('DOMContentLoaded', () => {
    const userStr = sessionStorage.getItem('currentUser');
    if (!userStr) { window.location.replace('index.jsp'); return; }

    let user;
    try { user = JSON.parse(userStr); }
    catch (e) { window.location.replace('index.jsp'); return; }

    const role = (user.userType || '').toUpperCase();
    
    // Setup header logic similar to dashboard
    const authUsername = document.getElementById('authUsername');
    const authRole     = document.getElementById('authRole');
    if (authUsername) authUsername.textContent = user.username || user.name || 'User';
    if (authRole)     authRole.textContent     = user.userType || 'RECEPTIONIST';

    if (role !== 'ADMINISTRATOR') {
        const navUsers = document.getElementById('nav-users');
        if (navUsers) navUsers.style.display = 'none';
    }

    if (role === 'ADMINISTRATOR') {
        renderHelpView('Administrator');
    } else {
        renderHelpView('Receptionist');
    }
});

function getFaqsByRole(role) {
    const isAdmin = role === 'Administrator';
    
    const faqs = [];

    // General Basics
    faqs.push({
        topic: 'General Concept',
        questions: [
            {
                q: 'What is the Ocean View Resort Management System?',
                a: 'This system allows resort staff to easily manage rooms, guests, user accounts, and reservations. It has specialized features depending on whether you log in as an Administrator or a Receptionist.'
            },
            {
                q: 'How do I log out?',
                a: 'You can log out by clicking the "Logout" button at the bottom of the left sidebar. This will end your session securely.'
            }
        ]
    });

    // Dashboard
    if (isAdmin) {
        faqs.push({
            topic: 'Dashboard Overview',
            questions: [
                {
                    q: 'What does the Admin Dashboard show?',
                    a: 'The Admin Dashboard provides key performance indicators (KPIs) such as Total Revenue, Confirmed Bookings, and Room Occupancy. It also highlights recent reservations and visually categorizes booking statuses.'
                },
                {
                    q: 'How is Total Revenue calculated?',
                    a: 'Total Revenue is computed by summing up the costs of all reservations that are marked with a "COMPLETED" status.'
                }
            ]
        });
    } else {
        faqs.push({
            topic: 'Dashboard Overview',
            questions: [
                {
                    q: 'What does the Receptionist Dashboard show?',
                    a: 'The dashboard gives a quick view of today\'s Expected Check-ins, Check-outs, and Available Rooms. You can interactively switch between the tabs for immediate visibility into the schedule.'
                },
                {
                    q: 'How can I quickly create a reservation from the Dashboard?',
                    a: 'In the Quick Actions box near the bottom, click the "New Booking" button to swiftly navigate directly to the reservations creation form.'
                }
            ]
        });
    }

    // Bookings & Reservations
    faqs.push({
        topic: 'Reservations & Guests',
        questions: [
            {
                q: 'How do I add a new Guest?',
                a: 'Navigate to the "Guests" tab from the sidebar. You can review existing guests or click the "Add New Guest" button entirely independent of reservations.'
            },
            {
                q: 'How do I create a new Reservation?',
                a: 'Go to the "Reservations" tab and select "New Reservation". You will specify check-in and check-out dates, pick an existing guest (or optionally create a new one on the fly), and attach one or multiple available rooms.'
            },
            {
                q: 'How do I generate a Bill / Invoice?',
                a: 'In the Reservations view, find the booking you want to print, and select "Generate Bill". This typically creates a downloadable PDF with a breakdown of exact room costs, stay duration, and guests.'
            }
        ]
    });

    if (isAdmin) {
        faqs.push({
            topic: 'Reports & Exports',
            questions: [
                {
                    q: 'Can I export a list of all reservations?',
                    a: 'Yes, as an Administrator, you can view the complete reservations history and click the Export buttons (e.g., CSV or PDF) to save local offline copies of the data.'
                }
            ]
        });
    }

    // Roles and Users (Admin Only)
    if (isAdmin) {
        faqs.push({
            topic: 'System Users (Admin Only)',
            questions: [
                {
                    q: 'How do I add another Receptionist or Admin?',
                    a: 'Go to the "Users Panel" (restricted to Admins only). You can create new system accounts, set their usernames, passwords, and specify their Role.'
                },
                {
                    q: 'What permissions do Receptionists lack?',
                    a: 'Receptionists cannot access the "Users Panel" nor can they edit other staff member accounts. Similarly, they do not see high-level revenue summaries.'
                }
            ]
        });
    }

    // Rooms
    faqs.push({
        topic: 'Rooms Management',
        questions: [
            {
                q: 'How do I see if a room is available?',
                a: 'Navigate to the "Rooms" menu. The list will clearly indicate the Availability status ("Available" vs "Occupied").'
            },
            {
                q: 'How are rooms categorized?',
                a: 'Rooms are generally categorized by Types (e.g., Standard, Deluxe, Family Suite) with individual base daily rates assigned to them.'
            }
        ]
    });
    
    return faqs;
}

function renderHelpView(role) {
    const container = document.getElementById('viewContainer');
    const faqs = getFaqsByRole(role);
    
    // Generate HTML for FAQ items
    let faqHtml = '';
    
    faqs.forEach((section, sIndex) => {
        faqHtml += `
            <div class="mb-8 help-section" data-topic="${section.topic.toLowerCase()}">
                <h3 class="text-lg font-extrabold text-gray-800 mb-4 flex items-center gap-2 border-b border-gray-100 pb-2">
                    <i class="fa-solid fa-bookmark text-blue-500"></i> ${section.topic}
                </h3>
                <div class="space-y-3">
        `;
        
        section.questions.forEach((qItem, qIndex) => {
            const collapseId = `faq-${sIndex}-${qIndex}`;
            faqHtml += `
                <div class="faq-item bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm transition-all duration-200">
                    <button class="faq-toggle w-full text-left px-5 py-4 flex items-center justify-between focus:outline-none hover:bg-gray-50" aria-expanded="false" aria-controls="${collapseId}">
                        <span class="font-semibold text-gray-800 text-sm">${qItem.q}</span>
                        <i class="fa-solid fa-chevron-down text-gray-400 text-sm transition-transform duration-200 icon-arrow"></i>
                    </button>
                    <div id="${collapseId}" class="faq-content hidden px-5 py-4 border-t border-gray-100 bg-gray-50 text-sm text-gray-600 leading-relaxed">
                        ${qItem.a}
                    </div>
                </div>
            `;
        });
        
        faqHtml += `
                </div>
            </div>
        `;
    });

    container.innerHTML = `
    <style>
      @keyframes fadeUp { from { opacity:0; transform:translateY(15px); } to { opacity:1; transform:translateY(0); } }
      .fade-up-view { animation: fadeUp .4s ease both; }
      .faq-toggle[aria-expanded="true"] .icon-arrow { transform: rotate(180deg); }
      .faq-item:hover { border-color: #93c5fd; }
    </style>

    <div class="fade-up-view">
        <!-- Header -->
        <div class="relative overflow-hidden rounded-2xl mb-8 bg-gradient-to-br from-blue-700 to-indigo-800 text-white p-8 shadow-xl">
            <div class="absolute inset-0 opacity-10"
                style="background-image:radial-gradient(circle at 80% 20%, #ffffff 0%, transparent 50%)"></div>
            <div class="relative z-10 flex items-start justify-between flex-wrap gap-4">
                <div>
                    <h1 class="text-3xl font-black tracking-tight mb-2">Help Guide</h1>
                    <p class="text-blue-100 text-sm max-w-xl">
                        Welcome to the Ocean View Resort knowledge base. You are viewing documentation customized for your <span class="font-bold bg-white/20 px-2 py-0.5 rounded">${role}</span> access level.
                    </p>
                </div>
                <div class="w-12 h-12 bg-white/10 backdrop-blur-sm rounded-xl border border-white/20 flex items-center justify-center text-2xl">
                    <i class="fa-solid fa-circle-info"></i>
                </div>
            </div>
        </div>

        <!-- Search Bar -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-8 flex items-center gap-3">
            <i class="fa-solid fa-magnifying-glass text-gray-400 ml-2"></i>
            <input type="text" id="helpSearchInput" placeholder="Search for questions or keywords..." class="flex-1 bg-transparent border-none focus:ring-0 text-sm py-1 text-gray-800 outline-none w-full">
            <button id="clearSearchBtn" class="hidden text-gray-400 hover:text-gray-600 px-2">
                <i class="fa-solid fa-times"></i>
            </button>
        </div>

        <!-- FAQ Container -->
        <div id="faqList">
            ${faqHtml}
        </div>
        
        <!-- Empty State -->
        <div id="emptySearchState" class="hidden text-center py-12">
            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-400 text-2xl">
                <i class="fa-solid fa-ghost"></i>
            </div>
            <h3 class="text-lg font-bold text-gray-700">No results found</h3>
            <p class="text-sm text-gray-500 mt-1">Try adjusting your search terms.</p>
        </div>
    </div>
    `;

    // Initialize Accorgion functionality
    document.querySelectorAll('.faq-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const isExpanded = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', !isExpanded);
            const contentId = btn.getAttribute('aria-controls');
            const content = document.getElementById(contentId);
            if (content) {
                content.classList.toggle('hidden');
            }
        });
    });

    // Search functionality
    const searchInput = document.getElementById('helpSearchInput');
    const clearBtn = document.getElementById('clearSearchBtn');
    
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase().trim();
            
            if (term.length > 0) {
                clearBtn.classList.remove('hidden');
            } else {
                clearBtn.classList.add('hidden');
            }

            let anyVisible = false;

            // Iterate over each section
            document.querySelectorAll('.help-section').forEach(section => {
                let sectionHasMatch = false;
                
                // Iterate over questions in section
                section.querySelectorAll('.faq-item').forEach(item => {
                    const text = item.textContent.toLowerCase();
                    if (text.includes(term)) {
                        item.classList.remove('hidden');
                        sectionHasMatch = true;
                    } else {
                        item.classList.add('hidden');
                    }
                });

                if (sectionHasMatch) {
                    section.classList.remove('hidden');
                    anyVisible = true;
                } else {
                    section.classList.add('hidden');
                }
            });

            const emptyState = document.getElementById('emptySearchState');
            if (emptyState) {
                if (!anyVisible && term !== '') {
                    emptyState.classList.remove('hidden');
                } else {
                    emptyState.classList.add('hidden');
                }
            }
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            searchInput.value = '';
            searchInput.dispatchEvent(new Event('input'));
            searchInput.focus();
        });
    }
}
