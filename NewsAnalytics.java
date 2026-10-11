import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class NewsAnalytics {

    // =========================
    // POST MODEL
    // =========================
    static class Post {
        int id;
        String title;
        String category;
        String source;
        String content;
        String url;
        String sentiment;

        Post(int id, String title, String category, String source,
             String content, String url) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.source = source;
            this.content = content;
            this.url = url;
            this.sentiment = analyzeSentiment(content);
        }
    }

    // =========================
    // DSA DATA STRUCTURES
    // =========================
    static ArrayList<Post> posts = new ArrayList<>();
    static HashMap<String, Integer> keywordFrequency = new HashMap<>();

    static int nextId = 6;

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) throws Exception {

        addSamplePosts();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/", NewsAnalytics::home);
        server.createContext("/api/posts", NewsAnalytics::getPosts);
        server.createContext("/api/add", NewsAnalytics::addPost);
        server.createContext("/api/search", NewsAnalytics::searchPosts);
        server.createContext("/api/trending", NewsAnalytics::trending);
        server.createContext("/api/stats", NewsAnalytics::stats);

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("======================================");
        System.out.println(" News Analytics API running");
        System.out.println(" http://localhost:8080");
        System.out.println("======================================");

        // Automatically open browser
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(
                        new URI("http://localhost:8080"));
            }
        } catch (Exception e) {
            System.out.println(
                    "Open http://localhost:8080 manually.");
        }
    }

    // =========================
    // SAMPLE NEWS
    // =========================
    static void addSamplePosts() {

        posts.add(new Post(
                1,
                "India Wins Exciting Cricket Match",
                "Sports",
                "Sports News",
                "India won an exciting cricket match after a strong performance by the team. "
                        + "The players showed excellent confidence and teamwork.",
                "https://www.espncricinfo.com/"
        ));

        posts.add(new Post(
                2,
                "New Artificial Intelligence Technology",
                "Technology",
                "Tech Daily",
                "New artificial intelligence technology is helping developers build faster "
                        + "and smarter applications. The latest innovation is receiving positive attention.",
                "https://www.microsoft.com/"
        ));

        posts.add(new Post(
                3,
                "Environmental Protection Campaign",
                "Environment",
                "Green News",
                "A new environmental protection campaign encourages people to plant trees, "
                        + "reduce pollution and protect nature.",
                "https://www.unep.org/"
        ));

        posts.add(new Post(
                4,
                "Health Awareness Program",
                "Health",
                "Health Today",
                "A health awareness program focuses on healthy habits, regular exercise, "
                        + "good nutrition and disease prevention.",
                "https://www.who.int/"
        ));

        posts.add(new Post(
                5,
                "Education Technology Helps Students",
                "Education",
                "Education Times",
                "Modern education technology is helping students learn new subjects through "
                        + "interactive digital platforms and online resources.",
                "https://www.unesco.org/"
        ));

        rebuildKeywords();
    }

    // =========================
    // HOME PAGE
    // =========================
    static void home(HttpExchange exchange) throws IOException {

        String html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>News & Social Media Analytics Engine</title>

<style>

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    font-family: Arial, sans-serif;
    background: #f4f6f8;
    color: #222;
}

header {
    background: #17202a;
    color: white;
    padding: 20px 40px;
}

header h1 {
    margin: 0;
    font-size: 28px;
}

header p {
    margin: 7px 0 0;
    color: #cfd8dc;
}

.container {
    width: 92%;
    max-width: 1200px;
    margin: 25px auto;
}

.stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;
    margin-bottom: 20px;
}

.stat {
    background: white;
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

.stat h2 {
    margin: 0;
    font-size: 28px;
}

.stat p {
    margin-bottom: 0;
    color: #777;
}

.controls {
    background: white;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

.search {
    display: flex;
    gap: 10px;
}

input, textarea, select {
    width: 100%;
    padding: 11px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 14px;
}

button {
    padding: 11px 18px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    background: #17202a;
    color: white;
}

button:hover {
    opacity: .85;
}

.add-box {
    background: white;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

.form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
}

.full {
    grid-column: 1 / 3;
}

.news-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
}

.posts {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.card {
    background: white;
    padding: 18px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

.card h3 {
    margin-top: 0;
    cursor: pointer;
}

.card h3:hover {
    text-decoration: underline;
}

.meta {
    color: #777;
    font-size: 13px;
    margin-bottom: 10px;
}

.category {
    display: inline-block;
    padding: 5px 9px;
    background: #e8eef5;
    border-radius: 15px;
    font-size: 12px;
}

.sentiment {
    margin-left: 5px;
    font-size: 12px;
}

.trending {
    background: white;
    padding: 18px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0,0,0,.08);
}

.trend-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
}

.modal {
    display: none;
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,.6);
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.modal-content {
    background: white;
    max-width: 700px;
    width: 100%;
    max-height: 85vh;
    overflow-y: auto;
    padding: 25px;
    border-radius: 10px;
}

.close {
    float: right;
    font-size: 25px;
    cursor: pointer;
}

.news-content {
    line-height: 1.7;
}

@media(max-width:800px) {

    .stats {
        grid-template-columns: 1fr 1fr;
    }

    .news-grid {
        grid-template-columns: 1fr;
    }

    .form-grid {
        grid-template-columns: 1fr;
    }

    .full {
        grid-column: 1;
    }
}

</style>
</head>

<body>

<header>
    <h1>News & Social Media Analytics Engine</h1>
    <p>Data Structures and Algorithms -3</p>
</header>

<div class="container">

    <div class="stats">

        <div class="stat">
            <h2 id="totalPosts">0</h2>
            <p>Total Posts</p>
        </div>

        <div class="stat">
            <h2 id="positive">0</h2>
            <p>Positive</p>
        </div>

        <div class="stat">
            <h2 id="negative">0</h2>
            <p>Negative</p>
        </div>

        <div class="stat">
            <h2 id="neutral">0</h2>
            <p>Neutral</p>
        </div>

    </div>


    <div class="controls">

        <h2>Search News</h2>

        <div class="search">

            <input
                id="searchInput"
                placeholder="Search title, category, source or keyword..."
                onkeyup="searchNews()"
            >

            <button onclick="loadPosts()">Show All</button>

        </div>

        <p>
            Try:
            <b>Sports</b>,
            <b>Technology</b>,
            <b>Health</b>,
            <b>Education</b>
        </p>

    </div>


    <div class="add-box">

        <h2>Add News Post</h2>

        <div class="form-grid">

            <input id="title"
                   placeholder="News Title">

            <input id="category"
                   placeholder="Category e.g. Sports">

            <input id="source"
                   placeholder="Source">

            <input id="url"
                   placeholder="News URL (optional)">

            <textarea id="content"
                      class="full"
                      rows="5"
                      placeholder="Full news content"></textarea>

            <button class="full"
                    onclick="addNews()">
                Add Post
            </button>

        </div>

    </div>


    <div class="news-grid">

        <div>

            <h2>News Posts</h2>

            <div id="posts"
                 class="posts">
            </div>

        </div>


        <div>

            <h2>Trending Topics</h2>

            <div id="trending"
                 class="trending">
            </div>

        </div>

    </div>

</div>


<!-- FULL NEWS MODAL -->

<div id="modal"
     class="modal"
     onclick="closeOutside(event)">

    <div class="modal-content">

        <span class="close"
              onclick="closeModal()">
              &times;
        </span>

        <h2 id="modalTitle"></h2>

        <p id="modalMeta"
           class="meta"></p>

        <div id="modalContent"
             class="news-content"></div>

        <br>

        <button id="openNewsButton"
                onclick="openOriginalNews()">
            Open News
        </button>

    </div>

</div>


<script>

let allPosts = [];
let selectedUrl = "";


// =========================
// LOAD POSTS
// =========================

async function loadPosts() {

    const response =
        await fetch("/api/posts");

    allPosts =
        await response.json();

    displayPosts(allPosts);

    loadStats();

    loadTrending();
}


// =========================
// DISPLAY POSTS
// =========================

function displayPosts(data) {

    const container =
        document.getElementById("posts");

    container.innerHTML = "";

    if (data.length === 0) {

        container.innerHTML =
            "<div class='card'>No news found.</div>";

        return;
    }

    data.forEach(post => {

        const card =
            document.createElement("div");

        card.className = "card";

        card.innerHTML = `

            <h3 onclick="showFullNews(${post.id})">
                ${escapeHtml(post.title)}
            </h3>

            <div class="meta">
                ${escapeHtml(post.source)}
            </div>

            <span class="category">
                ${escapeHtml(post.category)}
            </span>

            <span class="sentiment">
                ${escapeHtml(post.sentiment)}
            </span>

            <p>
                ${escapeHtml(
                    post.content.substring(0, 160)
                )}...
            </p>

            <button onclick="showFullNews(${post.id})">
                Read Full News
            </button>

        `;

        container.appendChild(card);
    });
}


// =========================
// SEARCH
// =========================

async function searchNews() {

    const query =
        document.getElementById("searchInput").value.trim();

    if (query === "") {

        displayPosts(allPosts);
        return;
    }

    const response =
        await fetch(
            "/api/search?q=" +
            encodeURIComponent(query)
        );

    const results =
        await response.json();

    displayPosts(results);
}


// =========================
// SHOW FULL NEWS
// =========================

function showFullNews(id) {

    const post =
        allPosts.find(p => p.id === id);

    if (!post)
        return;

    document.getElementById("modalTitle")
        .innerText = post.title;

    document.getElementById("modalMeta")
        .innerText =
        post.category +
        " | " +
        post.source +
        " | Sentiment: " +
        post.sentiment;

    document.getElementById("modalContent")
        .innerText = post.content;

    selectedUrl = post.url;

    const btn =
        document.getElementById("openNewsButton");

    if (selectedUrl && selectedUrl.trim() !== "") {

        btn.style.display = "inline-block";

    } else {

        btn.style.display = "none";
    }

    document.getElementById("modal")
        .style.display = "flex";
}


// =========================
// OPEN ORIGINAL NEWS
// =========================

function openOriginalNews() {

    if (selectedUrl) {

        window.open(
            selectedUrl,
            "_blank"
        );

    }
}


// =========================
// CLOSE MODAL
// =========================

function closeModal() {

    document.getElementById("modal")
        .style.display = "none";
}


function closeOutside(event) {

    if (event.target.id === "modal") {

        closeModal();

    }
}


// =========================
// ADD NEWS
// =========================

async function addNews() {

    const title =
        document.getElementById("title").value;

    const category =
        document.getElementById("category").value;

    const source =
        document.getElementById("source").value;

    const content =
        document.getElementById("content").value;

    const url =
        document.getElementById("url").value;


    if (!title ||
        !category ||
        !source ||
        !content) {

        alert(
            "Please fill Title, Category, Source and Content."
        );

        return;
    }


    const data =

        "title=" +
        encodeURIComponent(title) +

        "&category=" +
        encodeURIComponent(category) +

        "&source=" +
        encodeURIComponent(source) +

        "&content=" +
        encodeURIComponent(content) +

        "&url=" +
        encodeURIComponent(url);


    await fetch(
        "/api/add",
        {
            method: "POST",

            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded"
            },

            body: data
        }
    );


    document.getElementById("title").value = "";
    document.getElementById("category").value = "";
    document.getElementById("source").value = "";
    document.getElementById("content").value = "";
    document.getElementById("url").value = "";


    alert("News post added successfully!");

    loadPosts();
}


// =========================
// STATS
// =========================

async function loadStats() {

    const response =
        await fetch("/api/stats");

    const data =
        await response.json();


    document.getElementById("totalPosts")
        .innerText = data.total;

    document.getElementById("positive")
        .innerText = data.positive;

    document.getElementById("negative")
        .innerText = data.negative;

    document.getElementById("neutral")
        .innerText = data.neutral;
}


// =========================
// TRENDING TOPICS
// =========================

async function loadTrending() {

    const response =
        await fetch("/api/trending");

    const data =
        await response.json();


    const container =
        document.getElementById("trending");

    container.innerHTML = "";


    data.forEach(item => {

        const div =
            document.createElement("div");

        div.className =
            "trend-item";

        div.innerHTML =

            "<span>" +
            escapeHtml(item.keyword) +
            "</span>" +

            "<b>" +
            item.count +
            "</b>";

        container.appendChild(div);

    });
}


// =========================
// HTML ESCAPE
// =========================

function escapeHtml(text) {

    return String(text)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


// INITIAL LOAD

loadPosts();

</script>

</body>
</html>
""";

        sendResponse(exchange, html, "text/html");
    }


    // =========================
    // GET ALL POSTS
    // =========================
    static void getPosts(HttpExchange exchange)
            throws IOException {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < posts.size(); i++) {

            if (i > 0)
                json.append(",");

            json.append(postToJson(posts.get(i)));
        }

        json.append("]");

        sendResponse(
                exchange,
                json.toString(),
                "application/json");
    }


    // =========================
    // ADD POST
    // =========================
    static void addPost(HttpExchange exchange)
            throws IOException {

        String body =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);

        Map<String, String> data =
                parseForm(body);

        String title =
                data.getOrDefault("title", "");

        String category =
                data.getOrDefault("category", "");

        String source =
                data.getOrDefault("source", "");

        String content =
                data.getOrDefault("content", "");

        String url =
                data.getOrDefault("url", "");


        Post post =
                new Post(
                        nextId++,
                        title,
                        category,
                        source,
                        content,
                        url
                );


        posts.add(post);

        rebuildKeywords();


        sendResponse(
                exchange,
                "{\"success\":true}",
                "application/json");
    }


    // =========================
    // SEARCH
    // =========================
    static void searchPosts(HttpExchange exchange)
            throws IOException {

        String query =
                getQueryParam(exchange, "q");

        query =
                query == null
                        ? ""
                        : query.toLowerCase();


        ArrayList<Post> results =
                new ArrayList<>();


        // Linear Searching
        for (Post post : posts) {

            if (
                post.title.toLowerCase().contains(query)
                ||
                post.category.toLowerCase().contains(query)
                ||
                post.source.toLowerCase().contains(query)
                ||
                post.content.toLowerCase().contains(query)
            ) {

                results.add(post);
            }
        }


        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < results.size(); i++) {

            if (i > 0)
                json.append(",");

            json.append(
                    postToJson(results.get(i))
            );
        }

        json.append("]");


        sendResponse(
                exchange,
                json.toString(),
                "application/json");
    }


    // =========================
    // TRENDING TOPICS
    // =========================
    static void trending(HttpExchange exchange)
            throws IOException {

        ArrayList<Map.Entry<String, Integer>> list =
                new ArrayList<>(
                        keywordFrequency.entrySet()
                );


        // Sorting
        list.sort(
                (a, b) ->
                        Integer.compare(
                                b.getValue(),
                                a.getValue()
                        )
        );


        StringBuilder json =
                new StringBuilder("[");


        int limit =
                Math.min(10, list.size());


        for (int i = 0; i < limit; i++) {

            if (i > 0)
                json.append(",");


            Map.Entry<String, Integer> entry =
                    list.get(i);


            json.append("{");

            json.append("\"keyword\":\"")
                    .append(jsonEscape(entry.getKey()))
                    .append("\",");

            json.append("\"count\":")
                    .append(entry.getValue());

            json.append("}");
        }


        json.append("]");


        sendResponse(
                exchange,
                json.toString(),
                "application/json");
    }


    // =========================
    // STATISTICS
    // =========================
    static void stats(HttpExchange exchange)
            throws IOException {

        int positive = 0;
        int negative = 0;
        int neutral = 0;


        for (Post post : posts) {

            if (post.sentiment.equals("Positive"))
                positive++;

            else if (post.sentiment.equals("Negative"))
                negative++;

            else
                neutral++;
        }


        String json =
                "{"
                + "\"total\":" + posts.size() + ","
                + "\"positive\":" + positive + ","
                + "\"negative\":" + negative + ","
                + "\"neutral\":" + neutral
                + "}";


        sendResponse(
                exchange,
                json,
                "application/json");
    }


    // =========================
    // KEYWORD HASHMAP
    // =========================
    static void rebuildKeywords() {

        keywordFrequency.clear();


        for (Post post : posts) {

            String text =
                    post.title + " "
                    + post.category + " "
                    + post.content;


            String[] words =
                    text.toLowerCase()
                            .replaceAll("[^a-zA-Z ]", " ")
                            .split("\\s+");


            for (String word : words) {

                if (word.length() < 4)
                    continue;


                if (
                    word.equals("this") ||
                    word.equals("that") ||
                    word.equals("with") ||
                    word.equals("from") ||
                    word.equals("news") ||
                    word.equals("have") ||
                    word.equals("been") ||
                    word.equals("will") ||
                    word.equals("they") ||
                    word.equals("their")
                )
                    continue;


                keywordFrequency.put(
                        word,
                        keywordFrequency.getOrDefault(word, 0) + 1
                );
            }
        }
    }


    // =========================
    // SENTIMENT ANALYSIS
    // =========================
    static String analyzeSentiment(String text) {

        String lower =
                text.toLowerCase();


        String[] positiveWords = {
                "good",
                "great",
                "excellent",
                "success",
                "successful",
                "win",
                "wins",
                "positive",
                "happy",
                "best",
                "strong",
                "benefit",
                "improve",
                "improvement"
        };


        String[] negativeWords = {
                "bad",
                "worst",
                "failure",
                "failed",
                "negative",
                "loss",
                "lose",
                "problem",
                "danger",
                "poor",
                "damage",
                "crisis",
                "pollution"
        };


        int positive = 0;
        int negative = 0;


        for (String word : positiveWords) {

            if (lower.contains(word))
                positive++;
        }


        for (String word : negativeWords) {

            if (lower.contains(word))
                negative++;
        }


        if (positive > negative)
            return "Positive";

        if (negative > positive)
            return "Negative";

        return "Neutral";
    }


    // =========================
    // JSON CONVERSION
    // =========================
    static String postToJson(Post post) {

        return "{"

                + "\"id\":"
                + post.id
                + ","

                + "\"title\":\""
                + jsonEscape(post.title)
                + "\","

                + "\"category\":\""
                + jsonEscape(post.category)
                + "\","

                + "\"source\":\""
                + jsonEscape(post.source)
                + "\","

                + "\"content\":\""
                + jsonEscape(post.content)
                + "\","

                + "\"url\":\""
                + jsonEscape(post.url)
                + "\","

                + "\"sentiment\":\""
                + jsonEscape(post.sentiment)
                + "\""

                + "}";
    }


    // =========================
    // FORM PARSER
    // =========================
    static Map<String, String> parseForm(String body) {

        Map<String, String> map =
                new HashMap<>();


        if (body == null || body.isEmpty())
            return map;


        String[] pairs =
                body.split("&");


        for (String pair : pairs) {

            String[] parts =
                    pair.split("=", 2);


            if (parts.length == 2) {

                try {

                    String key =
                            URLDecoder.decode(
                                    parts[0],
                                    StandardCharsets.UTF_8
                            );

                    String value =
                            URLDecoder.decode(
                                    parts[1],
                                    StandardCharsets.UTF_8
                            );

                    map.put(key, value);

                } catch (Exception ignored) {
                }
            }
        }


        return map;
    }


    // =========================
    // QUERY PARAMETER
    // =========================
    static String getQueryParam(
            HttpExchange exchange,
            String parameter) {

        String query =
                exchange.getRequestURI()
                        .getRawQuery();


        if (query == null)
            return null;


        for (String pair :
                query.split("&")) {

            String[] parts =
                    pair.split("=", 2);


            if (parts.length == 2 &&
                parts[0].equals(parameter)) {

                try {

                    return URLDecoder.decode(
                            parts[1],
                            StandardCharsets.UTF_8
                    );

                } catch (Exception e) {

                    return parts[1];
                }
            }
        }


        return null;
    }


    // =========================
    // JSON ESCAPE
    // =========================
    static String jsonEscape(String text) {

        if (text == null)
            return "";


        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    // =========================
    // SEND RESPONSE
    // =========================
    static void sendResponse(
            HttpExchange exchange,
            String response,
            String contentType)
            throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8);


        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType
                                + "; charset=UTF-8"
                );


        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );


        exchange.sendResponseHeaders(
                200,
                bytes.length
        );


        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(bytes);
        }
    }
}