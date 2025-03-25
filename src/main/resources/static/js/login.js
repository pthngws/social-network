$(document).ready(function () {
    // Khởi tạo các modal
    const verifyOtpModal = new bootstrap.Modal(document.getElementById("verifyOtpModal"), {});
    const forgotPasswordModal = new bootstrap.Modal(document.getElementById("forgotPasswordModal"), {});
    const resetPasswordModal = new bootstrap.Modal(document.getElementById("resetPasswordModal"), {});

    // Xử lý nút đăng nhập Google
    $("#googleLoginBtn").click(function () {
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    });

    // Xử lý callback OAuth2
    function handleOAuthCallback() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get("error");
        const oauth2Success = urlParams.get("oauth2");

        if (error) {
            $("#login-error-message").text("Đăng nhập OAuth thất bại.");
            return;
        }

        if (window.location.pathname === "/login" && oauth2Success === "success") {
            fetchOAuthToken();
        }
    }

    async function fetchOAuthToken() {
        try {
            const response = await fetch("/auth/oauth2-login", {
                method: "GET",
                headers: { "Content-Type": "application/json" },
                credentials: "include" // Gửi cookie
            });
            const data = await response.json();
            if (data.status === 200 && data.data && data.data.token) {
                localStorage.setItem("token", data.data.token);
                localStorage.setItem("email", data.data.email);
                localStorage.setItem("userId",data.data.id);
                window.location.href = "/home";
            } else {
                $("#login-error-message").text(data.message || "Đăng nhập OAuth thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#login-error-message").text("Lỗi trong quá trình đăng nhập OAuth.");
        }
    }

    // Xử lý form đăng nhập
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
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
                credentials: "include" // Gửi cookie
            });
            const data = await response.json();
            if (data.status === 200 && data.data && data.data.token) {
                localStorage.setItem("token", data.data.token);
                localStorage.setItem("email", data.data.email);
                localStorage.setItem("userId",data.data.id)
                window.location.href = "/home";
            } else {
                $("#login-error-message").text(data.message || "Đăng nhập thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#login-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý form đăng ký
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
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });
            const data = await response.json();
            if (data.status === 200) {
                $("#otpEmail").val(email);
                $("#otp-error-message").removeClass("text-danger").addClass("text-success").text("Đăng ký thành công! Vui lòng kiểm tra email để lấy OTP.");
                verifyOtpModal.show();
            } else {
                $("#signup-error-message").text(data.message || "Đăng ký thất bại.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#signup-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý form verify OTP (cho signup)
    $("#verifyOtpForm").submit(async function (event) {
        event.preventDefault();
        const email = $("#otpEmail").val().trim();
        const otp = $("#otpCode").val().trim();

        if (!otp) {
            $("#otp-error-message").text("Vui lòng nhập mã OTP.");
            return;
        }

        try {
            const response = await fetch(`/auth/verify-otp?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            const data = await response.json();
            if (data.status === 200) {
                $("#otp-error-message").removeClass("text-danger").addClass("text-success").text("Xác thực thành công! Đang chuyển hướng...");
                setTimeout(() => {
                    verifyOtpModal.hide();
                    $("#login-tab").tab("show");
                    $("#otp-error-message").text("");
                }, 2000);
            } else {
                $("#otp-error-message").text(data.message || "Mã OTP không hợp lệ.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#otp-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý gửi lại OTP (cho signup)
    $("#resendOtp").click(async function (e) {
        e.preventDefault();
        const email = $("#otpEmail").val().trim();
        try {
            const response = await fetch("/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password: "dummy" })
            });
            const data = await response.json();
            if (data.status === 200) {
                $("#otp-error-message").removeClass("text-danger").addClass("text-success").text("OTP đã được gửi lại!");
            } else {
                $("#otp-error-message").text(data.message || "Không thể gửi lại OTP.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#otp-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý nút "Forgot Password"
    $("#forgotPasswordLink").click(function (e) {
        e.preventDefault();
        forgotPasswordModal.show();
    });

    // Xử lý form quên mật khẩu
    $("#forgotPasswordForm").submit(async function (event) {
        event.preventDefault();
        const email = $("#forgotEmail").val().trim();

        if (!isValidEmail(email)) {
            $("#forgot-error-message").text("Định dạng email không hợp lệ.");
            return;
        }

        try {
            const response = await fetch(`/auth/forgot-password?email=${encodeURIComponent(email)}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            const data = await response.json();
            if (data.status === 200) {
                $("#resetEmail").val(email);
                $("#forgot-error-message").removeClass("text-danger").addClass("text-success").text("OTP đã được gửi đến email của bạn!");
                setTimeout(() => {
                    forgotPasswordModal.hide();
                    resetPasswordModal.show();
                    $("#forgot-error-message").text("");
                }, 2000);
            } else {
                $("#forgot-error-message").text(data.message || "Không thể gửi OTP.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#forgot-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý form đặt lại mật khẩu
    $("#resetPasswordForm").submit(async function (event) {
        event.preventDefault();
        const email = $("#resetEmail").val().trim();
        const otp = $("#resetOtp").val().trim();
        const newPassword = $("#newPassword").val();
        const confirmNewPassword = $("#confirmNewPassword").val();

        if (!otp) {
            $("#reset-error-message").text("Vui lòng nhập mã OTP.");
            return;
        }
        if (newPassword.length < 6) {
            $("#reset-error-message").text("Mật khẩu mới phải dài ít nhất 6 ký tự.");
            return;
        }
        if (newPassword !== confirmNewPassword) {
            $("#reset-error-message").text("Mật khẩu không khớp.");
            return;
        }

        try {
            const verifyResponse = await fetch(`/auth/verify-reset-otp?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            const verifyData = await verifyResponse.json();
            if (verifyData.status === 200) {
                const resetResponse = await fetch(`/auth/reset-password?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}&newPassword=${encodeURIComponent(newPassword)}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" }
                });
                const resetData = await resetResponse.json();
                if (resetData.status === 200) {
                    $("#reset-error-message").removeClass("text-danger").addClass("text-success").text("Đặt lại mật khẩu thành công! Đang chuyển hướng...");
                    setTimeout(() => {
                        resetPasswordModal.hide();
                        $("#login-tab").tab("show");
                        $("#reset-error-message").text("");
                    }, 2000);
                } else {
                    $("#reset-error-message").text(resetData.message || "Không thể đặt lại mật khẩu.");
                }
            } else {
                $("#reset-error-message").text(verifyData.message || "Mã OTP không hợp lệ.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#reset-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý gửi lại OTP (cho reset password)
    $("#resendResetOtp").click(async function (e) {
        e.preventDefault();
        const email = $("#resetEmail").val().trim();
        try {
            const response = await fetch(`/auth/forgot-password?email=${encodeURIComponent(email)}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            const data = await response.json();
            if (data.status === 200) {
                $("#reset-error-message").removeClass("text-danger").addClass("text-success").text("OTP đã được gửi lại!");
            } else {
                $("#reset-error-message").text(data.message || "Không thể gửi lại OTP.");
            }
        } catch (error) {
            console.error("Lỗi fetch:", error);
            $("#reset-error-message").text("Đã xảy ra lỗi, vui lòng thử lại.");
        }
    });

    // Xử lý refresh token
    async function refreshToken() {
        try {
            const response = await fetch("/auth/refresh-token", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include" // Gửi cookie
            });
            const data = await response.json();
            if (data.status === 200 && data.data) {
                localStorage.setItem("token", data.data);
                return data.data;
            } else {
                localStorage.clear();
                window.location.href = "/login";
                return null;
            }
        } catch (error) {
            console.error("Lỗi refresh token:", error);
            localStorage.clear();
            window.location.href = "/login";
            return null;
        }
    }

    // Xử lý gọi API bảo mật
    async function callSecureApi(endpoint, method = "GET", body = null) {
        let token = localStorage.getItem("token");
        try {
            const headers = {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            };
            const config = {
                method,
                headers,
                credentials: "include" // Gửi cookie
            };
            if (body) config.body = JSON.stringify(body);

            const response = await fetch(endpoint, config);
            if (response.status === 401) { // Token hết hạn
                token = await refreshToken();
                if (token) {
                    config.headers["Authorization"] = `Bearer ${token}`;
                    const retryResponse = await fetch(endpoint, config);
                    return await retryResponse.json();
                }
                throw new Error("Unable to refresh token");
            }
            return await response.json();
        } catch (error) {
            console.error("Lỗi gọi API:", error);
            throw error;
        }
    }


    // Hàm kiểm tra email hợp lệ
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    // Gọi xử lý callback khi trang tải
    handleOAuthCallback();
});