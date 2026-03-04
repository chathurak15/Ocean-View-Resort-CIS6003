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
