<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="h-16 bg-white border-b border-gray-200 shadow-sm flex items-center justify-between px-8">
    <h2 class="text-xl font-semibold text-gray-800" id="pageTitle"><%= request.getParameter("title") != null ? request.getParameter("title") : "Dashboard Overview" %></h2>
    <div class="flex items-center gap-4">
        <span class="text-sm text-gray-500 bg-gray-100 px-3 py-1 rounded-full"><i class="fa-regular fa-clock mr-2"></i><span id="currentTime"></span></span>
    </div>
</header>
<script>
    // Time update loop
    setInterval(() => {
        const timeEl = document.getElementById('currentTime');
        if(timeEl) {
            timeEl.textContent = new Date().toLocaleTimeString();
        }
    }, 1000);
</script>
