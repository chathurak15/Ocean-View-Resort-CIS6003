const API_BASE_URL = (window.APP_CONTEXT || '') + '/api';

/**
 * Perform an API call
 * @param {string} endpoint - API end point (e.g. '/users/login')
 * @param {string} method - HTTP Method (GET, POST, PUT, DELETE, PATCH)
 * @param {object} body - Optional body object for POST/PUT/PATCH
 */
async function fetchAPI(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const options = { method, headers };

    if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            let errorMessage = 'Network response was not ok';
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorMessage;
            } catch (e) {}
            throw new Error(errorMessage);
        }
        if (response.status === 204) return null;
        return await response.json();
    } catch (error) {
        throw error;
    }
}
