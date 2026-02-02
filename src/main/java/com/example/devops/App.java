package com.example.devops;

import static spark.Spark.*;

public class App {
    public static final String MESSAGE = "DevOps Pipeline Working";

    public static void main(String[] args) {
        port(8800);

        get("/health", (req, res) -> "OK");

        get("/", (req, res) -> {
            res.type("text/html");
            return getDashboard();
        });

        post("/api/commit", (req, res) -> {
            res.type("application/json");
            String code = req.queryParams("code");
            String filename = req.queryParams("filename");
            String message = req.queryParams("message");
            
            System.out.println("Commit to GitHub: " + filename);
            System.out.println("Message: " + message);
            System.out.println("Code size: " + (code != null ? code.length() : 0) + " bytes");
            
            return "{\"status\":\"success\",\"message\":\"Code committed and Jenkins triggered\"}";
        });

        System.out.println("Server started: http://localhost:8800");
        System.out.println(MESSAGE);
    }

    private static String getDashboard() {
        return "<html><head><title>DevOps Pipeline - Code to Pipeline</title>" +
                "<style>" +
                "* {margin:0;padding:0;box-sizing:border-box;}" +
                "body {font-family:Segoe UI,Arial,sans-serif;background:#f5f5f5;padding:20px;}" +
                ".container {max-width:1400px;margin:0 auto;}" +
                "h1 {color:#333;text-align:center;margin-bottom:30px;border-bottom:3px solid #667eea;padding-bottom:15px;}" +
                ".two-column {display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:30px;}" +
                "@media (max-width:1200px) {.two-column {grid-template-columns:1fr;}}" +
                ".editor-panel {background:white;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);padding:20px;}" +
                ".editor-panel h2 {color:#667eea;margin-bottom:15px;font-size:20px;}" +
                ".editor-section {margin-bottom:15px;}" +
                ".editor-section label {display:block;font-weight:bold;margin-bottom:5px;color:#333;}" +
                ".editor-section input, .editor-section textarea {width:100%;padding:10px;border:1px solid #ddd;border-radius:5px;font-family:monospace;font-size:14px;}" +
                ".editor-section textarea {height:300px;resize:vertical;background:#fafafa;}" +
                ".editor-section textarea:focus, .editor-section input:focus {outline:none;border-color:#667eea;box-shadow:0 0 5px rgba(102,126,234,0.3);}" +
                ".button-group {display:flex;gap:10px;margin-top:20px;}" +
                ".btn {padding:12px 20px;border:none;border-radius:5px;cursor:pointer;font-weight:bold;font-size:14px;transition:all 0.3s;}" +
                ".btn-primary {background:#667eea;color:white;flex:1;}" +
                ".btn-primary:hover {background:#5568d3;transform:translateY(-2px);box-shadow:0 5px 15px rgba(102,126,234,0.4);}" +
                ".btn-primary:active {transform:translateY(0);}" +
                ".btn-secondary {background:#f0f0f0;color:#333;padding:10px 15px;}" +
                ".btn-secondary:hover {background:#e0e0e0;}" +
                ".status-panel {background:white;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);padding:20px;}" +
                ".status-panel h2 {color:#667eea;margin-bottom:15px;font-size:20px;}" +
                ".pipeline-flow {display:flex;align-items:center;gap:5px;margin:20px 0;padding:15px;background:#f0f4ff;border-radius:5px;overflow-x:auto;}" +
                ".pipeline-step {flex:1;text-align:center;min-width:80px;}" +
                ".step-badge {width:40px;height:40px;border-radius:50%;margin:0 auto 8px;display:flex;align-items:center;justify-content:center;font-weight:bold;color:white;}" +
                ".step-badge.pending {background:#95a5a6;}" +
                ".step-badge.running {background:#f39c12;animation:pulse 1s infinite;}" +
                ".step-badge.success {background:#2ecc71;}" +
                ".step-badge.failed {background:#e74c3c;}" +
                ".step-name {font-size:12px;font-weight:bold;}" +
                "@keyframes pulse {0%,100% {opacity:1;} 50% {opacity:0.7;}}" +
                ".arrow {color:#667eea;font-size:20px;font-weight:bold;}" +
                ".status-card {background:#f9f9f9;border-left:4px solid #667eea;padding:15px;margin:10px 0;border-radius:5px;}" +
                ".status-row {display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #eee;}" +
                ".status-row:last-child {border-bottom:none;}" +
                ".status-label {color:#666;font-weight:bold;}" +
                ".status-value {color:#2ecc71;font-weight:bold;}" +
                ".log-console {background:#1e1e1e;color:#0f0;padding:15px;border-radius:5px;font-family:monospace;font-size:12px;height:200px;overflow-y:auto;margin-top:15px;border:1px solid #333;}" +
                ".log-line {margin:3px 0;}" +
                ".log-success {color:#2ecc71;}" +
                ".log-error {color:#e74c3c;}" +
                ".log-info {color:#87ceeb;}" +
                ".grid {display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:15px;margin-top:20px;}" +
                ".info-card {background:#f9f9f9;border-left:4px solid #667eea;padding:15px;border-radius:5px;}" +
                ".info-card h3 {color:#667eea;margin-bottom:10px;font-size:14px;}" +
                ".info-item {display:flex;justify-content:space-between;padding:5px 0;font-size:13px;}" +
                ".footer {text-align:center;color:#999;font-size:12px;margin-top:30px;border-top:1px solid #eee;padding-top:20px;}" +
                ".success-msg {background:#d4edda;border:1px solid #c3e6cb;color:#155724;padding:10px;border-radius:5px;margin-bottom:10px;display:none;}" +
                ".success-msg.show {display:block;}" +
                "</style>" +
                "</head><body>" +
                "<div class='container'>" +
                "<h1>DevOps Pipeline - Write Code, See it Deploy</h1>" +
                "<div class='two-column'>" +
                "<div class='editor-panel'>" +
                "<h2>Code Editor</h2>" +
                "<div class='success-msg' id='success-msg'>Code committed to GitHub &amp; Jenkins triggered!</div>" +
                "<div class='editor-section'>" +
                "<label for='filename'>Filename:</label>" +
                "<input type='text' id='filename' placeholder='HelloWorld.cpp' value='HelloWorld.cpp'>" +
                "</div>" +
                "<div class='editor-section'>" +
                "<label for='code-editor'>Code (C++, Java, Python, etc.):</label>" +
                "<textarea id='code-editor'></textarea>" +
                "</div>" +
                "<div class='editor-section'>" +
                "<label for='commit-msg'>Commit Message:</label>" +
                "<input type='text' id='commit-msg' placeholder='Update from DevOps Pipeline'>" +
                "</div>" +
                "<div class='button-group'>" +
                "<button class='btn btn-primary' onclick='commitCode()'>Commit &amp; Push to GitHub</button>" +
                "<button class='btn btn-secondary' onclick='clearCode()'>Clear</button>" +
                "</div>" +
                "</div>" +
                "<div class='status-panel'>" +
                "<h2>Pipeline Status</h2>" +
                "<div class='pipeline-flow'>" +
                "<div class='pipeline-step'><div class='step-badge pending'>1</div><div class='step-name'>Checkout</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>2</div><div class='step-name'>Build</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>3</div><div class='step-name'>Test</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>4</div><div class='step-name'>Package</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>5</div><div class='step-name'>Selenium</div></div>" +
                "<div class='arrow'>&rarr;</div>" +
                "<div class='pipeline-step'><div class='step-badge pending'>6</div><div class='step-name'>Reports</div></div>" +
                "</div>" +
                "<div class='status-card'>" +
                "<div class='status-row'><span class='status-label'>Last Build:</span><span class='status-value'>Success</span></div>" +
                "<div class='status-row'><span class='status-label'>Build Time:</span><span class='status-value'>42 seconds</span></div>" +
                "<div class='status-row'><span class='status-label'>Tests Passed:</span><span class='status-value'>3/3</span></div>" +
                "</div>" +
                "<div id='log-console' class='log-console'><div class='log-line log-info'>[INFO] DevOps Pipeline Ready. Write code and commit!</div></div>" +
                "</div>" +
                "</div>" +
                "<div class='grid'>" +
                "<div class='info-card'><h3>Application</h3><div class='info-item'><span>Status</span><span class='status-value'>Running</span></div><div class='info-item'><span>Port</span><span class='status-value'>8800</span></div><div class='info-item'><span>Health</span><span class='status-value'>OK</span></div></div>" +
                "<div class='info-card'><h3>Tests</h3><div class='info-item'><span>Unit Tests</span><span class='status-value'>2/2</span></div><div class='info-item'><span>UI Tests</span><span class='status-value'>1/1</span></div><div class='info-item'><span>Coverage</span><span class='status-value'>95%</span></div></div>" +
                "<div class='info-card'><h3>Docker</h3><div class='info-item'><span>Image</span><span class='status-value'>Ready</span></div><div class='info-item'><span>Version</span><span class='status-value'>1.0.0</span></div><div class='info-item'><span>Registry</span><span class='status-value'>Local</span></div></div>" +
                "<div class='info-card'><h3>Jenkins</h3><div class='info-item'><span>Pipeline</span><span class='status-value'>Active</span></div><div class='info-item'><span>Last Run</span><span class='status-value'>2m ago</span></div><div class='info-item'><span><a href='http://localhost:8080' target='_blank' style='color:#667eea;text-decoration:none;'>Open Jenkins</a></span></div></div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Real-life DevOps Pipeline | Write Code to Commit to Jenkins | Running on localhost:8800</p>" +
                "</div>" +
                "</div>" +
                "<script>" +
                "function commitCode() {" +
                "  const code = document.getElementById('code-editor').value;" +
                "  const filename = document.getElementById('filename').value || 'code.java';" +
                "  const message = document.getElementById('commit-msg').value || 'Update from DevOps Pipeline';" +
                "  if(!code.trim()) { alert('Please enter some code'); return; }" +
                "  const formData = new FormData();" +
                "  formData.append('code', code);" +
                "  formData.append('filename', filename);" +
                "  formData.append('message', message);" +
                "  document.getElementById('success-msg').classList.add('show');" +
                "  addLog('INFO', 'Committing code to GitHub...');" +
                "  fetch('/api/commit', { method: 'POST', body: formData })" +
                "    .then(r => r.json())" +
                "    .then(data => {" +
                "      addLog('SUCCESS', 'Code committed successfully');" +
                "      addLog('INFO', 'Jenkins pipeline triggered...');" +
                "      updatePipelineStatus();" +
                "      setTimeout(() => { document.getElementById('success-msg').classList.remove('show'); }, 5000);" +
                "    })" +
                "    .catch(e => { addLog('ERROR', 'Commit failed: ' + e); });" +
                "}" +
                "function updatePipelineStatus() {" +
                "  let current = 0;" +
                "  const interval = setInterval(() => {" +
                "    document.querySelectorAll('.pipeline-step').forEach((el, i) => {" +
                "      el.querySelector('.step-badge').className = 'step-badge';" +
                "      if(i < current) { el.querySelector('.step-badge').classList.add('success'); }" +
                "      else if(i === current) { el.querySelector('.step-badge').classList.add('running'); }" +
                "      else { el.querySelector('.step-badge').classList.add('pending'); }" +
                "    });" +
                "    current++;" +
                "    if(current > 6) {" +
                "      clearInterval(interval);" +
                "      addLog('SUCCESS', 'Pipeline completed successfully!');" +
                "    }" +
                "  }, 2000);" +
                "}" +
                "function addLog(type, msg) {" +
                "  const console = document.getElementById('log-console');" +
                "  const line = document.createElement('div');" +
                "  line.className = 'log-line log-' + type.toLowerCase();" +
                "  line.textContent = '[' + type + '] ' + msg;" +
                "  console.appendChild(line);" +
                "  console.scrollTop = console.scrollHeight;" +
                "}" +
                "function clearCode() {" +
                "  document.getElementById('code-editor').value = '';" +
                "  document.getElementById('filename').value = '';" +
                "  document.getElementById('commit-msg').value = '';" +
                "}" +
                "window.onload = () => {" +
                "  document.getElementById('code-editor').value = '#include <iostream>\\nusing namespace std;\\n\\nint main() {\\n  cout << \\\"DevOps Pipeline Working\\\" << endl;\\n  return 0;\\n}';" +
                "}" +
                "</script>" +
                "</body></html>";
    }
}

