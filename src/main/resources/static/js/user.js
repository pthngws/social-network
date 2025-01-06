document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "/login.html"; // Redirect to login if no token
        return;
    }

    if (token) {
        // Gửi request tới API với token
        fetch("http://localhost:8080/user/profile", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,  // Thêm token vào header
                "Content-Type": "application/json"
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    const user = data.data;
                    $("#avatar-mini").attr("src", user.avatar);
                    $("#avatar").attr("src", user.avatar);  // Gán đường dẫn ảnh
                    $("#name").text(user.firstName + " " + user.lastName);  // Gán tên
                } else {
                    console.error("Không thể lấy thông tin người dùng:", data.message);
                }
            })
            .catch(error => {
                console.error("Lỗi khi lấy thông tin người dùng:", error);
            });
    } else {
        console.error("Không tìm thấy token!");
    }
    const friendRequestBtn = document.getElementById("friend-request-btn");
    const url = window.location.href;
    const userId = url.substring(url.lastIndexOf("/") + 1);

    fetchPosts();

    function fetchPosts() {


        // Gọi API thông tin người dùng
        fetch(`http://localhost:8080/user/${userId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    const user = data.data;
                    $("#main-avatar").attr("src", user.avatar);
                    $("#main-name").text(user.firstName + " " + user.lastName);  // Gán tên
                    $("#main-about").text(user.about);
                    // Gán dữ liệu vào form
                    $("#firstname").val(user.firstName);
                    $("#lastname").val(user.lastName);
                    $("#about").val(user.about);
                    $("#birthday").val(user.birthday.split("T")[0]); // Lấy phần ngày từ birthday
                    $(`input[name='gender'][value='${user.gender}']`).prop("checked", true); // Chọn giới tính
                    // Gán avatar
                    $("#avatarImage").attr("src", user.avatar);

                    // Cập nhật trạng thái nút
                    updateFriendButton(user.friendStatus);
                } else {
                    console.error("Không thể lấy thông tin người dùng:", data.message);
                }
            })
            .catch(error => console.error("Lỗi khi lấy thông tin người dùng:", error));
// Gọi API bài viết

        fetch(`http://localhost:8080/userpost/${userId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Failed to fetch posts");
                }
                return response.json();
            })
            .then((data) => {
                const postsContainer = document.querySelector(".post-container");
                postsContainer.innerHTML = ""; // Clear previous posts

                // Kiểm tra nếu không có bài viết
                if (data && data.data && data.data.length === 0) {
                    postsContainer.innerHTML = "<p style=\"text-align: center; font-weight: bold; color: black;\">Không có bài viết.</p>"; // Hiển thị thông báo
                } else if (data && data.data) {
                    data.data.forEach((post) => {
                        const postElement = createPostElement(post);
                        postsContainer.appendChild(postElement);
                    });
                }
            })
            .catch((error) => {
                console.error("Error fetching posts:", error);
            });
    }

    // Hàm cập nhật trạng thái nút "Kết bạn"
    function updateFriendButton(friendStatus) {
        // Giữ màu trắng mặc định cho nút
        friendRequestBtn.classList.remove("btn-primary", "btn-warning", "btn-danger", "btn-success");
        friendRequestBtn.classList.add("btn-primary"); // Màu trắng mặc định

        // Dù có trạng thái nào thì nút vẫn luôn có màu trắng và thay đổi text và hành động
        if (friendStatus === "PENDING") {
            friendRequestBtn.textContent = "Hủy yêu cầu";
            friendRequestBtn.disabled = false;
            friendRequestBtn.onclick = cancelFriendRequest; // Hủy yêu cầu
        } else if (friendStatus === "ACCEPTED") {
            friendRequestBtn.textContent = "Bạn bè";
            friendRequestBtn.disabled = false;
            friendRequestBtn.onclick = askToCancelFriendRequest; // Hủy bạn bè
        } else if (friendStatus === "SENT_BY_OTHER") {
            friendRequestBtn.textContent = "Chấp nhận";
            friendRequestBtn.disabled = false;
            friendRequestBtn.onclick = acceptFriendRequest; // Chấp nhận yêu cầu
        } else {
            friendRequestBtn.textContent = "Kết bạn";
            friendRequestBtn.disabled = false;
            friendRequestBtn.onclick = sendFriendRequest; // Gửi yêu cầu
        }
    }

    function askToCancelFriendRequest() {
        const confirmCancel = confirm("Bạn có chắc chắn muốn hủy bạn bè không?");
        if (confirmCancel) {
            cancelFriendRequest(); // Hủy bạn bè nếu người dùng đồng ý
        }
    }
// Hàm chấp nhận yêu cầu kết bạn
    function acceptFriendRequest() {
        fetch("http://localhost:8080/friendship/accept", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ receiverId: userId }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    fetchPosts(); // Làm mới trạng thái
                } else {
                    console.error("Không thể chấp nhận yêu cầu kết bạn:", data.message);
                }
            })
            .catch(error => console.error("Lỗi khi chấp nhận yêu cầu kết bạn:", error));
    }

    // Hàm gửi yêu cầu kết bạn
    function sendFriendRequest() {
        fetch("http://localhost:8080/friendship/add", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ receiverId: userId }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    fetchPosts(); // Làm mới trạng thái
                } else {
                    console.error("Không thể gửi yêu cầu kết bạn:", data.message);
                }
            })
            .catch(error => console.error("Lỗi khi gửi yêu cầu kết bạn:", error));
    }

    // Hàm hủy yêu cầu kết bạn hoặc hủy bạn bè
    function cancelFriendRequest() {
        fetch("http://localhost:8080/friendship/cancel", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ receiverId: userId }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    fetchPosts(); // Làm mới trạng thái
                } else {
                    console.error("Không thể hủy yêu cầu/bạn bè:", data.message);
                }
            })
            .catch(error => console.error("Lỗi khi hủy yêu cầu/bạn bè:", error));
    }
});



// Gọi API bài viết
