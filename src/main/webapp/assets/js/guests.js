// guests.js — Guest Management Module
const GuestsModule = {

    _cache: new Map(), // guestId → guest object (for edit lookups)

    // ─── Init ────────────────────────────────────────────────────────────────
    init: async () => {
        const container = document.getElementById('viewContainer');
        GuestsModule.renderLayout(container);
        await GuestsModule.loadGuests();
    },

    // ─── Layout Shell ────────────────────────────────────────────────────────
    renderLayout: (container) => {
        container.innerHTML = `
            <div class="space-y-6 animate-fade-in">

                <!-- Header -->
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900">Guest Management</h2>
                        <p class="text-sm text-gray-500 mt-1">Manage guest profiles, contact info and records.</p>
                    </div>
                    <button onclick="GuestsModule.showForm()"
                        class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-all flex items-center gap-2">
                        <i class="fa-solid fa-user-plus"></i> Add New Guest
                    </button>
                </div>

                <!-- Search Bar -->
                <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-100 space-y-3">
                    <h3 class="text-sm font-semibold text-gray-700 uppercase tracking-wider">Search Guest</h3>
                    <div class="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
                        <div class="md:col-span-3">
                            <label class="block text-xs font-medium text-gray-600 mb-1">Search By</label>
                            <select id="searchType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none">
                                <option value="id">Guest ID</option>
                                <option value="phone">Phone Number</option>
                                <option value="nic">NIC</option>
                            </select>
                        </div>
                        <div class="md:col-span-5">
                            <label class="block text-xs font-medium text-gray-600 mb-1">Search Value</label>
                            <input type="text" id="searchValue" placeholder="Enter value..."
                                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none">
                        </div>
                        <div class="md:col-span-2">
                            <button onclick="GuestsModule.search()"
                                class="w-full bg-gray-800 hover:bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors">
                                <i class="fa-solid fa-search mr-1"></i> Search
                            </button>
                        </div>
                        <div class="md:col-span-2">
                            <button onclick="GuestsModule.loadGuests()"
                                class="w-full text-sm text-blue-600 hover:text-blue-800 font-medium border border-blue-200 hover:bg-blue-50 px-4 py-2 rounded-lg transition-colors">
                                <i class="fa-solid fa-rotate-right mr-1"></i> Reset
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Table -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-sm text-gray-600">
                            <thead class="text-xs text-gray-700 uppercase bg-gray-50/80 border-b border-gray-200">
                                <tr>
                                    <th class="px-6 py-4 font-semibold">ID</th>
                                    <th class="px-6 py-4 font-semibold">Name</th>
                                    <th class="px-6 py-4 font-semibold">Email</th>
                                    <th class="px-6 py-4 font-semibold">Phone</th>
                                    <th class="px-6 py-4 font-semibold">NIC</th>
                                    <th class="px-6 py-4 font-semibold">Address</th>
                                    <th class="px-6 py-4 font-semibold">Registered</th>
                                    <th class="px-6 py-4 font-semibold text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody id="guestsTableBody" class="divide-y divide-gray-100">
                                <tr><td colspan="8" class="px-6 py-8 text-center">
                                    <div class="animate-pulse flex flex-col items-center">
                                        <div class="h-8 w-8 rounded-full border-4 border-gray-200 border-t-blue-500 animate-spin mb-3"></div>
                                        <div class="text-gray-400 font-medium">Loading guests...</div>
                                    </div>
                                </td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Modal -->
            <div id="guestModal" class="hidden fixed inset-0 bg-gray-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 opacity-0 transition-opacity duration-300">
                <div id="guestModalContent" class="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden transform scale-95 transition-transform duration-300"></div>
            </div>
        `;
    },

    // ─── Data Loading ────────────────────────────────────────────────────────
    loadGuests: async () => {
        showLoader(true);
        try {
            document.getElementById('searchValue').value = '';
            // GET /api/guests/
            const guests = await fetchAPI('/guests/');
            GuestsModule.renderTable(guests);
        } catch (e) {
            GuestsModule.renderError(e.message);
        } finally {
            showLoader(false);
        }
    },

    search: async () => {
        const type  = document.getElementById('searchType').value;
        const value = document.getElementById('searchValue').value.trim();

        if (!value) { alert('Please enter a search value.'); return; }

        showLoader(true);
        try {
            // GET /api/guests/search?id=X  OR  ?nic=X  OR  ?phone=X  (exactly one)
            const guests = await fetchAPI(`/guests/search?${type}=${encodeURIComponent(value)}`);
            GuestsModule.renderTable(guests);
        } catch (e) {
            GuestsModule.renderError('No results found. ' + e.message);
        } finally {
            showLoader(false);
        }
    },

    // ─── Render ───────────────────────────────────────────────────────────────
    renderTable: (guests) => {
        const tbody = document.getElementById('guestsTableBody');
        if (!guests || guests.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="px-6 py-12 text-center text-gray-500">
                <i class="fa-solid fa-inbox text-4xl mb-3 text-gray-300 block"></i>
                No guests found.</td></tr>`;
            return;
        }

        GuestsModule._cache.clear();
        guests.forEach(g => GuestsModule._cache.set(g.guestId, g));

        tbody.innerHTML = guests.map(g => {
            const registered = g.createdAt
                ? new Date(g.createdAt).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' })
                : '—';
            return `
            <tr class="bg-white hover:bg-blue-50/30 transition-colors group">
                <td class="px-6 py-4 font-semibold text-gray-900 bg-gray-50/50 group-hover:bg-transparent">#${g.guestId}</td>
                <td class="px-6 py-4">
                    <div class="font-medium text-gray-900">${g.name || '—'}</div>
                </td>
                <td class="px-6 py-4 text-gray-500">${g.email || '—'}</td>
                <td class="px-6 py-4 font-mono text-xs text-gray-700">${g.phoneNumber || '—'}</td>
                <td class="px-6 py-4 font-mono text-xs text-gray-700">${g.nic || '—'}</td>
                <td class="px-6 py-4 text-gray-500 max-w-[160px] truncate" title="${g.address || ''}">${g.address || '—'}</td>
                <td class="px-6 py-4 text-xs text-gray-400">${registered}</td>
                <td class="px-6 py-4 text-right space-x-1 whitespace-nowrap">
                    <button onclick="GuestsModule.showForm(${g.guestId})"
                        class="p-2 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-colors" title="Edit Guest">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button onclick="GuestsModule.deleteGuest(${g.guestId}, '${(g.name || '').replace(/'/g, '')}')"
                        class="p-2 rounded-lg text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors" title="Delete Guest">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </td>
            </tr>`;
        }).join('');
    },

    renderError: (msg) => {
        const tbody = document.getElementById('guestsTableBody');
        if (tbody) tbody.innerHTML = `<tr><td colspan="8" class="px-6 py-12 text-center text-red-500 bg-red-50/50">
            <i class="fa-solid fa-triangle-exclamation text-3xl mb-3 block opacity-70"></i>
            <strong>Error:</strong> ${msg}</td></tr>`;
    },

    // ─── Modal ────────────────────────────────────────────────────────────────
    // id = null → Add mode | id = number → Edit mode (lookup from cache)
    showForm: (id = null) => {
        const guest  = id != null ? GuestsModule._cache.get(id) : null;
        const isEdit = !!guest;

        const modal        = document.getElementById('guestModal');
        const modalContent = document.getElementById('guestModalContent');

        modalContent.innerHTML = `
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80">
                <h3 class="text-lg font-bold text-gray-900">
                    <i class="fa-solid ${isEdit ? 'fa-user-pen' : 'fa-user-plus'} text-blue-600 mr-2"></i>
                    ${isEdit ? 'Edit Guest #' + id : 'Add New Guest'}
                </h3>
                <button onclick="GuestsModule.closeModal()"
                    class="text-gray-400 hover:text-gray-600 p-1 rounded-md hover:bg-gray-200 transition-colors">
                    <i class="fa-solid fa-xmark text-lg"></i>
                </button>
            </div>
            <form onsubmit="GuestsModule.saveGuest(event, ${id})" class="p-6 space-y-4">
                <div class="grid grid-cols-2 gap-4">
                    <div class="col-span-2">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Full Name <span class="text-red-500">*</span></label>
                        <input type="text" id="guestName" value="${guest ? (guest.name || '') : ''}" required
                            placeholder="e.g. John Perera"
                            class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Email <span class="text-red-500">*</span></label>
                        <input type="email" id="guestEmail" value="${guest ? (guest.email || '') : ''}" required
                            placeholder="email@example.com"
                            class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Phone Number <span class="text-red-500">*</span></label>
                        <input type="tel" id="guestPhone" value="${guest ? (guest.phoneNumber || '') : ''}" required
                            placeholder="e.g. 0789362885"
                            class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">NIC <span class="text-red-500">*</span></label>
                        <input type="text" id="guestNic" value="${guest ? (guest.nic || '') : ''}" required
                            placeholder="e.g. 200401500020"
                            class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
                        <input type="text" id="guestAddress" value="${guest ? (guest.address || '') : ''}"
                            placeholder="e.g. 12 Beach Road, Galle"
                            class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all">
                    </div>
                </div>

                <div id="guestFormError" class="hidden bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-600"></div>

                <div class="pt-4 flex gap-3 justify-end border-t border-gray-100">
                    <button type="button" onclick="GuestsModule.closeModal()"
                        class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                        Cancel
                    </button>
                    <button type="submit" id="guestSaveBtn"
                        class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center gap-2">
                        <i class="fa-solid ${isEdit ? 'fa-floppy-disk' : 'fa-user-plus'}"></i>
                        ${isEdit ? 'Save Changes' : 'Create Guest'}
                    </button>
                </div>
            </form>
        `;

        modal.classList.remove('hidden');
        setTimeout(() => {
            modal.classList.remove('opacity-0');
            modalContent.classList.remove('scale-95');
        }, 10);
    },

    closeModal: () => {
        const modal        = document.getElementById('guestModal');
        const modalContent = document.getElementById('guestModalContent');
        modal.classList.add('opacity-0');
        if (modalContent) modalContent.classList.add('scale-95');
        setTimeout(() => modal.classList.add('hidden'), 300);
    },

    // ─── CRUD Actions ─────────────────────────────────────────────────────────

    // POST /api/guests/  OR  PUT /api/guests/{id}
    // Both use CreateGuestDTO: { name, email, phoneNumber, address, nic }
    saveGuest: async (e, id) => {
        e.preventDefault();

        const payload = {
            name:        document.getElementById('guestName').value.trim(),
            email:       document.getElementById('guestEmail').value.trim(),
            phoneNumber: document.getElementById('guestPhone').value.trim(),
            nic:         document.getElementById('guestNic').value.trim(),
            address:     document.getElementById('guestAddress').value.trim()
        };

        const errEl = document.getElementById('guestFormError');
        const btn   = document.getElementById('guestSaveBtn');
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';
        errEl.classList.add('hidden');

        GuestsModule.closeModal();
        showLoader(true);

        try {
            if (id != null) {
                // PUT /api/guests/{id}
                await fetchAPI(`/guests/${id}`, 'PUT', payload);
            } else {
                // POST /api/guests/
                await fetchAPI('/guests/', 'POST', payload);
            }
            await GuestsModule.loadGuests();
        } catch (error) {
            showLoader(false);
            alert('Failed to save guest: ' + (error.message || 'Unknown error'));
        }
    },

    // DELETE /api/guests/{id}
    deleteGuest: async (id, name) => {
        if (!confirm(`Delete guest "${name || '#' + id}"? This cannot be undone.`)) return;
        showLoader(true);
        try {
            await fetchAPI(`/guests/${id}`, 'DELETE');
            await GuestsModule.loadGuests();
        } catch (e) {
            alert('Delete failed: ' + e.message);
            showLoader(false);
        }
    }
};
