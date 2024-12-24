$(document).ready(function () {
    const token = localStorage.getItem("token");
    function isTokenValid(token) {
        try {
            // Tách phần payload (Base64) từ token
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000); // Thời gian hiện tại (giây)
            return payload.exp && payload.exp > currentTime; // Kiểm tra thời gian hết hạn
        } catch (error) {
            console.error("Token không hợp lệ:", error);
            return false;
        }
    }


    if (token && isTokenValid(token)) {
        // Nếu token hợp lệ, chuyển hướng
        window.location.href = "http://localhost:8080/home";
        return; // Dừng mã khác
    } else {
        // Xử lý nếu token hết hạn hoặc không tồn tại
        console.log("Token hết hạn hoặc không tồn tại.");
    }


    // Handle login form submission
    $("#loginForm").submit(function (event) {
        event.preventDefault();

        const email = $("#loginEmail").val();
        const password = $("#loginPassword").val();

        const loginData = {
            email: email,
            password: password
        };

        $.ajax({
            url: "http://localhost:8080/auth/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(loginData),
            success: function (data) {
                if (data.status === 200) {
                    const token = data.data.token;
                    if (token) {
                        localStorage.setItem("token", token);
                        window.location.href = "http://localhost:8080/home"; // Redirect to home page
                    } else {
                        $("#login-error-message").text("Login failed. No token received.");
                    }
                } else {
                    $("#login-error-message").text(data.message);
                }
            },
            error: function (error) {
                console.error("Error:", error);
                $("#login-error-message").text("An error occurred, please try again.");
            }
        });
    });

    // Handle signup form submission
    $("#signupForm").submit(function (event) {
        event.preventDefault();

        const email = $("#signupEmail").val();
        const password = $("#signupPassword").val();
        const confirmPassword = $("#signupConfirmPassword").val();

        if (password !== confirmPassword) {
            $("#signup-error-message").text("Passwords do not match!");
            return;
        }

        const signupData = {
            email: email,
            password: password
        };

        $.ajax({
            url: "http://localhost:8080/auth/signup",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(signupData),
            success: function (data) {
                if (data.status === 200) {
                    $("#signup-error-message").text("Signup successful! You can now log in.");
                } else {
                    $("#signup-error-message").text(data.message);
                }
            },
            error: function (error) {
                console.error("Error:", error);
                $("#signup-error-message").text("An error occurred, please try again.");
            }
        });
    });

});
