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
        const mediaCount = post.media.length;
        const mediaItems = post.media.map(mediaItem => {
            const mediaUrl = mediaItem.media.url;
            const mediaType = mediaUrl.split('.').pop().toLowerCase();

            // Thêm sự kiện onclick để mở modal
            if (['jpg', 'jpeg', 'png'].includes(mediaType)) {
                return `<img src="${mediaUrl}" alt="Post media" class="post-media img-fluid" onclick="openMediaPreview('${mediaUrl}', 'image')" />`;
            } else if (['mp4', 'mov'].includes(mediaType)) {
                return `<video src="${mediaUrl}" controls class="post-media img-fluid" onclick="openMediaPreview('${mediaUrl}', 'video')"></video>`;
            }
            return '';
        }).join('');

        mediaContent = `
            <div class="media-container media-count-${mediaCount}">
                ${mediaItems}
            </div>
        `;
    }

    // Tạo HTML cho bài viết
    postElement.innerHTML = `
        <div class="post-header d-flex align-items-center mb-3">
            <img src="${post.imageUrl}" alt="User" class="me-2 rounded-circle" style="width: 50px; height: 50px; object-fit: cover;">
            <div>
                <a href='/${post.authorId}' style="color: black; font-weight: bold; text-decoration: none;">${post.authorName}</a>
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
                ` : `<li><button class="dropdown-item report-post" data-id="${post.id}">Báo cáo</button></li>`}
                </ul>
            </div>
        </div>
<!-- Modal chọn lý do báo cáo -->
<div class="modal fade" id="reportPostModal" tabindex="-1" aria-labelledby="reportPostModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="reportPostModalLabel">Báo cáo bài viết</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <form id="reportForm">
          <input type="hidden" id="reportPostId">
          <div class="mb-3">
            <label class="form-label">Chọn lý do báo cáo:</label>
            <div>
              <input type="radio" name="reportReason" value="spam" id="reasonSpam">
              <label for="reasonSpam">Spam</label>
            </div>
            <div>
              <input type="radio" name="reportReason" value="violence" id="reasonViolence">
              <label for="reasonViolence">Bạo lực</label>
            </div>
            <div>
              <input type="radio" name="reportReason" value="harassment" id="reasonHarassment">
              <label for="reasonHarassment">Quấy rối</label>
            </div>
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
        <button type="button" class="btn btn-primary" id="submitReport" onclick="report()">Gửi báo cáo</button>
      </div>
    </div>
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

    handleLikeButton(post, postElement);
    handleCommentButton(post, postElement);

    return postElement;
}

// Hàm mở modal xem ảnh/video chi tiết
function openMediaPreview(mediaUrl, mediaType) {
    const mediaPreviewContent = document.getElementById("mediaPreviewContent");

    // Xóa nội dung cũ trong modal
    mediaPreviewContent.innerHTML = '';

    // Tạo phần tử media tương ứng
    if (mediaType === 'image') {
        mediaPreviewContent.innerHTML = `<img src="${mediaUrl}" alt="Media preview" class="img-fluid" style="max-height: 80vh; max-width: 100%;" />`;
    } else if (mediaType === 'video') {
        mediaPreviewContent.innerHTML = `<video src="${mediaUrl}" controls autoplay class="img-fluid" style="max-height: 80vh; max-width: 100%;"></video>`;
    }

    // Mở modal
    const mediaPreviewModal = new bootstrap.Modal(document.getElementById("mediaPreviewModal"));
    mediaPreviewModal.show();
}

// Xử lý sự kiện Like
function handleLikeButton(post, postElement) {
    const likeBtn = postElement.querySelector(".likeBtn");
    let newLikeCount = post.likedByCount; // Số lượng người thích
    let isLiked = post.liked; // Trạng thái thích của bài viết

    likeBtn.addEventListener("click", function () {
        const postId = likeBtn.getAttribute("data-post-id");

        fetch(`/like/${postId}`, {
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

            fetch(`/comment/${post.id}`, {
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
    fetch(`/comment/${postId}`, {
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
                                    <a href='/${comment.authorId}' class="d-block" style="font-size: 0.95rem; color: black; font-weight: bold; text-decoration: none;">${comment.authorName}</a>
                                    <p class="mb-1" style="font-size: 0.9rem; line-height: 1.4;">
                                        ${comment.replyAuthorName ? `<a href="/${comment.replyAuthorId}" style="font-weight: bold; text-decoration: none;" class="text-primary">${comment.replyAuthorName}</a> ` : ""}
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

                                fetch(`/comment/${commentId}`, {
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

                                    fetch(`/comment/${postId}`, {
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
// Biến để lưu trữ danh sách các tệp đã chọn
let selectedFiles = [];

// Xử lý sự kiện thay đổi file để hiển thị trước
document.getElementById("postMedia").addEventListener("change", function (event) {
    const files = event.target.files;
    const uploadContainer = document.getElementById("mediaUploadContainer");

    // Log số lượng tệp ngay khi chọn
    console.log("Số tệp được chọn trong sự kiện change:", files.length);
    for (let i = 0; i < files.length; i++) {
        console.log(`Tệp ${i + 1} trong change: ${files[i].name}`);
    }

    // Thêm các tệp mới vào danh sách selectedFiles
    Array.from(files).forEach(file => {
        // Kiểm tra xem tệp đã tồn tại trong selectedFiles chưa (dựa trên tên và kích thước)
        const isDuplicate = selectedFiles.some(existingFile =>
            existingFile.name === file.name && existingFile.size === file.size
        );
        if (!isDuplicate) {
            selectedFiles.push(file);
        }
    });

    // Log danh sách tệp hiện tại
    console.log("Danh sách tệp hiện tại:", selectedFiles.map(file => file.name));

    // Xóa các media cũ trước khi hiển thị lại
    uploadContainer.querySelectorAll(".media-item").forEach(item => item.remove());

    // Hiển thị tất cả các tệp trong selectedFiles
    selectedFiles.forEach((file, index) => {
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
            removeBtn.innerHTML = "×";
            removeBtn.onclick = function () {
                // Xóa tệp khỏi selectedFiles
                selectedFiles.splice(index, 1);
                // Cập nhật lại giao diện
                mediaItem.remove();
                // Log danh sách tệp sau khi xóa
                console.log("Danh sách tệp sau khi xóa:", selectedFiles.map(file => file.name));
            };
            mediaItem.appendChild(removeBtn);

            uploadContainer.appendChild(mediaItem);
        };
        reader.readAsDataURL(file);
    });
});

// Xử lý gửi bài viết
document.getElementById("postSubmitBtn").addEventListener("click", function () {
    const content = document.getElementById("postContent").value.trim();

    // Log số lượng tệp media được chọn khi nhấn Post
    console.log("Số lượng tệp media được chọn khi gửi:", selectedFiles.length);
    for (let i = 0; i < selectedFiles.length; i++) {
        console.log(`Tệp ${i + 1}:`, selectedFiles[i].name, `- Loại: ${selectedFiles[i].type}, Kích thước: ${selectedFiles[i].size} bytes`);
    }

    // Kiểm tra nếu cả content và media đều trống
    if (!content && selectedFiles.length === 0) {
        alert("Bạn phải nhập nội dung hoặc tải lên ít nhất một tệp media!");
        return;
    }

    const formData = new FormData();
    formData.append("content", content || " ");

    // Thêm tất cả các tệp từ selectedFiles vào FormData
    for (let i = 0; i < selectedFiles.length; i++) {
        formData.append("media", selectedFiles[i]);
        console.log(`Đã thêm tệp ${i + 1} vào FormData:`, selectedFiles[i].name);
    }

    console.log("Đang gửi yêu cầu tạo bài viết với FormData...");
    fetch("/post", {
        method: "POST",
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
    })
        .then((response) => {
            console.log("Đã nhận phản hồi từ server, trạng thái:", response.status);
            return response.json();
        })
        .then((data) => {
            console.log("Phản hồi từ server:", data);
            if (data.status === 200) {
                console.log("Tạo bài viết thành công! Số tệp media đã gửi:", selectedFiles.length);
                // Reset form
                document.getElementById("postContent").value = "";
                document.getElementById("postMedia").value = ""; // Reset input file
                selectedFiles = []; // Reset danh sách tệp
                const uploadContainer = document.getElementById("mediaUploadContainer");
                uploadContainer.querySelectorAll(".media-item").forEach(item => item.remove()); // Xóa media hiển thị
                fetchPosts();
                const createPostModal = bootstrap.Modal.getInstance(document.getElementById("createPostModal"));
                createPostModal.hide();
            } else {
                console.log("Tạo bài viết thất bại! Phản hồi:", data);
                alert("Tạo bài viết thất bại!");
            }
        })
        .catch((error) => {
            console.error("Lỗi khi tạo bài viết:", error);
            alert("Đã xảy ra lỗi khi đăng bài!");
        });
});
let editSelectedFiles = []; // Media mới được thêm
let editCurrentMedia = []; // Media hiện tại của bài viết
let mediaToDelete = []; // Danh sách media cần xóa

document.addEventListener("click", function (event) {
    if (event.target.classList.contains("edit-post")) {
        const postId = event.target.getAttribute("data-id");
        const postBox = event.target.closest(".post-box");
        const postContent = postBox.querySelector(".post-content h2").innerText;

        // Đưa dữ liệu vào modal
        document.getElementById("editPostId").value = postId;
        document.getElementById("editPostContent").value = postContent;

        // Lấy danh sách media hiện tại từ bài viết
        const mediaContainer = postBox.querySelector(".media-container");
        editCurrentMedia = [];
        mediaToDelete = [];
        editSelectedFiles = [];

        if (mediaContainer) {
            const mediaItems = mediaContainer.querySelectorAll(".post-media");
            mediaItems.forEach((media, index) => {
                editCurrentMedia.push({
                    url: media.src,
                    type: media.tagName === "IMG" ? "image" : "video",
                    index: index
                });
            });
        }

        // Hiển thị media hiện tại trong modal
        const editCurrentMediaContainer = document.getElementById("editCurrentMediaContainer");
        editCurrentMediaContainer.innerHTML = "";
        editCurrentMedia.forEach((media, index) => {
            const mediaItem = document.createElement("div");
            mediaItem.classList.add("current-media-item");

            if (media.type === "image") {
                mediaItem.innerHTML = `<img src="${media.url}" alt="Current media" />`;
            } else {
                mediaItem.innerHTML = `<video src="${media.url}" controls></video>`;
            }

            const removeBtn = document.createElement("button");
            removeBtn.classList.add("remove-btn");
            removeBtn.innerHTML = "×";
            removeBtn.onclick = function () {
                mediaToDelete.push(media.url); // Thêm URL vào danh sách cần xóa
                editCurrentMedia.splice(index, 1); // Xóa khỏi danh sách hiện tại
                mediaItem.remove(); // Xóa khỏi giao diện
                console.log("Media cần xóa:", mediaToDelete);
            };
            mediaItem.appendChild(removeBtn);

            editCurrentMediaContainer.appendChild(mediaItem);
        });

        // Hiển thị modal
        const editModal = new bootstrap.Modal(document.getElementById("editPostModal"));
        editModal.show();
    }
});
// Xử lý sự kiện thay đổi file trong modal chỉnh sửa
document.getElementById("editPostMedia").addEventListener("change", function (event) {
    const files = event.target.files;
    const uploadContainer = document.getElementById("editMediaUploadContainer");

    // Log số lượng tệp ngay khi chọn
    console.log("Số tệp được chọn trong modal chỉnh sửa:", files.length);
    for (let i = 0; i < files.length; i++) {
        console.log(`Tệp ${i + 1} trong change: ${files[i].name}`);
    }

    // Thêm các tệp mới vào danh sách editSelectedFiles
    Array.from(files).forEach(file => {
        const isDuplicate = editSelectedFiles.some(existingFile =>
            existingFile.name === file.name && existingFile.size === file.size
        );
        if (!isDuplicate) {
            editSelectedFiles.push(file);
        }
    });

    // Log danh sách tệp hiện tại
    console.log("Danh sách tệp mới:", editSelectedFiles.map(file => file.name));

    // Xóa các media cũ trước khi hiển thị lại
    uploadContainer.querySelectorAll(".media-item").forEach(item => item.remove());

    // Hiển thị tất cả các tệp trong editSelectedFiles
    editSelectedFiles.forEach((file, index) => {
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

            const removeBtn = document.createElement("button");
            removeBtn.classList.add("remove-btn");
            removeBtn.innerHTML = "×";
            removeBtn.onclick = function () {
                editSelectedFiles.splice(index, 1);
                mediaItem.remove();
                console.log("Danh sách tệp mới sau khi xóa:", editSelectedFiles.map(file => file.name));
            };
            mediaItem.appendChild(removeBtn);

            uploadContainer.appendChild(mediaItem);
        };
        reader.readAsDataURL(file);
    });
});

// Xử lý lưu thay đổi bài viết
document.getElementById("saveEditPost").addEventListener("click", function () {
    const postId = document.getElementById("editPostId").value;
    const newContent = document.getElementById("editPostContent").value;

    // Tạo FormData để gửi dữ liệu
    const formData = new FormData();
    formData.append("content", newContent);
    formData.append("mediaToDelete", JSON.stringify(mediaToDelete)); // Danh sách URL media cần xóa

    // Thêm các tệp media mới vào FormData
    for (let i = 0; i < editSelectedFiles.length; i++) {
        formData.append("media", editSelectedFiles[i]);
    }

    // Gửi yêu cầu cập nhật lên server
    fetch(`/post/${postId}`, {
        method: "PUT",
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 200) {
                // Cập nhật nội dung bài viết trên giao diện
                const postElement = document.querySelector(`.edit-post[data-id="${postId}"]`).closest(".post-box");
                postElement.querySelector(".post-content h2").innerText = newContent;

                // Cập nhật media trên giao diện
                const mediaContainer = postElement.querySelector(".media-container");
                if (mediaContainer) {
                    mediaContainer.remove(); // Xóa media cũ
                }

                if (data.data.media && data.data.media.length > 0) {
                    const newMediaContainer = document.createElement("div");
                    newMediaContainer.classList.add("media-container", `media-count-${data.data.media.length}`);
                    data.data.media.forEach(mediaItem => {
                        const mediaUrl = mediaItem.media.url;
                        const mediaType = mediaUrl.split('.').pop().toLowerCase();
                        if (['jpg', 'jpeg', 'png'].includes(mediaType)) {
                            newMediaContainer.innerHTML += `<img src="${mediaUrl}" alt="Post media" class="post-media img-fluid" onclick="openMediaPreview('${mediaUrl}', 'image')" />`;
                        } else if (['mp4', 'mov'].includes(mediaType)) {
                            newMediaContainer.innerHTML += `<video src="${mediaUrl}" controls class="post-media img-fluid" onclick="openMediaPreview('${mediaUrl}', 'video')"></video>`;
                        }
                    });
                    postElement.querySelector(".post-content").appendChild(newMediaContainer);
                }

                // Đóng modal và reset
                const editModal = bootstrap.Modal.getInstance(document.getElementById("editPostModal"));
                editModal.hide();
                editSelectedFiles = [];
                editCurrentMedia = [];
                mediaToDelete = [];
                document.getElementById("editMediaUploadContainer").querySelectorAll(".media-item").forEach(item => item.remove());
                fetchPosts();
            } else {
                alert("Cập nhật thất bại!");
            }
        })
        .catch(error => console.error("Lỗi:", error));
});
document.addEventListener("click", function (event) {
    // Khi nhấn vào nút Báo cáo
    if (event.target.classList.contains("report-post")) {
        const postId = event.target.getAttribute("data-id");

        // Đưa postId vào hidden input trong modal
        document.getElementById("reportPostId").value = postId;

        // Hiển thị modal
        const reportModal = new bootstrap.Modal(document.getElementById("reportPostModal"));
        reportModal.show();
    }
});

// Khi nhấn nút Gửi báo cáo
function report() {
    const postId = document.getElementById("reportPostId").value;
    const reason = document.querySelector('input[name="reportReason"]:checked');
    console.log("")
    if (reason) {
        const reasonValue = reason.value;

        // Tạo đối tượng reportDto
        const reportDto = {
            title: reasonValue,
            postId: postId
        };

        // Gửi request lên server
        fetch('/report', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(reportDto)
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 200) {
                    alert('Báo cáo thành công!');
                } else {
                    alert('Báo cáo thất bại!');
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                alert('Có lỗi xảy ra, vui lòng thử lại sau!');
            });

        // Đóng modal sau khi gửi báo cáo
        const reportModal = bootstrap.Modal.getInstance(document.getElementById("reportPostModal"));
        reportModal.hide();
    } else {
        alert('Vui lòng chọn lý do báo cáo.');
    }
};
