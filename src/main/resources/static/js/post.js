function createPostElement(post) {
    const postElement = document.createElement("div");
    postElement.classList.add("post-box");

    // Tính toán thời gian chênh lệch
    const postDate = new Date(post.timestamp);
    const currentDate = new Date();
    const timeDiff = currentDate - postDate;

    const secondsAgo = Math.floor(timeDiff / 1000); // Số giây đã trôi qua
    const minutesAgo = Math.floor(secondsAgo / 60); // Số phút đã trôi qua
    const hoursAgo = Math.floor(minutesAgo / 60); // Số giờ đã trôi qua

    // Hiển thị thời gian theo định dạng phù hợp
    let timeDisplay;
    if (hoursAgo < 1) {
        timeDisplay = minutesAgo < 1 ? `${secondsAgo} giây trước` : `${minutesAgo} phút trước`;
    } else if (hoursAgo < 24) {
        timeDisplay = `${hoursAgo} giờ trước`;
    } else {
        timeDisplay = postDate.toLocaleString();
    }

    // Xử lý media (ảnh hoặc video)
    let mediaContent = '';
    if (post.media && post.media.length > 0) {
        const mediaUrl = post.media[0].media.url;
        const mediaType = mediaUrl.split('.').pop();

        if (['jpg', 'jpeg', 'png'].includes(mediaType)) {
            mediaContent = `<img src="${mediaUrl}" alt="Post media" class="post-media img-fluid" />`;
        } else if (['mp4', 'mov'].includes(mediaType)) {
            mediaContent = `<video src="${mediaUrl}" controls class="post-media img-fluid"></video>`;
        }
    }

    // Tạo HTML cho bài viết
    postElement.innerHTML = `
        <div class="post-header d-flex align-items-center mb-3">
            <img src="${post.imageUrl}" alt="User" class="me-2 rounded-circle" style="width: 50px; height: 50px; object-fit: cover;">
            <div>
                <a href='http://localhost:8080/${post.authorId}' style="color: black; font-weight: bold; text-decoration: none;">${post.authorName}</a>
                <br>
                <small class="text-muted">${timeDisplay}</small>
            </div>
            <div class="dropdown ms-auto">
                <button class="btn btn-link" type="button" id="postOptions" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-three-dots-vertical"></i>
                </button>
                <ul class="dropdown-menu" aria-labelledby="postOptions">
                    ${post.authorId == localStorage.getItem("userId") ? `
                    <li><button class="dropdown-item edit-post" data-id="${post.id}">Chỉnh sửa</button></li>
                    <li><button class="dropdown-item delete-post" data-id="${post.id}">Xóa</button></li>
                ` : '<li><button class="dropdown-item report-post" data-id="${post.id}">Báo cáo</button></li>'}
                
                </ul>
            </div>
        </div>

        <div class="post-content">
            <h2>${post.content}</h2>
            ${mediaContent}
        </div>

        <hr>

        <div class="reaction-icons d-flex justify-content-between">
            <i class="bi ${post.liked ? 'bi-heart-fill text-danger' : 'bi-heart'} likeBtn" data-post-id="${post.id}">
                Yêu thích (${post.likedByCount})
            </i>
            <i class="bi bi-chat"> Bình luận (${post.commentCount})</i>
        </div>
    `;

    // Tách phần like và comment ra ngoài hàm
    handleLikeButton(post, postElement);
    handleCommentButton(post, postElement);

    return postElement;
}

// Xử lý sự kiện Like
function handleLikeButton(post, postElement) {
    const likeBtn = postElement.querySelector(".likeBtn");
    let newLikeCount = post.likedByCount; // Số lượng người thích
    let isLiked = post.liked; // Trạng thái thích của bài viết

    likeBtn.addEventListener("click", function () {
        const postId = likeBtn.getAttribute("data-post-id");

        fetch(`http://localhost:8080/like/${postId}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
        })
                    // Kiểm tra nếu nút đã được "liked"
                if (isLiked) {
                    // Nếu đã thích thì thay đổi trạng thái thành chưa thích
                    likeBtn.classList.remove("text-danger");
                    likeBtn.classList.remove("bi-heart-fill");
                    likeBtn.classList.add("bi-heart");
                    newLikeCount -= 1; // Giảm số người thích
                } else {
                    // Nếu chưa thích thì thay đổi trạng thái thành đã thích
                    likeBtn.classList.add("text-danger");
                    likeBtn.classList.add("bi-heart-fill");
                    likeBtn.classList.remove("bi-heart");
                    newLikeCount += 1; // Tăng số người thích
                }

                // Cập nhật trạng thái liked
                isLiked = !isLiked;

                // Cập nhật số lượng người thích trên giao diện
                likeBtn.textContent = ` Yêu thích (${newLikeCount})`;
        })

}


// Xử lý sự kiện Comment
function handleCommentButton(post, postElement) {
    const commentBtn = postElement.querySelector(".bi-chat");
    commentBtn.addEventListener("click", function () {
        const existingCommentBox = postElement.querySelector(".comment-box");
        if (existingCommentBox) {
            existingCommentBox.remove();
            return;
        }

        const commentBox = document.createElement("div");
        commentBox.classList.add("comment-box", "mt-3");
        commentBox.innerHTML = `
            <div class="d-flex align-items-center">
                <div class="flex-grow-1">
                    <textarea class="form-control" rows="1" style="resize: none;" placeholder="Viết bình luận..."></textarea>
                </div>
                <button class="btn btn-primary btn-sm ms-2 submit-comment" style="white-space: nowrap;">Đăng</button>
            </div>
        `;

        postElement.appendChild(commentBox);
        fetchComments(post.id, commentBox);

        const submitCommentBtn = commentBox.querySelector(".submit-comment");
        const commentInput = commentBox.querySelector("textarea");

        submitCommentBtn.addEventListener("click", function () {
            let commentContent = commentInput.value.trim();
            if (!commentContent) {
                alert("Vui lòng nhập nội dung bình luận.");
                return;
            }

            // Lấy thông tin authorId và authorName từ metadata
            const authorId = commentInput.getAttribute("data-author-id");
            const authorName = commentInput.getAttribute("data-author-name");

            if (authorId && authorName) {
                // Thay thế tên đầy đủ của người dùng vào thẻ <a>
                commentContent = commentContent.replace(
                    `@${authorName}`,
                    `<a href="/${authorId}" style="font-weight: bold; text-decoration: none;">${authorName}</a>`
                );
            }

            fetch(`http://localhost:8080/comment/${post.id}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
                body: JSON.stringify({ content: commentContent }),
            })
                .then((response) => response.json())
                .then((data) => {
                    if (data.message === "Comment successful!") {
                        commentInput.value = "";
                        commentInput.removeAttribute("data-author-id"); // Xóa metadata
                        commentInput.removeAttribute("data-author-name");
                        commentBox.querySelector(".comment-list")?.remove();
                        fetchComments(post.id, commentBox);
                    } else {
                        alert("Bình luận thất bại.");
                    }
                })
                .catch((error) => {
                    console.error("Error while commenting:", error);
                });
        });


    });
}
function fetchComments(postId, commentBox) {
    fetch(`http://localhost:8080/comment/${postId}`, {
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
    })
        .then((response) => response.json())
        .then((data) => {
            if (data.status === 200 && data.data.length > 0) {
                const comments = data.data;

                // Chỉ lấy các bình luận gốc (không phải trả lời)
                const rootComments = comments
                    .filter((comment) => !comment.replyId)
                    .sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

                const commentList = document.createElement("div");
                commentList.classList.add("comment-list", "mt-2");

                // Hàm hiển thị các bình luận với thanh cây
                function renderComments(commentTree, parentElement, level = 0) {
                    commentTree.forEach((comment) => {
                        const commentItem = document.createElement("div");
                        commentItem.classList.add("comment-item", "mb-2", "position-relative");

                        // Tính toán thời gian bình luận
                        const commentDate = new Date(comment.timestamp);
                        const currentDate = new Date();
                        const timeDiff = currentDate - commentDate;

                        const secondsAgo = Math.floor(timeDiff / 1000);
                        const minutesAgo = Math.floor(secondsAgo / 60);
                        const hoursAgo = Math.floor(minutesAgo / 60);

                        let timeDisplay;
                        if (hoursAgo < 1) {
                            timeDisplay = minutesAgo < 1 ? `${secondsAgo} giây trước` : `${minutesAgo} phút trước`;
                        } else if (hoursAgo < 24) {
                            timeDisplay = `${hoursAgo} giờ trước`;
                        } else {
                            timeDisplay = commentDate.toLocaleString();
                        }

                        // Tạo giao diện bình luận
                        commentItem.innerHTML = `
                            <div class="d-flex align-items-start">
                                <img src="${comment.imageUrl}" alt="${comment.authorName}" class="me-2 rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">
                                <div class="bg-light rounded-3 p-2" style="flex-grow: 1; max-width: calc(100% - 50px);">
                                    <a href='http://localhost:8080/${comment.authorId}' class="d-block" style="font-size: 0.95rem; color: black; font-weight: bold; text-decoration: none;">${comment.authorName}</a>
                                    <p class="mb-1" style="font-size: 0.9rem; line-height: 1.4;">
                                        ${comment.replyAuthorName ? `<a href="http://localhost:8080/${comment.replyAuthorId}" style="font-weight: bold; text-decoration: none;" class="text-primary">${comment.replyAuthorName}</a> ` : ""}
                                        ${comment.content}
                                    </p>
                                </div>
                            </div>
                            <div class="ms-5">
                                <small class="text-muted">${timeDisplay}</small>
                                <a class="btn reply-btn" href="javascript:void(0);" data-comment-id="${comment.id}" data-author-name="${comment.authorName}" data-author-id="${comment.authorId}">Trả lời</a>
                                <a class="btn delete-btn" href="javascript:void(0);" data-comment-id="${comment.id}" onmouseover="this.style.color='red';" onmouseout="this.style.color='black'; this.style.textDecoration='none'">Xóa</a>
                                <div class="reply-box mt-2" style="display: none;"></div>
                            </div>
                        `;
// Thêm sự kiện "Xóa"
                        const deleteBtn = commentItem.querySelector(".delete-btn");
                        deleteBtn.addEventListener("click", function () {
                            if (confirm("Bạn có chắc chắn muốn xóa bình luận này không?")) {
                                const commentId = deleteBtn.getAttribute("data-comment-id");

                                fetch(`http://localhost:8080/comment/${commentId}`, {
                                    method: "DELETE",
                                    headers: {
                                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                                    },
                                })
                                    .then((response) => response.json())
                                    .then((data) => {
                                        if (data.status === 200) {
                                            alert("Xóa bình luận thành công!");
                                            commentItem.remove(); // Xóa bình luận khỏi giao diện
                                        } else {
                                            alert("Xóa bình luận thất bại!");
                                        }
                                    })
                                    .catch((error) => {
                                        console.error("Error deleting comment:", error);
                                        alert("Đã xảy ra lỗi khi xóa bình luận.");
                                    });
                            }
                        });
                        // Đánh dấu ID cho việc link đến
                        commentItem.id = `comment-${comment.id}`;

                        // Thêm thụt lề để thể hiện cấp độ
                        commentItem.style.marginLeft = `${level * 20}px`; // Mỗi cấp sẽ thụt vào 20px

                        // Thêm sự kiện "Trả lời"
                        const replyBtn = commentItem.querySelector(".reply-btn");
                        replyBtn.addEventListener("click", function () {
                            const replyBox = commentItem.querySelector(".reply-box");
                            replyBox.innerHTML = ""; // Reset box

                            if (replyBox.style.display === "none") {
                                replyBox.style.display = "block";

                                // Tạo box nhập trả lời
                                const replyInputBox = document.createElement("div");
                                replyInputBox.innerHTML = `
                                    <div class="d-flex align-items-center">
                                        <textarea class="form-control" rows="1" style="resize: none;" placeholder="Trả lời..."></textarea>
                                        <button class="btn btn-primary btn-sm ms-2 submit-reply" style="white-space: nowrap;">Đăng</button>
                                    </div>
                                `;
                                replyBox.appendChild(replyInputBox);

                                const replyInput = replyBox.querySelector("textarea");
                                const submitReplyBtn = replyBox.querySelector(".submit-reply");

                                submitReplyBtn.addEventListener("click", function () {
                                    const replyContent = replyInput.value.trim();
                                    if (!replyContent) {
                                        alert("Vui lòng nhập nội dung trả lời.");
                                        return;
                                    }

                                    fetch(`http://localhost:8080/comment/${postId}`, {
                                        method: "POST",
                                        headers: {
                                            "Content-Type": "application/json",
                                            Authorization: `Bearer ${localStorage.getItem("token")}`,
                                        },
                                        body: JSON.stringify({
                                            content: replyContent,
                                            parentCommentId: replyBtn.getAttribute("data-comment-id"),
                                        }),
                                    })
                                        .then((response) => response.json())
                                        .then((data) => {
                                            if (data.message === "Comment successful!") {
                                                replyInput.value = "";
                                                replyBox.style.display = "none";
                                                commentBox.querySelector(".comment-list")?.remove();
                                                fetchComments(postId, commentBox);
                                            } else {
                                                alert("Trả lời thất bại.");
                                            }
                                        })
                                        .catch((error) => {
                                            console.error("Error while replying:", error);
                                        });
                                });
                            } else {
                                replyBox.style.display = "none";
                            }
                        });

                        parentElement.appendChild(commentItem);

                        // Tạo phần bình luận con cho bình luận gốc
                        const childComments = comments.filter(c => c.replyId === comment.id);
                        if (childComments.length > 0) {
                            // Tạo nút "Xem thêm" cho bình luận gốc
                            const showMoreBtn = document.createElement("button");
                            showMoreBtn.classList.add("btn", "btn-link", "show-more-replies");
                            showMoreBtn.textContent = "Xem thêm ";// Căn gần lề phải
                            showMoreBtn.style.marginLeft = "10px"; // Khoảng cách lề phải
                            showMoreBtn.style.fontWeight = "bold"; // In đậm
                            showMoreBtn.style.color = "black";    // Chữ màu đen
                            commentItem.appendChild(showMoreBtn);

                            // Tạo container cho phần phản hồi
                            const replyContainer = document.createElement("div");
                            replyContainer.classList.add("child-comments", "ms-4", "mt-2");
                            commentItem.appendChild(replyContainer);

                            // Ẩn phần phản hồi ban đầu
                            replyContainer.style.display = "none";

                            // Khi nhấn nút "Xem thêm", hiển thị phản hồi
                            showMoreBtn.addEventListener("click", () => {
                                if (replyContainer.style.display === "none") {
                                    // Hiển thị phản hồi nếu chưa được render
                                    if (!replyContainer.dataset.loaded) {
                                        renderComments(childComments, replyContainer, level ); // Tăng cấp độ khi render
                                        replyContainer.dataset.loaded = true; // Đánh dấu đã render
                                    }
                                    replyContainer.style.display = "block";
                                    showMoreBtn.textContent = "Ẩn phản hồi"; // Thay đổi nội dung nút
                                } else {
                                    // Ẩn phản hồi
                                    replyContainer.style.display = "none";
                                    showMoreBtn.textContent = "Xem thêm"; // Thay đổi nội dung nút
                                }
                            });
                        }
                    });
                }

                renderComments(rootComments, commentList); // Không chia cấp bậc cho bình luận
                commentBox.appendChild(commentList);
            } else {
                const noComments = document.createElement("p");
                noComments.classList.add("text-muted", "mt-2");
                noComments.textContent = "Chưa có bình luận nào.";
                commentBox.appendChild(noComments);
            }
        })
        .catch((error) => {
            console.error("Error fetching comments:", error);
        });
}

// Sử dụng sự kiện jQuery động
$(document).ready(function() {


    $("body").on("click", ".delete-post", function() {
        var postId = $(this).data("id");

        // Gửi yêu cầu xóa đến server
        $.ajax({
            url: "/post/" + postId,
            type: "DELETE",
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
            success: function(response) {
                // Loại bỏ bài viết khỏi giao diện
                location.reload();

            },
            error: function(xhr, status, error) {
                alert("Xóa bài viết thất bại!");
            }
        });
    });
});

// Xử lý thay đổi media khi tạo bài viết mới
document.getElementById("postMedia").addEventListener("change", function (event) {
    const files = event.target.files;
    const uploadContainer = document.getElementById("mediaUploadContainer");

    Array.from(files).forEach((file) => {
        const reader = new FileReader();
        reader.onload = function (e) {
            const mediaItem = document.createElement("div");
            mediaItem.classList.add("media-item");

            if (file.type.startsWith("image/")) {
                const img = document.createElement("img");
                img.src = e.target.result;
                mediaItem.appendChild(img);
            } else if (file.type.startsWith("video/")) {
                const video = document.createElement("video");
                video.src = e.target.result;
                video.controls = true;
                mediaItem.appendChild(video);
            }

            // Thêm nút xóa
            const removeBtn = document.createElement("button");
            removeBtn.classList.add("remove-btn");
            removeBtn.innerHTML = "&times;";
            removeBtn.onclick = function () {
                mediaItem.remove();
            };
            mediaItem.appendChild(removeBtn);

            uploadContainer.appendChild(mediaItem);
        };
        reader.readAsDataURL(file);
    });
});

// Xử lý tạo bài viết mới
document.getElementById("postSubmitBtn").addEventListener("click", function () {
    const content = document.getElementById("postContent").value.trim();
    const mediaFiles = document.getElementById("postMedia").files;

    // Kiểm tra nếu cả content và media đều trống
    if (!content && mediaFiles.length === 0) {
        alert("Bạn phải nhập nội dung hoặc tải lên ít nhất một tệp media!");
        return;
    }

    const postData = {
        content: content || "      ", // Nếu content trống, đặt là null
        media: [], // Mảng media để lưu dữ liệu file
    };

    // Nếu không có media, gửi ngay yêu cầu
    if (mediaFiles.length === 0) {
        createPost(postData);
        return;
    }

    // Convert media files thành các đối tượng MediaDto
    for (let i = 0; i < mediaFiles.length; i++) {
        const file = mediaFiles[i];
        const reader = new FileReader();

        reader.onloadend = function () {
            postData.media.push({
                url: reader.result, // Chuỗi Base64
                type: file.type, // 'image/jpeg', 'video/mp4', v.v.
            });

            if (postData.media.length === mediaFiles.length) {
                createPost(postData); // Gửi yêu cầu sau khi xử lý tất cả media
            }
        };

        reader.readAsDataURL(file);
    }
});

// Hàm gửi yêu cầu tạo bài viết
function createPost(postData) {
    fetch("http://localhost:8080/post", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(postData),
    })
        .then((response) => response.json())
        .then((data) => {
            fetchPosts(); // Lấy lại danh sách bài viết sau khi tạo
            const createPostModal = bootstrap.Modal.getInstance(
                document.getElementById("createPostModal")
            );
            createPostModal.hide();
        })
        .catch((error) => {
            console.error("Error creating post:", error);
        });
}


document.addEventListener("click", function (event) {
    if (event.target.classList.contains("edit-post")) {
        const postId = event.target.getAttribute("data-id");
        const postContent = event.target.closest(".post-box").querySelector(".post-content h2").innerText;

        // Đưa dữ liệu vào modal
        document.getElementById("editPostId").value = postId;
        document.getElementById("editPostContent").value = postContent;

        // Hiển thị modal
        const editModal = new bootstrap.Modal(document.getElementById("editPostModal"));
        editModal.show();
    }
});

document.getElementById("saveEditPost").addEventListener("click", function () {
    const postId = document.getElementById("editPostId").value;
    const newContent = document.getElementById("editPostContent").value;

    // Gửi yêu cầu cập nhật lên server (giả sử API là /updatePost)
    fetch(`/post/${postId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: postId, content: newContent })
    })
        .then(response => response.json())
        .then(data => {
            if (data.status== 200) {
                // Cập nhật nội dung bài viết trên giao diện
                const postElement = document.querySelector(`.edit-post[data-id="${postId}"]`).closest(".post-box");
                postElement.querySelector(".post-content h2").innerText = newContent;

                // Đóng modal
                const editModal = bootstrap.Modal.getInstance(document.getElementById("editPostModal"));
                editModal.hide();
            } else {
                alert("Cập nhật thất bại!");
            }
        })
        .catch(error => console.error("Lỗi:", error));
});
