console.log("GitHub Code Analyzer Loaded");

const API_URL = "http://127.0.0.1:8080/api/code/analyze";

// ✅ Cache
const codeCache = new Map();

let analyzeBtn;

createAnalyzeButton();

// ✅ Prevent duplicate button
function createAnalyzeButton() {

    if (document.getElementById("ai-analyze-btn")) return;

    analyzeBtn = document.createElement("button");
    analyzeBtn.id = "ai-analyze-btn";
    analyzeBtn.innerText = "Analyze Code";

    Object.assign(analyzeBtn.style, {
        position: "fixed",
        top: "120px",
        right: "20px",
        zIndex: "9999",
        padding: "10px 15px",
        background: "#2ea44f",
        color: "white",
        border: "none",
        borderRadius: "6px",
        cursor: "pointer"
    });

    analyzeBtn.onclick = analyzeCode;

    document.body.appendChild(analyzeBtn);
}

// ✅ MAIN FUNCTION
async function analyzeCode() {

    const code = extractCode();

    if (!code) {
        alert("No code found on this page");
        return;
    }

    const cacheKey = hashCode(code);

    // ⚡ CACHE HIT
    if (codeCache.has(cacheKey)) {
        showSidePanel(codeCache.get(cacheKey));
        return;
    }

    // 🔥 Loading UI
    analyzeBtn.innerHTML = `Analyzing <span class="loading-spinner"></span>`;
    analyzeBtn.disabled = true;
    analyzeBtn.style.opacity = "0.7";

    showSidePanel("⏳ Analyzing your code...");

    try {

        console.log("Calling API:", API_URL);

        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ code })
        });

        if (!response.ok) {
            throw new Error("Server error: " + response.status);
        }

        const result = await response.text();

        // ✅ Cache save
        codeCache.set(cacheKey, result);

        showSidePanel(result);

    } catch (error) {

        console.error("API Error:", error);

        showSidePanel(
            "❌ Failed to analyze code.\n\n" +
            "Check:\n" +
            "1. Backend running\n" +
            "2. CORS enabled\n" +
            "3. Port = 8080"
        );

    } finally {

        analyzeBtn.innerText = "Analyze Code";
        analyzeBtn.disabled = false;
        analyzeBtn.style.opacity = "1";
    }
}

// ✅ BETTER CODE EXTRACTION
function extractCode() {

    let code = "";

    // New GitHub UI
    const codeLines = document.querySelectorAll("table tr td.blob-code");

    if (codeLines.length > 0) {
        codeLines.forEach(line => {
            code += line.innerText + "\n";
        });
    }

    // Fallback
    if (!code) {
        const altLines = document.querySelectorAll(".react-code-text");
        altLines.forEach(line => {
            code += line.innerText + "\n";
        });
    }

    return code.substring(0, 3000);
}

// ✅ HASH FUNCTION
function hashCode(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = (hash << 5) - hash + str.charCodeAt(i);
        hash |= 0;
    }
    return hash.toString();
}

// ✅ PANEL UI
function showSidePanel(text) {

    const existingPanel = document.getElementById("ai-code-panel");
    if (existingPanel) existingPanel.remove();

    const panel = document.createElement("div");
    panel.id = "ai-code-panel";

    panel.innerHTML = `
        <div class="panel-header">
            <h2>AI Code Analyzer</h2>
            <button id="close-panel">X</button>
        </div>

        <div class="panel-content">
            <pre>${text}</pre>

            <p class="footer-text">
            🚀 Like this tool? Share with friends!
            </p>
        </div>
    `;

    document.body.appendChild(panel);

    document.getElementById("close-panel")
        .onclick = () => panel.remove();
}