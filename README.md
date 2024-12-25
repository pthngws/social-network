# Mạng Xã Hội Đơn Giản

Đây là một dự án đồ án phát triển một mạng xã hội đơn giản với các tính năng cơ bản như nhắn tin, kết bạn, bình luận và thích bài viết. Dự án được phát triển bằng **Spring Boot**.

## Mục Lục

- [Giới Thiệu](#giới-thiệu)
- [Cài Đặt](#cài-đặt)
  - [Cài Đặt Spring Boot](#cài-đặt-spring-boot)
  - [Cài Đặt Cơ Sở Dữ Liệu MySQL](#cài-đặt-cơ-sở-dữ-liệu-mysql)
  - [Cài Đặt jQuery](#cài-đặt-jquery)
- [Các Chức Năng](#các-chức-năng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cảm Ơn](#cảm-ơn)

## Giới Thiệu

Đây là một ứng dụng mạng xã hội đơn giản với hai vai trò chính:

- **Admin**: Quản lý hệ thống, kiểm duyệt nội dung và người dùng.
- **Client**: Người dùng có thể kết bạn, nhắn tin, bình luận và thích bài viết.

Các tính năng chính bao gồm:

- Gửi và nhận tin nhắn.
- Kết bạn với người dùng khác.
- Bình luận và thích bài viết.

## Cài Đặt

Để cài đặt và chạy dự án, thực hiện các bước sau:

### Cài Đặt Spring Boot

Clone repository về máy:

```bash
git clone https://github.com/yourusername/simple-social-network.git
cd simple-social-network
```

Cài đặt các phụ thuộc bằng Maven:

```bash
mvn install
```

### Cài Đặt Cơ Sở Dữ Liệu MySQL

1. Cài đặt MySQL và tạo một cơ sở dữ liệu mới, ví dụ: `social_network`.
2. Cấu hình kết nối MySQL trong file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/social_network
spring.datasource.username=root
spring.datasource.password=yourpassword
```

3. Chạy các câu lệnh SQL để tạo bảng dữ liệu. Các bảng sẽ được tạo tự động khi chạy ứng dụng, hoặc bạn có thể chạy script SQL đã chuẩn bị sẵn.

### Cài Đặt jQuery

Nếu bạn sử dụng jQuery cho phần frontend, thêm jQuery vào file HTML từ CDN:

```html
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
```

Hoặc tải về và lưu trữ trong thư mục `src/main/resources/static/js`.

## Các Chức Năng

### Nhắn Tin

- Người dùng có thể gửi và nhận tin nhắn giữa bạn bè.
- Tin nhắn được lưu trong cơ sở dữ liệu và hiển thị theo dạng hội thoại.

### Kết Bạn

- Gửi yêu cầu kết bạn và xác nhận yêu cầu từ người dùng khác.

### Bình Luận và Thích Bài Viết

- Người dùng có thể bình luận và bày tỏ sự yêu thích các bài viết của bạn bè.

## Cấu Trúc Dự Án

Dự án được tổ chức như sau:

```
src/
├── main/
│   ├── java/com/example/socialnetwork/       # Backend logic (Controllers, Services, Repositories)
│   ├── resources/
│   │   ├── templates/                        # Templates (HTML) cho giao diện
│   │   ├── static/                           # File tĩnh (CSS, JS, hình ảnh)
```

## Yêu Cầu Hệ Thống

- **Java**: Phiên bản 17 trở lên.
- **MySQL**: Phiên bản 5.7 trở lên.
- **Maven**: Phiên bản 3.6 trở lên.

## Cảm Ơn

- Hy vọng bạn sẽ thích dự án này! 🚀
