-- Create books table
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    published_year INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create index on ISBN for faster lookups
CREATE INDEX idx_books_isbn ON books(isbn);

-- Create index on author for searching
CREATE INDEX idx_books_author ON books(author);
