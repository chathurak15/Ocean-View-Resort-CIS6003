const API_BASE_URL = (window.APP_CONTEXT || '') + '/api';

async function fetchAPI(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const options = { method, headers };

    if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (!response.ok) {
        let errorMessage = `Server error (${response.status})`;
        try {
            const errorData = await response.json();
            if (errorData.error) {
                errorMessage = errorData.error;
            } else if (errorData.errors && typeof errorData.errors === 'object') {
                const msgs = Object.values(errorData.errors);
                errorMessage = msgs.length > 0 ? msgs.join(' | ') : 'Validation failed.';
            } else if (errorData.message) {
                errorMessage = errorData.message;
            }
        } catch (e) { /* body not JSON */ }
        throw new Error(errorMessage);
    }

    if (response.status === 204) return null;
    return await response.json();
}

// Global Toast Utility
window.showToast = function(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'fixed bottom-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    const isSuccess = type === 'success';
    
    const icon = isSuccess ? '<i class="fa-solid fa-circle-check text-emerald-500"></i>' 
                           : '<i class="fa-solid fa-circle-xmark text-red-500"></i>';
    const bgColor = isSuccess ? 'bg-white' : 'bg-red-50'; // White background for pleasant success, red for error
    const borderColor = isSuccess ? 'border-emerald-500' : 'border-red-200';
    const textColor = isSuccess ? 'text-gray-800' : 'text-red-800';

    toast.className = `transform translate-y-8 opacity-0 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-lg shadow-[0_8px_30px_rgb(0,0,0,0.12)] border-l-4 ${bgColor} ${borderColor} ${textColor} min-w-[300px] max-w-md pointer-events-auto`;
    
    toast.innerHTML = `
        <div class="text-xl">${icon}</div>
        <div class="flex-1 font-medium text-sm leading-tight">${message}</div>
        <button onclick="this.parentElement.remove()" class="text-gray-400 hover:text-gray-600 transition-colors ml-2">
            <i class="fa-solid fa-xmark"></i>
        </button>
    `;

    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-8', 'opacity-0');
    });

    setTimeout(() => {
        if (!toast.parentElement) return;
        toast.classList.add('opacity-0', 'translate-y-8');
        setTimeout(() => toast.remove(), 300);
    }, 4000); // Wait 4 seconds
};
