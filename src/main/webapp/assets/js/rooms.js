const RoomsModule = {
    _cache: new Map(), // stores room objects by id
    init: async () => {
        const container = document.getElementById('viewContainer');
        const curUserStr = sessionStorage.getItem('currentUser');

        const curUser = curUserStr ? JSON.parse(curUserStr) : { userType: 'RECEPTIONIST' };
        RoomsModule.isReception = (curUser.userType || '').toUpperCase() === 'RECEPTIONIST';

        RoomsModule.renderLayout(container);
        await RoomsModule.loadRooms();
    },

    renderLayout: (container) => {
        const adminActions = !RoomsModule.isReception ? `
            <button onclick="RoomsModule.showForm()" class="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-all focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 flex items-center gap-2">
                <i class="fa-solid fa-plus"></i> Add New Room
            </button>
        ` : '';

        container.innerHTML = `
            <div class="space-y-6 animate-fade-in">
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900">Room Management</h2>
                        <p class="text-sm text-gray-500 mt-1">Manage resort rooms, availability, and pricing.</p>
                    </div>
                    ${adminActions}
                </div>

                <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 space-y-4">
                    <h3 class="text-sm font-semibold text-gray-700 uppercase tracking-wider mb-2">Search & Filter</h3>
                    <div class="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
                        
                        <div class="md:col-span-3">
                            <label class="block text-xs font-medium text-gray-600 mb-1">Room ID</label>
                            <div class="relative">
                                <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                                    <i class="fa-solid fa-hashtag text-sm"></i>
                                </span>
                                <input type="number" id="searchRoomId" placeholder="e.g. 1" class="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-shadow">
                            </div>
                        </div>
                        <div class="md:col-span-1">
                            <button onclick="RoomsModule.searchById()" class="w-full bg-gray-800 hover:bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors shadow-sm">
                                Search
                            </button>
                        </div>

                        <div class="md:col-span-1 hidden md:flex justify-center">
                            <span class="text-gray-300 font-light text-2xl">|</span>
                        </div>

                        <div class="md:col-span-3">
                            <label class="block text-xs font-medium text-gray-600 mb-1">Check In</label>
                            <input type="date" id="filterCheckIn" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-shadow">
                        </div>
                        <div class="md:col-span-3">
                            <label class="block text-xs font-medium text-gray-600 mb-1">Check Out</label>
                            <input type="date" id="filterCheckOut" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-shadow">
                        </div>
                        <div class="md:col-span-1">
                            <button onclick="RoomsModule.checkAvailability()" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors shadow-sm">
                                Check
                            </button>
                        </div>
                    </div>
                    <div class="pt-2 flex justify-between items-center">
                        <span class="text-xs text-gray-500">Tip: Receptionists can only view rooms and availability.</span>
                        <button onclick="RoomsModule.loadRooms()" class="text-sm text-blue-600 hover:text-blue-800 font-medium transition-colors">
                            <i class="fa-solid fa-rotate-right mr-1"></i> Reset All Filters
                        </button>
                    </div>
                </div>

                <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-sm text-gray-600">
                            <thead class="text-xs text-gray-700 uppercase bg-gray-50/80 border-b border-gray-200">
                                <tr>
                                    <th class="px-6 py-4 font-semibold">ID</th>
                                    <th class="px-6 py-4 font-semibold">Name / Type</th>
                                    <th class="px-6 py-4 font-semibold">Description</th>
                                    <th class="px-6 py-4 font-semibold">Price / Night</th>
                                    <th class="px-6 py-4 font-semibold text-center">Status</th>
                                    ${!RoomsModule.isReception ? '<th class="px-6 py-4 font-semibold text-right">Actions</th>' : ''}
                                </tr>
                            </thead>
                            <tbody id="roomsTableBody" class="divide-y divide-gray-100">
                                <tr><td colspan="6" class="px-6 py-8 text-center"><div class="animate-pulse flex flex-col items-center"><div class="h-8 w-8 rounded-full border-4 border-gray-200 border-t-blue-500 animate-spin mb-3"></div><div class="text-gray-400 font-medium">Loading rooms...</div></div></td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div id="roomModal" class="hidden fixed inset-0 bg-gray-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 opacity-0 transition-opacity duration-300">
                <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden transform scale-95 transition-transform duration-300" id="roomModalContent">
                    </div>
            </div>
        `;
    },

    loadRooms: async () => {
        showLoader(true);
        try {
            document.getElementById('searchRoomId').value = '';
            document.getElementById('filterCheckIn').value = '';
            document.getElementById('filterCheckOut').value = '';

            // GET /api/rooms/
            const rooms = await fetchAPI('/rooms/');
            RoomsModule.renderTable(rooms);
        } catch (error) {
            RoomsModule.renderError(error.message);
        } finally {
            showLoader(false);
        }
    },

    searchById: async () => {
        const id = document.getElementById('searchRoomId').value;
        if (!id) return;
        showLoader(true);
        try {
            // GET /api/rooms/{id}
            const room = await fetchAPI(`/rooms/${id}`);
            RoomsModule.renderTable([room]); // Render as an array of 1
        } catch (error) {
            RoomsModule.renderError('Room not found. ' + error.message);
        } finally {
            showLoader(false);
        }
    },

    checkAvailability: async () => {
        const checkIn = document.getElementById('filterCheckIn').value;
        const checkOut = document.getElementById('filterCheckOut').value;

        if (!checkIn || !checkOut) {
            alert('Please select both Check In and Check Out dates');
            return;
        }

        showLoader(true);
        try {
            // GET /api/rooms/available?checkIn=...&checkOut=...
            const rooms = await fetchAPI(`/rooms/available?checkIn=${checkIn}&checkOut=${checkOut}`);
            RoomsModule.renderTable(rooms);
        } catch (error) {
            RoomsModule.renderError(error.message);
        } finally {
            showLoader(false);
        }
    },

    renderTable: (rooms) => {
        const tbody = document.getElementById('roomsTableBody');
        if (!rooms || rooms.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${RoomsModule.isReception ? 5 : 6}" class="px-6 py-12 text-center text-gray-500"><i class="fa-solid fa-inbox text-4xl mb-3 text-gray-300 block"></i> No rooms found matching the criteria.</td></tr>`;
            return;
        }

        // Cache room objects so showForm can look them up safely by id
        RoomsModule._cache.clear();
        rooms.forEach(r => RoomsModule._cache.set(r.id || r.roomId, r));

        tbody.innerHTML = rooms.map(r => {
            const rid = r.id || r.roomId || '?';
            return `
            <tr class="bg-white hover:bg-blue-50/30 transition-colors group">
                <td class="px-6 py-4 font-semibold text-gray-900 bg-gray-50/50 group-hover:bg-transparent">#${rid}</td>
                <td class="px-6 py-4">
                    <div class="font-medium text-gray-900">${r.roomName || r.roomNumber || 'Unnamed Room'}</div>
                    <div class="text-xs text-gray-500 mt-1 uppercase tracking-wide font-semibold">${r.roomType || 'Standard'}</div>
                </td>
                <td class="px-6 py-4 text-gray-500 max-w-xs truncate" title="${r.roomDescription || ''}">
                    ${r.roomDescription || '<span class="italic text-gray-400">No description</span>'}
                </td>
                <td class="px-6 py-4 font-medium text-emerald-600">
                    $${parseFloat(r.roomPrice || r.rate || 0).toFixed(2)}
                </td>
                <td class="px-6 py-4 text-center">
                    <span class="px-3 py-1 text-xs font-semibold rounded-full ${r.available !== false && r.status !== 'UNAVAILABLE' ? 'bg-emerald-100 text-emerald-700 border border-emerald-200' : 'bg-rose-100 text-rose-700 border border-rose-200'}">
                        ${r.available !== false && r.status !== 'UNAVAILABLE' ? '<i class="fa-solid fa-check-circle mr-1"></i> Available' : '<i class="fa-solid fa-xmark-circle mr-1"></i> Unavailable'}
                    </span>
                </td>
                ${!RoomsModule.isReception ? `
                <td class="px-6 py-4 text-right space-x-2 whitespace-nowrap">
                    <button onclick="RoomsModule.toggleStatus(${rid}, ${r.available !== false})" class="p-2 rounded-lg text-gray-500 hover:text-amber-600 hover:bg-amber-50 transition-colors tooltip-btn" title="Toggle Status">
                        <i class="fa-solid fa-power-off"></i>
                    </button>
                    <button onclick="RoomsModule.showForm(${rid})" class="p-2 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-colors tooltip-btn" title="Edit Room">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button onclick="RoomsModule.deleteRoom(${rid})" class="p-2 rounded-lg text-gray-500 hover:text-red-600 hover:bg-red-50 transition-colors tooltip-btn" title="Delete Room">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </td>
                ` : ''}
            </tr>`;
        }).join('');
    },

    renderError: (msg) => {
        const tbody = document.getElementById('roomsTableBody');
        tbody.innerHTML = `<tr><td colspan="${RoomsModule.isReception ? 5 : 6}" class="px-6 py-12 text-center text-red-500 bg-red-50/50"><i class="fa-solid fa-triangle-exclamation text-3xl mb-3 block opacity-70"></i> <strong>Error:</strong> ${msg}</td></tr>`;
    },

    // Modal & Form Logic (Admin Only)
    // id: numeric room id to edit, or null/undefined to add a new room
    showForm: (id = null) => {
        if (RoomsModule.isReception) return;

        const room = (id != null) ? RoomsModule._cache.get(id) : null;
        const isEdit = !!room;
        const roomId = room ? (room.id || room.roomId) : null;

        const modal = document.getElementById('roomModal');
        const modalContent = document.getElementById('roomModalContent');

        modalContent.innerHTML = `
            <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80">
                <h3 class="text-lg font-bold text-gray-900">${isEdit ? 'Edit Room #'+roomId : 'Add New Room'}</h3>
                <button onclick="RoomsModule.closeModal()" class="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-md hover:bg-gray-200">
                    <i class="fa-solid fa-xmark text-lg"></i>
                </button>
            </div>
            <form onsubmit="RoomsModule.saveRoom(event, ${roomId})" class="p-6 space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Room Name / Number</label>
                    <input type="text" id="roomName" value="${room ? (room.roomName || room.roomNumber || '') : ''}" required class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                    <textarea id="roomDesc" rows="2" class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none resize-none">${room ? (room.roomDescription||'') : ''}</textarea>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Price per Night ($)</label>
                        <input type="number" step="0.01" min="0" id="roomPrice" value="${room ? (room.roomPrice || room.rate || '') : ''}" required class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Room Type</label>
                        <select id="roomType" class="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all outline-none">
                            <option value="STANDARD" ${room && room.roomType === 'STANDARD' ? 'selected' : ''}>Standard</option>
                            <option value="DELUXE" ${room && room.roomType === 'DELUXE' ? 'selected' : ''}>Deluxe</option>
                            <option value="FAMILY_SUITE" ${room && room.roomType === 'FAMILY_SUITE' ? 'selected' : ''}>Family Suite</option>
                        </select>
                    </div>
                </div>
                ${!isEdit ? `
                <div class="pt-2">
                    <label class="flex items-center gap-2 cursor-pointer">
                        <div class="relative flex items-center">
                            <input type="checkbox" id="roomAvailable" class="peer sr-only" checked>
                            <div class="w-10 h-5 bg-gray-200 rounded-full peer peer-checked:bg-emerald-500 transition-colors after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:after:translate-x-5 peer-checked:after:border-white"></div>
                        </div>
                        <span class="text-sm font-medium text-gray-700">Currently Available</span>
                    </label>
                </div>
                ` : ''}
                
                <div class="pt-6 flex gap-3 justify-end items-center border-t border-gray-100 mt-6">
                    <button type="button" onclick="RoomsModule.closeModal()" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                        Cancel
                    </button>
                    <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center gap-2">
                        <i class="fa-solid ${isEdit ? 'fa-floppy-disk' : 'fa-plus'}"></i> ${isEdit ? 'Save Changes' : 'Create Room'}
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
        const modal = document.getElementById('roomModal');
        const modalContent = document.getElementById('roomModalContent');

        modal.classList.add('opacity-0');
        modalContent.classList.add('scale-95');

        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300);
    },

    saveRoom: async (e, id) => {
        e.preventDefault();

        const payload = {
            roomName: document.getElementById('roomName').value.trim(),
            roomDescription: document.getElementById('roomDesc').value.trim(),
            roomPrice: parseFloat(document.getElementById('roomPrice').value),
            roomType: document.getElementById('roomType').value
        };

        // NOTE: do NOT include 'id' in the body for PUT requests.
        // UpdateRoomDTO has no 'id' field — Jackson will throw UnrecognizedPropertyException.
        // The room ID is already in the URL: PUT /api/rooms/{id}
        if (!id) {
            payload.available = document.getElementById('roomAvailable').checked;
        }

        RoomsModule.closeModal();
        showLoader(true);

        try {
            if (id) {
                // PUT /api/rooms/{id}
                await fetchAPI(`/rooms/${id}`, 'PUT', payload);
            } else {
                // POST /api/rooms/
                await fetchAPI('/rooms/', 'POST', payload);
            }
            await RoomsModule.loadRooms();
        } catch (error) {
            alert('Failed to save room: ' + (error.message || JSON.stringify(error)));
            showLoader(false);
        }
    },

    deleteRoom: async (id) => {
        if(!confirm('Are you certain you want to delete Room #' + id + '? This action cannot be undone.')) return;
        showLoader(true);
        try {
            // DELETE /api/rooms/{id}
            await fetchAPI(`/rooms/${id}`, 'DELETE');
            await RoomsModule.loadRooms();
        } catch (e) {
            alert('Delete failed: ' + e.message);
            showLoader(false);
        }
    },

    toggleStatus: async (id, currentAvailable) => {
        if(!confirm(`Change room status to ${currentAvailable ? 'Unavailable' : 'Available'}?`)) return;
        showLoader(true);
        try {
            // PATCH /api/rooms/{id}/status
            await fetchAPI(`/rooms/${id}/status`, 'PATCH', { available: !currentAvailable });
            await RoomsModule.loadRooms();
        } catch (e) {
            alert('Status update failed: ' + e.message);
            showLoader(false);
        }
    }
};

// Page initialization
document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('viewContainer');
    if (container && window.location.pathname.includes('rooms.jsp')) {
        RoomsModule.init();
    }
});