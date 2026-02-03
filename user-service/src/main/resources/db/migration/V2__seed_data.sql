-- Seed users with BCrypt hashed passwords (password: password123)
INSERT INTO users (id, username, email, password, bio, image) VALUES
('user-1', 'johndoe', 'john@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Software developer and tech enthusiast', 'https://api.realworld.io/images/smiley-cyrus.jpeg'),
('user-2', 'janedoe', 'jane@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Full-stack developer passionate about clean code', 'https://api.realworld.io/images/smiley-cyrus.jpeg'),
('user-3', 'bobsmith', 'bob@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'DevOps engineer and cloud architect', 'https://api.realworld.io/images/smiley-cyrus.jpeg');

-- Seed follow relationships
INSERT INTO follows (user_id, follow_id) VALUES
('user-1', 'user-2'),
('user-1', 'user-3'),
('user-2', 'user-1'),
('user-3', 'user-1');
