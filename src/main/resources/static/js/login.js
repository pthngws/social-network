$(document).ready(function () {
    // ... (các hàm khác như isTokenValid giữ nguyên)

    // Xử lý nút đăng nhập Google
    $("#googleLoginBtn").click(function () {
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    });

    // Hàm lấy dữ liệu từ server sau OAuth2
    async function fetchOAuthToken() {
        try {
            const response = await fetch("http://localhost:8080/auth/oauth2-login", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include" // Gửi cookie nếu cần
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            console.log("Phản hồi từ server:", data);

            if (data.status === 200 && data.data && data.data.token && data.data.email) {
                localStorage.setItem("token", data.data.token);
                localStorage.setItem("userId", data.data.id);
                localStorage.setItem("email", data.data.email);
                console.log("Token đã lưu:", localStorage.getItem("token"));
                window.location.href = "/home";
            } else {
                $("#login-error-message").text(data.message || "Đăng nhập OAuth thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#login-error-message").text("Lỗi trong quá trình đăng nhập OAuth.");
        }
    }

    // Xử lý callback OAuth2
    function handleOAuthCallback() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get("error");
        const oauth2Success = urlParams.get("oauth2");

        if (error) {
            $("#login-error-message").text("Đăng nhập OAuth thất bại.");
            return;
        }

        // Nếu redirect về /login với oauth2=success, gọi API
        if (window.location.pathname === "/login" && oauth2Success === "success") {
            fetchOAuthToken();
        }
    }

    // Gọi xử lý callback khi trang tải
    handleOAuthCallback();
    // Xử lý form đăng nhập thủ công với async/await
    $("#loginForm").submit(async function (event) {
        event.preventDefault();

        const email = $("#loginEmail").val().trim();
        const password = $("#loginPassword").val().trim();

        if (!email || !password) {
            $("#login-error-message").text("Vui lòng nhập email và mật khẩu.");
            return;
        }

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            console.log("Phản hồi đăng nhập thủ công:", data);

            if (data.status === 200 && data.data && data.data.token && data.data.email) {
                localStorage.setItem("token", data.data.token);
                localStorage.setItem("userId", data.data.id);
                localStorage.setItem("email", data.data.email);
                window.location.href = "/home";
            } else {
                $("#login-error-message").text(data.message || "Đăng nhập thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#login-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Hàm kiểm tra email hợp lệ (nếu cần cho đăng ký)
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    // Xử lý form đăng ký với async/await
    $("#signupForm").submit(async function (event) {
        event.preventDefault();

        const email = $("#signupEmail").val().trim();
        const password = $("#signupPassword").val();
        const confirmPassword = $("#signupConfirmPassword").val();

        if (!isValidEmail(email)) {
            $("#signup-error-message").text("Định dạng email không hợp lệ.");
            return;
        }
        if (password.length < 6) {
            $("#signup-error-message").text("Mật khẩu phải dài ít nhất 6 ký tự.");
            return;
        }
        if (password !== confirmPassword) {
            $("#signup-error-message").text("Mật khẩu không khớp.");
            return;
        }

        try {
            const response = await fetch("/auth/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            if (data.status === 200) {
                $("#signup-error-message").text("Đăng ký thành công! Vui lòng đăng nhập.");
            } else {
                $("#signup-error-message").text(data.message || "Đăng ký thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#signup-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });
});