const PORT = 35000;
const BASE_URL = `http://localhost:${PORT}`;

async function fetchName() {
    const res = await fetch(`${BASE_URL}/name`);
    document.getElementById("nameResult").textContent = await res.text();
}

async function addBook() {
    const bookName = document.getElementById("bookInput").value;
    if (!bookName) return;
    const res = await fetch(`${BASE_URL}/books?name=${encodeURIComponent(bookName)}`);
    document.getElementById("booksResult").textContent = await res.text();
}

async function stopServer() {
    const res = await fetch(`${BASE_URL}/stop`);
    document.getElementById("stopResult").textContent = await res.text();
}