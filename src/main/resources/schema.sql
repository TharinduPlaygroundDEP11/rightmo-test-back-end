CREATE TABLE polls
(
    poll_id     INT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    category_id INT          NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

CREATE TABLE options
(
    option_id   INT PRIMARY KEY AUTO_INCREMENT,
    poll_id     INT,
    option_text TEXT NOT NULL,
    FOREIGN KEY (poll_id) REFERENCES polls (poll_id)
);

CREATE TABLE votes
(
    vote_id   INT PRIMARY KEY AUTO_INCREMENT,
    poll_id   INT,
    option_id INT,
    FOREIGN KEY (poll_id) REFERENCES polls (poll_id),
    FOREIGN KEY (option_id) REFERENCES options (option_id)
);

CREATE TABLE categories
(
    category_id  INT PRIMARY KEY AUTO_INCREMENT,
    CategoryName VARCHAR(255) NOT NULL
);

