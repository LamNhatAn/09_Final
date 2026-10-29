(() => {
    "use strict";

    const root = document.getElementById("notification-center");
    if (!root) return;

    const contextPath = root.dataset.contextPath || "";
    const countBadge = document.getElementById("notification-count");
    const list = document.getElementById("notification-list");
    const readAll = document.getElementById("notification-read-all");
    const toastContainer = document.getElementById("notification-toasts");
    let reconnectTimer;

    function csrfToken() {
        const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
        return match ? decodeURIComponent(match[1]) : "";
    }

    async function api(path, options = {}) {
        const headers = new Headers(options.headers || {});
        if ((options.method || "GET") !== "GET") {
            headers.set("X-XSRF-TOKEN", csrfToken());
        }
        const response = await fetch(contextPath + path, {
            credentials: "same-origin",
            ...options,
            headers
        });
        if (!response.ok) throw new Error("Notification request failed: " + response.status);
        return response.status === 204 ? null : response.json();
    }

    function formatTime(value) {
        if (!value) return "";
        return new Intl.DateTimeFormat("vi-VN", {
            day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit"
        }).format(new Date(value));
    }

    function render(feed) {
        const unread = Number(feed.unreadCount || 0);
        countBadge.textContent = unread > 99 ? "99+" : String(unread);
        countBadge.classList.toggle("d-none", unread === 0);
        list.replaceChildren();

        if (!feed.items || feed.items.length === 0) {
            const empty = document.createElement("div");
            empty.className = "text-center text-muted small py-4";
            empty.textContent = "Bạn chưa có thông báo nào.";
            list.appendChild(empty);
            return;
        }

        feed.items.forEach(item => {
            const link = document.createElement("a");
            link.href = contextPath + item.link;
            link.className = "notification-item d-block px-3 py-2 text-decoration-none border-bottom" +
                    (item.unread ? " is-unread" : "");

            const title = document.createElement("div");
            title.className = "fw-semibold text-body";
            title.textContent = item.title;
            const message = document.createElement("div");
            message.className = "small text-body-secondary";
            message.textContent = item.message;
            const time = document.createElement("time");
            time.className = "d-block small text-muted mt-1";
            time.textContent = formatTime(item.createdAt);
            link.append(title, message, time);

            if (item.unread) {
                link.addEventListener("click", async event => {
                    event.preventDefault();
                    try {
                        await api(`/api/notifications/${item.id}/read`, {method: "POST"});
                    } finally {
                        window.location.href = link.href;
                    }
                });
            }
            list.appendChild(link);
        });
    }

    async function refresh() {
        try {
            render(await api("/api/notifications"));
        } catch (error) {
            list.innerHTML = '<div class="text-center text-danger small py-4">Không tải được thông báo.</div>';
        }
    }

    function showToast(item) {
        if (!item || !window.bootstrap) return;
        const toast = document.createElement("div");
        toast.className = "toast";
        toast.setAttribute("role", "status");
        const body = document.createElement("div");
        body.className = "toast-body d-flex gap-2 align-items-start";
        const text = document.createElement("div");
        const strong = document.createElement("strong");
        strong.className = "d-block";
        strong.textContent = item.title;
        const message = document.createElement("span");
        message.textContent = item.message;
        text.append(strong, message);
        const close = document.createElement("button");
        close.type = "button";
        close.className = "btn-close ms-auto";
        close.dataset.bsDismiss = "toast";
        close.setAttribute("aria-label", "Đóng");
        body.append(text, close);
        toast.appendChild(body);
        toastContainer.appendChild(toast);
        toast.addEventListener("hidden.bs.toast", () => toast.remove());
        new bootstrap.Toast(toast, {delay: 6000}).show();
    }

    function parseFrame(frame) {
        const clean = frame.replace(/^\n+/, "");
        const separator = clean.indexOf("\n\n");
        const headerPart = separator >= 0 ? clean.slice(0, separator) : clean;
        const body = separator >= 0 ? clean.slice(separator + 2) : "";
        const lines = headerPart.split("\n");
        const command = lines.shift();
        const headers = {};
        lines.forEach(line => {
            const colon = line.indexOf(":");
            if (colon > 0) headers[line.slice(0, colon)] = line.slice(colon + 1);
        });
        return {command, headers, body};
    }

    function connect() {
        clearTimeout(reconnectTimer);
        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const socket = new WebSocket(protocol + "//" + window.location.host + contextPath + "/ws",
                ["v12.stomp", "v11.stomp"]);

        socket.addEventListener("open", () => {
            socket.send("CONNECT\naccept-version:1.2\nheart-beat:0,0\nhost:" + window.location.host + "\n\n\0");
        });
        socket.addEventListener("message", event => {
            String(event.data).split("\0").filter(Boolean).forEach(raw => {
                const frame = parseFrame(raw);
                if (frame.command === "CONNECTED") {
                    socket.send("SUBSCRIBE\nid:notifications\ndestination:/user/queue/notifications\nack:auto\n\n\0");
                    refresh();
                } else if (frame.command === "MESSAGE") {
                    try { showToast(JSON.parse(frame.body)); } catch (ignored) { }
                    refresh();
                }
            });
        });
        socket.addEventListener("close", () => {
            reconnectTimer = setTimeout(connect, 5000);
        });
        socket.addEventListener("error", () => socket.close());
    }

    readAll.addEventListener("click", async () => {
        try {
            await api("/api/notifications/read-all", {method: "POST"});
            await refresh();
        } catch (ignored) { }
    });

    refresh();
    connect();
})();
